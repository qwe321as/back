package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneInvestDebt implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String firstBank;
    
    private String secondBank;
    
    private String cash;
    
    private String lendding;
    
    private String ptop;
    
    private String guarantee;
    
    public OneInvestDebt() {

    }
    
}
