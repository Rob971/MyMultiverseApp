-- Fix remove_household_dependant: the P2 migration defined it calling
-- space_member_can_write_nutrition, which was dropped with CASCADE in
-- 20250620000000_complete_household_terminology.sql. Any call to
-- remove_household_dependant has been failing with "function does not exist"
-- ever since. Recreate using is_household_manager so that only owners and
-- admins can remove dependants (matching the UI canRemoveMember gate).

create or replace function public.remove_household_dependant(p_dependant_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id      uuid := auth.uid();
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
    where id = p_dependant_id;
end;
$$;

revoke all on function public.remove_household_dependant(uuid) from public;
grant execute on function public.remove_household_dependant(uuid) to authenticated;
