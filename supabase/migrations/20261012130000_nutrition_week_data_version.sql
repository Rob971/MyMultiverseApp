-- S3: version column for optimistic concurrency on grocery/ai_grocery writes.
-- Every UPSERT bumps version so new clients can detect stale-write conditions.

alter table public.nutrition_household_week_data
    add column version bigint not null default 1;

-- Update the existing trigger to bump version on every row update.
-- The trigger fires for INSERT ... ON CONFLICT DO UPDATE as well, so a blind
-- upsert from an old client still bumps the version.
create or replace function public.set_nutrition_week_data_updated_at()
returns trigger
language plpgsql
set search_path = public
as $$
begin
    new.updated_at = now();
    new.version = old.version + 1;
    return new;
end;
$$;
