package com.crepass.restfulapi.inside.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneInsideDepositCancel implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String custId;
    
    private String acctNb;
    
    private String trOrgDate;
    
    private String trOrgSeq;
    
    private String trAmt;
    
    private String trNb;
    
    private String trAmtGbn;
    
    private String typeFlag;
}
