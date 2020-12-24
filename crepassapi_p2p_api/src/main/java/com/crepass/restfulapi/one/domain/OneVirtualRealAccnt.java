package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneVirtualRealAccnt implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String myBankacc;
    
    private String myBankcodeName;
    
    private String myBankcode;
    
    private String myBankName;
    
    private String emoney;
    
    private String mname;
    
    private String custId;
    
    public OneVirtualRealAccnt() {

    }
    
}
