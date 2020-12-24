package com.crepass.restfulapi.cre.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class CreInvestAgreed implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String mid;
    
    private String inriskGuide;
    
    private String pinfGather;
    
    private String investUse;
    
    private String inriskNotice;

    public CreInvestAgreed() {

    }
    
}
