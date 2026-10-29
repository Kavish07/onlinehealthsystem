package com.onlinehealthsystem;

import org.junit.jupiter.api.Test;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppointmentDAOTest extends AbstractMySQLIntegrationTest {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
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
    void registerAppointment_thenListAppointments_includesIt_unapproved() throws Exception {
        Patient patient = registerPatient("jane.doe");
        Doctor doctor = registerDoctor("john.smith");

        appointmentDAO.registerAppointment(Date.valueOf("2026-01-15"), "Room 1", "CARD",
                patient.getLoginID(), doctor.getLoginID());

        var appointments = appointmentDAO.listAppointments();

        assertEquals(1, appointments.size());
        assertFalse(appointments.get(0).getApproved());
    }

    @Test
    void confirmAppointment_setsApprovedTrue() throws Exception {
        Patient patient = registerPatient("jane.doe");
        Doctor doctor = registerDoctor("john.smith");
        appointmentDAO.registerAppointment(Date.valueOf("2026-01-15"), "Room 1", "CARD",
                patient.getLoginID(), doctor.getLoginID());
        int appointmentId = appointmentDAO.listAppointments().get(0).getAppointmentID();

        appointmentDAO.confirmAppointment(appointmentId);

        var appointments = appointmentDAO.listAppointments();
        assertTrue(appointments.get(0).getApproved());
    }

    @Test
    void checkAppointment_returnsMatchingAppointment_forPatientUserName() throws Exception {
        // Regression test: checkAppointment used to select under typo'd/wrong-alias
        // column names (de.DepartmenName/DepartmenCode, l.PatientID/l.DoctorID) and
        // would throw "column not found" before this was fixed.
        Patient patient = registerPatient("jane.doe");
        Doctor doctor = registerDoctor("john.smith");
        appointmentDAO.registerAppointment(Date.valueOf("2026-01-15"), "Room 1", "CARD",
                patient.getLoginID(), doctor.getLoginID());

        Appointment found = appointmentDAO.checkAppointment("jane.doe");

        assertEquals("Room 1", found.getLocation());
        assertEquals("Cardiology", found.getDepartmentName());
        assertEquals("CARD", found.getDepartmentCode());
    }
}
