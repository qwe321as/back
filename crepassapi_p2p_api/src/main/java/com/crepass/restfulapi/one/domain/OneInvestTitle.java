package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneInvestTitle implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String name;
    
    private String custId;
    
    private String subject;
    
    private String investSeq;
    
    private String hp;
    
    public OneInvestTitle() {

    }
    
}
