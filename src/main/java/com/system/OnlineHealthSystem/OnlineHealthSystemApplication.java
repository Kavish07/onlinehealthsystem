package com.system.OnlineHealthSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the incremental Spring Boot migration of OnlineHealthSystem.
 * Lives alongside the legacy {@code com.onlinehealthsystem} Servlet/JDBC app
 * (still the active runtime for any entity/feature not yet migrated) and is
 * intentionally a thin, minimal scaffold: migrated slices register their own
 * {@code @Entity}/{@code @Repository}/{@code @Service}/{@code @Controller}
 * beans, auto-detected via component scanning under this package.
 */
@SpringBootApplication
public class OnlineHealthSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineHealthSystemApplication.class, args);
    }
}
