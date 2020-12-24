package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneRepayScheduleAdd implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String loanId;
    
    private String repayCount;
    
    private String repayDate;
    
    private String payAmount;
    
    private String lnAmount;
    
    private String interestAmount;
    
    private String balance;
    
    public OneRepayScheduleAdd() {

    }
    
}
