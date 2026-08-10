## 2026-08-10 — Project contract and data model

**Built**
- README: problem, users, non-goals, acceptance criteria, endpoint table.
- docs/data-model.md: Mermaid ER diagram plus constraint rationale.

**Decided**
- Overdraft option B: per-account overdraft_limit column rather than a hard
  zero floor. Limits can vary without a code change.
- 422 for overdraft rejection, not 400. The request is well-formed; rejection
  depends on account state, so it isn't a syntax problem. Bean Validation
  failures will produce 400 separately, which is the correct split.
- No currency on Transaction. Transactions inherit the account's currency —
  fewer fields to validate, and multi-currency is an explicit non-goal.
- Balance derived from the ledger, not stored on Account.
- RequestRecord as its own table rather than a key column on Transaction.

**Broke**
- Nothing broke, but IntelliJ Community doesn't render Mermaid in preview.
  Confirmed it renders on GitHub instead.

**Can explain without help**
- Why an immutable ledger is auditable and a stored balance isn't.
- What request_hash adds over a bare idempotency key, and why the body must be
  canonicalised before hashing.
- The difference between 400, 409 and 422.
- Why the overdraft check is a concurrency hazard.

**Next**
Scaffold via Spring Initializr (Maven, Java 21, Web, Validation, Data JPA,
PostgreSQL driver, Actuator; add springdoc, Flyway, Testcontainers manually).
Docker Compose for PostgreSQL 16 before writing any entity.




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