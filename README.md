# Cash Figure App

Cash Figure is a production-quality commercial Android application built specifically for counting Bangladeshi cash (Taka). Designed for daily use by shop owners, supermarkets, mobile banking agents, accountants, wholesalers, and restaurants across Bangladesh.

---

## Key Features

- Bangladeshi Denominations: Full support for 1000, 500, 200, 100, 50, 20, 10, 5, 2, and 1 Taka notes/coins.
- Live Calculation: Instant calculation of row totals, grand total, total cash pieces, and active denominations without pressing a calculate button.
- Amount in Words (100% Offline): Converts grand totals to words in English and Bangla up to 999 Crore using the Bangladeshi numbering system (Lakh/Crore).
- Bangladeshi Currency Formatting: Native digit grouping and Bengali numeral formatting.
- History and Auto-Save: Automatically persists calculations to Room DB. Supports searching, renaming, duplicating, pinning, favoriting, and restoring deleted calculations.
- Reports and Printing: Export reports in PDF, CSV, and TXT formats directly to the device storage. Includes native Android PrintManager printing support and one-tap sharing via FileProvider.
- JSON Backup and Restore: Backup all app data to JSON and restore seamlessly with schema version safety.
- Privacy First: 100% offline with zero network permissions, zero tracking, zero ads, and zero account requirements.
- Screen On Mode: Automatically keeps the screen awake during active cash counting sessions.

---

## Tech Stack and Architecture

- Language: Kotlin (100%)
- UI Framework: Jetpack Compose + Material Design 3 (Teal/Amber Palette)
- Architecture: Model-View-ViewModel (MVVM) + Clean Architecture (Data, Domain, UI layers)
- Database: Room Database (SQLite)
- Preferences: Jetpack DataStore
- Dependency Injection: Hilt
- Async Execution: Kotlin Coroutines + StateFlow / Flow
- CI/CD Pipeline: GitHub Actions with automated Telegram release notifications

---

## Project Structure

The project follows a standard Clean Architecture modular pattern:

- app/src/main/java/app/cash/tanvir/info/
  - data/: Implementations of data sources, local databases (Room), preferences (DataStore), and repositories.
  - domain/: Core business logic, domain models, and repository interfaces.
  - ui/: UI components, Compose screens, navigation controllers, and theme configurations.
    - screen/calculator/: Main calculator interface, denomination inputs, and breakdown dialogs.
    - screen/history/: History listing with search, filters, actions, and statistics.
    - screen/report/: Export options, print managers, and report preview screen.
    - screen/settings/: Language configurations, backup/restore, haptic feedback, and misc options.
  - util/: Helper classes for formatting, number-to-words conversions, and report generation (PDF, CSV, TXT).

---

## Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/tanvirr007/cash-figure-app.git
   cd cash-figure-app
   ```
2. Open the project in Android Studio (Ladybug or newer) with JDK 17+.
3. Run debug build:
   ```bash
   ./gradlew assembleDebug
   ```
4. Run unit tests:
   ```bash
   ./gradlew test
   ```

---

## Data Portability (JSON Backup)

The application supports backing up history transactions to a JSON format, facilitating easy migration or backup:
- Format: Native JSON arrays mapping to sheet records.
- Versioning: Handled inside database schemas, ensuring backward compatibility.

---

## License

Distributed under the MIT License. See LICENSE for more information.