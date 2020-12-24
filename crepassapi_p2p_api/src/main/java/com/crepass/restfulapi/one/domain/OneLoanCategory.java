package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneLoanCategory implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String loanId;
    
    private String goal;
    
    private String socialCorp;
    
    private String corpStartDt;
    
    private String corpEndDt;
    
    public OneLoanCategory() {

    }
    
}
