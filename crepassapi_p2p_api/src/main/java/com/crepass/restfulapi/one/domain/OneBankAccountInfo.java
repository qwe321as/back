package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneBankAccountInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String bankCode;
    
    private String bankName;
    
    public OneBankAccountInfo() {

    }
    
}
