package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OnePaymentInvestSchedule implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String mid;
    
    private String loanPay;
    
    private String pay;
    
    private String regdatetime;
    
    private String level;
    
    private String service;
    
    public OnePaymentInvestSchedule() {

    }
    
}
