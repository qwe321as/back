package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneRepaymentScheduleItem implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String loanId;
    
    private String count;
    
    private String payDate;
    
    private String payAmount;
    
    private String lnAmount;
    
    private String interestAmount;
    
    private String nextDate;
    
    private String rDelqAmount;
    
    private String delqState;
    
    public OneRepaymentScheduleItem() {

    }
    
}
