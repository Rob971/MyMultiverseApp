-- Profile bootstrap helper for client CRUD paths that must work under RLS.

create or replace function public.ensure_current_profile()
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_email text := auth.jwt() ->> 'email';
    v_display_name text := nullif(split_part(coalesce(v_email, ''), '@', 1), '');
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    insert into public.profiles (id, email, display_name)
    values (v_user_id, v_email, v_display_name)
    on conflict (id) do update
    set email = coalesce(excluded.email, public.profiles.email),
        display_name = coalesce(public.profiles.display_name, excluded.display_name),
        updated_at = now();
end;
$$;

revoke all on function public.ensure_current_profile() from public;
grant execute on function public.ensure_current_profile() to authenticated;
