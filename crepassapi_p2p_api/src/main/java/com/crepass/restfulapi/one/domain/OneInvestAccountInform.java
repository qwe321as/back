package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneInvestAccountInform implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String sName;
    
    private String accntNo;
    
    private String sid;
    
    private String mName;
    
    private String mid;
    
    private String custId;
    
    public OneInvestAccountInform() {

    }
    
}
