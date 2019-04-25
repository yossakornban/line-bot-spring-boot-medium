package com.iphayao.linebot.model;

import java.sql.Date;
import java.sql.ResultSet;

import lombok.Data;
@Data
public class Holiday {

	private String date_holiday;
	private String name_holiday;
	private String day1_holiday_soon;
	private String day2_holiday_soon;
	private String day3_holiday_soon;
	
	
	
	public String getDay1_holiday_soon() {
		return day1_holiday_soon;
	}
	public void setDay1_holiday_soon(String day1_holiday_soon) {
		this.day1_holiday_soon = day1_holiday_soon;
	}
	public String getDay2_holiday_soon() {
		return day2_holiday_soon;
	}
	public void setDay2_holiday_soon(String day2_holiday_soon) {
		this.day2_holiday_soon = day2_holiday_soon;
	}
	public String getDay3_holiday_soon() {
		return day3_holiday_soon;
	}
	public void setDay3_holiday_soon(String day3_holiday_soon) {
		this.day3_holiday_soon = day3_holiday_soon;
	}
	public String getDate_holiday() {
		return date_holiday;
	}
	public void setDate_holiday(String date_holiday) {
		this.date_holiday = date_holiday;
	}
	public String getName_holiday() {
		return name_holiday;
	}
	public void setName_holiday(String name_holiday) {
		this.name_holiday = name_holiday;
	} 



}
 