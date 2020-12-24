package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneRepaymentDataInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String loanDay;
    
    private String interestRate;
    
    private String loanAccntNo;
    
    public OneRepaymentDataInfo() {

    }
    
}
