-- P1: sole owner transfers household ownership to another active member.

create or replace function public.transfer_household_ownership(p_new_owner_user_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_space_id uuid;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    if p_new_owner_user_id is null or p_new_owner_user_id = v_user_id then
        raise exception 'invalid_transfer_target';
    end if;

    select s.id
    into v_space_id
    from public.sharing_spaces s
    where s.topic = 'nutrition'
      and s.owner_id = v_user_id
    limit 1;

    if v_space_id is null then
        raise exception 'household_not_found';
    end if;

    if not exists (
        select 1
        from public.space_members sm
        where sm.space_id = v_space_id
          and sm.user_id = p_new_owner_user_id
          and sm.left_at is null
    ) then
        raise exception 'transfer_target_not_member';
    end if;

    update public.sharing_spaces
    set owner_id = p_new_owner_user_id,
        updated_at = now()
    where id = v_space_id;

    insert into public.space_members (space_id, user_id, role)
    values (v_space_id, v_user_id, 'editor')
    on conflict (space_id, user_id)
    do update set role = 'editor', left_at = null;
end;
$$;

revoke all on function public.transfer_household_ownership(uuid) from public;
grant execute on function public.transfer_household_ownership(uuid) to authenticated;
