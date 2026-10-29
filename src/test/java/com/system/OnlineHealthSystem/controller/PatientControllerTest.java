package com.system.OnlineHealthSystem.controller;

import com.onlinehealthsystem.AppointmentDAO;
import com.onlinehealthsystem.Doctor;
import com.onlinehealthsystem.DoctorDAO;
import com.onlinehealthsystem.TreatmentDAO;
import com.system.OnlineHealthSystem.service.PatientService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

@WebMvcTest(PatientController.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @MockBean
    private DoctorDAO doctorDAO;

    @MockBean
    private TreatmentDAO treatmentDAO;

    @MockBean
    private AppointmentDAO appointmentDAO;

    @Test
    void login_forwardsToHomePage_andSetsSessionAttributes_forCorrectCredentials() throws Exception {
        com.onlinehealthsystem.Patient patient = new com.onlinehealthsystem.Patient();
        patient.setLoginID(7);
        patient.setUserName("jane.doe");
        when(patientService.login("jane.doe", "s3cret-pw")).thenReturn(Optional.of(patient));
        when(doctorDAO.listDoctors()).thenReturn(List.of());
        when(treatmentDAO.listTreatments(7)).thenReturn(List.of());

        var result = mockMvc.perform(post("/PatientLoginServlet")
                        .param("username", "jane.doe")
                        .param("password", "s3cret-pw"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("home.jsp"))
                .andReturn();

        HttpSession session = result.getRequest().getSession(false);
        org.junit.jupiter.api.Assertions.assertNotNull(session);
        org.junit.jupiter.api.Assertions.assertSame(patient, session.getAttribute("patient"));
    }

    @Test
    void login_forwardsToIndexWithMessage_forWrongCredentials() throws Exception {
        when(patientService.login("jane.doe", "wrong")).thenReturn(Optional.empty());

        mockMvc.perform(post("/PatientLoginServlet")
                        .param("username", "jane.doe")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.jsp"))
                .andExpect(request().attribute("message", "Invalid email/password"));
    }

    @Test
    void logout_doesNothing_whenNoOneIsLoggedIn() throws Exception {
        mockMvc.perform(get("/PatientLogoutServlet"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl(null));

        verify(patientService, never()).registerLogout(anyInt());
    }

    @Test
    void logout_closesTheSessionsOwnLoginHistoryRow_ignoringAnyLoginIdParameter() throws Exception {
        // Regression check: PatientLogoutServlet used to trust a "loginID"
        // request parameter instead of the session's own identity.
        com.onlinehealthsystem.Patient self = new com.onlinehealthsystem.Patient();
        self.setLoginID(7);

        var session = new org.springframework.mock.web.MockHttpSession();
        session.setAttribute("patient", self);

        mockMvc.perform(get("/PatientLogoutServlet")
                        .param("loginID", "999")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.jsp"));

        verify(patientService).registerLogout(7);
        org.junit.jupiter.api.Assertions.assertNull(session.getAttribute("patient"));
    }

    @Test
    void delete_returnsForbidden_whenNotLoggedInAsAdmin() throws Exception {
        var session = new org.springframework.mock.web.MockHttpSession();
        Doctor nonAdmin = new Doctor();
        nonAdmin.setUserName("john.smith");
        session.setAttribute("doctor", nonAdmin);

        mockMvc.perform(post("/PatientDeleteServlet")
                        .param("loginID", "7")
                        .session(session))
                .andExpect(status().isForbidden());

        verify(patientService, never()).deletePatient(anyInt());
    }

    @Test
    void delete_deletesAndForwardsToAdminPage_whenLoggedInAsAdmin() throws Exception {
        var session = new org.springframework.mock.web.MockHttpSession();
        Doctor admin = new Doctor();
        admin.setUserName("admin@gmail.com");
        session.setAttribute("doctor", admin);
        when(doctorDAO.listDoctors()).thenReturn(List.of());
        when(appointmentDAO.listAppointments()).thenReturn(List.of());
        when(treatmentDAO.listAllTreatments()).thenReturn(List.of());
        when(patientService.listPatients()).thenReturn(List.of());

        mockMvc.perform(post("/PatientDeleteServlet")
                        .param("loginID", "7")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("admin.jsp"));

        verify(patientService).deletePatient(7);
    }

    @Test
    void listPatients_returnsForbidden_whenNotLoggedInAsAdmin() throws Exception {
        mockMvc.perform(post("/ListPatientsServlet"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listPatients_forwardsToAdminPage_withThePatientList_whenLoggedInAsAdmin() throws Exception {
        var session = new org.springframework.mock.web.MockHttpSession();
        Doctor admin = new Doctor();
        admin.setUserName("admin@gmail.com");
        session.setAttribute("doctor", admin);
        when(patientService.listPatients()).thenReturn(List.of());

        mockMvc.perform(post("/ListPatientsServlet").session(session))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("admin.jsp"))
                .andExpect(request().attribute("listPatient", List.of()));
    }

    @Test
    void updatePassword_setsSessionPatient_andForwardsHome_whenCallerIsTheSameLoggedInUser() throws Exception {
        com.onlinehealthsystem.Patient self = new com.onlinehealthsystem.Patient();
        self.setUserName("jane.doe");
        var session = new org.springframework.mock.web.MockHttpSession();
        session.setAttribute("patient", self);

        com.onlinehealthsystem.Patient updated = new com.onlinehealthsystem.Patient();
        updated.setUserName("jane.doe");
        when(patientService.updatePassword("jane.doe", "new-pw")).thenReturn(Optional.of(updated));

        mockMvc.perform(post("/UpdatePatientPasswordServlet")
                        .param("username", "jane.doe")
                        .param("password", "new-pw")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("home.jsp"));
    }

    @Test
    void updatePassword_returnsForbidden_whenNotLoggedIn() throws Exception {
        mockMvc.perform(post("/UpdatePatientPasswordServlet")
                        .param("username", "jane.doe")
                        .param("password", "new-pw"))
                .andExpect(status().isForbidden());

        verify(patientService, never()).updatePassword(any(), any());
    }

    @Test
    void updatePassword_returnsForbidden_whenUserNameDoesNotMatchTheLoggedInSession() throws Exception {
        // Regression check: UpdatePatientPasswordServlet used to trust the
        // "username" request parameter with no session check at all,
        // letting anyone change any account's password.
        com.onlinehealthsystem.Patient self = new com.onlinehealthsystem.Patient();
        self.setUserName("jane.doe");
        var session = new org.springframework.mock.web.MockHttpSession();
        session.setAttribute("patient", self);

        mockMvc.perform(post("/UpdatePatientPasswordServlet")
                        .param("username", "someone.else")
                        .param("password", "new-pw")
                        .session(session))
                .andExpect(status().isForbidden());

        verify(patientService, never()).updatePassword(any(), any());
    }

    @Test
    void register_createsAccount_whenUserNameIsNotTaken() throws Exception {
        when(patientService.isUsernameTaken("jane.doe")).thenReturn(false);
        com.onlinehealthsystem.Patient created = new com.onlinehealthsystem.Patient();
        created.setUserName("jane.doe");
        when(patientService.register(eq("jane.doe"), eq("s3cret-pw"), eq("Jane"), eq("Doe"), eq("N1"),
                any(), eq("F"), eq("1 Main St"), eq("Springfield"), eq(65), eq(170)))
                .thenReturn(created);

        mockMvc.perform(post("/PatientRegisterServlet")
                        .param("username", "jane.doe")
                        .param("password", "s3cret-pw")
                        .param("firstname", "Jane")
                        .param("lastname", "Doe")
                        .param("nationalid", "N1")
                        .param("dateofbirth", "1990-01-01")
                        .param("sex", "F")
                        .param("address", "1 Main St")
                        .param("city", "Springfield")
                        .param("weight", "65")
                        .param("height", "170"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.jsp"))
                .andExpect(request().attribute("message", "User successfully created"));
    }

    @Test
    void register_showsAlreadyExistsMessage_whenUserNameIsTaken() throws Exception {
        when(patientService.isUsernameTaken("jane.doe")).thenReturn(true);

        mockMvc.perform(post("/PatientRegisterServlet")
                        .param("username", "jane.doe")
                        .param("password", "s3cret-pw")
                        .param("firstname", "Jane")
                        .param("lastname", "Doe")
                        .param("nationalid", "N1")
                        .param("dateofbirth", "1990-01-01")
                        .param("sex", "F")
                        .param("address", "1 Main St")
                        .param("city", "Springfield")
                        .param("weight", "65")
                        .param("height", "170"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.jsp"))
                .andExpect(request().attribute("message", "User already exists"));

        verify(patientService, never()).register(any(), any(), any(), any(), any(), any(), any(), any(), any(),
                anyInt(), anyInt());
    }
}
