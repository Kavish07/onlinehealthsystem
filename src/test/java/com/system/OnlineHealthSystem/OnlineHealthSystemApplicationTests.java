package com.system.OnlineHealthSystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test: the Spring application context must load successfully with
 * the real datasource configuration in application.properties. Requires a
 * reachable MySQL instance at the configured URL (same database the legacy
 * app uses) since spring.jpa.hibernate.ddl-auto=validate needs a live schema
 * to validate mapped entities against.
 */
@SpringBootTest
class OnlineHealthSystemApplicationTests {

    @Test
    void contextLoads() {
    }
}
