package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneLoanExcuteTotal implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String loanId;
    
    private int rCount;
    
    private int lnAmount;
    
    private int rInterestAmt;
    
    public OneLoanExcuteTotal() {

    }
    
}
