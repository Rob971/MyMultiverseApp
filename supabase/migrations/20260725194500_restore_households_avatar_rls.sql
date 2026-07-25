-- Ensure households SELECT + UPDATE RLS policies exist for PostgREST avatar_url writes.
--
-- Migration 20250718000000 may have been recorded as applied before the SELECT
-- policy was added to the same file (amended in-place). This follow-up migration
-- is idempotent and safe to run even when policies already exist.

drop policy if exists households_select_member on public.households;
create policy households_select_member
    on public.households
    for select
    to authenticated
    using (public.is_household_member(id));

drop policy if exists households_update_manager on public.households;
create policy households_update_manager
    on public.households
    for update
    to authenticated
    using  (public.is_household_manager(id))
    with check (public.is_household_manager(id));
