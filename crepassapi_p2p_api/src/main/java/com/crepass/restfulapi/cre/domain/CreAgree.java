package com.crepass.restfulapi.cre.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class CreAgree implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String mid;
    
    private String uinfProcess;
    
    private String cinfProvide;

    private String cinfRetrieve;
    
    private String cinfGather;
    
    private String minfReceive;

    public CreAgree() {

    }
    
}
