## 2026-08-20 — Account endpoints working end to end

**Built**
- `CreateAccountRequest` and `AccountResponse` as records in a new `dto`
  package, with Bean Validation annotations mirroring the database
  constraints.
- `AccountService` with `createAccount` and `getAccount`, constructor
  injection of `AccountRepository`, and `@Transactional` boundaries.
- `AccountNotFoundException` as a domain-specific exception.
- `AccountController` exposing `POST /accounts` (201 with a Location header)
  and `GET /accounts/{id}` (200).
- Verified end to end: created an account over HTTP, fetched it back, and
  confirmed the row directly in PostgreSQL with psql rather than trusting the
  API response. `overdraft_limit` stored as 500.0000, so the numeric(19,4)
  scale survives the whole round trip.
- Confirmed validation rejects a blank name, lowercase currency and negative
  overdraft with 400 before the service method runs.

**Broke**
- Long pastes into IntelliJ kept truncating — `AccountService` arrived twice
  with the methods missing, the second time parsing cleanly but doing nothing.
  A truncated paste looks correct at the top, so check the last line matches
  what was expected. The compiler is the authority, not the editor.
- Created the `dto` package at `src/main/java/dto` instead of under the
  application package, again. The New Package dialog pre-fills the parent
  path; the fix is to read the box before typing rather than typing the full
  name over it.
- PowerShell mangles escaped quotes in `curl -d`, so the JSON never arrived
  and the endpoint returned 400 for the wrong reason. `Invoke-RestMethod` with
  `ConvertTo-Json` avoids the escaping entirely.
- `docker compose` failed with "no configuration file provided" in a second
  terminal — wrong working directory, not a Compose problem.

**Decided**
- DTOs rather than exposing entities. The response carries `balance`, which
  has no column, so the API shape and the schema shape differ deliberately —
  the derived-balance decision surfacing at the boundary.
- Records for DTOs: immutable, no behaviour, everything generated from one
  declaration.
- Validation duplicated between DTO annotations and database constraints on
  purpose. The database must be correct regardless of caller; the API
  boundary exists to give clients a useful message instead of a constraint
  violation.
- Constructor injection over field `@Autowired` — dependencies are explicit,
  the field can be final, and the service is unit-testable without Spring.
- `@Transactional(readOnly = true)` on the getter: lets Hibernate skip dirty
  checking and states intent.
- `calculateBalance` left as a placeholder returning zero. A new account has
  no ledger entries, so this is correct until transactions exist.

**Known gaps**
- `GET /accounts/{id}` on a missing account returns 500, not the 404 the
  acceptance criteria specify. `AccountNotFoundException` propagates
  unhandled.
- The 400 response uses Spring's default error shape, not the documented
  contract (code, message, timestamp, path, field errors).
- Both are blueprint step 11 and are the next piece of work.

**Can explain without help**
- Why `@Valid` is required for Bean Validation annotations to do anything.
- Why DTOs decouple the API contract from the database schema.
- What `ResponseEntity.created(location)` returns and why 201 plus a Location
  header is the convention for creation.
- Why constructor injection beats field injection for testability.
- What `@Transactional` guarantees and why it will matter for the transaction
  submission endpoint.

**Next**
Global exception handler and the error contract: map
`AccountNotFoundException` to 404 and `MethodArgumentNotValidException` to the
documented 400 shape with field errors.



## 2026-08-12 — Flyway migrations

**Built**
- V1__create_core_tables.sql: account, transactions, request_record and alert,
  with named check constraints, foreign keys, and the unique constraints on
  idempotency_key and (transaction_id, rule_code).
- V2__add_indexes.sql: one composite index on transactions
  (account_id, occurred_at DESC), plus comments recording the indexes
  deliberately omitted and why.
- Verified against the running database with `\dt`, `\d transactions` and a
  query on flyway_schema_history. Both migrations applied, both successful.

**Broke**
- Flyway silently did not run. No error, no log line, green build, empty
  database. Cause: Spring Boot 4 no longer auto-configures Flyway from
  `flyway-core` alone — the auto-configuration moved into
  `spring-boot-starter-flyway` as part of the modularization. Swapping the
  dependency fixed it. This is the third Boot 4 modularization problem so far,
  after the per-module test starters and the Testcontainers artifact rename.
  General rule: in Boot 4 a raw library on the classpath is not wired up; it
  needs its starter.
- Two SQL syntax errors in V1: a missing `);` closing the transactions table,
  then a `):` typo in its place. Both were reported by PostgreSQL at the *next*
  `CREATE` keyword rather than at the actual mistake — the parser fails one
  token past the problem, so look immediately before the reported position.
- Each failure had to be cleared before Flyway would retry, since it records
  failed migrations in flyway_schema_history. Used `docker compose down -v` to
  drop the volume, which is fine for a dev database with nothing in it;
  `flyway repair` is the equivalent for anything real.
- Wasted time investigating the migration folder path on a false lead — the
  folder was correct all along, IntelliJ just compacts `db/migration` into a
  single `db.migration` row in the project tree. Confirmed with `dir` rather
  than by reading the IDE.

**Decided**
- `transactions` plural, because `transaction` is a reserved SQL word. Will map
  the entity with `@Table(name = "transactions")`.
- `numeric(19,4)` for money, never a floating-point type. Binary floating point
  cannot represent decimal fractions exactly and the error compounds across a
  ledger. Maps to BigDecimal.
- Named every constraint rather than letting PostgreSQL generate names, so
  error messages are meaningful and tests can assert on which constraint fired.
- Regex check on currency (`^[A-Z]{3}$`) rather than relying on char(3) alone,
  which would accept 'ab1'.
- `request_record.transaction_id` left nullable, so a request that was received
  and rejected can still be recorded and a retry gets the same response.
- No `updated_at` on any table — nothing is ever updated. The immutability
  decision shows up as an absence in the schema.
- V2 contains only one index. Omitted `transactions(type)` (two distinct
  values, so an index would match roughly half the table and the planner would
  prefer a sequential scan), `alert(transaction_id)` (redundant — the UNIQUE
  (transaction_id, rule_code) constraint creates a composite index whose
  leading column is transaction_id), and `request_record(idempotency_key)`
  (redundant for the same reason). Not yet verified against a real query plan;
  will check with EXPLAIN ANALYZE once there is data.

**Can explain without help**
- Why Flyway rather than Hibernate owns the schema, and what
  `ddl-auto=validate` does.
- Why migrations are immutable and checksummed, and why a change means adding
  V3 rather than editing V1.
- Why a composite index on (a, b) serves queries on `a` or on `a` and `b`, but
  not on `b` alone.
- Why a unique constraint gives you an index for free, and why that makes some
  additional indexes redundant.
- Why low-cardinality columns rarely justify an index.
- Why exact decimal types matter for money.

**Next**
Domain layer: Account and Transaction entities, enums for status and type,
repository interfaces, BigDecimal for money and Instant for timestamps.
`ddl-auto=validate` will finally have something to check the mapping against.




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