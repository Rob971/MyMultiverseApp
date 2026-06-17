-- P2: invite notifications outbox, device tokens, household dependants, account deletion prep.

-- ---------------------------------------------------------------------------
-- Device push tokens (FCM / APNs)
-- ---------------------------------------------------------------------------

create table public.user_device_tokens (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references public.profiles (id) on delete cascade,
    platform text not null check (platform in ('android', 'ios')),
    token text not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (user_id, platform, token)
);

alter table public.user_device_tokens enable row level security;

create policy user_device_tokens_select_own
    on public.user_device_tokens
    for select
    to authenticated
    using ((select auth.uid()) = user_id);

create policy user_device_tokens_insert_own
    on public.user_device_tokens
    for insert
    to authenticated
    with check ((select auth.uid()) = user_id);

create policy user_device_tokens_update_own
    on public.user_device_tokens
    for update
    to authenticated
    using ((select auth.uid()) = user_id)
    with check ((select auth.uid()) = user_id);

create policy user_device_tokens_delete_own
    on public.user_device_tokens
    for delete
    to authenticated
    using ((select auth.uid()) = user_id);

grant select, insert, update, delete on public.user_device_tokens to authenticated;

-- ---------------------------------------------------------------------------
-- Notification outbox (processed by Edge Function notify-household-invite)
-- ---------------------------------------------------------------------------

create table public.household_notification_outbox (
    id uuid primary key default gen_random_uuid(),
    kind text not null,
    payload jsonb not null,
    created_at timestamptz not null default now(),
    processed_at timestamptz
);

alter table public.household_notification_outbox enable row level security;

-- No client policies: only service role / edge functions process the outbox.

-- ---------------------------------------------------------------------------
-- Household dependants (children / non-login members)
-- ---------------------------------------------------------------------------

create table public.household_dependants (
    id uuid primary key default gen_random_uuid(),
    household_id uuid not null references public.households (id) on delete cascade,
    display_name text not null,
    created_by uuid not null references public.profiles (id),
    created_at timestamptz not null default now(),
    removed_at timestamptz,
    constraint household_dependants_display_name_nonempty check (char_length(trim(display_name)) > 0)
);

create index household_dependants_active_idx
    on public.household_dependants (household_id)
    where removed_at is null;

alter table public.household_dependants enable row level security;

create policy household_dependants_select
    on public.household_dependants
    for select
    to authenticated
    using (public.is_space_member(household_id));

create policy household_dependants_insert
    on public.household_dependants
    for insert
    to authenticated
    with check (public.space_member_can_write_nutrition(household_id));

create policy household_dependants_update
    on public.household_dependants
    for update
    to authenticated
    using (public.space_member_can_write_nutrition(household_id))
    with check (public.space_member_can_write_nutrition(household_id));

grant select, insert, update on public.household_dependants to authenticated;

-- ---------------------------------------------------------------------------
-- Member count includes active dependants
-- ---------------------------------------------------------------------------

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
            from public.household_members sm
            join public.households s on s.id = p_space_id
            where sm.household_id = p_space_id
              and sm.user_id is not null
              and sm.left_at is null
              and sm.user_id <> s.owner_id
        ),
        0
    ) + coalesce(
        (
            select count(*)::integer
            from public.household_dependants hd
            where hd.household_id = p_space_id
              and hd.removed_at is null
        ),
        0
    );
$$;

-- ---------------------------------------------------------------------------
-- RPC: register device token
-- ---------------------------------------------------------------------------

create or replace function public.register_device_token(p_platform text, p_token text)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    if p_platform not in ('android', 'ios') then
        raise exception 'invalid_platform';
    end if;

    if char_length(trim(p_token)) = 0 then
        raise exception 'device_token_required';
    end if;

    perform public.ensure_current_profile();

    insert into public.user_device_tokens (user_id, platform, token)
    values (v_user_id, p_platform, trim(p_token))
    on conflict (user_id, platform, token)
    do update set updated_at = now();
end;
$$;

revoke all on function public.register_device_token(text, text) from public;
grant execute on function public.register_device_token(text, text) to authenticated;

-- ---------------------------------------------------------------------------
-- RPC: dependants
-- ---------------------------------------------------------------------------

create or replace function public.add_household_dependant(p_space_id uuid, p_display_name text)
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

    if public.household_active_member_count(p_space_id) >= 20 then
        raise exception 'household_member_limit_reached';
    end if;

    if not public.space_member_can_write_nutrition(p_space_id) then
        raise exception 'insufficient_role';
    end if;

    insert into public.household_dependants (household_id, display_name, created_by)
    values (p_space_id, v_name, v_user_id)
    returning id into v_dependant_id;

    return v_dependant_id;
end;
$$;

create or replace function public.remove_household_dependant(p_dependant_id uuid)
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

    select household_id
    into v_household_id
    from public.household_dependants
    where id = p_dependant_id
      and removed_at is null;

    if v_household_id is null then
        raise exception 'dependant_not_found';
    end if;

    if not public.space_member_can_write_nutrition(v_household_id) then
        raise exception 'insufficient_role';
    end if;

    update public.household_dependants
    set removed_at = now()
    where id = p_dependant_id;
end;
$$;

revoke all on function public.add_household_dependant(uuid, text) from public;
grant execute on function public.add_household_dependant(uuid, text) to authenticated;

revoke all on function public.remove_household_dependant(uuid) from public;
grant execute on function public.remove_household_dependant(uuid) to authenticated;

-- ---------------------------------------------------------------------------
-- RPC: prepare account deletion (DB cleanup before Edge Function deletes auth user)
-- ---------------------------------------------------------------------------

create or replace function public.prepare_account_deletion()
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_is_owner boolean;
    v_other_members integer;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    select exists (
        select 1
        from public.households s
        where s.topic = 'nutrition'
          and s.owner_id = v_user_id
    )
    into v_is_owner;

    if v_is_owner then
        select count(*)::integer
        into v_other_members
        from public.household_members sm
        join public.households s on s.id = sm.household_id
        where s.owner_id = v_user_id
          and sm.left_at is null
          and sm.user_id <> v_user_id;

        if v_other_members > 0 then
            raise exception 'owner_must_transfer_or_dissolve';
        end if;

        perform public.dissolve_household();
    elsif exists (
        select 1
        from public.household_members sm
        where sm.user_id = v_user_id
          and sm.left_at is null
    ) then
        perform public.leave_household();
    end if;

    delete from public.user_device_tokens where user_id = v_user_id;

    update public.profiles
    set display_name = 'Deleted user',
        avatar_url = null,
        updated_at = now()
    where id = v_user_id;
end;
$$;

revoke all on function public.prepare_account_deletion() from public;
grant execute on function public.prepare_account_deletion() to authenticated;

-- ---------------------------------------------------------------------------
-- invite_space_member: enqueue notification after invite
-- ---------------------------------------------------------------------------

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
            from public.households s
            where s.id = p_space_id
              and (
                  s.owner_id = v_profile_id
                  or exists (
                      select 1
                      from public.household_members sm
                      where sm.household_id = p_space_id
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
    from public.household_invites
    where household_id = p_space_id
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
        values (p_space_id, v_normalized_email, p_role, v_user_id)
        returning id into v_invite_id;
    end if;

    select s.name, coalesce(p.display_name, p.email, 'Someone')
    into v_household_name, v_inviter_name
    from public.households s
    cross join public.profiles p
    where s.id = p_space_id
      and p.id = v_user_id;

    insert into public.household_notification_outbox (kind, payload)
    values (
        'household_invite',
        jsonb_build_object(
            'invite_id', v_invite_id,
            'household_id', p_space_id,
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
