package com.iphayao.linebot.model;

import java.security.Timestamp;

import lombok.Data;

@Data
public class UserLog {
	
	public enum status {CALL, CLOSE, SAVE, Q11};
	
	public status getStatusBot() {
		return statusBot;
	}
	
	public void setStatusBot(status statusBot) {
		this.statusBot = statusBot;
	}
	
	private String userID;
	private Integer leaveID;
	private status statusBot;
	private String leaveType;
	private String detail;
	private Timestamp startDate;
	private Timestamp end_Date;
}
