package com.onlinehealthsystem;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

	public Appointment registerAppointment(Date date, String location, String departmentCode, int patientID, int doctorID) throws SQLException, ClassNotFoundException {
        String jdbcURL = "jdbc:mysql://localhost:3306/onlinehealthsystem";
        String dbUser = "root";
        String dbPassword = "root";

        Appointment appointment = null;

        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)) {
            String sql = "insert into Appointments (Date, Location, DepartmentCode, PatientID, DoctorID, Approved) values(?,?,?,?,?, false)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1, date);
            statement.setString(2, location);
            statement.setString(3, departmentCode);
            statement.setInt(4, patientID);
            statement.setInt(5, doctorID);

            statement.executeUpdate();
        }

        return appointment;
    }

	public Appointment checkAppointment(String userName) throws SQLException, ClassNotFoundException {
        String jdbcURL = "jdbc:mysql://localhost:3306/onlinehealthsystem";
        String dbUser = "root";
        String dbPassword = "root";

        Class.forName("com.mysql.cj.jdbc.Driver");

        Appointment appointment = new Appointment();

        try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)) {
            String sql = "select * from Appointments a, Departments de, Patients p, Doctors d, Login l where a.PatientID = p.LoginID and a.DoctorID = d.LoginID and a.DepartmentCode = de.DepartmentCode and p.LoginID = l.LoginID and l.UserName = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, userName);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
            	appointment.setAppointmentDate(result.getDate("Date"));
            	appointment.setLocation(result.getString("Location"));
            	appointment.setDepartmentName(result.getString("de.DepartmentName"));
            	appointment.setDepartmentCode(result.getString("de.DepartmentCode"));
            	appointment.setPatientID(result.getInt("p.PatientID"));
            	appointment.setPatientName(result.getString("p.FirstName") + result.getString("p.LastName"));
            	appointment.setDoctorID(result.getInt("d.DoctorID"));
            	appointment.setDoctorName(result.getString("d.FirstName") + result.getString("d.LastName"));
            	appointment.setApproved(result.getBoolean("Approved"));
            }
        }

        return appointment;
    }

	public List<Appointment> listAppointments() throws SQLException, ClassNotFoundException {
    	List<Appointment> appointments = new ArrayList<>();

    	String jdbcURL = "jdbc:mysql://localhost:3306/onlinehealthsystem";
        String dbUser = "root";
        String dbPassword = "root";

        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)) {
            String sql = "select * from Appointments a, Departments de, Patients p, Doctors d where a.PatientID = p.LoginID and a.DoctorID = d.LoginID and a.DepartmentCode = de.DepartmentCode";
            PreparedStatement statement = connection.prepareStatement(sql);

            ResultSet result = statement.executeQuery();

			while (result.next()) {
				int appointmentID = result.getInt("AppointmentId");
				Date appointmentDate = result.getDate("Date");
				String location = result.getString("Location");
				String departmentName = result.getString("de.DepartmentName");
				String departmentCode = result.getString("de.DepartmentCode");
				int patientID = result.getInt("p.PatientID");
				String patientName = result.getString("p.FirstName") + " " + result.getString("p.LastName");
				int doctorID = result.getInt("d.DoctorID");
				String doctorName = result.getString("d.FirstName")  + " " + result.getString("d.LastName");
            	Boolean approved = result.getBoolean("Approved");
				appointments.add(new Appointment(appointmentID, appointmentDate, location, departmentCode, departmentName, patientID, patientName, doctorID, doctorName, approved));
			}
        }

        return appointments;
    }

	public Appointment confirmAppointment(int appointmentID) throws SQLException, ClassNotFoundException {
        String jdbcURL = "jdbc:mysql://localhost:3306/onlinehealthsystem";
        String dbUser = "root";
        String dbPassword = "root";

        Appointment appointment = null;

        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)) {
            String sql = "update Appointments set Approved = True where AppointmentId = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, appointmentID);

            statement.executeUpdate();
        }

        return appointment;
    }
}
