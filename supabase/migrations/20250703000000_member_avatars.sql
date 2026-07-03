-- Family member profile photos: dependant avatar_url column + storage bucket.

alter table public.household_dependants
    add column if not exists avatar_url text;

create or replace function public.can_upload_dependant_avatar(p_dependant_id uuid)
returns boolean
language sql
stable
security invoker
set search_path = public
as $$
    select exists (
        select 1
        from public.household_dependants hd
        where hd.id = p_dependant_id
          and hd.removed_at is null
          and public.household_member_can_write_nutrition(hd.household_id)
    );
$$;

revoke all on function public.can_upload_dependant_avatar(uuid) from public;
grant execute on function public.can_upload_dependant_avatar(uuid) to authenticated;

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
    'member-avatars',
    'member-avatars',
    true,
    5242880,
    array['image/jpeg', 'image/png', 'image/webp']
)
on conflict (id) do update
set
    public = excluded.public,
    file_size_limit = excluded.file_size_limit,
    allowed_mime_types = excluded.allowed_mime_types;

drop policy if exists member_avatars_profile_insert on storage.objects;
drop policy if exists member_avatars_profile_update on storage.objects;
drop policy if exists member_avatars_profile_select on storage.objects;
drop policy if exists member_avatars_dependant_insert on storage.objects;
drop policy if exists member_avatars_dependant_update on storage.objects;
drop policy if exists member_avatars_dependant_select on storage.objects;

create policy member_avatars_profile_insert
    on storage.objects
    for insert
    to authenticated
    with check (
        bucket_id = 'member-avatars'
        and (storage.foldername(name))[1] = 'profiles'
        and (storage.foldername(name))[2] = (select auth.uid()::text)
    );

create policy member_avatars_profile_update
    on storage.objects
    for update
    to authenticated
    using (
        bucket_id = 'member-avatars'
        and (storage.foldername(name))[1] = 'profiles'
        and (storage.foldername(name))[2] = (select auth.uid()::text)
    )
    with check (
        bucket_id = 'member-avatars'
        and (storage.foldername(name))[1] = 'profiles'
        and (storage.foldername(name))[2] = (select auth.uid()::text)
    );

create policy member_avatars_profile_select
    on storage.objects
    for select
    to authenticated
    using (
        bucket_id = 'member-avatars'
        and (storage.foldername(name))[1] = 'profiles'
        and (storage.foldername(name))[2] = (select auth.uid()::text)
    );

create policy member_avatars_dependant_insert
    on storage.objects
    for insert
    to authenticated
    with check (
        bucket_id = 'member-avatars'
        and (storage.foldername(name))[1] = 'dependants'
        and public.can_upload_dependant_avatar(((storage.foldername(name))[2])::uuid)
    );

create policy member_avatars_dependant_update
    on storage.objects
    for update
    to authenticated
    using (
        bucket_id = 'member-avatars'
        and (storage.foldername(name))[1] = 'dependants'
        and public.can_upload_dependant_avatar(((storage.foldername(name))[2])::uuid)
    )
    with check (
        bucket_id = 'member-avatars'
        and (storage.foldername(name))[1] = 'dependants'
        and public.can_upload_dependant_avatar(((storage.foldername(name))[2])::uuid)
    );

create policy member_avatars_dependant_select
    on storage.objects
    for select
    to authenticated
    using (
        bucket_id = 'member-avatars'
        and (storage.foldername(name))[1] = 'dependants'
        and public.can_upload_dependant_avatar(((storage.foldername(name))[2])::uuid)
    );
