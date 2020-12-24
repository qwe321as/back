package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneInvestAccntReturn implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String custId;
    
    private String accntNo;
    
    public OneInvestAccntReturn() {

    }
    
}
