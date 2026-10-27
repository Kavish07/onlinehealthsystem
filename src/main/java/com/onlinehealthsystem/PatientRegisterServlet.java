package com.onlinehealthsystem;

import java.io.*;
import java.sql.Date;
import java.sql.SQLException;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/PatientRegisterServlet")
public class PatientRegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
 
    public PatientRegisterServlet() {
        super();
    }
 
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
         
        PatientDAO patientDao = new PatientDAO();
         
        try {
            Patient patient = patientDao.checkRegistration(userName);
            String destPage = "index.jsp";
             
            if (patient == null) {
            	patient = patientDao.registerPatient(userName, userPassword, firstName, lastName, nationalID, dateOfBirth, sex, address, city, weight, height);
            	
            	HttpSession session = request.getSession();
                session.setAttribute("patient", patient);
            	String message = "User successfully created";
                request.setAttribute("message", message);
            } else {
            	if (patient.getUserName().equals(userName)) {
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
