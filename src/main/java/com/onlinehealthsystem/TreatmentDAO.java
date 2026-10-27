package com.onlinehealthsystem;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TreatmentDAO {

	public Treatment registerTreatment(Date treatmentDate, String location, String departmentCode, String treatmentName, String medicineName, int patientID, int doctorID, int billAmount, Date billDate) throws SQLException, ClassNotFoundException {
        String jdbcURL = "jdbc:mysql://localhost:3306/onlinehealthsystem";
        String dbUser = "root";
        String dbPassword = "root";

        Treatment treatment = new Treatment();
        int billId = 0;
        String medicineCode = "";

        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)) {
            String sql = "insert into Bill (Date, Amount) values(?,?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDate(1, billDate);
            statement.setInt(2, billAmount);

            statement.executeUpdate();

            String sql2 = "select * from Bill order by 1 desc limit 1";
            PreparedStatement statement2 = connection.prepareStatement(sql2);

            ResultSet result = statement2.executeQuery();

            while (result.next()) {
            	billId = result.getInt("BillID");
			}

            String sql3 = "select * from Medicines where MedicineName in (?)";
            PreparedStatement statement3 = connection.prepareStatement(sql3);
            statement3.setString(1, medicineName);

            ResultSet result2 = statement3.executeQuery();

            while (result2.next()) {
            	medicineCode = result2.getString("MedicineCode");
			}

            String sql4 = "insert into Treatments (Date, Location, DepartmentCode, TreatmentName, MedicineCode, PatientID, DoctorID, BillID) values(?,?,?,?,?,?,?,?)";
            PreparedStatement statement4 = connection.prepareStatement(sql4);
            statement4.setDate(1, treatmentDate);
            statement4.setString(2, location);
            statement4.setString(3, departmentCode);
            statement4.setString(4, treatmentName);
            statement4.setString(5, medicineCode);
            statement4.setInt(6, patientID);
            statement4.setInt(7, doctorID);
            statement4.setInt(8, billId);

            statement4.executeUpdate();
        }

        return treatment;
    }

	public List<Treatment> listTreatments(int loginId) throws SQLException, ClassNotFoundException {
    	List<Treatment> treatments = new ArrayList<>();

    	String jdbcURL = "jdbc:mysql://localhost:3306/onlinehealthsystem";
        String dbUser = "root";
        String dbPassword = "root";

        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)) {
            String sql = "select * from Treatments t, Departments de, Patients p, Doctors d, Bill b, Medicines m where t.PatientID = p.LoginID and t.DoctorID = d.LoginID and t.DepartmentCode = de.DepartmentCode and t.BillID = b.BillID and m.MedicineCode = t.MedicineCode and p.LoginID = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, loginId);

            ResultSet result = statement.executeQuery();

			while (result.next()) {
				int treatmentID = result.getInt("TreatmentId");
				Date treatmentDate = result.getDate("t.Date");
				String location = result.getString("Location");
				String departmentName = result.getString("de.DepartmentName");
				String departmentCode = result.getString("de.DepartmentCode");
				String treatmentName = result.getString("TreatmentName");
				String medicineCode = result.getString("m.MedicineCode");
				String medicineName = result.getString("m.MedicineName");
				int patientID = result.getInt("p.PatientID");
				String patientName = result.getString("p.FirstName") + " " + result.getString("p.LastName");
				int doctorID = result.getInt("d.DoctorID");
				String doctorName = result.getString("d.FirstName")  + " " + result.getString("d.LastName");
				int billId = result.getInt("b.BillID");
				Date billDate = result.getDate("b.Date");
				int billAmount = result.getInt("b.Amount");
            	treatments.add(new Treatment(treatmentID, treatmentDate, location, departmentCode, departmentName, treatmentName, medicineCode, medicineName, patientID, patientName, doctorID, doctorName, billId, billDate, billAmount));
			}
        }

        return treatments;
    }

	public List<Treatment> listAllTreatments() throws SQLException, ClassNotFoundException {
    	List<Treatment> treatments = new ArrayList<>();

    	String jdbcURL = "jdbc:mysql://localhost:3306/onlinehealthsystem";
        String dbUser = "root";
        String dbPassword = "root";

        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)) {
            String sql = "select * from Treatments t, Departments de, Patients p, Doctors d, Bill b, Medicines m where t.PatientID = p.LoginID and t.DoctorID = d.LoginID and t.DepartmentCode = de.DepartmentCode and t.BillID = b.BillID and m.MedicineCode = t.MedicineCode";
            PreparedStatement statement = connection.prepareStatement(sql);

            ResultSet result = statement.executeQuery();

			while (result.next()) {
				int treatmentID = result.getInt("TreatmentId");
				Date treatmentDate = result.getDate("t.Date");
				String location = result.getString("Location");
				String departmentName = result.getString("de.DepartmentName");
				String departmentCode = result.getString("de.DepartmentCode");
				String treatmentName = result.getString("TreatmentName");
				String medicineCode = result.getString("m.MedicineCode");
				String medicineName = result.getString("m.MedicineName");
				int patientID = result.getInt("p.PatientID");
				String patientName = result.getString("p.FirstName") + " " + result.getString("p.LastName");
				int doctorID = result.getInt("d.DoctorID");
				String doctorName = result.getString("d.FirstName")  + " " + result.getString("d.LastName");
				int billId = result.getInt("b.BillID");
				Date billDate = result.getDate("b.Date");
				int billAmount = result.getInt("b.Amount");
            	treatments.add(new Treatment(treatmentID, treatmentDate, location, departmentCode, departmentName, treatmentName, medicineCode, medicineName, patientID, patientName, doctorID, doctorName, billId, billDate, billAmount));
			}
        }

        return treatments;
    }

	public Treatment updateTreatment(int treatmentID, Date treatmentDate, String location, String departmentCode, String treatmentName, String medicineName, int patientID, int doctorID, int billId, int billAmount, Date billDate) throws SQLException, ClassNotFoundException {
        String jdbcURL = "jdbc:mysql://localhost:3306/onlinehealthsystem";
        String dbUser = "root";
        String dbPassword = "root";

        Treatment treatment = new Treatment();
        String medicineCode = "";

        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)) {
            String sql2 = "select * from Medicines where MedicineName in (?)";
            PreparedStatement statement2 = connection.prepareStatement(sql2);
            statement2.setString(1, medicineName);

            ResultSet result = statement2.executeQuery();

            while (result.next()) {
            	medicineCode = result.getString("MedicineCode");
			}

            String sql3 = "update Treatments set Date = ?, Location = ?, DepartmentCode = ?, TreatmentName = ?, MedicineCode = ? where TreatmentId = ?";
            PreparedStatement statement3 = connection.prepareStatement(sql3);
            statement3.setDate(1, treatmentDate);
            statement3.setString(2, location);
            statement3.setString(3, departmentCode);
            statement3.setString(4, treatmentName);
            statement3.setString(5, medicineCode);
            statement3.setInt(6, treatmentID);

            statement3.executeUpdate();

            String sql = "update Bill set Date = ?, Amount = ? where BillID = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDate(1, billDate);
            statement.setInt(2, billAmount);
            statement.setInt(3, billId);

            statement.executeUpdate();
        }

        return treatment;
    }
}
