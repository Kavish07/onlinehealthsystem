package com.onlinehealthsystem;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfirmAppointmentServletTest extends AbstractMySQLIntegrationTest {

    private final ConfirmAppointmentServlet servlet = new ConfirmAppointmentServlet();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    @Test
    void doPost_returnsForbidden_whenNoDoctorIsLoggedIn() throws Exception {
        // Regression test: this used to approve the appointment in the DB first
        // and only check the session afterward (and then NPE on a missing doctor).
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(false)).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    void doPost_confirmsAppointment_whenDoctorIsLoggedIn() throws Exception {
        Patient patient = patientDAO.registerPatient("jane.doe", "pw", "Jane", "Doe", "N1",
                Date.valueOf("1990-01-01"), "F", "1 Main St", "Springfield", 65, 170);
        seedDepartment("CARD", "Cardiology");
        Doctor doctor = doctorDAO.registerDoctor("john.smith", "pw", "John", "Smith", "N2",
                Date.valueOf("1980-01-01"), "M", "2 Main St", "Springfield", 80, 180,
                "MD", "Cardiology", "CARD");
        appointmentDAO.registerAppointment(Date.valueOf("2026-01-15"), "Room 1", "CARD",
                patient.getLoginID(), doctor.getLoginID());
        int appointmentId = appointmentDAO.listAppointments().get(0).getAppointmentID();

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("doctor")).thenReturn(doctor);
        when(request.getParameter("appointmentID")).thenReturn(String.valueOf(appointmentId));
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doPost(request, response);

        assertTrue(appointmentDAO.listAppointments().get(0).getApproved());
        verify(request).getRequestDispatcher("home2.jsp");
    }
}
