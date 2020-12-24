package com.crepass.restfulapi.inside.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class InsideDeposit implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String custId;
    
    private String accntNb;
    
    private String erpTransDt;
    
}
