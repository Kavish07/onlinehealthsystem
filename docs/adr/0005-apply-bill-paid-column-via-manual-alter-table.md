# Add `Bill.Paid` to production with a one-time manual `ALTER TABLE`, not a migration tool

Status: accepted

The Billing feature adds a boolean `Paid` column to `Bill`. You'd expect editing `src/test/resources/schema.sql` to be enough, but it isn't. That file is the only schema in the repo. It is both the test fixture and the production bootstrap script (`Dockerfile` copies it to `/app/schema.sql`), and `docker-entrypoint.sh` runs it only on a brand-new Postgres data volume, guarded by a `$DATADIR/.initialized` marker. The live Fly.io volume ([0003](./0003-colocate-app-and-mysql-for-deployment.md), [0004](./0004-migrate-database-to-postgresql.md)) was initialized long ago, so a `schema.sql` edit never reaches the running database. We're adding the column to production by hand, once, against the Fly.io Postgres instance: `ALTER TABLE Bill ADD COLUMN Paid boolean DEFAULT false;`, and adding the same column to `schema.sql` so fresh volumes and tests match.

## Considered options
- **Introduce Flyway (or similar) for schema migrations**: rejected. It's too much tooling for one column on one table at this project's demo/portfolio scale, and `docs/IMPROVEMENTS.md` explicitly leaves production hardening out of scope. Revisit if schema changes to live data start happening regularly.
- **Wipe and recreate the Fly volume so `schema.sql` re-runs fresh**: rejected. It would destroy the existing demo data (patients, doctors, appointments, treatments, login history).

## Consequences
- **Run the `ALTER TABLE` before deploying the build that maps `Paid`.** `application.properties` sets `spring.jpa.hibernate.ddl-auto=validate`, so if this step is skipped the app **fails to start** in production with a schema-validation mismatch. It doesn't just misbehave at runtime. The column has a default, so running it early is harmless to the currently deployed build.
- **Leave the column name unquoted.** Every other identifier in `schema.sql` is unquoted, so PostgreSQL folds it to lowercase, and Hibernate isn't set to quote identifiers. A quoted `"Paid"` would create a case-sensitive column that validation can't find, which causes the same startup failure.
- Production schema state is no longer fully reproducible from the repo alone. Any later column change to live data needs the same manual step until a migration tool is adopted.
