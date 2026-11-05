package com.system.OnlineHealthSystem.repository;

import com.system.OnlineHealthSystem.entity.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TreatmentRepository extends JpaRepository<Treatment, Integer> {

    /**
     * Equivalent of the legacy {@code select *, ... from Treatments t,
     * Departments de, Patients p, Doctors d, Bill b, Medicines m where
     * t.PatientID = p.LoginID and t.DoctorID = d.LoginID and
     * t.DepartmentCode = de.DepartmentCode and t.BillID = b.BillID and
     * m.MedicineCode = t.MedicineCode and p.LoginID = ?} used by
     * {@code TreatmentDAO.listTreatments()}: an inner `join fetch` against
     * every associated table (so a treatment row missing any of them is
     * silently dropped, matching the legacy query's implicit-inner-join
     * semantics) plus both {@code Login} associations, so callers (see
     * {@code TreatmentService}) don't trigger one extra SELECT per row.
     */
    @Query("select t from Treatment t join fetch t.department join fetch t.medicine join fetch t.patientLogin "
            + "join fetch t.doctorLogin join fetch t.bill where t.patientLogin.loginId = :loginId")
    List<Treatment> findByPatientLoginId(@Param("loginId") Integer loginId);

    /**
     * Equivalent of the legacy {@code TreatmentDAO.listAllTreatments()} -
     * same query/join semantics as {@link #findByPatientLoginId}, without
     * the patient filter.
     */
    @Query("select t from Treatment t join fetch t.department join fetch t.medicine join fetch t.patientLogin "
            + "join fetch t.doctorLogin join fetch t.bill")
    List<Treatment> findAllWithAssociations();

    /**
     * Replaces {@code PatientRepository.deleteTreatmentsByPatientLoginId}'s
     * native-SQL placeholder now that {@code Treatment} has its own JPA
     * entity/repository - see {@code PatientService.deletePatient()}. A
     * JPQL bulk delete validated against the entity model, rather than a
     * raw table/column-name string. Matches the legacy delete exactly: only
     * the {@code Treatments} row is removed, not its associated {@code Bill}
     * row (the legacy DAO never deleted from {@code Bill} either).
     */
    @Modifying
    @Query("delete from Treatment t where t.patientLogin.loginId = :loginId")
    void deleteByPatientLoginId(@Param("loginId") Integer loginId);

    /**
     * Replaces {@code DoctorRepository.deleteTreatmentsByDoctorLoginId}'s
     * native-SQL placeholder - see {@code DoctorService.deleteDoctor()}.
     */
    @Modifying
    @Query("delete from Treatment t where t.doctorLogin.loginId = :loginId")
    void deleteByDoctorLoginId(@Param("loginId") Integer loginId);
}
