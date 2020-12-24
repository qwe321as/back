package com.crepass.restfulapi.cre.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class CreLoanAgreed implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String mid;
    
    private String uinfProcess;
    
    private String cinfProvide;
    
    private String cinfRetrieve;
    
    private String cinfGather;
    
    private String minfReceive;
    
    private String linfSms;
    
    private String linfEmail;
    
    private String linfHp;
    
    private String pinfGather;
    
    private String uinfGather;

    public CreLoanAgreed() {

    }
    
}
