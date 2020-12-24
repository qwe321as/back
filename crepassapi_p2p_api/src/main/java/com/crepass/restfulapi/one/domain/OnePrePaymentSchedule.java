package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OnePrePaymentSchedule implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String pid;
    
    private String mid;
    
    private String loanId;
    
    private String interest;
    
    private String tax;
    
    private String taxLocal;
    
    private String fee;
    
    private String payAmount;
    
    private String loanPay;
    
    private String repayCount;
    
    
    public OnePrePaymentSchedule() {

    }
    
}
