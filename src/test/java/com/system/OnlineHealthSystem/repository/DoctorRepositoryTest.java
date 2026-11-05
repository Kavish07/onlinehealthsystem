package com.system.OnlineHealthSystem.repository;

import com.system.OnlineHealthSystem.entity.Department;
import com.system.OnlineHealthSystem.entity.Doctor;
import com.system.OnlineHealthSystem.entity.Login;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DoctorRepositoryTest {

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private Login savedLogin(String userName) {
        Login login = new Login();
        login.setUserName(userName);
        login.setUserPassword("hashed-password");
        login.setSalt("salt-value");
        return loginRepository.save(login);
    }

    private Department savedDepartment(String code, String name) {
        Department department = new Department();
        department.setDepartmentCode(code);
        department.setDepartmentName(name);
        entityManager.persist(department);
        return department;
    }

    private Doctor profileFor(Login login, Department department) {
        Doctor doctor = new Doctor();
        doctor.setLogin(login);
        doctor.setFirstName("John");
        doctor.setLastName("Smith");
        doctor.setNationalId("N98765");
        doctor.setDateOfBirth(LocalDate.of(1980, 5, 5));
        doctor.setSex("M");
        doctor.setAddress("2 Main St");
        doctor.setCity("Springfield");
        doctor.setWeight(80);
        doctor.setHeight(180);
        doctor.setQualifications("MD");
        doctor.setSpeciality("Cardiology");
        doctor.setDepartment(department);
        return doctor;
    }

    @Test
    void findByLoginLoginId_returnsTheMatchingProfile() {
        Login login = savedLogin("john.smith");
        Department department = savedDepartment("CARD", "Cardiology");
        doctorRepository.save(profileFor(login, department));

        var found = doctorRepository.findByLogin_LoginId(login.getLoginId());

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("John");
        assertThat(found.get().getLogin().getLoginId()).isEqualTo(login.getLoginId());
    }

    @Test
    void findAllExcludingUserName_joinsLoginAndDepartment_andExcludesTheGivenUserName() {
        Department department = savedDepartment("CARD", "Cardiology");
        Login login1 = savedLogin("john.smith");
        doctorRepository.save(profileFor(login1, department));
        Login adminLogin = savedLogin("admin@gmail.com");
        doctorRepository.save(profileFor(adminLogin, department));

        var result = doctorRepository.findAllExcludingUserName("admin@gmail.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLogin().getUserName()).isEqualTo("john.smith");
        assertThat(result.get(0).getDepartment().getDepartmentName()).isEqualTo("Cardiology");
    }

    @Test
    void findAllExcludingUserName_omitsDoctorsWithoutAMatchingDepartment() {
        // Mirrors the legacy inner-join behavior: a doctor row whose
        // DepartmentCode doesn't match any Departments row is silently
        // dropped from the listing, not returned with a null department.
        Login login = savedLogin("no.department");
        Doctor doctor = new Doctor();
        doctor.setLogin(login);
        doctor.setFirstName("No");
        doctor.setLastName("Department");
        doctorRepository.save(doctor);

        var result = doctorRepository.findAllExcludingUserName("admin@gmail.com");

        assertThat(result).isEmpty();
    }

    @Test
    void deleteByLoginLoginId_removesTheProfileRow() {
        Login login = savedLogin("john.smith");
        Department department = savedDepartment("CARD", "Cardiology");
        doctorRepository.save(profileFor(login, department));

        long deleted = doctorRepository.deleteByLogin_LoginId(login.getLoginId());

        assertThat(deleted).isEqualTo(1);
        assertThat(doctorRepository.findByLogin_LoginId(login.getLoginId())).isEmpty();
    }

    // The deleteTreatmentsByDoctorLoginId native-SQL placeholder that used
    // to be tested here moved to TreatmentRepository.deleteByDoctorLoginId
    // once the Treatment slice gave it a real JPA entity/repository to
    // belong to - see TreatmentRepositoryTest and DoctorService.deleteDoctor().
}
