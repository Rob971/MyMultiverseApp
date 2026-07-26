-- Restore household member removal for owners/admins and dependant removal dropped by
-- CASCADE when space_member_can_write_nutrition was removed. Fix household_invites RLS
-- so managers can list outbound invites (policies were dropped with is_space_owner).

create or replace function public.remove_household_member(p_member_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_household_id uuid;
    v_target_role public.household_member_role;
    v_rows integer;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    select sm.household_id, sm.role
    into v_household_id, v_target_role
    from public.household_members sm
    where sm.id = p_member_id
      and sm.left_at is null
      and sm.user_id is not null;

    if v_household_id is null then
        raise exception 'member_not_found';
    end if;

    if v_target_role = 'owner' then
        raise exception 'insufficient_role';
    end if;

    if not public.is_household_manager(v_household_id) then
        raise exception 'insufficient_role';
    end if;

    update public.household_members
    set left_at = now()
    where id = p_member_id
      and left_at is null;

    get diagnostics v_rows = row_count;
    if v_rows = 0 then
        raise exception 'member_not_found';
    end if;
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
    v_rows integer;
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

    if not public.is_household_manager(v_household_id) then
        raise exception 'insufficient_role';
    end if;

    update public.household_dependants
    set removed_at = now()
    where id = p_dependant_id
      and removed_at is null;

    get diagnostics v_rows = row_count;
    if v_rows = 0 then
        raise exception 'dependant_not_found';
    end if;
end;
$$;

revoke all on function public.remove_household_member(uuid) from public;
grant execute on function public.remove_household_member(uuid) to authenticated;

revoke all on function public.remove_household_dependant(uuid) from public;
grant execute on function public.remove_household_dependant(uuid) to authenticated;

drop policy if exists space_invites_select on public.household_invites;
drop policy if exists space_invites_insert on public.household_invites;
drop policy if exists space_invites_update on public.household_invites;
drop policy if exists household_invites_select on public.household_invites;
drop policy if exists household_invites_insert on public.household_invites;
drop policy if exists household_invites_update on public.household_invites;

create policy household_invites_select
    on public.household_invites
    for select
    to authenticated
    using (
        public.is_household_manager(household_id)
        or lower(trim(email)) = lower(trim(coalesce(
            (select p.email from public.profiles p where p.id = (select auth.uid())),
            auth.jwt() ->> 'email',
            ''
        )))
    );

create policy household_invites_insert
    on public.household_invites
    for insert
    to authenticated
    with check (public.is_household_manager(household_id));

create policy household_invites_update
    on public.household_invites
    for update
    to authenticated
    using (
        public.is_household_manager(household_id)
        or lower(trim(email)) = lower(trim(coalesce(
            (select p.email from public.profiles p where p.id = (select auth.uid())),
            auth.jwt() ->> 'email',
            ''
        )))
    )
    with check (
        public.is_household_manager(household_id)
        or lower(trim(email)) = lower(trim(coalesce(
            (select p.email from public.profiles p where p.id = (select auth.uid())),
            auth.jwt() ->> 'email',
            ''
        )))
    );
