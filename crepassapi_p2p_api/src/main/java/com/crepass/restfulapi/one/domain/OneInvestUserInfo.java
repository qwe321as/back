package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneInvestUserInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String iPay;
    
    private String subject;
    
    private String custId;
    
    public OneInvestUserInfo() {

    }
    
}
