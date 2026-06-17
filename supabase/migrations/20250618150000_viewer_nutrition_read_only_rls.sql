-- Viewers may SELECT shared nutrition data but cannot INSERT/UPDATE/DELETE.

create or replace function public.space_member_can_write_nutrition(p_space_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select public.is_space_owner(p_space_id)
        or exists (
            select 1
            from public.space_members sm
            where sm.space_id = p_space_id
              and sm.user_id = (select auth.uid())
              and sm.left_at is null
              and sm.role = 'editor'
        );
$$;

drop policy if exists nutrition_space_week_data_insert on public.nutrition_space_week_data;
drop policy if exists nutrition_space_week_data_update on public.nutrition_space_week_data;
drop policy if exists nutrition_space_week_data_delete on public.nutrition_space_week_data;

create policy nutrition_space_week_data_insert
    on public.nutrition_space_week_data
    for insert
    to authenticated
    with check (public.space_member_can_write_nutrition(space_id));

create policy nutrition_space_week_data_update
    on public.nutrition_space_week_data
    for update
    to authenticated
    using (public.space_member_can_write_nutrition(space_id))
    with check (public.space_member_can_write_nutrition(space_id));

create policy nutrition_space_week_data_delete
    on public.nutrition_space_week_data
    for delete
    to authenticated
    using (public.space_member_can_write_nutrition(space_id));

revoke all on function public.space_member_can_write_nutrition(uuid) from public;
grant execute on function public.space_member_can_write_nutrition(uuid) to authenticated;
