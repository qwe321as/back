package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneInvestCredit implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String investLevel;
    
    private String grade;
    
    private String investMin;
    
    public OneInvestCredit() {

    }
    
}
