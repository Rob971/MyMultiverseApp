-- Complete household terminology: rename enum/helpers/RPCs; household_id JSON keys; drop space_* aliases.

alter type public.space_member_role rename to household_member_role;

alter index if exists sharing_spaces_one_nutrition_owner_idx rename to households_one_nutrition_owner_idx;
alter index if exists space_members_one_active_per_user_idx rename to household_members_one_active_per_user_idx;

create or replace function public.is_household_owner(p_household_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select exists (
        select 1
        from public.households s
        where s.id = p_household_id
          and s.owner_id = (select auth.uid())
    );
$$;

create or replace function public.is_household_member(p_household_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select public.is_household_owner(p_household_id)
        or exists (
            select 1
            from public.household_members sm
            where sm.household_id = p_household_id
              and sm.user_id = (select auth.uid())
              and sm.left_at is null
        )
        or exists (
            select 1
            from public.household_members sm
            join public.group_members gm on gm.group_id = sm.group_id
            where sm.household_id = p_household_id
              and sm.left_at is null
              and gm.user_id = (select auth.uid())
        );
$$;

create or replace function public.household_member_can_write_nutrition(p_household_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select public.is_household_owner(p_household_id)
        or exists (
            select 1
            from public.household_members sm
            where sm.household_id = p_household_id
              and sm.user_id = (select auth.uid())
              and sm.left_at is null
              and sm.role = 'editor'
        );
$$;

drop function if exists public.resolve_user_household_row() cascade;

create function public.resolve_user_household_row()
returns table (
    household_id uuid,
    household_name text,
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
    from public.households s
    join public.profiles p on p.id = s.owner_id
    left join public.household_members sm
        on sm.household_id = s.id
       and sm.user_id = v_user_id
       and sm.left_at is null
    where s.topic = 'nutrition'
      and (
          s.owner_id = v_user_id
          or sm.user_id is not null
          or exists (
              select 1
              from public.household_members sm2
              join public.group_members gm on gm.group_id = sm2.group_id
              where sm2.household_id = s.id
                and sm2.left_at is null
                and gm.user_id = v_user_id
          )
      )
    order by (s.owner_id = v_user_id) desc, s.created_at
    limit 1;
end;
$$;

revoke all on function public.resolve_user_household_row() from public;
grant execute on function public.resolve_user_household_row() to authenticated;

create or replace function public.household_membership_status()
returns json
language plpgsql
security definer
set search_path = public
as $$
declare
    v_row record;
    v_features json;
begin
    perform public.ensure_current_profile();

    select * into v_row
    from public.resolve_user_household_row();

    if not found then
        return json_build_object('status', 'none');
    end if;

    select coalesce(json_agg(feature order by feature), '[]'::json)
    into v_features
    from public.household_modules
    where household_id = v_row.household_id;

    return json_build_object(
        'status', 'active',
        'household_id', v_row.household_id,
        'household_name', v_row.household_name,
        'owner_id', v_row.owner_id,
        'owner_display_name', v_row.owner_display_name,
        'role', v_row.member_role,
        'features', v_features
    );
end;
$$;

create or replace function public.create_household(p_name text)
returns json
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_existing record;
    v_household_id uuid;
    v_household_name text;
    v_owner_id uuid;
    v_owner_display_name text;
    v_features json;
    v_trimmed_name text := trim(p_name);
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    if char_length(v_trimmed_name) = 0 then
        raise exception 'household_name_required';
    end if;

    perform public.ensure_current_profile();

    select * into v_existing
    from public.resolve_user_household_row();

    if found then
        raise exception 'household_already_active';
    end if;

    insert into public.households (topic, name, owner_id)
    values ('nutrition', v_trimmed_name, v_user_id)
    returning id, name, owner_id
    into v_household_id, v_household_name, v_owner_id;

    select coalesce(nullif(trim(display_name), ''), nullif(trim(email), ''), v_trimmed_name)
    into v_owner_display_name
    from public.profiles
    where id = v_user_id;

    insert into public.household_modules (household_id, feature)
    values
        (v_household_id, 'grocery'),
        (v_household_id, 'meal_plan'),
        (v_household_id, 'ai_advice');

    select coalesce(json_agg(feature order by feature), '[]'::json)
    into v_features
    from public.household_modules
    where household_id = v_household_id;

    return json_build_object(
        'status', 'active',
        'household_id', v_household_id,
        'household_name', v_household_name,
        'owner_id', v_owner_id,
        'owner_display_name', v_owner_display_name,
        'role', 'owner',
        'features', v_features
    );
end;
$$;

create or replace function public.ensure_household()
returns json
language plpgsql
security definer
set search_path = public
as $$
declare
    v_row record;
    v_features json;
begin
    perform public.ensure_current_profile();

    select * into v_row
    from public.resolve_user_household_row();

    if not found then
        raise exception 'household_required';
    end if;

    select coalesce(json_agg(feature order by feature), '[]'::json)
    into v_features
    from public.household_modules
    where household_id = v_row.household_id;

    return json_build_object(
        'household_id', v_row.household_id,
        'household_name', v_row.household_name,
        'owner_id', v_row.owner_id,
        'owner_display_name', v_row.owner_display_name,
        'features', v_features
    );
end;
$$;

revoke all on function public.household_membership_status() from public;
grant execute on function public.household_membership_status() to authenticated;

revoke all on function public.create_household(text) from public;
grant execute on function public.create_household(text) to authenticated;

drop function if exists public.user_has_active_nutrition_household(uuid, uuid);

create or replace function public.user_has_active_nutrition_household(
    p_user_id uuid,
    p_exclude_household_id uuid default null
)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select exists (
        select 1
        from public.households s
        where s.topic = 'nutrition'
          and (p_exclude_household_id is null or s.id <> p_exclude_household_id)
          and (
              s.owner_id = p_user_id
              or exists (
                  select 1
                  from public.household_members sm
                  where sm.household_id = s.id
                    and sm.user_id = p_user_id
                    and sm.left_at is null
              )
          )
    );
$$;

create unique index if not exists sharing_spaces_one_nutrition_owner_idx
    on public.households (owner_id)
    where topic = 'nutrition';

create unique index if not exists space_members_one_active_per_user_idx
    on public.household_members (user_id)
    where user_id is not null and left_at is null;

create or replace function public.list_my_pending_household_invites()
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
                'household_id', i.household_id,
                'household_name', s.name,
                'email', i.email,
                'role', i.role::text,
                'expires_at', i.expires_at
            )
            order by s.name
        ),
        '[]'::json
    )
    into v_result
    from public.household_invites i
    join public.households s on s.id = i.household_id
    where lower(trim(i.email)) = v_profile_email
      and i.accepted_at is null
      and i.declined_at is null
      and i.expires_at > now();

    return v_result;
end;
$$;


create or replace function public.accept_household_invite(p_invite_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_invite public.household_invites%rowtype;
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
    from public.household_invites
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

    if public.household_active_member_count(v_invite.household_id) >= 20 then
        raise exception 'household_member_limit_reached';
    end if;

    insert into public.household_members (household_id, user_id, role)
    values (v_invite.household_id, v_profile_id, v_invite.role);

    update public.household_invites
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
        from public.households s
        where s.topic = 'nutrition'
          and s.owner_id = v_user_id
    ) then
        raise exception 'owner_must_transfer_or_dissolve';
    end if;

    update public.household_members
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
    v_household_id uuid;
    v_member_count integer;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    select s.id
    into v_household_id
    from public.households s
    where s.topic = 'nutrition'
      and s.owner_id = v_user_id
    limit 1;

    if v_household_id is null then
        raise exception 'household_not_found';
    end if;

    select count(*)::integer
    into v_member_count
    from public.household_members sm
    where sm.household_id = v_household_id
      and sm.left_at is null
      and sm.user_id is not null
      and sm.user_id <> v_user_id;

    if v_member_count > 0 then
        raise exception 'owner_must_transfer_or_dissolve';
    end if;

    delete from public.households
    where id = v_household_id;
end;
$$;

revoke all on function public.household_active_member_count(uuid) from public;
grant execute on function public.household_active_member_count(uuid) to authenticated;

revoke all on function public.leave_household() from public;
grant execute on function public.leave_household() to authenticated;

revoke all on function public.dissolve_household() from public;
grant execute on function public.dissolve_household() to authenticated;

-- P1: sole owner transfers household ownership to another active member.

create or replace function public.transfer_household_ownership(p_new_owner_user_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_household_id uuid;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    if p_new_owner_user_id is null or p_new_owner_user_id = v_user_id then
        raise exception 'invalid_transfer_target';
    end if;

    select s.id
    into v_household_id
    from public.households s
    where s.topic = 'nutrition'
      and s.owner_id = v_user_id
    limit 1;

    if v_household_id is null then
        raise exception 'household_not_found';
    end if;

    if not exists (
        select 1
        from public.household_members sm
        where sm.household_id = v_household_id
          and sm.user_id = p_new_owner_user_id
          and sm.left_at is null
    ) then
        raise exception 'transfer_target_not_member';
    end if;

    update public.households
    set owner_id = p_new_owner_user_id,
        updated_at = now()
    where id = v_household_id;

    insert into public.household_members (household_id, user_id, role)
    values (v_household_id, v_user_id, 'editor')
    on conflict (household_id, user_id)
    do update set role = 'editor', left_at = null;
end;
$$;

revoke all on function public.transfer_household_ownership(uuid) from public;
grant execute on function public.transfer_household_ownership(uuid) to authenticated;

-- P1 GDPR: export signed-in user's profile and active household affiliation metadata.

drop function if exists public.household_active_member_count(uuid);

create or replace function public.household_active_member_count(p_household_id uuid)
returns integer
language sql
stable
security definer
set search_path = public
as $$
    select 1 + coalesce(
        (
            select count(*)::integer
            from public.household_members sm
            join public.households s on s.id = p_household_id
            where sm.household_id = p_household_id
              and sm.user_id is not null
              and sm.left_at is null
              and sm.user_id <> s.owner_id
        ),
        0
    ) + coalesce(
        (
            select count(*)::integer
            from public.household_dependants hd
            where hd.household_id = p_household_id
              and hd.removed_at is null
        ),
        0
    );
$$;

drop function if exists public.add_household_dependant(uuid, text);

create or replace function public.add_household_dependant(p_household_id uuid, p_display_name text)
returns uuid
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_dependant_id uuid;
    v_name text := trim(p_display_name);
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    if char_length(v_name) = 0 then
        raise exception 'dependant_name_required';
    end if;

    if public.household_active_member_count(p_household_id) >= 20 then
        raise exception 'household_member_limit_reached';
    end if;

    if not public.household_member_can_write_nutrition(p_household_id) then
        raise exception 'insufficient_role';
    end if;

    insert into public.household_dependants (household_id, display_name, created_by)
    values (p_household_id, v_name, v_user_id)
    returning id into v_dependant_id;

    return v_dependant_id;
end;
$$;

create or replace function public.invite_household_member(
    p_household_id uuid,
    p_email text,
    p_role public.household_member_role default 'editor'
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
    v_household_name text;
    v_inviter_name text;
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

    if public.household_active_member_count(p_household_id) >= 20 then
        raise exception 'household_member_limit_reached';
    end if;

    perform public.ensure_current_profile();

    if not public.is_household_owner(p_household_id) then
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
            from public.households s
            where s.id = p_household_id
              and (
                  s.owner_id = v_profile_id
                  or exists (
                      select 1
                      from public.household_members sm
                      where sm.household_id = p_household_id
                        and sm.user_id = v_profile_id
                        and sm.left_at is null
                  )
              )
        ) then
            raise exception 'member_already_exists';
        end if;

        if public.user_has_active_nutrition_household(v_profile_id, p_household_id) then
            raise exception 'invitee_household_already_active';
        end if;
    end if;

    select id
    into v_invite_id
    from public.household_invites
    where household_id = p_household_id
      and lower(trim(email)) = v_normalized_email
      and accepted_at is null
      and declined_at is null
    limit 1;

    if v_invite_id is not null then
        update public.household_invites
        set role = p_role,
            invited_by = v_user_id,
            expires_at = now() + interval '14 days'
        where id = v_invite_id;
    else
        insert into public.household_invites (household_id, email, role, invited_by)
        values (p_household_id, v_normalized_email, p_role, v_user_id)
        returning id into v_invite_id;
    end if;

    select s.name, coalesce(p.display_name, p.email, 'Someone')
    into v_household_name, v_inviter_name
    from public.households s
    cross join public.profiles p
    where s.id = p_household_id
      and p.id = v_user_id;

    insert into public.household_notification_outbox (kind, payload)
    values (
        'household_invite',
        jsonb_build_object(
            'invite_id', v_invite_id,
            'household_id', p_household_id,
            'household_name', v_household_name,
            'invitee_email', v_normalized_email,
            'invitee_user_id', v_profile_id,
            'inviter_name', v_inviter_name,
            'role', p_role::text
        )
    );

    return json_build_object('result', 'invited');
end;
$$;
create or replace function public.export_my_personal_data()
returns json
language plpgsql
stable
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_profile json;
    v_membership json;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    select json_build_object(
        'id', p.id,
        'email', p.email,
        'display_name', p.display_name,
        'avatar_url', p.avatar_url,
        'created_at', p.created_at,
        'updated_at', p.updated_at
    )
    into v_profile
    from public.profiles p
    where p.id = v_user_id;

    select json_build_object(
        'household_id', row.household_id,
        'household_name', row.household_name,
        'owner_id', row.owner_id,
        'role', row.member_role
    )
    into v_membership
    from public.resolve_user_household_row() row
    limit 1;

    return json_build_object(
        'exported_at', now(),
        'profile', coalesce(v_profile, json 'null'),
        'household_membership', v_membership
    );
end;
$$;

revoke all on function public.export_my_personal_data() from public;
grant execute on function public.export_my_personal_data() to authenticated;

create or replace function public.profiles_share_household(p_profile_id uuid, p_viewer_id uuid)
returns boolean
language sql
stable
security invoker
set search_path = public
as $$
    select p_profile_id = p_viewer_id
        or exists (
            select 1
            from public.household_members sm_viewer
            join public.household_members sm_peer on sm_peer.household_id = sm_viewer.household_id
            where sm_viewer.user_id = p_viewer_id
              and sm_peer.user_id = p_profile_id
        )
        or exists (
            select 1
            from public.households s
            where s.owner_id = p_viewer_id
              and (
                  exists (
                      select 1 from public.household_members sm
                      where sm.household_id = s.id and sm.user_id = p_profile_id
                  )
                  or s.owner_id = p_profile_id
              )
        );
$$;

-- RLS policies (household helper names)
drop policy if exists nutrition_space_week_data_insert on public.nutrition_household_week_data;
drop policy if exists nutrition_space_week_data_update on public.nutrition_household_week_data;
drop policy if exists nutrition_space_week_data_delete on public.nutrition_household_week_data;
drop policy if exists nutrition_space_week_data_select on public.nutrition_household_week_data;
drop policy if exists nutrition_household_week_data_insert on public.nutrition_household_week_data;
drop policy if exists nutrition_household_week_data_update on public.nutrition_household_week_data;
drop policy if exists nutrition_household_week_data_delete on public.nutrition_household_week_data;
drop policy if exists nutrition_household_week_data_select on public.nutrition_household_week_data;

create policy nutrition_household_week_data_select
    on public.nutrition_household_week_data
    for select to authenticated
    using (public.is_household_member(household_id));

create policy nutrition_household_week_data_insert
    on public.nutrition_household_week_data
    for insert to authenticated
    with check (public.household_member_can_write_nutrition(household_id));

create policy nutrition_household_week_data_update
    on public.nutrition_household_week_data
    for update to authenticated
    using (public.household_member_can_write_nutrition(household_id))
    with check (public.household_member_can_write_nutrition(household_id));

create policy nutrition_household_week_data_delete
    on public.nutrition_household_week_data
    for delete to authenticated
    using (public.household_member_can_write_nutrition(household_id));

drop policy if exists household_dependants_select on public.household_dependants;
drop policy if exists household_dependants_insert on public.household_dependants;
drop policy if exists household_dependants_update on public.household_dependants;

create policy household_dependants_select
    on public.household_dependants
    for select to authenticated
    using (public.is_household_member(household_id));

create policy household_dependants_insert
    on public.household_dependants
    for insert to authenticated
    with check (public.household_member_can_write_nutrition(household_id));

create policy household_dependants_update
    on public.household_dependants
    for update to authenticated
    using (public.household_member_can_write_nutrition(household_id))
    with check (public.household_member_can_write_nutrition(household_id));

drop policy if exists profiles_select_space_peers on public.profiles;
create policy profiles_select_household_peers
    on public.profiles
    for select to authenticated
    using (public.profiles_share_household(id, (select auth.uid())));

drop function if exists public.list_my_pending_space_invites();
drop function if exists public.invite_space_member(uuid, text, public.household_member_role);
drop function if exists public.accept_space_invite(uuid);
drop function if exists public.is_space_owner(uuid);
drop function if exists public.is_space_member(uuid);
drop function if exists public.space_member_can_write_nutrition(uuid);
drop function if exists public.profiles_share_space(uuid, uuid);

revoke all on function public.list_my_pending_household_invites() from public;
grant execute on function public.list_my_pending_household_invites() to authenticated;
revoke all on function public.invite_household_member(uuid, text, public.household_member_role) from public;
grant execute on function public.invite_household_member(uuid, text, public.household_member_role) to authenticated;
revoke all on function public.accept_household_invite(uuid) from public;
grant execute on function public.accept_household_invite(uuid) to authenticated;
revoke all on function public.is_household_owner(uuid) from public;
grant execute on function public.is_household_owner(uuid) to authenticated;
revoke all on function public.is_household_member(uuid) from public;
grant execute on function public.is_household_member(uuid) to authenticated;
revoke all on function public.household_member_can_write_nutrition(uuid) from public;
grant execute on function public.household_member_can_write_nutrition(uuid) to authenticated;
revoke all on function public.add_household_dependant(uuid, text) from public;
grant execute on function public.add_household_dependant(uuid, text) to authenticated;
