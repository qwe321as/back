package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneInvestBalanceState implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String loanId;
    
    private String subject;
    
    private String ipay;
    
    private String maturity;
    
    private String yearPlus;
    
    private String count;
    
    private String investamount;
    
    private String inMoneyTo;
    
    private String interest;
    
    private String tax;
    
    private String fee;
    
    private String overDue;
    
    private String repayDate;
    
    private String repaymentStatus;
    
    public OneInvestBalanceState() {

    }
    
}
