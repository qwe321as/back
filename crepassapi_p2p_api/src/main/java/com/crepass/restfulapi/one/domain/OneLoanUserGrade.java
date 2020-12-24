package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneLoanUserGrade implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String lenddoGrade;
    
    private String kcbGrade;
    
    public OneLoanUserGrade() {

    }
    
}
