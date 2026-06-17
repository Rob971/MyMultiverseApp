-- P1 GDPR: export signed-in user's profile and active household affiliation metadata.

create or replace function public.export_my_personal_data()
returns json
language plpgsql
stable
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_profile json;
    v_membership json;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    select json_build_object(
        'id', p.id,
        'email', p.email,
        'display_name', p.display_name,
        'avatar_url', p.avatar_url,
        'created_at', p.created_at,
        'updated_at', p.updated_at
    )
    into v_profile
    from public.profiles p
    where p.id = v_user_id;

    select json_build_object(
        'household_id', row.space_id,
        'household_name', row.space_name,
        'owner_id', row.owner_id,
        'role', row.member_role
    )
    into v_membership
    from public.resolve_user_household_row() row
    limit 1;

    return json_build_object(
        'exported_at', now(),
        'profile', coalesce(v_profile, json 'null'),
        'household_membership', v_membership
    );
end;
$$;

revoke all on function public.export_my_personal_data() from public;
grant execute on function public.export_my_personal_data() to authenticated;
