package com.system.OnlineHealthSystem.controller;

import com.onlinehealthsystem.Patient;
import com.system.OnlineHealthSystem.service.BillService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;

/**
 * Patient-facing bill list, mirroring {@code TreatmentController.listTreatments}.
 * The nav link to {@code bills.jsp} actually reads the session-scoped
 * "listBills" set by {@code PatientController.login}; this endpoint exists
 * for parity with {@code /ListTreatmentsServlet}.
 */
@Controller
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @PostMapping("/ListBillsServlet")
    public void listBills(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Patient patient = (session != null) ? (Patient) session.getAttribute("patient") : null;

        if (patient == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        request.setAttribute("listBills", billService.listBills(patient.getLoginID()));
        forward("bills.jsp", request, response);
    }

    private void forward(String page, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher(page).forward(request, response);
    }
}
