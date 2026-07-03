-- Ensure household owners have an active household_members row so member lists
-- and collaboration counts stay consistent for every signed-in member.

create or replace function public.create_household(p_name text)
returns json
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_existing record;
    v_household_id uuid;
    v_household_name text;
    v_owner_id uuid;
    v_owner_display_name text;
    v_features json;
    v_trimmed_name text := trim(p_name);
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    if char_length(v_trimmed_name) = 0 then
        raise exception 'household_name_required';
    end if;

    if not public.is_household_name_available(v_trimmed_name) then
        raise exception 'household_name_taken';
    end if;

    perform public.ensure_current_profile();

    select * into v_existing
    from public.resolve_user_household_row();

    if found then
        raise exception 'household_already_active';
    end if;

    insert into public.households (topic, name, owner_id)
    values ('nutrition', v_trimmed_name, v_user_id)
    returning id, name, owner_id
    into v_household_id, v_household_name, v_owner_id;

    select coalesce(nullif(trim(display_name), ''), nullif(trim(email), ''), v_trimmed_name)
    into v_owner_display_name
    from public.profiles
    where id = v_user_id;

    insert into public.household_modules (household_id, feature)
    values
        (v_household_id, 'grocery'),
        (v_household_id, 'meal_plan'),
        (v_household_id, 'ai_advice');

    insert into public.household_members (household_id, user_id, role)
    values (v_household_id, v_user_id, 'owner')
    on conflict (household_id, user_id)
    do update set role = 'owner', left_at = null;

    select coalesce(json_agg(feature order by feature), '[]'::json)
    into v_features
    from public.household_modules
    where household_id = v_household_id;

    return json_build_object(
        'status', 'active',
        'household_id', v_household_id,
        'household_name', v_household_name,
        'owner_id', v_owner_id,
        'owner_display_name', v_owner_display_name,
        'role', 'owner',
        'features', v_features
    );
end;
$$;

insert into public.household_members (household_id, user_id, role)
select s.id, s.owner_id, 'owner'
from public.households s
where s.topic = 'nutrition'
  and not exists (
      select 1
      from public.household_members sm
      where sm.household_id = s.id
        and sm.user_id = s.owner_id
        and sm.left_at is null
  )
on conflict (household_id, user_id)
do update set role = 'owner', left_at = null;

drop policy if exists space_members_select on public.household_members;
create policy household_members_select
    on public.household_members
    for select
    to authenticated
    using (public.is_household_member(household_id));

drop policy if exists space_members_insert on public.household_members;
create policy household_members_insert
    on public.household_members
    for insert
    to authenticated
    with check (public.is_household_owner(household_id));

drop policy if exists space_members_update on public.household_members;
create policy household_members_update
    on public.household_members
    for update
    to authenticated
    using (public.is_household_owner(household_id))
    with check (public.is_household_owner(household_id));

drop policy if exists space_members_delete on public.household_members;
create policy household_members_delete
    on public.household_members
    for delete
    to authenticated
    using (public.is_household_owner(household_id));
