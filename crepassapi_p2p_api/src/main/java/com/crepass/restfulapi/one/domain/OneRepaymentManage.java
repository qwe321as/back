package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneRepaymentManage implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String loanId;
    
    private String mid;
       
    private String subject;
    
    private String name;
    
    private String loanPay;
    
}
