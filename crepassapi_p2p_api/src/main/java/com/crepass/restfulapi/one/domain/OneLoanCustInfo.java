package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneLoanCustInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String loanId;
    
    private String mid;
    
    private String name;
    
    private String loanPay;
    
    private String hp;
    
    private String birth;
    
    private String myBankcode;
    
    private String myBankacc;
    
    private String custId;
    
    private String subject;
    
    private String loanAccntNo;
    
    private String loanCate;
    
    public OneLoanCustInfo() {

    }
    
}
