# Builds and runs OnlineHealthSystem as a single container with MySQL
# co-located alongside the app. Required because the legacy com.onlinehealthsystem
# DAOs hardcode jdbc:mysql://localhost:3306 with no way to point them
# elsewhere (see CLAUDE.md) - app and database must share one network
# namespace, so they run as two processes in the same container rather than
# as separate services.

# ---- Build stage ----
FROM maven:3.8.6-eclipse-temurin-17 AS build
WORKDIR /build
COPY . .
RUN ./mvnw -B -q package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y mysql-server \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /build/target/OnlineHealthSystem-0.0.1-SNAPSHOT.war /app/app.war
# Reused from the Testcontainers-backed integration tests: a reverse-engineered
# schema matching what the DAOs' SQL depends on (no original schema file
# exists in this repo - see the file's own header comment for caveats).
COPY src/test/resources/schema.sql /app/schema.sql
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh \
    && mkdir -p /var/lib/mysql \
    && chown -R mysql:mysql /var/lib/mysql

EXPOSE 8080
ENTRYPOINT ["/app/docker-entrypoint.sh"]
