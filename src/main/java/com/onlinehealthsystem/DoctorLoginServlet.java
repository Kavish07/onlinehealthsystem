package com.onlinehealthsystem;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/DoctorLoginServlet")
public class DoctorLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
 
    public DoctorLoginServlet() {
        super();
    }
 
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String userPassword = request.getParameter("password");
        String userName = request.getParameter("username");
         
        DoctorDAO doctorDao = new DoctorDAO();
        PatientDAO patientDao = new PatientDAO();
        AppointmentDAO appointmentDao = new AppointmentDAO();
        TreatmentDAO treatmentDao = new TreatmentDAO();
         
        try {
            Doctor doctor = doctorDao.checkLogin(userName, userPassword, LocalDateTime.now());
            String destPage = "index.jsp";
             
            if (doctor != null) {
                HttpSession session = request.getSession();
                session.setAttribute("doctor", doctor);
                
            	List<Patient> listPatients = patientDao.listPatients();                    
                session.setAttribute("listPatients", listPatients);
                
                List<Appointment> listAppointments = appointmentDao.listAppointments();                    
                session.setAttribute("listAppointments", listAppointments);
                
                List<Treatment> listAllTreatments = treatmentDao.listAllTreatments();                    
                session.setAttribute("listAllTreatments", listAllTreatments);
                
                if (doctor.getUserName().equals("admin@gmail.com")) {
                    
                    List<Doctor> listDoctors = doctorDao.listDoctors();                    
                    session.setAttribute("listDoctors", listDoctors);
                    
                	destPage = "admin.jsp";
                } else {
                	destPage = "home2.jsp";
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

