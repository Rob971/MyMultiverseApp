# Using official library documentation in Cursor

When you adopt a **new** or **fast-moving** library (for example the latest **Framer Motion**, a new Compose or KMP release, or a major-version bump), Cursor’s training data may not match the current API. Indexing the **live** documentation keeps suggestions aligned with what ships today.

## What to do

1. Open **Cursor Settings** (macOS: **Cursor → Settings**; or **Cmd + ,**).
2. Go to **Docs** (sometimes under **Features** or **Indexing**, depending on your Cursor version).
3. Use **Add documentation** / **Add new doc** (wording varies by version).
4. Paste the **root documentation URL** for the library (for example `https://www.framer.com/motion/` or the official docs home for the tool you use).
5. Wait until indexing finishes (status shows ready / green when available).

## Using indexed docs in chat

- Mention **`@docs`** and pick the indexed source so the model grounds answers in that crawl, not stale patterns from training alone.

## When this matters most

- Major or breaking releases (new hooks, renamed exports, deprecated APIs).
- Libraries that change often (animation, routing, build tooling).
- Anything where the official site documents behavior that differs from older blog posts.

For this repository, prefer official **Kotlin**, **Compose Multiplatform**, **Voyager**, **Koin**, and **Gradle** documentation URLs when you add new dependencies or upgrade versions.
