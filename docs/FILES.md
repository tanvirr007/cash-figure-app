# FILES.md — Cash Figure File Reference

Complete per-file reference for the Cash Figure Android app. Companion to `AGENTS.md` (agent rules) and `README.md` (features/build).

## Tech Stack

Kotlin 100% · Jetpack Compose (Material 3, Teal/Amber) · MVVM + Clean Architecture · Room · DataStore · Hilt + KSP · Coroutines/Flow · 100% offline

## Source Tree

```
app/src/main/java/app/cash/tanvir/info/
├── CashFigureApp.kt                      Application class
├── domain/                               Pure Kotlin — no Android deps
│   ├── model/                            Sheet, Denomination, DenominationRow, BackupData, UpdateManifest (+ DownloadedUpdate)
│   └── repository/                       SheetRepository, SettingsRepository, UpdateRepository (interfaces)
├── data/
│   ├── local/db/                         CashFigureDatabase, SheetDao, SheetEntity
│   ├── local/preferences/                PreferencesManager (DataStore)
│   └── repository/                       SheetRepositoryImpl, SettingsRepositoryImpl, UpdateRepositoryImpl
├── di/                                   DatabaseModule, RepositoryModule (Hilt)
├── ui/
│   ├── MainActivity.kt                   Entry point: splash, biometric lock, FLAG_SECURE, launch OTA check
│   ├── navigation/NavGraph.kt            4 routes
│   ├── theme/                            Color.kt, Theme.kt, Type.kt (Tiro Bangla font)
│   └── screen/
│       ├── calculator/                   CalculatorScreen, CalculatorViewModel, components/
│       ├── history/                      HistoryScreen, HistoryViewModel
│       ├── report/                       ReportScreen, ReportViewModel
│       └── settings/                     SettingsScreen, SettingsViewModel
└── util/
    ├── BanglaDigitConverter.kt           Digits ০-৯ conversion
    ├── CurrencyFormatter.kt              Taka grouping + Bengali numerals
    ├── DateTimeFormatter.kt              EN/BN date-time strings
    ├── HapticHelper.kt                   Global haptic toggles/intensity
    ├── NumberToWordsConverter.kt         EN/BN words, Lakh/Crore up to 999 crore
    ├── PackageVersion.kt                 Installed version (name, code) reader
    ├── SizeFormatter.kt                  Byte-size formatting (B/KB/MB/GB)
    ├── UpdateManifestParser.kt           version.json parsing (pure, unit-tested)
    └── report/                           CsvReportGenerator, PdfReportGenerator,
                                         TxtReportGenerator, PrintHelper, StorageUtil

app/src/test/java/app/cash/tanvir/info/util/  6 JUnit test classes
```

## Per-File Reference

### Root / Build
| File | Lines | Purpose |
|---|---|---|
| `build.gradle.kts` | 40 | Root build script |
| `app/build.gradle.kts` | 126 | App module: compileSdk 35, minSdk 24, target 35, versionCode 3 / versionName 2.3.0, release signing via `keystore.properties`, `printVersionName` task, `org.json` test dependency |
| `settings.gradle.kts` | — | Module + repo setup |
| `gradle/libs.versions.toml` | — | Version catalog (all deps) |
| `gradle.properties` | — | Gradle JVM args |
| `version.json` | 6 | OTA manifest (v2.3.1) used by CI releases — fetched by the in-app updater |
| `app/src/main/AndroidManifest.xml` | 54 | Permissions: `WRITE_EXTERNAL_STORAGE` (maxSdk 28), `VIBRATE`, `INTERNET` + `REQUEST_INSTALL_PACKAGES` (OTA only); FileProvider + `res/xml/file_paths.xml` |
| `.github/workflows/build_apk.yml` | — | CI: build APK, notify Telegram |
| `scripts/bot.py` | — | Telegram release-notification bot |
| `keystore.properties` | — (gitignored) | Release keystore config, optional |
| `assets/` | — | App icons (512, square, bleed) |

