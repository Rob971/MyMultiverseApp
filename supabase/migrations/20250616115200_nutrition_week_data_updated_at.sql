-- Keep nutrition week data timestamps current so clients can load the latest payload.

create or replace function public.set_nutrition_week_data_updated_at()
returns trigger
language plpgsql
set search_path = public
as $$
begin
    new.updated_at = now();
    return new;
end;
$$;

drop trigger if exists nutrition_space_week_data_set_updated_at on public.nutrition_space_week_data;

create trigger nutrition_space_week_data_set_updated_at
    before update on public.nutrition_space_week_data
    for each row
    execute function public.set_nutrition_week_data_updated_at();

revoke all on function public.set_nutrition_week_data_updated_at() from public;
