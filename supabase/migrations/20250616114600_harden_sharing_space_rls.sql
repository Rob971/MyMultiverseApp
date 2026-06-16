-- Harden sharing-space RLS helpers used by policies and allow profile bootstrap upserts.

do $$
begin
    if not exists (
        select 1
        from pg_policies
        where schemaname = 'public'
          and tablename = 'profiles'
          and policyname = 'profiles_insert_own'
    ) then
        create policy profiles_insert_own
            on public.profiles
            for insert
            to authenticated
            with check ((select auth.uid()) = id);
    end if;
end $$;

create or replace function public.is_space_owner(p_space_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select exists (
        select 1
        from public.sharing_spaces s
        where s.id = p_space_id
          and s.owner_id = (select auth.uid())
    );
$$;

create or replace function public.is_group_owner(p_group_id uuid)
returns boolean
language sql
stable
security definer
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
security definer
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

create or replace function public.is_space_member(p_space_id uuid)
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
        )
        or exists (
            select 1
            from public.space_members sm
            join public.group_members gm on gm.group_id = sm.group_id
            where sm.space_id = p_space_id
              and gm.user_id = (select auth.uid())
        );
$$;

revoke all on function public.is_space_owner(uuid) from public;
revoke all on function public.is_space_member(uuid) from public;
revoke all on function public.is_group_owner(uuid) from public;
revoke all on function public.is_group_member(uuid) from public;

grant execute on function public.is_space_owner(uuid) to authenticated;
grant execute on function public.is_space_member(uuid) to authenticated;
grant execute on function public.is_group_owner(uuid) to authenticated;
grant execute on function public.is_group_member(uuid) to authenticated;
