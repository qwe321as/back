package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneUnpaidRepayment implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String loanId; 
    
    private double payAmt; 
    
    private double delqAmt; 
    
    private double balance; 
    
    private String pCount; 
    
    private String payStatus; 
    
    private String payDate;
    
    
    private String delqState;
    
    public OneUnpaidRepayment() {

    }
    
}
