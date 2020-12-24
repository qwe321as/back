package com.crepass.restfulapi.inside.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class InsideDepositInfo2 implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    
    private String custId;
    
    private String accntNb;
    
    private String trAmt;
    
    private String erpTransDt;
    
    private String typeFlag;
    
}
