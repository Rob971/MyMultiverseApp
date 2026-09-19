-- Daily request limits for the ai-generate Edge Function (the server-held Gemini key).
-- Only the function touches this, through the service role: clients can neither read the
-- counters nor call consume_ai_quota, so they cannot inspect, reset or burn anyone's quota.

create table public.ai_usage_daily (
    day      date    not null,
    -- The all-zero uuid row holds the project-wide total for the day.
    user_id  uuid    not null,
    requests integer not null default 0,
    primary key (day, user_id)
);

alter table public.ai_usage_daily enable row level security;
revoke all on public.ai_usage_daily from anon, authenticated;

-- Counts one request for p_user_id and for the project, today (UTC). Raises
-- 'ai_global_limit' or 'ai_user_limit' when a limit would be exceeded; the raise rolls both
-- increments back, so refused requests are not counted. The upserts lock their rows, so
-- concurrent requests are counted exactly.
create or replace function public.consume_ai_quota(
    p_user_id uuid,
    p_user_limit integer,
    p_global_limit integer
)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_day    date := (now() at time zone 'utc')::date;
    v_global integer;
    v_user   integer;
begin
    if p_user_id is null then
        raise exception 'ai_user_required';
    end if;

    insert into public.ai_usage_daily as u (day, user_id, requests)
    values (v_day, '00000000-0000-0000-0000-000000000000', 1)
    on conflict (day, user_id) do update set requests = u.requests + 1
    returning requests into v_global;
    if v_global > p_global_limit then
        raise exception 'ai_global_limit';
    end if;

    insert into public.ai_usage_daily as u (day, user_id, requests)
    values (v_day, p_user_id, 1)
    on conflict (day, user_id) do update set requests = u.requests + 1
    returning requests into v_user;
    if v_user > p_user_limit then
        raise exception 'ai_user_limit';
    end if;
end;
$$;

revoke all on function public.consume_ai_quota(uuid, integer, integer) from public, anon, authenticated;
grant execute on function public.consume_ai_quota(uuid, integer, integer) to service_role;