### domain/ — Models & Repository Interfaces (no Android deps)
| File | Lines | Purpose / Key APIs |
|---|---|---|
| `domain/model/Sheet.kt` | 17 | Full calculation sheet: `id`, `name`, `rows`, computed `grandTotal`, `totalPieces`, `activeDenominations`, `createdAt`/`updatedAt`, `remark` |
| `domain/model/Denomination.kt` | 26 | `Denomination.ALL` — 1000/500/200/100/50/20/10/5/2/1 Taka with labels (EN/BN) |
| `domain/model/DenominationRow.kt` | 11 | One denomination entry: `quantity`, `total`, `isActive` |
| `domain/model/BackupData.kt` | 20 | JSON backup payload (version + sheets) |
| `domain/model/UpdateManifest.kt` | 27 | OTA manifest (`versionCode`/`versionName`/`downloadUrl`/`changelog`) + `DownloadedUpdate` result (URI + legacy file) |
| `domain/repository/SheetRepository.kt` | 23 | Sheet CRUD, search, pin/favorite, duplicate, restore-deleted, backup/restore |
| `domain/repository/SettingsRepository.kt` | 27 | Settings flows: theme, language, biometrics, haptics, screenshot block, visible denominations |
| `domain/repository/UpdateRepository.kt` | 27 | OTA contract: `fetchManifest()` / `downloadApk(manifest, onProgress)` (network-IO only) |

### data/ — Room, DataStore, Repository Implementations
| File | Lines | Purpose / Key APIs |
|---|---|---|
| `data/local/db/CashFigureDatabase.kt` | 22 | Room `@Database` (SheetEntity, version-safe) |
| `data/local/db/entity/SheetEntity.kt` | 25 | Room entity mirroring `Sheet` |
| `data/local/db/dao/SheetDao.kt` | 67 | Flow queries, upsert, delete, soft-delete/restore, search, backup export |
| `data/local/preferences/PreferencesManager.kt` | 134 | DataStore keys + flows: `AppTheme`, `AppLanguage`, biometric, haptics (enabled/intensity), screenshot block, visible denominations |
| `data/repository/SheetRepositoryImpl.kt` | 195 | Maps entity ↔ domain, applies computations, backup/restore logic |
| `data/repository/SettingsRepositoryImpl.kt` | 75 | Settings flow plumbing |
| `data/repository/UpdateRepositoryImpl.kt` | 94 | `HttpURLConnection` OTA: raw-CDN manifest fetch + streamed APK download to `Downloads/CashFigure/ota/` with progress + rollback |

### di/ — Hilt Modules
| File | Lines | Purpose |
|---|---|---|
| `di/DatabaseModule.kt` | 32 | Provides Room DB + DAO |
| `di/RepositoryModule.kt` | 36 | Binds repository interfaces to implementations (incl. `UpdateRepository`) |

### ui/ — Activity, Navigation, Theme
| File | Lines | Purpose |
|---|---|---|
| `ui/MainActivity.kt` | 277 | Single activity: splash, edge-to-edge, dark/dynamic theme, FLAG_SECURE (screenshot block), FLAG_KEEP_SCREEN_ON, biometric lock (40s background timeout), one-shot launch OTA check + lightweight update dialog, `LockScreen` composable |
| `ui/navigation/NavGraph.kt` | 70 | Sealed `Screen`: Calculator (start) / History / Report (`report/{sheetId}?fromSave=`) / Settings |
| `ui/theme/Color.kt` | 51 | Teal/Amber M3 palettes, light/dark schemes |
| `ui/theme/Theme.kt` | 97 | `CashFigureTheme`, dynamic color support |
| `ui/theme/Type.kt` | 101 | Typography with Tiro Bangla font |

### ui/screen/ — Feature Screens + ViewModels
| File | Lines | Purpose |
|---|---|---|
| `calculator/CalculatorScreen.kt` | 623 | Main counting UI: denomination rows, live totals, breakdown dialog, save flow |
| `calculator/CalculatorViewModel.kt` | 212 | StateFlow of rows, live totals, save/update sheet |
| `calculator/components/DashboardCard.kt` | 110 | Summary cards (grand total, pieces, words) |
| `calculator/components/DenominationRowItem.kt` | 140 | Single denomination row with quantity input |
| `history/HistoryScreen.kt` | 339 | History list: search, rename, duplicate, pin, favorite, restore deleted, stats |
| `history/HistoryViewModel.kt` | 123 | History state, filters, actions |
| `report/ReportScreen.kt` | 425 | Report view: sheet summary, notes, export (PDF/CSV/TXT), print, share |
| `report/ReportViewModel.kt` | 134 | Report state, export/print/share orchestration |
| `settings/SettingsScreen.kt` | 1533 | **Large** — language, theme, denominations, haptics, biometric, screenshot block, backup/restore, **updates card + OTA dialog**, reset all |
| `settings/SettingsViewModel.kt` | 431 | All settings state + backup/restore/reset actions + OTA state machine (`UpdateStatus`, check/download/cancel/dismiss) |

