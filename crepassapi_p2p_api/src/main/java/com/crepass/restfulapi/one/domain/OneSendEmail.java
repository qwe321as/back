package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneSendEmail implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String m_id;
    
    public OneSendEmail() {

    }
    
}
