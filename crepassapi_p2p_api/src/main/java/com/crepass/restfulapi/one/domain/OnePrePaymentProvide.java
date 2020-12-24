package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OnePrePaymentProvide implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String mid;
    
    private String loanId;

    private String interest;

    private String interestNormal;
    
    private String interestOverDue;
    
    private String interestGihan;
    
    private String fee;
    
    private String tax;
    
    private String taxLocal;
    
    private String payAmount;
    
    public OnePrePaymentProvide() {

    }
    
}
