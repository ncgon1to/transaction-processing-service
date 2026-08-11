## 2026-08-11 - Scaffold, dependencies and local database

**Built**
- Spring Boot 4.0.7 scaffold via Spring Initializr (Maven, Java 21, Web,
  Validation, Data JPA, PostgreSQL driver, Actuator).
- Added Flyway, springdoc and Testcontainers to pom.xml by hand.
- compose.yaml running PostgreSQL 16 with a named volume and a pg_isready
  health check; credentials in an untracked .env with a committed .env.example.
- Datasource configuration in application.properties. Build green.

**Broke**
- First `mvnw verify` failed: contextLoads couldn't start the application
  because Data JPA auto-configuration wants a DataSource and none was
  configured. Compilation was fine — only the context failed. Fixed later by
  actually having a database rather than by configuring one prematurely.
- Committed `target/` by accident. GitHub's Java .gitignore template doesn't
  include it, and I'd skipped Initializr's .gitignore (which does) to keep my
  own. Removed with `git rm -r --cached target`.
- `org.testcontainers:postgresql` failed with a missing-version error.
  Two causes: Boot 4's parent POM no longer manages Testcontainers versions,
  and Testcontainers 2.x renamed its artifacts to a `testcontainers-` prefix.
  Fixed by importing testcontainers-bom 2.0.5 and using
  `testcontainers-postgresql`.
- `docker compose up` failed with "failed to connect to the docker API".
  Docker Desktop wasn't running after a reboot — not a Compose problem.
- Earlier: PowerShell's `Add-Content` with single quotes wrote a literal
  `n.idea/` instead of a newline, so .idea/ stayed untracked until I noticed
  in `git status`.

**Decided**
- Spring Boot 4.0.7 rather than 4.1.0. 3.x is no longer offered by Initializr
  (OSS support ended June 2026), so 4.x was forced; picked 4.0 over 4.1 because
  it has seven patch releases behind it and the migration guidance written
  about Boot 4 targets 4.0 specifically.
- springdoc 3.0.3. The 2.x line targets Boot 3; 3.x is the Spring Framework 7
  line. Took 3.0.3 rather than 3.0.0 because 3.0.1 fixed /v3/api-docs
  returning a Base64-encoded response under Framework 7.0.2.
- `postgres:16-alpine` pinned to a major version, not `latest`, so a fresh
  clone gets the database I developed against.
- Named volume rather than a bind mount: Docker-managed, better performance on
  Windows, no host permission issues.
- Health check with pg_isready so `--wait` blocks until Postgres actually
  accepts connections, rather than until the process merely starts.
- `ddl-auto=validate` so Flyway owns the schema and Hibernate only checks it.
- `open-in-view=false` so lazy-loading problems surface in development instead
  of being hidden by a request-scoped session.

**Can explain without help**
- Why a DataSource is required at startup once Data JPA is on the classpath.
- What a Maven BOM does and why importing one beats pinning each artifact.
- Why versions are absent on Boot-managed dependencies but required on
  springdoc.
- The difference between a named volume and a bind mount, and why containers
  need one at all.
- Why a container health check is not the same as a container being "up".
- Why Flyway rather than Hibernate should own the schema.

**Next**
Flyway V1: the four tables with primary keys, foreign keys, the unique
constraint on idempotency_key and the unique (transaction_id, rule_code) on
alert. V2 for indexes. Then the account endpoints.





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