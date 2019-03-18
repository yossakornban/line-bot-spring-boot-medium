package com.iphayao.linebot.model;

import java.sql.Date;
import java.sql.ResultSet;

import lombok.Data;

public class Holiday_date_NotRemove {

	private Date date_holiday;
	private String name_holiday;
	public Date getDate_holiday() {
		return date_holiday;
	}
	public void setDate_holiday(Date date_holiday) {
		this.date_holiday = date_holiday;
	}
	public String getName_holiday() {
		return name_holiday;
	}
	public void setName_holiday(String name_holiday) {
		this.name_holiday = name_holiday;
	}

	
}