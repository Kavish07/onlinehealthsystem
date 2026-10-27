package com.onlinehealthsystem;

import java.io.*;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/ListDoctorsServlet")
public class ListDoctorsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public ListDoctorsServlet() {
        super();
    }
    
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
        DoctorDAO doctorDao = new DoctorDAO();
         
        try {
        	List<Doctor> listDoctor = doctorDao.listDoctors();
            String destPage = "admin.jsp";
            
            request.setAttribute("listDoctor", listDoctor);
             
            RequestDispatcher dispatcher = request.getRequestDispatcher(destPage);
            dispatcher.forward(request, response);
        } catch (SQLException | ClassNotFoundException ex) {
            throw new ServletException(ex);
        }
	}
}

