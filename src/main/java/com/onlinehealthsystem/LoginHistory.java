package com.onlinehealthsystem;

import java.time.LocalDateTime;

public class LoginHistory {
	
	public LoginHistory() {
	}
	
	public LoginHistory(int id, int userID, LocalDateTime loginDateTime, LocalDateTime logoutDateTime) {
		super();
		this.id = id;
		this.loginID = userID;
		this.loginDateTime = loginDateTime;
		this.logoutDateTime = logoutDateTime;
	}
	
    public int getId() {
		return id;
	}
    
	public void setId(int id) {
		this.id = id;
	}
	
    public int getLoginID() {
		return loginID;
	}

	public void setLoginID(int userID) {
		this.loginID = userID;
	}

	public LocalDateTime getLoginDateTime() {
		return loginDateTime;
	}

	public void setLoginDateTime(LocalDateTime loginDateTime) {
		this.loginDateTime = loginDateTime;
	}

	public LocalDateTime getLogoutDateTime() {
		return logoutDateTime;
	}

	public void setLogoutDateTime(LocalDateTime logoutDateTime) {
		this.logoutDateTime = logoutDateTime;
	}
	
	private int id;
	private int loginID;
    private LocalDateTime loginDateTime;
    private LocalDateTime logoutDateTime;
}
