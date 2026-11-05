# Migrate the database from MySQL to PostgreSQL

Status: accepted

The app (both the legacy `com.onlinehealthsystem` DAOs and the Spring Boot/JPA migration) moves from MySQL to PostgreSQL. This is a straight vendor swap, not a schema redesign: table/column names, the hand-written `schema.sql` used by tests and deployment, and the JPA entity mappings are otherwise unchanged.

## Considered options
- **Stay on MySQL**: rejected — no functional reason to switch other than the explicit request driving this change; recorded here only so the "why Postgres" question has an answer.
- **Rewrite `schema.sql`/DAOs to be vendor-neutral (e.g. via an abstraction layer)**: rejected — out of proportion to a one-time vendor swap; the handful of MySQL-specific SQL extensions in use (`AUTO_INCREMENT`, inline column `COMMENT`) have direct PostgreSQL equivalents (`GENERATED ALWAYS AS IDENTITY`, dropping the inline comment in favor of a `--` line comment) that didn't need an abstraction to adopt.

## What changed
- Legacy DAOs' hardcoded JDBC URL/driver: `jdbc:mysql://localhost:3306/...` + `com.mysql.cj.jdbc.Driver` → `jdbc:postgresql://localhost:5432/...` + `org.postgresql.Driver`; credentials `root`/`root` → `postgres`/`root` (PostgreSQL has no built-in `root` superuser).
- `pom.xml`: `com.mysql:mysql-connector-j` → `org.postgresql:postgresql`; `org.testcontainers:mysql` → `org.testcontainers:postgresql`.
- `application.properties` (main): datasource URL/driver/credentials updated to match.
- `application.properties` (test): the `@DataJpaTest`/`@SpringBootTest` H2 compatibility mode switched from `MODE=MySQL` to `MODE=PostgreSQL`.
- `schema.sql`: `AUTO_INCREMENT` → `GENERATED ALWAYS AS IDENTITY`; the MySQL-only inline `COMMENT '...'` column attribute (not valid PostgreSQL DDL) replaced with a plain `--` comment. This file is shared by the H2 test slices, the Testcontainers-backed integration tests, and the Docker/Fly production bootstrap, so it has to parse under all three.
- `AbstractMySQLIntegrationTest` → `AbstractPostgreSQLIntegrationTest`: `MySQLContainer` → `PostgreSQLContainer`, fixed host port 3306 → 5432 (still bound to the DAOs' hardcoded port, per [0003](./0003-colocate-app-and-mysql-for-deployment.md)'s rationale), and the MySQL-specific `SET FOREIGN_KEY_CHECKS`/`TRUNCATE` cleanup replaced with plain `TRUNCATE ... RESTART IDENTITY` (no real FKs exist in this schema either way).
- `Dockerfile`/`docker-entrypoint.sh`: `mysql-server` + `mysqld`/`mysqladmin`/`mysql` → `postgresql` + `pg_ctlcluster`/`pg_isready`/`psql`.
- `fly.toml`: the `mysql_data` volume mounted at `/var/lib/mysql` → `postgres_data` mounted at `/var/lib/postgresql`.
- `.github/workflows/ci.yml`: the step stopping the runner's pre-installed, port-colliding database service now targets PostgreSQL (`postgresql.service`) instead of MySQL — `ubuntu-latest` ships both pre-installed.

## Consequences
- The existing Fly.io volume (`mysql_data`) isn't reused — Fly volumes can't change the mount path/rename in place, so the one-time manual setup (`fly volumes create postgres_data ...`) needs to be redone for the new volume name before the next deploy. Old MySQL data isn't migrated; this is a demo deployment with no backup/migration story either way (see [0003](./0003-colocate-app-and-mysql-for-deployment.md)'s consequences).
- JPA entity mappings (`@Table`/`@Column`) needed no changes: they already named columns explicitly and PostgreSQL, like MySQL, folds unquoted identifiers consistently, so the existing `PhysicalNamingStrategyStandardImpl` override (added to keep explicit names authoritative — see `application.properties`) carries over unchanged.
