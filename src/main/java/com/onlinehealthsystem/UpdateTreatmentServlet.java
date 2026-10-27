package com.onlinehealthsystem;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/UpdateTreatmentServlet")
public class UpdateTreatmentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public UpdateTreatmentServlet() {
        super();
    }
 
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Doctor doctor = (session != null) ? (Doctor) session.getAttribute("doctor") : null;

        if (doctor == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

    	int treatmentID = Integer.parseInt(request.getParameter("treatmentID"));
        Date treatmentDate = Date.valueOf(request.getParameter("dateoftreatment"));
        String location = request.getParameter("location");
        String departmentCode = request.getParameter("department");
        String treatmentName = request.getParameter("treatment");
        String medicineCode = request.getParameter("medicines");
        int patientID = Integer.parseInt(request.getParameter("patientID"));
        int doctorID = doctor.getLoginID();
        int billID = Integer.parseInt(request.getParameter("billID"));
        int billAmount = Integer.parseInt(request.getParameter("bill"));
        Date billDate = Date.valueOf(request.getParameter("dateofbill"));
        
        TreatmentDAO treatmentDao = new TreatmentDAO();
         
        try {
        	Treatment treatment = null;
            String destPage = "home2.jsp";
             
            treatment = treatmentDao.updateTreatment(treatmentID, treatmentDate, location, departmentCode, treatmentName, medicineCode, patientID, doctorID, billID, billAmount, billDate);

            session.setAttribute("appointment", treatment);
            
            List<Treatment> listAllTreatments = treatmentDao.listAllTreatments();                    
            session.setAttribute("listAllTreatments", listAllTreatments);
            
            String message = "Appointment successfully created";
            request.setAttribute("message", message);
       
            RequestDispatcher dispatcher = request.getRequestDispatcher(destPage);
            dispatcher.forward(request, response);
             
        } catch (SQLException | ClassNotFoundException ex) {
            throw new ServletException(ex);
        }
    }
}

