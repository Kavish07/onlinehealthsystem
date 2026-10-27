package com.onlinehealthsystem;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/ConfirmAppointmentServlet")
public class ConfirmAppointmentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public ConfirmAppointmentServlet() {
        super();
    }
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Doctor doctor = (session != null) ? (Doctor) session.getAttribute("doctor") : null;

        if (doctor == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        int appointmentId = Integer.parseInt(request.getParameter("appointmentID"));

        AppointmentDAO appointmentDao = new AppointmentDAO();

        try {
            Appointment appointment = null;
            String destPage = "home2.jsp";

            appointment = appointmentDao.confirmAppointment(appointmentId);

            session.setAttribute("appointment", appointment);

            List<Appointment> listAppointments = appointmentDao.listAppointments();
            session.setAttribute("listAppointments", listAppointments);

            String message = "Appointment successfully updated";
            request.setAttribute("message", message);

            if (doctor.getUserName().equals("admin@gmail.com")) {
            	destPage = "admin.jsp";
            } else {
            	destPage = "home2.jsp";
            }
             
            RequestDispatcher dispatcher = request.getRequestDispatcher(destPage);
            dispatcher.forward(request, response);
             
        } catch (SQLException | ClassNotFoundException ex) {
            throw new ServletException(ex);
        }
    }

}
