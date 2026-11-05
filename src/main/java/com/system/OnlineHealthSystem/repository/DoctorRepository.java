package com.system.OnlineHealthSystem.repository;

import com.system.OnlineHealthSystem.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Integer> {

    Optional<Doctor> findByLogin_LoginId(Integer loginId);

    /**
     * Equivalent of the legacy {@code select * from Login l, Doctors d,
     * Departments de where l.LoginID = d.LoginID and d.DepartmentCode =
     * de.DepartmentCode and l.UserName not in (?)} used by
     * {@code DoctorDAO.listDoctors()}: an inner join against Login and
     * Departments (so a doctor row with no matching department wouldn't be
     * returned, matching the legacy query's behavior), with the excluded
     * username passed as a bind parameter rather than baked into the query
     * text - callers (see {@code DoctorService.listDoctors()}) pass
     * {@code com.onlinehealthsystem.AppConstants.ADMIN_USERNAME}.
     */
    @Query("select d from Doctor d join fetch d.login l join fetch d.department where l.userName <> :userName")
    List<Doctor> findAllExcludingUserName(@Param("userName") String userName);

    long deleteByLogin_LoginId(Integer loginId);
}
