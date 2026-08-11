# Contributing to Cash Figure

Cash Figure is a 100% free, offline-first Android app for counting Bangladeshi
cash (Taka), built with Kotlin + Jetpack Compose. It is bilingual (English +
Bangla), open source (MIT), and welcomes contributors of every skill level —
including non-developers.

## The easiest way to help: Bangla translation

The Bangla side of the app is a work in progress. If you are a native Bangla
speaker, the full step-by-step guide is in the
[README — Bangla Translation section](README.md#bangla-translation). It covers
finding a string, fixing it, verifying the build, and opening a PR.

Before translating, read the **Settled terms** table in the same section — those
EN/BN pairs are already fixed and every new translation must reuse them.

No-code option: open an issue on the
[issues page](https://github.com/tanvirr007/cash-figure-app/issues) with the
screen name, the wrong Bangla text, and your suggested correction.

## Contributing code

- Read [README.md](README.md) for the architecture, tech stack, and build steps.
- [docs/FILES.md](docs/FILES.md) maps every file in the project.
- [ota.md](ota.md) documents the in-app OTA updater — the only network feature.
- The app is offline-only by design. Do not add analytics, tracking, or any
  network usage beyond the OTA feature.

## Commit format

Every commit follows the repo structure — title, bullet description, TEST
section, and a Change-Id footer:

```text
<type>: <short summary>

- Bullet list of what changed
- One line per change
- Explain why, not just what

TEST:
- Run ./gradlew assembleDebug and confirm the app compiles.
- <what you verified manually>

----------------------------------------
Change-Id: I<40-char hex>

Signed-off-by: Your Name <you@example.com>
```

- `<type>` is one of `fix:`, `feat:`, `style:`, `refactor:`, `docs:`, `test:`.
  `release:` is reserved for CI-generated OTA commits.
- The `Change-Id` is any 40 hex characters not already used
  (`openssl rand -hex 20` prefixed with `I`).
- Commit with `git commit -s` so the `Signed-off-by` line is added automatically.

## PR etiquette

- Don't force-push to your PR branch — add new commits instead.
- Keep your branch rebased on the latest `main` before opening the PR.
- The PR template mirrors the commit format above; fill it in completely.

Every PR is reviewed and merged gratefully. Thank you for contributing.
