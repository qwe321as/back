package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneOverdueNumberOfCount implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String rCount;
    
    private String minCount;
    
    private String loanId;
    
    private String mid;
    
    public OneOverdueNumberOfCount() {

    }
    
}
