package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneLenddoWebhookInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String app_id;
    
    private String created_dt;
    
    public OneLenddoWebhookInfo() {

    }
    
}
