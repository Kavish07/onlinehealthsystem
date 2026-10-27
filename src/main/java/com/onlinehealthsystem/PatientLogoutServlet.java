package com.onlinehealthsystem;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDateTime;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
 
@WebServlet("/PatientLogoutServlet")
public class PatientLogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
 
    public PatientLogoutServlet() {
        super();
    }
 
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int loginID = Integer.parseInt(request.getParameter("loginID"));
        
        PatientDAO patientDao = new PatientDAO();
        
		if (session != null) {
			try {
				patientDao.registerLogout(loginID, LocalDateTime.now());
			} catch (ClassNotFoundException | SQLException e) {
				String message = "Invalid loginID";
                request.setAttribute("message", message);
			}
            
		    session.removeAttribute("patient");
		     
		    RequestDispatcher dispatcher = request.getRequestDispatcher("index.jsp");
		    dispatcher.forward(request, response);
		}
    }
}

