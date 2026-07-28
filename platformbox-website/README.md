# PlatformBox website

High-end one-pager for **[PlatformBox](https://www.platformbox.io)** — the 14-Day Enterprise Internal Developer Platform engagement.

## What’s included

- Single-page marketing site (`index.html`)
- Brand-aligned dark espresso visual system (matching live Framer site tokens)
- Sections: Hero · Bottleneck · Delivery system · Economics · Fixed engagement · Contact
- Responsive layout + scroll reveals (respects `prefers-reduced-motion`)

## Local preview

```bash
cd platformbox-website
python3 -m http.server 8080
# open http://localhost:8080
```

## Deploy to platformbox.io

This folder is ready to publish as the root of `Rob971/platformbox.io` (repo not created yet) or any static host:

| Host | Notes |
|------|--------|
| **GitHub Pages** | Push this directory as the repo root; enable Pages from `main` |
| **Cloudflare Pages / Netlify / Vercel** | Point build output / publish directory at this folder (no build step) |
| **Firebase Hosting** | `firebase init hosting` with `public` = this folder |

Custom domain: point `www.platformbox.io` / `platformbox.io` A/CNAME records at the host, then replace the current Framer publish if desired.

## Brand / CTA references

- Audit CTA: https://calendly.com/robertocornano
- Email: roberto@platformbox.io
- LinkedIn: https://www.linkedin.com/in/robertocornano/
- Phone: +34 624 41 11 64

## Content sources

- Live reference: https://www.platformbox.io
- Offer brief: *PLATFORMBOX: The 14-Day Enterprise IDP* (deliverables, ROI, fixed fee)
