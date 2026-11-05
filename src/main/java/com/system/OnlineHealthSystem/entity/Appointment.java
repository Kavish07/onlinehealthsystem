package com.system.OnlineHealthSystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Maps the {@code Appointments} table. Mirrors {@link Patient}/{@link Doctor}'s
 * shape: {@code AppointmentId} is this table's own surrogate key, but the
 * {@code PatientID}/{@code DoctorID} columns on this table are a different
 * animal from {@code Patients.PatientID}/{@code Doctors.DoctorID} - they
 * store the booking patient's/doctor's {@code Login.LoginID} directly (see
 * schema.sql's header note and the legacy {@code AppointmentDAO}'s join
 * conditions, e.g. {@code a.PatientID = p.LoginID}). That's exactly why
 * {@link #patientLogin}/{@link #doctorLogin} below are modeled as
 * associations to {@link Login}, not to {@link Patient}/{@link Doctor} -
 * mapping them to the profile tables directly would be wrong against the
 * actual column semantics.
 */
@Entity
@Table(name = "Appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AppointmentId")
    private Integer appointmentId;

    @Column(name = "Date")
    private LocalDateTime appointmentDate;

    @Column(name = "Location")
    private String location;

    // No cascade, same rationale as Doctor.department: a transient
    // Department with just its natural id (DepartmentCode) set is enough
    // for Hibernate to write the FK column without needing the Department
    // row itself to be managed/saved through this association - see
    // AppointmentService.registerAppointment(). Nullable: the legacy
    // schema allows a null DepartmentCode on this table (unlike
    // PatientID/DoctorID), and the legacy listAppointments()/checkAppointment()
    // queries inner-join against Departments, silently dropping any
    // appointment row whose DepartmentCode doesn't match - preserved by
    // using an inner `join fetch` in AppointmentRepository rather than a
    // `left join fetch`.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DepartmentCode")
    private Department department;

    // See the class javadoc: this stores the booking patient's
    // Login.LoginID, not a Patients.PatientID. No cascade - a transient
    // Login with just loginId set is enough for Hibernate to write the FK
    // column on insert (see AppointmentService.registerAppointment()).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PatientID", nullable = false)
    private Login patientLogin;

    // See the class javadoc: this stores the assigned doctor's
    // Login.LoginID, not a Doctors.DoctorID.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DoctorID", nullable = false)
    private Login doctorLogin;

    @Column(name = "Approved")
    private Boolean approved;

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDateTime appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Login getPatientLogin() {
        return patientLogin;
    }

    public void setPatientLogin(Login patientLogin) {
        this.patientLogin = patientLogin;
    }

    public Login getDoctorLogin() {
        return doctorLogin;
    }

    public void setDoctorLogin(Login doctorLogin) {
        this.doctorLogin = doctorLogin;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }
}
