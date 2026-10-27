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

@WebServlet("/ListTreatmentsServlet")
public class ListTreatmentsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public ListTreatmentsServlet() {
        super();
    }
    
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
        TreatmentDAO treatmentDao = new TreatmentDAO();
         
        try {
        	HttpSession session = request.getSession();
        	Patient patient = (Patient) session.getAttribute("patient");
        	
        	List<Treatment> listTreatment = treatmentDao.listTreatments(patient.getLoginID());
            String destPage = "treatments.jsp";
            
            request.setAttribute("listTreatment", listTreatment);
             
            RequestDispatcher dispatcher = request.getRequestDispatcher(destPage);
            dispatcher.forward(request, response);
        } catch (SQLException | ClassNotFoundException ex) {
            throw new ServletException(ex);
        }
	}
}

