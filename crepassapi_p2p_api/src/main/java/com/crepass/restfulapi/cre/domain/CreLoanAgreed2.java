package com.crepass.restfulapi.cre.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class CreLoanAgreed2 implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String mid;
    
    private String loanId;
    
    private String customerNotice;
    
    private String loanContract;
    
    private String basicContract;
    
    private String mainContract;
    
    private String contractCondition;
    
    private String delayInterest;
    
    private String stampFee;
    
    private String intermFee;
    
    private String platformFee;

    private String brokerFee;
    
    private String termsExp;
    
    private String cinfProvide;

    public CreLoanAgreed2() {

    }
    
}
