package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OnePayScheduleWithdraw implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String mid;
    
    private double accntNo;
    
    public OnePayScheduleWithdraw() {

    }
    
}
