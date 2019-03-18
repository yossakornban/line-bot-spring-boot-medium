package com.iphayao.linebot.model;

import java.sql.Date;
import java.sql.ResultSet;

import lombok.Data;

public class Holiday_date_NotRemove {

	private Date date_holiday_date;
	private String name_holiday_stirng;

	public Date getDate_holiday_date() {
		return date_holiday_date;
	}

	public void setDate_holiday_date(Date date_holiday_date) {
		this.date_holiday_date = date_holiday_date;
	}

	public String getName_holiday_stirng() {
		return name_holiday_stirng;
	}

	public void setName_holiday_stirng(String name_holiday_stirng) {
		this.name_holiday_stirng = name_holiday_stirng;
	}

}