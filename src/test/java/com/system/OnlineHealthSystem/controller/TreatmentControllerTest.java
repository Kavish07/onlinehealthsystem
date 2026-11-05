package com.system.OnlineHealthSystem.controller;

import com.onlinehealthsystem.Doctor;
import com.onlinehealthsystem.Patient;
import com.onlinehealthsystem.Treatment;
import com.system.OnlineHealthSystem.service.TreatmentService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TreatmentController.class)
class TreatmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TreatmentService treatmentService;

    private Doctor loggedInDoctor() {
        Doctor doctor = new Doctor();
        doctor.setLoginID(42);
        doctor.setUserName("john.smith");
        return doctor;
    }

    private Patient loggedInPatient() {
        Patient patient = new Patient();
        patient.setLoginID(7);
        patient.setUserName("jane.doe");
        return patient;
    }

    @Test
    void newTreatment_returnsForbidden_whenNoDoctorIsLoggedIn() throws Exception {
        mockMvc.perform(post("/NewTreatmentServlet")
                        .param("dateoftreatment", "2026-01-15")
                        .param("location", "Room 1")
                        .param("department", "CARD")
                        .param("treatment", "Checkup")
                        .param("medicines", "Paracetamol")
                        .param("patient", "7")
                        .param("bill", "100")
                        .param("dateofbill", "2026-01-15"))
                .andExpect(status().isForbidden());

        verify(treatmentService, never()).registerTreatment(any(), anyString(), anyString(), anyString(),
                anyString(), anyInt(), anyInt(), anyInt(), any());
    }

    @Test
    void newTreatment_registersTheTreatment_andForwardsToHome2_withAMessage_whenDoctorIsLoggedIn() throws Exception {
        var session = new MockHttpSession();
        session.setAttribute("doctor", loggedInDoctor());

        mockMvc.perform(post("/NewTreatmentServlet")
                        .session(session)
                        .param("dateoftreatment", "2026-01-15")
                        .param("location", "Room 1")
                        .param("department", "CARD")
                        .param("treatment", "Checkup")
                        .param("medicines", "Paracetamol")
                        .param("patient", "7")
                        .param("bill", "100")
                        .param("dateofbill", "2026-01-15"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("home2.jsp"))
                // Matches the legacy NewTreatmentServlet's own (copy-pasted)
                // message text, preserved as-is rather than "fixed".
                .andExpect(request().attribute("message", "Appointment successfully created"));

        verify(treatmentService).registerTreatment(LocalDate.of(2026, 1, 15), "Room 1", "CARD", "Checkup",
                "Paracetamol", 7, 42, 100, LocalDate.of(2026, 1, 15));
    }

    @Test
    void updateTreatment_returnsForbidden_whenNoDoctorIsLoggedIn() throws Exception {
        mockMvc.perform(post("/UpdateTreatmentServlet")
                        .param("treatmentID", "99")
                        .param("dateoftreatment", "2026-02-01")
                        .param("location", "Room 2")
                        .param("department", "CARD")
                        .param("treatment", "Follow-up")
                        .param("medicines", "Ibuprofen")
                        .param("patientID", "7")
                        .param("billID", "55")
                        .param("bill", "200")
                        .param("dateofbill", "2026-02-01"))
                .andExpect(status().isForbidden());

        verify(treatmentService, never()).updateTreatment(anyInt(), any(), anyString(), anyString(), anyString(),
                anyString(), anyInt(), anyInt(), any(), anyBoolean());
    }

    @Test
    void updateTreatment_updatesAndForwardsToHome2_andSetsSessionListAllTreatments_whenDoctorIsLoggedIn()
            throws Exception {
        var session = new MockHttpSession();
        session.setAttribute("doctor", loggedInDoctor());
        Treatment treatment = new Treatment();
        when(treatmentService.listAllTreatments()).thenReturn(List.of(treatment));

        var result = mockMvc.perform(post("/UpdateTreatmentServlet")
                        .session(session)
                        .param("treatmentID", "99")
                        .param("dateoftreatment", "2026-02-01")
                        .param("location", "Room 2")
                        .param("department", "CARD")
                        .param("treatment", "Follow-up")
                        .param("medicines", "Ibuprofen")
                        .param("patientID", "7")
                        .param("billID", "55")
                        .param("bill", "200")
                        .param("dateofbill", "2026-02-01"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("home2.jsp"))
                .andExpect(request().attribute("message", "Appointment successfully created"))
                .andReturn();

        verify(treatmentService).updateTreatment(99, LocalDate.of(2026, 2, 1), "Room 2", "CARD", "Follow-up",
                "Ibuprofen", 55, 200, LocalDate.of(2026, 2, 1), false);
        HttpSession resultSession = result.getRequest().getSession(false);
        assertThat(resultSession).isNotNull();
        assertThat(resultSession.getAttribute("listAllTreatments")).isEqualTo(List.of(treatment));
    }

    @Test
    void listTreatments_returnsForbidden_whenNoPatientIsLoggedIn() throws Exception {
        mockMvc.perform(post("/ListTreatmentsServlet"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listTreatments_forwardsToTreatmentsJsp_withThePluralListTreatmentsRequestAttribute() throws Exception {
        // Regression check: this used to be set under the singular
        // "listTreatment", which treatments.jsp doesn't read.
        var session = new MockHttpSession();
        session.setAttribute("patient", loggedInPatient());
        Treatment treatment = new Treatment();
        when(treatmentService.listTreatments(7)).thenReturn(List.of(treatment));

        mockMvc.perform(post("/ListTreatmentsServlet").session(session))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("treatments.jsp"))
                .andExpect(request().attribute("listTreatments", List.of(treatment)));
    }
}
