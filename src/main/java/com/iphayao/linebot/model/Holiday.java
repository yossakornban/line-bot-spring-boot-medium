package com.iphayao.linebot.model;

import java.util.Date;

import lombok.Data;

@Data
public class Holiday {
	private String year_holiday;
	private Date date_holiday;
	private String name_holiday;
}
