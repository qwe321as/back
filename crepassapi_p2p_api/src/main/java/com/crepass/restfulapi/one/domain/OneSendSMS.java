package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneSendSMS implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String createDt;
    
    private String name;
    
    private String type;
    
    private String request;
    
    public OneSendSMS() {

    }
    
}
