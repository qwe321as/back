package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneWish implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String loanId;
    
    private String investName;
    
    private String investPay;
    
    private String yearPlus;
    
    private String investCredit;
    
    private String investPer;
    
    private String medalFlag;
    
    private String companyLogo;
    
    public OneWish() {

    }
    
}
