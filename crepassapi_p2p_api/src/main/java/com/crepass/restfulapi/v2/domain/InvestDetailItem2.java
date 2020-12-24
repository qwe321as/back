package com.crepass.restfulapi.v2.domain;

import java.util.Map;

import lombok.Data;

@Data
public class InvestDetailItem2  {
			
	private static final long serialVersionUID = 1L;
    
    private String univName;
    
    private String jobState;
       
    private String birth;
    
    private String gender;
    
    private String repayJob;
    
    private String loanPose;
    
    private String gradeKCB;
    
    private String scoreKCB;
    
    private String gradeLenddo;
    
    private String scoreLenddo;
    
    private String plan;
    
    private Map<String, Long> loanDebt;
    
    private String attention;
    
    private String businessCorp;
    
    private String socialCorp;
    
    private String eduFlag;
    
    private boolean isReady;
    
    private String corpGrade;
    
    private String categoryId; // 법인 개인 구분하기 위함
    
}
