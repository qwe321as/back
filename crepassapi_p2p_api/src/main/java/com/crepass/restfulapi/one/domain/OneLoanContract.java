package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneLoanContract implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String loanId;
    
    private String name;
    
    private String birth;
    
    private String address;
    
    private String hp;
    
    private String bankName;
    
    private String bankAccnt;
    
    private String repayDay;
    
    private String repay;
    
    private String loanPay;
    
    private String yearPlus;
    
    private String overDue;
    
    public OneLoanContract() {

    }
    
}
