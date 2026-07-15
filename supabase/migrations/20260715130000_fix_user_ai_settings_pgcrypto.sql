-- Fix Gemini key RPCs: pgcrypto lives in the extensions schema on Supabase.
-- With search_path = '' (security hardening), unqualified pgp_sym_* calls fail with
-- "function pgp_sym_encrypt(text, text) does not exist", so upserts never persisted.

create extension if not exists pgcrypto with schema extensions;

create or replace function public.get_gemini_api_key()
returns table (key text)
language plpgsql
security invoker
set search_path = ''
as $$
declare
    v_enc  bytea;
    v_pass text;
begin
    select coalesce(current_setting('app.ai_key_secret', true), 'ammo-ai-key-v1')
    into v_pass;

    select gemini_key
    into   v_enc
    from   public.user_ai_settings
    where  user_id = (select auth.uid());

    if v_enc is null then
        return;
    end if;

    return query select extensions.pgp_sym_decrypt(v_enc, v_pass);
end;
$$;

create or replace function public.upsert_gemini_api_key(p_key text)
returns void
language plpgsql
security invoker
set search_path = ''
as $$
declare
    v_pass text;
begin
    select coalesce(current_setting('app.ai_key_secret', true), 'ammo-ai-key-v1')
    into v_pass;

    if p_key is null or trim(p_key) = '' then
        delete from public.user_ai_settings
        where user_id = (select auth.uid());
    else
        insert into public.user_ai_settings (user_id, gemini_key, updated_at)
        values (
            (select auth.uid()),
            extensions.pgp_sym_encrypt(trim(p_key), v_pass),
            now()
        )
        on conflict (user_id) do update
            set gemini_key = excluded.gemini_key,
                updated_at = excluded.updated_at;
    end if;
end;
$$;

revoke all on function public.get_gemini_api_key()          from public;
revoke all on function public.upsert_gemini_api_key(text)   from public;
grant execute on function public.get_gemini_api_key()        to authenticated;
grant execute on function public.upsert_gemini_api_key(text) to authenticated;
