package com.onlinehealthsystem;

import java.io.*;
import java.sql.SQLException;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/UpdatePatientPasswordServlet")
public class UpdatePatientPasswordServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public UpdatePatientPasswordServlet() {
        super();
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String userPassword = request.getParameter("password");
        String userName = request.getParameter("username");
         
        PatientDAO patientDao = new PatientDAO();
         
        try {
        	Patient patient = patientDao.updatePassword(userName, userPassword);
            String destPage = "home.jsp";
            
            if (patient != null) {
	            HttpSession session = request.getSession();
	            session.setAttribute("patient", patient);
	             
	            destPage = "home.jsp";
	            
            } else {
                String message = "Invalid email/password";
                request.setAttribute("message", message);
            }
            
            RequestDispatcher dispatcher = request.getRequestDispatcher(destPage);
            dispatcher.forward(request, response);
             
        } catch (SQLException | ClassNotFoundException ex) {
            throw new ServletException(ex);
        }
    }
}
