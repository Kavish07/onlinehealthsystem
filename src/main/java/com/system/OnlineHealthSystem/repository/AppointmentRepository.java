package com.system.OnlineHealthSystem.repository;

import com.system.OnlineHealthSystem.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    /**
     * Equivalent of the legacy {@code select * from Appointments a,
     * Departments de, Patients p, Doctors d where a.PatientID = p.LoginID
     * and a.DoctorID = d.LoginID and a.DepartmentCode = de.DepartmentCode}
     * used by {@code AppointmentDAO.listAppointments()}: an inner `join
     * fetch` against {@code Department} (so an appointment row with no
     * matching department is silently dropped, matching the legacy query's
     * behavior) plus both {@code Login} associations, so callers (see
     * {@code AppointmentService}) don't trigger one extra SELECT per row.
     */
    @Query("select a from Appointment a join fetch a.department join fetch a.patientLogin join fetch a.doctorLogin")
    List<Appointment> findAllWithAssociations();

    /**
     * Equivalent of the legacy {@code AppointmentDAO.checkAppointment}'s
     * join, narrowed to a given patient's {@code Login.LoginID} rather than
     * joining through a username filter directly - see
     * {@code AppointmentService.checkAppointment()}, which resolves the
     * username to a {@code Login.LoginID} first via {@code LoginRepository}.
     * Same inner-join-drops-rows-without-a-department semantics as
     * {@link #findAllWithAssociations()}.
     */
    @Query("select a from Appointment a join fetch a.department join fetch a.patientLogin join fetch a.doctorLogin "
            + "where a.patientLogin.loginId = :loginId")
    List<Appointment> findByPatientLoginId(@Param("loginId") Integer loginId);

    /**
     * Replaces {@code PatientRepository.deleteAppointmentsByPatientLoginId}'s
     * native-SQL placeholder now that {@code Appointment} has its own JPA
     * entity/repository - see {@code PatientService.deletePatient()}. A
     * JPQL bulk delete validated against the entity model, rather than a
     * raw table/column-name string.
     */
    @Modifying
    @Query("delete from Appointment a where a.patientLogin.loginId = :loginId")
    void deleteByPatientLoginId(@Param("loginId") Integer loginId);

    /**
     * Replaces {@code DoctorRepository.deleteAppointmentsByDoctorLoginId}'s
     * native-SQL placeholder - see {@code DoctorService.deleteDoctor()}.
     */
    @Modifying
    @Query("delete from Appointment a where a.doctorLogin.loginId = :loginId")
    void deleteByDoctorLoginId(@Param("loginId") Integer loginId);
}
