package com.system.OnlineHealthSystem.repository;

import com.system.OnlineHealthSystem.entity.Login;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LoginRepositoryTest {

    @Autowired
    private LoginRepository loginRepository;

    private Login newLogin(String userName) {
        Login login = new Login();
        login.setUserName(userName);
        login.setUserPassword("hashed-password");
        login.setSalt("salt-value");
        return login;
    }

    @Test
    void findByUserName_returnsSavedLogin() {
        loginRepository.save(newLogin("jane.doe"));

        var found = loginRepository.findByUserName("jane.doe");

        assertThat(found).isPresent();
        assertThat(found.get().getUserName()).isEqualTo("jane.doe");
        assertThat(found.get().getLoginId()).isNotNull();
    }

    @Test
    void findByUserName_isEmpty_whenNoSuchUser() {
        assertThat(loginRepository.findByUserName("nobody")).isEmpty();
    }

    @Test
    void existsByUserName_reflectsWhetherTheUserNameIsTaken() {
        assertThat(loginRepository.existsByUserName("jane.doe")).isFalse();

        loginRepository.save(newLogin("jane.doe"));

        assertThat(loginRepository.existsByUserName("jane.doe")).isTrue();
    }
}
