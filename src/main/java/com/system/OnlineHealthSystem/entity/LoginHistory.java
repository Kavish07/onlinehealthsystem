package com.system.OnlineHealthSystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Maps the {@code LoginHistory} table. Created during the Patient slice
 * (ahead of the dedicated LoginHistory slice) because it was a prerequisite
 * for that one: {@code checkLogin}/{@code registerLogout}'s behavior - an
 * inserted row on login, a stamped {@code LogoutDateTime} on logout - had
 * to be preserved as part of the Patient/Doctor login/logout flows.
 *
 * <p>The LoginHistory slice itself turned out to be pure cleanup: this
 * entity, {@link com.system.OnlineHealthSystem.repository.LoginHistoryRepository},
 * and the {@code PatientService}/{@code DoctorService} callers were already
 * fully wired up by the time that slice ran. {@code LoginHistoryDAO} (and
 * the legacy {@code com.onlinehealthsystem.LoginHistory} model, which had no
 * remaining callers) were deleted then, with no new
 * service/controller/reporting layer needed - LoginHistory was always an
 * internal audit mechanism, never its own user-facing endpoint.
 */
@Entity
@Table(name = "LoginHistory")
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LoginHistoryID")
    private Integer loginHistoryId;

    @Column(name = "LoginID", nullable = false)
    private Integer loginId;

    @Column(name = "LoginDateTime")
    private LocalDateTime loginDateTime;

    @Column(name = "LogoutDateTime")
    private LocalDateTime logoutDateTime;

    public Integer getLoginHistoryId() {
        return loginHistoryId;
    }

    public void setLoginHistoryId(Integer loginHistoryId) {
        this.loginHistoryId = loginHistoryId;
    }

    public Integer getLoginId() {
        return loginId;
    }

    public void setLoginId(Integer loginId) {
        this.loginId = loginId;
    }

    public LocalDateTime getLoginDateTime() {
        return loginDateTime;
    }

    public void setLoginDateTime(LocalDateTime loginDateTime) {
        this.loginDateTime = loginDateTime;
    }

    public LocalDateTime getLogoutDateTime() {
        return logoutDateTime;
    }

    public void setLogoutDateTime(LocalDateTime logoutDateTime) {
        this.logoutDateTime = logoutDateTime;
    }
}
