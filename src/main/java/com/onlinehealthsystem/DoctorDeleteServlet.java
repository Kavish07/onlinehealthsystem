package com.onlinehealthsystem;

import java.io.*;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/DoctorDeleteServlet")
public class DoctorDeleteServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public DoctorDeleteServlet() {
        super();
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int loginID = Integer.parseInt(request.getParameter("loginID"));
         
        DoctorDAO doctorDao = new DoctorDAO();
        PatientDAO patientDao = new PatientDAO();
        AppointmentDAO appointmentDao = new AppointmentDAO();
        TreatmentDAO treatmentDao = new TreatmentDAO();
         
        try {
        	Doctor doctor = doctorDao.deleteDoctor(loginID);
            String destPage = "admin.jsp";
            
            if (doctor != null) {
	            HttpSession session = request.getSession();
	            session.setAttribute("doctor", doctor);
	            
	            List<Patient> listPatients = patientDao.listPatients();                    
                session.setAttribute("listPatients", listPatients);
                
                List<Appointment> listAppointments = appointmentDao.listAppointments();                    
                session.setAttribute("listAppointments", listAppointments);
                
                List<Treatment> listAllTreatments = treatmentDao.listAllTreatments();                    
                session.setAttribute("listAllTreatments", listAllTreatments);
                
                List<Doctor> listDoctors = doctorDao.listDoctors();                    
                session.setAttribute("listDoctors", listDoctors);
	            
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

