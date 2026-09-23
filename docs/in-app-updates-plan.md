# In-app updates for alpha testers — plan (handoff to DeepSeek)

_Written 2026-09-22, trimmed 2026-09-23 to two slices. Nothing is implemented. Repo claims cite
file:line at commit `9a9a8b4`; Firebase facts come from the docs read on 2026-09-22 (sources at the
end). The pre-trim version (467 lines, with the Play adapter, an updater abstraction, a policy
controller and an iOS slice) is superseded — those are deferred, not cancelled (§6)._

## 0. What gets built

**S0 — a stable signing key for alpha builds (CI only).** Without it nothing can update in place.
**S1 — Firebase App Distribution's own in-app update, for the tester (debug) build.**

The SDK checks on app start, shows its own "new version available" dialog (that dialog is the
permission ask), downloads, and hands off to the Android installer. The existing "Check for updates"
button triggers the same check on demand.

Deferred, with today's behaviour kept: Play in-app updates (nothing is installable from Play yet) and
iOS (no update API exists). Both keep their current store links via `AppStoreLauncher`.

## 1. Why S0 comes first

The release job prints the debug APK's signing certificate (`kmp-ci.yml:501-508`):

```
gh run view 35444863644 --log | grep -oE 'ANDROID_SHA256_FINGERPRINT=[0-9A-F]{64}'   # 1.6.12
ANDROID_SHA256_FINGERPRINT=F9E77F510CBCD1C0B426C215834EBF939AB0FD34E6FC0E7C0C0BE66E1229CD12
gh run view 35719405518 --log | grep -oE 'ANDROID_SHA256_FINGERPRINT=[0-9A-F]{64}'   # 1.6.13
ANDROID_SHA256_FINGERPRINT=630CE1314922E6289E7DDF69DB32FEB357A36CE85BD0E18B91D3D5E578E8DC62
```

No job restores a debug keystore (`.github/actions/android-ci-setup/action.yml` has none; `grep
debug.keystore .github` finds nothing), so every runner signs with a key it just generated. Android
installs an update only when the certificate matches, so one alpha cannot replace another — not from
the App Tester app, and not from any in-app updater.

**Migration, and it cannot be skipped:** a new stable key does not repair the APKs already on
testers' phones. Every tester uninstalls once, then installs the first stable-key build; from then on
updates land in place. Tell them to open the app online first so the sync outbox drains — uninstalling
drops the session and any unsynced local data.

*(Adjacent, not in scope: the website's `assetlinks.json` carries one run's fingerprint, so App Links
verification breaks for every other alpha. A stable key fixes it once the site is updated.)*

## 2. S0 — stable alpha signing key

A composite action mirroring `.github/actions/android-release-signing-setup`:

```bash
mkdir -p "$HOME/.android"                                 # a fresh runner has no ~/.android
printf '%s' "$AMMO_ALPHA_KEYSTORE_BASE64" | base64 --decode --ignore-garbage > "$HOME/.android/debug.keystore"
keytool -list -keystore "$HOME/.android/debug.keystore" -storepass android -alias androiddebugkey >/dev/null \
  || { echo "::error::alpha keystore did not validate"; exit 1; }
```

`--ignore-garbage` for the reason the release action documents at its `action.yml:48-50` (`base64`
wraps at 76 chars). **Do not reuse the release action:** it writes `keystore.properties`, which
`loadReleaseSigningProperties()` (`androidApp/build.gradle.kts:74-106`) turns into the *release*
signing config.

| Job | Install step | Fingerprint guard |
|---|---|---|
| `release` (`kmp-ci.yml:493`) | before `assembleDebug` | it already computes the fingerprint (`:501-508`); add the comparison against `vars.ALPHA_CERT_SHA256` |
| `distribute-alpha` (`kmp-ci.yml:641`) | before `assembleDebug` | **new step** — `print-android-apk-fingerprint.sh` appears exactly once in the workflow (`:504`) |

`android-ci` also runs `assembleDebug` (`:273`), but that APK only feeds the instrumented tests.
Leave it. If the guard shows AGP ignored `~/.android/debug.keystore`, override
`signingConfigs.getByName("debug")` from environment variables instead.

**Proof:** two separately built alphas print the same fingerprint, equal to `ALPHA_CERT_SHA256`; then
on a phone, alpha N+1 installs over alpha N from the App Tester app with no uninstall and the session
still signed in.
**Negative control:** run the guard against the 1.6.12 fingerprint (`F9E77F51…`) — it must fail.

