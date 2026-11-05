package com.system.OnlineHealthSystem.repository;

import com.system.OnlineHealthSystem.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
