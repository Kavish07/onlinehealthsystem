package com.system.OnlineHealthSystem.controller;

import com.onlinehealthsystem.AppConstants;
import com.onlinehealthsystem.Doctor;
import com.system.OnlineHealthSystem.service.AppointmentService;
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
import java.util.Optional;

/**
 * Replacement for the six legacy Doctor Servlets (DoctorLoginServlet,
 * DoctorLogoutServlet, DoctorRegisterServlet, DoctorDeleteServlet,
 * UpdateDoctorPasswordServlet, ListDoctorsServlet). Deliberately keeps the
 * exact same URL mappings and forwards to the exact same JSPs, with a
 * manual {@link RequestDispatcher#forward} rather than a Spring view-name
 * return, so page behavior is unchanged while only the controller/DAO
 * layer underneath it is replaced - mirrors {@code PatientController}.
 *
 * Appointment/Treatment data now comes from the migrated
 * {@link AppointmentService}/{@link TreatmentService} (those slices' own
 * JPA-backed replacements for the legacy {@code AppointmentDAO}/
 * {@code TreatmentDAO}).
 */
@Controller
public class DoctorController {

    private final DoctorService doctorService;
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final TreatmentService treatmentService;

    public DoctorController(DoctorService doctorService, PatientService patientService,
            AppointmentService appointmentService, TreatmentService treatmentService) {
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.appointmentService = appointmentService;
        this.treatmentService = treatmentService;
    }

    @PostMapping("/DoctorLoginServlet")
    public void login(@RequestParam("username") String userName, @RequestParam("password") String password,
            HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Optional<Doctor> doctorOpt = doctorService.login(userName, password);
        String destPage = "index.jsp";

        if (doctorOpt.isPresent()) {
            Doctor doctor = doctorOpt.get();
            HttpSession session = request.getSession();
            session.setAttribute("doctor", doctor);

            session.setAttribute("listPatients", patientService.listPatients());
            session.setAttribute("listAppointments", appointmentService.listAppointments());
            session.setAttribute("listAllTreatments", treatmentService.listAllTreatments());

            if (isAdmin(doctor)) {
                session.setAttribute("listDoctors", doctorService.listDoctors());
                destPage = "admin.jsp";
            } else {
                destPage = "home2.jsp";
            }
        } else {
            request.setAttribute("message", "Invalid email/password");
        }

        forward(destPage, request, response);
    }

    @GetMapping("/DoctorLogoutServlet")
    public void logout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Doctor doctor = (session != null) ? (Doctor) session.getAttribute("doctor") : null;

        // Matches the legacy DoctorLogoutServlet's behavior exactly: unlike
        // PatientLogoutServlet, this only forwards at all when a doctor was
        // actually logged in on this session.
        if (session == null || doctor == null) {
            return;
        }

        doctorService.registerLogout(doctor.getLoginID());

        session.removeAttribute("patient");
        session.removeAttribute("doctor");

        forward("index.jsp", request, response);
    }

    @PostMapping("/DoctorRegisterServlet")
    public void register(@RequestParam("username") String userName, @RequestParam("password") String password,
            @RequestParam("firstname") String firstName, @RequestParam("lastname") String lastName,
            @RequestParam("nationalid") String nationalId, @RequestParam("dateofbirth") String dateOfBirth,
            @RequestParam("sex") String sex, @RequestParam("address") String address,
            @RequestParam("city") String city, @RequestParam("weight") int weight,
            @RequestParam("height") int height, @RequestParam("qualifications") String qualifications,
            @RequestParam("speciality") String speciality, @RequestParam("departmentcode") String departmentCode,
            HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (!isAdminSession(session)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (!doctorService.isUsernameTaken(userName)) {
            // Deliberately doesn't do session.setAttribute("doctor", ...)
            // with the newly created doctor - this endpoint is called by
            // the admin to register someone else, and overwriting the
            // "doctor" session attribute here would silently log the admin
            // out of their own identity and into the new doctor's (the
            // exact bug DoctorRegisterServlet's own comment documented and
            // avoided).
            doctorService.register(userName, password, firstName, lastName, nationalId, LocalDate.parse(dateOfBirth),
                    sex, address, city, weight, height, qualifications, speciality, departmentCode);

            session.setAttribute("listDoctors", doctorService.listDoctors());
            request.setAttribute("message", "User successfully created");
        } else {
            request.setAttribute("message", "User already exists");
        }

        forward("admin.jsp", request, response);
    }

    @PostMapping("/DoctorDeleteServlet")
    public void delete(@RequestParam("loginID") int loginId, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (!isAdminSession(session)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        doctorService.deleteDoctor(loginId);

        session.setAttribute("listPatients", patientService.listPatients());
        session.setAttribute("listAppointments", appointmentService.listAppointments());
        session.setAttribute("listAllTreatments", treatmentService.listAllTreatments());
        session.setAttribute("listDoctors", doctorService.listDoctors());

        forward("admin.jsp", request, response);
    }

    @PostMapping("/UpdateDoctorPasswordServlet")
    public void updatePassword(@RequestParam("username") String userName, @RequestParam("password") String password,
            HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // The legacy UpdateDoctorPasswordServlet trusted the "username"
        // request parameter with no session check at all, letting anyone
        // change any account's password (same bug class fixed for patients
        // in UpdatePatientPasswordServlet/PatientController). Fixed here:
        // only the already logged-in doctor may change their own password.
        HttpSession session = request.getSession(false);
        Doctor self = (session != null) ? (Doctor) session.getAttribute("doctor") : null;

        if (self == null || !userName.equals(self.getUserName())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Optional<Doctor> doctorOpt = doctorService.updatePassword(userName, password);
        // Matches the legacy servlet's own (slightly odd) routing exactly:
        // a successful non-admin password change forwards to "home.jsp",
        // not "home2.jsp" (the page DoctorLoginServlet sends non-admin
        // doctors to) - preserved as-is rather than "fixed" mid-migration.
        String destPage = "home.jsp";

        if (doctorOpt.isPresent()) {
            Doctor doctor = doctorOpt.get();
            session.setAttribute("doctor", doctor);
            destPage = isAdmin(doctor) ? "admin.jsp" : "home.jsp";
        } else {
            request.setAttribute("message", "Invalid email/password");
        }

        forward(destPage, request, response);
    }

    @PostMapping("/ListDoctorsServlet")
    public void listDoctors(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isAdminSession(request.getSession(false))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // admin.jsp reads the plural "listDoctors" (the name
        // login/register/delete put the same list under at session scope) -
        // preserves this session's fix (the request-scope attribute used to
        // be set under the singular "listDoctor").
        request.setAttribute("listDoctors", doctorService.listDoctors());

        forward("admin.jsp", request, response);
    }

    private boolean isAdmin(Doctor doctor) {
        return doctor != null && AppConstants.ADMIN_USERNAME.equals(doctor.getUserName());
    }

    private boolean isAdminSession(HttpSession session) {
        Doctor admin = (session != null) ? (Doctor) session.getAttribute("doctor") : null;
        return isAdmin(admin);
    }

    private void forward(String page, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher(page).forward(request, response);
    }
}
