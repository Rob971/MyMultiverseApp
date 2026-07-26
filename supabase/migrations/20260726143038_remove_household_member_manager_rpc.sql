-- Let household managers remove non-owner people through a checked RPC.
-- The client previously issued a direct DELETE against household_members, but
-- RLS only permits owners to delete rows. Keep removal consistent with leave
-- flows by marking the member inactive instead of hard-deleting history.

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
