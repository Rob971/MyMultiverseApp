-- Push partners to update the shared grocery list before a household member shops.

create or replace function public.nudge_household_grocery_list(
    p_household_id uuid,
    p_week_key text default null
)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_profile_id uuid := auth.uid();
    v_nudger_name text;
    v_household_name text;
    v_partner_count int;
begin
    if v_profile_id is null then
        raise exception 'auth_required';
    end if;

    perform public.ensure_current_profile();

    if not public.household_member_can_write_nutrition(p_household_id) then
        raise exception 'insufficient_role';
    end if;

    select count(*)
    into v_partner_count
    from public.household_members sm
    where sm.household_id = p_household_id
      and sm.left_at is null
      and sm.user_id is not null;

    if v_partner_count < 2 then
        raise exception 'grocery_nudge_no_partners';
    end if;

    if exists (
        select 1
        from public.household_notification_outbox o
        where o.kind = 'grocery_list_nudge'
          and o.payload->>'nudger_user_id' = v_profile_id::text
          and o.payload->>'household_id' = p_household_id::text
          and o.created_at > now() - interval '1 hour'
    ) then
        raise exception 'grocery_nudge_cooldown';
    end if;

    select coalesce(nullif(trim(p.display_name), ''), nullif(trim(p.email), ''), 'Member')
    into v_nudger_name
    from public.profiles p
    where p.id = v_profile_id;

    select s.name
    into v_household_name
    from public.households s
    where s.id = p_household_id;

    insert into public.household_notification_outbox (kind, payload)
    values (
        'grocery_list_nudge',
        jsonb_build_object(
            'household_id', p_household_id,
            'household_name', v_household_name,
            'nudger_user_id', v_profile_id,
            'nudger_name', v_nudger_name,
            'week_key', nullif(trim(p_week_key), '')
        )
    );
end;
$$;

revoke all on function public.nudge_household_grocery_list(uuid, text) from public;
grant execute on function public.nudge_household_grocery_list(uuid, text) to authenticated;
