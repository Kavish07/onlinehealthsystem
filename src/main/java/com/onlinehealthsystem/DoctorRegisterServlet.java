package com.onlinehealthsystem;

import java.io.*;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/DoctorRegisterServlet")
public class DoctorRegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
 
    public DoctorRegisterServlet() {
        super();
    }
 
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Doctor admin = (session != null) ? (Doctor) session.getAttribute("doctor") : null;

        if (admin == null || !admin.getUserName().equals("admin@gmail.com")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String userName = request.getParameter("username");
        String userPassword = request.getParameter("password");
        String firstName = request.getParameter("firstname");
        String lastName = request.getParameter("lastname");
        String nationalID = request.getParameter("nationalid");
        Date dateOfBirth = Date.valueOf(request.getParameter("dateofbirth"));
        String sex = request.getParameter("sex");
        String address = request.getParameter("address");
        String city = request.getParameter("city"); 
        int weight = Integer.parseInt(request.getParameter("weight")); 
        int height= Integer.parseInt(request.getParameter("height"));
        String qualifications = request.getParameter("qualifications");
        String speciality = request.getParameter("speciality");
        String departmentCode = request.getParameter("departmentcode");
         
        DoctorDAO doctorDao = new DoctorDAO();
         
        try {
            Doctor doctor = doctorDao.checkRegistration(userName);
            String destPage = "admin.jsp";
             
            if (doctor == null) {
            	doctor = doctorDao.registerDoctor(userName, userPassword, firstName, lastName, nationalID, dateOfBirth, sex, address, city, weight, height, qualifications, speciality, departmentCode);

                session.setAttribute("doctor", doctor);
                
                List<Doctor> listDoctors = doctorDao.listDoctors();                    
                session.setAttribute("listDoctors", listDoctors);
                
            	String message = "User successfully created";
                request.setAttribute("message", message);
            } else {
            	if (doctor.getUserName().equals(userName)) {
                    String message = "User already exists";
                    request.setAttribute("message", message);
                }
            }
             
            RequestDispatcher dispatcher = request.getRequestDispatcher(destPage);
            dispatcher.forward(request, response);
             
        } catch (SQLException | ClassNotFoundException ex) {
            throw new ServletException(ex);
        }
    }
}
