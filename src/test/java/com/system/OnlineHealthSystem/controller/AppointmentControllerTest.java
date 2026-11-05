package com.system.OnlineHealthSystem.controller;

import com.onlinehealthsystem.Appointment;
import com.onlinehealthsystem.Doctor;
import com.onlinehealthsystem.Patient;
import com.system.OnlineHealthSystem.service.AppointmentService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentService appointmentService;

    private Doctor admin() {
        Doctor admin = new Doctor();
        admin.setUserName("admin@gmail.com");
        return admin;
    }

    private Doctor nonAdminDoctor() {
        Doctor doctor = new Doctor();
        doctor.setUserName("john.smith");
        doctor.setLoginID(42);
        return doctor;
    }

    @Test
    void newAppointment_returnsForbidden_whenNoPatientIsLoggedIn() throws Exception {
        mockMvc.perform(post("/NewAppointmentServlet")
                        .param("dateofappointment", "2026-01-15T10:00")
                        .param("location", "Room 1")
                        .param("department", "CARD")
                        .param("doctor", "42"))
                .andExpect(status().isForbidden());

        verify(appointmentService, never()).registerAppointment(any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void newAppointment_registersTheAppointment_andForwardsToHome_withAMessage_whenPatientIsLoggedIn() throws Exception {
        Patient patient = new Patient();
        patient.setLoginID(7);
        var session = new MockHttpSession();
        session.setAttribute("patient", patient);

        mockMvc.perform(post("/NewAppointmentServlet")
                        .session(session)
                        .param("dateofappointment", "2026-01-15T10:00")
                        .param("location", "Room 1")
                        .param("department", "CARD")
                        .param("doctor", "42"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("home.jsp"))
                .andExpect(request().attribute("message", "Appointment successfully created"));

        verify(appointmentService).registerAppointment(LocalDateTime.of(2026, 1, 15, 10, 0), "Room 1", "CARD", 7, 42);
    }

    @Test
    void newAppointment_forwardsToAppointmentsWithAMessage_whenTheServiceRejectsTheDateTime() throws Exception {
        Patient patient = new Patient();
        patient.setLoginID(7);
        var session = new MockHttpSession();
        session.setAttribute("patient", patient);
        org.mockito.Mockito.doThrow(new IllegalArgumentException("The clinic is closed on Sundays"))
                .when(appointmentService)
                .registerAppointment(any(), any(), any(), anyInt(), anyInt());

        mockMvc.perform(post("/NewAppointmentServlet")
                        .session(session)
                        .param("dateofappointment", "2026-01-18T10:00")
                        .param("location", "Room 1")
                        .param("department", "CARD")
                        .param("doctor", "42"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("appointments.jsp"))
                .andExpect(request().attribute("message", "The clinic is closed on Sundays"));
    }

    @Test
    void confirmAppointment_returnsForbidden_whenNoDoctorIsLoggedIn() throws Exception {
        mockMvc.perform(post("/ConfirmAppointmentServlet").param("appointmentID", "99"))
                .andExpect(status().isForbidden());

        verify(appointmentService, never()).confirmAppointment(anyInt());
    }

    @Test
    void confirmAppointment_confirmsAndForwardsToHome2_andUpdatesSessionListAppointments_forNonAdminDoctor()
            throws Exception {
        var session = new MockHttpSession();
        session.setAttribute("doctor", nonAdminDoctor());
        when(appointmentService.listAppointments()).thenReturn(List.of());

        var result = mockMvc.perform(post("/ConfirmAppointmentServlet")
                        .session(session)
                        .param("appointmentID", "99"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("home2.jsp"))
                .andExpect(request().attribute("message", "Appointment successfully updated"))
                .andReturn();

        verify(appointmentService).confirmAppointment(99);
        HttpSession resultSession = result.getRequest().getSession(false);
        assertThat(resultSession).isNotNull();
        assertThat(resultSession.getAttribute("listAppointments")).isEqualTo(List.of());
    }

    @Test
    void confirmAppointment_forwardsToAdminPage_forTheAdminAccount() throws Exception {
        var session = new MockHttpSession();
        session.setAttribute("doctor", admin());
        when(appointmentService.listAppointments()).thenReturn(List.of());

        mockMvc.perform(post("/ConfirmAppointmentServlet")
                        .session(session)
                        .param("appointmentID", "99"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("admin.jsp"));
    }

    @Test
    void listAppointments_returnsForbidden_whenNoDoctorIsLoggedIn() throws Exception {
        mockMvc.perform(post("/ListAppointmentsServlet"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listAppointments_forwardsToAppointments2_withThePluralListAppointmentsRequestAttribute() throws Exception {
        // Regression check: this used to be set under the singular
        // "listAppointment", which appointments2.jsp doesn't read.
        var session = new MockHttpSession();
        session.setAttribute("doctor", nonAdminDoctor());
        Appointment appointment = new Appointment();
        when(appointmentService.listAppointments()).thenReturn(List.of(appointment));

        mockMvc.perform(post("/ListAppointmentsServlet").session(session))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("appointments2.jsp"))
                .andExpect(request().attribute("listAppointments", List.of(appointment)));
    }
}
