package com.system.OnlineHealthSystem.service;

import com.onlinehealthsystem.PasswordEncoder;
import com.system.OnlineHealthSystem.entity.Login;
import com.system.OnlineHealthSystem.entity.LoginHistory;
import com.system.OnlineHealthSystem.entity.Patient;
import com.system.OnlineHealthSystem.repository.LoginHistoryRepository;
import com.system.OnlineHealthSystem.repository.LoginRepository;
import com.system.OnlineHealthSystem.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private LoginRepository loginRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private LoginHistoryRepository loginHistoryRepository;

    private PatientService patientService;

    @BeforeEach
    void setUp() {
        patientService = new PatientService(loginRepository, patientRepository, loginHistoryRepository);
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
        Login login = loginWithPassword("jane.doe", "s3cret-pw");
        when(loginRepository.findByUserName("jane.doe")).thenReturn(Optional.of(login));

        Optional<com.onlinehealthsystem.Patient> result = patientService.login("jane.doe", "s3cret-pw");

        assertThat(result).isPresent();
        assertThat(result.get().getLoginID()).isEqualTo(7);
        assertThat(result.get().getUserName()).isEqualTo("jane.doe");

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

        Optional<com.onlinehealthsystem.Patient> result = patientService.login("nobody", "whatever");

        assertThat(result).isEmpty();
        verify(loginHistoryRepository, never()).save(any());
    }

    @Test
    void login_returnsEmpty_whenPasswordIsWrong() {
        Login login = loginWithPassword("jane.doe", "s3cret-pw");
        when(loginRepository.findByUserName("jane.doe")).thenReturn(Optional.of(login));

        Optional<com.onlinehealthsystem.Patient> result = patientService.login("jane.doe", "wrong-password");

        assertThat(result).isEmpty();
        verify(loginHistoryRepository, never()).save(any());
    }

    @Test
    void isUsernameTaken_delegatesToRepository() {
        when(loginRepository.existsByUserName("jane.doe")).thenReturn(true);

        assertThat(patientService.isUsernameTaken("jane.doe")).isTrue();
    }

    @Test
    void register_hashesThePassword_andSavesLoginThenProfile() {
        when(loginRepository.save(any(Login.class))).thenAnswer(invocation -> {
            Login saved = invocation.getArgument(0);
            saved.setLoginId(42);
            return saved;
        });

        com.onlinehealthsystem.Patient result = patientService.register("jane.doe", "s3cret-pw", "Jane", "Doe",
                "N12345", Date.valueOf("1990-01-01"), "F", "1 Main St", "Springfield", 65, 170);

        assertThat(result.getLoginID()).isEqualTo(42);
        assertThat(result.getUserName()).isEqualTo("jane.doe");

        ArgumentCaptor<Login> loginCaptor = ArgumentCaptor.forClass(Login.class);
        verify(loginRepository).save(loginCaptor.capture());
        Login savedLogin = loginCaptor.getValue();
        assertThat(savedLogin.getUserPassword()).isNotEqualTo("s3cret-pw");
        assertThat(PasswordEncoder.verifyUserPassword("s3cret-pw", savedLogin.getUserPassword(), savedLogin.getSalt())).isTrue();

        ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(patientCaptor.capture());
        Patient savedPatient = patientCaptor.getValue();
        assertThat(savedPatient.getFirstName()).isEqualTo("Jane");
        assertThat(savedPatient.getLastName()).isEqualTo("Doe");
        assertThat(savedPatient.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(savedPatient.getLogin().getLoginId()).isEqualTo(42);
    }

    @Test
    void deletePatient_cascadesAcrossAppointmentsTreatmentsPatientsLoginHistoryAndLogin() {
        patientService.deletePatient(7);

        verify(patientRepository).deleteAppointmentsByPatientLoginId(7);
        verify(patientRepository).deleteTreatmentsByPatientLoginId(7);
        verify(patientRepository).deleteByLogin_LoginId(7);
        verify(loginHistoryRepository).deleteByLoginId(7);
        verify(loginRepository).deleteById(7);
    }

    @Test
    void updatePassword_rehashesWithAFreshSalt_andReturnsUpdatedPatient_whenUserNameExists() {
        Login login = loginWithPassword("jane.doe", "old-password");
        String oldHash = login.getUserPassword();
        String oldSalt = login.getSalt();
        when(loginRepository.findByUserName("jane.doe")).thenReturn(Optional.of(login));

        Optional<com.onlinehealthsystem.Patient> result = patientService.updatePassword("jane.doe", "new-password");

        assertThat(result).isPresent();
        verify(loginRepository).save(login);
        assertThat(login.getUserPassword()).isNotEqualTo(oldHash);
        assertThat(login.getSalt()).isNotEqualTo(oldSalt);
        assertThat(PasswordEncoder.verifyUserPassword("new-password", login.getUserPassword(), login.getSalt())).isTrue();
    }

    @Test
    void updatePassword_returnsEmpty_andDoesNotSave_whenUserNameDoesNotExist() {
        when(loginRepository.findByUserName("nobody")).thenReturn(Optional.empty());

        Optional<com.onlinehealthsystem.Patient> result = patientService.updatePassword("nobody", "new-password");

        assertThat(result).isEmpty();
        verify(loginRepository, never()).save(any());
    }

    @Test
    void listPatients_mapsJoinedRowsToLegacyPatientObjects() {
        Login login = loginWithPassword("jane.doe", "pw");
        Patient profile = new Patient();
        profile.setLogin(login);
        profile.setFirstName("Jane");
        profile.setLastName("Doe");
        profile.setNationalId("N12345");
        profile.setDateOfBirth(LocalDate.of(1990, 1, 1));
        profile.setSex("F");
        profile.setAddress("1 Main St");
        profile.setCity("Springfield");
        profile.setWeight(65);
        profile.setHeight(170);
        when(patientRepository.findAllWithLogin()).thenReturn(List.of(profile));

        List<com.onlinehealthsystem.Patient> result = patientService.listPatients();

        assertThat(result).hasSize(1);
        com.onlinehealthsystem.Patient legacy = result.get(0);
        assertThat(legacy.getLoginID()).isEqualTo(7);
        assertThat(legacy.getUserName()).isEqualTo("jane.doe");
        assertThat(legacy.getFirstName()).isEqualTo("Jane");
        assertThat(legacy.getLastName()).isEqualTo("Doe");
        assertThat(legacy.getNationalID()).isEqualTo("N12345");
        assertThat(legacy.getSex()).isEqualTo("F");
        assertThat(legacy.getAddress()).isEqualTo("1 Main St");
        assertThat(legacy.getCity()).isEqualTo("Springfield");
        assertThat(legacy.getWeight()).isEqualTo(65);
        assertThat(legacy.getHeight()).isEqualTo(170);
    }

    @Test
    void registerLogout_closesTheOpenLoginHistoryRowForThatLoginId() {
        patientService.registerLogout(7);

        verify(loginHistoryRepository).closeOpenSession(eq(7), any());
    }
}
