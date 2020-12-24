package com.crepass.restfulapi.v2.domain;

import java.util.List;

import lombok.Data;

@Data
public class InvestScheduleInfo  {
			
	private static final long serialVersionUID = 1L;

	private String loanSubject;

	private String profitsRate;
	
	private String investPay;
	
    private String iSchePaySum;
    
    private List<InvestScheduleItem> list;
    
}
