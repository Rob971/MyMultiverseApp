# PlatformBox — Landing Page

A high-end, single-page marketing site for **PlatformBox**, proposing the *14-Day Enterprise Internal Developer Platform* engagement.

It is a fully static, zero-build site (HTML + CSS + vanilla JS), so it can be hosted anywhere — GitHub Pages, Netlify, Vercel, S3/CloudFront, or any static file server.

## Structure

```
platformbox-site/
├── index.html        # One-pager markup (hero, bottleneck, delivery system, economics, process, CTA)
├── styles.css        # Dark editorial design system + responsive rules
├── main.js           # Scroll reveal, sticky nav, card spotlight (no dependencies)
├── assets/
│   ├── favicon.svg   # Brand mark
│   └── og-image.svg  # Social share card
└── README.md
```

## Sections

1. **Hero** — headline, sub-headline, primary CTA (Architecture Audit), key stats.
2. **01 / The Bottleneck** — the post-Series A deployment problem.
3. **02 / The Delivery System** — the four deliverables: IaC, DevSecOps CI/CD, Ephemeral Environments, Production Kubernetes.
4. **03 / The Economics** — ROI (avoided headcount, reclaimed payroll, FinOps savings) + fixed €20,000 / 14-day investment card.
5. **04 / The Process** — audit → 14-day delivery → engineering handoff.
6. **CTA + Footer** — contact details and links.

## Run locally

No build step. Just serve the folder:

```bash
cd platformbox-site
python3 -m http.server 8080
# open http://localhost:8080
```

## Customize

- **Booking link / contact**: search `calendly.com/robertocornano`, `roberto@platformbox.io`, `+34 624 41 11 64`, and the LinkedIn URL in `index.html`.
- **Colors / typography**: CSS custom properties at the top of `styles.css` (`:root`).
- **Copy**: all content lives in `index.html`.

## Deploy to GitHub Pages

Point Pages at this directory (or move its contents to the repo root / a `docs/` folder) and enable Pages in the repository settings.
