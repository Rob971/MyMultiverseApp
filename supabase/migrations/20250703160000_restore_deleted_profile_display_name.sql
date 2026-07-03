-- Restore profile display names when a user signs back in after prepare_account_deletion
-- left the sentinel value but auth was not fully removed.

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
        display_name = case
            when trim(coalesce(public.profiles.display_name, '')) = 'Deleted user' then
                coalesce(
                    nullif(trim(excluded.display_name), ''),
                    nullif(
                        split_part(
                            coalesce(excluded.email, public.profiles.email, auth.jwt() ->> 'email'),
                            '@',
                            1
                        ),
                        ''
                    )
                )
            else coalesce(public.profiles.display_name, excluded.display_name)
        end,
        updated_at = now();
end;
$$;
