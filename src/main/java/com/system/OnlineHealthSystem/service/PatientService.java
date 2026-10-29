package com.system.OnlineHealthSystem.service;

import com.onlinehealthsystem.PasswordEncoder;
import com.system.OnlineHealthSystem.entity.Login;
import com.system.OnlineHealthSystem.entity.LoginHistory;
import com.system.OnlineHealthSystem.entity.Patient;
import com.system.OnlineHealthSystem.repository.LoginHistoryRepository;
import com.system.OnlineHealthSystem.repository.LoginRepository;
import com.system.OnlineHealthSystem.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Replacement for {@code com.onlinehealthsystem.PatientDAO}'s business
 * logic. Returns/accepts the legacy {@code com.onlinehealthsystem.Patient}
 * POJO for the single-record operations (login, register, update password)
 * because that's exactly the type still placed in the HttpSession's
 * "patient" attribute and read by not-yet-migrated legacy servlets/JSPs
 * (several still do a hard {@code (Patient) session.getAttribute("patient")}
 * cast) - introducing a different type here would break them. New code
 * should otherwise prefer the {@code com.system.OnlineHealthSystem.entity}
 * types, which is what the JPA repositories themselves deal in.
 */
@Service
public class PatientService {

    private static final int SALT_LENGTH = 30;

    private final LoginRepository loginRepository;
    private final PatientRepository patientRepository;
    private final LoginHistoryRepository loginHistoryRepository;

    public PatientService(LoginRepository loginRepository, PatientRepository patientRepository,
            LoginHistoryRepository loginHistoryRepository) {
        this.loginRepository = loginRepository;
        this.patientRepository = patientRepository;
        this.loginHistoryRepository = loginHistoryRepository;
    }

    /**
     * Equivalent of {@code PatientDAO.checkLogin}: verifies the password
     * with {@link PasswordEncoder}'s constant-time comparison and records a
     * LoginHistory row on success. Returns {@link Optional#empty()} rather
     * than null for both "no such username" and "wrong password" - the
     * legacy checkLogin NPE'd on the former before an earlier fix; this
     * keeps both outcomes handled the same, safe way.
     */
    @Transactional
    public Optional<com.onlinehealthsystem.Patient> login(String userName, String rawPassword) {
        Optional<Login> loginOpt = loginRepository.findByUserName(userName);
        if (loginOpt.isEmpty()) {
            return Optional.empty();
        }

        Login login = loginOpt.get();
        if (!PasswordEncoder.verifyUserPassword(rawPassword, login.getUserPassword(), login.getSalt())) {
            return Optional.empty();
        }

        LoginHistory history = new LoginHistory();
        history.setLoginId(login.getLoginId());
        history.setLoginDateTime(LocalDateTime.now());
        loginHistoryRepository.save(history);

        return Optional.of(toLegacyLoginResult(login));
    }

    /** Equivalent of {@code PatientDAO.checkRegistration}'s existence check. */
    public boolean isUsernameTaken(String userName) {
        return loginRepository.existsByUserName(userName);
    }

    /**
     * Equivalent of {@code PatientDAO.registerPatient}: hashes+salts the
     * password, inserts the {@code Login} row, then the {@code Patients}
     * profile row pointing at it. Unlike the legacy DAO - which re-reads
     * the new LoginID back via {@code select ... order by 1 desc limit 1}
     * (a race condition under concurrent registrations) - this relies on
     * JPA populating the generated id directly from the insert, which is
     * both simpler and race-free.
     */
    @Transactional
    public com.onlinehealthsystem.Patient register(String userName, String rawPassword, String firstName,
            String lastName, String nationalId, Date dateOfBirth, String sex, String address, String city,
            int weight, int height) {
        String salt = PasswordEncoder.getSaltvalue(SALT_LENGTH);
        String hashedPassword = PasswordEncoder.generateSecurePassword(rawPassword, salt);

        Login login = new Login();
        login.setUserName(userName);
        login.setUserPassword(hashedPassword);
        login.setSalt(salt);
        login = loginRepository.save(login);

        Patient profile = new Patient();
        profile.setLogin(login);
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setNationalId(nationalId);
        profile.setDateOfBirth(dateOfBirth == null ? null : dateOfBirth.toLocalDate());
        profile.setSex(sex);
        profile.setAddress(address);
        profile.setCity(city);
        profile.setWeight(weight);
        profile.setHeight(height);
        patientRepository.save(profile);

        return toLegacyLoginResult(login);
    }

