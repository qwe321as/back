package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneInvestAccount implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String myBankacc;
    
    private double mEmoney;
    
    private String myBankcode;
    
    private String myBankName;
    
    public OneInvestAccount() {

    }
    
}
