package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneSeyfert implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String mid;
    
    private String name;
       
    private String memGuid;
    
    private String ipAddr;
    
    private String memUse;
    
    private String telhp;

}
