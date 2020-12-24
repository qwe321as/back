package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneInvestPaymentHistory implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String mid;
    
    private String content;
    
    private String emoney;
    
    private String topEmoney;
    
    private String ip;
    
    private String loanId;
    
    public OneInvestPaymentHistory() {

    }
    
}
