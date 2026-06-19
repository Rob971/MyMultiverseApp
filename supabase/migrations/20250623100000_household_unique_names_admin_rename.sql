-- Unique household names (Unicode-aware lower + collapsed whitespace), rename RPC, admin role.

alter type public.household_member_role add value if not exists 'admin';

create or replace function public.normalize_household_name(p_name text)
returns text
language sql
immutable
as $$
    select lower(regexp_replace(trim(coalesce(p_name, '')), '\s+', ' ', 'g'));
$$;

-- Resolve duplicate names before enforcing uniqueness (auto-suffix).
with ranked as (
    select
        id,
        name,
        row_number() over (
            partition by public.normalize_household_name(name)
            order by created_at, id
        ) as rn
    from public.households
)
update public.households h
set name = h.name || ' (' || r.rn || ')'
from ranked r
where h.id = r.id
  and r.rn > 1;

create unique index if not exists households_name_normalized_unique_idx
    on public.households (public.normalize_household_name(name));

create or replace function public.is_household_name_available(
    p_name text,
    p_exclude_household_id uuid default null
)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select not exists (
        select 1
        from public.households h
        where public.normalize_household_name(h.name) = public.normalize_household_name(p_name)
          and (p_exclude_household_id is null or h.id <> p_exclude_household_id)
    );
$$;

create or replace function public.check_household_name_available(
    p_name text,
    p_exclude_household_id uuid default null
)
returns json
language plpgsql
security definer
set search_path = public
as $$
declare
    v_trimmed text := trim(coalesce(p_name, ''));
begin
    if auth.uid() is null then
        raise exception 'auth_required';
    end if;

    if char_length(v_trimmed) = 0 then
        return json_build_object('available', false, 'reason', 'household_name_required');
    end if;

    return json_build_object(
        'available',
        public.is_household_name_available(v_trimmed, p_exclude_household_id)
    );
end;
$$;

