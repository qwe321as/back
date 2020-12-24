package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneOutPay implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String mid;
    
    private String mName;
    
    private String pay;
    
    private String gCode;
    
    private String ip;
    
    public OneOutPay() {

    }
    
}
