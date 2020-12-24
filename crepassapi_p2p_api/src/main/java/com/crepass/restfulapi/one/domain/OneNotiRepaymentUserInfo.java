package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneNotiRepaymentUserInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String subject;
    
    private String mid;
    
    private String name;
    
    public OneNotiRepaymentUserInfo() {

    }
    
}
