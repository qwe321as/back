package com.crepass.restfulapi.cre.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class CreUniver implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String schoolName;
    
    public CreUniver() {

    }
    
}
