---
name: git
description: Guidelines for git commits, writing commit messages, and commit/push workflow behavior.
---

# Git Skill

## Git Commit Message Pattern
- Title + Description Combo: Always use a Title and a Description together, separated by exactly one blank line (no extra blank lines).
- Title: Must be short, simple, and direct (max 35 characters, up to 40 characters preferred max). Do not end the title with a period.
- Imperative Mood: Use present-tense imperative verbs for action (e.g., fix: ..., add: ..., update: ...).
- Standard Prefixes: Always use consistent prefixes (fix:, feat:, refactor:, style:, docs:, chore:).
- Description: Must include:
  - Bullet points explaining the changes in the simplest words possible, with no blank lines between them. All bullet points must use - as a prefix and end with a period.
  - A divider line of hyphens (e.g., ----------------------------------------) before the "TEST:" section, with a blank line on top of the divider.
  - A "TEST:" section with brief, easy-to-understand bullet points detailing how to verify/test the changes. Each bullet point must end with a period.
  - A divider line of hyphens (e.g., ----------------------------------------) after the "TEST:" section.
- Change-Id: Include a Change-Id footer at the bottom of the commit message description.

## Workflow Rules
- Clean Staging: Check git status to ensure build artifacts, binaries, or temporary files are not staged accidentally.
- Signed-off-by: Always commit with the -s (signoff) flag.
- Branch & Push Safety: Verify active branch (git branch --show-current) before pushing and NEVER force push (-f/--force) without explicit user permission.
- No Auto-Commits/Pushes: Do not automatically commit or push code after making changes.
- Explicit Permission: Always ask the user for permission before executing a commit or push.
