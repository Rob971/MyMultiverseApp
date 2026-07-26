-- Allow household owners and admins to remove (kick) any non-owner member.
-- Previously, removeMember used a direct DELETE on household_members, which the
-- RLS delete policy only allowed for the owner. Admins were silently blocked.
-- This RPC enforces the same permission model as update_household_member_role:
-- owner or admin may act; owner cannot be removed; actor cannot be the target.

create or replace function public.remove_household_member(p_member_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id        uuid := auth.uid();
    v_household_id   uuid;
    v_target_role    public.household_member_role;
    v_actor_is_owner boolean;
    v_actor_is_admin boolean;
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

    v_actor_is_owner := public.is_household_owner(v_household_id);
    v_actor_is_admin := exists (
        select 1
        from public.household_members sm
        where sm.household_id = v_household_id
          and sm.user_id = v_user_id
          and sm.left_at is null
          and sm.role = 'admin'
    );

    if not v_actor_is_owner and not v_actor_is_admin then
        raise exception 'insufficient_role';
    end if;

    -- Soft-delete: consistent with leave_household; preserves audit trail.
    update public.household_members
    set left_at = now()
    where id = p_member_id;
end;
$$;

revoke all on function public.remove_household_member(uuid) from public;
grant execute on function public.remove_household_member(uuid) to authenticated;
