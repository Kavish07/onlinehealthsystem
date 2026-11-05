package com.system.OnlineHealthSystem.service;

import com.system.OnlineHealthSystem.entity.Bill;
import com.system.OnlineHealthSystem.entity.Department;
import com.system.OnlineHealthSystem.entity.Doctor;
import com.system.OnlineHealthSystem.entity.Login;
import com.system.OnlineHealthSystem.entity.Medicine;
import com.system.OnlineHealthSystem.entity.Patient;
import com.system.OnlineHealthSystem.repository.BillRepository;
import com.system.OnlineHealthSystem.repository.DoctorRepository;
import com.system.OnlineHealthSystem.repository.MedicineRepository;
import com.system.OnlineHealthSystem.repository.PatientRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TreatmentServiceTest {

    @Mock
    private TreatmentRepository treatmentRepository;

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private BillRepository billRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    private TreatmentService treatmentService;

    @BeforeEach
    void setUp() {
        treatmentService = new TreatmentService(treatmentRepository, medicineRepository, billRepository,
                patientRepository, doctorRepository);
    }

    private com.system.OnlineHealthSystem.entity.Treatment treatmentEntity(int patientLoginId, int doctorLoginId,
            String departmentCode, String departmentName, String medicineCode, String medicineName) {
        com.system.OnlineHealthSystem.entity.Treatment treatment = new com.system.OnlineHealthSystem.entity.Treatment();
        treatment.setTreatmentId(99);
        treatment.setTreatmentDate(LocalDate.of(2026, 1, 15));
        treatment.setLocation("Room 1");
        Department department = new Department();
        department.setDepartmentCode(departmentCode);
        department.setDepartmentName(departmentName);
        treatment.setDepartment(department);
        treatment.setTreatmentName("Checkup");
        Medicine medicine = new Medicine();
        medicine.setMedicineCode(medicineCode);
        medicine.setMedicineName(medicineName);
        treatment.setMedicine(medicine);
        Login patientLogin = new Login();
        patientLogin.setLoginId(patientLoginId);
        treatment.setPatientLogin(patientLogin);
        Login doctorLogin = new Login();
        doctorLogin.setLoginId(doctorLoginId);
        treatment.setDoctorLogin(doctorLogin);
        Bill bill = new Bill();
        bill.setBillId(55);
        bill.setBillDate(LocalDate.of(2026, 1, 15));
        bill.setAmount(100);
        treatment.setBill(bill);
        return treatment;
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
    void registerTreatment_savesANewTreatmentWithItsBill_andResolvesTheMedicineCodeByName() {
        when(medicineRepository.findByMedicineName("Paracetamol"))
                .thenReturn(Optional.of(medicineWithCode("MED1", "Paracetamol")));

        treatmentService.registerTreatment(LocalDate.of(2026, 1, 15), "Room 1", "CARD", "Checkup", "Paracetamol",
                7, 42, 100, LocalDate.of(2026, 1, 15));

        ArgumentCaptor<com.system.OnlineHealthSystem.entity.Treatment> captor =
                ArgumentCaptor.forClass(com.system.OnlineHealthSystem.entity.Treatment.class);
        verify(treatmentRepository).save(captor.capture());
        com.system.OnlineHealthSystem.entity.Treatment saved = captor.getValue();
        assertThat(saved.getTreatmentDate()).isEqualTo(LocalDate.of(2026, 1, 15));
        assertThat(saved.getLocation()).isEqualTo("Room 1");
        assertThat(saved.getDepartment().getDepartmentCode()).isEqualTo("CARD");
        assertThat(saved.getTreatmentName()).isEqualTo("Checkup");
        assertThat(saved.getMedicine().getMedicineCode()).isEqualTo("MED1");
        assertThat(saved.getPatientLogin().getLoginId()).isEqualTo(7);
        assertThat(saved.getDoctorLogin().getLoginId()).isEqualTo(42);
        assertThat(saved.getBill().getAmount()).isEqualTo(100);
        assertThat(saved.getBill().getBillDate()).isEqualTo(LocalDate.of(2026, 1, 15));
    }

    @Test
    void registerTreatment_fallsBackToAnEmptyStringMedicineCode_whenNoMedicineMatchesTheName() {
        // Matches the legacy TreatmentDAO.registerTreatment's fallback: an
        // unmatched medicine name leaves MedicineCode as "" (not null).
        when(medicineRepository.findByMedicineName("Unknown")).thenReturn(Optional.empty());

        treatmentService.registerTreatment(LocalDate.of(2026, 1, 15), "Room 1", "CARD", "Checkup", "Unknown",
                7, 42, 100, LocalDate.of(2026, 1, 15));

        ArgumentCaptor<com.system.OnlineHealthSystem.entity.Treatment> captor =
                ArgumentCaptor.forClass(com.system.OnlineHealthSystem.entity.Treatment.class);
        verify(treatmentRepository).save(captor.capture());
        assertThat(captor.getValue().getMedicine().getMedicineCode()).isEmpty();
    }

    private Medicine medicineWithCode(String code, String name) {
        Medicine medicine = new Medicine();
        medicine.setMedicineCode(code);
        medicine.setMedicineName(name);
        return medicine;
    }

    @Test
    void updateTreatment_updatesTreatmentFields_andMedicineCode_whenTreatmentExists() {
        com.system.OnlineHealthSystem.entity.Treatment existing = treatmentEntity(7, 42, "CARD", "Cardiology",
                "MED1", "Paracetamol");
        when(treatmentRepository.findById(99)).thenReturn(Optional.of(existing));
        when(medicineRepository.findByMedicineName("Ibuprofen"))
                .thenReturn(Optional.of(medicineWithCode("MED2", "Ibuprofen")));
        when(billRepository.findById(55)).thenReturn(Optional.of(existing.getBill()));

        treatmentService.updateTreatment(99, LocalDate.of(2026, 2, 1), "Room 2", "ORTH", "Follow-up", "Ibuprofen",
                55, 200, LocalDate.of(2026, 2, 1), false);

        assertThat(existing.getTreatmentDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(existing.getLocation()).isEqualTo("Room 2");
        assertThat(existing.getDepartment().getDepartmentCode()).isEqualTo("ORTH");
        assertThat(existing.getTreatmentName()).isEqualTo("Follow-up");
        assertThat(existing.getMedicine().getMedicineCode()).isEqualTo("MED2");
        verify(treatmentRepository).save(existing);
    }

    @Test
    void updateTreatment_doesNothingToTheTreatment_whenTreatmentDoesNotExist() {
        when(treatmentRepository.findById(99)).thenReturn(Optional.empty());
        when(billRepository.findById(55)).thenReturn(Optional.empty());

        treatmentService.updateTreatment(99, LocalDate.of(2026, 2, 1), "Room 2", "ORTH", "Follow-up", "Ibuprofen",
                55, 200, LocalDate.of(2026, 2, 1), false);

        verify(treatmentRepository, never()).save(any());
    }

    @Test
    void updateTreatment_updatesTheBillByItsOwnId() {
        com.system.OnlineHealthSystem.entity.Treatment existing = treatmentEntity(7, 42, "CARD", "Cardiology",
                "MED1", "Paracetamol");
        when(treatmentRepository.findById(99)).thenReturn(Optional.of(existing));
        when(medicineRepository.findByMedicineName("Paracetamol"))
                .thenReturn(Optional.of(medicineWithCode("MED1", "Paracetamol")));
        Bill bill = existing.getBill();
        when(billRepository.findById(55)).thenReturn(Optional.of(bill));

        treatmentService.updateTreatment(99, LocalDate.of(2026, 2, 1), "Room 2", "CARD", "Follow-up", "Paracetamol",
                55, 200, LocalDate.of(2026, 2, 1), false);

        assertThat(bill.getAmount()).isEqualTo(200);
        assertThat(bill.getBillDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        verify(billRepository).save(bill);
    }

    @Test
    void updateTreatment_doesNothingToTheBill_whenBillIdDoesNotMatchAnyBill() {
        com.system.OnlineHealthSystem.entity.Treatment existing = treatmentEntity(7, 42, "CARD", "Cardiology",
                "MED1", "Paracetamol");
        when(treatmentRepository.findById(99)).thenReturn(Optional.of(existing));
        when(medicineRepository.findByMedicineName("Paracetamol"))
                .thenReturn(Optional.of(medicineWithCode("MED1", "Paracetamol")));
        when(billRepository.findById(999)).thenReturn(Optional.empty());

        treatmentService.updateTreatment(99, LocalDate.of(2026, 2, 1), "Room 2", "CARD", "Follow-up", "Paracetamol",
                999, 200, LocalDate.of(2026, 2, 1), false);

        verify(billRepository, never()).save(any());
    }

    @Test
    void updateTreatment_marksTheBillPaid_whenPaidIsTrue() {
        Bill bill = new Bill();
        when(treatmentRepository.findById(99)).thenReturn(Optional.empty());
        when(billRepository.findById(55)).thenReturn(Optional.of(bill));

        treatmentService.updateTreatment(99, LocalDate.of(2026, 2, 1), "Room 2", "CARD", "Follow-up", "Paracetamol",
                55, 200, LocalDate.of(2026, 2, 1), true);

        assertThat(bill.isPaid()).isTrue();
    }

    @Test
    void updateTreatment_neverUnpaysAnAlreadyPaidBill_whenPaidIsFalse() {
        Bill bill = new Bill();
        bill.setPaid(true);
        when(treatmentRepository.findById(99)).thenReturn(Optional.empty());
        when(billRepository.findById(55)).thenReturn(Optional.of(bill));

        treatmentService.updateTreatment(99, LocalDate.of(2026, 2, 1), "Room 2", "CARD", "Follow-up", "Paracetamol",
                55, 200, LocalDate.of(2026, 2, 1), false);

        assertThat(bill.isPaid()).isTrue();
    }

    @Test
    void listTreatments_mapsJoinedRowsToLegacyTreatmentObjects_usingPatientsAndDoctorsSurrogateIds() {
        com.system.OnlineHealthSystem.entity.Treatment entity = treatmentEntity(7, 42, "CARD", "Cardiology",
                "MED1", "Paracetamol");
        when(treatmentRepository.findByPatientLoginId(7)).thenReturn(List.of(entity));
        when(patientRepository.findByLogin_LoginId(7)).thenReturn(Optional.of(patientProfile(7, 3, "Jane", "Doe")));
        when(doctorRepository.findByLogin_LoginId(42)).thenReturn(Optional.of(doctorProfile(42, 5, "John", "Smith")));

        List<com.onlinehealthsystem.Treatment> result = treatmentService.listTreatments(7);

        assertThat(result).hasSize(1);
        com.onlinehealthsystem.Treatment legacy = result.get(0);
        assertThat(legacy.getTreatmentID()).isEqualTo(99);
        assertThat(legacy.getTreatmentDate()).isEqualTo(java.sql.Date.valueOf(LocalDate.of(2026, 1, 15)));
        assertThat(legacy.getLocation()).isEqualTo("Room 1");
        assertThat(legacy.getDepartmentCode()).isEqualTo("CARD");
        assertThat(legacy.getDepartmentName()).isEqualTo("Cardiology");
        assertThat(legacy.getTreatmentName()).isEqualTo("Checkup");
        assertThat(legacy.getMedicineCode()).isEqualTo("MED1");
        assertThat(legacy.getMedicineName()).isEqualTo("Paracetamol");
        // Treatment's legacy patientID/doctorID are the Patients/Doctors
        // surrogate keys (p_PatientID/d_DoctorID in the legacy readTreatment
        // helper), not the Login.LoginID that Treatments.PatientID/DoctorID
        // actually stores - matches AppointmentService.listAppointments's
        // identical mapping.
        assertThat(legacy.getPatientID()).isEqualTo(3);
        assertThat(legacy.getPatientName()).isEqualTo("Jane Doe");
        assertThat(legacy.getDoctorID()).isEqualTo(5);
        assertThat(legacy.getDoctorName()).isEqualTo("John Smith");
        assertThat(legacy.getBillID()).isEqualTo(55);
        assertThat(legacy.getBillAmount()).isEqualTo(100);
        assertThat(legacy.getBillDate()).isEqualTo(java.sql.Date.valueOf(LocalDate.of(2026, 1, 15)));
    }

    @Test
    void listTreatments_omitsRows_whenPatientOrDoctorProfileIsMissing() {
        com.system.OnlineHealthSystem.entity.Treatment entity = treatmentEntity(7, 42, "CARD", "Cardiology",
                "MED1", "Paracetamol");
        when(treatmentRepository.findByPatientLoginId(7)).thenReturn(List.of(entity));
        when(patientRepository.findByLogin_LoginId(7)).thenReturn(Optional.empty());

        List<com.onlinehealthsystem.Treatment> result = treatmentService.listTreatments(7);

        assertThat(result).isEmpty();
    }

    @Test
    void listTreatments_returnsEmpty_whenPatientHasNoTreatments() {
        when(treatmentRepository.findByPatientLoginId(7)).thenReturn(List.of());

        assertThat(treatmentService.listTreatments(7)).isEmpty();
    }

    @Test
    void listAllTreatments_includesTreatmentsForEveryPatient() {
        com.system.OnlineHealthSystem.entity.Treatment entity1 = treatmentEntity(7, 42, "CARD", "Cardiology",
                "MED1", "Paracetamol");
        com.system.OnlineHealthSystem.entity.Treatment entity2 = treatmentEntity(8, 42, "CARD", "Cardiology",
                "MED1", "Paracetamol");
        when(treatmentRepository.findAllWithAssociations()).thenReturn(List.of(entity1, entity2));
        when(patientRepository.findByLogin_LoginId(7)).thenReturn(Optional.of(patientProfile(7, 3, "Jane", "Doe")));
        when(patientRepository.findByLogin_LoginId(8)).thenReturn(Optional.of(patientProfile(8, 4, "John", "Doe")));
        when(doctorRepository.findByLogin_LoginId(42)).thenReturn(Optional.of(doctorProfile(42, 5, "John", "Smith")));

        assertThat(treatmentService.listAllTreatments()).hasSize(2);
    }
}
