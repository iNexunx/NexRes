# NexRes

[![Build APK](https://github.com/iNexunx/NexRes/actions/workflows/build.yml/badge.svg)](https://github.com/iNexunx/NexRes/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/iNexunx/NexRes?sort=semver)](https://github.com/iNexunx/NexRes/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Minimal display **resolution & density** changer for Android, in the spirit of
Resolution Changer but without the bloat: **one screen, no ads, no account, no
pairing, no automations**.

It changes the `wm size` / `wm density` overrides through **Shizuku** (no root
required) and falls back to a **root shell** when Shizuku is unavailable. Pick
whichever you have — the app detects both.

## Features

- Modern **Material 3 / Jetpack Compose** UI with a gradient hero card showing
  current + physical size and density, and a live applied/overridden pill.
- Clear backend status (Shizuku connected / permission needed / not running, or
  root) with one-tap actions to **grant** permission or **open** Shizuku.
- Detects **both** Shizuku and root; when both are usable a small selector lets
  you pick the backend.
- Adjust **width, height and density** with sliders *and* exact numeric fields.
- **Apply** new values or **Reset** the overrides back to defaults.
- Save / apply / delete **presets** (persisted with DataStore) for one-tap
  switching between resolutions.
- Custom **adaptive launcher icon** (background / foreground / monochrome) and a
  custom color palette (no dynamic-color bleed).
- **Localized** in 7 languages — English, Spanish, French, German, Portuguese,
  Italian and Russian — with a per-app language picker (`localeConfig`).

## Screens

- **Hero card** — current resolution, density and physical panel info.
- **Backend card** — access status, action button and backend switch.
- **Editor card** — sliders + numeric inputs, Apply / Reset.
- **Presets** — name a size, save it, apply or delete in a tap.

## Backends

| Backend | Requirement |
| --- | --- |
| Shizuku | [Shizuku](https://shizuku.rikka.app/) installed and running; grant permission in-app |
| Root | `su` available on the device |

The app never requests root or Shizuku permission by itself; for Shizuku you tap
**Grant**. Root commands are tried two ways for maximum compatibility — `su -c`
and an interactive `su` shell fed through stdin — so Magisk, KernelSU, SuperSU
and ROM superuser binaries all work.

## Languages

English · Español · Français · Deutsch · Português · Italiano · Русский

Missing a translation? Copy `app/src/main/res/values/strings.xml` into a new
`values-<code>/strings.xml` and open a pull request.

## Stack

- Kotlin `2.4.21`, AGP `9.4.1` (built-in Kotlin — no `kotlin-android` plugin),
  Gradle `9.8.1`.
- Jetpack Compose (BOM `2026.09.00`) + Material 3.
- `lifecycle-viewmodel-compose` / `lifecycle-runtime-compose`, DataStore
  Preferences, Shizuku `13.1.5`.
- `compileSdk`/`targetSdk` 37, `minSdk` 26.

## Build

Local (requires JDK 17+ and the Android SDK):

```bash
./gradlew assembleDebug     # debug APK
./gradlew assembleRelease   # R8, signed with the AOSP testkey
# APK -> app/build/outputs/apk/debug|release/
```

Release builds run **R8** (`isMinifyEnabled` + `isShrinkResources`) and are
signed with **APK Signature Scheme v1 + v2 + v3**.

## Signing

The release build is signed with the **public AOSP testkey** committed in
[`keystore/`](keystore/README.md) — the well-known `build/make/target/product/security`
key (subject `CN=Android`). It is a **public, non-secret** key meant for testing,
**not** for Play Store or any real distribution.

Environment variables can override it (e.g. in CI):

| Variable | Default |
| --- | --- |
| `KEYSTORE_PATH` | `keystore/testkey.p12` |
| `KEYSTORE_PASSWORD` | `android` |
| `KEY_ALIAS` | `platform` |
| `KEY_PASSWORD` | value of `KEYSTORE_PASSWORD` |

## CI (GitHub Actions)

[`.github/workflows/build.yml`](.github/workflows/build.yml) runs on a `v*` tag,
a pull request or a manual dispatch:

1. Sets up JDK 21, the Android SDK and Gradle.
2. Builds `assembleDebug` and `assembleRelease` (R8, signed).
3. Re-signs the release APK so it carries **v1 + v2 + v3** (AGP omits v1 at
   `minSdk` ≥ 24), then verifies all three with `apksigner`.
4. Uploads artifacts: `app-debug`, `app-release`, `mapping` (R8 mapping file).

On a `v*` tag it also publishes a GitHub Release with the signed release APK.

## Notes

- `wm size` / `wm density` are the same commands Resolution Changer uses; the
  override is reset by the system on reboot unless you reapply it.
- Shizuku must be running before the app can read or change anything.

## License

Released under the [MIT License](LICENSE) © 2026 iNexunx.
