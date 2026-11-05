package com.system.OnlineHealthSystem.service;

import com.onlinehealthsystem.PasswordEncoder;
import com.system.OnlineHealthSystem.entity.Department;
import com.system.OnlineHealthSystem.entity.Doctor;
import com.system.OnlineHealthSystem.entity.Login;
import com.system.OnlineHealthSystem.entity.LoginHistory;
import com.system.OnlineHealthSystem.repository.AppointmentRepository;
import com.system.OnlineHealthSystem.repository.DoctorRepository;
import com.system.OnlineHealthSystem.repository.LoginHistoryRepository;
import com.system.OnlineHealthSystem.repository.LoginRepository;
import com.system.OnlineHealthSystem.repository.TreatmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private LoginRepository loginRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private LoginHistoryRepository loginHistoryRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private TreatmentRepository treatmentRepository;

    private DoctorService doctorService;

    @BeforeEach
    void setUp() {
        doctorService = new DoctorService(loginRepository, doctorRepository, loginHistoryRepository,
                appointmentRepository, treatmentRepository);
    }

    private Login loginWithPassword(String userName, String rawPassword) {
        String salt = PasswordEncoder.getSaltvalue(30);
        Login login = new Login();
        login.setLoginId(7);
        login.setUserName(userName);
        login.setUserPassword(PasswordEncoder.generateSecurePassword(rawPassword, salt));
        login.setSalt(salt);
        return login;
    }

    @Test
    void login_succeeds_andRecordsLoginHistory_whenCredentialsAreCorrect() {
        Login login = loginWithPassword("john.smith", "s3cret-pw");
        when(loginRepository.findByUserName("john.smith")).thenReturn(Optional.of(login));

        Optional<com.onlinehealthsystem.Doctor> result = doctorService.login("john.smith", "s3cret-pw");

        assertThat(result).isPresent();
        assertThat(result.get().getLoginID()).isEqualTo(7);
        assertThat(result.get().getUserName()).isEqualTo("john.smith");

        ArgumentCaptor<LoginHistory> captor = ArgumentCaptor.forClass(LoginHistory.class);
        verify(loginHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getLoginId()).isEqualTo(7);
        assertThat(captor.getValue().getLoginDateTime()).isNotNull();
    }

    @Test
    void login_returnsEmpty_whenUserNameDoesNotExist() {
        // Regression check: the legacy checkLogin used to NPE here instead
        // of returning null for an unknown username.
        when(loginRepository.findByUserName("nobody")).thenReturn(Optional.empty());

        Optional<com.onlinehealthsystem.Doctor> result = doctorService.login("nobody", "whatever");

        assertThat(result).isEmpty();
        verify(loginHistoryRepository, never()).save(any());
    }

    @Test
    void login_returnsEmpty_whenPasswordIsWrong() {
        Login login = loginWithPassword("john.smith", "s3cret-pw");
        when(loginRepository.findByUserName("john.smith")).thenReturn(Optional.of(login));

        Optional<com.onlinehealthsystem.Doctor> result = doctorService.login("john.smith", "wrong-password");

        assertThat(result).isEmpty();
        verify(loginHistoryRepository, never()).save(any());
    }

    @Test
    void isUsernameTaken_delegatesToRepository() {
        when(loginRepository.existsByUserName("john.smith")).thenReturn(true);

        assertThat(doctorService.isUsernameTaken("john.smith")).isTrue();
    }

    @Test
    void register_hashesThePassword_andSavesLoginThenProfile() {
        when(loginRepository.save(any(Login.class))).thenAnswer(invocation -> {
            Login saved = invocation.getArgument(0);
            saved.setLoginId(42);
            return saved;
        });

        com.onlinehealthsystem.Doctor result = doctorService.register("john.smith", "s3cret-pw", "John", "Smith",
                "N98765", LocalDate.of(1980, 5, 5), "M", "2 Main St", "Springfield", 80, 180, "MD", "Cardiology",
                "CARD");

        assertThat(result.getLoginID()).isEqualTo(42);
        assertThat(result.getUserName()).isEqualTo("john.smith");

        ArgumentCaptor<Login> loginCaptor = ArgumentCaptor.forClass(Login.class);
        verify(loginRepository).save(loginCaptor.capture());
        Login savedLogin = loginCaptor.getValue();
        assertThat(savedLogin.getUserPassword()).isNotEqualTo("s3cret-pw");
        assertThat(PasswordEncoder.verifyUserPassword("s3cret-pw", savedLogin.getUserPassword(), savedLogin.getSalt())).isTrue();

        ArgumentCaptor<Doctor> doctorCaptor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(doctorCaptor.capture());
        Doctor savedDoctor = doctorCaptor.getValue();
        assertThat(savedDoctor.getFirstName()).isEqualTo("John");
        assertThat(savedDoctor.getLastName()).isEqualTo("Smith");
        assertThat(savedDoctor.getQualifications()).isEqualTo("MD");
        assertThat(savedDoctor.getSpeciality()).isEqualTo("Cardiology");
        assertThat(savedDoctor.getDepartment().getDepartmentCode()).isEqualTo("CARD");
        assertThat(savedDoctor.getDateOfBirth()).isEqualTo(LocalDate.of(1980, 5, 5));
        assertThat(savedDoctor.getLogin().getLoginId()).isEqualTo(42);
    }

    @Test
    void deleteDoctor_cascadesAcrossAppointmentsTreatmentsDoctorsLoginHistoryAndLogin() {
        doctorService.deleteDoctor(7);

        verify(appointmentRepository).deleteByDoctorLoginId(7);
        verify(treatmentRepository).deleteByDoctorLoginId(7);
        verify(doctorRepository).deleteByLogin_LoginId(7);
        verify(loginHistoryRepository).deleteByLoginId(7);
        verify(loginRepository).deleteById(7);
    }

    @Test
    void updatePassword_rehashesWithAFreshSalt_andReturnsUpdatedDoctor_whenUserNameExists() {
        Login login = loginWithPassword("john.smith", "old-password");
        String oldHash = login.getUserPassword();
        String oldSalt = login.getSalt();
        when(loginRepository.findByUserName("john.smith")).thenReturn(Optional.of(login));

        Optional<com.onlinehealthsystem.Doctor> result = doctorService.updatePassword("john.smith", "new-password");

        assertThat(result).isPresent();
        // Fixes a dropped-LoginID bug in the legacy DoctorDAO.updatePassword,
        // which only copied username/password onto the returned Doctor.
        assertThat(result.get().getLoginID()).isEqualTo(7);
        verify(loginRepository).save(login);
        assertThat(login.getUserPassword()).isNotEqualTo(oldHash);
        assertThat(login.getSalt()).isNotEqualTo(oldSalt);
        assertThat(PasswordEncoder.verifyUserPassword("new-password", login.getUserPassword(), login.getSalt())).isTrue();
    }

    @Test
    void updatePassword_returnsEmpty_andDoesNotSave_whenUserNameDoesNotExist() {
        when(loginRepository.findByUserName("nobody")).thenReturn(Optional.empty());

        Optional<com.onlinehealthsystem.Doctor> result = doctorService.updatePassword("nobody", "new-password");

        assertThat(result).isEmpty();
        verify(loginRepository, never()).save(any());
    }

    @Test
    void listDoctors_excludesAdmin_andMapsJoinedRowsToLegacyDoctorObjects() {
        Login login = loginWithPassword("john.smith", "pw");
        Department department = new Department();
        department.setDepartmentCode("CARD");
        department.setDepartmentName("Cardiology");
        Doctor profile = new Doctor();
        profile.setLogin(login);
        profile.setFirstName("John");
        profile.setLastName("Smith");
        profile.setNationalId("N98765");
        profile.setDateOfBirth(LocalDate.of(1980, 5, 5));
        profile.setSex("M");
        profile.setAddress("2 Main St");
        profile.setCity("Springfield");
        profile.setWeight(80);
        profile.setHeight(180);
        profile.setQualifications("MD");
        profile.setSpeciality("Cardiology");
        profile.setDepartment(department);
        when(doctorRepository.findAllExcludingUserName(com.onlinehealthsystem.AppConstants.ADMIN_USERNAME))
                .thenReturn(List.of(profile));

        List<com.onlinehealthsystem.Doctor> result = doctorService.listDoctors();

        assertThat(result).hasSize(1);
        com.onlinehealthsystem.Doctor legacy = result.get(0);
        assertThat(legacy.getLoginID()).isEqualTo(7);
        assertThat(legacy.getUserName()).isEqualTo("john.smith");
        assertThat(legacy.getFirstName()).isEqualTo("John");
        assertThat(legacy.getLastName()).isEqualTo("Smith");
        assertThat(legacy.getNationalID()).isEqualTo("N98765");
        assertThat(legacy.getSex()).isEqualTo("M");
        assertThat(legacy.getAddress()).isEqualTo("2 Main St");
        assertThat(legacy.getCity()).isEqualTo("Springfield");
        assertThat(legacy.getWeight()).isEqualTo(80);
        assertThat(legacy.getHeight()).isEqualTo(180);
        assertThat(legacy.getQualifications()).isEqualTo("MD");
        assertThat(legacy.getSpeciality()).isEqualTo("Cardiology");
        // Matches the legacy listDoctors()'s quirk of stuffing the joined
        // DepartmentName (not DepartmentCode) into this field.
        assertThat(legacy.getDepartmentCode()).isEqualTo("Cardiology");
    }

    @Test
    void registerLogout_closesTheOpenLoginHistoryRowForThatLoginId() {
        doctorService.registerLogout(7);

        verify(loginHistoryRepository).closeOpenSession(eq(7), any());
    }
}
