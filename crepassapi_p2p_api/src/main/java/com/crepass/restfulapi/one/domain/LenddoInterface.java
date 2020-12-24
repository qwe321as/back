package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class LenddoInterface implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String sendId;
    
    private String mid;
    
    private String step;

    private String statusCode;
    
    private String statusDesc;
    
    public LenddoInterface() {

    }
    
}
