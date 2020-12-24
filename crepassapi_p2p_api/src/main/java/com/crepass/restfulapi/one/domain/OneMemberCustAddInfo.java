package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneMemberCustAddInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String hp;
    
    private String myBankacc;
       
    private String myBankcode;
    
    private String virtualAccnt;
    
    private String name;
    
    private String birth;
    
    private String custId;
    
}
