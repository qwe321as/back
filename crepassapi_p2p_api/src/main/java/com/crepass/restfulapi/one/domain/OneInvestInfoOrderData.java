package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneInvestInfoOrderData implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String loan_id;
    
    private String title;
    
    private String i_pay;
    
    private String interest_rate;
    
    private String maturity;
    
    private String collectiondate;
    
    private String count;
    
    private String principal;
    
    private String interest;
    
    private String overdueinterest;
    
    private String sum;
    
    private String status;
    
    private String fee;
    
    private String withholding;
    
    private String o_id;
    
    public OneInvestInfoOrderData() {

    }
    
}
