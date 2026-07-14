-- Household family photo: avatar_url column on households + storage policies.

alter table public.households
    add column if not exists avatar_url text;

-- Update household_membership_status to include avatar_url.
create or replace function public.household_membership_status()
returns json
language plpgsql
security definer
set search_path = public
as $$
declare
    v_row record;
    v_features json;
    v_avatar_url text;
begin
    perform public.ensure_current_profile();

    select * into v_row
    from public.resolve_user_household_row();

    if not found then
        return json_build_object('status', 'none');
    end if;

    select coalesce(json_agg(feature order by feature), '[]'::json)
    into v_features
    from public.household_modules
    where household_id = v_row.household_id;

    select h.avatar_url into v_avatar_url
    from public.households h
    where h.id = v_row.household_id;

    return json_build_object(
        'status', 'active',
        'household_id', v_row.household_id,
        'household_name', v_row.household_name,
        'owner_id', v_row.owner_id,
        'owner_display_name', v_row.owner_display_name,
        'role', v_row.member_role,
        'features', v_features,
        'avatar_url', v_avatar_url
    );
end;
$$;

revoke all on function public.household_membership_status() from public;
grant execute on function public.household_membership_status() to authenticated;

-- Storage policies: household managers (owner or admin) can upload/replace
-- the household photo at member-avatars/households/{household_id}/avatar.*
-- All household members can read it (public bucket, but RLS scoped to members).

drop policy if exists member_avatars_household_insert on storage.objects;
drop policy if exists member_avatars_household_update on storage.objects;
drop policy if exists member_avatars_household_select on storage.objects;

create policy member_avatars_household_insert
    on storage.objects
    for insert
    to authenticated
    with check (
        bucket_id = 'member-avatars'
        and (storage.foldername(name))[1] = 'households'
        and public.is_household_manager(((storage.foldername(name))[2])::uuid)
    );

create policy member_avatars_household_update
    on storage.objects
    for update
    to authenticated
    using (
        bucket_id = 'member-avatars'
        and (storage.foldername(name))[1] = 'households'
        and public.is_household_manager(((storage.foldername(name))[2])::uuid)
    )
    with check (
        bucket_id = 'member-avatars'
        and (storage.foldername(name))[1] = 'households'
        and public.is_household_manager(((storage.foldername(name))[2])::uuid)
    );

create policy member_avatars_household_select
    on storage.objects
    for select
    to authenticated
    using (
        bucket_id = 'member-avatars'
        and (storage.foldername(name))[1] = 'households'
        and public.is_household_member(((storage.foldername(name))[2])::uuid)
    );
