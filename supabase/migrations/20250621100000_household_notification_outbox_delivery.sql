-- P2 closeout: auto-dispatch household_notification_outbox rows to notify-household-invite.
-- Runtime URL + gateway bearer token are stored in private.household_notification_delivery_config
-- (populated by scripts/configure-household-notification-delivery.sh during deploy).

create schema if not exists private;

revoke all on schema private from public, anon, authenticated, service_role;

create table if not exists private.household_notification_delivery_config (
    id integer primary key default 1 check (id = 1),
    project_url text not null,
    invoke_bearer_token text not null,
    updated_at timestamptz not null default now()
);

revoke all on private.household_notification_delivery_config from public, anon, authenticated, service_role;

create extension if not exists pg_net with schema extensions;

create or replace function private.dispatch_household_notification_outbox()
returns trigger
language plpgsql
security definer
set search_path = public, extensions, private
as $$
declare
    v_project_url text;
    v_token text;
    v_request_id bigint;
begin
    select project_url, invoke_bearer_token
    into v_project_url, v_token
    from private.household_notification_delivery_config
    where id = 1;

    if v_project_url is null
        or v_token is null
        or char_length(trim(v_project_url)) = 0
        or char_length(trim(v_token)) = 0
    then
        raise warning 'household_notification_delivery_skipped: delivery config not set';
        return new;
    end if;

    select net.http_post(
        url := rtrim(v_project_url, '/') || '/functions/v1/notify-household-invite',
        headers := jsonb_build_object(
            'Content-Type', 'application/json',
            'Authorization', 'Bearer ' || v_token,
            'apikey', v_token
        ),
        body := jsonb_build_object('outbox_id', new.id::text)
    )
    into v_request_id;

    return new;
end;
$$;

revoke all on function private.dispatch_household_notification_outbox() from public, anon, authenticated, service_role;

drop trigger if exists household_notification_outbox_dispatch on public.household_notification_outbox;

create trigger household_notification_outbox_dispatch
    after insert on public.household_notification_outbox
    for each row
    execute function private.dispatch_household_notification_outbox();
