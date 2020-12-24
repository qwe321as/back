package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneStartPaymentInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    
    private long sumPayln;
    
    private String loanId;
    
    private String repayCount;

    public OneStartPaymentInfo() {

    }
    
}
