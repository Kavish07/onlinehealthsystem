package com.system.OnlineHealthSystem.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegistrationValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"jane.doe", "john.smith", "User123", "abcde"})
    void validateUsername_acceptsWellFormedUsernames(String userName) {
        assertDoesNotThrow(() -> RegistrationValidator.validateUsername(userName));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ab",                   // too short
            "john doe",             // space not allowed
            "john@doe.com",         // '@' not allowed
            "\"><script>alert(1)</script>",
            ""
    })
    void validateUsername_rejectsMalformedOrDangerousUsernames(String userName) {
        assertThrows(IllegalArgumentException.class, () -> RegistrationValidator.validateUsername(userName));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Jane", "O'Brien", "Jean-Luc", "Doe"})
    void validateName_acceptsWellFormedNames(String name) {
        assertDoesNotThrow(() -> RegistrationValidator.validateName("First name", name));
    }

    @Test
    void validateName_rejectsTheConfirmedAdminJspXssPayload() {
        // Reproduces the exact firstname payload from the audit's confirmed
        // finding (stored-xss.admin-jsp.patient-registration-fields):
        // breaking out of admin.jsp's value="..." attribute via a double
        // quote and injecting a live <script> element.
        String payload = "a\" disabled><script>alert(document.domain)</script><input type=\"hidden\" value=\"";
        assertThrows(IllegalArgumentException.class, () -> RegistrationValidator.validateName("First name", payload));
    }

    @Test
    void validateNationalId_rejectsDangerousCharacters() {
        assertThrows(IllegalArgumentException.class,
                () -> RegistrationValidator.validateNationalId("N1\"><script>alert(1)</script>"));
    }

    @Test
    void validateSex_rejectsDangerousCharacters() {
        assertThrows(IllegalArgumentException.class,
                () -> RegistrationValidator.validateSex("F\"><script>alert(1)</script>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1 Main St", "Springfield", "Apt #4, 2nd Floor"})
    void validateFreeText_acceptsOrdinaryAddressesAndCities(String value) {
        assertDoesNotThrow(() -> RegistrationValidator.validateFreeText("Address", value));
    }

    @Test
    void validateFreeText_rejectsDangerousCharacters() {
        assertThrows(IllegalArgumentException.class,
                () -> RegistrationValidator.validateFreeText("Address", "1 Main St\"><script>alert(1)</script>"));
    }
}
