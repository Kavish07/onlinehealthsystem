package com.system.OnlineHealthSystem.service;

import com.onlinehealthsystem.AppConstants;
import com.system.OnlineHealthSystem.entity.Appointment;
import com.system.OnlineHealthSystem.entity.Department;
import com.system.OnlineHealthSystem.entity.Doctor;
import com.system.OnlineHealthSystem.entity.Login;
import com.system.OnlineHealthSystem.entity.Patient;
import com.system.OnlineHealthSystem.repository.AppointmentRepository;
import com.system.OnlineHealthSystem.repository.DoctorRepository;
import com.system.OnlineHealthSystem.repository.LoginRepository;
import com.system.OnlineHealthSystem.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Replacement for {@code com.onlinehealthsystem.AppointmentDAO}'s business
 * logic. Returns/accepts the legacy {@code com.onlinehealthsystem.Appointment}
 * POJO from {@link #listAppointments()}/{@link #checkAppointment(String)}
 * because that's exactly the type the "listAppointments" session/request
 * attribute holds and {@code appointments2.jsp}/{@code appointments3.jsp}
 * read via EL - introducing a different type here would break those pages.
 * New code should otherwise prefer the
 * {@code com.system.OnlineHealthSystem.entity} types.
 *
 * <p>Unlike {@code AppointmentDAO.listAppointments()}'s single five-table
 * cross-join query, this resolves each appointment's patient/doctor profile
 * via a separate {@link PatientRepository}/{@link DoctorRepository} lookup
 * per row. That's an N+1 query pattern rather than one big join - a
 * deliberate simplicity trade-off for this app's scale (a single clinic's
 * worth of appointments), and it avoids having to either bolt a
 * {@code Patient}/{@code Doctor} association directly onto this entity
 * (which would be semantically wrong - see {@link Appointment}'s javadoc on
 * why {@code PatientID}/{@code DoctorID} map to {@link Login}, not to those
 * profile tables) or write a brittle multi-entity native query. Revisit with
 * a batched lookup (e.g. {@code findAllWithLogin()} on both repositories) if
 * this ever becomes a hot path.
 */
