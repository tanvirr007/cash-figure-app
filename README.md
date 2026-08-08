# Cash Figure — Bangladeshi Cash Counter

[![Build APK](https://github.com/tanvirr007/cash-figure-app/actions/workflows/build_apk.yml/badge.svg)](https://github.com/tanvirr007/cash-figure-app/actions/workflows/build_apk.yml)

**Cash Figure** is a production-quality commercial Android application built specifically for counting Bangladeshi cash (Taka). Designed for daily use by shop owners, supermarkets, mobile banking agents, accountants, wholesalers, and restaurants across Bangladesh.

---

## Key Features

- **Bangladeshi Denominations**: Full support for ৳1000, ৳500, ৳200, ৳100, ৳50, ৳20, ৳10, ৳5, ৳2, and ৳1 notes/coins.
- **Live Calculation**: Instant calculation of row totals, grand total, total cash pieces, and active denominations without pressing "Calculate".
- **Amount in Words (100% Offline)**: Converts grand totals to words in English and Bangla up to **999 Crore** using the Bangladeshi numbering system (Lakh/Crore).
- **Bangladeshi Currency Formatting**: Native digit grouping (`৳1,25,650`) and Bengali numeral formatting (`৳১,২৫,৬৫০`).
- **History & Auto-Save**: Automatically persists calculations to Room DB. Supports searching, renaming, duplicating, pinning, favoriting, and restoring deleted calculations.
- **Reports & Printing**: Export reports in PDF, CSV, and TXT formats directly to `Download/CashFigure/`. Includes native Android `PrintManager` printing support and one-tap sharing via `FileProvider`.
- **JSON Backup & Restore**: Backup all app data to JSON and restore seamlessly with schema version safety.
- **Privacy First**: 100% offline with zero network permissions, zero tracking, zero ads, and zero account requirements.
- **Screen On Mode**: Automatically keeps the screen awake during active cash counting sessions.

---

## Tech Stack & Architecture

- **Language**: Kotlin (100%)
- **UI Framework**: Jetpack Compose + Material Design 3 (Teal/Amber Palette)
- **Architecture**: MVVM + Clean Architecture (Data, Domain, UI layers)
- **Database**: Room Database (SQLite)
- **Preferences**: Jetpack DataStore
- **Dependency Injection**: Hilt
- **Async Execution**: Kotlin Coroutines + StateFlow / Flow
- **CI/CD Pipeline**: GitHub Actions (`build_apk.yml`) with automated Telegram release notifications (`scripts/bot.py`)

---

## Screenshots & Design

- **Light & Dark Theme Support**: Automatic Material You dynamic colors on Android 12+.
- **Adaptive Icon**: High-resolution custom Android adaptive icon.

---

## Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/tanvirr007/cash-figure-app.git
   cd cash-figure-app
   ```
2. Open in Android Studio (Ladybug or newer) with JDK 17+.
3. Run debug build:
   ```bash
   ./gradlew assembleDebug
   ```
4. Run unit tests:
   ```bash
   ./gradlew test
   ```

---

## License

Distributed under the MIT License. See `LICENSE` for more information.