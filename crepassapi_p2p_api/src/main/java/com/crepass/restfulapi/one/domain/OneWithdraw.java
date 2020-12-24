package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneWithdraw implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String loanId;
    
    private String tid;
    
    public OneWithdraw() {

    }
    
}
