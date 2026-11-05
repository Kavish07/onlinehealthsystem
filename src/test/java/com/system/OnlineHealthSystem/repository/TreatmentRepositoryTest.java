package com.system.OnlineHealthSystem.repository;

import com.system.OnlineHealthSystem.entity.Bill;
import com.system.OnlineHealthSystem.entity.Department;
import com.system.OnlineHealthSystem.entity.Login;
import com.system.OnlineHealthSystem.entity.Medicine;
import com.system.OnlineHealthSystem.entity.Treatment;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TreatmentRepositoryTest {

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private TreatmentRepository treatmentRepository;

    @Autowired
    private EntityManager entityManager;

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

    private Medicine savedMedicine(String code, String name) {
        Medicine medicine = new Medicine();
        medicine.setMedicineCode(code);
        medicine.setMedicineName(name);
        entityManager.persist(medicine);
        return medicine;
    }

    private Treatment treatmentFor(Login patientLogin, Login doctorLogin, Department department, Medicine medicine) {
        Treatment treatment = new Treatment();
        treatment.setTreatmentDate(LocalDate.of(2026, 1, 15));
        treatment.setLocation("Room 1");
        treatment.setDepartment(department);
        treatment.setTreatmentName("Checkup");
        treatment.setMedicine(medicine);
        treatment.setPatientLogin(patientLogin);
        treatment.setDoctorLogin(doctorLogin);
        Bill bill = new Bill();
        bill.setBillDate(LocalDate.of(2026, 1, 15));
        bill.setAmount(100);
        treatment.setBill(bill);
        return treatment;
    }

    @Test
    void savingATreatmentWithATransientBill_cascadesTheBillInsert_andPopulatesItsGeneratedId() {
        Login patientLogin = savedLogin("jane.doe");
        Login doctorLogin = savedLogin("john.smith");
        Department department = savedDepartment("CARD", "Cardiology");
        Medicine medicine = savedMedicine("MED1", "Paracetamol");

        Treatment saved = treatmentRepository.save(
                treatmentFor(patientLogin, doctorLogin, department, medicine));

        assertThat(saved.getBill().getBillId()).isNotNull();
    }

    @Test
    void findByPatientLoginId_joinsDepartmentMedicinePatientLoginDoctorLoginAndBill() {
        Login patientLogin = savedLogin("jane.doe");
        Login doctorLogin = savedLogin("john.smith");
        Department department = savedDepartment("CARD", "Cardiology");
        Medicine medicine = savedMedicine("MED1", "Paracetamol");
        treatmentRepository.save(treatmentFor(patientLogin, doctorLogin, department, medicine));

        var found = treatmentRepository.findByPatientLoginId(patientLogin.getLoginId());

        assertThat(found).hasSize(1);
        Treatment treatment = found.get(0);
        assertThat(treatment.getLocation()).isEqualTo("Room 1");
        assertThat(treatment.getDepartment().getDepartmentName()).isEqualTo("Cardiology");
        assertThat(treatment.getMedicine().getMedicineName()).isEqualTo("Paracetamol");
        assertThat(treatment.getPatientLogin().getLoginId()).isEqualTo(patientLogin.getLoginId());
        assertThat(treatment.getDoctorLogin().getLoginId()).isEqualTo(doctorLogin.getLoginId());
        assertThat(treatment.getBill().getAmount()).isEqualTo(100);
    }

    @Test
    void findByPatientLoginId_omitsTreatmentsWithoutAMatchingDepartment() {
        // Mirrors the legacy TreatmentDAO.listTreatments()'s inner-join
        // behavior across all five joined tables: a treatment row whose
        // DepartmentCode doesn't match any Departments row is silently
        // dropped from the listing.
        Login patientLogin = savedLogin("jane.doe");
        Login doctorLogin = savedLogin("john.smith");
        Medicine medicine = savedMedicine("MED1", "Paracetamol");
        Treatment treatment = treatmentFor(patientLogin, doctorLogin, null, medicine);
        treatmentRepository.save(treatment);

        var found = treatmentRepository.findByPatientLoginId(patientLogin.getLoginId());

        assertThat(found).isEmpty();
    }

    @Test
    void findByPatientLoginId_omitsTreatmentsWithoutAMatchingMedicine() {
        Login patientLogin = savedLogin("jane.doe");
        Login doctorLogin = savedLogin("john.smith");
        Department department = savedDepartment("CARD", "Cardiology");
        Treatment treatment = treatmentFor(patientLogin, doctorLogin, department, null);
        treatmentRepository.save(treatment);

        var found = treatmentRepository.findByPatientLoginId(patientLogin.getLoginId());

        assertThat(found).isEmpty();
    }

    @Test
    void findByPatientLoginId_returnsOnlyThatPatientsTreatments() {
        Login patientLogin = savedLogin("jane.doe");
        Login otherPatientLogin = savedLogin("other.patient");
        Login doctorLogin = savedLogin("john.smith");
        Department department = savedDepartment("CARD", "Cardiology");
        Medicine medicine = savedMedicine("MED1", "Paracetamol");
        treatmentRepository.save(treatmentFor(patientLogin, doctorLogin, department, medicine));
        treatmentRepository.save(treatmentFor(otherPatientLogin, doctorLogin, department, medicine));

        var found = treatmentRepository.findByPatientLoginId(patientLogin.getLoginId());

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getPatientLogin().getLoginId()).isEqualTo(patientLogin.getLoginId());
    }

    @Test
    void findAllWithAssociations_includesTreatmentsForEveryPatient() {
        Login patientLogin = savedLogin("jane.doe");
        Login otherPatientLogin = savedLogin("other.patient");
        Login doctorLogin = savedLogin("john.smith");
        Department department = savedDepartment("CARD", "Cardiology");
        Medicine medicine = savedMedicine("MED1", "Paracetamol");
        treatmentRepository.save(treatmentFor(patientLogin, doctorLogin, department, medicine));
        treatmentRepository.save(treatmentFor(otherPatientLogin, doctorLogin, department, medicine));

        assertThat(treatmentRepository.findAllWithAssociations()).hasSize(2);
    }

    @Test
    void deleteByPatientLoginId_removesOnlyThatPatientsTreatments() {
        Login patientLogin = savedLogin("jane.doe");
        Login otherPatientLogin = savedLogin("other.patient");
        Login doctorLogin = savedLogin("john.smith");
        Department department = savedDepartment("CARD", "Cardiology");
        Medicine medicine = savedMedicine("MED1", "Paracetamol");
        treatmentRepository.save(treatmentFor(patientLogin, doctorLogin, department, medicine));
        treatmentRepository.save(treatmentFor(otherPatientLogin, doctorLogin, department, medicine));

        treatmentRepository.deleteByPatientLoginId(patientLogin.getLoginId());

        assertThat(treatmentRepository.findAllWithAssociations()).hasSize(1);
        assertThat(treatmentRepository.findAllWithAssociations().get(0).getPatientLogin().getLoginId())
                .isEqualTo(otherPatientLogin.getLoginId());
    }

    @Test
    void deleteByDoctorLoginId_removesOnlyThatDoctorsTreatments() {
        Login patientLogin = savedLogin("jane.doe");
        Login doctorLogin = savedLogin("john.smith");
        Login otherDoctorLogin = savedLogin("other.doctor");
        Department department = savedDepartment("CARD", "Cardiology");
        Medicine medicine = savedMedicine("MED1", "Paracetamol");
        treatmentRepository.save(treatmentFor(patientLogin, doctorLogin, department, medicine));
        treatmentRepository.save(treatmentFor(patientLogin, otherDoctorLogin, department, medicine));

        treatmentRepository.deleteByDoctorLoginId(doctorLogin.getLoginId());

        assertThat(treatmentRepository.findAllWithAssociations()).hasSize(1);
        assertThat(treatmentRepository.findAllWithAssociations().get(0).getDoctorLogin().getLoginId())
                .isEqualTo(otherDoctorLogin.getLoginId());
    }
}
