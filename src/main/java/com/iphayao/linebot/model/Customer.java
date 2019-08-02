package com.iphayao.linebot.model;

import lombok.Data;

@Data
public class Customer {
	private String customer_user_line_id;
	private String customer_name;
	private Integer customer_user_id;
	private Integer customer_code;
	private Boolean approve_status;
	private Float account_credit;
	private Integer account_period;
	private Float account_interest;
}
