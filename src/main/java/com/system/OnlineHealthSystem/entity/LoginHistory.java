package com.system.OnlineHealthSystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Maps the {@code LoginHistory} table. Created ahead of the dedicated
 * LoginHistory slice (see the migration PRD) because it's a prerequisite
 * for this one: {@code checkLogin}/{@code registerLogout}'s behavior - an
 * inserted row on login, a stamped {@code LogoutDateTime} on logout - has
 * to be preserved as part of the Patient login/logout flows. The future
 * LoginHistory slice is expected to build a service/controller/reporting
 * layer on top of this entity and retire {@code LoginHistoryDAO}, not to
 * introduce the entity itself.
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
