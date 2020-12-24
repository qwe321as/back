package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneRepayment implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String oId;
    
    private String loanId;
    
    private String custId;
    
    private String mid;
    
    private String lnMoneyTo;
    
    private String investAmount;
    
    private String amount;
    
    private String saleToTotalAmount;
    
    private String interest;
    
    private String prinRcvNo;
    
    public OneRepayment() {

    }
    
}
