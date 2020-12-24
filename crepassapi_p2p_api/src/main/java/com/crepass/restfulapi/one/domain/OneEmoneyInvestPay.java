package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneEmoneyInvestPay implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String ipay;
    
    private String custId;
    
    public OneEmoneyInvestPay() {

    }
    
}
