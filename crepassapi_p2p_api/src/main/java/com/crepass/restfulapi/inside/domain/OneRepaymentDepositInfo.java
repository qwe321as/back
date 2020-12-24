package com.crepass.restfulapi.inside.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneRepaymentDepositInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String trAmt;
    
    private String erpTransDt;
    
    public OneRepaymentDepositInfo() {

    }
    
}
