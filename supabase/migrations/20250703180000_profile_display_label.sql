-- Treat the account-deletion sentinel as absent when resolving profile labels in RPCs.

create or replace function public.profile_display_label(p_display_name text, p_email text)
returns text
language sql
immutable
as $$
    select coalesce(
        case
            when trim(coalesce(p_display_name, '')) ilike 'deleted user' then null
            else nullif(trim(p_display_name), '')
        end,
        nullif(trim(p_email), ''),
        ''
    )::text;
$$;

create or replace function public.resolve_user_household_row()
returns table (
    household_id uuid,
    household_name text,
    owner_id uuid,
    owner_display_name text,
    member_role text
)
language plpgsql
stable
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    return query
    select
        s.id,
        s.name,
        s.owner_id,
        public.profile_display_label(p.display_name, p.email),
        case
            when s.owner_id = v_user_id then 'owner'
            else coalesce(sm.role::text, 'editor')
        end
    from public.households s
    join public.profiles p on p.id = s.owner_id
    left join public.household_members sm
        on sm.household_id = s.id
       and sm.user_id = v_user_id
       and sm.left_at is null
    where s.topic = 'nutrition'
      and (
          s.owner_id = v_user_id
          or sm.user_id is not null
          or exists (
              select 1
              from public.household_members sm2
              join public.group_members gm on gm.group_id = sm2.group_id
              where sm2.household_id = s.id
                and sm2.left_at is null
                and gm.user_id = v_user_id
          )
      )
    order by (s.owner_id = v_user_id) desc, s.created_at
    limit 1;
end;
$$;

-- Repair profiles left on the deletion sentinel for accounts that signed back in.
update public.profiles
set display_name = nullif(
        split_part(coalesce(email, ''), '@', 1),
        ''
    ),
    updated_at = now()
where trim(coalesce(display_name, '')) ilike 'deleted user'
  and coalesce(nullif(trim(email), ''), '') <> '';
