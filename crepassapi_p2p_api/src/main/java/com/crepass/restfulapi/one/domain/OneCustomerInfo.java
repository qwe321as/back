package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneCustomerInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String custId;
    
    private String name;
    
    private String birth;
    
    private String sex;
    
    private String hp;
    
    private String applyDate;
    
    public OneCustomerInfo() {

    }
    
}
