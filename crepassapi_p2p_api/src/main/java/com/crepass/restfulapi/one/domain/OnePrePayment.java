package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OnePrePayment implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String loanId;

    private String interest;
    
    private String overdue;
    
    private String prepay;
    
    private String balance;
    
    public OnePrePayment() {

    }
    
}
