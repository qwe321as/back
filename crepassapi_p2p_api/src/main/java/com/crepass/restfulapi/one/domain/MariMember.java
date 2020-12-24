package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class MariMember implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String mno;
    
    private String signPurpose;
       
    private String name;
    
    private String mid;
    
    private String xes;
    
    private String hpNumber;
    
    private String birth;
    
    private String smsInvest;
    
    private String charType;

    private String withholdingZip;
    
    private String bankCode;
    
    private String newsagency;
    
    private String myBankacc;

    private String custId;
    
    private String level;
}
