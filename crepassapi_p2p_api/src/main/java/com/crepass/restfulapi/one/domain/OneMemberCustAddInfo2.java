package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneMemberCustAddInfo2 implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String hp;
    
    private String myBankacc;
       
    private String myBankcode;
    
    private String virtualAccnt;
    
    private String name;
    
    private String custId;
    
    private String companyName;
    
    private String companyNum;
    
}
