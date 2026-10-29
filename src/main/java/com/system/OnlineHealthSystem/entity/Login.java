package com.system.OnlineHealthSystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Maps the {@code Login} table, which is shared between patients and
 * doctors (see schema.sql) - every account, regardless of role, has exactly
 * one row here holding its username/hashed-password/salt. Modeled as its
 * own entity (rather than folded into {@code Patient}) so the upcoming
 * Doctor slice can reuse it without any rework.
 */
@Entity
@Table(name = "Login")
public class Login {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LoginID")
    private Integer loginId;

    @Column(name = "UserName", nullable = false, unique = true)
    private String userName;

    @Column(name = "UserPassword", nullable = false)
    private String userPassword;

    @Column(name = "Salt", nullable = false)
    private String salt;

    public Integer getLoginId() {
        return loginId;
    }

    public void setLoginId(Integer loginId) {
        this.loginId = loginId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
