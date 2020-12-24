package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneLoanDataInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String loan_id;
    
    private String i_loan_pay;
    
    private String invest_pay;
    
    private String invest_cn;
    
    private String regdate;
    
    private String title;
    
    private String ratio;
    
    private String status;
    
    public OneLoanDataInfo() {

    }
    
}
