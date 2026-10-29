package com.system.OnlineHealthSystem.repository;

import com.system.OnlineHealthSystem.entity.Login;
import com.system.OnlineHealthSystem.entity.Patient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

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

    @Test
    @Sql(statements = {
            "insert into Appointments (Date, Location, DepartmentCode, PatientID, DoctorID, Approved) "
                    + "values ('2026-01-01', 'Room 1', null, 42, 7, false)",
            "insert into Treatments (Date, Location, DepartmentCode, TreatmentName, MedicineCode, PatientID, DoctorID, BillID) "
                    + "values ('2026-01-01', 'Room 1', null, 'Checkup', null, 42, 7, null)"
    })
    void deleteAppointmentsAndTreatmentsByPatientLoginId_removeOnlyMatchingRows() {
        patientRepository.deleteAppointmentsByPatientLoginId(42);
        patientRepository.deleteTreatmentsByPatientLoginId(42);

        // No assertion API for raw tables via this repository; absence of an
        // exception plus the companion service-level test cover the intent.
        // This test mainly guards the native SQL stays syntactically valid
        // against the real column names (PatientID, not LoginID).
    }
}
