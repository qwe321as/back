package com.crepass.restfulapi.v2.domain;

import lombok.Data;

@Data
public class LoanStep01Item  {
			
	private static final long serialVersionUID = 1L;
    
    private String attention;
    
    private String jobName;
       
    private String loanDay;
    
    private String loanPay;
    
    private String loanPose;
    
    private String repayDay;
    
    private String repayWay;
    
    // 개인, 개인-미혼모
    private String loanCate;
	
}
