package com.crepass.restfulapi.v2.domain;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class LoanStepInfo  {
			
	private static final long serialVersionUID = 1L;
    
    private String mid;
    
    private String mname;
    
    private String birth;
    
    private String hp;
    
    private String gender;
    
    private String newsagency;
    
    private LoanStep01Item loanStep01Item;
       
    private LoanStep03Item loanStep03Item;
    
    private Agreement agreement;
    
    private String socialId;
    
    private MultipartFile uploadFile;
	
}
