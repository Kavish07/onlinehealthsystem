# Defer Spring Security; centralize session-based role checks instead

Status: proposed

The app is migrating to Spring Boot, which makes Spring Security the obvious next step for auth — but adopting it mid-migration would mean re-deriving every existing role check (patient/doctor session attributes, the ad hoc admin check) against a new framework before the entity migration itself is even done. We're keeping plain `HttpSession` attributes for now, collapsing the ~8 duplicated `username.equals("admin@gmail.com")` checks into one `Role`-aware helper instead, and revisiting Spring Security once the migration finishes and `Role` is a real column on `Login`.

## Considered options
- **Full Spring Security adoption now**: rejected — the biggest behavior-parity risk of anything in this review, and `docs/PRD-spring-boot-migration.md`'s open questions already explicitly deferred it.
- **Do nothing**: rejected — the duplicated admin-check literal across 8+ call sites (legacy and migrated code alike) is exactly the kind of inconsistency that causes a real authorization bug.
