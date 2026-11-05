package com.system.OnlineHealthSystem.service;

import com.system.OnlineHealthSystem.entity.Bill;
import com.system.OnlineHealthSystem.entity.Department;
import com.system.OnlineHealthSystem.entity.Doctor;
import com.system.OnlineHealthSystem.entity.Login;
import com.system.OnlineHealthSystem.entity.Medicine;
import com.system.OnlineHealthSystem.entity.Patient;
import com.system.OnlineHealthSystem.entity.Treatment;
import com.system.OnlineHealthSystem.repository.BillRepository;
import com.system.OnlineHealthSystem.repository.DoctorRepository;
import com.system.OnlineHealthSystem.repository.MedicineRepository;
import com.system.OnlineHealthSystem.repository.PatientRepository;
import com.system.OnlineHealthSystem.repository.TreatmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Replacement for {@code com.onlinehealthsystem.TreatmentDAO}'s business
 * logic. Returns/accepts the legacy {@code com.onlinehealthsystem.Treatment}
 * POJO from {@link #listTreatments(int)}/{@link #listAllTreatments()}
 * because that's exactly the type the "listTreatments"/"listAllTreatments"
 * session/request attributes hold and {@code treatments.jsp}/{@code home.jsp}/
 * {@code home2.jsp} read via EL - introducing a different type here would
 * break those pages, mirroring {@code AppointmentService}'s identical
 * rationale for {@code com.onlinehealthsystem.Appointment}. New code should
 * otherwise prefer the {@code com.system.OnlineHealthSystem.entity} types.
 *
 * <p>Like {@code AppointmentService}, resolves each treatment's patient/
 * doctor profile via a separate {@link PatientRepository}/
 * {@link DoctorRepository} lookup per row (an N+1 query pattern, the same
 * deliberate simplicity trade-off at this app's scale) rather than bolting a
 * {@code Patient}/{@code Doctor} association directly onto the
 * {@link Treatment} entity, which would be semantically wrong - see that
 * entity's javadoc on why {@code PatientID}/{@code DoctorID} map to
 * {@link Login}, not to those profile tables.
 */
@Service
public class TreatmentService {

    private final TreatmentRepository treatmentRepository;
    private final MedicineRepository medicineRepository;
    private final BillRepository billRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public TreatmentService(TreatmentRepository treatmentRepository, MedicineRepository medicineRepository,
            BillRepository billRepository, PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.treatmentRepository = treatmentRepository;
        this.medicineRepository = medicineRepository;
        this.billRepository = billRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    /**
     * Equivalent of {@code TreatmentDAO.registerTreatment}. {@code
     * patientLoginId}/{@code doctorLoginId} are {@code Login.LoginID}
     * values, not {@code Patients.PatientID}/{@code Doctors.DoctorID} - see
     * {@link Treatment}'s javadoc. Saving the new {@link Treatment} with a
     * transient {@link Bill} attached cascades the {@code Bill} insert (see
     * {@link Treatment#getBill()}'s javadoc) - the JPA equivalent of the
     * legacy DAO's already-fixed atomic {@code insert into Bill ...
     * returning BillID}, without the race-prone "select the last-inserted
     * row" pattern it replaced; no race condition is reintroduced here.
     */
    @Transactional
    public void registerTreatment(LocalDate treatmentDate, String location, String departmentCode,
            String treatmentName, String medicineName, int patientLoginId, int doctorLoginId, int billAmount,
            LocalDate billDate) {
        Treatment treatment = new Treatment();
        treatment.setTreatmentDate(treatmentDate);
        treatment.setLocation(location);

        // Legacy TreatmentDAO.registerTreatment passes departmentCode
        // straight through with no null check, unlike AppointmentDAO's
        // equivalent - preserved as-is rather than "fixed" to match that
        // other slice mid-migration.
        Department department = new Department();
        department.setDepartmentCode(departmentCode);
        treatment.setDepartment(department);

        treatment.setTreatmentName(treatmentName);
        treatment.setMedicine(resolveMedicineByName(medicineName));

        Login patientLogin = new Login();
        patientLogin.setLoginId(patientLoginId);
        treatment.setPatientLogin(patientLogin);

        Login doctorLogin = new Login();
        doctorLogin.setLoginId(doctorLoginId);
        treatment.setDoctorLogin(doctorLogin);

        Bill bill = new Bill();
        bill.setBillDate(billDate);
        bill.setAmount(billAmount);
        treatment.setBill(bill);

        treatmentRepository.save(treatment);
    }

    /**
     * Equivalent of {@code TreatmentDAO.updateTreatment}. Note the legacy
     * DAO's own {@code patientID}/{@code doctorID} parameters were never
     * actually used in its update SQL (only {@code treatmentID}/{@code
     * billId} address the rows being updated) - dropped here rather than
     * carried forward as dead parameters. Silently does nothing to the
     * {@link Treatment} row if {@code treatmentId} doesn't match any
     * existing row, and likewise for the {@link Bill} row and {@code
     * billId}, matching the legacy UPDATEs' silent zero-rows-affected
     * behavior on a non-matching id.
     */
    @Transactional
    public void updateTreatment(int treatmentId, LocalDate treatmentDate, String location, String departmentCode,
            String treatmentName, String medicineName, int billId, int billAmount, LocalDate billDate, boolean paid) {
        treatmentRepository.findById(treatmentId).ifPresent(treatment -> {
            treatment.setTreatmentDate(treatmentDate);
            treatment.setLocation(location);

            Department department = new Department();
            department.setDepartmentCode(departmentCode);
            treatment.setDepartment(department);

            treatment.setTreatmentName(treatmentName);
            treatment.setMedicine(resolveMedicineByName(medicineName));

            treatmentRepository.save(treatment);
        });

        billRepository.findById(billId).ifPresent(bill -> {
            bill.setBillDate(billDate);
            bill.setAmount(billAmount);
            // One-directional: a bill can be marked paid, never un-paid, whatever the form sends.
            if (paid) {
                bill.setPaid(true);
            }
            billRepository.save(bill);
        });
    }

    /**
     * Looks up a {@link Medicine} by name - the legacy DAO's "medicines"
     * request parameter actually holds a medicine *name*, despite the
     * confusingly-named local variable in {@code NewTreatmentServlet}/
     * {@code UpdateTreatmentServlet} - falling back to an empty-string
     * {@code MedicineCode}, not null, when no match is found. Preserved
     * exactly: a transient {@link Medicine} with code {@code ""} still
     * writes an empty-string {@code MedicineCode} column value, same as the
     * legacy DAO's default-initialized local variable.
     */
    private Medicine resolveMedicineByName(String medicineName) {
        String medicineCode = medicineRepository.findByMedicineName(medicineName)
                .map(Medicine::getMedicineCode)
                .orElse("");
        Medicine medicine = new Medicine();
        medicine.setMedicineCode(medicineCode);
        return medicine;
    }

    /**
     * Equivalent of {@code TreatmentDAO.listTreatments(int)}. Drops any
     * treatment whose department, medicine, patient profile, or doctor
     * profile can't be resolved, matching the legacy query's inner-join
     * semantics across every joined table (department/medicine/bill are
     * already dropped by {@code TreatmentRepository.findByPatientLoginId}'s
     * own inner `join fetch`es; this adds the same drop for a missing
     * patient/doctor profile).
     */
    public List<com.onlinehealthsystem.Treatment> listTreatments(int patientLoginId) {
        return treatmentRepository.findByPatientLoginId(patientLoginId).stream()
                .map(this::toLegacyTreatment)
                .filter(Objects::nonNull)
                .toList();
    }

    /** Equivalent of {@code TreatmentDAO.listAllTreatments()}. */
    public List<com.onlinehealthsystem.Treatment> listAllTreatments() {
        return treatmentRepository.findAllWithAssociations().stream()
                .map(this::toLegacyTreatment)
                .filter(Objects::nonNull)
                .toList();
    }

    private com.onlinehealthsystem.Treatment toLegacyTreatment(Treatment treatment) {
        Optional<Patient> patientOpt =
                patientRepository.findByLogin_LoginId(treatment.getPatientLogin().getLoginId());
        Optional<Doctor> doctorOpt = doctorRepository.findByLogin_LoginId(treatment.getDoctorLogin().getLoginId());

        if (patientOpt.isEmpty() || doctorOpt.isEmpty()) {
            return null;
        }

        Patient patient = patientOpt.get();
        Doctor doctor = doctorOpt.get();
        Bill bill = treatment.getBill();

        return new com.onlinehealthsystem.Treatment(
                treatment.getTreatmentId(),
                Date.valueOf(treatment.getTreatmentDate()),
                treatment.getLocation(),
                treatment.getDepartment().getDepartmentCode(),
                treatment.getDepartment().getDepartmentName(),
                treatment.getTreatmentName(),
                treatment.getMedicine().getMedicineCode(),
                treatment.getMedicine().getMedicineName(),
                patient.getPatientId(),
                patient.getFirstName() + " " + patient.getLastName(),
                doctor.getDoctorId(),
                doctor.getFirstName() + " " + doctor.getLastName(),
                bill.getBillId(),
                Date.valueOf(bill.getBillDate()),
                bill.getAmount(),
                bill.isPaid());
    }
}
