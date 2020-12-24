package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneLoanVirtualAccntInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String loanAccntNo;
    
    private String mid;
    
    private String name;
    
    private String custId;
    
    private String execDate;
    
    private String subject;
    
    public OneLoanVirtualAccntInfo() {

    }
    
}
