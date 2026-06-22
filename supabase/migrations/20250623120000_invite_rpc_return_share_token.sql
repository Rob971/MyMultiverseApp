-- Return invite_token to clients for the share sheet; restore notification outbox enqueue.

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
    v_invite_token text;
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

    if p_role = 'admin' and not public.is_household_owner(p_household_id) then
        raise exception 'insufficient_role';
    end if;

    if public.household_active_member_count(p_household_id) >= 20 then
        raise exception 'household_member_limit_reached';
    end if;

    perform public.ensure_current_profile();

    if not public.is_household_manager(p_household_id) then
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

    select token
    into v_invite_token
    from public.household_invites
    where id = v_invite_id;

    select s.name, coalesce(nullif(trim(p.display_name), ''), nullif(trim(p.email), ''), 'Member')
    into v_household_name, v_inviter_name
    from public.households s
    join public.profiles p on p.id = v_user_id
    where s.id = p_household_id;

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
            'role', p_role::text,
            'invite_token', v_invite_token
        )
    );

    return json_build_object(
        'result', 'invited',
        'invite_id', v_invite_id,
        'invite_token', v_invite_token,
        'household_name', v_household_name,
        'inviter_name', v_inviter_name
    );
end;
$$;
