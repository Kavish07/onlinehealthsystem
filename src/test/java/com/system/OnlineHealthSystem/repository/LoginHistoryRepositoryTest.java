package com.system.OnlineHealthSystem.repository;

import com.system.OnlineHealthSystem.entity.LoginHistory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LoginHistoryRepositoryTest {

    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

    private LoginHistory openSession(int loginId, LocalDateTime loginDateTime) {
        LoginHistory history = new LoginHistory();
        history.setLoginId(loginId);
        history.setLoginDateTime(loginDateTime);
        return loginHistoryRepository.save(history);
    }

    @Test
    void closeOpenSession_stampsLogoutDateTime_onTheOpenRowForThatLoginId() {
        LoginHistory saved = openSession(7, LocalDateTime.now().minusHours(1));
        // Truncated to microseconds: H2's TIMESTAMP(6) column only stores
        // that much precision, so a full-nanosecond LocalDateTime.now()
        // wouldn't round-trip for an exact equality check.
        LocalDateTime logoutTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

        int updated = loginHistoryRepository.closeOpenSession(7, logoutTime);

        assertThat(updated).isEqualTo(1);
        LoginHistory reloaded = loginHistoryRepository.findById(saved.getLoginHistoryId()).orElseThrow();
        assertThat(reloaded.getLogoutDateTime()).isEqualTo(logoutTime);
    }

    @Test
    void closeOpenSession_doesNothing_whenNoOpenRowExistsForThatLoginId() {
        int updated = loginHistoryRepository.closeOpenSession(999, LocalDateTime.now());

        assertThat(updated).isEqualTo(0);
    }

    @Test
    void deleteByLoginId_removesAllHistoryRowsForThatLoginId() {
        openSession(7, LocalDateTime.now());
        openSession(7, LocalDateTime.now().minusDays(1));
        openSession(8, LocalDateTime.now());

        long deleted = loginHistoryRepository.deleteByLoginId(7);

        assertThat(deleted).isEqualTo(2);
        assertThat(loginHistoryRepository.findAll()).hasSize(1);
    }
}
