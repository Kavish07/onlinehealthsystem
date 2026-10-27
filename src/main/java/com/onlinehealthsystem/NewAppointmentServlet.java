package com.onlinehealthsystem;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/NewAppointmentServlet")
public class NewAppointmentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

	public NewAppointmentServlet() {
        super();
    }
 
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Patient patient = (session != null) ? (Patient) session.getAttribute("patient") : null;

        if (patient == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Date appointmentDate = Date.valueOf(request.getParameter("dateofappointment"));
        String location = request.getParameter("location");
        String departmentCode = request.getParameter("department");
        int patientID = patient.getLoginID();
        int doctorID = Integer.parseInt(request.getParameter("doctor"));

        AppointmentDAO appointmentDao = new AppointmentDAO();
         
        try {
            Appointment appointment = null;
            String destPage = "home.jsp";
             
            appointment = appointmentDao.registerAppointment(appointmentDate, location, departmentCode, patientID, doctorID);

            session.setAttribute("appointment", appointment);
            String message = "Appointment successfully created";
            request.setAttribute("message", message);
             
            RequestDispatcher dispatcher = request.getRequestDispatcher(destPage);
            dispatcher.forward(request, response);
             
        } catch (SQLException | ClassNotFoundException ex) {
            throw new ServletException(ex);
        }
    }
}
