package com.onlinehealthsystem;

import java.sql.Date;

public class Treatment {

	public Treatment() {
		super();
	}
	
	public Treatment(int treatmentID, Date treatmentDate, String location, String departmentCode, String departmentName,
			String treatmentName, String medicineCode, String medicineName, int patientID, String patientName, int doctorID, String doctorName, int billID, Date billDate, int billAmount) {
		super();
		this.treatmentID = treatmentID;
		this.treatmentDate = treatmentDate;
		this.location = location;
		this.departmentCode = departmentCode;
		this.departmentName = departmentName;
		this.treatmentName = treatmentName;
		this.medicineCode = medicineCode;
		this.medicineName = medicineName;
		this.patientID = patientID;
		this.patientName = patientName;
		this.doctorID = doctorID;
		this.doctorName = doctorName;
		this.billID = billID;
		this.billDate = billDate;
		this.billAmount = billAmount;
	}

	public int getAppointmentID() {
		return treatmentID;
	}
	
	public void setAppointmentID(int appointmentID) {
		this.treatmentID = appointmentID;
	}
	
	public String getLocation() {
		return location;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	public String getDepartmentCode() {
		return departmentCode;
	}
	
	public void setDepartmentCode(String departmentCode) {
		this.departmentCode = departmentCode;
	}
	
	public String getDepartmentName() {
		return departmentName;
	}
	
	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}
	
	public int getTreatmentID() {
		return treatmentID;
	}

	public void setTreatmentID(int treatmentID) {
		this.treatmentID = treatmentID;
	}

	public String getTreatmentName() {
		return treatmentName;
	}

	public void setTreatmentName(String treatmentName) {
		this.treatmentName = treatmentName;
	}

	public String getMedicineCode() {
		return medicineCode;
	}

	public void setMedicineCode(String medicineCode) {
		this.medicineCode = medicineCode;
	}

	public String getMedicineName() {
		return medicineName;
	}

	public void setMedicineName(String medicineName) {
		this.medicineName = medicineName;
	}
	
	public int getPatientID() {
		return patientID;
	}
	
	public void setPatientID(int patientID) {
		this.patientID = patientID;
	}
	
	public int getDoctorID() {
		return doctorID;
	}
	
	public void setDoctorID(int doctorID) {
		this.doctorID = doctorID;
	}

	public int getBillID() {
		return billID;
	}

	public void setBillID(int billID) {
		this.billID = billID;
	}

	public int getBillAmount() {
		return billAmount;
	}

	public void setBillAmount(int billAmount) {
		this.billAmount = billAmount;
	}

	public Date getTreatmentDate() {
		return treatmentDate;
	}

	public void setTreatmentDate(Date treatmentDate) {
		this.treatmentDate = treatmentDate;
	}

	public Date getBillDate() {
		return billDate;
	}

	public void setBillDate(Date billDate) {
		this.billDate = billDate;
	}

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public String getDoctorName() {
		return doctorName;
	}

	public void setDoctorName(String doctorName) {
		this.doctorName = doctorName;
	}

	private int treatmentID;
	private Date treatmentDate;
	private String location;
	private String departmentCode;
	private String departmentName;
	private String treatmentName;
	private String medicineCode;
	private String medicineName;
	private int patientID;
	private String patientName;
	private int doctorID;
	private String doctorName;
	private int billID;
	private Date billDate;
	private int billAmount;
}
