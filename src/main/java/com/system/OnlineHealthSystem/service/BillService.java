package com.system.OnlineHealthSystem.service;

import com.system.OnlineHealthSystem.entity.Bill;
import com.system.OnlineHealthSystem.entity.Treatment;
import com.system.OnlineHealthSystem.repository.TreatmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lists a patient's own {@link Bill}s. Reuses
 * {@link TreatmentRepository#findByPatientLoginId}'s existing join-fetch
 * (which already fetches {@code t.bill}) because {@link Bill} has no
 * back-reference to {@link Treatment} - the one-to-one is owned by
 * {@code Treatments.BillID}.
 */
@Service
public class BillService {

    private final TreatmentRepository treatmentRepository;

    public BillService(TreatmentRepository treatmentRepository) {
        this.treatmentRepository = treatmentRepository;
    }

    public List<Bill> listBills(int patientLoginId) {
        return treatmentRepository.findByPatientLoginId(patientLoginId).stream()
                .map(Treatment::getBill)
                .toList();
    }
}
