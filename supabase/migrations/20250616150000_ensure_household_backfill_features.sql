-- Backfill default nutrition features for households created before feature rows existed.

create or replace function public.ensure_household()
returns json
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_display_name text;
    v_space_id uuid;
    v_space_name text;
    v_owner_id uuid;
    v_owner_display_name text;
    v_features json;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    perform public.ensure_current_profile();

    select
        s.id,
        s.name,
        s.owner_id,
        coalesce(p.display_name, p.email, '')
    into
        v_space_id,
        v_space_name,
        v_owner_id,
        v_owner_display_name
    from public.sharing_spaces s
    join public.profiles p on p.id = s.owner_id
    where s.topic = 'nutrition'
      and (
          s.owner_id = v_user_id
          or exists (
              select 1
              from public.space_members sm
              where sm.space_id = s.id
                and sm.user_id = v_user_id
          )
          or exists (
              select 1
              from public.space_members sm
              join public.group_members gm on gm.group_id = sm.group_id
              where sm.space_id = s.id
                and gm.user_id = v_user_id
          )
      )
    order by (s.owner_id = v_user_id) desc, s.created_at
    limit 1;

    if v_space_id is null then
        select coalesce(nullif(trim(display_name), ''), nullif(trim(email), ''), 'Our household')
        into v_display_name
        from public.profiles
        where id = v_user_id;

        insert into public.sharing_spaces (topic, name, owner_id)
        values ('nutrition', v_display_name, v_user_id)
        returning id, name, owner_id
        into v_space_id, v_space_name, v_owner_id;

        v_owner_display_name := v_display_name;

        insert into public.space_nutrition_features (space_id, feature)
        values
            (v_space_id, 'grocery'),
            (v_space_id, 'meal_plan'),
            (v_space_id, 'ai_advice');
    else
        insert into public.space_nutrition_features (space_id, feature)
        select v_space_id, f.feature
        from (values
            ('grocery'::public.nutrition_feature),
            ('meal_plan'::public.nutrition_feature),
            ('ai_advice'::public.nutrition_feature)
        ) as f(feature)
        on conflict do nothing;
    end if;

    select coalesce(json_agg(feature order by feature), '[]'::json)
    into v_features
    from public.space_nutrition_features
    where space_id = v_space_id;

    return json_build_object(
        'space_id', v_space_id,
        'space_name', v_space_name,
        'owner_id', v_owner_id,
        'owner_display_name', v_owner_display_name,
        'features', v_features
    );
end;
$$;

revoke all on function public.ensure_household() from public;
grant execute on function public.ensure_household() to authenticated;
