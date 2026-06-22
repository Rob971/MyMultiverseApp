-- Notify household members when someone accepts an invite.

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
    v_member_name text;
    v_household_name text;
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

    select coalesce(nullif(trim(p.display_name), ''), nullif(trim(p.email), ''), 'Member')
    into v_member_name
    from public.profiles p
    where p.id = v_profile_id;

    select s.name
    into v_household_name
    from public.households s
    where s.id = v_invite.household_id;

    insert into public.household_notification_outbox (kind, payload)
    values (
        'household_member_joined',
        jsonb_build_object(
            'household_id', v_invite.household_id,
            'household_name', v_household_name,
            'member_user_id', v_profile_id,
            'member_name', v_member_name
        )
    );
end;
$$;
