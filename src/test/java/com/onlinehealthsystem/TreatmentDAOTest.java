package com.onlinehealthsystem;

import org.junit.jupiter.api.Test;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreatmentDAOTest extends AbstractMySQLIntegrationTest {

    private final TreatmentDAO treatmentDAO = new TreatmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    private Patient registerPatient(String userName) throws Exception {
        return patientDAO.registerPatient(userName, "pw", "Jane", "Doe", "N1",
                Date.valueOf("1990-01-01"), "F", "1 Main St", "Springfield", 65, 170);
    }

    private Doctor registerDoctor(String userName) throws Exception {
        seedDepartment("CARD", "Cardiology");
        return doctorDAO.registerDoctor(userName, "pw", "John", "Smith", "N2",
                Date.valueOf("1980-01-01"), "M", "2 Main St", "Springfield", 80, 180,
                "MD", "Cardiology", "CARD");
    }

    @Test
    void registerTreatment_thenListTreatments_includesIt() throws Exception {
        Patient patient = registerPatient("jane.doe");
        Doctor doctor = registerDoctor("john.smith");
        seedMedicine("MED1", "Paracetamol");

        treatmentDAO.registerTreatment(Date.valueOf("2026-01-15"), "Room 1", "CARD",
                "Checkup", "Paracetamol", patient.getLoginID(), doctor.getLoginID(), 100,
                Date.valueOf("2026-01-15"));

        var treatments = treatmentDAO.listTreatments(patient.getLoginID());

        assertEquals(1, treatments.size());
        assertEquals("Checkup", treatments.get(0).getTreatmentName());
        assertEquals("MED1", treatments.get(0).getMedicineCode());
        assertEquals(100, treatments.get(0).getBillAmount());
    }

    @Test
    void listTreatments_returnsEmpty_forPatientWithNoTreatments() throws Exception {
        Patient patient = registerPatient("jane.doe");

        assertTrue(treatmentDAO.listTreatments(patient.getLoginID()).isEmpty());
    }

    @Test
    void listAllTreatments_includesTreatmentsForEveryPatient() throws Exception {
        Patient patient1 = registerPatient("jane.doe");
        Doctor doctor = registerDoctor("john.smith");
        seedMedicine("MED1", "Paracetamol");
        treatmentDAO.registerTreatment(Date.valueOf("2026-01-15"), "Room 1", "CARD",
                "Checkup", "Paracetamol", patient1.getLoginID(), doctor.getLoginID(), 100,
                Date.valueOf("2026-01-15"));

        Patient patient2 = registerPatient("john.doe2");
        treatmentDAO.registerTreatment(Date.valueOf("2026-01-16"), "Room 2", "CARD",
                "Follow-up", "Paracetamol", patient2.getLoginID(), doctor.getLoginID(), 50,
                Date.valueOf("2026-01-16"));

        assertEquals(2, treatmentDAO.listAllTreatments().size());
    }

    @Test
    void updateTreatment_updatesTreatmentAndBillFields() throws Exception {
        Patient patient = registerPatient("jane.doe");
        Doctor doctor = registerDoctor("john.smith");
        seedMedicine("MED1", "Paracetamol");
        seedMedicine("MED2", "Ibuprofen");
        treatmentDAO.registerTreatment(Date.valueOf("2026-01-15"), "Room 1", "CARD",
                "Checkup", "Paracetamol", patient.getLoginID(), doctor.getLoginID(), 100,
                Date.valueOf("2026-01-15"));
        Treatment created = treatmentDAO.listTreatments(patient.getLoginID()).get(0);

        treatmentDAO.updateTreatment(created.getTreatmentID(), Date.valueOf("2026-02-01"),
                "Room 2", "CARD", "Follow-up", "Ibuprofen", patient.getLoginID(),
                doctor.getLoginID(), created.getBillID(), 200, Date.valueOf("2026-02-01"));

        Treatment updated = treatmentDAO.listTreatments(patient.getLoginID()).get(0);
        assertEquals("Follow-up", updated.getTreatmentName());
        assertEquals("MED2", updated.getMedicineCode());
        assertEquals(200, updated.getBillAmount());
    }
}
