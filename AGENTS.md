<!-- BEGIN:roberto-project-facts -->
===============================================================
MyMultiverseApp (Ammò) — household nutrition logistics, KMP
===============================================================
Compose Multiplatform 1.8.0 + Material 3 on a custom AppNavigator (Voyager
and SQLDelight sit in the catalog UNUSED). Kotlin 2.3.21, Koin 4.0.2,
coroutines 1.10.2, Ktor 3.4.0, Supabase Kotlin 3.5.0 (auth, postgrest,
realtime, functions, storage) over Postgres RLS + Deno edge functions. AGP
9.2.1: `:androidApp` is the application, `:composeApp` the KMP library;
minSdk 24, compileSdk 36. iOS compiles locally only — the CI iOS job is
`if: false`. Eight locales: values, -fr, -es, -de, -it, -ar, -ar-rSA, -nap.

PROOF:  export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
        ./gradlew :composeApp:testDebugUnitTest         (includes locale parity suites)
        ./gradlew :androidApp:assembleDebugAndroidTest  when UI, fakes or interfaces changed
        ./gradlew :composeApp:compileKotlinIosSimulatorArm64  after iosMain changes

This Mac has no emulator, AVD or device: instrumented tests only COMPILE
locally. To run them, dispatch `gh workflow run kmp-ci.yml --ref <branch> -f
job=android-instrumented-tests` (~8 min, one dispatch per ref — the
concurrency group cancels the earlier one). `am instrument` exits 0 even when
the process crashes, so the job counts only when it reports started=N passed=N.

MERGING TO main SHIPS. Every push to main runs `distribute-alpha`, sending a
debug APK to Firebase App Distribution testers once Android CI and the UI
tests pass. Any doc claiming "no auto-release on merge" is wrong. Supabase
Deploy runs on main too, so after a merge read `gh run list --commit <sha>`
for BOTH workflows before calling anything shipped.

HARD INVARIANTS
- Eight locales or it does not ship. A new key goes in all eight
  values*/strings.xml AND its i18n/*StringKeys.kt registry, or
  *LocaleStringsTest fails. Composables call stringResource; screen models
  expose counts, ids and enums, never formatted English.
- Layers are domain <- data <- presentation. Domain imports no Compose, no
  Android, no iOS, no Supabase SDK. Composables never touch *Impl types or
  DTOs — only Koin-injected domain contracts.
- A fake that does not implement the full interface is a build break, not a
  shortcut. An interface change updates every Fake*/Instrumented*Fakes in
  commonTest and androidInstrumentedTest in the same commit.
- Instrumented tests build their OWN Koin graph (InstrumentedKoinHost), not
  MainActivity's. A newly injected dependency must be registered there, with
  a fake, or those tests fail on a missing definition.
- RLS on every user-facing table, scoped to auth.uid() and household role;
  viewers cannot write nutrition rows (RLS *and* read-only UI). The service
  role key belongs in edge functions and CI only, never in the APK.
- Never edit an applied migration — add a timestamped one. Storage buckets
  are declared in supabase/config.toml and seeded with `supabase seed
  buckets`; never INSERT into storage.buckets from SQL.
- version.code is monotonic. version.prerelease is stamped in CI memory only
  and is never committed; a production tag firing while that key sits in the
  file is a release blocker.
- CI version codes come from GITHUB_RUN_NUMBER in androidApp/build.gradle.kts:
  alpha/debug 1_000_000+run, Play 100_000+3*run+{1 beta, 2 production}.
  AppBuildInfo.VERSION_CODE is the raw properties value and is NOT what CI
  installs — read the installed code from PackageManager.
- A changed user-facing flow updates firebase-appdistribution-testcases.yaml
  and bumps its version.

RELEASE, AS THE WORKFLOW BEHAVES (read kmp-ci.yml, never the prose)
- Named release: `gh workflow run "KMP CI" --ref main -f job=release -f
  version_bump=patch` bumps, ships a Firebase build, commits [skip ci], tags.
- distribute-beta uploads to Play track `internal`, not Closed Testing.
- No `v*` tag has ever started a run here: the release job pushes tags with
  GITHUB_TOKEN. Treat the tag trigger as dead until someone proves otherwise.
- Never dispatch distribute-production as a test — the Play secrets are real
  and the upload is a real rollout.
- The alpha debug APK is signed with a key each runner generates on the fly,
  so one alpha cannot install over another. Fix is S0 of
  docs/in-app-updates-plan.md.

WHERE THE REST LIVES
- docs/conventions.md — Journey design system, layer map, QA coverage,
  CI jobs and secrets, versioning policy, Supabase, iOS, Cursor Cloud notes.
- .cline/mistakes.md — this repo's ledger. Read it before touching an area
  it names.
- .agents/skills/supabase/SKILL.md and
  .agents/skills/supabase-postgres-best-practices/SKILL.md before Supabase work.
<!-- END:roberto-project-facts -->

<!-- BEGIN:roberto-operating-rules -->
Operating rules are loaded globally from ~/.codex/AGENTS.md (Codex) and ~/.claude/CLAUDE.md (Claude Code).
<!-- END:roberto-operating-rules -->
