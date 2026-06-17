-- Atomic household invite / add-member RPC and more reliable invite acceptance.
-- Repairs environments where space_invites was never created despite migration history.

create table if not exists public.space_invites (
    id uuid primary key default gen_random_uuid(),
    space_id uuid not null references public.sharing_spaces (id) on delete cascade,
    email text not null,
    role public.space_member_role not null default 'editor',
    invited_by uuid not null references public.profiles (id) on delete cascade,
    token text not null unique default replace(gen_random_uuid()::text || gen_random_uuid()::text, '-', ''),
    expires_at timestamptz not null default (now() + interval '14 days'),
    accepted_at timestamptz,
    declined_at timestamptz,
    created_at timestamptz not null default now(),
    constraint space_invites_email_not_blank check (char_length(trim(email)) > 0)
);

create unique index if not exists space_invites_pending_email_idx
    on public.space_invites (space_id, lower(trim(email)))
    where accepted_at is null and declined_at is null;

create index if not exists space_invites_email_pending_idx
    on public.space_invites (lower(trim(email)))
    where accepted_at is null and declined_at is null;

alter table public.space_invites enable row level security;

drop policy if exists space_invites_select on public.space_invites;
create policy space_invites_select
    on public.space_invites
    for select
    to authenticated
    using (
        public.is_space_owner(space_id)
        or lower(trim(email)) = lower(trim((select p.email from public.profiles p where p.id = (select auth.uid()))))
    );

drop policy if exists space_invites_insert on public.space_invites;
create policy space_invites_insert
    on public.space_invites
    for insert
    to authenticated
    with check (public.is_space_owner(space_id));

drop policy if exists space_invites_update on public.space_invites;
create policy space_invites_update
    on public.space_invites
    for update
    to authenticated
    using (
        public.is_space_owner(space_id)
        or lower(trim(email)) = lower(trim((select p.email from public.profiles p where p.id = (select auth.uid()))))
    )
    with check (
        public.is_space_owner(space_id)
        or lower(trim(email)) = lower(trim((select p.email from public.profiles p where p.id = (select auth.uid()))))
    );

grant select, insert, update on public.space_invites to authenticated;

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
              and s.owner_id = v_profile_id
        ) or exists (
            select 1
            from public.space_members sm
            where sm.space_id = p_space_id
              and sm.user_id = v_profile_id
        ) then
            raise exception 'member_already_exists';
        end if;

        insert into public.space_members (space_id, user_id, role)
        values (p_space_id, v_profile_id, p_role);

        return json_build_object('result', 'added');
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

    if exists (
        select 1
        from public.sharing_spaces s
        where s.topic = 'nutrition'
          and s.id <> v_invite.space_id
          and (
              s.owner_id = v_profile_id
              or exists (
                  select 1
                  from public.space_members sm
                  where sm.space_id = s.id
                    and sm.user_id = v_profile_id
              )
          )
    ) then
        raise exception 'household_already_active';
    end if;

    insert into public.space_members (space_id, user_id, role)
    values (v_invite.space_id, v_profile_id, v_invite.role)
    on conflict do nothing;

    update public.space_invites
    set accepted_at = now()
    where id = p_invite_id;
end;
$$;

revoke all on function public.invite_space_member(uuid, text, public.space_member_role) from public;
grant execute on function public.invite_space_member(uuid, text, public.space_member_role) to authenticated;

revoke all on function public.accept_space_invite(uuid) from public;
grant execute on function public.accept_space_invite(uuid) to authenticated;
