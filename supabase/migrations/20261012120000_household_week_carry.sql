-- S4: server-held week carry claim — a household can claim carry-once per week.
-- Prevents a returning device from re-adding items a partner cleared.
-- No user_id column; GDPR cascade handled by FK on households(id).
-- No realtime subscription needed.

create table public.nutrition_household_week_carry (
    household_id uuid not null references public.households (id) on delete cascade,
    week_key     text not null,
    carried_at   timestamptz not null default now(),
    primary key (household_id, week_key)
);

alter table public.nutrition_household_week_carry enable row level security;

create policy nutrition_household_week_carry_select
    on public.nutrition_household_week_carry
    for select
    to authenticated
    using (public.is_household_member(household_id));

create policy nutrition_household_week_carry_insert
    on public.nutrition_household_week_carry
    for insert
    to authenticated
    with check (public.household_member_can_write_nutrition(household_id));

grant select, insert on public.nutrition_household_week_carry to authenticated;