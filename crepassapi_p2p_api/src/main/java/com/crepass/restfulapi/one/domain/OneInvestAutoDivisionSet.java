package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneInvestAutoDivisionSet implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String mid;
    
    private String isActivate;
    
    private String limitLoan;
    
    private String limitMonth;
    
    private String univName;
    
    private String agreedYN;
    
    public OneInvestAutoDivisionSet() {

    }
    
}
