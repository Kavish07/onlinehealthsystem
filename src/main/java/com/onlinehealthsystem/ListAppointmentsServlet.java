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

@WebServlet("/ListAppointmentsServlet")
public class ListAppointmentsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public ListAppointmentsServlet() {
        super();
    }
    
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
        AppointmentDAO appointmentDao = new AppointmentDAO();
         
        try {
        	List<Appointment> listAppointment = appointmentDao.listAppointments();
            String destPage = "appointments2.jsp";
            
            request.setAttribute("listAppointment", listAppointment);
             
            RequestDispatcher dispatcher = request.getRequestDispatcher(destPage);
            dispatcher.forward(request, response);
        } catch (SQLException | ClassNotFoundException ex) {
            throw new ServletException(ex);
        }
	}
}
