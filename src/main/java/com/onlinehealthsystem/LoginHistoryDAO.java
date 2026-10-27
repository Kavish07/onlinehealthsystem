package com.onlinehealthsystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class LoginHistoryDAO {

	public LoginHistory registerLogin(int loginID, LocalDateTime loginDateTime) throws SQLException, ClassNotFoundException {
        String jdbcURL = "jdbc:mysql://localhost:3306/onlinehealthsystem";
        String dbUser = "root";
        String dbPassword = "root";
        
        LoginHistory loginHistory = null;
        
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
        
        String sql = "insert into LoginHistory (LoginID, LoginDateTime) values(?,?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, loginID);
        statement.setObject(2, loginDateTime);
        
        statement.executeUpdate();
 
        connection.close();
        
        return loginHistory;
    }
	
	public LoginHistory registerLogout(int loginID, LocalDateTime logoutDateTime) throws SQLException, ClassNotFoundException {
        String jdbcURL = "jdbc:mysql://localhost:3306/onlinehealthsystem";
        String dbUser = "root";
        String dbPassword = "root";
        
        LoginHistory loginHistory = null;
        
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
        
        String sql = "update LoginHistory set LogoutDateTime = ? where Login = ? and LogoutDateTime is null";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setObject(1, logoutDateTime);
        
        statement.executeUpdate();
 
        connection.close();
        
        return loginHistory;
    }
}