## 3. S1 — Firebase in-app update for the tester build

### Dependencies

Firebase's split is mandatory, not architecture: the full SDK "contains self-update functionality
that may be considered a violation of Google Play policy, even if that code is not executed at
runtime", so it may ship only in the tester build.

| Artifact | Where | Why |
|---|---|---|
| `firebase-appdistribution-api:16.0.0-beta15` | `composeApp` androidMain `implementation`, inside the `firebaseCrashlyticsEnabled` guard (`composeApp/build.gradle.kts:167-169`) | what the code compiles against; inert in release builds |
| — | `:androidApp` needs it **only if** androidApp code touches the SDK. composeApp declares its dependencies as `implementation`, so they are not on androidApp's compile classpath — the repo documents exactly this at `androidApp/build.gradle.kts:193-195`. Keeping the call inside composeApp (below) avoids the second declaration. | |
| `firebase-appdistribution:16.0.0-beta15` | `:androidApp` **`debugImplementation`**, same guard (`androidApp/build.gradle.kts:180-184`) | debug APK = the Firebase build (`kmp-ci.yml:493,641`); release = the Play AAB |

`beta15`, not the current `beta20`: beta20 needs firebase-common 22.0.1 while crashlytics 19.4.0
resolves 21.0.0. Confirm with
`./gradlew :androidApp:dependencyInsight --dependency com.google.firebase:firebase-common --configuration debugRuntimeClasspath`.

### Code

- **Automatic check:** keep the SDK call inside `composeApp/src/androidFirebase/…`, next to
  `AndroidFirebasePlatformModule`. That source set is compiled only when `google-services.json`
  exists, and the no-Firebase module already supplies the fallback, so nothing breaks without it —
  and `MainActivity` never references the SDK, which matters because composeApp's `implementation`
  dependencies are not on androidApp's compile classpath (`androidApp/build.gradle.kts:193-195`).
  Guard it on the build being debuggable (`applicationInfo.flags and FLAG_DEBUGGABLE != 0`), then
  call `FirebaseAppDistribution.getInstance().updateIfNewReleaseAvailable()`: it handles tester
  sign-in, the prompt, the download and the install handoff. Failures are logged, never a crash.
  Trigger it once per launch from `App.kt` through the same platform contract as the button (a
  second method, no-op on iOS) — not from commonMain calling the store launcher, which would open
  the App Store on every iOS launch. If the call must live in `:androidApp` instead, declare
  `firebase-appdistribution-api` there too, inside the `firebaseCrashlyticsEnabled` guard.
- **Manual button:** it already exists (`HomeAccountSheet.kt:190-213` →
  `HomeScreenModel.checkForUpdates()` at `:635`) and goes through the `AppStoreLauncher` domain
  contract. Keep that boundary — shared code must not gain Firebase dependencies. Add one method to
  that existing interface (e.g. `requestUpdate()`), implemented in
  `AndroidAppStoreLauncher` by the same SDK call, and on iOS by today's `openStoreListing`. No new
  interface, no controller, no preferences store.
- **Do not select the updater from `ReleaseChannel`:** the release job's Firebase APK is named
  `1.6.13` with no suffix, which `ReleaseChannel.fromVersionName` (`ReleaseChannel.kt:20-24`) reads as
  Production. Decide on the build, as above.

### Not built (§6 says when to revisit)

An updater interface with result types, a policy controller, a preferences store, throttle/snooze,
an in-app toggle, an app-owned dialog, new strings, the Play adapter, the iOS version check.

### Release-build guard

Fail every AAB job when the **resolved release classpath** carries the full SDK:

```bash
./gradlew :androidApp:dependencies --configuration releaseRuntimeClasspath \
  | grep -q 'com.google.firebase:firebase-appdistribution:' \
  && { echo "::error::full App Distribution SDK on the release classpath"; exit 1; } || true
```

**Negative control:** on a scratch branch, make the full SDK a plain `implementation` — the guard must
fail. (`firebase-appdistribution-api:` does not match that grep.)

### Acceptance test — on a phone, not in CI

Compilation and unit tests cannot see any of this. With alpha N installed (stable key, S1 included):

1. Publish alpha N+1 (any merge to `main` does it). Open the app: the SDK prompts; accept; the APK
   downloads; the installer runs; the app reopens on N+1 with the session intact.
2. Decline the prompt: the app continues normally.
3. No update available: nothing appears, no error.
4. Offline: the check fails silently, and nothing blocks the UI.
5. First run on a device: the tester sign-in flow appears once. Check it does not disturb
   `MainActivity`'s auth and invite deep links (`singleTop`, `AndroidManifest.xml:39`,
   `MainActivity.kt:31-34`, `onNewIntent`).

