-- Sharing foundation: profiles, contact groups, nutrition sharing spaces, RLS.

create type public.group_lifecycle as enum ('persistent', 'event');
create type public.app_topic as enum ('nutrition', 'adventures', 'budget');
create type public.nutrition_feature as enum ('grocery', 'meal_plan', 'ai_advice');
create type public.space_member_role as enum ('owner', 'editor', 'viewer');

create table public.profiles (
    id uuid primary key references auth.users (id) on delete cascade,
    display_name text,
    email text,
    avatar_url text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table public.contact_groups (
    id uuid primary key default gen_random_uuid(),
    owner_id uuid not null references public.profiles (id) on delete cascade,
    name text not null,
    lifecycle public.group_lifecycle not null default 'persistent',
    event_label text,
    starts_at timestamptz,
    expires_at timestamptz,
    archived_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint contact_groups_name_not_blank check (char_length(trim(name)) > 0)
);

create table public.group_members (
    id uuid primary key default gen_random_uuid(),
    group_id uuid not null references public.contact_groups (id) on delete cascade,
    user_id uuid not null references public.profiles (id) on delete cascade,
    created_at timestamptz not null default now(),
    unique (group_id, user_id)
);

create table public.sharing_spaces (
    id uuid primary key default gen_random_uuid(),
    topic public.app_topic not null,
    name text not null,
    owner_id uuid not null references public.profiles (id) on delete cascade,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint sharing_spaces_name_not_blank check (char_length(trim(name)) > 0)
);

create table public.space_members (
    id uuid primary key default gen_random_uuid(),
    space_id uuid not null references public.sharing_spaces (id) on delete cascade,
    user_id uuid references public.profiles (id) on delete cascade,
    group_id uuid references public.contact_groups (id) on delete cascade,
    role public.space_member_role not null default 'editor',
    created_at timestamptz not null default now(),
    constraint space_members_user_or_group check (
        (user_id is not null and group_id is null)
        or (user_id is null and group_id is not null)
    )
);

create unique index space_members_user_unique
    on public.space_members (space_id, user_id)
    where user_id is not null;

create unique index space_members_group_unique
    on public.space_members (space_id, group_id)
    where group_id is not null;

create table public.space_nutrition_features (
    space_id uuid not null references public.sharing_spaces (id) on delete cascade,
    feature public.nutrition_feature not null,
    primary key (space_id, feature)
);

create index sharing_spaces_topic_owner_idx on public.sharing_spaces (topic, owner_id);
create index contact_groups_owner_idx on public.contact_groups (owner_id);
create index group_members_user_idx on public.group_members (user_id);
create index space_members_space_idx on public.space_members (space_id);

create or replace function public.is_space_owner(p_space_id uuid)
returns boolean
language sql
stable
security invoker
set search_path = public
as $$
    select exists (
        select 1
        from public.sharing_spaces s
        where s.id = p_space_id
          and s.owner_id = (select auth.uid())
    );
$$;

create or replace function public.is_space_member(p_space_id uuid)
returns boolean
language sql
stable
security invoker
set search_path = public
as $$
    select public.is_space_owner(p_space_id)
        or exists (
            select 1
            from public.space_members sm
            where sm.space_id = p_space_id
              and sm.user_id = (select auth.uid())
        )
        or exists (
            select 1
            from public.space_members sm
            join public.group_members gm on gm.group_id = sm.group_id
            where sm.space_id = p_space_id
              and gm.user_id = (select auth.uid())
        );
$$;

create or replace function public.is_group_owner(p_group_id uuid)
returns boolean
language sql
stable
security invoker
set search_path = public
as $$
    select exists (
        select 1
        from public.contact_groups g
        where g.id = p_group_id
          and g.owner_id = (select auth.uid())
    );
$$;

create or replace function public.is_group_member(p_group_id uuid)
returns boolean
language sql
stable
security invoker
set search_path = public
as $$
    select public.is_group_owner(p_group_id)
        or exists (
            select 1
            from public.group_members gm
            where gm.group_id = p_group_id
              and gm.user_id = (select auth.uid())
        );
$$;

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    insert into public.profiles (id, email, display_name)
    values (
        new.id,
        new.email,
        coalesce(
            new.raw_user_meta_data ->> 'full_name',
            new.raw_user_meta_data ->> 'name',
            split_part(new.email, '@', 1)
        )
    );
    return new;
end;
$$;

create trigger on_auth_user_created
    after insert on auth.users
    for each row
    execute function public.handle_new_user();

alter table public.profiles enable row level security;
alter table public.contact_groups enable row level security;
alter table public.group_members enable row level security;
alter table public.sharing_spaces enable row level security;
alter table public.space_members enable row level security;
alter table public.space_nutrition_features enable row level security;

create policy profiles_select_own
    on public.profiles
    for select
    to authenticated
    using ((select auth.uid()) = id);

create policy profiles_update_own
    on public.profiles
    for update
    to authenticated
    using ((select auth.uid()) = id)
    with check ((select auth.uid()) = id);

create policy contact_groups_select
    on public.contact_groups
    for select
    to authenticated
    using (public.is_group_member(id));

create policy contact_groups_insert
    on public.contact_groups
    for insert
    to authenticated
    with check ((select auth.uid()) = owner_id);

create policy contact_groups_update
    on public.contact_groups
    for update
    to authenticated
    using (public.is_group_owner(id))
    with check (public.is_group_owner(id));

create policy contact_groups_delete
    on public.contact_groups
    for delete
    to authenticated
    using (public.is_group_owner(id));

create policy group_members_select
    on public.group_members
    for select
    to authenticated
    using (public.is_group_member(group_id));

create policy group_members_insert
    on public.group_members
    for insert
    to authenticated
    with check (public.is_group_owner(group_id));

create policy group_members_delete
    on public.group_members
    for delete
    to authenticated
    using (public.is_group_owner(group_id));

create policy sharing_spaces_select
    on public.sharing_spaces
    for select
    to authenticated
    using (public.is_space_member(id));

create policy sharing_spaces_insert
    on public.sharing_spaces
    for insert
    to authenticated
    with check ((select auth.uid()) = owner_id);

create policy sharing_spaces_update
    on public.sharing_spaces
    for update
    to authenticated
    using (public.is_space_owner(id))
    with check (public.is_space_owner(id));

create policy sharing_spaces_delete
    on public.sharing_spaces
    for delete
    to authenticated
    using (public.is_space_owner(id));

create policy space_members_select
    on public.space_members
    for select
    to authenticated
    using (public.is_space_member(space_id));

create policy space_members_insert
    on public.space_members
    for insert
    to authenticated
    with check (public.is_space_owner(space_id));

create policy space_members_update
    on public.space_members
    for update
    to authenticated
    using (public.is_space_owner(space_id))
    with check (public.is_space_owner(space_id));

create policy space_members_delete
    on public.space_members
    for delete
    to authenticated
    using (public.is_space_owner(space_id));

create policy space_nutrition_features_select
    on public.space_nutrition_features
    for select
    to authenticated
    using (public.is_space_member(space_id));

create policy space_nutrition_features_insert
    on public.space_nutrition_features
    for insert
    to authenticated
    with check (public.is_space_owner(space_id));

create policy space_nutrition_features_update
    on public.space_nutrition_features
    for update
    to authenticated
    using (public.is_space_owner(space_id))
    with check (public.is_space_owner(space_id));

create policy space_nutrition_features_delete
    on public.space_nutrition_features
    for delete
    to authenticated
    using (public.is_space_owner(space_id));

grant usage on schema public to authenticated;
grant select, insert, update, delete on public.profiles to authenticated;
grant select, insert, update, delete on public.contact_groups to authenticated;
grant select, insert, delete on public.group_members to authenticated;
grant select, insert, update, delete on public.sharing_spaces to authenticated;
grant select, insert, update, delete on public.space_members to authenticated;
grant select, insert, update, delete on public.space_nutrition_features to authenticated;

grant usage on type public.group_lifecycle to authenticated;
grant usage on type public.app_topic to authenticated;
grant usage on type public.nutrition_feature to authenticated;
grant usage on type public.space_member_role to authenticated;
