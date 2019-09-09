package com.pico.communication.model;

import java.security.Timestamp;

import lombok.Data;

@Data
public class UserLogPayment {
	
	public enum statusPayment {DEFAULT,PAYMENT};
	
	public statusPayment getStatusBot() {
		return statusBot;
	}
	
	public void setStatusBot(statusPayment statusBot) {
		this.statusBot = statusBot;
	}
	
	public UserLogPayment(String userID, statusPayment statusBot) {
		this.userID = userID;
		this.statusBot = statusBot;
	}
	
	public UserLogPayment() {
	}

	private String userID;
	private Integer leaveID;
	private statusPayment statusBot;
	private String leaveType;
	private String detail;
	private Timestamp startDate;
	private Timestamp end_Date;
	private String empCode;
	private String period;
}
