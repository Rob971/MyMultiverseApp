-- Fix household member CRUD for owners and admins.
--
-- Bugs addressed:
-- 1) Client removed people via DELETE on household_members, but RLS only allows
--    owners (admins silently deleted 0 rows; refresh restored the member).
-- 2) remove_household_dependant still called space_member_can_write_nutrition,
--    which was dropped in 20250620000000 — so dependant removal failed for everyone.
--
-- Solution: soft-remove people via security-definer RPC gated by is_household_manager,
-- and rebuild remove_household_dependant / add_household_dependant on the same gate.

create or replace function public.remove_household_member(p_member_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_household_id uuid;
    v_target_user_id uuid;
    v_target_role public.household_member_role;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    select sm.household_id, sm.user_id, sm.role
    into v_household_id, v_target_user_id, v_target_role
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

    -- Self-removal must go through leave_household (owner transfer / dissolve rules).
    if v_target_user_id = v_user_id then
        raise exception 'insufficient_role';
    end if;

    if not public.is_household_manager(v_household_id) then
        raise exception 'insufficient_role';
    end if;

    update public.household_members
    set left_at = now()
    where id = p_member_id
      and left_at is null;
end;
$$;

revoke all on function public.remove_household_member(uuid) from public;
grant execute on function public.remove_household_member(uuid) to authenticated;

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

    if not public.is_household_manager(v_household_id) then
        raise exception 'insufficient_role';
    end if;

    update public.household_dependants
    set removed_at = now()
    where id = p_dependant_id
      and removed_at is null;
end;
$$;

revoke all on function public.remove_household_dependant(uuid) from public;
grant execute on function public.remove_household_dependant(uuid) to authenticated;

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

    -- Match product UI: only owner/admin manage the members list (not editors).
    if not public.is_household_manager(p_household_id) then
        raise exception 'insufficient_role';
    end if;

    insert into public.household_dependants (household_id, display_name, created_by)
    values (p_household_id, v_name, v_user_id)
    returning id into v_dependant_id;

    return v_dependant_id;
end;
$$;

revoke all on function public.add_household_dependant(uuid, text) from public;
grant execute on function public.add_household_dependant(uuid, text) to authenticated;

-- Align direct-table mutations with manager permissions used by invite/role RPCs.
-- Soft-remove still goes through remove_household_member (security definer); these
-- policies keep accidental client UPDATE/DELETE consistent for owners and admins.
drop policy if exists household_members_update on public.household_members;
create policy household_members_update
    on public.household_members
    for update
    to authenticated
    using (public.is_household_manager(household_id))
    with check (public.is_household_manager(household_id));

drop policy if exists household_members_delete on public.household_members;
create policy household_members_delete
    on public.household_members
    for delete
    to authenticated
    using (
        public.is_household_manager(household_id)
        and role <> 'owner'
    );
