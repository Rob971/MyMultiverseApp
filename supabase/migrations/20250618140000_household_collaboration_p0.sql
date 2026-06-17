-- P0 household collaboration: one active affiliation, leave/dissolve, pending invites
-- always by email, 20-member cap.

alter table public.space_members
    add column if not exists left_at timestamptz;

create or replace function public.household_active_member_count(p_space_id uuid)
returns integer
language sql
stable
security definer
set search_path = public
as $$
    select 1 + coalesce(
        (
            select count(*)::integer
            from public.space_members sm
            join public.sharing_spaces s on s.id = p_space_id
            where sm.space_id = p_space_id
              and sm.user_id is not null
              and sm.left_at is null
              and sm.user_id <> s.owner_id
        ),
        0
    );
$$;

create or replace function public.resolve_user_household_row()
returns table (
    space_id uuid,
    space_name text,
    owner_id uuid,
    owner_display_name text,
    member_role text
)
language plpgsql
stable
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    return query
    select
        s.id,
        s.name,
        s.owner_id,
        coalesce(nullif(trim(p.display_name), ''), nullif(trim(p.email), ''), '')::text,
        case
            when s.owner_id = v_user_id then 'owner'
            else coalesce(sm.role::text, 'editor')
        end
    from public.sharing_spaces s
    join public.profiles p on p.id = s.owner_id
    left join public.space_members sm
        on sm.space_id = s.id
       and sm.user_id = v_user_id
       and sm.left_at is null
    where s.topic = 'nutrition'
      and (
          s.owner_id = v_user_id
          or sm.user_id is not null
          or exists (
              select 1
              from public.space_members sm2
              join public.group_members gm on gm.group_id = sm2.group_id
              where sm2.space_id = s.id
                and sm2.left_at is null
                and gm.user_id = v_user_id
          )
      )
    order by (s.owner_id = v_user_id) desc, s.created_at
    limit 1;
end;
$$;

create or replace function public.user_has_active_nutrition_household(
    p_user_id uuid,
    p_exclude_space_id uuid default null
)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select exists (
        select 1
        from public.sharing_spaces s
        where s.topic = 'nutrition'
          and (p_exclude_space_id is null or s.id <> p_exclude_space_id)
          and (
              s.owner_id = p_user_id
              or exists (
                  select 1
                  from public.space_members sm
                  where sm.space_id = s.id
                    and sm.user_id = p_user_id
                    and sm.left_at is null
              )
          )
    );
$$;

create unique index if not exists sharing_spaces_one_nutrition_owner_idx
    on public.sharing_spaces (owner_id)
    where topic = 'nutrition';

create unique index if not exists space_members_one_active_per_user_idx
    on public.space_members (user_id)
    where user_id is not null and left_at is null;

create or replace function public.list_my_pending_space_invites()
returns json
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_profile_email text;
    v_result json;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    perform public.ensure_current_profile();

    select lower(trim(coalesce(p.email, auth.jwt() ->> 'email', '')))
    into v_profile_email
    from public.profiles p
    where p.id = v_user_id;

    if v_profile_email is null or char_length(v_profile_email) = 0 then
        return '[]'::json;
    end if;

    select coalesce(
        json_agg(
            json_build_object(
                'id', i.id,
                'space_id', i.space_id,
                'space_name', s.name,
                'email', i.email,
                'role', i.role::text,
                'expires_at', i.expires_at
            )
            order by s.name
        ),
        '[]'::json
    )
    into v_result
    from public.space_invites i
    join public.sharing_spaces s on s.id = i.space_id
    where lower(trim(i.email)) = v_profile_email
      and i.accepted_at is null
      and i.declined_at is null
      and i.expires_at > now();

    return v_result;
end;
$$;

