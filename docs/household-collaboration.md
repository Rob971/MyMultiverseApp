# Household collaboration — product & UX specification

This document is the **source of truth** for household sharing in MyMultiverse: product rules, UX behaviour, architecture direction, and what remains open.

**Audience:** product, design, mobile, backend.  
**Related code:** `supabase/migrations/*household*`, `*invite*`, `presentation/screens/household/`, `firebase-appdistribution-testcases.yaml`.

---

## Product invariants (locked)

| Rule | Meaning |
|------|---------|
| **One email → one user** | Supabase Auth enforces unique email; invites target that address (normalized: `lower(trim)`). |
| **One active household at a time** | A user is in zero or one household at any moment—not two. |
| **Join ↔ leave ↔ join another** | Membership is not permanent; leaving frees the user to accept another invite or create a new household. |
| **One household, all modules** | Nutrition, Adventures, and Budget share **one** household (one row/id)—not separate spaces per module. |
| **Invite before membership** | Adding someone always creates a **pending invite**; the invitee **accepts** (no silent direct-add). |
| **Max 20 members** | A household may have at most **20** people (owners + members). Enforce in invite/accept RPCs. |
| **Viewer read-only** | Users with role **viewer** cannot edit **any** shared entry (grocery, meal plan, AI lists). They may read only. Enforced in **UI** (disabled controls) and **Postgres RLS** (no INSERT/UPDATE/DELETE on shared nutrition data). |
| **Invite email must match auth** | Pending invites are listed and accept succeeds only when the signed-in user's auth/profile email matches the invite email (`lower(trim)`). Mismatch → invite hidden or `invite_email_mismatch` on accept. |
| **Child / shared email accounts** | v2; v1 = one auth account per email. |
| **Push / email invite notifications** | v2; v1 = invitee sees invite when opening the app on the gate. |

---

## Locked product decisions (2025-06-18)

| Topic | Decision |
|-------|----------|
| **Owner leaves with other members** | Owner may **transfer ownership** when they are the **only owner** and other members remain. Plain “Leave” is not offered to the sole owner without transfer or dissolve. |
| **Dissolve household** | **Hard delete** household data (not soft archive) for v1. Confirm in UI with strong destructive copy. |
| **Pending invites + Create household** | **Do not block** create; use **layout** (invites above create on gate). |
| **Max household size** | **20** members. |
| **Rename `sharing_spaces` → `households`** | **Yes** — migration planned; keep stable UUIDs for client cache keys. |
| **Trip / Budget** | **Same unique household** as Nutrition (module flags, not separate household rows). |
| **Invite expiry in UI** | **No** — expiry still enforced server-side (14 days); no countdown in app v1. |
| **GDPR on leave** | **Yes** — implement leave/export/delete flows per privacy policy (see § GDPR). |
| **Firebase QA two-phone flow** | **Yes** — simulate in `firebase-appdistribution-testcases.yaml` (see QA section). |
| **Viewer edits** | **No** — viewers never edit any shared entry; read-only UI + RLS on `nutrition_space_week_data`. |
| **Auth email match for invites** | **Required** — sign in with the **exact invited email** (normalized) to see pending invites and accept. No auto-migration when auth email changes. |

---

## FAQ — questions clarified

### What does “Viewer write blocked in RLS” mean?

**Locked (v1):** Viewers are **read-only everywhere**. They must not add, edit, toggle, delete, or clear any shared grocery, meal-plan, or AI-saved list entry.

**RLS** = Row Level Security in Postgres: rules on who can `SELECT` / `INSERT` / `UPDATE` / `DELETE` rows.

Members have a **role**: `owner`, `editor`, or `viewer`.

| Role | Intended access |
|------|-----------------|
| Editor / Owner | Read and **edit** shared grocery, meal plan, etc. |
| Viewer | **Read only** — can see shared data, must not change it |

**Implementation:** Postgres policies on `nutrition_space_week_data` allow `INSERT`/`UPDATE`/`DELETE` only when the caller is **owner or editor** on that household. The app disables all write controls when `role = viewer`.

