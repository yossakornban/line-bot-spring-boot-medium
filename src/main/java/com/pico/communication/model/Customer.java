package com.pico.communication.model;

import lombok.Data;

@Data
public class Customer {
	private String line_user_id;
    private String first_name;
    private String last_name;
	private Float credit;
	private Integer period;
	private Float interest;
	private Boolean status;
}