### util/ — Pure Helpers
| File | Lines | Purpose / Key APIs |
|---|---|---|
| `util/BanglaDigitConverter.kt` | 51 | Convert Arabic ↔ Bangla digits |
| `util/CurrencyFormatter.kt` | 66 | Taka formatting with grouping, EN/BN |
| `util/DateTimeFormatter.kt` | 60 | Date/time strings EN/BN |
| `util/HapticHelper.kt` | 47 | Global haptic enabled/intensity + perform |
| `util/NumberToWordsConverter.kt` | 222 | Amount → words, EN/BN, Lakh/Crore up to 999 crore |
| `util/PackageVersion.kt` | 29 | `getInstalledVersion(context)` — API-33/28-branch version pair reader |
| `util/SizeFormatter.kt` | 23 | `format(bytes)` → "2.60 MB" (unit-tested) |
| `util/UpdateManifestParser.kt` | 27 | `parse(body)` → `UpdateManifest?` (org.json, validation, unit-tested) |
| `util/report/CsvReportGenerator.kt` | 45 | CSV export (unit-tested) |
| `util/report/PdfReportGenerator.kt` | 320 | PDF layout with logo/seal, totals, notes |
| `util/report/TxtReportGenerator.kt` | 69 | Plain-text report |
| `util/report/PrintHelper.kt` | 73 | Android `PrintManager` printing |
| `util/report/StorageUtil.kt` | 153 | File writing to Downloads/shared storage + streaming `openReportFile`/`OpenFileHandle` for OTA APK downloads |

### Tests — `app/src/test/java/app/cash/tanvir/info/util/`
| File | Covers |
|---|---|
| `CsvReportGeneratorTest.kt` | CSV output incl. BOM handling |
| `NumberToWordsConverterTest.kt` | EN/BN words, Lakh/Crore |
| `DateTimeFormatterTest.kt` | EN/BN date-time |
| `CurrencyFormatterTest.kt` | Grouping + Bengali digits |
| `UpdateManifestParserTest.kt` | version.json parsing + validation failures |
| `SizeFormatterTest.kt` | B/KB/MB/GB formatting |

## Data Flow

```
Compose Screen ──events──> ViewModel ──calls──> Repository (impl)
     ▲                                            │
     │ StateFlow<State>                           ▼
     └──────────────────────────────  Room DAO / DataStore
```

- UI never touches DB/DataStore directly — always through ViewModel → Repository.
- Sheet computations (grand total, pieces, active count) live in `domain/model/Sheet.kt`.
- Settings UI state comes from `PreferencesManager` flows surfaced through `SettingsViewModel`.

## Navigation Map

| Route | Arguments | Destination |
|---|---|---|
| `calculator` | — | Start destination |
| `history` | — | History list |
| `report/{sheetId}?fromSave=` | `sheetId: Long`, `fromSave: Bool = false` | Report/export |
| `settings` | — | Settings |

## Conventions Cheat-Sheet

- Hilt: `@HiltViewModel` + `@AndroidEntryPoint`; modules in `di/`.
- Bilingual: `if (isBangla) "…" else "…"` inline — no string resources.
- Room changes → bump DB version in `CashFigureDatabase.kt`.
- Report generators must stay pure/side-effect free (unit-tested).
- Offline by default — the **only** network usage is the OTA updater (`INTERNET` + `REQUEST_INSTALL_PACKAGES`): raw-CDN `version.json` fetch + release APK download. Never add analytics, ads, or tracking.
- OTA install flow: MediaStore content URI on API 29+; `FileProvider` (external-path `Download/CashFigure/ota/`) on ≤ 28. Partial downloads are always rolled back.
