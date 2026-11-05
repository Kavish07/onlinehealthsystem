# OnlineHealthSystem

Patient/doctor appointment and treatment management system. It started as a legacy
Servlet/JDBC app (`com.onlinehealthsystem`) and is being migrated to Spring Boot +
Spring Data JPA (`com.system.OnlineHealthSystem`) one entity at a time; views stay on
JSP throughout.

## Current state

The entity migration (see [`docs/PRD-spring-boot-migration.md`](docs/PRD-spring-boot-migration.md))
is essentially complete:

- `com.system.OnlineHealthSystem` has `@Entity`/`JpaRepository`/`@Service`/`@Controller`
  classes for Patient, Doctor, Appointment, Treatment, Bill, and LoginHistory.
- The old DAOs and `HttpServlet`s are gone. `com.onlinehealthsystem` only holds a few
  classes the Spring code still depends on directly (`PasswordEncoder`, `AppConstants`,
  and the legacy `Patient`/`Doctor`/`Appointment`/`Treatment` POJOs) — a known, deliberate
  transitional coupling, not something to "clean up" incidentally.
- `OnlineHealthSystemApplication` is the sole runtime entry point.

## Tech stack

- Java 17, Spring Boot 4.1 (Web MVC + Data JPA), packaged as a WAR
- PostgreSQL (see [ADR 0004](docs/adr/0004-migrate-database-to-postgresql.md) for the
  MySQL → PostgreSQL switch)
- JSP views, served via `tomcat-embed-jasper` + JSTL (see
  [ADR 0002](docs/adr/0002-keep-jsp-no-rest-api.md) for why no Thymeleaf/REST API)
- JUnit 5 + Mockito + Testcontainers (PostgreSQL) for tests

## Running locally

Requires a local PostgreSQL instance matching `src/main/resources/application.properties`:

- URL: `jdbc:postgresql://localhost:5432/onlinehealthsystem`
- User/password: `postgres` / `root`
- Schema: load [`src/test/resources/schema.sql`](src/test/resources/schema.sql) — it's the
  only schema file in the repo (test fixture and production bootstrap script both; see
  [ADR 0003](docs/adr/0003-colocate-app-and-mysql-for-deployment.md)). `ddl-auto=validate`
  means the app **won't start** if the schema doesn't already match the entities.

Then:

```bash
./mvnw spring-boot:run
```

The app listens on port 8080.

## Testing

```bash
./mvnw test
```

Tests run against a disposable Testcontainers PostgreSQL instance (no local Postgres
needed for `test`, but Docker is required). CI runs the same command on every push/PR to
`master` via [`.github/workflows/ci.yml`](.github/workflows/ci.yml).

## Deployment

`Dockerfile` + `docker-entrypoint.sh` build a single image that runs PostgreSQL and the
app as two processes in one container (see [ADR 0003](docs/adr/0003-colocate-app-and-mysql-for-deployment.md)
for why). `fly.toml` plus the `deploy` job in the CI workflow push that image to Fly.io on
every push to `master`.

## Docs

- [`docs/PRD-spring-boot-migration.md`](docs/PRD-spring-boot-migration.md) — migration plan, scope, and success criteria
- [`docs/IMPROVEMENTS.md`](docs/IMPROVEMENTS.md) — backlog of post-migration code-health/deployment work
- [`GLOSSARY.md`](GLOSSARY.md) — domain terminology (Login, LoginHistory, Role, Admin, Bill)
- [`docs/adr/`](docs/adr/) — architecture decision records
