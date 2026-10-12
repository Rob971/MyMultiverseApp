-- S3 SQL smoke test
-- Run against a Supabase DB with the version migration applied.

-- 1. Insert a row and verify version = 1 (column default)
insert into public.nutrition_household_week_data (household_id, week_key, data_kind, payload)
values ('00000000-0000-0000-0000-000000000001', '2026-W42', 'grocery', 'test-payload')
on conflict (household_id, week_key, data_kind) do nothing;
-- Check: version should be 1
select id, version from public.nutrition_household_week_data
where household_id = '00000000-0000-0000-0000-000000000001'
  and week_key = '2026-W42' and data_kind = 'grocery';

-- 2. UPDATE with stale version should affect 0 rows
update public.nutrition_household_week_data
set payload = 'stale-update'
where household_id = '00000000-0000-0000-0000-000000000001'
  and week_key = '2026-W42' and data_kind = 'grocery'
  and version = 999;
-- Check: should say "UPDATE 0"
-- Row version should still be 1
select version from public.nutrition_household_week_data
where household_id = '00000000-0000-0000-0000-000000000001'
  and week_key = '2026-W42' and data_kind = 'grocery';

-- 3. UPDATE with correct version should affect 1 row
update public.nutrition_household_week_data
set payload = 'correct-update'
where household_id = '00000000-0000-0000-0000-000000000001'
  and week_key = '2026-W42' and data_kind = 'grocery'
  and version = 1;
-- Check: should say "UPDATE 1"
-- Version should now be 2 (bumped by trigger)
select version from public.nutrition_household_week_data
where household_id = '00000000-0000-0000-0000-000000000001'
  and week_key = '2026-W42' and data_kind = 'grocery';

-- 4. Blind upsert (INSERT ... ON CONFLICT DO UPDATE) bumps version
insert into public.nutrition_household_week_data (household_id, week_key, data_kind, payload)
values ('00000000-0000-0000-0000-000000000001', '2026-W42', 'grocery', 'blind-upsert')
on conflict (household_id, week_key, data_kind)
do update set payload = excluded.payload;
-- Version should now be 3 (bumped from 2 by the ON CONFLICT trigger)
select version from public.nutrition_household_week_data
where household_id = '00000000-0000-0000-0000-000000000001'
  and week_key = '2026-W42' and data_kind = 'grocery';

-- 5. Viewer (not a household member) gets 42501 on select
-- (test by verifying RLS policy exists and is enforced)
-- This requires a real auth context; stating the expected error:
-- ERROR:  new row for relation "nutrition_household_week_data" violates
--   row-level security policy for table "nutrition_household_week_data" (42501)

-- Cleanup
delete from public.nutrition_household_week_data
where household_id = '00000000-0000-0000-0000-000000000001'
  and week_key = '2026-W42' and data_kind = 'grocery';
