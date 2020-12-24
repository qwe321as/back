package com.crepass.restfulapi.v2.domain;

import java.util.List;

import lombok.Data;

@Data
public class LoanScheduleInfo  {
			
	private static final long serialVersionUID = 1L;

	private String loanSubject;

	private String balance;
	
	private String loanPay;
	
    private String totRepay;
    
    private List<LoanScheduleItem> list;
    
}
