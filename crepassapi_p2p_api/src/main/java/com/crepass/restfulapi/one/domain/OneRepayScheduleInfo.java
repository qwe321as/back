package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneRepayScheduleInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String loanId;
    
    private String mid;
    
    private String loanPay;
    
    private String loanDay;
    
    private String yearPlus;
    
    private String repayInfo;
    
    private String repayDay;
    
    private String execDate;

    private String loanCate;
    
    public OneRepayScheduleInfo() {

    }
    
}
