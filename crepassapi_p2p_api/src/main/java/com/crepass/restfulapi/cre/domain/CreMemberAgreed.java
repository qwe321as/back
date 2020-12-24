package com.crepass.restfulapi.cre.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class CreMemberAgreed implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String mid;

    private String selfConfirm;
    
    private String pinfGather;
    
    private String uinfProcess;
    
    private String selfService;
    
    private String telcoGb;
    
    private String sms;
    
    private String agreeTerms;

    public CreMemberAgreed() {

    }
    
}
