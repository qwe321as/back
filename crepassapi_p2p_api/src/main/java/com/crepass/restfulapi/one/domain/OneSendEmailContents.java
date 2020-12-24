package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneSendEmailContents implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String fileFk;
    
    private String emailTitle;
    
    private String emailBody;
    
    public OneSendEmailContents() {

    }
    
}
