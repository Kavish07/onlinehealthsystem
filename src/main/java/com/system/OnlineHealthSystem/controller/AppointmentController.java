package com.system.OnlineHealthSystem.controller;

import com.onlinehealthsystem.AppConstants;
import com.onlinehealthsystem.Doctor;
import com.onlinehealthsystem.Patient;
import com.system.OnlineHealthSystem.service.AppointmentService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Replacement for the three legacy Appointment Servlets
 * (NewAppointmentServlet, ConfirmAppointmentServlet,
 * ListAppointmentsServlet). Deliberately keeps the exact same URL mappings
 * and forwards to the exact same JSPs, with a manual
 * {@link RequestDispatcher#forward} rather than a Spring view-name return,
 * so page behavior is unchanged while only the controller/DAO layer
 * underneath it is replaced - mirrors {@code PatientController}/
 * {@code DoctorController}.
 *
 * <p>Session attributes still hold the legacy {@code com.onlinehealthsystem}
 * {@code Patient}/{@code Doctor} POJOs (set by those controllers' login
 * flows), so this controller casts to those exact types rather than the
 * JPA entity types, same as {@code PatientController}/{@code DoctorController}
 * already do for each other's session attribute.
 *
 * <p>{@code isAdmin} below is a third copy of the same one-line admin
 * check already duplicated in {@code PatientController}/
 * {@code DoctorController}. Deliberately not extracted to a shared helper
 * in this slice: the check is a single {@code equals} call, both existing
 * copies are already covered by their own passing tests, and extracting it
 * now would mean touching two already-shipped, independently-tested
 * controllers for a one-line de-duplication - more risk than the
 * duplication itself currently costs. Worth revisiting once a fourth
 * caller or any actual drift between the copies shows up.
 */
@Controller
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/NewAppointmentServlet")
    public void newAppointment(@RequestParam("dateofappointment") String dateOfAppointment,
            @RequestParam("location") String location, @RequestParam("department") String department,
            @RequestParam("doctor") int doctorId, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Patient patient = (session != null) ? (Patient) session.getAttribute("patient") : null;

        if (patient == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            appointmentService.registerAppointment(LocalDateTime.parse(dateOfAppointment), location, department,
                    patient.getLoginID(), doctorId);
        } catch (IllegalArgumentException e) {
            request.setAttribute("message", e.getMessage());
            forward("appointments.jsp", request, response);
            return;
        }

        request.setAttribute("message", "Appointment successfully created");
        forward("home.jsp", request, response);
    }

    @PostMapping("/ConfirmAppointmentServlet")
    public void confirmAppointment(@RequestParam("appointmentID") int appointmentId, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Doctor doctor = (session != null) ? (Doctor) session.getAttribute("doctor") : null;

        if (doctor == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        appointmentService.confirmAppointment(appointmentId);
        session.setAttribute("listAppointments", appointmentService.listAppointments());
        request.setAttribute("message", "Appointment successfully updated");

        String destPage = isAdmin(doctor) ? "admin.jsp" : "home2.jsp";
        forward(destPage, request, response);
    }

    @PostMapping("/ListAppointmentsServlet")
    public void listAppointments(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Doctor doctor = (session != null) ? (Doctor) session.getAttribute("doctor") : null;

        if (doctor == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // appointments2.jsp reads the plural "listAppointments" (the name
        // ConfirmAppointmentServlet and the login/delete flows in
        // PatientController/DoctorController put the same list under at
        // session scope) - preserves this slice's fix: the request-scope
        // attribute used to be set under the singular "listAppointment".
        request.setAttribute("listAppointments", appointmentService.listAppointments());
        forward("appointments2.jsp", request, response);
    }

    private boolean isAdmin(Doctor doctor) {
        return doctor != null && AppConstants.ADMIN_USERNAME.equals(doctor.getUserName());
    }

    private void forward(String page, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher(page).forward(request, response);
    }
}
