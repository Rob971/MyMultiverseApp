-- Enable Supabase Realtime for collaborative nutrition week data.
alter table public.nutrition_space_week_data replica identity full;

do $$
begin
    if not exists (
        select 1
        from pg_publication_tables
        where pubname = 'supabase_realtime'
          and schemaname = 'public'
          and tablename = 'nutrition_space_week_data'
    ) then
        alter publication supabase_realtime add table public.nutrition_space_week_data;
    end if;
end $$;
