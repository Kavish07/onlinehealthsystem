package com.onlinehealthsystem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsernameValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "abcde",            // minimum length (5), alphanumeric only
            "john.doe",         // single separator surrounded by alphanumerics
            "john-doe",
            "john_doe",
            "User123",
            "a2345678901234567z" // maximum length (20)
    })
    void isValid_acceptsWellFormedUsernames(String username) {
        assertTrue(UsernameValidator.isValid(username));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ab",                   // too short (< 5)
            "a234567890123456789z", // too long (> 20)
            ".john",                // starts with a separator
            "john.",                // ends with a separator
            "john..doe",            // consecutive separators
            "john doe",             // space not allowed
            "john@doe.com",         // '@' not allowed
            ""                      // empty
    })
    void isValid_rejectsMalformedUsernames(String username) {
        assertFalse(UsernameValidator.isValid(username));
    }

    @Test
    void isValid_throwsOnNull() {
        // Documents actual behavior: Pattern.matcher(null) throws rather than
        // isValid returning false, so callers must not pass null unchecked.
        assertThrows(NullPointerException.class, () -> UsernameValidator.isValid(null));
    }
}
