package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneWithhold implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String withZip;
    
    private String withAddress1;
    
    private String withAddress2;
    
    public OneWithhold() {

    }
    
}