@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final LoginRepository loginRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, PatientRepository patientRepository,
            DoctorRepository doctorRepository, LoginRepository loginRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.loginRepository = loginRepository;
    }

    /**
     * Equivalent of {@code AppointmentDAO.registerAppointment}: always
     * created unapproved, same as the legacy insert's hardcoded
     * {@code Approved = false}. {@code patientLoginId}/{@code doctorLoginId}
     * are {@code Login.LoginID} values (the session-held patient's/doctor's
     * identity), not {@code Patients.PatientID}/{@code Doctors.DoctorID} -
     * see {@link Appointment}'s javadoc. A null/blank {@code departmentCode}
     * leaves the association unset, so the FK column is written as NULL,
     * matching the legacy DAO's plain pass-through of whatever the request
     * parameter held.
     *
     * <p>Rejects a {@code dateTime} outside the clinic's opening hours
     * ({@link AppConstants#CLINIC_OPENING_TIME}-{@link AppConstants#CLINIC_CLOSING_TIME}),
     * on a Sunday, or on a date in {@link AppConstants#PUBLIC_HOLIDAYS} by
     * throwing {@link IllegalArgumentException}, rather than saving an
     * appointment nobody at the clinic can attend.
     */
    @Transactional
    public void registerAppointment(LocalDateTime dateTime, String location, String departmentCode,
            int patientLoginId, int doctorLoginId) {
        validateClinicHours(dateTime);

        Appointment appointment = new Appointment();
        appointment.setAppointmentDate(dateTime);
        appointment.setLocation(location);

        if (departmentCode != null) {
            Department department = new Department();
            department.setDepartmentCode(departmentCode);
            appointment.setDepartment(department);
        }

        Login patientLogin = new Login();
        patientLogin.setLoginId(patientLoginId);
        appointment.setPatientLogin(patientLogin);

        Login doctorLogin = new Login();
        doctorLogin.setLoginId(doctorLoginId);
        appointment.setDoctorLogin(doctorLogin);

        appointment.setApproved(false);
        appointmentRepository.save(appointment);
    }

    private void validateClinicHours(LocalDateTime dateTime) {
        if (dateTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("The clinic is closed on Sundays");
        }
        if (AppConstants.PUBLIC_HOLIDAYS.contains(dateTime.toLocalDate())) {
            throw new IllegalArgumentException("The clinic is closed on public holidays");
        }
        if (dateTime.toLocalTime().isBefore(AppConstants.CLINIC_OPENING_TIME)
                || !dateTime.toLocalTime().isBefore(AppConstants.CLINIC_CLOSING_TIME)) {
            throw new IllegalArgumentException("Appointments must be between " + AppConstants.CLINIC_OPENING_TIME
                    + " and " + AppConstants.CLINIC_CLOSING_TIME);
        }
    }

    /**
     * Equivalent of {@code AppointmentDAO.confirmAppointment}: silently does
     * nothing if the id doesn't match any appointment, same as the legacy
     * {@code UPDATE ... WHERE AppointmentId = ?} affecting zero rows.
     */
    @Transactional
    public void confirmAppointment(int appointmentId) {
        appointmentRepository.findById(appointmentId).ifPresent(appointment -> {
            appointment.setApproved(true);
            appointmentRepository.save(appointment);
        });
    }

    /**
     * Equivalent of {@code AppointmentDAO.listAppointments()}. Drops any
     * appointment whose department, patient profile, or doctor profile
     * can't be resolved, matching the legacy query's inner-join semantics
     * across all of {@code Departments}/{@code Patients}/{@code Doctors}.
     * Patient/doctor names are joined with a space, matching the legacy
     * method's own concatenation (see {@link #checkAppointment(String)}
     * for the other, space-less variant it doesn't share).
     */
    public List<com.onlinehealthsystem.Appointment> listAppointments() {
        return appointmentRepository.findAllWithAssociations().stream()
                .map(this::toLegacyListing)
                .filter(Objects::nonNull)
                .toList();
    }

    private com.onlinehealthsystem.Appointment toLegacyListing(Appointment appointment) {
        Optional<Patient> patientOpt = patientRepository.findByLogin_LoginId(appointment.getPatientLogin().getLoginId());
        Optional<Doctor> doctorOpt = doctorRepository.findByLogin_LoginId(appointment.getDoctorLogin().getLoginId());

        if (patientOpt.isEmpty() || doctorOpt.isEmpty()) {
            return null;
        }

        Patient patient = patientOpt.get();
        Doctor doctor = doctorOpt.get();

        return new com.onlinehealthsystem.Appointment(
                appointment.getAppointmentId(),
                Timestamp.valueOf(appointment.getAppointmentDate()),
                appointment.getLocation(),
                appointment.getDepartment().getDepartmentCode(),
                appointment.getDepartment().getDepartmentName(),
                patient.getPatientId(),
                patient.getFirstName() + " " + patient.getLastName(),
                doctor.getDoctorId(),
                doctor.getFirstName() + " " + doctor.getLastName(),
                appointment.getApproved());
    }

    /**
     * Equivalent of {@code AppointmentDAO.checkAppointment}: returns null
     * (rather than throwing or returning an empty-but-non-null Appointment)
     * when the username doesn't exist or has no appointment - preserving
     * the null-on-no-match fix already made to the legacy DAO this
     * session. Not currently called from any migrated or legacy
     * controller/servlet (the legacy method was already dead code - no
     * Servlet called it either); kept for behavior parity and its own test
     * coverage.
     *
     * <p>Unlike {@link #listAppointments()}, this matches the legacy
     * method's own (distinct, pre-existing) quirks: patient/doctor names
     * are concatenated without a space, and the returned object's
     * {@code AppointmentID} is left unset (0) - the legacy
     * {@code checkAppointment} never calls {@code setAppointmentID}.
     * Preserved as-is rather than "fixed" to match {@code listAppointments}
     * mid-migration.
     */
    public com.onlinehealthsystem.Appointment checkAppointment(String userName) {
        Optional<Login> loginOpt = loginRepository.findByUserName(userName);
        if (loginOpt.isEmpty()) {
            return null;
        }

        List<Appointment> matches = appointmentRepository.findByPatientLoginId(loginOpt.get().getLoginId());
        if (matches.isEmpty()) {
            return null;
        }

        Appointment appointment = matches.get(0);
        Optional<Patient> patientOpt = patientRepository.findByLogin_LoginId(appointment.getPatientLogin().getLoginId());
        Optional<Doctor> doctorOpt = doctorRepository.findByLogin_LoginId(appointment.getDoctorLogin().getLoginId());

        if (patientOpt.isEmpty() || doctorOpt.isEmpty()) {
            return null;
        }

        Patient patient = patientOpt.get();
        Doctor doctor = doctorOpt.get();

        com.onlinehealthsystem.Appointment legacy = new com.onlinehealthsystem.Appointment();
        legacy.setAppointmentDate(Timestamp.valueOf(appointment.getAppointmentDate()));
        legacy.setLocation(appointment.getLocation());
        legacy.setDepartmentName(appointment.getDepartment().getDepartmentName());
        legacy.setDepartmentCode(appointment.getDepartment().getDepartmentCode());
        legacy.setPatientID(patient.getPatientId());
        legacy.setPatientName(patient.getFirstName() + patient.getLastName());
        legacy.setDoctorID(doctor.getDoctorId());
        legacy.setDoctorName(doctor.getFirstName() + doctor.getLastName());
        legacy.setApproved(appointment.getApproved());
        return legacy;
    }
}
