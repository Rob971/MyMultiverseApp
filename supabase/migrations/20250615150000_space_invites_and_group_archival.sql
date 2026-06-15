-- Pending space invites and archival for expired event groups.

create table public.space_invites (
    id uuid primary key default gen_random_uuid(),
    space_id uuid not null references public.sharing_spaces (id) on delete cascade,
    email text not null,
    role public.space_member_role not null default 'editor',
    invited_by uuid not null references public.profiles (id) on delete cascade,
    token text not null unique default encode(gen_random_bytes(32), 'hex'),
    expires_at timestamptz not null default (now() + interval '14 days'),
    accepted_at timestamptz,
    declined_at timestamptz,
    created_at timestamptz not null default now(),
    constraint space_invites_email_not_blank check (char_length(trim(email)) > 0)
);

create unique index space_invites_pending_email_idx
    on public.space_invites (space_id, lower(trim(email)))
    where accepted_at is null and declined_at is null;

create index space_invites_email_pending_idx
    on public.space_invites (lower(trim(email)))
    where accepted_at is null and declined_at is null;

alter table public.space_invites enable row level security;

create policy space_invites_select
    on public.space_invites
    for select
    to authenticated
    using (
        public.is_space_owner(space_id)
        or lower(trim(email)) = lower(trim((select p.email from public.profiles p where p.id = (select auth.uid()))))
    );

create policy space_invites_insert
    on public.space_invites
    for insert
    to authenticated
    with check (public.is_space_owner(space_id));

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

    select email into v_profile_email from public.profiles where id = v_profile_id;
    if v_profile_email is null then
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

    if lower(trim(v_invite.email)) <> lower(trim(v_profile_email)) then
        raise exception 'invite_email_mismatch';
    end if;

    insert into public.space_members (space_id, user_id, role)
    values (v_invite.space_id, v_profile_id, v_invite.role)
    on conflict do nothing;

    update public.space_invites
    set accepted_at = now()
    where id = p_invite_id;
end;
$$;

revoke all on function public.accept_space_invite(uuid) from public;
grant execute on function public.accept_space_invite(uuid) to authenticated;

create or replace function public.archive_expired_contact_groups()
returns integer
language plpgsql
security definer
set search_path = public
as $$
declare
    v_count integer;
begin
    update public.contact_groups
    set archived_at = now(),
        updated_at = now()
    where lifecycle = 'event'
      and archived_at is null
      and expires_at is not null
      and expires_at <= now();

    get diagnostics v_count = row_count;
    return v_count;
end;
$$;

revoke all on function public.archive_expired_contact_groups() from public;
grant execute on function public.archive_expired_contact_groups() to authenticated;
