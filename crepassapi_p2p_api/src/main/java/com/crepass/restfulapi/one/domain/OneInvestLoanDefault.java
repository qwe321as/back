package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneInvestLoanDefault implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String mid;
    
    private String mname;
    
    private String loanPay;
    
    private String payMent;
    
    private String loanDay;
    
    private String yearPlus;
    
    public OneInvestLoanDefault() {

    }
    
}