create or replace function public.is_household_manager(p_household_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select public.is_household_owner(p_household_id)
        or exists (
            select 1
            from public.household_members sm
            where sm.household_id = p_household_id
              and sm.user_id = (select auth.uid())
              and sm.left_at is null
              and sm.role = 'admin'
        );
$$;

create or replace function public.household_member_can_write_nutrition(p_household_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select public.is_household_owner(p_household_id)
        or exists (
            select 1
            from public.household_members sm
            where sm.household_id = p_household_id
              and sm.user_id = (select auth.uid())
              and sm.left_at is null
              and sm.role in ('editor', 'admin')
        );
$$;

create or replace function public.create_household(p_name text)
returns json
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_existing record;
    v_household_id uuid;
    v_household_name text;
    v_owner_id uuid;
    v_owner_display_name text;
    v_features json;
    v_trimmed_name text := trim(p_name);
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    if char_length(v_trimmed_name) = 0 then
        raise exception 'household_name_required';
    end if;

    if not public.is_household_name_available(v_trimmed_name) then
        raise exception 'household_name_taken';
    end if;

    perform public.ensure_current_profile();

    select * into v_existing
    from public.resolve_user_household_row();

    if found then
        raise exception 'household_already_active';
    end if;

    insert into public.households (topic, name, owner_id)
    values ('nutrition', v_trimmed_name, v_user_id)
    returning id, name, owner_id
    into v_household_id, v_household_name, v_owner_id;

    select coalesce(nullif(trim(display_name), ''), nullif(trim(email), ''), v_trimmed_name)
    into v_owner_display_name
    from public.profiles
    where id = v_user_id;

    insert into public.household_modules (household_id, feature)
    values
        (v_household_id, 'grocery'),
        (v_household_id, 'meal_plan'),
        (v_household_id, 'ai_advice');

    select coalesce(json_agg(feature order by feature), '[]'::json)
    into v_features
    from public.household_modules
    where household_id = v_household_id;

    return json_build_object(
        'status', 'active',
        'household_id', v_household_id,
        'household_name', v_household_name,
        'owner_id', v_owner_id,
        'owner_display_name', v_owner_display_name,
        'role', 'owner',
        'features', v_features
    );
end;
$$;

create or replace function public.rename_household(
    p_household_id uuid,
    p_name text
)
returns json
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_trimmed_name text := trim(coalesce(p_name, ''));
    v_features json;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    if char_length(v_trimmed_name) = 0 then
        raise exception 'household_name_required';
    end if;

    if not public.is_household_manager(p_household_id) then
        raise exception 'insufficient_role';
    end if;

    if not public.is_household_name_available(v_trimmed_name, p_household_id) then
        raise exception 'household_name_taken';
    end if;

    update public.households
    set name = v_trimmed_name,
        updated_at = now()
    where id = p_household_id;

    select coalesce(json_agg(feature order by feature), '[]'::json)
    into v_features
    from public.household_modules
    where household_id = p_household_id;

    return json_build_object(
        'household_id', p_household_id,
        'household_name', v_trimmed_name,
        'features', v_features
    );
end;
$$;

create or replace function public.update_household_member_role(
    p_member_id uuid,
    p_role public.household_member_role
)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_household_id uuid;
    v_target_user_id uuid;
    v_target_role public.household_member_role;
    v_actor_is_owner boolean;
    v_actor_is_admin boolean;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    if p_role = 'owner' then
        raise exception 'cannot_assign_owner_role';
    end if;

    select sm.household_id, sm.user_id, sm.role
    into v_household_id, v_target_user_id, v_target_role
    from public.household_members sm
    where sm.id = p_member_id
      and sm.left_at is null
      and sm.user_id is not null;

    if v_household_id is null then
        raise exception 'member_not_found';
    end if;

    v_actor_is_owner := public.is_household_owner(v_household_id);
    v_actor_is_admin := exists (
        select 1
        from public.household_members sm
        where sm.household_id = v_household_id
          and sm.user_id = v_user_id
          and sm.left_at is null
          and sm.role = 'admin'
    );

    if not v_actor_is_owner and not v_actor_is_admin then
        raise exception 'insufficient_role';
    end if;

    if v_target_role = 'owner' or v_target_role = 'admin' then
        if not v_actor_is_owner then
            raise exception 'insufficient_role';
        end if;
    end if;

    if p_role = 'admin' and not v_actor_is_owner then
        raise exception 'insufficient_role';
    end if;

    update public.household_members
    set role = p_role
    where id = p_member_id;
end;
$$;

create or replace function public.invite_household_member(
    p_household_id uuid,
    p_email text,
    p_role public.household_member_role default 'editor'
)
returns json
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_normalized_email text := lower(trim(p_email));
    v_profile_id uuid;
    v_profile_email text;
    v_invite_id uuid;
    v_household_name text;
    v_inviter_name text;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    if char_length(v_normalized_email) = 0 then
        raise exception 'member_email_required';
    end if;

    if p_role = 'owner' then
        raise exception 'cannot_invite_as_owner';
    end if;

    if p_role = 'admin' and not public.is_household_owner(p_household_id) then
        raise exception 'insufficient_role';
    end if;

    if public.household_active_member_count(p_household_id) >= 20 then
        raise exception 'household_member_limit_reached';
    end if;

    perform public.ensure_current_profile();

    if not public.is_household_manager(p_household_id) then
        raise exception 'insufficient_role';
    end if;

    select lower(trim(coalesce(p.email, auth.jwt() ->> 'email', '')))
    into v_profile_email
    from public.profiles p
    where p.id = v_user_id;

    if v_normalized_email = v_profile_email then
        raise exception 'member_cannot_add_self';
    end if;

    select id
    into v_profile_id
    from public.profiles
    where lower(trim(coalesce(email, ''))) = v_normalized_email
    limit 1;

    if v_profile_id is not null then
        if exists (
            select 1
            from public.households s
            where s.id = p_household_id
              and (
                  s.owner_id = v_profile_id
                  or exists (
                      select 1
                      from public.household_members sm
                      where sm.household_id = p_household_id
                        and sm.user_id = v_profile_id
                        and sm.left_at is null
                  )
              )
        ) then
            raise exception 'member_already_exists';
        end if;

        if public.user_has_active_nutrition_household(v_profile_id, p_household_id) then
            raise exception 'invitee_household_already_active';
        end if;
    end if;

    select id
    into v_invite_id
    from public.household_invites
    where household_id = p_household_id
      and lower(trim(email)) = v_normalized_email
      and accepted_at is null
      and declined_at is null
    limit 1;

    if v_invite_id is not null then
        update public.household_invites
        set role = p_role,
            expires_at = now() + interval '14 days'
        where id = v_invite_id;
    else
        insert into public.household_invites (household_id, email, role, invited_by)
        values (p_household_id, v_normalized_email, p_role, v_user_id)
        returning id into v_invite_id;
    end if;

    select s.name, coalesce(nullif(trim(p.display_name), ''), nullif(trim(p.email), ''), 'Member')
    into v_household_name, v_inviter_name
    from public.households s
    join public.profiles p on p.id = v_user_id
    where s.id = p_household_id;

    return json_build_object(
        'invite_id', v_invite_id,
        'household_name', v_household_name,
        'inviter_name', v_inviter_name
    );
end;
$$;

revoke all on function public.normalize_household_name(text) from public;
grant execute on function public.normalize_household_name(text) to authenticated;

revoke all on function public.is_household_name_available(text, uuid) from public;
grant execute on function public.is_household_name_available(text, uuid) to authenticated;

revoke all on function public.check_household_name_available(text, uuid) from public;
grant execute on function public.check_household_name_available(text, uuid) to authenticated;

revoke all on function public.is_household_manager(uuid) from public;
grant execute on function public.is_household_manager(uuid) to authenticated;

revoke all on function public.rename_household(uuid, text) from public;
grant execute on function public.rename_household(uuid, text) to authenticated;

revoke all on function public.update_household_member_role(uuid, public.household_member_role) from public;
grant execute on function public.update_household_member_role(uuid, public.household_member_role) to authenticated;
