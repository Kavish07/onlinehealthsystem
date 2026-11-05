package com.system.OnlineHealthSystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Maps the {@code Medicines} table. {@code MedicineCode} is this table's own
 * natural (not generated) primary key, the same value stored in
 * {@code Treatments.MedicineCode}. Added as a minimal entity purely so
 * {@link Treatment} can join to it - mirrors how {@link Department} was
 * added minimally in the Doctor slice for the same reason. A full Medicine
 * slice (its own repository/service/controller) isn't in scope here, beyond
 * the one lookup-by-name query {@code TreatmentService} needs (see
 * {@code MedicineRepository}).
 */
@Entity
@Table(name = "Medicines")
public class Medicine {

    @Id
    @Column(name = "MedicineCode")
    private String medicineCode;

    @Column(name = "MedicineName")
    private String medicineName;

    public String getMedicineCode() {
        return medicineCode;
    }

    public void setMedicineCode(String medicineCode) {
        this.medicineCode = medicineCode;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }
}
