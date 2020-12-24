package com.crepass.restfulapi.inside.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneRepaymentException implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String sDate;
    
    private String sTime;
    
    private String regSeq;
    
    private String execStatus;
    
    private String totalTrAmt;
    
    private String totalCtaxAmt;
    
    private String totalFee;
    
    public OneRepaymentException() {

    }
    
}
