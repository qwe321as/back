package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneWithholdAccount implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String myBankacc;
    
    private String myBankcodeName;
    
    private String myBankcode;
    
    private String myBankName;
    
    public OneWithholdAccount() {

    }
    
}
