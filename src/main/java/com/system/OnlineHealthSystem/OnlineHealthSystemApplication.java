package com.system.OnlineHealthSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Sole entry point for OnlineHealthSystem, now that every entity/feature
 * (Patient, Doctor, Appointment, Treatment, LoginHistory) has been migrated
 * off the legacy {@code com.onlinehealthsystem} Servlet/JDBC app onto
 * {@code @Entity}/{@code @Repository}/{@code @Service}/{@code @Controller}
 * beans under this package, auto-detected via component scanning.
 *
 * <p>The legacy package's remaining classes
 * ({@code com.onlinehealthsystem.Patient}/{@code Doctor}/{@code Appointment}
 * /{@code Treatment}, {@code PasswordEncoder}, {@code AppConstants}) are
 * plain POJOs/utility classes still referenced by this package's
 * services/controllers (e.g. for session attributes and password hashing);
 * none of them are {@code @WebServlet}s, so there is nothing left for
 * {@code @ServletComponentScan} to register, and it has been removed.
 */
@SpringBootApplication
public class OnlineHealthSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineHealthSystemApplication.class, args);
    }
}
