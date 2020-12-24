package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneStartInvestUserInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String mid;
    
    private String custId;
    
    private String name;
    
    private String investPay;
    
    private String subject;
    
    private String hp;
    
    public OneStartInvestUserInfo() {

    }
    
}