Expect Android's "install unknown apps" consent once, then the system install confirmation each time.
Both are unverified assumptions — confirm them in step 1.

## 4. Decisions

| # | Question | Default |
|---|---|---|
| D1 | Feedback when the manual button finds nothing | None in the first cut. An "up to date" message needs `checkForNewRelease()` plus sign-in and failure handling and one new string in all 8 locales — a small feature, not a line of code. |
| D2 | How often the automatic check runs | Every cold start, no throttle. Whether a declined prompt returns on the next launch is **unverified** — if it nags in step 2 of the acceptance test, add a per-version dismiss then, not before. |
| D3 | Firebase SDK version | `16.0.0-beta15` (see above) |
| D4 | Where the alpha key lives | A GitHub secret, restored to the default debug keystore path. A committed key would let anyone sign an "update" that testers' phones accept. |

## 5. Things only you can do

1. Create the key and register it (keep an offline copy — losing it costs everyone another uninstall):
   ```bash
   keytool -genkeypair -v -keystore ammo-alpha.keystore -storepass android -keypass android -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
   ```
   ```bash
   base64 -i ammo-alpha.keystore | gh secret set AMMO_ALPHA_KEYSTORE_BASE64
   ```
   ```bash
   gh variable set ALPHA_CERT_SHA256 --body "$(keytool -list -v -keystore ammo-alpha.keystore -storepass android -alias androiddebugkey | awk '/SHA256:/{print $2}' | tr -d ':')"
   ```
2. Enable the **App Distribution Tester API** in Google Cloud for Firebase project `37917280954`.
3. Tell the testers about the one-time uninstall (§1), after S0 ships.
4. Run the acceptance test (§3) — this Mac has no emulator, and CI cannot sign in as a tester.
5. Optional, adjacent: update the alpha fingerprint in `assetlinks.json` in `Rob971/mymultiverse-website`.

## 6. Deferred, and what would bring it back

| Deferred | Revisit when |
|---|---|
| Play in-app updates (`app-update-ktx:2.1.0`, flexible flow) | A build is installable from Play. Today's production uploads fail (runs `35725517800`, `35750563119`) and the last internal-track run was cancelled. |
| An updater interface and controller | A second source exists — i.e. together with the Play adapter. One caller does not earn an abstraction. |
| Throttle, snooze, in-app toggle | The acceptance test shows the SDK nags (D2). |
| App-owned localized dialog | Testers report the SDK dialog's language or wording is a problem. |
| iOS version check (App Store lookup) | Someone asks for it; iOS has no in-place update either way. |

## 7. Traps this repo has already paid for

- A merge to `main` ships an alpha (`distribute-alpha`), so S1 reaches testers the moment it merges.
  After any merge read `gh run list --commit <sha>` for both KMP CI and Supabase Deploy.
- Instrumented tests build their own Koin graph (`InstrumentedKoinHost`); a new injected dependency
  must be registered there. `HomeHouseholdUxInstrumentedTest.kt:158` passes `onCheckForUpdates = {}`,
  so it renders the sheet and proves nothing about the flow.
- `checkForUpdates_delegatesToAppStoreLauncher` (`HomeScreenModelTest.kt:791-803`) asserts today's
  behaviour and will need updating.
- `FirebaseBuildFlags` and `AppBuildInfo` are generated (`composeApp/build.gradle.kts:23-43`, `:62-80`);
  add fields in the generator. `AppBuildInfo.VERSION_CODE` is the raw properties value, never the
  installed one.
- A new string resource needs its explicit `import ammo.composeapp.generated.resources.<key>`, and the
  key registries contain more than one `listOf(...)` — assert a scripted edit's anchor matches once.
- `am instrument` exits 0 even when the process crashes; an instrumented CI job counts only when it
  reports `started=N passed=N`.
- Never dispatch `distribute-production` as a test — the Play secrets are real.
- `export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home` before Gradle.

## Sources

- https://firebase.google.com/docs/app-distribution/set-up-alerts?platform=android
- https://firebase.google.com/docs/reference/android/com/google/firebase/appdistribution/FirebaseAppDistribution
- https://developer.android.com/guide/playcore/in-app-updates (deferred path)
- Google Maven POMs for `firebase-appdistribution(-api)` beta15/beta20 and `firebase-crashlytics` 19.4.0
