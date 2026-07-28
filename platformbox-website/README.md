# PlatformBox website

High-end marketing one-pager for **[PlatformBox](https://www.platformbox.io)** — the 14-Day Enterprise Internal Developer Platform.

## Contents

| File | Purpose |
|------|---------|
| `index.html` | One-pager structure and copy |
| `styles.css` | Visual system and layout |
| `script.js` | Scroll header + reveal motion |

## Local preview

```bash
cd platformbox-website
python3 -m http.server 8080
# open http://localhost:8080
```

## Deploy

Intended host: **www.platformbox.io** (currently on Framer).

Recommended path when `github.com/Rob971/platformbox.io` exists:

1. Copy this folder into that repository as the site root (or `public/`).
2. Deploy with GitHub Pages, Cloudflare Pages, Netlify, or Firebase Hosting.
3. Point the `platformbox.io` DNS / custom domain at the host.

Until that repo is created, this folder ships inside MyMultiverseApp as the source of truth for the redesign.

## Contact / CTAs

- Audit booking: https://calendly.com/robertocornano
- Email: roberto@platformbox.io
- Phone: +34 624 41 11 64
- LinkedIn: https://www.linkedin.com/in/robertocornano/

## Offer summary

- **Product:** Opinionated Internal Developer Platform in AWS/GCP
- **Deliverables:** Terraform IaC, DevSecOps CI/CD, ephemeral PR environments, production Kubernetes
- **Engagement:** €20,000 fixed fee · 14 days kickoff → handoff
