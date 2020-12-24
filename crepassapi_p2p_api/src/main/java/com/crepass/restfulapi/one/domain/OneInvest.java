package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneInvest implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String gCode;
    
    private String mid;
    
    private String mName;
    
    private String subject;
    
    private String loanId;
    
    private String iPay;
    
    public OneInvest() {

    }
    
}