    /**
     * Equivalent of {@code PatientDAO.deletePatient}'s cascading delete
     * across Appointments, Treatments, Patients, LoginHistory and Login -
     * all keyed by this person's {@code Login.LoginID} (stored directly in
     * Appointments/Treatments' own {@code PatientID} column, not
     * {@code Patients.PatientID}; see schema.sql).
     */
    @Transactional
    public void deletePatient(int loginId) {
        patientRepository.deleteAppointmentsByPatientLoginId(loginId);
        patientRepository.deleteTreatmentsByPatientLoginId(loginId);
        patientRepository.deleteByLogin_LoginId(loginId);
        loginHistoryRepository.deleteByLoginId(loginId);
        loginRepository.deleteById(loginId);
    }

    /**
     * Equivalent of {@code PatientDAO.updatePassword}: re-hashes with a
     * fresh salt and updates the existing {@code Login} row in place.
     * Unlike the legacy DAO - which re-selects by username+new-password
     * afterward just to confirm the row still exists, and loses the
     * logged-in patient's LoginID from the returned object in the process
     * (it only sets username/password on the result, not loginID) - this
     * reuses the already-loaded {@code Login} entity directly, so no
     * redundant query and no dropped LoginID.
     */
    @Transactional
    public Optional<com.onlinehealthsystem.Patient> updatePassword(String userName, String newRawPassword) {
        Optional<Login> loginOpt = loginRepository.findByUserName(userName);
        if (loginOpt.isEmpty()) {
            return Optional.empty();
        }

        Login login = loginOpt.get();
        String salt = PasswordEncoder.getSaltvalue(SALT_LENGTH);
        login.setUserPassword(PasswordEncoder.generateSecurePassword(newRawPassword, salt));
        login.setSalt(salt);
        loginRepository.save(login);

        return Optional.of(toLegacyLoginResult(login));
    }

    /** Equivalent of {@code PatientDAO.listPatients}'s joined Login+Patients listing. */
    public List<com.onlinehealthsystem.Patient> listPatients() {
        return patientRepository.findAllWithLogin().stream()
                .map(this::toLegacyProfile)
                .toList();
    }

    /** Equivalent of {@code PatientDAO.registerLogout}. */
    @Transactional
    public void registerLogout(int loginId) {
        loginHistoryRepository.closeOpenSession(loginId, LocalDateTime.now());
    }

    /**
     * Deliberately leaves the hashed password/salt off the returned object -
     * unlike the legacy DAO, which copied both into the session-bound
     * {@code Patient}. Nothing server-side reads them back off the session
     * (confirmed: {@code changepwd.jsp}'s "current-password" hidden field is
     * never read by this controller), so carrying them along only exposed
     * the PBKDF2 hash in page source for no functional reason.
     */
    private com.onlinehealthsystem.Patient toLegacyLoginResult(Login login) {
        com.onlinehealthsystem.Patient patient = new com.onlinehealthsystem.Patient();
        patient.setLoginID(login.getLoginId());
        patient.setUserName(login.getUserName());
        return patient;
    }

    private com.onlinehealthsystem.Patient toLegacyProfile(Patient profile) {
        return new com.onlinehealthsystem.Patient(
                profile.getLogin().getLoginId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getLogin().getUserName(),
                profile.getNationalId(),
                profile.getDateOfBirth() == null ? null : Date.valueOf(profile.getDateOfBirth()),
                profile.getSex(),
                profile.getAddress(),
                profile.getCity(),
                profile.getWeight() == null ? 0 : profile.getWeight(),
                profile.getHeight() == null ? 0 : profile.getHeight());
    }
}
