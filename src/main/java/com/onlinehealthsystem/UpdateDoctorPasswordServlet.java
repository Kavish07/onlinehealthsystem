package com.onlinehealthsystem;

import java.io.*;
import java.sql.SQLException;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/UpdateDoctorPasswordServlet")
public class UpdateDoctorPasswordServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public UpdateDoctorPasswordServlet() {
        super();
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String userPassword = request.getParameter("password");
        String userName = request.getParameter("username");
         
        DoctorDAO doctorDao = new DoctorDAO();
         
        try {
        	Doctor doctor = doctorDao.updatePassword(userName, userPassword);
            String destPage = "home.jsp";
            
            if (doctor != null) {
	            HttpSession session = request.getSession();
	            session.setAttribute("doctor", doctor);
	             
	            if (doctor.getUserName().equals("admin@gmail.com")) {
	                destPage = "admin.jsp";
	            } else {
	            	destPage = "home.jsp";
	            }
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
