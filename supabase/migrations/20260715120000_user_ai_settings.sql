-- User AI settings: stores the Gemini API key per user, encrypted at rest with pgcrypto.
-- The key is personal and is never shared with other household members.
-- Owner-only RLS ensures no cross-user access even within the same household.

create extension if not exists pgcrypto;

create table public.user_ai_settings (
    user_id     uuid primary key references auth.users (id) on delete cascade,
    gemini_key  bytea,
    updated_at  timestamptz not null default now()
);

alter table public.user_ai_settings enable row level security;

create policy "owner_select" on public.user_ai_settings
    for select
    to authenticated
    using ((select auth.uid()) = user_id);

create policy "owner_insert" on public.user_ai_settings
    for insert
    to authenticated
    with check ((select auth.uid()) = user_id);

create policy "owner_update" on public.user_ai_settings
    for update
    to authenticated
    using  ((select auth.uid()) = user_id)
    with check ((select auth.uid()) = user_id);

create policy "owner_delete" on public.user_ai_settings
    for delete
    to authenticated
    using ((select auth.uid()) = user_id);

-- Returns the decrypted Gemini API key for the current user.
-- Returns an empty result set when no key has been stored.
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

    return query select pgp_sym_decrypt(v_enc, v_pass);
end;
$$;

-- Inserts or replaces the Gemini API key for the current user (encrypted).
-- Passing a blank value removes the stored key.
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
            pgp_sym_encrypt(trim(p_key), v_pass),
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
