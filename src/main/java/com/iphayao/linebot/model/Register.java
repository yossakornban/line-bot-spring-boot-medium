package com.iphayao.linebot.model;

import lombok.Data;

@Data
public class Register {
    private Integer prefix_id;
    private String customer_user_line_id;
    private String customer_first_name;
    private String customer_last_name;
    private String customer_tel;
    private String customer_email;
    private Integer credit_type_id;
    private String salary;
}
