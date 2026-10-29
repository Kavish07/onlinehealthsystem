package com.onlinehealthsystem;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.MySQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Every DAO method under test hardcodes the connection URL
 * "jdbc:mysql://localhost:3306/onlinehealthsystem" with user/password
 * root/root (see CLAUDE.md), with no way to inject a different one. Rather
 * than changing that production code to make it testable, this base class
 * binds a disposable MySQL container to that exact host port/db/credentials
 * so the DAOs connect to it unmodified. It's started once (the singleton
 * container pattern) and reused across every test class in the run, since
 * each container's startup is too slow to repeat per class and the fixed
 * port binding means only one can run at a time anyway.
 */
public abstract class AbstractMySQLIntegrationTest {

    protected static final String JDBC_URL = "jdbc:mysql://localhost:3306/onlinehealthsystem";
    protected static final String DB_USER = "root";
    protected static final String DB_PASSWORD = "root";

    private static final MySQLContainer<?> MYSQL;

    static {
        @SuppressWarnings("resource")
        MySQLContainer<?> container = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("onlinehealthsystem")
                .withUsername(DB_USER)
                .withPassword(DB_PASSWORD)
                .withInitScript("schema.sql")
                .withCreateContainerCmdModifier(cmd -> cmd.getHostConfig().withPortBindings(
                        new PortBinding(Ports.Binding.bindPort(3306), new ExposedPort(3306))));
        container.start();
        MYSQL = container;
    }

    @BeforeEach
    void cleanDatabase() throws Exception {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");
            for (String table : new String[] {
                    "Treatments", "Bill", "Appointments", "LoginHistory",
                    "Doctors", "Patients", "Medicines", "Departments", "Login"
            }) {
                statement.execute("TRUNCATE TABLE " + table);
            }
            statement.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    /**
     * No DAO in this codebase inserts into Departments/Medicines (there's no
     * DepartmentDAO/MedicineDAO), yet several DAO queries join against them
     * with an implicit inner join - so tests that exercise those joins need
     * a matching lookup row seeded directly, the way a DBA would pre-populate
     * them in a real environment.
     */
    protected void seedDepartment(String code, String name) throws Exception {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             var statement = connection.prepareStatement(
                     "insert into Departments (DepartmentCode, DepartmentName) values (?, ?)")) {
            statement.setString(1, code);
            statement.setString(2, name);
            statement.executeUpdate();
        }
    }

    protected void seedMedicine(String code, String name) throws Exception {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             var statement = connection.prepareStatement(
                     "insert into Medicines (MedicineCode, MedicineName) values (?, ?)")) {
            statement.setString(1, code);
            statement.setString(2, name);
            statement.executeUpdate();
        }
    }
}
