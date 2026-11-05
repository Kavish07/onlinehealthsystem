package com.system.OnlineHealthSystem.repository;

import com.system.OnlineHealthSystem.entity.Login;
import com.system.OnlineHealthSystem.entity.Patient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PatientRepositoryTest {

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private PatientRepository patientRepository;

    private Login savedLogin(String userName) {
        Login login = new Login();
        login.setUserName(userName);
        login.setUserPassword("hashed-password");
        login.setSalt("salt-value");
        return loginRepository.save(login);
    }

    private Patient profileFor(Login login) {
        Patient patient = new Patient();
        patient.setLogin(login);
        patient.setFirstName("Jane");
        patient.setLastName("Doe");
        patient.setNationalId("N12345");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setSex("F");
        patient.setAddress("1 Main St");
        patient.setCity("Springfield");
        patient.setWeight(65);
        patient.setHeight(170);
        return patient;
    }

    @Test
    void findByLoginLoginId_returnsTheMatchingProfile() {
        Login login = savedLogin("jane.doe");
        patientRepository.save(profileFor(login));

        var found = patientRepository.findByLogin_LoginId(login.getLoginId());

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Jane");
        assertThat(found.get().getLogin().getLoginId()).isEqualTo(login.getLoginId());
    }

    @Test
    void findAllWithLogin_joinsBothTables_withoutLazyLoadingFailures() {
        Login login1 = savedLogin("jane.doe");
        patientRepository.save(profileFor(login1));
        Login login2 = savedLogin("john.smith");
        patientRepository.save(profileFor(login2));

        var all = patientRepository.findAllWithLogin();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(p -> p.getLogin().getUserName())
                .containsExactlyInAnyOrder("jane.doe", "john.smith");
    }

    @Test
    void deleteByLoginLoginId_removesTheProfileRow() {
        Login login = savedLogin("jane.doe");
        patientRepository.save(profileFor(login));

        long deleted = patientRepository.deleteByLogin_LoginId(login.getLoginId());

        assertThat(deleted).isEqualTo(1);
        assertThat(patientRepository.findByLogin_LoginId(login.getLoginId())).isEmpty();
    }

    // The deleteTreatmentsByPatientLoginId native-SQL placeholder that used
    // to be tested here moved to TreatmentRepository.deleteByPatientLoginId
    // once the Treatment slice gave it a real JPA entity/repository to
    // belong to - see TreatmentRepositoryTest and PatientService.deletePatient().
}
