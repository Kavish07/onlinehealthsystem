package com.system.OnlineHealthSystem.repository;

import com.system.OnlineHealthSystem.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, String> {

    /**
     * Equivalent of the legacy {@code select * from Medicines where
     * MedicineName in (?)} used by {@code TreatmentDAO.registerTreatment}/
     * {@code updateTreatment} to resolve a medicine name (the value the
     * "medicines" request parameter actually holds) to its
     * {@code MedicineCode} - see {@code TreatmentService}.
     */
    Optional<Medicine> findByMedicineName(String medicineName);
}
