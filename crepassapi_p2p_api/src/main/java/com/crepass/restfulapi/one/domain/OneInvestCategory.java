package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneInvestCategory implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String categoryId;
    
    public OneInvestCategory() {

    }
    
}
