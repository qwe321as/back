package com.crepass.restfulapi.cre.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class CreMember implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String mid;
       
    private String charType;
    
    private String name;
    
    private String alarm;
    
    private String tdiaryMsg;
        
    @Override
    public String toString() {
        return getMid()
                + "," + getCharType();
    }

}
