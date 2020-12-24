package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneRepaySchedule implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String payCount;
    
    private String paidAmount;
    
    private String loanInterest;
    
    private String repayAmount;
    
    private String balance;
    
    public OneRepaySchedule() {

    }
    
}
