create type public.nutrition_data_kind as enum ('grocery', 'ai_grocery', 'meal_plan');

create table public.nutrition_space_week_data (
    space_id uuid not null references public.sharing_spaces (id) on delete cascade,
    week_key text not null,
    data_kind public.nutrition_data_kind not null,
    payload text not null default '',
    updated_at timestamptz not null default now(),
    updated_by uuid references public.profiles (id),
    primary key (space_id, week_key, data_kind)
);

create index nutrition_space_week_data_space_week_idx
    on public.nutrition_space_week_data (space_id, week_key);

alter table public.nutrition_space_week_data enable row level security;

create policy nutrition_space_week_data_select
    on public.nutrition_space_week_data
    for select
    to authenticated
    using (public.is_space_member(space_id));

create policy nutrition_space_week_data_insert
    on public.nutrition_space_week_data
    for insert
    to authenticated
    with check (public.is_space_member(space_id));

create policy nutrition_space_week_data_update
    on public.nutrition_space_week_data
    for update
    to authenticated
    using (public.is_space_member(space_id))
    with check (public.is_space_member(space_id));

create policy nutrition_space_week_data_delete
    on public.nutrition_space_week_data
    for delete
    to authenticated
    using (public.is_space_member(space_id));

grant select, insert, update, delete on public.nutrition_space_week_data to authenticated;
grant usage on type public.nutrition_data_kind to authenticated;

create or replace function public.find_profile_id_by_email(p_email text)
returns uuid
language sql
stable
security definer
set search_path = public
as $$
    select id
    from public.profiles
    where lower(email) = lower(trim(p_email))
    limit 1;
$$;

revoke all on function public.find_profile_id_by_email(text) from public;
grant execute on function public.find_profile_id_by_email(text) to authenticated;

create or replace function public.profiles_share_space(p_profile_id uuid, p_viewer_id uuid)
returns boolean
language sql
stable
security invoker
set search_path = public
as $$
    select p_profile_id = p_viewer_id
        or exists (
            select 1
            from public.space_members sm_viewer
            join public.space_members sm_peer on sm_peer.space_id = sm_viewer.space_id
            where sm_viewer.user_id = p_viewer_id
              and sm_peer.user_id = p_profile_id
        )
        or exists (
            select 1
            from public.sharing_spaces s
            where s.owner_id = p_viewer_id
              and (
                  exists (
                      select 1 from public.space_members sm
                      where sm.space_id = s.id and sm.user_id = p_profile_id
                  )
                  or s.owner_id = p_profile_id
              )
        );
$$;

create policy profiles_select_space_peers
    on public.profiles
    for select
    to authenticated
    using (public.profiles_share_space(id, (select auth.uid())));
