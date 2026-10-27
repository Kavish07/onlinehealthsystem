# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A legacy Java web app (`com.onlinehealthsystem`) for managing patients, doctors, appointments, and treatments: plain `HttpServlet`s + JSP views + raw JDBC (MySQL), no Spring, no ORM, no DI. It's an Eclipse Dynamic Web Project (`.project`/`.classpath`/`.settings` present) that was later wrapped in Maven.

A **separate, half-finished Spring Boot migration** also lives in the tree: `src/main/java/com/system/OnlineHealthSystem/OnlineHealthSystemApplication.java` (`@SpringBootApplication`) and its matching `@SpringBootTest` in `src/test`. `pom.xml` currently declares none of the `spring-boot-starter*` / JUnit Jupiter dependencies these need, so **this entry point and test do not compile as-is**. Don't assume Spring Boot is wired up; if asked to work on it, use the `spring-boot-migration` skill, which exists specifically to fix this broken scaffold incrementally.

## Build notes

`pom.xml` uses Maven's default source layout (`src/main/java`, `src/main/webapp`) — it no longer overrides `sourceDirectory`/`warSourceDirectory` to the stale Eclipse-era `src`/`WebContent` paths, which didn't exist on disk.

Dependency/plugin resolution may fail with connect-timeout errors against an internal Nexus mirror (`10.0.1.10:8081`) if that host isn't reachable from the current environment — that's a repository-connectivity issue in local Maven settings, not a `pom.xml` bug.

There is no linter or test suite exercising the legacy `com.onlinehealthsystem` code (no test files target it). The only test present is the `@SpringBootTest` context-load test for the unfinished Spring Boot entry point, which won't run until that migration's dependencies are added.

## Architecture (legacy app, `com.onlinehealthsystem`)

- **Model**: plain data classes (`Patient`, `Doctor`, `Appointment`, `Treatment`, `LoginHistory`).
- **DAO**: one `*DAO` class per entity (`PatientDAO`, `DoctorDAO`, `AppointmentDAO`, `TreatmentDAO`, `LoginHistoryDAO`). Every method opens its own `Connection` via `DriverManager.getConnection(...)` with a hardcoded local MySQL URL/credentials (`jdbc:mysql://localhost:3306/onlinehealthsystem`, `root`/`root`), runs one or more `PreparedStatement`s, and closes the connection manually at the end of the method (not try-with-resources, and not closed on exception paths — this is a known pattern, not something to silently "fix" in unrelated changes).
- **Servlet**: one `HttpServlet` per action, registered both via `@WebServlet("/<ClassName>")` annotation and redundantly in `src/main/webapp/WEB-INF/web.xml`. Keep both in sync when adding a servlet. Pattern: read `request.getParameter(...)`, call into one or more DAOs, wrap `SQLException`/`ClassNotFoundException` into `ServletException`, set session attributes for the logged-in entity (`patient` or `doctor`) and any lists the destination JSP needs, then `RequestDispatcher.forward(...)` to a JSP.
- **Auth**: `PasswordEncoder` does PBKDF2WithHmacSHA512 salted hashing; `UsernameValidator` enforces a username regex. Role/session state is tracked via the `patient`/`doctor` session attributes set at login — servlets and JSPs gate behavior on these, not on request parameters.
- **Views (JSP)**: several page *families* exist in numbered variants for different roles/states, e.g. `appointments.jsp` / `appointments2.jsp` / `appointments3.jsp`, `treatments.jsp` / `2` / `3`, `changepwd.jsp` / `2` / `3`, `home.jsp` / `2`, `login.jsp` / `2`. These are near-duplicate hand-copies, not a templating mechanism — when adding a new role-specific view, consult the `role-view-jsp-consolidation` skill rather than hand-copying another variant.

## Project-specific skills and agents

This repo has Claude Code skills/agents already set up for its known conventions and known issues — use them instead of re-deriving the patterns from scratch:
- `legacy-dao-servlet-pattern` skill / `dao-servlet-builder` agent — for writing new DAO methods or Servlets matching existing conventions.
- `jdbc-security-review` skill / `legacy-code-reviewer` agent — for reviewing DAO/Servlet/password-handling changes (SQL injection, unclosed JDBC resources, null-deref after `result.next()`, non-constant-time password comparison, session vs. request-parameter role checks).
- `role-view-jsp-consolidation` skill — for merging or adding to the numbered JSP variants described above without changing per-role behavior.
- `spring-boot-migration` skill — for incrementally migrating this app to Spring Boot/Spring Data JPA, starting from fixing `OnlineHealthSystemApplication`.
- `spring-boot-tdd` skill — TDD loop (JUnit 5/Mockito/Spring test slices) once Spring Boot code exists.
