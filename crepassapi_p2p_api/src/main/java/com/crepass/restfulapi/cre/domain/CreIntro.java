package com.crepass.restfulapi.cre.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class CreIntro implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String packageName;
    
    private String appVersion = "0.1.1";
    
    private int forceUpdate;

    private String forceMsg;
    
    private int forceBlock;
    
    private String blockMsg;
    
    private String note;
    
    private String aesEncrypt;
    
    private String newAppVersion;
    
    public CreIntro() {

    }
    
}
