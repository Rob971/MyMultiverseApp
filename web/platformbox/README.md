# PlatformBox one-pager

Standalone static one-pager for **platformbox.io** — "The 14-Day Enterprise Internal Developer Platform".

Content sources: the PlatformBox proposal PDF (bottleneck, deliverables, ROI, €20,000 fixed fee / 14-day timeline) and the live [www.platformbox.io](https://www.platformbox.io) section structure.

## Files

| File | Purpose |
|------|---------|
| `index.html` | Full one-pager (hero, bottleneck, delivery system, economics, investment, footer) |
| `styles.css` | Editorial dark theme — Fraunces display serif, Inter body, IBM Plex Mono accents |
| `script.js` | Scroll-reveal animation (IntersectionObserver) + footer year |

No build step — deploy the three files as-is to any static host (Firebase Hosting, GitHub Pages, Netlify). Fonts load from Google Fonts CDN.

## Preview locally

```bash
python3 -m http.server 8080 --directory web/platformbox
# open http://localhost:8080
```

## Deploy target

Intended for the `Rob971/platformbox.io` repository / platformbox.io hosting. This copy lives here as the staging reference, matching the `web/site-updates/` convention.
