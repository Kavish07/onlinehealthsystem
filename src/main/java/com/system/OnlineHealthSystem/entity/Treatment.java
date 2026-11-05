package com.system.OnlineHealthSystem.entity;

import jakarta.persistence.CascadeType;
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
 * Maps the {@code Treatments} table. Mirrors {@link Appointment}'s shape and
 * the same caveat applies: {@code TreatmentId} is this table's own surrogate
 * key, but the {@code PatientID}/{@code DoctorID} columns on this table store
 * the treated patient's/treating doctor's {@code Login.LoginID} directly
 * (see schema.sql's header note and the legacy {@code TreatmentDAO}'s join
 * conditions, e.g. {@code t.PatientID = p.LoginID}), not
 * {@code Patients.PatientID}/{@code Doctors.DoctorID} - hence
 * {@link #patientLogin}/{@link #doctorLogin} are associations to
 * {@link Login}, not to {@link Patient}/{@link Doctor}.
 *
 * <p>{@link #bill} is the owning side of a one-to-one to {@link Bill} via
 * this table's own {@code BillID} column, with {@code CascadeType.PERSIST}/
 * {@code MERGE} so that saving a new {@code Treatment} with a transient
 * {@code Bill} attached inserts the {@code Bill} row first (to obtain its
 * generated {@code BillID}) and then this row, in one transaction/flush -
 * the JPA equivalent of the legacy DAO's already-fixed
 * {@code insert into Bill ... returning BillID} atomic insert, without the
 * race-prone "select the last-inserted row" pattern it replaced. See
 * {@code TreatmentService.registerTreatment()}.
 */
@Entity
@Table(name = "Treatments")
public class Treatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TreatmentId")
    private Integer treatmentId;

    @Column(name = "Date")
    private LocalDate treatmentDate;

    @Column(name = "Location")
    private String location;

    // No cascade, same rationale as Appointment.department: a transient
    // Department with just its natural id (DepartmentCode) set is enough for
    // Hibernate to write the FK column. Nullable, and the legacy
    // listTreatments()/listAllTreatments() queries inner-join against
    // Departments, silently dropping any treatment row whose DepartmentCode
    // doesn't match - preserved via an inner `join fetch` in
    // TreatmentRepository rather than a `left join fetch`.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DepartmentCode")
    private Department department;

    @Column(name = "TreatmentName")
    private String treatmentName;

    // Same no-cascade rationale as department above. Nullable, and same
    // inner-join-drops-rows semantics - see TreatmentRepository.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MedicineCode")
    private Medicine medicine;

    // See the class javadoc: this stores the treated patient's
    // Login.LoginID, not a Patients.PatientID. No cascade - a transient
    // Login with just loginId set is enough for Hibernate to write the FK
    // column on insert (see TreatmentService.registerTreatment()).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PatientID", nullable = false)
    private Login patientLogin;

    // See the class javadoc: this stores the treating doctor's
    // Login.LoginID, not a Doctors.DoctorID.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DoctorID", nullable = false)
    private Login doctorLogin;

    @OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "BillID")
    private Bill bill;

    public Integer getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(Integer treatmentId) {
        this.treatmentId = treatmentId;
    }

    public LocalDate getTreatmentDate() {
        return treatmentDate;
    }

    public void setTreatmentDate(LocalDate treatmentDate) {
        this.treatmentDate = treatmentDate;
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

    public String getTreatmentName() {
        return treatmentName;
    }

    public void setTreatmentName(String treatmentName) {
        this.treatmentName = treatmentName;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
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

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }
}
