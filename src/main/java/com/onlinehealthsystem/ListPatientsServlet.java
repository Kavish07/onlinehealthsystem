package com.onlinehealthsystem;

import java.io.*;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/ListPatientsServlet")
public class ListPatientsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public ListPatientsServlet() {
        super();
    }
    
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
        PatientDAO patientDao = new PatientDAO();
         
        try {
        	List<Patient> listPatient = patientDao.listPatients();
            String destPage = "admin.jsp";
            
            request.setAttribute("listPatient", listPatient);
             
            RequestDispatcher dispatcher = request.getRequestDispatcher(destPage);
            dispatcher.forward(request, response);
        } catch (SQLException | ClassNotFoundException ex) {
            throw new ServletException(ex);
        }
	}

}

