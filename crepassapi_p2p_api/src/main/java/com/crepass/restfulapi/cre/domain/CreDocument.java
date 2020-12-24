package com.crepass.restfulapi.cre.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class CreDocument implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String docCode;
    
    private String docName;
    
    private String docContents;

    public CreDocument() {

    }
    
}
