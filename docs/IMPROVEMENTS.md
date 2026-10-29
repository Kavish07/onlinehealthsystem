# Future Improvements — OnlineHealthSystem

## Status
Draft — 2026-10-29

## Scope

This doc covers improvements **not** already owned by `docs/PRD-spring-boot-migration.md` (entity migration order, behavior parity) or the `role-view-jsp-consolidation` skill (numbered JSP de-duplication). It's scoped to backend code-health and deployability for a portfolio/demo project — not production hardening (no secrets vault, no compliance controls, no HA/scaling work).

## Sequencing

Start this work only after the migration PRD reaches its end state (zero references to `com.onlinehealthsystem`, `OnlineHealthSystemApplication` as sole entry point). Piling these changes onto the current mid-migration state compounds the transitional coupling already present (e.g. `PatientController` depending directly on legacy `DoctorDAO`/`TreatmentDAO`/`AppointmentDAO`).

## Items

### 1. Role modeling & auth cleanup
- Add a `Role` column/enum (`PATIENT` / `DOCTOR` / `ADMIN`) to the `Login` entity, replacing the convention of comparing a Doctor's username against the literal `"admin@gmail.com"`.
- Collapse the ~8 duplicated admin-check call sites (`PatientController`, `ConfirmAppointmentServlet`, `DoctorDeleteServlet`, `DoctorLoginServlet`, `DoctorRegisterServlet`, `ListDoctorsServlet`, `UpdateDoctorPasswordServlet`, `DoctorDAO`'s SQL) into one reusable role-check helper.
- Do **not** adopt Spring Security as part of this — see [ADR 0001](./adr/0001-defer-spring-security-centralize-role-checks.md).
- See `GLOSSARY.md` for the resolved `Role`/`Admin` terminology.

### 2. CI pipeline
- ~~Add a GitHub Actions workflow running `mvn test` on push/PR.~~ Done ahead of schedule: `.github/workflows/ci.yml` runs `./mvnw test` on push/PR to `master`. The project-local `.mvn/settings.xml` already bypasses the unreachable internal Nexus mirror, and Testcontainers (already a test dependency) runs fine against GitHub-hosted runners' built-in Docker — no additional CI-specific wiring needed for either. (`mvnw` is checked out without its executable bit since the repo was authored on Windows — the workflow `chmod +x`s it as a step rather than fixing the tracked file mode.)
- Still pending: add SpotBugs to the same pipeline, not a style linter (Checkstyle). This project's actual bug history (JDBC leaks, SQL injection risk, null-deref-on-missing-row — see the migration PRD's background) is exactly what a bug-pattern checker catches; a style linter would mostly bikeshed formatting.

### 3. Observability baseline
- There is currently zero logging anywhere in `src/main` — no `Logger`, no `System.out`, no `printStackTrace`; exceptions are wrapped and rethrown silently.
- Introduce SLF4J (already pulled in transitively by Spring Boot) at exception catch/rethrow sites across both the legacy DAO/Servlet code and the migrated Spring code. A full observability stack (metrics/tracing) is out of scope for this project's size.

### 4. Local deployment
- Add a `Dockerfile` and `docker-compose.yml` wiring the app to a MySQL container, so `docker compose up` replaces manual local MySQL setup.
- No live hosting target as part of this — that's a separate decision (provider, cost) outside a code-health review.
- Externalize DB credentials to environment variables as part of this work (`${DB_PASSWORD:root}`-style defaults), since docker-compose needs to inject them anyway — not full secrets management, just enough to stop hardcoding `root`/`root` in `application.properties`.

### 5. Dependency hygiene
- Remove the `com.efsavage.jquery:jquery-maven` dependency from `pom.xml` — it's dead weight; the jQuery actually served to the browser is the vendored `src/main/webapp/assets/js/jquery.min.js`, not anything this Maven artifact provides.
- Collapse the two JSTL implementations (`jakarta.servlet.jsp.jstl-api` and `org.glassfish.web:jakarta.servlet.jsp.jstl`) to one.

## Explicitly out of scope
- Spring Security adoption (see [ADR 0001](./adr/0001-defer-spring-security-centralize-role-checks.md)).
- REST API layer or JSP→Thymeleaf swap (see [ADR 0002](./adr/0002-keep-jsp-no-rest-api.md)).
- Database schema changes (already a non-goal in the migration PRD).
- Production-grade hardening: secrets management, HIPAA-style compliance controls, load balancing/HA.
