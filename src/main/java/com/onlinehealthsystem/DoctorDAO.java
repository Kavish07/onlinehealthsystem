package com.onlinehealthsystem;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

	public Doctor checkLogin(String userName, String userPassword, LocalDateTime loginDateTime) throws SQLException, ClassNotFoundException {
        String jdbcURL = "jdbc:mysql://localhost:3306/onlinehealthsystem";
        String dbUser = "root";
        String dbPassword = "root";
 
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
        
        String sql = "select * from Login where UserName = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, userName);
 
        ResultSet result = statement.executeQuery();
 
        Doctor doctor = null;
 
        if (result.next()) {
        	doctor = new Doctor();
        	doctor.setLoginID(result.getInt("LoginID"));
        	doctor.setUserName(result.getString("UserName"));
        	doctor.setUserPassword(result.getString("UserPassword"));
        	doctor.setSalt(result.getString("Salt"));
        }
        
        Boolean status = PasswordEncoder.verifyUserPassword(userPassword, doctor.getUserPassword(), doctor.getSalt());
        if (status != true)  
        {  
        	doctor = null;  
        }
        else
        {
        	String sql2 = "insert into LoginHistory (LoginID, LoginDateTime) values(?,?)";
        	PreparedStatement statement2 = connection.prepareStatement(sql2);
        	statement2.setInt(1, doctor.getLoginID());
        	statement2.setObject(2, loginDateTime);
        	
            statement2.executeUpdate();
        }
 
        connection.close();
 
        return doctor;
    }
    
    public Doctor checkRegistration(String userName) throws SQLException, ClassNotFoundException {
        String jdbcURL = "jdbc:mysql://localhost:3306/onlinehealthsystem";
        String dbUser = "root";
        String dbPassword = "root";
 
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
        String sql = "select * from Login where UserName = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, userName);
 
        ResultSet result = statement.executeQuery();
 
        Doctor doctor = null;
 
        if (result.next()) {
        	doctor = new Doctor();
        	doctor.setUserName(result.getString("UserName"));
        	doctor.setUserPassword(result.getString("UserPassword"));
        	doctor.setSalt(result.getString("Salt"));
        }
 
        connection.close();
 
        return doctor;
    }
    
    public Doctor registerDoctor(String userName, String userPassword, String firstName, String lastName, String nationalID, Date dateOfBirth, String sex, String address, String city, int weight, int height, String qualifications, String speciality, String departmentCode) throws SQLException, ClassNotFoundException {
        String jdbcURL = "jdbc:mysql://localhost:3306/onlinehealthsystem";
        String dbUser = "root";
        String dbPassword = "root";
        
        Doctor doctor = null;
        
        /* generates the Salt value. It can be stored in a database. */  
        String saltvalue = PasswordEncoder.getSaltvalue(30);  
          
        /* generates an encrypted password. It can be stored in a database.*/  
        String encryptedpassword = PasswordEncoder.generateSecurePassword(userPassword, saltvalue);  
 
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
        
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
        	doctor = new Doctor();
        	doctor.setLoginID(result.getInt("LoginID"));
        	doctor.setUserName(result.getString("UserName"));
        	doctor.setUserPassword(result.getString("UserPassword"));
        }
        
        String sql3 = "insert into Doctors (FirstName, LastName, NationalID, DateOfBirth, Sex, Address, City, Weight, Height, Qualifications, Speciality, DepartmentCode, LoginID) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
        statement3.setString(10, qualifications);
        statement3.setString(11, speciality);
        statement3.setString(12, departmentCode);
        statement3.setInt(13, doctor.getLoginID());
 
        statement3.executeUpdate();
 
        connection.close();
        
        return doctor;
    }
    
    public Doctor deleteDoctor(int loginID) throws SQLException, ClassNotFoundException {
        String jdbcURL = "jdbc:mysql://localhost:3306/onlinehealthsystem";
        String dbUser = "root";
        String dbPassword = "root";
        
        Doctor doctor = null;
 
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
        
        String sql = "delete from Appointments where DoctorID = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, loginID);
        statement.executeUpdate();
        
        String sql2 = "delete from Treatments where DoctorID = ?";
        PreparedStatement statement2 = connection.prepareStatement(sql2);
        statement2.setInt(1, loginID);
        statement2.executeUpdate();
        
        String sql3 = "delete from Doctors where LoginID = ?";
        PreparedStatement statement3 = connection.prepareStatement(sql3);
        statement3.setInt(1, loginID);
        statement3.executeUpdate();
        
        String sql4 = "delete from LoginHistory where LoginID = ?";
        PreparedStatement statement4 = connection.prepareStatement(sql4);
        statement4.setInt(1, loginID);
        statement4.executeUpdate();
        
        String sql5 = "delete from Login where LoginID = ?";
        PreparedStatement statement5 = connection.prepareStatement(sql5);
        statement5.setInt(1, loginID);
        statement5.executeUpdate();
 
        connection.close();
        
        return doctor;
    }
    
    public Doctor updatePassword(String userName, String userPassword) throws SQLException, ClassNotFoundException {
    	String jdbcURL = "jdbc:mysql://localhost:3306/onlinehealthsystem";
        String dbUser = "root";
        String dbPassword = "root";
        
        /* generates the Salt value. It can be stored in a database. */  
        String saltvalue = PasswordEncoder.getSaltvalue(30);  
          
        /* generates an encrypted password. It can be stored in a database.*/  
        String encryptedpassword = PasswordEncoder.generateSecurePassword(userPassword, saltvalue);
 
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
        
        String sql = "update Login set UserPassword = ?, Salt = ? where UserName = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, encryptedpassword);
        statement.setString(2, saltvalue);
        statement.setString(3, userName);
 
        statement.executeUpdate();
        
        String sql2 = "select * from Login where UserName = ? and UserPassword = ?";
        PreparedStatement statement2 = connection.prepareStatement(sql2);
        statement2.setString(1, userName);
        statement2.setString(2, encryptedpassword);
 
        ResultSet result2 = statement2.executeQuery();
 
        Doctor doctor = null;
 
        if (result2.next()) {
        	doctor = new Doctor();
        	doctor.setUserName(result2.getString("userName"));
        	doctor.setUserPassword(result2.getString("userPassword"));
        }
        
        connection.close();
        
        return doctor;
    }
    
    public List <Doctor> listDoctors() throws SQLException, ClassNotFoundException {
    	List <Doctor> doctors = new ArrayList<>();
    	
    	String jdbcURL = "jdbc:mysql://localhost:3306/onlinehealthsystem";
        String dbUser = "root";
        String dbPassword = "root";
 
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
        
        String sql = "select * from Login l, Doctors d, Departments de where l.LoginID = d.LoginID and d.DepartmentCode = de.DepartmentCode and l.UserName not in ('admin@gmail.com')";
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
        	String qualifications = result.getString("Qualifications");
        	String speciality = result.getString("Speciality");
        	String departmentName = result.getString("de.DepartmentName");
			
			doctors.add(new Doctor(loginID, firstName, lastName, username, nationalID, dateofbirth, sex, address, city, weight, height, qualifications, speciality, departmentName));
		}
 
        connection.close();
 
        return doctors;
    }
    
    public Doctor registerLogout(int loginID, LocalDateTime logoutDateTime) throws SQLException, ClassNotFoundException {
        String jdbcURL = "jdbc:mysql://localhost:3306/onlinehealthsystem";
        String dbUser = "root";
        String dbPassword = "root";
        
        Doctor doctor = null;
        
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
        
        String sql = "update LoginHistory set LogoutDateTime = ? where LoginID = ? and LogoutDateTime is null";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setObject(1, logoutDateTime);
        statement.setInt(2, loginID);
        
        statement.executeUpdate();
 
        connection.close();
        
        return doctor;
    }
}
