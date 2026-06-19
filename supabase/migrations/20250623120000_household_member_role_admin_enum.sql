-- Enum value must be committed before functions reference 'admin' (Postgres 55P04).
alter type public.household_member_role add value if not exists 'admin';
