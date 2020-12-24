package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneRateInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String tax;
    
    private String taxLocal;
    
    private String fee;
    
    public OneRateInfo() {

    }
    
}
