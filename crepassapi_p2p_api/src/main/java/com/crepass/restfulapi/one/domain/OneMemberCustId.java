package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneMemberCustId implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String name;
    
    private String custId;
    
    public OneMemberCustId() {

    }
    
}
