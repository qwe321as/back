package com.crepass.restfulapi.cre.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class Tdairy implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String actCd;
    
    private String onOff;
    
    private int min;

    public Tdairy() {

    }
    
    public Tdairy(String actCd, String onOff, int min) {
        this.actCd = actCd;
        this.onOff = onOff;
        this.min = min;
    }
}