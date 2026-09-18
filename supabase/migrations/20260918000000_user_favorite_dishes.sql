-- F2: personal favorite dishes, capped at 10.
-- Favorites are private to the requesting user; they are never shared with a household.
-- Uniqueness is on (user_id, normalised_label) where normalised_label = lower(trim(label)).
-- The cap is enforced by a BEFORE INSERT trigger that takes a per-user transaction
-- advisory lock, so two concurrent inserts cannot both slip past the cap (READ COMMITTED
-- cannot see the other's uncommitted insert, but the advisory lock serialises them).

create table public.user_favorite_dishes (
    user_id          uuid not null references auth.users (id) on delete cascade,
    label            text not null,
    normalised_label text not null,
    created_at       timestamptz not null default now(),
    unique (user_id, normalised_label)
);

alter table public.user_favorite_dishes enable row level security;

create policy user_favorite_dishes_select on public.user_favorite_dishes
    for select to authenticated
    using (user_id = (select auth.uid()));

create policy user_favorite_dishes_insert on public.user_favorite_dishes
    for insert to authenticated
    with check (user_id = (select auth.uid()));

create policy user_favorite_dishes_delete on public.user_favorite_dishes
    for delete to authenticated
    using (user_id = (select auth.uid()));

-- No UPDATE policy on purpose: favourites are replaced atomically through
-- replace_favorite(), keeping the uniqueness + cap invariants in one place.

create or replace function public.enforce_favorite_cap()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
    -- Re-entrant per transaction: replace_favorite() takes the same lock, so an
    -- insert it performs does not deadlock against the trigger.
    perform pg_advisory_xact_lock(hashtext('fav_cap_' || new.user_id::text));
    if (select count(*) from public.user_favorite_dishes
        where user_id = new.user_id) >= 10 then
        raise exception 'favorite_cap_exceeded' using errcode = 'check_violation';
    end if;
    return new;
end;
$$;

create trigger enforce_favorite_cap
    before insert on public.user_favorite_dishes
    for each row execute function public.enforce_favorite_cap();

-- Add a favorite. Rooted exclusively in auth.uid().
create or replace function public.add_favorite(p_label text)
returns void
language plpgsql
security invoker
set search_path = ''
as $$
declare
    v_uid   uuid := (select auth.uid());
    v_label text := trim(p_label);
begin
    if v_uid is null then
        raise exception 'auth_required';
    end if;
    if v_label is null or v_label = '' then
        return;
    end if;
    insert into public.user_favorite_dishes (user_id, label, normalised_label)
    values (v_uid, v_label, lower(v_label));
end;
$$;

create or replace function public.remove_favorite(p_normalised_label text)
returns void
language plpgsql
security invoker
set search_path = ''
as $$
declare
    v_uid uuid := (select auth.uid());
begin
    if v_uid is null then
        raise exception 'auth_required';
    end if;
    delete from public.user_favorite_dishes
     where user_id = v_uid
       and normalised_label = p_normalised_label;
end;
$$;

-- Atomic replace rooted exclusively in auth.uid() (never a caller-supplied id).
create or replace function public.replace_favorite(
    p_remove_normalised text,
    p_new_label text
)
returns void
language plpgsql
security invoker
set search_path = ''
as $$
declare
    v_uid   uuid := (select auth.uid());
    v_label text := trim(p_new_label);
begin
    if v_uid is null then
        raise exception 'auth_required';
    end if;
    perform pg_advisory_xact_lock(hashtext('fav_cap_' || v_uid::text));
    delete from public.user_favorite_dishes
     where user_id = v_uid
       and normalised_label = p_remove_normalised;
    if v_label is not null and v_label <> '' then
        insert into public.user_favorite_dishes (user_id, label, normalised_label)
        values (v_uid, v_label, lower(v_label));
    end if;
end;
$$;

revoke all on function public.add_favorite(text) from public;
revoke all on function public.remove_favorite(text) from public;
revoke all on function public.replace_favorite(text, text) from public;
grant execute on function public.add_favorite(text) to authenticated;
grant execute on function public.remove_favorite(text) to authenticated;
grant execute on function public.replace_favorite(text, text) to authenticated;