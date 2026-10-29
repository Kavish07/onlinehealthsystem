package com.system.OnlineHealthSystem.repository;

import com.system.OnlineHealthSystem.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Integer> {

    Optional<Patient> findByLogin_LoginId(Integer loginId);

    /**
     * Equivalent of the legacy {@code select * from Login l, Patients p
     * where l.LoginID = p.LoginID} join used by {@code PatientDAO.listPatients}:
     * fetches the associated {@link com.system.OnlineHealthSystem.entity.Login}
     * eagerly so callers don't trigger one lazy-load query per row.
     */
    @Query("select p from Patient p join fetch p.login")
    List<Patient> findAllWithLogin();

    long deleteByLogin_LoginId(Integer loginId);

    /**
     * {@code Appointments}/{@code Treatments} belong to future slices (see
     * the migration PRD) and don't have JPA entities/repositories yet, but
     * {@code PatientDAO.deletePatient}'s cascading delete across both
     * tables (keyed by the person's {@code Login.LoginID}, stored in each
     * table's own {@code PatientID} column) has to be preserved now. These
     * two native deletes are a deliberately narrow stand-in for that -
     * move them into the Appointment/Treatment slices' own repositories
     * once those exist, instead of duplicating them there.
     */
    @Modifying
    @Query(value = "delete from Appointments where PatientID = :loginId", nativeQuery = true)
    void deleteAppointmentsByPatientLoginId(@Param("loginId") Integer loginId);

    @Modifying
    @Query(value = "delete from Treatments where PatientID = :loginId", nativeQuery = true)
    void deleteTreatmentsByPatientLoginId(@Param("loginId") Integer loginId);
}