**Status:** Implemented (migration `20250618150000` + read-only UI).

---

### Auth email must match invite

**Locked (v1):**

- Invites are stored and matched by **normalized email** (`lower(trim)`).
- `list_my_pending_space_invites()` returns only invites whose email equals the signed-in user's profile/auth email.
- `accept_space_invite` raises `invite_email_mismatch` when emails differ.
- If the user changes their auth email after an invite was sent, they need a **new invite** to the new address or must sign in with the **original invited email**.

**Status:** Server rules in place; client shows dedicated copy on mismatch (see §8).

---

### What does “Auth email change after invite” mean? (edge case)

**Scenario:**

1. Partner invites `maria@gmail.com`.
2. Maria has an account but **changes her login email** to `maria@work.com` before accepting.
3. The pending invite is still stored as `maria@gmail.com`.
4. Maria signs in as `maria@work.com` → invite **does not match** → she does not see it / accept fails.

**v1 behaviour (document, no auto-migration):**

- Invites are tied to the **email address at invite time**.
- If the user changes auth email, they need a **new invite** to the new address, or must sign in with the **original invited email**.
- Show clear copy on `invite_email_mismatch` (§8 below).

---

## UX decisions (apply in implementation)

### 1. Gate screen hierarchy (no household yet)

Pending invites **above** “Create household” (primary vs secondary action). Do **not** disable create when invites exist.

**Status:** Partially implemented — polish visual hierarchy.

---

### 2. Pending invites when user already has a household

Show pending invites even when affiliated. Accept opens confirmation:

> “You're in **{current}**. To join **{invited}**, you must leave your current household first.”  
> [Leave and accept] [Cancel]

**Status:** Implemented in P0 branch (migration `20250618140000` + app).

---

### 3. Inviter feedback (add-person dialog)

| Outcome | UX |
|---------|-----|
| Invite sent | Close dialog + snackbar: “Invitation sent to **{email}**…” |
| Already in your household | Inline error |
| In another household | Inline error (`invitee_household_already_active`) |

**Status:** Errors done; richer success copy pending.

---

### 4. Invitee login flow

Sign in → **gate** (if unaffiliated) → accept invite → **home** + snackbar “You joined **{name}**”.

---

### 5. Leave, transfer, dissolve

| Who | Action |
|-----|--------|
| **Editor / viewer** | **Leave household** → confirm → gate (unaffiliated) |
| **Only owner, other members exist** | **Transfer ownership** to another member, then may leave |
| **Only owner, no other members** | **Delete household** (hard delete) — not plain Leave |
| **Any member** | GDPR-related **export/delete my data** where required (see § GDPR) |

**Status:** Implemented in P0 branch (leave / dissolve + switch-household dialog). **Transfer ownership** implemented in P1 (`transfer_household_ownership` RPC + members UI).

---

### 6. One active household (database)

Partial unique index: at most one `household_members` row per `user_id` where `left_at IS NULL` (after `households` migration).

**Status:** Implemented in migration `20250618140000`.

---

### 7. Nutrition sync on join

`activateSpace(householdId)` immediately after accept or create—not only when opening Nutrition.

**Status:** Implemented in P0 branch (`activateSpace` on create/accept/home refresh).

---

### 8. Email mismatch on accept

> “This invitation was sent to **{invite_email}**. You're signed in as **{session_email}**. Sign in with the invited address or ask for a new invite.”

**Status:** Server enforced; client snackbar with `auth_pending_invites_email_mismatch` (8 locales).

---

### 9. Roles (v1)

| Role | Invite | Edit nutrition | Manage members |
|------|--------|----------------|----------------|
| Owner | Yes | Yes | Yes |
| Editor | No | Yes | No |
| Viewer | No | Read-only | No |

Owner-only invite enforced in RPC. **Viewer:** read-only UI on all nutrition screens + RLS write block (next implementation).

---

### 10. Copy & i18n

All strings in 8 locales; no `\'` in `strings.xml`; use `%1$s` for names/emails.

