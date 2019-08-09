package com.pico.communication.model;

import lombok.Data;

@Data
public class Approve {
    private Integer customer_user_id;
    private Integer prefix_id;
    private String customer_user_line_id;
    private String customer_first_name;
    private String customer_last_name;
    private String customer_tel;
    private String customer_email;
    private Integer credit_type_id;
    private String salary;
    // private String career;
    // private String career;
    // private String career;
    // private String career;
}
