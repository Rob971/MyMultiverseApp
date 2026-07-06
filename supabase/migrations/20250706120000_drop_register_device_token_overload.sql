-- Drop the legacy register_device_token(text, text) overload.
-- 20250706100000 added register_device_token(text, text, text default null);
-- keeping both makes PostgREST RPC dispatch ambiguous (HTTP 300) for every caller.
-- The 3-arg version defaults p_app_locale, so older clients keep working.

drop function if exists public.register_device_token(text, text);
