package com.onlinehealthsystem;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDateTime;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
 
@WebServlet("/DoctorLogoutServlet")
public class DoctorLogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
 
    public DoctorLogoutServlet() {
        super();
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Doctor doctor = (session != null) ? (Doctor) session.getAttribute("doctor") : null;

        DoctorDAO doctorDAO = new DoctorDAO();

		if (session != null && doctor != null) {
			try {
				doctorDAO.registerLogout(doctor.getLoginID(), LocalDateTime.now());
			} catch (ClassNotFoundException | SQLException e) {
				String message = "Invalid loginID";
                request.setAttribute("message", message);
			}
            
			session.removeAttribute("patient");
		    session.removeAttribute("doctor");
		     
		    RequestDispatcher dispatcher = request.getRequestDispatcher("index.jsp");
		    dispatcher.forward(request, response);
		}
    }
}

