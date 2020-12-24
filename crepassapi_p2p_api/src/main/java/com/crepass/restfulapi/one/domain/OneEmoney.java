package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneEmoney implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String paydate;
    
    private String pay;
    
    private String trData;
    
    private String trAmt;
    
    public OneEmoney() {

    }
    
}
