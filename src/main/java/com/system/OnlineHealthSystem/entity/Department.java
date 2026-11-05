package com.system.OnlineHealthSystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Maps the {@code Departments} table. {@code DepartmentCode} is this
 * table's own natural (not generated) primary key - it's the same value
 * stored in {@code Doctors.DepartmentCode}/{@code Appointments.DepartmentCode}/
 * {@code Treatments.DepartmentCode}. Added as a minimal entity purely so
 * {@link Doctor} can join to it for {@code DoctorRepository.findAllExcludingUserName}
 * (the equivalent of the legacy {@code DoctorDAO.listDoctors()}'s
 * three-way join against Login/Doctors/Departments) - a full Department
 * slice (its own repository/service/controller) isn't in scope here.
 */
@Entity
@Table(name = "Departments")
public class Department {

    @Id
    @Column(name = "DepartmentCode")
    private String departmentCode;

    @Column(name = "DepartmentName")
    private String departmentName;

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
}
