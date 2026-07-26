-- Fix household member CRUD for managers (owner + admin):
--
-- 1. remove_household_member: new security-definer RPC so managers can remove
--    non-owner members. The client previously issued a direct DELETE on
--    household_members, which is owner-only under RLS — for admins the delete
--    silently affected 0 rows and the member reappeared on refresh.
--
-- 2. remove_household_dependant: the previous definition (20250619000000) still
--    called space_member_can_write_nutrition, which was dropped by
--    20250620000000_complete_household_terminology.sql, so removing a dependant
--    failed at runtime for every role (including owner and admin). Recreate it
--    with the current household_member_can_write_nutrition helper, mirroring
--    add_household_dependant.
--
-- 3. transfer_household_ownership: the previous definition always failed at
--    runtime with "no unique or exclusion constraint matching the ON CONFLICT
--    specification" because its on conflict (household_id, user_id) clause did
--    not carry the "where user_id is not null" predicate of the partial unique
--    index. Recreate it with the correct arbiter and also stamp the new owner's
--    member row with role 'owner' so role badges and owner-protection checks
--    stay consistent after a transfer.

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

    -- The owner can never be removed. Check both the member row role and the
    -- authoritative households.owner_id, because member rows can carry a stale
    -- role after an ownership transfer.
    if v_target_role = 'owner' or exists (
        select 1
        from public.households h
        where h.id = v_household_id
          and h.owner_id = v_target_user_id
    ) then
        raise exception 'insufficient_role';
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

create or replace function public.transfer_household_ownership(p_new_owner_user_id uuid)
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

    if p_new_owner_user_id is null or p_new_owner_user_id = v_user_id then
        raise exception 'invalid_transfer_target';
    end if;

    select s.id
    into v_household_id
    from public.households s
    where s.topic = 'nutrition'
      and s.owner_id = v_user_id
    limit 1;

    if v_household_id is null then
        raise exception 'household_not_found';
    end if;

    if not exists (
        select 1
        from public.household_members sm
        where sm.household_id = v_household_id
          and sm.user_id = p_new_owner_user_id
          and sm.left_at is null
    ) then
        raise exception 'transfer_target_not_member';
    end if;

    update public.households
    set owner_id = p_new_owner_user_id,
        updated_at = now()
    where id = v_household_id;

    update public.household_members
    set role = 'owner',
        left_at = null
    where household_id = v_household_id
      and user_id = p_new_owner_user_id;

    insert into public.household_members (household_id, user_id, role)
    values (v_household_id, v_user_id, 'editor')
    on conflict (household_id, user_id) where user_id is not null
    do update set role = 'editor', left_at = null;
end;
$$;

revoke all on function public.transfer_household_ownership(uuid) from public;
grant execute on function public.transfer_household_ownership(uuid) to authenticated;
