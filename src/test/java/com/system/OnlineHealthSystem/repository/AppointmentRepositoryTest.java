package com.system.OnlineHealthSystem.repository;

import com.system.OnlineHealthSystem.entity.Appointment;
import com.system.OnlineHealthSystem.entity.Department;
import com.system.OnlineHealthSystem.entity.Login;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AppointmentRepositoryTest {

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

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

    private Appointment appointmentFor(Login patientLogin, Login doctorLogin, Department department) {
        Appointment appointment = new Appointment();
        appointment.setAppointmentDate(LocalDateTime.of(2026, 1, 15, 10, 0));
        appointment.setLocation("Room 1");
        appointment.setDepartment(department);
        appointment.setPatientLogin(patientLogin);
        appointment.setDoctorLogin(doctorLogin);
        appointment.setApproved(false);
        return appointment;
    }

    @Test
    void findAllWithAssociations_joinsDepartmentPatientLoginAndDoctorLogin() {
        Login patientLogin = savedLogin("jane.doe");
        Login doctorLogin = savedLogin("john.smith");
        Department department = savedDepartment("CARD", "Cardiology");
        appointmentRepository.save(appointmentFor(patientLogin, doctorLogin, department));

        var all = appointmentRepository.findAllWithAssociations();

        assertThat(all).hasSize(1);
        Appointment found = all.get(0);
        assertThat(found.getLocation()).isEqualTo("Room 1");
        assertThat(found.getDepartment().getDepartmentName()).isEqualTo("Cardiology");
        assertThat(found.getPatientLogin().getLoginId()).isEqualTo(patientLogin.getLoginId());
        assertThat(found.getDoctorLogin().getLoginId()).isEqualTo(doctorLogin.getLoginId());
    }

    @Test
    void findAllWithAssociations_omitsAppointmentsWithoutAMatchingDepartment() {
        // Mirrors the legacy AppointmentDAO.listAppointments()'s inner-join
        // behavior: an appointment row whose DepartmentCode doesn't match
        // any Departments row is silently dropped from the listing.
        Login patientLogin = savedLogin("jane.doe");
        Login doctorLogin = savedLogin("john.smith");
        Appointment appointment = new Appointment();
        appointment.setAppointmentDate(LocalDateTime.of(2026, 1, 15, 10, 0));
        appointment.setLocation("Room 1");
        appointment.setPatientLogin(patientLogin);
        appointment.setDoctorLogin(doctorLogin);
        appointment.setApproved(false);
        appointmentRepository.save(appointment);

        var all = appointmentRepository.findAllWithAssociations();

        assertThat(all).isEmpty();
    }

    @Test
    void findByPatientLoginId_returnsOnlyThatPatientsAppointments() {
        Login patientLogin = savedLogin("jane.doe");
        Login otherPatientLogin = savedLogin("other.patient");
        Login doctorLogin = savedLogin("john.smith");
        Department department = savedDepartment("CARD", "Cardiology");
        appointmentRepository.save(appointmentFor(patientLogin, doctorLogin, department));
        appointmentRepository.save(appointmentFor(otherPatientLogin, doctorLogin, department));

        var found = appointmentRepository.findByPatientLoginId(patientLogin.getLoginId());

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getPatientLogin().getLoginId()).isEqualTo(patientLogin.getLoginId());
    }

    @Test
    void deleteByPatientLoginId_removesOnlyThatPatientsAppointments() {
        Login patientLogin = savedLogin("jane.doe");
        Login otherPatientLogin = savedLogin("other.patient");
        Login doctorLogin = savedLogin("john.smith");
        Department department = savedDepartment("CARD", "Cardiology");
        appointmentRepository.save(appointmentFor(patientLogin, doctorLogin, department));
        appointmentRepository.save(appointmentFor(otherPatientLogin, doctorLogin, department));

        appointmentRepository.deleteByPatientLoginId(patientLogin.getLoginId());

        assertThat(appointmentRepository.findAllWithAssociations()).hasSize(1);
        assertThat(appointmentRepository.findAllWithAssociations().get(0).getPatientLogin().getLoginId())
                .isEqualTo(otherPatientLogin.getLoginId());
    }

    @Test
    void deleteByDoctorLoginId_removesOnlyThatDoctorsAppointments() {
        Login patientLogin = savedLogin("jane.doe");
        Login doctorLogin = savedLogin("john.smith");
        Login otherDoctorLogin = savedLogin("other.doctor");
        Department department = savedDepartment("CARD", "Cardiology");
        appointmentRepository.save(appointmentFor(patientLogin, doctorLogin, department));
        appointmentRepository.save(appointmentFor(patientLogin, otherDoctorLogin, department));

        appointmentRepository.deleteByDoctorLoginId(doctorLogin.getLoginId());

        assertThat(appointmentRepository.findAllWithAssociations()).hasSize(1);
        assertThat(appointmentRepository.findAllWithAssociations().get(0).getDoctorLogin().getLoginId())
                .isEqualTo(otherDoctorLogin.getLoginId());
    }
}
