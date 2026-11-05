package com.system.OnlineHealthSystem.service;

import com.onlinehealthsystem.AppConstants;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Replacement for {@code com.onlinehealthsystem.DoctorDAO}'s business
 * logic - mirrors {@code PatientService}'s shape exactly. Returns/accepts
 * the legacy {@code com.onlinehealthsystem.Doctor} POJO for the
 * single-record operations (login, register, update password) and for
 * {@link #listDoctors()} because that's exactly the type still placed in
 * the HttpSession's "doctor" attribute and read by not-yet-migrated legacy
 * servlets/JSPs (several still do a hard
 * {@code (Doctor) session.getAttribute("doctor")} cast, e.g.
 * {@code ListAppointmentsServlet}, {@code ConfirmAppointmentServlet},
 * {@code NewTreatmentServlet}, {@code UpdateTreatmentServlet}) - introducing
 * a different type here would break them. New code should otherwise prefer
 * the {@code com.system.OnlineHealthSystem.entity} types, which is what the
 * JPA repositories themselves deal in.
 */
@Service
public class DoctorService {

    private static final int SALT_LENGTH = 30;

    private final LoginRepository loginRepository;
    private final DoctorRepository doctorRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final AppointmentRepository appointmentRepository;
    private final TreatmentRepository treatmentRepository;

    public DoctorService(LoginRepository loginRepository, DoctorRepository doctorRepository,
            LoginHistoryRepository loginHistoryRepository, AppointmentRepository appointmentRepository,
            TreatmentRepository treatmentRepository) {
        this.loginRepository = loginRepository;
        this.doctorRepository = doctorRepository;
        this.loginHistoryRepository = loginHistoryRepository;
        this.appointmentRepository = appointmentRepository;
        this.treatmentRepository = treatmentRepository;
    }

    /**
     * Equivalent of {@code DoctorDAO.checkLogin}: verifies the password
     * with {@link PasswordEncoder}'s constant-time comparison and records a
     * LoginHistory row on success. Returns {@link Optional#empty()} rather
     * than null for both "no such username" and "wrong password" - the
     * legacy checkLogin's unknown-username path happened to already return
     * null correctly here (unlike PatientDAO.checkLogin's old NPE), but
     * this keeps both outcomes handled the same, explicit way.
     */
    @Transactional
    public Optional<com.onlinehealthsystem.Doctor> login(String userName, String rawPassword) {
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

    /** Equivalent of {@code DoctorDAO.checkRegistration}'s existence check. */
    public boolean isUsernameTaken(String userName) {
        return loginRepository.existsByUserName(userName);
    }

    /**
     * Equivalent of {@code DoctorDAO.registerDoctor}: hashes+salts the
     * password, inserts the {@code Login} row, then the {@code Doctors}
     * profile row pointing at it. Unlike the legacy DAO - which already
     * fixed its own race condition this session by switching to an insert
     * ... returning LoginID - this gets the same race-free guarantee for
     * free from JPA populating the generated id directly from the insert.
     */
    @Transactional
    public com.onlinehealthsystem.Doctor register(String userName, String rawPassword, String firstName,
            String lastName, String nationalId, LocalDate dateOfBirth, String sex, String address, String city,
            int weight, int height, String qualifications, String speciality, String departmentCode) {
        String salt = PasswordEncoder.getSaltvalue(SALT_LENGTH);
        String hashedPassword = PasswordEncoder.generateSecurePassword(rawPassword, salt);

        Login login = new Login();
        login.setUserName(userName);
        login.setUserPassword(hashedPassword);
        login.setSalt(salt);
        login = loginRepository.save(login);

        // No DepartmentRepository lookup needed: Department's id
        // (DepartmentCode) is a natural, caller-supplied key, so a
        // transient instance with just the id set is enough for Hibernate
        // to write the FK column - see Doctor.department's javadoc.
        Department department = new Department();
        department.setDepartmentCode(departmentCode);

        Doctor profile = new Doctor();
        profile.setLogin(login);
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setNationalId(nationalId);
        profile.setDateOfBirth(dateOfBirth);
        profile.setSex(sex);
        profile.setAddress(address);
        profile.setCity(city);
        profile.setWeight(weight);
        profile.setHeight(height);
        profile.setQualifications(qualifications);
        profile.setSpeciality(speciality);
        profile.setDepartment(department);
        doctorRepository.save(profile);

        return toLegacyLoginResult(login);
    }

    /**
     * Equivalent of {@code DoctorDAO.deleteDoctor}'s cascading delete
     * across Appointments, Treatments, Doctors, LoginHistory and Login -
     * all keyed by this doctor's {@code Login.LoginID} (stored directly in
     * Appointments/Treatments' own {@code DoctorID} column, not
     * {@code Doctors.DoctorID}; see schema.sql). The Appointments delete
     * now goes through {@link AppointmentRepository}, same rationale as
     * {@code PatientService.deletePatient()}'s identical change. The
     * Treatments delete made the same move once the Treatment slice gave
     * it a real JPA entity/repository too - see
     * {@code TreatmentRepository.deleteByDoctorLoginId}.
     */
    @Transactional
    public void deleteDoctor(int loginId) {
        appointmentRepository.deleteByDoctorLoginId(loginId);
        treatmentRepository.deleteByDoctorLoginId(loginId);
        doctorRepository.deleteByLogin_LoginId(loginId);
        loginHistoryRepository.deleteByLoginId(loginId);
        loginRepository.deleteById(loginId);
    }

    /**
     * Equivalent of {@code DoctorDAO.updatePassword}: re-hashes with a
     * fresh salt and updates the existing {@code Login} row in place.
     * Unlike the legacy DAO - which re-selects by username+new-password
     * afterward just to confirm the row still exists, and loses the
     * logged-in doctor's LoginID from the returned object in the process
     * (it only sets username/password on the result, not loginID) - this
     * reuses the already-loaded {@code Login} entity directly, so no
     * redundant query and no dropped LoginID.
     */
    @Transactional
    public Optional<com.onlinehealthsystem.Doctor> updatePassword(String userName, String newRawPassword) {
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

    /**
     * Equivalent of {@code DoctorDAO.listDoctors()}'s joined
     * Login+Doctors+Departments listing, excluding the admin account via a
     * bound query parameter (not a string literal baked into the query
     * text) - see {@code DoctorRepository.findAllExcludingUserName}.
     */
    public List<com.onlinehealthsystem.Doctor> listDoctors() {
        return doctorRepository.findAllExcludingUserName(AppConstants.ADMIN_USERNAME).stream()
                .map(this::toLegacyListing)
                .toList();
    }

    /** Equivalent of {@code DoctorDAO.registerLogout}. */
    @Transactional
    public void registerLogout(int loginId) {
        loginHistoryRepository.closeOpenSession(loginId, LocalDateTime.now());
    }

    /**
     * Deliberately leaves the hashed password/salt off the returned object,
     * same rationale as {@code PatientService.toLegacyLoginResult}.
     */
    private com.onlinehealthsystem.Doctor toLegacyLoginResult(Login login) {
        com.onlinehealthsystem.Doctor doctor = new com.onlinehealthsystem.Doctor();
        doctor.setLoginID(login.getLoginId());
        doctor.setUserName(login.getUserName());
        return doctor;
    }

    /**
     * Matches the legacy {@code DoctorDAO.listDoctors()}'s quirk of
     * stuffing the joined {@code Departments.DepartmentName} (not
     * {@code DepartmentCode}) into the returned object's
     * {@code departmentCode} field - preserved as-is rather than "fixed"
     * here, since {@code admin.jsp} already renders whatever that field
     * holds and this migration isn't meant to change that display.
     */
    private com.onlinehealthsystem.Doctor toLegacyListing(Doctor profile) {
        return new com.onlinehealthsystem.Doctor(
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
                profile.getHeight() == null ? 0 : profile.getHeight(),
                profile.getQualifications(),
                profile.getSpeciality(),
                profile.getDepartment() == null ? null : profile.getDepartment().getDepartmentName());
    }
}
