package com.onlinehealthsystem;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/NewTreatmentServlet")
public class NewTreatmentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public NewTreatmentServlet() {
        super();
    }
 
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Date treatmentDate = Date.valueOf(request.getParameter("dateoftreatment"));
        String location = request.getParameter("location");
        String departmentCode = request.getParameter("department");
        String treatmentName = request.getParameter("treatment");
        String medicineCode = request.getParameter("medicines");
        int patientID = Integer.parseInt(request.getParameter("patient")); 
        int doctorID = Integer.parseInt(request.getParameter("doctorID"));
        int billAmount = Integer.parseInt(request.getParameter("bill"));
        Date billDate = Date.valueOf(request.getParameter("dateofbill"));
        
        TreatmentDAO treatmentDao = new TreatmentDAO();
         
        try {
        	Treatment treatment = null;
            String destPage = "home2.jsp";
             
            treatment = treatmentDao.registerTreatment(treatmentDate, location, departmentCode, treatmentName, medicineCode, patientID, doctorID, billAmount, billDate);
            	
            HttpSession session = request.getSession();
            session.setAttribute("appointment", treatment);
            String message = "Appointment successfully created";
            request.setAttribute("message", message);
       
            RequestDispatcher dispatcher = request.getRequestDispatcher(destPage);
            dispatcher.forward(request, response);
             
        } catch (SQLException | ClassNotFoundException ex) {
            throw new ServletException(ex);
        }
    }
}
