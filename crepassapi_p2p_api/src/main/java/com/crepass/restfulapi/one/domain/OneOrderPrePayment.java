package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneOrderPrePayment implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String loanId;
    
    private String mid;
    
    private String mName;
    
    private String saleId;
    
    private String saleName;
    
    private String subject;
    
    private String count;
    
    private String lnAmount;
    
    private String payAmount;
    
    private String interestrate;
    
    private String maturity;
    
    private String ipay;
    
    private String interest;
    
    private String repay;
    
    public OneOrderPrePayment() {

    }
    
}
