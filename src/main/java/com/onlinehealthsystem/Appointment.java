package com.onlinehealthsystem;

import java.sql.Timestamp;

public class Appointment {
	
	public Appointment() {
		super();
	}
	
	public Appointment(Timestamp date, String location, String departmentCode, String departmentName, int patientID, String patientName, int doctorID, String doctorName, Boolean approved) {
		super();
		this.appointmentDate = date;
		this.location = location;
		this.departmentCode = departmentCode;
		this.departmentName = departmentName;
		this.patientID = patientID;
		this.patientName = patientName;
		this.doctorID = doctorID;
		this.doctorName = doctorName;
		this.approved = approved;
	}
	
	public Appointment(int appointmentID, Timestamp date, String location, String departmentCode, String departmentName, int patientID, String patientName, int doctorID, String doctorName, Boolean approved) {
		super();
		this.appointmentID = appointmentID;
		this.appointmentDate = date;
		this.location = location;
		this.departmentCode = departmentCode;
		this.departmentName = departmentName;
		this.patientID = patientID;
		this.patientName = patientName;
		this.doctorID = doctorID;
		this.doctorName = doctorName;
		this.approved = approved;
	}
	
	public int getAppointmentID() {
		return appointmentID;
	}
	
	public void setAppointmentID(int appointmentID) {
		this.appointmentID = appointmentID;
	}
	
	public Timestamp getAppointmentDate() {
		return appointmentDate;
	}

	public void setAppointmentDate(Timestamp date) {
		this.appointmentDate = date;
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

	public Boolean getApproved() {
		return approved;
	}

	public void setApproved(Boolean approved) {
		this.approved = approved;
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

	private int appointmentID;
	private Timestamp appointmentDate;
	private String location;
	private String departmentCode;
	private String departmentName;
	private int patientID;
	private String patientName;
	private int doctorID;
	private String doctorName;
	private Boolean approved;
}
