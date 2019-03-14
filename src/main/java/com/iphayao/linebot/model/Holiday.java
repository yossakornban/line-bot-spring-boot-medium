package com.iphayao.linebot.model;



import java.security.Timestamp;
import java.sql.Date;
import java.sql.ResultSet;

import lombok.Data;

@Data
public class Holiday {
	private String year_holiday;
	private Timestamp date_holiday;
	private String name_holiday;

	
}
