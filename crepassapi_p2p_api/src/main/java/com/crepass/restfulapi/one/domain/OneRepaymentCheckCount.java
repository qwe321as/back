package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneRepaymentCheckCount implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String payAmount;
    
    private String count;
    
    public OneRepaymentCheckCount() {

    }
    
}
