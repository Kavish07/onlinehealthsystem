package com.system.OnlineHealthSystem.service;

import java.util.regex.Pattern;

/**
 * Server-side format validation for patient/doctor self-registration fields,
 * shared by {@link PatientService#register} and {@link DoctorService#register}.
 *
 * <p>Replaces the legacy {@code com.onlinehealthsystem.UsernameValidator},
 * which was deleted during the Spring Boot migration and never replaced -
 * and which, even before deletion, was never actually called from any
 * Servlet (confirmed via git history: it existed only as a tested-but-unused
 * class since the very first commit). So no registration field has ever
 * actually been validated at the point of entry. Reinstated here - with the
 * username pattern kept as the legacy class defined it (it already accepts
 * the dot-separated usernames this app actually uses, e.g. "jane.doe") and
 * new patterns added for the other free-text fields - after a security
 * review found that unvalidated fields render unescaped in admin.jsp,
 * allowing stored XSS via patient self-registration.
 */
final class RegistrationValidator {

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9]([._-](?![._-])|[a-zA-Z0-9]){3,18}[a-zA-Z0-9]$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z '-]{0,49}$");
    private static final Pattern NATIONAL_ID_PATTERN = Pattern.compile("^[A-Za-z0-9-]{1,20}$");
    private static final Pattern SEX_PATTERN = Pattern.compile("^[A-Za-z -]{1,20}$");
    private static final Pattern FREE_TEXT_PATTERN = Pattern.compile("^[^<>\"]{1,100}$");

    private RegistrationValidator() {
    }

    static void validateUsername(String userName) {
        require(userName != null && USERNAME_PATTERN.matcher(userName).matches(),
                "Username must be 5-20 characters: letters/digits with optional single . _ - separators");
    }

    static void validateName(String label, String value) {
        require(value != null && NAME_PATTERN.matcher(value).matches(),
                label + " must contain only letters, spaces, hyphens, or apostrophes");
    }

    static void validateNationalId(String value) {
        require(value != null && NATIONAL_ID_PATTERN.matcher(value).matches(),
                "National ID must be 1-20 letters, digits, or hyphens");
    }

    static void validateSex(String value) {
        require(value != null && SEX_PATTERN.matcher(value).matches(),
                "Sex must contain only letters, spaces, or hyphens");
    }

    static void validateFreeText(String label, String value) {
        require(value != null && FREE_TEXT_PATTERN.matcher(value).matches(),
                label + " must not be blank, over 100 characters, or contain < > \"");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
