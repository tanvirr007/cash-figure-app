# ota.md — Cash Figure OTA Updater Design

Single exception to the offline rule: in-app OTA updates. Manifest check + APK download/install via GitHub Releases. No analytics, no tracking, no other network usage.

## 1. Network endpoints

| Endpoint | URL (raw GitHub CDN) | Used for |
|---|---|---|
| Manifest | `https://raw.githubusercontent.com/tanvirr007/cash-figure-app/main/version.json` | Update check (launch auto-check + manual) |
| Changelog | `https://raw.githubusercontent.com/tanvirr007/cash-figure-app/main/changelog.json` | Changelog card in Settings (lazy fetch on first expand) |
| APK | `https://github.com/tanvirr007/cash-figure-app/releases/download/<version>/CashFigure.apk` (from manifest `downloadUrl`) | Download + install |

Permissions: `INTERNET` + `REQUEST_INSTALL_PACKAGES` only. `User-Agent: CashFigure-OTA/2.3.1`, connect timeout 10 s, read timeout 30 s.

## 2. Manifest contract (`version.json`)

```json
{
  "versionCode": 89,
  "versionName": "v4.4.38",
  "downloadUrl": "https://github.com/tanvirr007/cash-figure-app/releases/download/v4.4.38/CashFigure.apk",
  "changelog": "* **fix: ...** (abc1234)\n  - ...",
  "fileSize": 6323716
}
```

- Required: `versionCode > 0`, non-blank `downloadUrl`. Else parser returns null.
- `fileSize` optional (positive → stored, else null).
- Parsed by `util/UpdateManifestParser.kt` (pure org.json, unit-tested). Malformed → null, never throws.

## 3. Changelog contract (`changelog.json`)

```json
{
  "releases": [
    {
      "tagName": "v4.4.38",
      "publishedAt": "2026-08-14T11:49:59Z",
      "items": [
        { "title": "fix: resolve Compose API compile errors", "subItems": ["...", "..."] }
      ]
    }
  ]
}
```

- Newest first, commit hashes stripped.
- Parsed by `util/ChangelogParser.kt` (pure, unit-tested). Malformed → empty list.
- `stripCommitHash()` strips trailing ` (faa4d87)` — shared with the OTA dialog so both surfaces render identical titles.
- `publishedAt` ISO-8601 UTC → epoch millis; unparseable → 0.

## 4. Version comparison

`util/VersionUtil.kt` — `isUpdateAvailable(manifestName, manifestCode, installedName, installedCode)`. Update available only when manifest versionCode is newer than installed versionCode (build number wins; name is informational). Unit-tested.

## 5. State machine (`SettingsViewModel` — `UpdateStatus`)

```
IDLE → CHECKING → UP_TO_DATE
              → UPDATE_AVAILABLE → DOWNLOADING → DOWNLOAD_READY → INSTALLING → UP_TO_DATE
                                              ↘ ERROR (retry → CHECKING)
                                    ↕ cancel → IDLE
```

| State | Meaning |
|---|---|
| `IDLE` | Nothing happened yet |
| `CHECKING` | Manifest fetch in flight (2 s artificial delay — makes state perceptible) |
| `UP_TO_DATE` | Installed is newest |
| `UPDATE_AVAILABLE` | Newer version exists; update dialog visible |
| `DOWNLOADING` | APK streaming; `downloadProgress` 0..1 (-1 = indeterminate, server omitted Content-Length) |
| `DOWNLOAD_READY` | APK downloaded; `downloadedUpdate` set; Install button armed |
| `INSTALLING` | Install intent launched (transient) |
| `ERROR` | Check or download failed; `updateErrorType` set |

`UpdateErrorType`: `CHECK_FAILED` (manifest null / network) or `DOWNLOAD_FAILED` (HTTP status, empty download, IO; raw message in `updateErrorReason`).

## 6. Check flow

`checkForUpdate(installedName, installedCode, fromManualCheck)`:
- `fromManualCheck = false` → launch auto-check (silent failures, no dialog on error).
- `fromManualCheck = true` → Update screen / Settings manual check (errors show dialog; `lastSuccessfulCheck` persisted via DataStore).
- Manifest null → `ERROR/CHECK_FAILED`. No newer version → `UP_TO_DATE`. Newer → `UPDATE_AVAILABLE` + dialog.
- Guard: no-op while `CHECKING`.

## 7. Download flow

