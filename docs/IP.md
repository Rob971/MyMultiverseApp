# Intellectual property (MyMultiverse / Ammò)

> **Internal reference.** Not legal advice. Confirm registrations and agreements
> with a qualified IP attorney in your jurisdiction (EU/Italy recommended).

## Rights holder

| Field | Value |
|-------|--------|
| **Owner** | Roberto Cornano |
| **Trade / studio name** | MyMultiverse |
| **Sole ownership** | Yes — Roberto Cornano is the sole owner of MyMultiverse and all associated IP unless transferred in a signed instrument |
| **Primary product** | Ammò (`app.mymultiverse.ammo`) |
| **Public website** | [mymultiverse.app](https://mymultiverse.app) (repo: [mymultiverse-website](https://github.com/Rob971/mymultiverse-website)) |

## Protected asset inventory

### Names and marks (trademark candidates)

| Asset | Usage | Registration status |
|-------|--------|---------------------|
| **MyMultiverse** | Company / studio brand | ☐ EUIPO / national — to file |
| **Ammò** | Product name | ☐ EUIPO / national — to file |
| **Napolitan Heart** | Design-system / visual language name | ☐ Optional figurative/word mark |

Use **™** until registered; **®** only after registration.

### Visual identity

| Asset | Location |
|-------|----------|
| Round Ammò logo (PNG) | `web/brand/ammo-round-logo-{128,256,512}.png` |
| In-app logo | `composeApp/.../drawable/ammo_round_logo.webp` |
| Android launcher / notification icons | `composeApp/src/androidMain/res/` |
| iOS app icon | `iosApp/.../app-icon-1024.png` |
| Hosted copies | `https://mymultiverse.app/brand/` |

Color palette (Napolitan Heart): terracotta orange, Mediterranean teal, parchment
/ ink surfaces — see `presentation/theme/`.

### Software and content

| Asset | Location / notes |
|-------|------------------|
| Kotlin Multiplatform app | This repo (`ammo`) |
| Package / bundle ID | `app.mymultiverse.ammo` |
| Deep links | `app.mymultiverse.ammo://` |
| Backend schema & edge functions | `supabase/` |
| UI copy (8 locales) | `composeApp/.../composeResources/values*/strings.xml` |
| Journey design system | `presentation/components/`, `presentation/theme/` |
| QA / product docs | `docs/`, `firebase-appdistribution-testcases.yaml` |

### Domains and accounts

| Asset | Notes |
|-------|--------|
| `mymultiverse.app` | Primary marketing + legal pages |
| `ammo.mymultiverse.app` | Supabase project URL (see `composeApp/build.gradle.kts`) |
| Firebase / Google Play / App Store | Keep under owner account or company org |
| GitHub repos | `ammo`, `mymultiverse-website` |

## Contributors and ownership

| Person | Role | IP status |
|--------|------|-----------|
| **Roberto Cornano** | Sole owner; engineering, brand, operations | Retains all IP |
| **Carola ZENO** | Product owner (vision, priorities, UX direction for Ammò) | **No ownership** unless agreed otherwise — use signed assignment (see below) |

Public marketing may credit Carola for product direction. Credit is **attribution**,
not co-ownership. See `docs/legal/ip-assignment-product-contributor-template.md`.

## Copyright notice

Repository root: [`COPYRIGHT`](../COPYRIGHT).

Recommended notice on published materials:

```
© [YEAR] Roberto Cornano (MyMultiverse). All rights reserved.
Ammò is a product of MyMultiverse.
```

## Registration checklist

- [ ] Signed **IP assignment / contributor agreement** with Carola ZENO
- [ ] EUIPO trademark: **MyMultiverse** (classes 9, 42; consider 35)
- [ ] EUIPO trademark: **Ammò** + figurative logo
- [ ] Lawyer review: `/privacy`, `/terms` on mymultiverse.app — drafts in `web/site-updates/` (see `docs/legal/website-legal-deploy.md`)
- [ ] Dated archive of logo sources + key git tags (evidence of creation)
- [ ] App Store / Play Console developer entity matches rights holder
- [ ] Registrar lock on `mymultiverse.app`

## Enforcement (if someone copies you)

1. Document the infringement (screenshots, URLs, dates).
2. Cease-and-desist letter from your lawyer.
3. Platform reports: Google Play, App Store, domain registrar, hosting provider.
4. EUIPO / national trademark opposition or infringement action if marks are registered.

## Related documents

- [`docs/BRAND.md`](BRAND.md) — asset paths and usage rules
- [`docs/legal/ip-assignment-product-contributor-template.md`](legal/ip-assignment-product-contributor-template.md) — contributor IP template (lawyer review required)
- [`docs/legal/website-legal-deploy.md`](legal/website-legal-deploy.md) — privacy/terms HTML deploy guide
