package com.onlinehealthsystem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordEncoderTest {

    @Test
    void verifyUserPassword_succeeds_forCorrectPasswordAndSalt() {
        String salt = PasswordEncoder.getSaltvalue(30);
        String hash = PasswordEncoder.generateSecurePassword("correct-password", salt);

        assertTrue(PasswordEncoder.verifyUserPassword("correct-password", hash, salt));
    }

    @Test
    void verifyUserPassword_fails_forWrongPassword() {
        String salt = PasswordEncoder.getSaltvalue(30);
        String hash = PasswordEncoder.generateSecurePassword("correct-password", salt);

        assertFalse(PasswordEncoder.verifyUserPassword("wrong-password", hash, salt));
    }

    @Test
    void verifyUserPassword_fails_forWrongSalt() {
        String hash = PasswordEncoder.generateSecurePassword("correct-password", PasswordEncoder.getSaltvalue(30));

        assertFalse(PasswordEncoder.verifyUserPassword("correct-password", hash, PasswordEncoder.getSaltvalue(30)));
    }

    @Test
    void verifyUserPassword_isCaseSensitive() {
        // Regression test: the comparison used to be equalsIgnoreCase, which could
        // accept a hash that differs from the real one only by letter case.
        String salt = PasswordEncoder.getSaltvalue(30);
        String hash = PasswordEncoder.generateSecurePassword("correct-password", salt);
        String flippedCaseHash = hash.equals(hash.toUpperCase()) ? hash.toLowerCase() : hash.toUpperCase();

        assertNotEquals(hash, flippedCaseHash, "precondition: flipping case must actually change the string");
        assertFalse(PasswordEncoder.verifyUserPassword("correct-password", flippedCaseHash, salt));
    }

    @Test
    void verifyUserPassword_failsClosed_whenStoredHashIsNotValidBase64() {
        // Regression test: a malformed/corrupted UserPassword column must not throw
        // out of verifyUserPassword - it must behave like any other mismatch.
        String salt = PasswordEncoder.getSaltvalue(30);

        assertFalse(PasswordEncoder.verifyUserPassword("any-password", "not-valid-base64-!!!", salt));
    }

    @Test
    void generateSecurePassword_isDeterministic_forSameInputAndSalt() {
        String salt = "fixed-salt-value";

        String first = PasswordEncoder.generateSecurePassword("some-password", salt);
        String second = PasswordEncoder.generateSecurePassword("some-password", salt);

        assertTrue(first.equals(second));
    }

    @Test
    void getSaltvalue_producesRequestedLength() {
        String salt = PasswordEncoder.getSaltvalue(30);

        assertTrue(salt.length() == 30);
    }

    @Test
    void getSaltvalue_producesDifferentValuesAcrossCalls() {
        String first = PasswordEncoder.getSaltvalue(30);
        String second = PasswordEncoder.getSaltvalue(30);

        assertNotEquals(first, second);
    }
}
