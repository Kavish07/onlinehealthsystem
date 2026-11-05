package com.onlinehealthsystem;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

/**
 * Shared constants for the legacy {@code com.onlinehealthsystem} app.
 * {@code ADMIN_USERNAME} used to be copied as a literal into every
 * admin-gate check (7 servlets plus the now-deleted legacy
 * {@code DoctorDAO.listDoctors()}); one copy means a future change to it
 * can't drift out of sync in just some of those spots.
 *
 * <p>Public (rather than package-private) so the Spring Boot migration's
 * {@code com.system.OnlineHealthSystem} code can reference the same single
 * constant for its own admin-role check instead of copying the literal into
 * yet another spot - see {@code DoctorService}/{@code DoctorController}.
 */
public final class AppConstants {

    public static final String ADMIN_USERNAME = "admin@gmail.com";

    public static final LocalTime CLINIC_OPENING_TIME = LocalTime.of(9, 0);
    public static final LocalTime CLINIC_CLOSING_TIME = LocalTime.of(17, 0);

    // shortcut: fixed-date Mauritius public holidays only, for 2026.
    // Lunar/variable-date holidays (Thaipoosam Cavadee, Maha Shivaratri,
    // Ugadi, Eid, Ganesh Chaturthi, Diwali, Chinese Spring Festival) aren't
    // included and this set needs a manual update every year - upgrade to a
    // maintained holiday-calendar source if that ever becomes worth the cost.
    public static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 2),
            LocalDate.of(2026, 2, 1),
            LocalDate.of(2026, 3, 12),
            LocalDate.of(2026, 5, 1),
            LocalDate.of(2026, 8, 15),
            LocalDate.of(2026, 11, 1),
            LocalDate.of(2026, 11, 2),
            LocalDate.of(2026, 12, 25));

    private AppConstants() {
    }
}
