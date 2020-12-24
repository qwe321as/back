package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneInvestDetail implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String loanId;
    
    private String mid;
    
    private String mname;
    
    private String userName;
    
    private String userId;
    
    private String pay;
    
    private String subject;
    
    private String goods;
    
    private String loanPay;
    
    private String maxPay;
    
    private String day;
    
    private String profitRate;
    
    private String ip;
    
    public OneInvestDetail() {

    }
    
}
