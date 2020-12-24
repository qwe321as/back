package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneSendEmailInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private long emailFk;
    
    private String from;
    
    private String to;
    
    private String is_sending;
    
    private String result;
    
    private String to_name;
    
    private String send_dt;
    
    public OneSendEmailInfo() {

    }
    
}
