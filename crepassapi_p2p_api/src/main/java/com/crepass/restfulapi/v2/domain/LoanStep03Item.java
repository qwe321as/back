package com.crepass.restfulapi.v2.domain;

import java.util.List;

import lombok.Data;

@Data
public class LoanStep03Item  {
			
	private static final long serialVersionUID = 1L;
    
    private String address;
    
    private String addressDetail;
       
    private List<LoanStepEmergency> emergencyList;
    
    private String isEmail;
    
    private String isPone;
    
    private String isSMS;
    
    private String postCode;
    
    private String repayJob;
    
    private String repayPlan;
	
}
