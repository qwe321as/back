package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OnePaymentFeeInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String mid;
    
    private String loanId;
    
    private String pCount;
    
    private String pStatus;
    
    private String lnAmount;

    private String fee;
    
    private String payDate;
    
    public OnePaymentFeeInfo() {

    }
    
}
