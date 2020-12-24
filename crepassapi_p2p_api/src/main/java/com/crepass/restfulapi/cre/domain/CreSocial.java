package com.crepass.restfulapi.cre.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class CreSocial implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String corpId;
    
    private String corpName;
    
    public CreSocial() {

    }
    
}
