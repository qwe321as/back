package com.crepass.restfulapi.cre.domain;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class CreTdairy implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String mid;
    
    private String actCd;
    
    private String onOff;
    
    public CreTdairy() {

    }

}

