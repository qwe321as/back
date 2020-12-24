package com.crepass.restfulapi.inside.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneRepaymentInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String trAmt;
    
    private String trAmtP;
    
    private String tranDate;
    
    private String tranTime;
    
    private String custID;
    
    public OneRepaymentInfo() {

    }
    
}
