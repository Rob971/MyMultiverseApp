-- Re-assert execute grants after ensure_household() was replaced in 20250618170000.
revoke all on function public.ensure_household() from public;
grant execute on function public.ensure_household() to authenticated;
