-- Notify household partners when someone adds grocery items or meal-plan slots.

alter table public.user_device_tokens
    add column if not exists app_locale text;

-- ---------------------------------------------------------------------------
-- Payload helpers (matches NutritionStorageCodec separators)
-- ---------------------------------------------------------------------------

create or replace function private.grocery_payload_item_ids(p_payload text)
returns table (item_id text)
language sql
immutable
as $$
    select split_part(rec, chr(31), 1) as item_id
    from unnest(string_to_array(coalesce(p_payload, ''), chr(30))) as rec
    where char_length(trim(rec)) > 0
      and char_length(split_part(rec, chr(31), 1)) > 0;
$$;

create or replace function private.grocery_payload_new_items(p_old text, p_new text)
returns table (item_id text, item_label text)
language sql
immutable
as $$
    select
        split_part(rec, chr(31), 1) as item_id,
        split_part(rec, chr(31), 2) as item_label
    from unnest(string_to_array(coalesce(p_new, ''), chr(30))) as rec
    where char_length(trim(rec)) > 0
      and char_length(split_part(rec, chr(31), 1)) > 0
      and split_part(rec, chr(31), 1) not in (
          select g.item_id from private.grocery_payload_item_ids(p_old) as g
      );
$$;

create or replace function private.meal_plan_new_slots(p_old text, p_new text)
returns table (day_index integer, meal_slot text, meal_label text)
language plpgsql
immutable
as $$
declare
    v_old_days text[];
    v_new_days text[];
    v_i integer;
    v_old_lunch text;
    v_old_dinner text;
    v_new_lunch text;
    v_new_dinner text;
begin
    v_old_days := string_to_array(coalesce(p_old, ''), chr(30));
    v_new_days := string_to_array(coalesce(p_new, ''), chr(30));

    for v_i in 0..6 loop
        v_old_lunch := trim(split_part(coalesce(v_old_days[v_i + 1], ''), chr(31), 1));
        v_old_dinner := trim(split_part(coalesce(v_old_days[v_i + 1], ''), chr(31), 2));
        v_new_lunch := trim(split_part(coalesce(v_new_days[v_i + 1], ''), chr(31), 1));
        v_new_dinner := trim(split_part(coalesce(v_new_days[v_i + 1], ''), chr(31), 2));

        if v_old_lunch = '' and v_new_lunch <> '' then
            day_index := v_i;
            meal_slot := 'lunch';
            meal_label := v_new_lunch;
            return next;
        end if;

        if v_old_dinner = '' and v_new_dinner <> '' then
            day_index := v_i;
            meal_slot := 'dinner';
            meal_label := v_new_dinner;
            return next;
        end if;
    end loop;
end;
$$;

revoke all on function private.grocery_payload_item_ids(text) from public, anon, authenticated, service_role;
revoke all on function private.grocery_payload_new_items(text, text) from public, anon, authenticated, service_role;
revoke all on function private.meal_plan_new_slots(text, text) from public, anon, authenticated, service_role;

-- ---------------------------------------------------------------------------
-- Outbox enqueue on nutrition payload writes
-- ---------------------------------------------------------------------------

create or replace function private.enqueue_nutrition_item_added_notification()
returns trigger
language plpgsql
security definer
set search_path = public, private
as $$
declare
    v_actor_user_id uuid;
    v_actor_name text;
    v_household_name text;
    v_partner_count integer;
    v_added_count integer;
    v_first_label text;
    v_first_day_index integer;
    v_first_meal_slot text;
begin
    if new.data_kind not in ('grocery', 'meal_plan') then
        return new;
    end if;

    if new.updated_by is null then
        return new;
    end if;

    v_actor_user_id := new.updated_by;

    select count(*)
    into v_partner_count
    from public.household_members sm
    where sm.household_id = new.household_id
      and sm.left_at is null
      and sm.user_id is not null
      and sm.user_id <> v_actor_user_id;

    if v_partner_count < 1 then
        return new;
    end if;

    select coalesce(nullif(public.profile_display_label(p.display_name, p.email), ''), 'Member')
    into v_actor_name
    from public.profiles p
    where p.id = v_actor_user_id;

    select s.name
    into v_household_name
    from public.households s
    where s.id = new.household_id;

    if new.data_kind = 'grocery' then
        select count(*)::integer, min(n.item_label)
        into v_added_count, v_first_label
        from private.grocery_payload_new_items(coalesce(old.payload, ''), new.payload) n;

        if v_added_count < 1 then
            return new;
        end if;

        insert into public.household_notification_outbox (kind, payload)
        values (
            'grocery_item_added',
            jsonb_build_object(
                'household_id', new.household_id,
                'household_name', v_household_name,
                'actor_user_id', v_actor_user_id,
                'actor_name', v_actor_name,
                'week_key', new.week_key,
                'item_label', v_first_label,
                'added_count', v_added_count
            )
        );
    else
        select count(*)::integer
        into v_added_count
        from private.meal_plan_new_slots(coalesce(old.payload, ''), new.payload) s;

        if v_added_count < 1 then
            return new;
        end if;

        select s.day_index, s.meal_slot, s.meal_label
        into v_first_day_index, v_first_meal_slot, v_first_label
        from private.meal_plan_new_slots(coalesce(old.payload, ''), new.payload) s
        order by s.day_index, case s.meal_slot when 'lunch' then 0 else 1 end
        limit 1;

        insert into public.household_notification_outbox (kind, payload)
        values (
            'meal_plan_item_added',
            jsonb_build_object(
                'household_id', new.household_id,
                'household_name', v_household_name,
                'actor_user_id', v_actor_user_id,
                'actor_name', v_actor_name,
                'week_key', new.week_key,
                'item_label', v_first_label,
                'added_count', v_added_count,
                'day_index', v_first_day_index,
                'meal_slot', v_first_meal_slot
            )
        );
    end if;

    return new;
end;
$$;

revoke all on function private.enqueue_nutrition_item_added_notification() from public, anon, authenticated, service_role;

drop trigger if exists nutrition_household_week_data_item_added_notify on public.nutrition_household_week_data;

create trigger nutrition_household_week_data_item_added_notify
    after insert or update of payload on public.nutrition_household_week_data
    for each row
    execute function private.enqueue_nutrition_item_added_notification();

-- ---------------------------------------------------------------------------
-- Device token registration with app locale for localized push copy
-- ---------------------------------------------------------------------------

create or replace function public.register_device_token(
    p_platform text,
    p_token text,
    p_app_locale text default null
)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_locale text := nullif(trim(p_app_locale), '');
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    if p_platform not in ('android', 'ios') then
        raise exception 'invalid_platform';
    end if;

    if char_length(trim(p_token)) = 0 then
        raise exception 'device_token_required';
    end if;

    perform public.ensure_current_profile();

    insert into public.user_device_tokens (user_id, platform, token, app_locale)
    values (v_user_id, p_platform, trim(p_token), v_locale)
    on conflict (user_id, platform, token)
    do update set
        updated_at = now(),
        app_locale = coalesce(excluded.app_locale, public.user_device_tokens.app_locale);
end;
$$;

revoke all on function public.register_device_token(text, text, text) from public;
grant execute on function public.register_device_token(text, text, text) to authenticated;
