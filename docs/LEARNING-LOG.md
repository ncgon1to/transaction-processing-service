# Learning Log

## 2026-08-07 — Day Zero: Toolchain

**Built**
Nothing yet. Development environment set up for Project 1.

**Broke**
- Nearly installed Oracle JDK 25 from an archive page instead of Temurin 21.
  Switched to Temurin for the Apache 2.0 licence and to match the versions
  used in Spring documentation.
- Temurin installer failed writing JavaSoft registry keys under HKLM. Caused
  by a per-user install having no rights to a machine-wide registry hive.
  Ignored it — nothing in the toolchain depends on those keys, and IntelliJ
  detected the JDK anyway.
- `docker` not found after install. PATH was stale in the open terminal;
  Windows only reads environment variables into new shells.

**Decided**
- Temurin 21 over Oracle: licensing, and every Spring guide assumes 21.
- Per-user install over all-users: no admin needed for quarterly patches.
- IntelliJ 2025.2 Community over 2025.3: last release with a clearly
  separated free edition.
- Skipped the VS Code keymap import — want IntelliJ's native shortcuts,
  since those are what Java shops use.

**Can explain without help**
- What JAVA_HOME is for and which tools read it.
- Why Docker means I never install PostgreSQL directly, and what that buys
  a repo in reproducibility.
- Why WSL2 is required for Docker on Windows.

**Next**
Write the Project 1 contract: problem, users, non-goals, acceptance
criteria, ER diagram for Account, Transaction, RequestRecord, Alert.