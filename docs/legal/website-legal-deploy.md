# Website legal pages — deploy guide

## Files to copy

| Source (this repo) | Destination (website repo) |
|--------------------|----------------------------|
| `web/site-updates/privacy-index.html` | `public/privacy/index.html` |
| `web/site-updates/terms-index.html` | `public/terms/index.html` |
| `web/site-updates/products-ammo-index.html` | `public/products/ammo/index.html` |
| `web/site-updates/site.css` | `public/styles/site.css` (merge if site.css diverged) |

## Key ownership clauses (already in drafts)

### Privacy Policy §12 — Intellectual property

- Data controller: **Roberto Cornano (MyMultiverse)**
- Ammò / MyMultiverse names, logos, software owned by rights holder
- No licence granted except use of the Service

### Terms §5–6 — IP ownership & user licence

- All IP owned by **Roberto Cornano (MyMultiverse)**
- Public contributor credit ≠ co-ownership
- Limited personal, non-commercial licence to use the app
- No copying/branding use without written consent

### Terms §7 — User content

- Users retain ownership of grocery/meal content
- Licence to host/sync solely to operate the Service

## Confirmed legal details

| Item | Confirmed |
|------|-----------|
| Governing law | Spain (Terms §16, IP assignment §7) |
| Courts | Barcelona (Terms §16, IP assignment §7) |
| Data controller address | Contact form only — add registered address if required |
| International transfers | Privacy §5 — confirm Supabase/Firebase DPA and SCCs |
| Consumer rights | Terms §11–12 — caps may need EU consumer-law adjustments |

## Product page credit (Carola)

Updated copy in `products-ammo-index.html`:

- Roberto: **Owner · engineering** (sole owner stated in intro blurb)
- Carola: **Product direction** (attribution, not co-creator/owner)
- Meta description: *"Product direction: Carola Zeno"*

## After deploy

- [ ] Verify `https://mymultiverse.app/privacy/` and `/terms/` load
- [ ] Link from Play Console / App Store listings
- [ ] Link from in-app settings if not already (optional)
- [ ] Tick checklist in [`docs/IP.md`](../IP.md)

## Related

- [`ip-assignment-product-contributor-template.md`](ip-assignment-product-contributor-template.md)
- [`../IP.md`](../IP.md)
