package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneInvestLimitPay implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String signpurposeL;
    
    private String signpurposeI;
    
    private String signpurposeP;
    
    private String signpurpose3;
    
    private String sumIpay;
    
    public OneInvestLimitPay() {

    }
    
}
