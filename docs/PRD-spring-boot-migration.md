# PRD: Spring Boot Migration for OnlineHealthSystem

## Status
Draft — 2026-10-29

## Background

`com.onlinehealthsystem` is the live legacy app: plain `HttpServlet`s + JSP views + raw JDBC against MySQL, no Spring, no ORM, no DI. A separate, half-finished Spring Boot entry point (`com.system.OnlineHealthSystem.OnlineHealthSystemApplication`, `@SpringBootApplication`) was scaffolded alongside it but never wired up — `pom.xml` didn't declare the `spring-boot-starter*` dependencies the entry point and its `@SpringBootTest` needed, so neither compiled.

Recent work already in this branch has:
- Fixed security/correctness bugs in the legacy DAO/Servlet layer (login NPE, authorization checks, JDBC connection leaks, password-timing, logout identity trust, dead/broken DAO methods).
- Added a Maven-default source layout and project-local Maven settings to bypass an unreachable internal Nexus mirror.
- Added a first test suite under `src/test/java/com/onlinehealthsystem` (DAO, Servlet, and utility tests) plus `src/test/resources/schema.sql`, giving the legacy code a safety net it previously lacked.
- Removed the broken `OnlineHealthSystemApplication` scaffold and its test, clearing the way to rebuild the Spring Boot entry point correctly instead of patching a broken one.

This PRD defines the plan to migrate the legacy app to Spring Boot + Spring Data JPA incrementally, one entity/feature "vertical slice" at a time, without a big-bang rewrite and without regressing existing behavior.

## Goals

1. Stand up a working Spring Boot application (correct dependencies, context loads, app starts) as the foundation for migration.
2. Migrate each entity's full stack — model → DAO → Servlet → JSP wiring — to Spring equivalents — `@Entity` → `JpaRepository` → `@Service` → `@Controller` — one entity at a time, in a working state at every step.
3. Preserve all current user-facing behavior for patients and doctors (login, appointments, treatments, password change, logout) through the migration; no feature regressions.
4. Use each slice as an opportunity to fix any remaining known legacy bugs in the code being replaced (e.g. unclosed JDBC resources, non-constant-time password comparisons) rather than carrying them into the new code.
5. Build up automated test coverage (JUnit 5 + Mockito + Spring test slices) for every migrated slice, test-first.
6. End state: the legacy `com.onlinehealthsystem` Servlet/DAO code and duplicate JSP variants are fully removed, with Spring Boot as the only runtime path.

## Non-goals

- No change to the database schema (table/column names) during migration — JPA entities map to the existing schema as-is. Schema evolution is a separate, later effort.
- No UI redesign. JSP views keep their current look/behavior; only the controller layer serving them changes. (Consolidating the numbered JSP variants is tracked separately via `role-view-jsp-consolidation` and is not required by this PRD, though a slice may coincidentally simplify a view it touches.)
- No authentication/authorization model change beyond what's needed to preserve current session-based (`patient`/`doctor` session attribute) behavior.
- No deployment/infra changes (this PRD is app-code scope only).

## Scope — entities to migrate

In order, matching the existing DAO/model inventory:

1. **Patient** (`PatientDAO`, patient-facing servlets: login, registration, delete, password change)
2. **Doctor** (`DoctorDAO`, doctor-facing servlets)
3. **Appointment** (`AppointmentDAO`, booking/confirmation/cancellation servlets)
4. **Treatment** (`TreatmentDAO`, treatment-record servlets)
5. **LoginHistory** (`LoginHistoryDAO`, audit logging tied into login flows)

Ordering follows dependency direction (appointments/treatments reference patients and doctors), so foundational entities migrate first.

## Approach

Each slice follows the checklist already codified in the `migration-slice-runner` agent and `spring-boot-migration` / `spring-boot-tdd` skills:

1. **Entity** — convert the POJO to a JPA `@Entity`, mapping existing table/column names (no schema change).
2. **Repository** — replace the hand-written DAO with a `JpaRepository`; move custom queries to derived methods or `@Query`.
3. **Service** — re-home business logic that doesn't belong in the repository (password hashing, multi-step operations currently inlined in DAO methods).
4. **Controller** — replace the matching Servlet(s); initially return the same JSP/view name so page behavior is unchanged mid-slice.
5. **Tests** — write repository/service/controller tests test-first (red before green).
6. **Cleanup** — delete the old DAO and Servlet(s) for that entity only after the new path is live and its tests pass. Old and new paths are never left mapped to overlapping behavior simultaneously.

### Prerequisite: fix the build

Before any entity slice starts: add the missing `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-test` (and Jakarta/JSP view support, e.g. `spring-boot-starter-tomcat` + JSTL) dependencies to `pom.xml`, and rebuild `OnlineHealthSystemApplication` so the app actually starts against the existing MySQL schema. This unblocks every subsequent slice.

## Success criteria

- `mvn spring-boot:run` (or equivalent) starts the app successfully at every slice boundary — the app is never left in a broken, half-migrated state between slices.
- For each migrated entity: old DAO and Servlet classes are deleted; no `@WebServlet`/`web.xml` entry remains for that entity's replaced actions; a `JpaRepository`-backed path serves the same URLs with the same session/JSP behavior.
- Test coverage exists for every migrated repository, service, and controller, added test-first.
- No regression in patient/doctor login, appointment booking/confirmation, treatment records, password change, or logout — verified per-slice.
- Known legacy bugs in migrated code (JDBC leaks, password timing, null-deref-on-missing-row, SQL injection risk) are not present in the replacement code.
- Final state: zero references to `com.onlinehealthsystem` package remain; `OnlineHealthSystemApplication` is the sole entry point.

## Open questions

- Should doctor/patient session handling move to Spring Security during this migration, or stay as plain `HttpSession` attributes until a later PRD? (Default assumption: keep session attributes as-is per slice; revisit security framework adoption separately.)
- Target JSP rendering (`spring-boot-starter-tomcat` + JSP support) vs. a view-layer rewrite (e.g. Thymeleaf) — this PRD assumes JSPs are kept as-is and only the controller layer changes, deferring any view-technology change to a future PRD.
