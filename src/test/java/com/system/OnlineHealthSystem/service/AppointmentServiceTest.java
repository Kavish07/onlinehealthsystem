package com.system.OnlineHealthSystem.service;

import com.system.OnlineHealthSystem.entity.Department;
import com.system.OnlineHealthSystem.entity.Doctor;
import com.system.OnlineHealthSystem.entity.Login;
import com.system.OnlineHealthSystem.entity.Patient;
import com.system.OnlineHealthSystem.repository.AppointmentRepository;
import com.system.OnlineHealthSystem.repository.DoctorRepository;
import com.system.OnlineHealthSystem.repository.LoginRepository;
import com.system.OnlineHealthSystem.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private LoginRepository loginRepository;

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(appointmentRepository, patientRepository, doctorRepository,
                loginRepository);
    }

    private com.system.OnlineHealthSystem.entity.Appointment appointmentEntity(int patientLoginId, int doctorLoginId,
            String departmentCode, String departmentName, boolean approved) {
        com.system.OnlineHealthSystem.entity.Appointment appointment = new com.system.OnlineHealthSystem.entity.Appointment();
        appointment.setAppointmentId(99);
        appointment.setAppointmentDate(LocalDateTime.of(2026, 1, 15, 10, 0));
        appointment.setLocation("Room 1");
        Department department = new Department();
        department.setDepartmentCode(departmentCode);
        department.setDepartmentName(departmentName);
        appointment.setDepartment(department);
        Login patientLogin = new Login();
        patientLogin.setLoginId(patientLoginId);
        appointment.setPatientLogin(patientLogin);
        Login doctorLogin = new Login();
        doctorLogin.setLoginId(doctorLoginId);
        appointment.setDoctorLogin(doctorLogin);
        appointment.setApproved(approved);
        return appointment;
    }

    private Patient patientProfile(int loginId, int patientId, String firstName, String lastName) {
        Login login = new Login();
        login.setLoginId(loginId);
        Patient patient = new Patient();
        patient.setPatientId(patientId);
        patient.setLogin(login);
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        return patient;
    }

    private Doctor doctorProfile(int loginId, int doctorId, String firstName, String lastName) {
        Login login = new Login();
        login.setLoginId(loginId);
        Doctor doctor = new Doctor();
        doctor.setDoctorId(doctorId);
        doctor.setLogin(login);
        doctor.setFirstName(firstName);
        doctor.setLastName(lastName);
        return doctor;
    }

    @Test
    void registerAppointment_savesANewUnapprovedAppointment() {
        appointmentService.registerAppointment(LocalDateTime.of(2026, 1, 15, 10, 0), "Room 1", "CARD", 7, 42);

        ArgumentCaptor<com.system.OnlineHealthSystem.entity.Appointment> captor =
                ArgumentCaptor.forClass(com.system.OnlineHealthSystem.entity.Appointment.class);
        verify(appointmentRepository).save(captor.capture());
        com.system.OnlineHealthSystem.entity.Appointment saved = captor.getValue();
        assertThat(saved.getAppointmentDate()).isEqualTo(LocalDateTime.of(2026, 1, 15, 10, 0));
        assertThat(saved.getLocation()).isEqualTo("Room 1");
        assertThat(saved.getDepartment().getDepartmentCode()).isEqualTo("CARD");
        assertThat(saved.getPatientLogin().getLoginId()).isEqualTo(7);
        assertThat(saved.getDoctorLogin().getLoginId()).isEqualTo(42);
        assertThat(saved.getApproved()).isFalse();
    }

    @Test
    void registerAppointment_leavesDepartmentNull_whenDepartmentCodeIsNull() {
        appointmentService.registerAppointment(LocalDateTime.of(2026, 1, 15, 10, 0), "Room 1", null, 7, 42);

        ArgumentCaptor<com.system.OnlineHealthSystem.entity.Appointment> captor =
                ArgumentCaptor.forClass(com.system.OnlineHealthSystem.entity.Appointment.class);
        verify(appointmentRepository).save(captor.capture());
        assertThat(captor.getValue().getDepartment()).isNull();
    }

    @Test
    void registerAppointment_rejectsASundayAppointment() {
        // 2026-01-18 is a Sunday.
        assertThatThrownBy(() ->
                appointmentService.registerAppointment(LocalDateTime.of(2026, 1, 18, 10, 0), "Room 1", "CARD", 7, 42))
                .isInstanceOf(IllegalArgumentException.class);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void registerAppointment_rejectsAPublicHoliday() {
        assertThatThrownBy(() ->
                appointmentService.registerAppointment(LocalDateTime.of(2026, 12, 25, 10, 0), "Room 1", "CARD", 7, 42))
                .isInstanceOf(IllegalArgumentException.class);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void registerAppointment_rejectsATimeOutsideClinicOpeningHours() {
        assertThatThrownBy(() ->
                appointmentService.registerAppointment(LocalDateTime.of(2026, 1, 15, 8, 0), "Room 1", "CARD", 7, 42))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() ->
                appointmentService.registerAppointment(LocalDateTime.of(2026, 1, 15, 17, 0), "Room 1", "CARD", 7, 42))
                .isInstanceOf(IllegalArgumentException.class);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void confirmAppointment_setsApprovedTrue_andSaves_whenAppointmentExists() {
        com.system.OnlineHealthSystem.entity.Appointment existing = appointmentEntity(7, 42, "CARD", "Cardiology", false);
        when(appointmentRepository.findById(99)).thenReturn(Optional.of(existing));

        appointmentService.confirmAppointment(99);

        assertThat(existing.getApproved()).isTrue();
        verify(appointmentRepository).save(existing);
    }

    @Test
    void confirmAppointment_doesNothing_whenAppointmentDoesNotExist() {
        // Matches the legacy AppointmentDAO.confirmAppointment's silent
        // no-op on a non-matching AppointmentId (an UPDATE affecting zero
        // rows doesn't error).
        when(appointmentRepository.findById(99)).thenReturn(Optional.empty());

        appointmentService.confirmAppointment(99);

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void listAppointments_mapsJoinedRowsToLegacyAppointmentObjects_withSpaceSeparatedNames() {
        com.system.OnlineHealthSystem.entity.Appointment entity = appointmentEntity(7, 42, "CARD", "Cardiology", false);
        when(appointmentRepository.findAllWithAssociations()).thenReturn(List.of(entity));
        when(patientRepository.findByLogin_LoginId(7)).thenReturn(Optional.of(patientProfile(7, 3, "Jane", "Doe")));
        when(doctorRepository.findByLogin_LoginId(42)).thenReturn(Optional.of(doctorProfile(42, 5, "John", "Smith")));

        List<com.onlinehealthsystem.Appointment> result = appointmentService.listAppointments();

        assertThat(result).hasSize(1);
        com.onlinehealthsystem.Appointment legacy = result.get(0);
        assertThat(legacy.getAppointmentID()).isEqualTo(99);
        assertThat(legacy.getLocation()).isEqualTo("Room 1");
        assertThat(legacy.getDepartmentCode()).isEqualTo("CARD");
        assertThat(legacy.getDepartmentName()).isEqualTo("Cardiology");
        assertThat(legacy.getPatientID()).isEqualTo(3);
        assertThat(legacy.getPatientName()).isEqualTo("Jane Doe");
        assertThat(legacy.getDoctorID()).isEqualTo(5);
        assertThat(legacy.getDoctorName()).isEqualTo("John Smith");
        assertThat(legacy.getApproved()).isFalse();
    }

    @Test
    void listAppointments_omitsRows_whenPatientOrDoctorProfileIsMissing() {
        // Mirrors the legacy query's inner join against Patients/Doctors:
        // an appointment whose PatientID/DoctorID has no matching profile
        // row is silently dropped.
        com.system.OnlineHealthSystem.entity.Appointment entity = appointmentEntity(7, 42, "CARD", "Cardiology", false);
        when(appointmentRepository.findAllWithAssociations()).thenReturn(List.of(entity));
        when(patientRepository.findByLogin_LoginId(7)).thenReturn(Optional.empty());

        List<com.onlinehealthsystem.Appointment> result = appointmentService.listAppointments();

        assertThat(result).isEmpty();
    }

    @Test
    void checkAppointment_returnsNull_whenUserNameDoesNotExist() {
        // Regression check: the legacy checkAppointment used to return a
        // non-null Appointment with every field at its default (0/null)
        // instead of null for "no such appointment".
        when(loginRepository.findByUserName("nobody")).thenReturn(Optional.empty());

        com.onlinehealthsystem.Appointment result = appointmentService.checkAppointment("nobody");

        assertThat(result).isNull();
    }

    @Test
    void checkAppointment_returnsNull_whenPatientHasNoAppointment() {
        Login login = new Login();
        login.setLoginId(7);
        when(loginRepository.findByUserName("jane.doe")).thenReturn(Optional.of(login));
        when(appointmentRepository.findByPatientLoginId(7)).thenReturn(List.of());

        com.onlinehealthsystem.Appointment result = appointmentService.checkAppointment("jane.doe");

        assertThat(result).isNull();
    }

    @Test
    void checkAppointment_returnsTheMatch_withNoSpaceSeparatedNames_andNoAppointmentIdSet() {
        // Matches the legacy checkAppointment's own quirk (distinct from
        // listAppointments): the name fields are concatenated without a
        // space, and the AppointmentID is never set on the result.
        Login login = new Login();
        login.setLoginId(7);
        when(loginRepository.findByUserName("jane.doe")).thenReturn(Optional.of(login));
        com.system.OnlineHealthSystem.entity.Appointment entity = appointmentEntity(7, 42, "CARD", "Cardiology", true);
        when(appointmentRepository.findByPatientLoginId(7)).thenReturn(List.of(entity));
        when(patientRepository.findByLogin_LoginId(7)).thenReturn(Optional.of(patientProfile(7, 3, "Jane", "Doe")));
        when(doctorRepository.findByLogin_LoginId(42)).thenReturn(Optional.of(doctorProfile(42, 5, "John", "Smith")));

        com.onlinehealthsystem.Appointment result = appointmentService.checkAppointment("jane.doe");

        assertThat(result).isNotNull();
        assertThat(result.getAppointmentID()).isZero();
        assertThat(result.getLocation()).isEqualTo("Room 1");
        assertThat(result.getDepartmentName()).isEqualTo("Cardiology");
        assertThat(result.getDepartmentCode()).isEqualTo("CARD");
        assertThat(result.getPatientID()).isEqualTo(3);
        assertThat(result.getPatientName()).isEqualTo("JaneDoe");
        assertThat(result.getDoctorID()).isEqualTo(5);
        assertThat(result.getDoctorName()).isEqualTo("JohnSmith");
        assertThat(result.getApproved()).isTrue();
    }
}
