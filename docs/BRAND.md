# Brand assets (MyMultiverse / Ammò)

Internal reference for logo sync, hosting, and IP traceability. For ownership
and registration, see [`IP.md`](IP.md).

## Names

| Name | Role |
|------|------|
| **MyMultiverse** | Studio / company brand |
| **Ammò** | Product name (household nutrition logistics) |
| **Napolitan Heart** | Visual language (terracotta + teal + parchment) |

## Logo files (canonical paths)

| Resolution | App / repo | Public web |
|------------|------------|------------|
| In-app (webp) | `composeApp/src/commonMain/composeResources/drawable/ammo_round_logo.webp` | — |
| 128×128 PNG | — | `web/brand/ammo-round-logo-128.png` → `mymultiverse-website/public/brand/` |
| 256×256 PNG | — | `web/brand/ammo-round-logo-256.png` (invite emails) |
| 512×512 PNG | — | `web/brand/ammo-round-logo-512.png` (OG image) |
| iOS 1024 | `iosApp/.../app-icon-1024.png` | — |
| Android mipmaps | `composeApp/src/androidMain/res/` | — |

Public URLs after deploy:

- `https://mymultiverse.app/brand/ammo-round-logo-256.png`
- `https://mymultiverse.app/brand/ammo-round-logo-512.png`

## Usage rules

- Do not publish logo or brand kit under an open license without written owner approval.
- Third parties: no use of MyMultiverse or Ammò marks without permission.
- In-app: use `AmmoRoundLogo` composable and theme tokens — do not embed one-off copies.

## Deploy sync (app → website)

See [`web/README.md`](../web/README.md):

1. Copy `web/brand/*` → `mymultiverse-website/public/brand/`
2. Deploy website hosting
3. Verify invite email logo URL loads

## Copyright on public pages

Footer template (already used on product page reference HTML):

```html
<p class="site-footer__copy">&copy; <span id="year"></span> Roberto Cornano (MyMultiverse). All rights reserved.</p>
<p class="site-footer__product">Ammò is a product of MyMultiverse.</p>
```

Apply the same pattern on all pages in the website repo.
