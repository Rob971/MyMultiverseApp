-- Restore the households table UPDATE policy that was silently removed.
--
-- History: migration 20250615120000 created `sharing_spaces_update` on what is
-- now `public.households`, guarded by `is_space_owner(id)`.  Migration
-- 20250620000000 ran `DROP FUNCTION … is_space_owner(uuid) CASCADE` to clean up
-- the old terminology — the CASCADE silently dropped the UPDATE policy along
-- with the function.  No replacement was ever added, meaning:
--   • `postgrest["households"].update(...)` always touched 0 rows (RLS blocked).
--   • The household avatar URL appeared to save (in-memory state patched) but
--     was lost on the next app launch because the DB column was never written.
--   • Similarly, any direct PostgREST UPDATE on `households` (e.g. renaming via
--     a future direct call instead of the SECURITY DEFINER RPC) would also fail.
--
-- Fix: create `households_update_manager` using the current `is_household_manager`
-- helper (owner OR admin), matching the storage-level policy added in
-- migration 20250714100000 for the household avatar bucket folder.

drop policy if exists households_update_manager on public.households;

create policy households_update_manager
    on public.households
    for update
    to authenticated
    using  (public.is_household_manager(id))
    with check (public.is_household_manager(id));
