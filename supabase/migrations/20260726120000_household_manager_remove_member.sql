-- Fix member-list CRUD for household managers (owner + admin).
--
-- Two defects addressed:
--   1. Removing a person member used a direct PostgREST DELETE, gated by the
--      owner-only `household_members_delete` RLS policy. Admins (who the product
--      allows to manage members) had their DELETE silently match zero rows, so
--      the member reappeared on the next refresh. This adds a SECURITY DEFINER
--      RPC that lets owner OR admin remove any non-owner member, mirroring
--      `update_household_member_role`.
--   2. `remove_household_dependant` still called `space_member_can_write_nutrition`,
--      which was dropped (CASCADE) in 20250620000000_complete_household_terminology.
--      Every dependant removal therefore failed at runtime for owner and admin
--      alike. Recreated to use `household_member_can_write_nutrition`.

-- 1) Manager (owner or admin) removes a person member.
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
        raise exception 'cannot_remove_owner';
    end if;

    if not public.is_household_manager(v_household_id) then
        raise exception 'insufficient_role';
    end if;

    delete from public.household_members
    where id = p_member_id;
end;
$$;

revoke all on function public.remove_household_member(uuid) from public;
grant execute on function public.remove_household_member(uuid) to authenticated;

-- 2) Recreate dependant removal against the current write-permission helper.
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

    if not public.household_member_can_write_nutrition(v_household_id) then
        raise exception 'insufficient_role';
    end if;

    update public.household_dependants
    set removed_at = now()
    where id = p_dependant_id;
end;
$$;

revoke all on function public.remove_household_dependant(uuid) from public;
grant execute on function public.remove_household_dependant(uuid) to authenticated;