`downloadUpdate()` (repo: `UpdateRepositoryImpl.downloadApk`):
- Streams to app-private `filesDir/ota/CashFigure.apk` (8 KB buffer, progress callbacks).
- Pre-deletes any stale file. Partial download → file deleted (rollback).
- Zero-byte download → throws "Empty download".
- Success → builds FileProvider content URI (`app.cash.tanvir.info.fileprovider`, files-path) → `DownloadedUpdate(uri, file)`.
- `CancellationException` rethrown (job cancellation); other exceptions → `ERROR/DOWNLOAD_FAILED`, counters reset.
- `cancelDownload()` — cancels job, resets to `IDLE`, deletes partial file.

## 8. Install flow

- API ≥ 26 without `canRequestPackageInstalls()` → toast + `ACTION_MANAGE_UNKNOWN_APP_SOURCES` settings intent; on return, install if permission granted, else open `downloadUrl` in browser.
- Else `onInstallLaunched()` → `launchInstaller()`: `ACTION_VIEW` + `application/vnd.android.package-archive`, `FLAG_GRANT_READ_URI_PERMISSION`, `FLAG_ACTIVITY_NEW_TASK`; FileProvider content URI on **all API levels** (no MediaStore — MediaStore is only for report exports).
- `ActivityNotFoundException` → browser fallback.
- On activity resume while `INSTALLING` (`onReturnedFromInstaller`): installed code ≥ manifest code → `UP_TO_DATE` (converges stuck state); else → `DOWNLOAD_READY` (APK still on disk).

## 9. Launch auto-check (`MainActivity`)

- One-shot per process (`updateCheckDone` flag), never while app locked or onboarding.
- Silent fetch; update available → lightweight dialog ("New version X is available", Later / Update). `dismissOnBackPress` + `dismissOnClickOutside` false; Update → navigates to Update screen. Running download is cancelled on dismiss.
- Same manifest/`isUpdateAvailable` logic as manual check.

## 10. UI surfaces

| Surface | Behavior |
|---|---|
| Launch dialog (`MainActivity`) | Lightweight; hands off to Update screen for full flow |
| Update screen (`ui/screen/update/UpdateScreen.kt`) | Full-screen Pixel-style updater; shares `SettingsViewModel` via `activityViewModels()`; auto-checks on entry if `IDLE`/`UP_TO_DATE`; AnimatedContent per state; vertical scrollbar on scrollable states |
| Update dialog in Settings | "Update now" inline; `showUpdateDialog()` reopens when manifest already known |
| Changelog card (Settings) | `ChangelogStatus` beside `UpdateStatus`; lazy fetch on first expand; full-screen changelog route |

## 11. Legacy cleanup

- `cleanupLegacyOtaApk()` — best-effort delete of `Download/CashFigure/ota/CashFigure.apk` left by pre-OTA versions (MediaStore delete via `StorageUtil.deleteReportFile`).
- Runs on fresh installs only (no last-known version in DataStore).
- `allowBackup=false` → uninstall wipes all app data, no stale "update complete" toast on reinstall.

## 12. CI pipeline (`.github/workflows/build_apk.yml` + `scripts/bot.py`)

1. Push to `main` → build release APK (JDK 21).
2. `printVersionName` + latest-tag logic → next version name (tag+1 if gradle not newer); versionCode = `github.run_number` (overrides gradle value).
3. `bot.py monitor` builds + posts Telegram release notification.
4. `gh release create vX` — two assets: `CashFigure-vX.apk` + permanent-link `CashFigure.apk`.
5. `bot.py ota` → regenerates `version.json` (run number as versionCode, `fileSize` from built APK).
6. `bot.py changelog` → regenerates `changelog.json` from release bodies (newest first, commit hashes stripped).
7. Both committed as `release: OTA manifest vX [skip ci]` and pushed to `main`.

## 13. Error handling matrix

| Scenario | Result |
|---|---|
| Manifest fetch fails / malformed | `ERROR/CHECK_FAILED`; silent on launch, dialog on manual |
| HTTP non-200 on manifest/changelog | null / empty list → same as above |
| HTTP non-200 on APK | `ERROR/DOWNLOAD_FAILED` ("HTTP <code>") |
| Empty download | `ERROR/DOWNLOAD_FAILED` ("Empty download") |
| IO error mid-stream | Partial file deleted, `ERROR/DOWNLOAD_FAILED` |
| User cancels download | Job cancelled, state `IDLE`, file rolled back |
| Installer cancelled/install fails | `onReturnedFromInstaller` → `DOWNLOAD_READY` (retry possible) |
| No resolver for install intent | Browser fallback to `downloadUrl` |
| Unknown sources blocked (O) | Permission screen, then browser fallback if refused |
