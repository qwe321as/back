package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneRepaymentUserInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String name;
    
    private String count;
       
    private String payAmount;
    
    private String loanAccntNo;
    
    private String repayDate;
    
    private String payDate;
    
    private String hp;
    
    private String custId;
    
    private String loanId;
}
