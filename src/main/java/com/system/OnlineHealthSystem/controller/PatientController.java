package com.system.OnlineHealthSystem.controller;

import com.onlinehealthsystem.AppointmentDAO;
import com.onlinehealthsystem.Appointment;
import com.onlinehealthsystem.Doctor;
import com.onlinehealthsystem.DoctorDAO;
import com.onlinehealthsystem.Treatment;
import com.onlinehealthsystem.TreatmentDAO;
import com.system.OnlineHealthSystem.service.PatientService;
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
import java.sql.Date;
import java.sql.SQLException;
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
 * Still depends directly on the legacy {@code DoctorDAO}/{@code TreatmentDAO}/
 * {@code AppointmentDAO} (wrapped as beans in {@code LegacyDaoBeansConfig})
 * to populate session attributes those not-yet-migrated entities' JSPs
 * need (e.g. "listDoctors", "listTreatments"). That coupling goes away as
 * each of those gets its own slice.
 */
@Controller
public class PatientController {

    private static final String ADMIN_USERNAME = "admin@gmail.com";

    private final PatientService patientService;
    private final DoctorDAO doctorDAO;
    private final TreatmentDAO treatmentDAO;
    private final AppointmentDAO appointmentDAO;

    public PatientController(PatientService patientService, DoctorDAO doctorDAO, TreatmentDAO treatmentDAO,
            AppointmentDAO appointmentDAO) {
        this.patientService = patientService;
        this.doctorDAO = doctorDAO;
        this.treatmentDAO = treatmentDAO;
        this.appointmentDAO = appointmentDAO;
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

            try {
                List<Doctor> listDoctors = doctorDAO.listDoctors();
                session.setAttribute("listDoctors", listDoctors);

                List<Treatment> listTreatments = treatmentDAO.listTreatments(patient.getLoginID());
                session.setAttribute("listTreatments", listTreatments);
            } catch (SQLException | ClassNotFoundException ex) {
                throw new ServletException(ex);
            }

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
        com.onlinehealthsystem.Patient patient = (session != null)
                ? (com.onlinehealthsystem.Patient) session.getAttribute("patient")
                : null;

        if (session != null && patient != null) {
            // Derives the LoginID to close out from the session's own
            // identity, not a "loginID" request parameter - a prior fix for
            // one logged-in user being able to pass another user's
            // loginID and corrupt their logout record.
            patientService.registerLogout(patient.getLoginID());

            session.removeAttribute("patient");

            forward("index.jsp", request, response);
        }
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
                    lastName, nationalId, Date.valueOf(dateOfBirth), sex, address, city, weight, height);

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
        Doctor admin = (session != null) ? (Doctor) session.getAttribute("doctor") : null;

        if (admin == null || !admin.getUserName().equals(ADMIN_USERNAME)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        patientService.deletePatient(loginId);

        com.onlinehealthsystem.Patient deleted = new com.onlinehealthsystem.Patient();
        deleted.setLoginID(loginId);
        session.setAttribute("patient", deleted);

        List<com.onlinehealthsystem.Patient> listPatients = patientService.listPatients();
        session.setAttribute("listPatients", listPatients);

        try {
            List<Appointment> listAppointments = appointmentDAO.listAppointments();
            session.setAttribute("listAppointments", listAppointments);

            List<Treatment> listAllTreatments = treatmentDAO.listAllTreatments();
            session.setAttribute("listAllTreatments", listAllTreatments);

            List<Doctor> listDoctors = doctorDAO.listDoctors();
            session.setAttribute("listDoctors", listDoctors);
        } catch (SQLException | ClassNotFoundException ex) {
            throw new ServletException(ex);
        }

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
        HttpSession session = request.getSession(false);
        Doctor admin = (session != null) ? (Doctor) session.getAttribute("doctor") : null;

        if (admin == null || !admin.getUserName().equals(ADMIN_USERNAME)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        request.setAttribute("listPatient", patientService.listPatients());

        forward("admin.jsp", request, response);
    }

    private void forward(String page, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher(page).forward(request, response);
    }
}
