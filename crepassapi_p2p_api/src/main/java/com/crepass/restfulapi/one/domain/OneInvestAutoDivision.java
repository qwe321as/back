package com.crepass.restfulapi.one.domain;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class OneInvestAutoDivision implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String aid;
    
    private String mid;
    
    private String isActivate;
    
    private String limitLoan;
    
    private String limitMonth;
    
    private String univName;
    
    private String agreedYN;
    
    private List<OneInvestCategory> category;
    
    public OneInvestAutoDivision() {

    }
    
}
