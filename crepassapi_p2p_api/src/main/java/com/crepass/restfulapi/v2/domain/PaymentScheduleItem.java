package com.crepass.restfulapi.v2.domain;

import lombok.Data;

@Data
public class PaymentScheduleItem  {
			
	private static final long serialVersionUID = 1L;
    
	private String mid;
	
	private String loanId;
	
	private String pCount;
	
	private String payDate;
	
	private String payStatus;
	
    private long inAmount;
    
    private int interest;
    
    private int overdue;
    
    private int tax;
    
    private int taxLocal;
    
    private int fee;
    
}
