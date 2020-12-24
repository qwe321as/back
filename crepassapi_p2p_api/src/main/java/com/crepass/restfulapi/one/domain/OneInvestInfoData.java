package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneInvestInfoData implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String i_look;
    
    private String loan_id;
    
    private String title;
    
    private String i_loan_pay;
    
    private String interest_rate;
    
    private String i_loan_day;
    
    private String i_pay;
    
    private String i_id;
    
    private String ratio;
    
    private String status;
    
    public OneInvestInfoData() {

    }
    
}
