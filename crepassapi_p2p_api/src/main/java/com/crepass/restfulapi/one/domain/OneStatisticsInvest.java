package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneStatisticsInvest implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String investingPay;
    
    private String restDepositPay;
    
    private String totDepositPay;
    
    private String investPay;
    
    private String totPayAmount;
    
    private String totInAmount;
    
    private String totInterest;
    
    private String totFee;
    
    private String totTax;
    
    private String totDepositPay2;
    
    public OneStatisticsInvest() {

    }
    
}