---

## GDPR on leave (required)

When a user **leaves** a household (or deletes account):

1. **Revoke access** — membership ends; RLS blocks shared household data immediately.
2. **Personal data** — profile row handled per privacy policy (export on request, delete account flow).
3. **Household content** — user’s edits may remain in shared payloads attributed by `updated_by`; leaving does not delete the whole household’s groceries for other members (unless **dissolve** by owner).
4. **Document** in-app privacy/settings copy and external privacy policy; implement export/delete tickets as engineering work.

**Status:** P1 — `export_my_personal_data` RPC + Home export action; leave dialog includes data-retention note. Account deletion flow remains P2/legal.

---

## Architecture direction

### Target tables (migration from `sharing_spaces`)

| Today | Target |
|-------|--------|
| `sharing_spaces` (`topic = nutrition`) | `households` |
| `space_members` | `household_members` (`joined_at`, `left_at`) |
| `space_invites` | `household_invites` |
| `space_nutrition_features` | `household_modules` (`nutrition`, `adventures`, `budget`) |
| `nutrition_space_week_data.space_id` | `nutrition_household_week_data.household_id` (same UUID) |

**One `households` row** powers Nutrition, Adventures, and Budget via `household_modules`.

---

## What is still open

| # | Topic | Notes |
|---|--------|-------|
| 1 | **Child / shared family email** | Deferred v2 |
| 2 | **Push/email on invite** | Deferred v2 |
| 3 | **Viewer RLS + read-only UI** | **Done** |
| 4 | **GDPR legal copy** | Engineering hooks done; legal review of exact wording |
| 5 | **`households` migration timing** | **Done** — migration `20250618170000` |
| 6 | **Hard delete cascade scope** | Confirm: nutrition rows + invites + members all removed on dissolve |

---

## Implementation backlog

| Priority | Item |
|----------|------|
| **P0** | One active household per user (DB + RPCs) — **done** (branch) |
| **P0** | Pending invites when affiliated + leave-then-accept — **done** (branch) |
| **P0** | `activateSpace` on membership active — **done** (branch) |
| **P0** | Max 20 members check on invite/accept — **done** (branch) |
| **P0** | **Viewer read-only UI** — disable all nutrition writes for `viewer` role — **done** |
| **P0** | **Viewer RLS** — block INSERT/UPDATE/DELETE on nutrition week data for viewers — **done** |
| **P0** | **Auth email match** — list + accept paths; QA in Firebase YAML — **done** |
| **P1** | `leave_household`, `dissolve_household` — **done**; `transfer_ownership` — **done** |
| **P1** | GDPR export hook + leave privacy copy — **done** (account delete deferred) |
| **P1** | Migrate `sharing_spaces` → `households` — **done** (`20250618170000`) |
| **P2** | Push/email notifications |
| **P2** | Child accounts |

---

## QA — two-phone simulation

Documented in **`firebase-appdistribution-testcases.yaml`**:

- `household-gate-pending-invite` — invitee sees invite on gate (not home), accepts, reaches home.
- `household-invite-blocked-already-member` — inviter sees error if invitee already in another household.
- `household-shared-nutrition-two-phones` — A edits grocery, B sees update in same household.
- `household-invite-email-mismatch` — wrong signed-in email cannot accept; dedicated mismatch message.
- `nutrition-viewer-read-only` — viewer cannot edit grocery, meal plan, or AI lists.

**Remote Supabase migrations required for field tests:**  
`20250617130000`, `20250617140000`, `20250618120000`, `20250618140000`, `20250618150000`, `20250618160000`, `20250618161000`, `20250618170000`.

---

## Revision history

| Date | Change |
|------|--------|
| 2025-06-18 | Initial spec |
| 2025-06-18 | Locked product decisions; FAQ for RLS viewer + email change; GDPR; QA |
| 2025-06-18 | Locked viewer read-only + auth email must match; P0 backlog and QA updated |
| 2025-06-18 | P1: transfer ownership, GDPR export, `households` table rename shipped |
