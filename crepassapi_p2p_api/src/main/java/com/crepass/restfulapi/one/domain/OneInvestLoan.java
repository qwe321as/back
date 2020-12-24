package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneInvestLoan implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String loanPay;
    
    private String look;
    
    private String loanDay;
    
    private String yearPlus;
    
    private String purpose;
    
    private String repay;
    
    private String loanPose;
    
    private String age;
    
    private String xes;
    
    private String school;
    
    private String major;
    
    private String graduate;
    
    private String summary;
    
    public OneInvestLoan() {

    }
    
}
