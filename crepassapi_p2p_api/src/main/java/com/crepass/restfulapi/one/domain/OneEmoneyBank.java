package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneEmoneyBank implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String myBankCodeVb;
    
    private String myBankNameVb;
    
    private String myBankaccVb;
    
    private String myName;
    
    private String myBankCode;
    
    private String myBankName;
    
    private String myBankacc;
    
    private double topEmoney;
    
    private String loanAccntNo;
    
    private String withdrawRealPay;
    
    public OneEmoneyBank() {

    }
    
}
