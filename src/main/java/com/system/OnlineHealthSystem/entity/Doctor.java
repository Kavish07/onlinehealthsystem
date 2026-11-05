package com.system.OnlineHealthSystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Maps the {@code Doctors} table. Mirrors {@link Patient}'s shape and the
 * same caveat applies: {@code DoctorID} is this table's own surrogate key,
 * barely used elsewhere - the identity that matters for sessions and for
 * {@code Appointments.DoctorID}/{@code Treatments.DoctorID}/
 * {@code LoginHistory.LoginID} is always {@code Login.LoginID} (exposed
 * here via {@link #getLogin()}), not this entity's own {@code doctorId}.
 */
@Entity
@Table(name = "Doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DoctorID")
    private Integer doctorId;

    // See Patient.login's javadoc: fetch = LAZY is only honored here because
    // DoctorRepository.findAllExcludingUserName() explicitly join fetches
    // this association; a caller that doesn't would get an extra
    // per-row SELECT instead of real lazy loading (no bytecode enhancement
    // configured).
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LoginID", nullable = false, unique = true)
    private Login login;

    @Column(name = "FirstName")
    private String firstName;

    @Column(name = "LastName")
    private String lastName;

    @Column(name = "NationalID")
    private String nationalId;

    @Column(name = "DateOfBirth")
    private LocalDate dateOfBirth;

    @Column(name = "Sex")
    private String sex;

    @Column(name = "Address")
    private String address;

    @Column(name = "City")
    private String city;

    @Column(name = "Weight")
    private Integer weight;

    @Column(name = "Height")
    private Integer height;

    @Column(name = "Qualifications")
    private String qualifications;

    @Column(name = "Speciality")
    private String speciality;

    // No cascade: a transient Department with just its natural id
    // (DepartmentCode) set is enough for Hibernate to write the FK column on
    // insert without needing the Department row itself to be managed/saved
    // through this association - see DoctorService.register().
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DepartmentCode")
    private Department department;

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public Login getLogin() {
        return login;
    }

    public void setLogin(Login login) {
        this.login = login;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getQualifications() {
        return qualifications;
    }

    public void setQualifications(String qualifications) {
        this.qualifications = qualifications;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
