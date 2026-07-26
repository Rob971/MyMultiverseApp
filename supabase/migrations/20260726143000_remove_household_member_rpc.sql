-- Allow household managers (owner + admin) to remove non-owner members.
-- Previously the app deleted household_members rows via PostgREST; RLS only
-- permitted is_household_owner, so admins silently no-op'd (0 rows) and the UI
-- looked broken. Soft-delete via left_at matches leave_household.

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

-- Keep direct table writes aligned with manager permissions for soft-delete /
-- role maintenance paths that are not going through SECURITY DEFINER RPCs.
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
    using (public.is_household_manager(household_id));

drop policy if exists household_members_insert on public.household_members;
create policy household_members_insert
    on public.household_members
    for insert
    to authenticated
    with check (public.is_household_manager(household_id));
