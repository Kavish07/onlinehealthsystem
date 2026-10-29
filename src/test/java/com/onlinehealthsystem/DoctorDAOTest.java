package com.onlinehealthsystem;

import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DoctorDAOTest extends AbstractMySQLIntegrationTest {

    private final DoctorDAO doctorDAO = new DoctorDAO();

    private Doctor registerSampleDoctor(String userName, String password) throws Exception {
        seedDepartment("CARD", "Cardiology");
        return doctorDAO.registerDoctor(userName, password, "John", "Smith", "N98765",
                Date.valueOf("1980-05-05"), "M", "2 Main St", "Springfield", 80, 180,
                "MD", "Cardiology", "CARD");
    }

    @Test
    void registerDoctor_thenCheckLogin_succeedsWithCorrectPassword() throws Exception {
        registerSampleDoctor("john.smith", "s3cret-pw");

        Doctor loggedIn = doctorDAO.checkLogin("john.smith", "s3cret-pw", LocalDateTime.now());

        assertNotNull(loggedIn);
        assertEquals("john.smith", loggedIn.getUserName());
    }

    @Test
    void checkLogin_returnsNull_whenUserNameDoesNotExist() throws Exception {
        // Regression test: used to throw an NPE instead of returning null.
        Doctor result = doctorDAO.checkLogin("nobody@example.com", "whatever", LocalDateTime.now());

        assertNull(result);
    }

    @Test
    void checkLogin_returnsNull_whenPasswordIsWrong() throws Exception {
        registerSampleDoctor("john.smith", "s3cret-pw");

        Doctor result = doctorDAO.checkLogin("john.smith", "wrong-password", LocalDateTime.now());

        assertNull(result);
    }

    @Test
    void checkRegistration_returnsNull_whenUserNameNotRegistered() throws Exception {
        assertNull(doctorDAO.checkRegistration("nobody@example.com"));
    }

    @Test
    void deleteDoctor_removesLoginRow_andReturnsNonNullResult() throws Exception {
        // Regression test: used to always return null even after a successful delete.
        Doctor registered = registerSampleDoctor("john.smith", "s3cret-pw");

        Doctor deleted = doctorDAO.deleteDoctor(registered.getLoginID());

        assertNotNull(deleted);
        assertEquals(registered.getLoginID(), deleted.getLoginID());
        assertNull(doctorDAO.checkRegistration("john.smith"));
    }

    @Test
    void updatePassword_thenCheckLogin_succeedsWithNewPassword() throws Exception {
        registerSampleDoctor("john.smith", "old-password");

        doctorDAO.updatePassword("john.smith", "new-password");

        assertNotNull(doctorDAO.checkLogin("john.smith", "new-password", LocalDateTime.now()));
        assertNull(doctorDAO.checkLogin("john.smith", "old-password", LocalDateTime.now()));
    }

    @Test
    void listDoctors_includesRegisteredDoctors_butExcludesAdmin() throws Exception {
        registerSampleDoctor("john.smith", "pw1");
        seedDepartment("DERM", "Dermatology");
        doctorDAO.registerDoctor("admin@gmail.com", "admin-pw", "Admin", "User", "N00000",
                Date.valueOf("1970-01-01"), "M", "HQ", "Springfield", 70, 175,
                "N/A", "N/A", "DERM");

        var doctors = doctorDAO.listDoctors();

        assertEquals(1, doctors.size());
        assertTrue(doctors.stream().anyMatch(d -> "john.smith".equals(d.getUserName())));
        assertTrue(doctors.stream().noneMatch(d -> "admin@gmail.com".equals(d.getUserName())));
    }

    @Test
    void registerLogout_closesTheOpenLoginHistoryRow() throws Exception {
        Doctor registered = registerSampleDoctor("john.smith", "s3cret-pw");
        doctorDAO.checkLogin("john.smith", "s3cret-pw", LocalDateTime.now());

        doctorDAO.registerLogout(registered.getLoginID(), LocalDateTime.now());

        try (var connection = java.sql.DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             var statement = connection.prepareStatement(
                     "select count(*) as openSessions from LoginHistory where LoginID = ? and LogoutDateTime is null")) {
            statement.setInt(1, registered.getLoginID());
            try (var result = statement.executeQuery()) {
                assertTrue(result.next());
                assertEquals(0, result.getInt("openSessions"));
            }
        }
    }
}
