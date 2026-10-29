package com.system.OnlineHealthSystem.repository;

import com.system.OnlineHealthSystem.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Integer> {

    /**
     * Equivalent of {@code PatientDAO.registerLogout}'s
     * {@code update LoginHistory set LogoutDateTime = ? where LoginID = ?
     * and LogoutDateTime is null} - stamps the currently-open session row
     * for this LoginID, if any.
     */
    // clearAutomatically=true: without it, a caller that already holds a
    // managed LoginHistory from this same transaction (e.g. the one just
    // inserted at login) would see this bulk update's effect in the
    // database but not reflected back onto that managed instance/any
    // subsequent findById in the same transaction, since JPQL bulk updates
    // bypass the persistence context.
    @Modifying(clearAutomatically = true)
    @Query("update LoginHistory lh set lh.logoutDateTime = :logoutDateTime "
            + "where lh.loginId = :loginId and lh.logoutDateTime is null")
    int closeOpenSession(@Param("loginId") Integer loginId, @Param("logoutDateTime") LocalDateTime logoutDateTime);

    long deleteByLoginId(Integer loginId);
}