create or replace function public.invite_space_member(
    p_space_id uuid,
    p_email text,
    p_role public.space_member_role default 'editor'
)
returns json
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_normalized_email text := lower(trim(p_email));
    v_profile_id uuid;
    v_profile_email text;
    v_invite_id uuid;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    if char_length(v_normalized_email) = 0 then
        raise exception 'member_email_required';
    end if;

    if p_role = 'owner' then
        raise exception 'cannot_invite_as_owner';
    end if;

    if public.household_active_member_count(p_space_id) >= 20 then
        raise exception 'household_member_limit_reached';
    end if;

    perform public.ensure_current_profile();

    if not public.is_space_owner(p_space_id) then
        raise exception 'insufficient_role';
    end if;

    select lower(trim(coalesce(p.email, auth.jwt() ->> 'email', '')))
    into v_profile_email
    from public.profiles p
    where p.id = v_user_id;

    if v_normalized_email = v_profile_email then
        raise exception 'member_cannot_add_self';
    end if;

    select id
    into v_profile_id
    from public.profiles
    where lower(trim(coalesce(email, ''))) = v_normalized_email
    limit 1;

    if v_profile_id is not null then
        if exists (
            select 1
            from public.sharing_spaces s
            where s.id = p_space_id
              and (
                  s.owner_id = v_profile_id
                  or exists (
                      select 1
                      from public.space_members sm
                      where sm.space_id = p_space_id
                        and sm.user_id = v_profile_id
                        and sm.left_at is null
                  )
              )
        ) then
            raise exception 'member_already_exists';
        end if;

        if public.user_has_active_nutrition_household(v_profile_id, p_space_id) then
            raise exception 'invitee_household_already_active';
        end if;
    end if;

    select id
    into v_invite_id
    from public.space_invites
    where space_id = p_space_id
      and lower(trim(email)) = v_normalized_email
      and accepted_at is null
      and declined_at is null
    limit 1;

    if v_invite_id is not null then
        update public.space_invites
        set role = p_role,
            invited_by = v_user_id,
            expires_at = now() + interval '14 days'
        where id = v_invite_id;
    else
        insert into public.space_invites (space_id, email, role, invited_by)
        values (p_space_id, v_normalized_email, p_role, v_user_id);
    end if;

    return json_build_object('result', 'invited');
end;
$$;

create or replace function public.accept_space_invite(p_invite_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_invite public.space_invites%rowtype;
    v_profile_id uuid := auth.uid();
    v_profile_email text;
begin
    if v_profile_id is null then
        raise exception 'auth_required';
    end if;

    perform public.ensure_current_profile();

    if public.user_has_active_nutrition_household(v_profile_id, null) then
        raise exception 'household_already_active';
    end if;

    select lower(trim(coalesce(p.email, auth.jwt() ->> 'email', '')))
    into v_profile_email
    from public.profiles p
    where p.id = v_profile_id;

    if v_profile_email is null or char_length(v_profile_email) = 0 then
        raise exception 'profile_email_required';
    end if;

    select * into v_invite
    from public.space_invites
    where id = p_invite_id
      and accepted_at is null
      and declined_at is null
      and expires_at > now()
    for update;

    if not found then
        raise exception 'invite_not_found';
    end if;

    if lower(trim(v_invite.email)) <> v_profile_email then
        raise exception 'invite_email_mismatch';
    end if;

    if public.household_active_member_count(v_invite.space_id) >= 20 then
        raise exception 'household_member_limit_reached';
    end if;

    insert into public.space_members (space_id, user_id, role)
    values (v_invite.space_id, v_profile_id, v_invite.role);

    update public.space_invites
    set accepted_at = now()
    where id = p_invite_id;
end;
$$;

create or replace function public.leave_household()
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_rows integer;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    if exists (
        select 1
        from public.sharing_spaces s
        where s.topic = 'nutrition'
          and s.owner_id = v_user_id
    ) then
        raise exception 'owner_must_transfer_or_dissolve';
    end if;

    update public.space_members
    set left_at = now()
    where user_id = v_user_id
      and left_at is null;

    get diagnostics v_rows = row_count;
    if v_rows = 0 then
        raise exception 'household_not_found';
    end if;
end;
$$;

create or replace function public.dissolve_household()
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_space_id uuid;
    v_member_count integer;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    select s.id
    into v_space_id
    from public.sharing_spaces s
    where s.topic = 'nutrition'
      and s.owner_id = v_user_id
    limit 1;

    if v_space_id is null then
        raise exception 'household_not_found';
    end if;

    select count(*)::integer
    into v_member_count
    from public.space_members sm
    where sm.space_id = v_space_id
      and sm.left_at is null
      and sm.user_id is not null
      and sm.user_id <> v_user_id;

    if v_member_count > 0 then
        raise exception 'owner_must_transfer_or_dissolve';
    end if;

    delete from public.sharing_spaces
    where id = v_space_id;
end;
$$;

revoke all on function public.household_active_member_count(uuid) from public;
grant execute on function public.household_active_member_count(uuid) to authenticated;

revoke all on function public.leave_household() from public;
grant execute on function public.leave_household() to authenticated;

revoke all on function public.dissolve_household() from public;
grant execute on function public.dissolve_household() to authenticated;
