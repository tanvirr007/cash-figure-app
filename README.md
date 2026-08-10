<p align="center">
  <img src="assets/icon_512.png" alt="Cash Figure App" width="120"/>
</p>

<h1 align="center">Cash Figure App</h1>

<p align="center">
  Count Bangladeshi cash (Taka) like a pro. 100% offline, zero ads, fully open source.
</p>

<p align="center">
  <img src="https://visitor-badge.laobi.icu/badge?page_id=tanvirr007.cash-figure-app" alt="Visitors"/>
  <img src="https://img.shields.io/github/v/release/tanvirr007/cash-figure-app" alt="Latest release"/>
  <img src="https://github.com/tanvirr007/cash-figure-app/actions/workflows/build_apk.yml/badge.svg" alt="Build status"/>
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" alt="Platform"/>
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white" alt="Language"/>
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white" alt="UI"/>
  <img src="https://img.shields.io/badge/Android-7.0%2B-3DDC84" alt="Minimum Android version"/>
  <img src="https://img.shields.io/badge/Ads-None-brightgreen" alt="No ads"/>
  <img src="https://img.shields.io/badge/Open%20Source-Yes-brightgreen" alt="Open source"/>
  <img src="https://img.shields.io/github/license/tanvirr007/cash-figure-app" alt="License"/>
</p>

---

## About

Cash Figure is a production-quality Android app built specifically for counting
Bangladeshi cash (Taka). It is designed for daily use by shop owners, supermarkets,
mobile banking agents, accountants, wholesalers, and restaurants across Bangladesh.

It is **100% free, ad-free, and open source** — no ads, no tracking, no analytics,
no accounts, and no data collection. It works entirely offline, except for the
optional in-app update check.

## Features

- **Bangladeshi Denominations** — full support for 1000, 500, 200, 100, 50, 20, 10,
  5, 2, and 1 Taka notes/coins.
- **Live Calculation** — instant row totals, grand total, total cash pieces, and
  active denomination count — no calculate button needed.
- **Amount in Words (100% Offline)** — converts grand totals to words in English
  and Bangla up to 999 Crore using the Bangladeshi Lakh/Crore numbering system.
- **Bangladeshi Currency Formatting** — native digit grouping and Bengali numerals.
- **History and Auto-Save** — every calculation is persisted to a local Room
  database with search, rename, duplicate, pin, favorite, and restore-deleted.
- **Reports and Printing** — export reports as PDF, CSV, or TXT, print via the
  native Android PrintManager, and share with one tap.
- **JSON Backup and Restore** — back up all data to JSON and restore seamlessly
  with schema-version safety.
- **In-App OTA Updates** — silent update check on launch, in-app APK download with
  live progress, and a bilingual (EN/BN) changelog — see [ota.md](ota.md).
- **Screen On Mode** — keeps the screen awake during active counting sessions.
- **No Ads** — zero advertisements, zero in-app purchases, zero paywalls.
- **Open Source** — full source under the MIT license; audit it, fork it, improve it.
- **Privacy First** — 100% offline with no analytics and no tracking. The only
  network calls are the OTA update endpoints (update manifest + release APK).

## Tech Stack

| Layer            | Technology                                             |
|------------------|--------------------------------------------------------|
| Language         | Kotlin (100%)                                          |
| UI               | Jetpack Compose + Material Design 3 (Teal/Amber theme) |
| Architecture     | MVVM + Clean Architecture (data / domain / ui layers)  |
| Database         | Room Database (SQLite)                                 |
| Preferences      | Jetpack DataStore                                      |
| Dependency Injection | Hilt + KSP                                         |
| Async            | Kotlin Coroutines + StateFlow / Flow                   |
| CI/CD            | GitHub Actions with Telegram release notifications     |

## Project Structure

```
app/src/main/java/app/cash/tanvir/info/
├── domain/            # Pure Kotlin models + repository interfaces (no Android deps)
├── data/              # Room DB, DataStore, repository implementations
├── di/                # Hilt modules (DatabaseModule, RepositoryModule)
├── ui/                # Single-activity Compose app
│   ├── MainActivity.kt    # Splash, biometric lock, FLAG_SECURE, launch OTA check
│   ├── navigation/        # NavGraph — 4 routes (Calculator, History, Report, Settings)
│   ├── theme/             # Material 3 theme with Tiro Bangla font
│   └── screen/            # Calculator, History, Report, Settings (+ components)
└── util/              # Pure helpers: formatters, converters, report generators
```

Data flows one way: Screen -> ViewModel -> Repository -> Room DB / DataStore.
ViewModels expose StateFlow; screens collect state. See [docs/FILES.md](docs/FILES.md)
for a complete per-file reference.

## Getting Started

Requirements: JDK 17+ and Android Studio (Ladybug or newer).

```bash
# Clone the repository
git clone https://github.com/tanvirr007/cash-figure-app.git
cd cash-figure-app

# Build a debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test
```

The APK lands in `app/build/outputs/apk/`. Prebuilt release APKs are published on
the [Releases page](https://github.com/tanvirr007/cash-figure-app/releases) —
installable directly without Google Play.

## Data Portability

All history transactions can be exported to a JSON backup file and restored on
the same or another device. Backups are versioned inside the database schema,
so restoring data created by an older app version stays safe.

## License

Distributed under the [MIT License](LICENSE) — use it, modify it, share it.
