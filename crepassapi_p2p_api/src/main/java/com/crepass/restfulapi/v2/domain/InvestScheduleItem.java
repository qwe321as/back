package com.crepass.restfulapi.v2.domain;

import lombok.Data;

@Data
public class InvestScheduleItem  {
			
	private static final long serialVersionUID = 1L;
    
    private int count;
    
    private String monthPayment;
       
    private String inAmount;
    
    private String interest;
    
    private String overdue;
    
    private int fee;
    
    private int tax;
    
    private int taxLocal;
    
    private int overdueState;
    
    private String paymentState;
    
    private String totCount;
    
}
