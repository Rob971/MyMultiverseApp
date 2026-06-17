-- Household gatekeeping: explicit membership status, create flow, and no silent auto-create.

create or replace function public.resolve_user_household_row()
returns table (
    space_id uuid,
    space_name text,
    owner_id uuid,
    owner_display_name text,
    member_role text
)
language plpgsql
stable
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    return query
    select
        s.id,
        s.name,
        s.owner_id,
        coalesce(nullif(trim(p.display_name), ''), nullif(trim(p.email), ''), '')::text,
        case
            when s.owner_id = v_user_id then 'owner'
            else coalesce(sm.role::text, 'editor')
        end
    from public.sharing_spaces s
    join public.profiles p on p.id = s.owner_id
    left join public.space_members sm
        on sm.space_id = s.id
       and sm.user_id = v_user_id
    where s.topic = 'nutrition'
      and (
          s.owner_id = v_user_id
          or sm.user_id is not null
          or exists (
              select 1
              from public.space_members sm2
              join public.group_members gm on gm.group_id = sm2.group_id
              where sm2.space_id = s.id
                and gm.user_id = v_user_id
          )
      )
    order by (s.owner_id = v_user_id) desc, s.created_at
    limit 1;
end;
$$;

create or replace function public.household_membership_status()
returns json
language plpgsql
security definer
set search_path = public
as $$
declare
    v_row record;
    v_features json;
begin
    perform public.ensure_current_profile();

    select * into v_row
    from public.resolve_user_household_row();

    if not found then
        return json_build_object('status', 'none');
    end if;

    select coalesce(json_agg(feature order by feature), '[]'::json)
    into v_features
    from public.space_nutrition_features
    where space_id = v_row.space_id;

    return json_build_object(
        'status', 'active',
        'space_id', v_row.space_id,
        'space_name', v_row.space_name,
        'owner_id', v_row.owner_id,
        'owner_display_name', v_row.owner_display_name,
        'role', v_row.member_role,
        'features', v_features
    );
end;
$$;

create or replace function public.create_household(p_name text)
returns json
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_existing record;
    v_space_id uuid;
    v_space_name text;
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

    perform public.ensure_current_profile();

    select * into v_existing
    from public.resolve_user_household_row();

    if found then
        raise exception 'household_already_active';
    end if;

    insert into public.sharing_spaces (topic, name, owner_id)
    values ('nutrition', v_trimmed_name, v_user_id)
    returning id, name, owner_id
    into v_space_id, v_space_name, v_owner_id;

    select coalesce(nullif(trim(display_name), ''), nullif(trim(email), ''), v_trimmed_name)
    into v_owner_display_name
    from public.profiles
    where id = v_user_id;

    insert into public.space_nutrition_features (space_id, feature)
    values
        (v_space_id, 'grocery'),
        (v_space_id, 'meal_plan'),
        (v_space_id, 'ai_advice');

    select coalesce(json_agg(feature order by feature), '[]'::json)
    into v_features
    from public.space_nutrition_features
    where space_id = v_space_id;

    return json_build_object(
        'status', 'active',
        'space_id', v_space_id,
        'space_name', v_space_name,
        'owner_id', v_owner_id,
        'owner_display_name', v_owner_display_name,
        'role', 'owner',
        'features', v_features
    );
end;
$$;

create or replace function public.ensure_household()
returns json
language plpgsql
security definer
set search_path = public
as $$
declare
    v_row record;
    v_features json;
begin
    perform public.ensure_current_profile();

    select * into v_row
    from public.resolve_user_household_row();

    if not found then
        raise exception 'household_required';
    end if;

    select coalesce(json_agg(feature order by feature), '[]'::json)
    into v_features
    from public.space_nutrition_features
    where space_id = v_row.space_id;

    return json_build_object(
        'space_id', v_row.space_id,
        'space_name', v_row.space_name,
        'owner_id', v_row.owner_id,
        'owner_display_name', v_row.owner_display_name,
        'features', v_features
    );
end;
$$;

revoke all on function public.resolve_user_household_row() from public;
grant execute on function public.resolve_user_household_row() to authenticated;

revoke all on function public.household_membership_status() from public;
grant execute on function public.household_membership_status() to authenticated;

revoke all on function public.create_household(text) from public;
grant execute on function public.create_household(text) to authenticated;
