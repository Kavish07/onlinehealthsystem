# Co-locate app and MySQL in one container instead of externalizing the JDBC URL

Status: proposed

Deploying this app anywhere beyond a developer's own machine raises an obvious question: why not point it at a managed MySQL database over the network, the normal PaaS pattern? The legacy `com.onlinehealthsystem` DAOs hardcode `jdbc:mysql://localhost:3306` with `root`/`root`, with no way to inject a different host — CLAUDE.md flags this as a known, deliberate pattern, not something to silently fix in an unrelated change. Rather than editing every legacy DAO to read a configurable JDBC URL, both the local Docker image and the Fly.io deployment run `mysqld` and the Spring Boot app as two processes inside one container, so "localhost" resolves correctly without touching the legacy code.

## Considered options
- **Externalize the JDBC URL to an env var**: rejected for now — touches every legacy DAO for a deployment concern unrelated to the entity migration in progress; revisit once the migration finishes and DAOs are gone anyway.
- **Separate app + MySQL containers via docker-compose/Fly, networked normally**: rejected — "localhost" wouldn't resolve across containers without a `network_mode: service:db`-style hack, which is more complex than co-locating the two processes directly.

## Consequences
- The schema used to initialize a fresh deployment is `src/test/resources/schema.sql` — a reverse-engineered reconstruction built for the Testcontainers-backed integration tests, not the original production schema (none exists in this repo). Good enough for a demo deploy; worth revisiting if a real schema file ever surfaces.
- MySQL data lives on a single Fly volume tied to one machine; this does not scale beyond a single instance and isn't a backup strategy.
