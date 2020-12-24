package com.crepass.restfulapi.v2.domain;

import lombok.Data;

@Data
public class LoanRepayAccntItem  {
			
	private static final long serialVersionUID = 1L;
	
	private String loanId;
	
    private String subject;
    
    private String loanDay;
    
    private String repayWay;
    
    private String loanAccntNo;
    
}
