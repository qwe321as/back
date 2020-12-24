package com.crepass.restfulapi.v2.domain;

import java.util.Map;

import lombok.Data;

@Data
public class InvestDetailItem  {
			
	private static final long serialVersionUID = 1L;
    
    private String univName;
    
    private String jobState;
       
    private String birth;
    
    private String gender;
    
    private String repayJob;
    
    private String loanPose;
    
    private String gradeKCB;
    
    private String gradeLenddo;
    
    private String plan;
    
    private Map<String, Long> loanDebt;
    
    private String attention;
    
    private String businessCorp;
    
    private String socialCorp;
    
    private String eduFlag;
    
    private boolean isReady;
    
    
}
