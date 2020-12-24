package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneInvestOrderUnit implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String ipay;
    
    private String maturity;
    
    private String lnIyul;
    
    private String paytype;
    
    public OneInvestOrderUnit() {

    }
    
}
