-- F2 GDPR: include the user's favorite dishes in the personal data export.
-- The export function lists its tables explicitly, so a new table must be added here.

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
    v_favorites json;
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
        'household_id', row.household_id,
        'household_name', row.household_name,
        'owner_id', row.owner_id,
        'role', row.member_role
    )
    into v_membership
    from public.resolve_user_household_row() row
    limit 1;

    select coalesce(json_agg(f.label order by f.created_at), json '[]')
    into v_favorites
    from public.user_favorite_dishes f
    where f.user_id = v_user_id;

    return json_build_object(
        'exported_at', now(),
        'profile', coalesce(v_profile, json 'null'),
        'household_membership', v_membership,
        'favorite_dishes', v_favorites
    );
end;
$$;

revoke all on function public.export_my_personal_data() from public;
grant execute on function public.export_my_personal_data() to authenticated;