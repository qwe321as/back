package com.crepass.restfulapi.v2.domain;

import lombok.Data;

@Data
public class InvestAutoSplit  {
			
	private static final long serialVersionUID = 1L;
    
	private String aid;
    
    private String mid;
    
    private String isActivate;
    
    private String limitLoan;
    
    private String limitMonth;
    
    private String agreedYN;
    
}
