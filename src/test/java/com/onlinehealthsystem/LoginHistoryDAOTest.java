package com.onlinehealthsystem;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginHistoryDAOTest extends AbstractMySQLIntegrationTest {

    private final LoginHistoryDAO loginHistoryDAO = new LoginHistoryDAO();

    private int insertLoginRow() throws Exception {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(
                     "insert into Login (UserName, UserPassword, Salt) values (?, ?, ?)")) {
            statement.setString(1, "jane.doe");
            statement.setString(2, "hash");
            statement.setString(3, "salt");
            statement.executeUpdate();
        }
        try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(
                     "select LoginID from Login where UserName = ?")) {
            statement.setString(1, "jane.doe");
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                return result.getInt("LoginID");
            }
        }
    }

    @Test
    void registerLogin_insertsAnOpenLoginHistoryRow() throws Exception {
        int loginId = insertLoginRow();

        loginHistoryDAO.registerLogin(loginId, LocalDateTime.now());

        assertEquals(1, countOpenSessions(loginId));
    }

    @Test
    void registerLogout_closesTheOpenLoginHistoryRow() throws Exception {
        // Regression test: registerLogout's SQL referenced a non-existent "Login"
        // column (instead of "LoginID") and never bound its second parameter, so
        // calling it threw a JDBC parameter-index error before this was fixed.
        int loginId = insertLoginRow();
        loginHistoryDAO.registerLogin(loginId, LocalDateTime.now());

        loginHistoryDAO.registerLogout(loginId, LocalDateTime.now());

        assertEquals(0, countOpenSessions(loginId));
    }

    @Test
    void registerLogout_onlyClosesTheGivenLoginIDsSession() throws Exception {
        int loginId1 = insertLoginRow();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(
                     "insert into Login (UserName, UserPassword, Salt) values (?, ?, ?)")) {
            statement.setString(1, "john.smith");
            statement.setString(2, "hash2");
            statement.setString(3, "salt2");
            statement.executeUpdate();
        }
        int loginId2;
        try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(
                     "select LoginID from Login where UserName = ?")) {
            statement.setString(1, "john.smith");
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                loginId2 = result.getInt("LoginID");
            }
        }
        loginHistoryDAO.registerLogin(loginId1, LocalDateTime.now());
        loginHistoryDAO.registerLogin(loginId2, LocalDateTime.now());

        loginHistoryDAO.registerLogout(loginId1, LocalDateTime.now());

        assertEquals(0, countOpenSessions(loginId1));
        assertEquals(1, countOpenSessions(loginId2));
    }

    private int countOpenSessions(int loginId) throws Exception {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(
                     "select count(*) as openSessions from LoginHistory where LoginID = ? and LogoutDateTime is null")) {
            statement.setInt(1, loginId);
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                return result.getInt("openSessions");
            }
        }
    }
}
