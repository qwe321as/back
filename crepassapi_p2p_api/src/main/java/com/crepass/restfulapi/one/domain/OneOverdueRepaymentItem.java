package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneOverdueRepaymentItem implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String count;
    
    private String payAmount;
    
    private String payDate;
    
    private String prevPayDate;
    
    public OneOverdueRepaymentItem() {

    }
    
}
