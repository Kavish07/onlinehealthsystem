package com.onlinehealthsystem;

import java.sql.Date;

public class Patient {
		
	public Patient() {
	}
		
	public Patient(String firstName, String lastName, String userName) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.userName = userName;
	}
		
	public Patient(int id, String firstName, String lastName, String userName) {
		super();
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.userName = userName;
	}

	public Patient(String firstName, String lastName, String userName, String userPassword) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.userName = userName;
		this.userPassword = userPassword;
	}
		
	public Patient(int id, String firstName, String lastName, String userName, String userPassword) {
		super();
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.userName = userName;
		this.userPassword = userPassword;
	}
	
	public Patient(int loginID, String firstName, String lastName, String userName, String nationalID, Date dateOfBirth, String sex, String address, String city, int weight, int height) {
		super();
		this.loginID = loginID;
		this.userName = userName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.nationalID = nationalID;
		this.dateOfBirth = dateOfBirth;
		this.sex = sex;
		this.address = address;
		this.city = city;
		this.weight = weight;
		this.height = height;
	}
	
	public Patient(int id, String firstName, String lastName, String userName, String userPassword, String nationalID, Date dateOfBirth, String sex, String address, String city, int weight, int height, int loginID, String salt) {
		super();
		this.id = id;
		this.userName = userName;
		this.userPassword = userPassword;
		this.firstName = firstName;
		this.lastName = lastName;
		this.nationalID = nationalID;
		this.dateOfBirth = dateOfBirth;
		this.sex = sex;
		this.address = address;
		this.city = city;
		this.weight = weight;
		this.height = height;
		this.loginID = loginID;
		this.salt = salt;
	}
	
	public int getId() {
		return id;
	}
	    
	public void setId(int id) {
		this.id = id;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
		
	public String getUserPassword() {
		return userPassword;
	}
		
	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}
		
	public String getFirstName() {
		return firstName;
	}
		
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
		
	public String getLastName() {
		return lastName;
	}
		
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getNationalID() {
		return nationalID;
	}

	public void setNationalID(String nationalID) {
		this.nationalID = nationalID;
	}

	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getLoginID() {
		return loginID;
	}

	public void setLoginID(int loginID) {
		this.loginID = loginID;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	private int id;
	private String userName;
	private String userPassword;
	private String firstName;
	private String lastName;
	private String nationalID;
	private Date dateOfBirth;
	private String sex;
	private String address;
	private String city;
	private int weight;
	private int height;
	private int loginID;
	private String salt;
}

