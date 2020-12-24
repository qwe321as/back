package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OnePaymentNewSchedule implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String oid;
    
    private String pid;
    
    private String mid;
    
    private String loanId;
    
    private String repayCount;
    
    private String payDate;
    
    private String payGubun;
    
    private String payStatus;
    
    private String lnAmount;
    
    private String interestAmount;
    
    private String delqAmount;
    
    private String tax;
    
    private String taxLocal;
    
    private String fee;
    
    private String payAmount;
    
    private String loanPay;
    
    private String loanDay;
    
    private double loanRate;
    
    private String repayDay;
    
    private String execDate;
    
    public OnePaymentNewSchedule() {

    }
    
}
