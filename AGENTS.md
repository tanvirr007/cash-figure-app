# AGENTS.md

Guidance for AI agents working in the Cash Figure codebase.

## Project Overview

Cash Figure is a production-quality, 100% offline Android app for counting Bangladeshi cash (Taka). Built with Kotlin + Jetpack Compose (Material 3, Teal/Amber), MVVM + Clean Architecture, Room, DataStore, Hilt, and Coroutines/Flow. Bilingual UI (English + Bangla), including Bangla digits and the Lakh/Crore numbering system.

## Commands (Windows)

- Build debug APK: `gradlew.bat assembleDebug`
- Run unit tests: `gradlew.bat test`
- Get version name: `gradlew.bat printVersionName`
- JDK 17+, Android Studio Ladybug+ recommended.

## Architecture

Package root: `app/src/main/java/app/cash/tanvir/info/` with strict layers:

- `domain/` — pure Kotlin models (`Sheet`, `Denomination`, `DenominationRow`, `BackupData`) and repository interfaces (`SheetRepository`, `SettingsRepository`). No Android dependencies.
- `data/` — Room DB (`CashFigureDatabase`, `SheetDao`, `SheetEntity`), DataStore (`PreferencesManager`), repository implementations.
- `ui/` — single-activity Compose app; `navigation/NavGraph.kt` has 4 routes (Calculator start / History / Report / Settings), each screen = `XxxScreen` + `XxxViewModel`.
- `util/` — pure helpers: `CurrencyFormatter`, `NumberToWordsConverter` (EN/BN, Lakh/Crore), `BanglaDigitConverter`, `DateTimeFormatter`, `HapticHelper`, and `util/report/` generators (PDF/CSV/TXT + PrintHelper, StorageUtil).

Rules: data flows one way (Screen → ViewModel → Repository → DB/DataStore). ViewModels expose StateFlow; screens collect state.

## Conventions

- Dependency injection via Hilt (`@HiltViewModel`, `@AndroidEntryPoint`); modules in `di/` (Room `@Database` in `DatabaseModule`, repository bindings in `RepositoryModule`).
- Compose Material 3, dark/light/dynamic theme via `ui/theme/`, custom Tiro Bangla font for Bangla text.
- UI strings are inline with `if (isBangla) "..." else "..."` pattern — no string resources for the main UI.
- Report exports use the generators in `util/report/` — keep them side-effect free and pure (unit-tested).
- Room schema changes require a database version bump in `CashFigureDatabase.kt` (backup/restore is version-safe).
- App is offline-only: never add network permissions, analytics, or tracking.

## Large Files — Edit With Care

- `ui/screen/settings/SettingsScreen.kt` (~959 lines) and `SettingsViewModel.kt` (~305)
- `ui/screen/calculator/CalculatorScreen.kt` (~623 lines)
- `util/report/PdfReportGenerator.kt` (~320 lines)
- `ui/MainActivity.kt` — biometric lock, FLAG_SECURE, edge-to-edge, screen-on logic

## Testing

- Unit tests in `app/src/test/java/app/cash/tanvir/info/util/` cover `CsvReportGenerator`, `NumberToWordsConverter`, `DateTimeFormatter`, `CurrencyFormatter`.
- Run `gradlew.bat test` after touching any `util/` code.

## Git

Follow the `git` skill (`.opencode/skills/git/SKILL.md`): commit message pattern with Change-Id footer, `-s` signoff, clean staging, never force push, always ask before committing or pushing.

## References

- `docs/FILES.md` — complete per-file reference table and data-flow map.
- `README.md` — feature overview and build instructions.
