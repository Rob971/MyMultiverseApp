# PlatformBox — Company Website

High-end one-page marketing site for **PlatformBox**: a 14-day Enterprise Internal Developer Platform (IDP) engagement for scaling engineering teams post-Series A.

**Live reference:** [platformbox.io](https://www.platformbox.io)

## Stack

- Static HTML / CSS / JavaScript (no build step)
- Firebase Hosting ready (`firebase.json`)
- Fonts: Sarpanch, Rajdhani, Inter (Google Fonts)

## Local preview

```bash
cd platformbox-website
python3 -m http.server 8080
# open http://localhost:8080
```

Or with Node:

```bash
npx serve .
```

## Deploy to Firebase Hosting

1. Create or select a Firebase project for `platformbox.io`
2. `firebase login` and `firebase use <project-id>`
3. From this directory:

```bash
firebase deploy --only hosting
```

## Sections

| Section | Content |
|---------|---------|
| Hero | 14-Day Enterprise IDP value prop + CTA |
| The Bottleneck | Post-Series A deployment pain points |
| The 14-Day Solution | Discovery → build → handoff timeline |
| Deliverables | IaC, CI/CD, ephemeral envs, Kubernetes |
| Financial ROI | Avoided headcount, reclaimed payroll, FinOps |
| Investment | $20,000 fixed fee, 14-day timeline, contact |

## Contact

- **Email:** roberto@platformbox.io
- **Phone:** +34 624 41 11 64
- **Calendly:** [Book a 15-Minute Architecture Audit](https://calendly.com/robertocornano)
- **LinkedIn:** [Roberto Cornano](https://www.linkedin.com/in/robertocornano/)

## Intellectual property

© Roberto Cornano (PlatformBox). All rights reserved.
