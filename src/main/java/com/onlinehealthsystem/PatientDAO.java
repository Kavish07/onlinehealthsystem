package com.onlinehealthsystem;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Narrowed down as part of the Patient Spring Boot migration slice: login,
 * registration-check, delete, password-update and logout have all been
 * replaced by {@code com.system.OnlineHealthSystem.service.PatientService}
 * backed by Spring Data JPA, and the six Patient servlets that used to call
 * them are deleted. Only two methods remain, each kept alive for a
 * different out-of-scope (Doctor/Appointment/Treatment) dependent that
 * this slice isn't allowed to touch:
 *
 * <ul>
 *   <li>{@link #listPatients()} - called directly by
 *       {@code DoctorDeleteServlet} and {@code DoctorLoginServlet} to
 *       populate their own "listPatients" session attribute.</li>
 *   <li>{@link #registerPatient} - used purely as a test-fixture helper by
 *       {@code AppointmentDAOTest}, {@code TreatmentDAOTest} and
 *       {@code ConfirmAppointmentServletTest} to seed a patient row before
 *       exercising Appointment/Treatment behavior; no production code path
 *       reaches it anymore (no servlet is mapped to it).</li>
 * </ul>
 *
 * Both go away once the Doctor and Appointment/Treatment slices migrate
 * their own callers.
 */
public class PatientDAO {

    public Patient registerPatient(String userName, String userPassword, String firstName, String lastName, String nationalID, Date dateOfBirth, String sex, String address, String city, int weight, int height) throws SQLException, ClassNotFoundException {
        String jdbcURL = "jdbc:mysql://localhost:3306/onlinehealthsystem";
        String dbUser = "root";
        String dbPassword = "root";

        Patient patient = null;

        /* generates the Salt value. It can be stored in a database. */
        String saltvalue = PasswordEncoder.getSaltvalue(30);

        /* generates an encrypted password. It can be stored in a database.*/
        String encryptedpassword = PasswordEncoder.generateSecurePassword(userPassword, saltvalue);

        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)) {
            String sql = "insert into Login (UserName, UserPassword, Salt) values(?,?,?)";
            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, userName);
            statement.setString(2, encryptedpassword);
            statement.setString(3, saltvalue);

            statement.executeUpdate();

            String sql2 = "select * from Login order by 1 desc limit 1";
            PreparedStatement statement2 = connection.prepareStatement(sql2);

            ResultSet result = statement2.executeQuery();

            if (result.next()) {
            	patient = new Patient();
            	patient.setLoginID(result.getInt("LoginID"));
            	patient.setUserName(result.getString("UserName"));
            	patient.setUserPassword(result.getString("UserPassword"));
            }

            String sql3 = "insert into Patients (FirstName, LastName, NationalID, DateOfBirth, Sex, Address, City, Weight, Height, LoginID) values(?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement statement3 = connection.prepareStatement(sql3);
            statement3.setString(1, firstName);
            statement3.setString(2, lastName);
            statement3.setString(3, nationalID);
            statement3.setDate(4, dateOfBirth);
            statement3.setString(5, sex);
            statement3.setString(6, address);
            statement3.setString(7, city);
            statement3.setInt(8, weight);
            statement3.setInt(9, height);
            statement3.setInt(10, patient.getLoginID());

            statement3.executeUpdate();
        }

        return patient;
    }

    public List<Patient> listPatients() throws SQLException, ClassNotFoundException {
    	List<Patient> patients = new ArrayList<>();

    	String jdbcURL = "jdbc:mysql://localhost:3306/onlinehealthsystem";
        String dbUser = "root";
        String dbPassword = "root";

        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)) {
            String sql = "select * from Login l, Patients p where l.LoginID = p.LoginID";
            PreparedStatement statement = connection.prepareStatement(sql);

            ResultSet result = statement.executeQuery();

            while (result.next()) {
				int loginID = result.getInt("l.LoginID");
				String username = result.getString("UserName");
				String firstName = result.getString("FirstName");
				String lastName = result.getString("LastName");
				String nationalID = result.getString("NationalID");
				Date dateofbirth = result.getDate("DateOfBirth");
            	String sex = result.getString("Sex");
            	String address = result.getString("Address");
            	String city = result.getString("City");
            	int weight = result.getInt("Weight");
            	int height = result.getInt("Height");

				patients.add(new Patient(loginID, firstName, lastName, username, nationalID, dateofbirth, sex, address, city, weight, height));
			}
        }

        return patients;
    }
}
