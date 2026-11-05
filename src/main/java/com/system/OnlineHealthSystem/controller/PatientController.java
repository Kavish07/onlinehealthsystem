package com.system.OnlineHealthSystem.controller;

import com.onlinehealthsystem.AppConstants;
import com.onlinehealthsystem.Appointment;
import com.onlinehealthsystem.Doctor;
import com.onlinehealthsystem.Treatment;
import com.system.OnlineHealthSystem.entity.Bill;
import com.system.OnlineHealthSystem.service.AppointmentService;
import com.system.OnlineHealthSystem.service.BillService;
import com.system.OnlineHealthSystem.service.DoctorService;
import com.system.OnlineHealthSystem.service.PatientService;
import com.system.OnlineHealthSystem.service.TreatmentService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Replacement for the six legacy Patient Servlets (PatientLoginServlet,
 * PatientLogoutServlet, PatientRegisterServlet, PatientDeleteServlet,
 * UpdatePatientPasswordServlet, ListPatientsServlet). Deliberately keeps
 * the exact same URL mappings and forwards to the exact same JSPs, with a
 * manual {@link RequestDispatcher#forward} rather than a Spring view-name
 * return, so page behavior is unchanged while only the controller/DAO
 * layer underneath it is replaced - per the migration checklist.
 *
 * Doctor/Appointment/Treatment data now comes from the migrated
 * {@link DoctorService}/{@link AppointmentService}/{@link TreatmentService}
 * (those slices' own JPA-backed replacements for the legacy
 * {@code DoctorDAO}/{@code AppointmentDAO}/{@code TreatmentDAO}).
 */
@Controller
public class PatientController {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final TreatmentService treatmentService;
    private final AppointmentService appointmentService;
    private final BillService billService;

    public PatientController(PatientService patientService, DoctorService doctorService,
            TreatmentService treatmentService, AppointmentService appointmentService, BillService billService) {
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.treatmentService = treatmentService;
        this.appointmentService = appointmentService;
        this.billService = billService;
    }

    @PostMapping("/PatientLoginServlet")
    public void login(@RequestParam("username") String userName, @RequestParam("password") String password,
            HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Optional<com.onlinehealthsystem.Patient> patientOpt = patientService.login(userName, password);
        String destPage = "index.jsp";

        if (patientOpt.isPresent()) {
            com.onlinehealthsystem.Patient patient = patientOpt.get();
            HttpSession session = request.getSession();
            session.setAttribute("patient", patient);

            List<Doctor> listDoctors = doctorService.listDoctors();
            session.setAttribute("listDoctors", listDoctors);

            List<Treatment> listTreatments = treatmentService.listTreatments(patient.getLoginID());
            session.setAttribute("listTreatments", listTreatments);

            List<Bill> listBills = billService.listBills(patient.getLoginID());
            session.setAttribute("listBills", listBills);

            destPage = "home.jsp";
        } else {
            request.setAttribute("message", "Invalid email/password");
        }

        forward(destPage, request, response);
    }

    @GetMapping("/PatientLogoutServlet")
    public void logout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return;
        }

        // Matches the legacy PatientLogoutServlet's behavior: forward to
        // index.jsp whenever a session exists, even one that never set a
        // "patient" attribute (e.g. a doctor-only session) - only the
        // LoginHistory close-out and attribute removal are conditional on
        // actually being logged in as a patient.
        com.onlinehealthsystem.Patient patient = (com.onlinehealthsystem.Patient) session.getAttribute("patient");

        if (patient != null) {
            // Derives the LoginID to close out from the session's own
            // identity, not a "loginID" request parameter - a prior fix for
            // one logged-in user being able to pass another user's
            // loginID and corrupt their logout record.
            patientService.registerLogout(patient.getLoginID());

            session.removeAttribute("patient");
        }

        forward("index.jsp", request, response);
    }

    @PostMapping("/PatientRegisterServlet")
    public void register(@RequestParam("username") String userName, @RequestParam("password") String password,
            @RequestParam("firstname") String firstName, @RequestParam("lastname") String lastName,
            @RequestParam("nationalid") String nationalId, @RequestParam("dateofbirth") String dateOfBirth,
            @RequestParam("sex") String sex, @RequestParam("address") String address,
            @RequestParam("city") String city, @RequestParam("weight") int weight,
            @RequestParam("height") int height, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String destPage = "index.jsp";

        if (!patientService.isUsernameTaken(userName)) {
            com.onlinehealthsystem.Patient patient = patientService.register(userName, password, firstName,
                    lastName, nationalId, LocalDate.parse(dateOfBirth), sex, address, city, weight, height);

            HttpSession session = request.getSession();
            session.setAttribute("patient", patient);
            request.setAttribute("message", "User successfully created");
        } else {
            request.setAttribute("message", "User already exists");
        }

        forward(destPage, request, response);
    }

    @PostMapping("/PatientDeleteServlet")
    public void delete(@RequestParam("loginID") int loginId, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (!isAdminSession(session)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        patientService.deletePatient(loginId);

        com.onlinehealthsystem.Patient deleted = new com.onlinehealthsystem.Patient();
        deleted.setLoginID(loginId);
        session.setAttribute("patient", deleted);

        List<com.onlinehealthsystem.Patient> listPatients = patientService.listPatients();
        session.setAttribute("listPatients", listPatients);

        List<Appointment> listAppointments = appointmentService.listAppointments();
        session.setAttribute("listAppointments", listAppointments);

        List<Treatment> listAllTreatments = treatmentService.listAllTreatments();
        session.setAttribute("listAllTreatments", listAllTreatments);

        List<Doctor> listDoctors = doctorService.listDoctors();
        session.setAttribute("listDoctors", listDoctors);

        forward("admin.jsp", request, response);
    }

    @PostMapping("/UpdatePatientPasswordServlet")
    public void updatePassword(@RequestParam("username") String userName, @RequestParam("password") String password,
            HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // The legacy UpdatePatientPasswordServlet trusted the "username"
        // request parameter with no session check at all, letting anyone
        // change any account's password. Fixed here: only the already
        // logged-in patient may change their own password.
        HttpSession session = request.getSession(false);
        com.onlinehealthsystem.Patient self = (session != null)
                ? (com.onlinehealthsystem.Patient) session.getAttribute("patient")
                : null;

        if (self == null || !userName.equals(self.getUserName())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Optional<com.onlinehealthsystem.Patient> patientOpt = patientService.updatePassword(userName, password);

        if (patientOpt.isPresent()) {
            session.setAttribute("patient", patientOpt.get());
        } else {
            request.setAttribute("message", "Invalid email/password");
        }

        forward("home.jsp", request, response);
    }

    @PostMapping("/ListPatientsServlet")
    public void listPatients(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isAdminSession(request.getSession(false))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        request.setAttribute("listPatient", patientService.listPatients());

        forward("admin.jsp", request, response);
    }

    private boolean isAdminSession(HttpSession session) {
        Doctor admin = (session != null) ? (Doctor) session.getAttribute("doctor") : null;
        return admin != null && admin.getUserName().equals(AppConstants.ADMIN_USERNAME);
    }

    private void forward(String page, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher(page).forward(request, response);
    }
}
