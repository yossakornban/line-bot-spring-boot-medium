package com.iphayao.linebot.model;

import lombok.Data;


@Data
public class ModelUpdate {
    private Integer paymentId;
    private Boolean approve;
    private String userLineId;
}
