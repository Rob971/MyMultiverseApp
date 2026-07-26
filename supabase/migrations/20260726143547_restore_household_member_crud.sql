-- Restore dependant removal after the household terminology migration dropped its
-- legacy helper with CASCADE, and route person removal through manager-authorized RPCs.

create or replace function public.add_household_dependant(
    p_household_id uuid,
    p_display_name text
)
returns uuid
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_user_id uuid := auth.uid();
    v_dependant_id uuid;
    v_name text := trim(p_display_name);
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    if not (
        exists (
            select 1
            from public.households h
            where h.id = p_household_id
              and h.owner_id = v_user_id
        )
        or exists (
            select 1
            from public.household_members hm
            where hm.household_id = p_household_id
              and hm.user_id = v_user_id
              and hm.left_at is null
              and hm.role = 'admin'
        )
    ) then
        raise exception 'insufficient_role';
    end if;

    if v_name is null or char_length(v_name) = 0 then
        raise exception 'dependant_name_required';
    end if;

    if public.household_active_member_count(p_household_id) >= 20 then
        raise exception 'household_member_limit_reached';
    end if;

    insert into public.household_dependants (household_id, display_name, created_by)
    values (p_household_id, v_name, v_user_id)
    returning id into v_dependant_id;

    return v_dependant_id;
end;
$$;

create or replace function public.remove_household_dependant(p_dependant_id uuid)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_user_id uuid := auth.uid();
    v_household_id uuid;
    v_rows integer;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    select hd.household_id
    into v_household_id
    from public.household_dependants hd
    where hd.id = p_dependant_id
      and hd.removed_at is null;

    if v_household_id is null then
        raise exception 'dependant_not_found';
    end if;

    if not (
        exists (
            select 1
            from public.households h
            where h.id = v_household_id
              and h.owner_id = v_user_id
        )
        or exists (
            select 1
            from public.household_members hm
            where hm.household_id = v_household_id
              and hm.user_id = v_user_id
              and hm.left_at is null
              and hm.role = 'admin'
        )
    ) then
        raise exception 'insufficient_role';
    end if;

    update public.household_dependants
    set removed_at = now()
    where id = p_dependant_id
      and removed_at is null;

    get diagnostics v_rows = row_count;
    if v_rows <> 1 then
        raise exception 'dependant_not_found';
    end if;
end;
$$;

create or replace function public.remove_household_member(p_member_id uuid)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_user_id uuid := auth.uid();
    v_household_id uuid;
    v_target_user_id uuid;
    v_target_role public.household_member_role;
    v_rows integer;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    select hm.household_id, hm.user_id, hm.role
    into v_household_id, v_target_user_id, v_target_role
    from public.household_members hm
    where hm.id = p_member_id
      and hm.user_id is not null
      and hm.left_at is null;

    if v_household_id is null then
        raise exception 'member_not_found';
    end if;

    if not (
        exists (
            select 1
            from public.households h
            where h.id = v_household_id
              and h.owner_id = v_user_id
        )
        or exists (
            select 1
            from public.household_members hm
            where hm.household_id = v_household_id
              and hm.user_id = v_user_id
              and hm.left_at is null
              and hm.role = 'admin'
        )
    ) then
        raise exception 'insufficient_role';
    end if;

    if v_target_role = 'owner'
        or exists (
            select 1
            from public.households h
            where h.id = v_household_id
              and h.owner_id = v_target_user_id
        )
    then
        raise exception 'cannot_remove_owner';
    end if;

    delete from public.household_members
    where id = p_member_id
      and left_at is null;

    get diagnostics v_rows = row_count;
    if v_rows <> 1 then
        raise exception 'member_not_found';
    end if;
end;
$$;

-- Dependants are created/removed only through the manager RPCs. Retain the
-- existing editor capability to update dependant photos, but no other columns.
revoke insert, update on public.household_dependants from public, anon, authenticated;
grant update (avatar_url) on public.household_dependants to authenticated;

revoke all on function public.add_household_dependant(uuid, text) from public, anon;
grant execute on function public.add_household_dependant(uuid, text) to authenticated;

revoke all on function public.remove_household_dependant(uuid) from public, anon;
grant execute on function public.remove_household_dependant(uuid) to authenticated;

revoke all on function public.remove_household_member(uuid) from public, anon;
grant execute on function public.remove_household_member(uuid) to authenticated;
