package com.system.OnlineHealthSystem.controller;

import com.onlinehealthsystem.Doctor;
import com.onlinehealthsystem.Patient;
import com.system.OnlineHealthSystem.service.TreatmentService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Replacement for the three legacy Treatment Servlets (NewTreatmentServlet,
 * UpdateTreatmentServlet, ListTreatmentsServlet). Deliberately keeps the
 * exact same URL mappings and forwards to the exact same JSPs, with a
 * manual {@link RequestDispatcher#forward} rather than a Spring view-name
 * return, so page behavior is unchanged while only the controller/DAO layer
 * underneath it is replaced - mirrors {@code AppointmentController}.
 *
 * <p>Session attributes still hold the legacy {@code com.onlinehealthsystem}
 * {@code Patient}/{@code Doctor} POJOs (set by {@code PatientController}/
 * {@code DoctorController}'s login flows), so this controller casts to
 * those exact types rather than the JPA entity types, same as
 * {@code AppointmentController} already does.
 *
 * <p>Note the auth gates differ by action, matching the legacy servlets
 * exactly: {@code newTreatment}/{@code updateTreatment} require a logged-in
 * "doctor" (a doctor records treatments for a chosen patient), while
 * {@code listTreatments} requires a logged-in "patient" (a patient views
 * their own treatment history) - these are not mirror images of each other.
 */
@Controller
public class TreatmentController {

    private final TreatmentService treatmentService;

    public TreatmentController(TreatmentService treatmentService) {
        this.treatmentService = treatmentService;
    }

    @PostMapping("/NewTreatmentServlet")
    public void newTreatment(@RequestParam("dateoftreatment") String dateOfTreatment,
            @RequestParam("location") String location, @RequestParam("department") String department,
            @RequestParam("treatment") String treatmentName, @RequestParam("medicines") String medicineName,
            @RequestParam("patient") int patientLoginId, @RequestParam("bill") int billAmount,
            @RequestParam("dateofbill") String dateOfBill, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Doctor doctor = (session != null) ? (Doctor) session.getAttribute("doctor") : null;

        if (doctor == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        treatmentService.registerTreatment(LocalDate.parse(dateOfTreatment), location, department, treatmentName,
                medicineName, patientLoginId, doctor.getLoginID(), billAmount, LocalDate.parse(dateOfBill));

        // Matches the legacy NewTreatmentServlet's own (copy-pasted from the
        // Appointment flow) message text - preserved as-is rather than
        // "fixed" mid-migration.
        request.setAttribute("message", "Appointment successfully created");
        forward("home2.jsp", request, response);
    }

    @PostMapping("/UpdateTreatmentServlet")
    public void updateTreatment(@RequestParam("treatmentID") int treatmentId,
            @RequestParam("dateoftreatment") String dateOfTreatment, @RequestParam("location") String location,
            @RequestParam("department") String department, @RequestParam("treatment") String treatmentName,
            @RequestParam("medicines") String medicineName, @RequestParam("patientID") int patientLoginId,
            @RequestParam("billID") int billId, @RequestParam("bill") int billAmount,
            @RequestParam("dateofbill") String dateOfBill,
            @RequestParam(value = "paid", required = false) boolean paid, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Doctor doctor = (session != null) ? (Doctor) session.getAttribute("doctor") : null;

        if (doctor == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // patientLoginId/doctor.getLoginID() are parsed/resolved here to
        // match the legacy UpdateTreatmentServlet's own request parameters,
        // but (like that servlet's call into TreatmentDAO.updateTreatment)
        // they're not actually used by the update itself - see
        // TreatmentService.updateTreatment's javadoc.
        treatmentService.updateTreatment(treatmentId, LocalDate.parse(dateOfTreatment), location, department,
                treatmentName, medicineName, billId, billAmount, LocalDate.parse(dateOfBill), paid);

        session.setAttribute("listAllTreatments", treatmentService.listAllTreatments());

        request.setAttribute("message", "Appointment successfully created");
        forward("home2.jsp", request, response);
    }

    @PostMapping("/ListTreatmentsServlet")
    public void listTreatments(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Patient patient = (session != null) ? (Patient) session.getAttribute("patient") : null;

        if (patient == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // treatments.jsp reads the plural "listTreatments" (the name
        // PatientController.login puts the same list under at session
        // scope) - preserves this slice's fix: the request-scope attribute
        // used to be set under the singular "listTreatment".
        request.setAttribute("listTreatments", treatmentService.listTreatments(patient.getLoginID()));
        forward("treatments.jsp", request, response);
    }

    private void forward(String page, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher(page).forward(request, response);
    }
}
