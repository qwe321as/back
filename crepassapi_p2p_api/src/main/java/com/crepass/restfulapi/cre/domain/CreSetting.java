package com.crepass.restfulapi.cre.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class CreSetting implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String mid;
    
    private String alarm;
    
    private String tdiaryMsg;

    public CreSetting() {

    }
    
}
