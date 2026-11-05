package com.system.OnlineHealthSystem.controller;

import com.onlinehealthsystem.Doctor;
import com.system.OnlineHealthSystem.service.AppointmentService;
import com.system.OnlineHealthSystem.service.DoctorService;
import com.system.OnlineHealthSystem.service.PatientService;
import com.system.OnlineHealthSystem.service.TreatmentService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DoctorController.class)
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DoctorService doctorService;

    @MockitoBean
    private PatientService patientService;

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private TreatmentService treatmentService;

    private Doctor admin() {
        Doctor admin = new Doctor();
        admin.setUserName("admin@gmail.com");
        return admin;
    }

    @Test
    void login_forwardsToHome2_andSetsSessionAttributes_forNonAdminDoctor() throws Exception {
        Doctor doctor = new Doctor();
        doctor.setLoginID(7);
        doctor.setUserName("john.smith");
        when(doctorService.login("john.smith", "s3cret-pw")).thenReturn(Optional.of(doctor));
        when(patientService.listPatients()).thenReturn(List.of());
        when(appointmentService.listAppointments()).thenReturn(List.of());
        when(treatmentService.listAllTreatments()).thenReturn(List.of());

        var result = mockMvc.perform(post("/DoctorLoginServlet")
                        .param("username", "john.smith")
                        .param("password", "s3cret-pw"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("home2.jsp"))
                .andReturn();

        HttpSession session = result.getRequest().getSession(false);
        org.junit.jupiter.api.Assertions.assertNotNull(session);
        org.junit.jupiter.api.Assertions.assertSame(doctor, session.getAttribute("doctor"));
        verify(doctorService, never()).listDoctors();
    }

    @Test
    void login_forwardsToAdminPage_andSetsListDoctors_forTheAdminAccount() throws Exception {
        Doctor doctor = admin();
        doctor.setLoginID(1);
        when(doctorService.login("admin@gmail.com", "admin-pw")).thenReturn(Optional.of(doctor));
        when(patientService.listPatients()).thenReturn(List.of());
        when(appointmentService.listAppointments()).thenReturn(List.of());
        when(treatmentService.listAllTreatments()).thenReturn(List.of());
        when(doctorService.listDoctors()).thenReturn(List.of());

        var result = mockMvc.perform(post("/DoctorLoginServlet")
                        .param("username", "admin@gmail.com")
                        .param("password", "admin-pw"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("admin.jsp"))
                .andReturn();

        HttpSession session = result.getRequest().getSession(false);
        org.junit.jupiter.api.Assertions.assertNotNull(session);
        org.junit.jupiter.api.Assertions.assertEquals(List.of(), session.getAttribute("listDoctors"));
    }

    @Test
    void login_forwardsToIndexWithMessage_forWrongCredentials() throws Exception {
        when(doctorService.login("john.smith", "wrong")).thenReturn(Optional.empty());

        mockMvc.perform(post("/DoctorLoginServlet")
                        .param("username", "john.smith")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.jsp"))
                .andExpect(request().attribute("message", "Invalid email/password"));
    }

    @Test
    void logout_doesNothing_whenNoOneIsLoggedIn() throws Exception {
        mockMvc.perform(get("/DoctorLogoutServlet"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl(null));

        verify(doctorService, never()).registerLogout(anyInt());
    }

    @Test
    void logout_closesTheSessionsOwnLoginHistoryRow_andRemovesPatientAndDoctorAttributes() throws Exception {
        Doctor self = new Doctor();
        self.setLoginID(7);

        var session = new org.springframework.mock.web.MockHttpSession();
        session.setAttribute("doctor", self);
        session.setAttribute("patient", "irrelevant-marker");

        mockMvc.perform(get("/DoctorLogoutServlet").session(session))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.jsp"));

        verify(doctorService).registerLogout(7);
        org.junit.jupiter.api.Assertions.assertNull(session.getAttribute("doctor"));
        org.junit.jupiter.api.Assertions.assertNull(session.getAttribute("patient"));
    }

    @Test
    void register_returnsForbidden_whenNotLoggedInAsAdmin() throws Exception {
        mockMvc.perform(post("/DoctorRegisterServlet")
                        .param("username", "john.smith")
                        .param("password", "pw")
                        .param("firstname", "John")
                        .param("lastname", "Smith")
                        .param("nationalid", "N1")
                        .param("dateofbirth", "1980-05-05")
                        .param("sex", "M")
                        .param("address", "2 Main St")
                        .param("city", "Springfield")
                        .param("weight", "80")
                        .param("height", "180")
                        .param("qualifications", "MD")
                        .param("speciality", "Cardiology")
                        .param("departmentcode", "CARD"))
                .andExpect(status().isForbidden());

        verify(doctorService, never()).register(any(), any(), any(), any(), any(), any(), any(), any(), any(),
                anyInt(), anyInt(), any(), any(), any());
    }

    @Test
    void register_createsAccount_whenLoggedInAsAdmin_andUserNameIsNotTaken() throws Exception {
        var session = new org.springframework.mock.web.MockHttpSession();
        session.setAttribute("doctor", admin());
        when(doctorService.isUsernameTaken("john.smith")).thenReturn(false);
        when(doctorService.listDoctors()).thenReturn(List.of());

        mockMvc.perform(post("/DoctorRegisterServlet")
                        .session(session)
                        .param("username", "john.smith")
                        .param("password", "pw")
                        .param("firstname", "John")
                        .param("lastname", "Smith")
                        .param("nationalid", "N1")
                        .param("dateofbirth", "1980-05-05")
                        .param("sex", "M")
                        .param("address", "2 Main St")
                        .param("city", "Springfield")
                        .param("weight", "80")
                        .param("height", "180")
                        .param("qualifications", "MD")
                        .param("speciality", "Cardiology")
                        .param("departmentcode", "CARD"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("admin.jsp"))
                .andExpect(request().attribute("message", "User successfully created"));

        verify(doctorService).register(eq("john.smith"), eq("pw"), eq("John"), eq("Smith"), eq("N1"), any(),
                eq("M"), eq("2 Main St"), eq("Springfield"), eq(80), eq(180), eq("MD"), eq("Cardiology"), eq("CARD"));

        // Regression check: registering a new doctor must not overwrite the
        // acting admin's own "doctor" session attribute with the new doctor.
        org.assertj.core.api.Assertions.assertThat(((Doctor) session.getAttribute("doctor")).getUserName())
                .isEqualTo("admin@gmail.com");
    }

    @Test
    void register_showsAlreadyExistsMessage_whenUserNameIsTaken() throws Exception {
        var session = new org.springframework.mock.web.MockHttpSession();
        session.setAttribute("doctor", admin());
        when(doctorService.isUsernameTaken("john.smith")).thenReturn(true);

        mockMvc.perform(post("/DoctorRegisterServlet")
                        .session(session)
                        .param("username", "john.smith")
                        .param("password", "pw")
                        .param("firstname", "John")
                        .param("lastname", "Smith")
                        .param("nationalid", "N1")
                        .param("dateofbirth", "1980-05-05")
                        .param("sex", "M")
                        .param("address", "2 Main St")
                        .param("city", "Springfield")
                        .param("weight", "80")
                        .param("height", "180")
                        .param("qualifications", "MD")
                        .param("speciality", "Cardiology")
                        .param("departmentcode", "CARD"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("admin.jsp"))
                .andExpect(request().attribute("message", "User already exists"));

        verify(doctorService, never()).register(any(), any(), any(), any(), any(), any(), any(), any(), any(),
                anyInt(), anyInt(), any(), any(), any());
    }

    @Test
    void delete_returnsForbidden_whenNotLoggedInAsAdmin() throws Exception {
        var session = new org.springframework.mock.web.MockHttpSession();
        Doctor nonAdmin = new Doctor();
        nonAdmin.setUserName("john.smith");
        session.setAttribute("doctor", nonAdmin);

        mockMvc.perform(post("/DoctorDeleteServlet")
                        .param("loginID", "7")
                        .session(session))
                .andExpect(status().isForbidden());

        verify(doctorService, never()).deleteDoctor(anyInt());
    }

    @Test
    void delete_deletesAndForwardsToAdminPage_whenLoggedInAsAdmin() throws Exception {
        var session = new org.springframework.mock.web.MockHttpSession();
        session.setAttribute("doctor", admin());
        when(patientService.listPatients()).thenReturn(List.of());
        when(appointmentService.listAppointments()).thenReturn(List.of());
        when(treatmentService.listAllTreatments()).thenReturn(List.of());
        when(doctorService.listDoctors()).thenReturn(List.of());

        mockMvc.perform(post("/DoctorDeleteServlet")
                        .param("loginID", "7")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("admin.jsp"));

        verify(doctorService).deleteDoctor(7);
    }

    @Test
    void updatePassword_returnsForbidden_whenNotLoggedIn() throws Exception {
        mockMvc.perform(post("/UpdateDoctorPasswordServlet")
                        .param("username", "john.smith")
                        .param("password", "new-pw"))
                .andExpect(status().isForbidden());

        verify(doctorService, never()).updatePassword(any(), any());
    }

    @Test
    void updatePassword_returnsForbidden_whenUserNameDoesNotMatchTheLoggedInSession() throws Exception {
        // Regression check: the legacy UpdateDoctorPasswordServlet trusted
        // the "username" request parameter with no session check at all,
        // letting anyone change any account's password.
        Doctor self = new Doctor();
        self.setUserName("john.smith");
        var session = new org.springframework.mock.web.MockHttpSession();
        session.setAttribute("doctor", self);

        mockMvc.perform(post("/UpdateDoctorPasswordServlet")
                        .param("username", "someone.else")
                        .param("password", "new-pw")
                        .session(session))
                .andExpect(status().isForbidden());

        verify(doctorService, never()).updatePassword(any(), any());
    }

    @Test
    void updatePassword_setsSessionDoctor_andForwardsHome_whenCallerIsTheSameLoggedInNonAdminUser() throws Exception {
        Doctor self = new Doctor();
        self.setUserName("john.smith");
        var session = new org.springframework.mock.web.MockHttpSession();
        session.setAttribute("doctor", self);

        Doctor updated = new Doctor();
        updated.setUserName("john.smith");
        when(doctorService.updatePassword("john.smith", "new-pw")).thenReturn(Optional.of(updated));

        mockMvc.perform(post("/UpdateDoctorPasswordServlet")
                        .param("username", "john.smith")
                        .param("password", "new-pw")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("home.jsp"));
    }

    @Test
    void updatePassword_forwardsToAdminPage_whenCallerIsTheAdminAccount() throws Exception {
        var session = new org.springframework.mock.web.MockHttpSession();
        session.setAttribute("doctor", admin());

        Doctor updated = admin();
        when(doctorService.updatePassword("admin@gmail.com", "new-pw")).thenReturn(Optional.of(updated));

        mockMvc.perform(post("/UpdateDoctorPasswordServlet")
                        .param("username", "admin@gmail.com")
                        .param("password", "new-pw")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("admin.jsp"));
    }

    @Test
    void listDoctors_returnsForbidden_whenNotLoggedInAsAdmin() throws Exception {
        mockMvc.perform(post("/ListDoctorsServlet"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listDoctors_forwardsToAdminPage_withThePluralListDoctorsAttribute_whenLoggedInAsAdmin() throws Exception {
        // Regression check: this used to be set under the singular
        // "listDoctor", which admin.jsp doesn't read.
        var session = new org.springframework.mock.web.MockHttpSession();
        session.setAttribute("doctor", admin());
        when(doctorService.listDoctors()).thenReturn(List.of());

        mockMvc.perform(post("/ListDoctorsServlet").session(session))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("admin.jsp"))
                .andExpect(request().attribute("listDoctors", List.of()));
    }
}
