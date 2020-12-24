package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneEmoneyDetailHistory implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String accntNo;
    
    private String trxType;
    
    private String trxAmt;
    
    private String createDt;
    
    public OneEmoneyDetailHistory() {

    }
    
}
