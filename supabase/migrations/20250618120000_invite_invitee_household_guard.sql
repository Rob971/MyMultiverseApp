-- Invites always go to the invitee (no silent direct-add). Block invite when invitee
-- already belongs to another nutrition household.

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
              )
          )
    );
$$;

revoke all on function public.user_has_active_nutrition_household(uuid, uuid) from public;
grant execute on function public.user_has_active_nutrition_household(uuid, uuid) to authenticated;

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
