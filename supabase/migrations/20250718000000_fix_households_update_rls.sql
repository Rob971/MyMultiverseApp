-- Restore households table RLS policies dropped by CASCADE.
--
-- History: migration 20250615120000 created `sharing_spaces_select` and
-- `sharing_spaces_update` on what is now `public.households`, both guarded by
-- functions that used `is_space_member` / `is_space_owner` respectively.
-- Migration 20250620000000 ran:
--     DROP FUNCTION … is_space_owner(uuid) CASCADE
--     DROP FUNCTION … is_space_member(uuid) CASCADE
-- Both CASCADEs silently dropped the SELECT and UPDATE policies.  No
-- replacements were added, meaning:
--
-- ① PostgREST's UPDATE path requires a SELECT policy to first determine which
--   rows are eligible.  Without one, `postgrest["households"].update(...)` hits
--   0 rows and returns silently — the Kotlin `Result` is Success but the DB
--   column is never written.
--
-- ② Even with an UPDATE policy, PostgREST cannot SELECT the row to verify its
--   existence before updating.
--
-- Effect: household family-photo uploads appeared to work in-session (the app
-- patched its local StateFlow) but vanished on every restart because the DB
-- `avatar_url` column was never updated.
--
-- Fix: restore both policies using the current `is_household_member` /
-- `is_household_manager` helpers introduced in migration 20250623100001.

-- SELECT: any household member can read their household row.
-- Required by PostgREST to make UPDATE work (not just SELECT endpoints).
drop policy if exists households_select_member on public.households;
create policy households_select_member
    on public.households
    for select
    to authenticated
    using (public.is_household_member(id));

-- UPDATE: only household managers (owner or admin) may write to the row.
drop policy if exists households_update_manager on public.households;
create policy households_update_manager
    on public.households
    for update
    to authenticated
    using  (public.is_household_manager(id))
    with check (public.is_household_manager(id));
