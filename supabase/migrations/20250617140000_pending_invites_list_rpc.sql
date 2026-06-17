-- List pending household invites for users without an active nutrition household.

drop policy if exists space_invites_select on public.space_invites;
create policy space_invites_select
    on public.space_invites
    for select
    to authenticated
    using (
        public.is_space_owner(space_id)
        or lower(trim(email)) = lower(trim(coalesce(
            (select p.email from public.profiles p where p.id = auth.uid()),
            auth.jwt() ->> 'email',
            ''
        )))
    );

create or replace function public.list_my_pending_space_invites()
returns json
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_profile_email text;
    v_result json;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    perform public.ensure_current_profile();

    if exists (select 1 from public.resolve_user_household_row()) then
        return '[]'::json;
    end if;

    select lower(trim(coalesce(p.email, auth.jwt() ->> 'email', '')))
    into v_profile_email
    from public.profiles p
    where p.id = v_user_id;

    if v_profile_email is null or char_length(v_profile_email) = 0 then
        return '[]'::json;
    end if;

    select coalesce(
        json_agg(
            json_build_object(
                'id', i.id,
                'space_id', i.space_id,
                'space_name', s.name,
                'email', i.email,
                'role', i.role::text,
                'expires_at', i.expires_at
            )
            order by s.name
        ),
        '[]'::json
    )
    into v_result
    from public.space_invites i
    join public.sharing_spaces s on s.id = i.space_id
    where lower(trim(i.email)) = v_profile_email
      and i.accepted_at is null
      and i.declined_at is null
      and i.expires_at > now();

    return v_result;
end;
$$;

revoke all on function public.list_my_pending_space_invites() from public;
grant execute on function public.list_my_pending_space_invites() to authenticated;
