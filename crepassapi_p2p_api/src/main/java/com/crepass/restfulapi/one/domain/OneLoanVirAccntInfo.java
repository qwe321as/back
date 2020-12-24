package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneLoanVirAccntInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String accntNo;
    
    private String loanAccntNo;
    
    public OneLoanVirAccntInfo() {

    }
    
}
