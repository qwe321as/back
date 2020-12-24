package com.crepass.restfulapi.v2.domain;

import lombok.Data;

@Data
public class LoanScheduleItem  {
			
	private static final long serialVersionUID = 1L;
    
    private String count;
    
    private String payAmount;
       
    private String inAmount;
    
    private String interest;
    
    private String overdue;
    
    private String overdueState;
    
    private String repayState;
    
    private String totCount;
    
}
