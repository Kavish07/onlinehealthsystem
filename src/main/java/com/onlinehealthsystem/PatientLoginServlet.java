package com.onlinehealthsystem;

import java.io.*;
import java.sql.SQLException;
import java.util.List;
import java.time.*;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/PatientLoginServlet")
public class PatientLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
 
    public PatientLoginServlet() {
        super();
    }
 
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String userPassword = request.getParameter("password");
        String userName = request.getParameter("username");
         
        PatientDAO patientDao = new PatientDAO();
        DoctorDAO doctorDao = new DoctorDAO();
        TreatmentDAO treatmentDao = new TreatmentDAO();
         
        try {
            Patient patient = patientDao.checkLogin(userName, userPassword, LocalDateTime.now());
            String destPage = "index.jsp";
             
            if (patient != null) {
                HttpSession session = request.getSession();
                session.setAttribute("patient", patient);
                
                List<Doctor> listDoctors = doctorDao.listDoctors();                    
                session.setAttribute("listDoctors", listDoctors);
                
                List<Treatment> listTreatments = treatmentDao.listTreatments(patient.getLoginID());                    
                session.setAttribute("listTreatments", listTreatments);
                
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

