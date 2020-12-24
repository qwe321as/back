package com.crepass.restfulapi.v2.domain;

import lombok.Data;

@Data
public class MemberInvestInfo  {
			
	private static final long serialVersionUID = 1L;
    
    private String bankCode;
    
    private String bankAccntName;
       
    private String bankAccntNum;
    
    private String bankName;
    
    private String birth;
    
    private String gender;
    
    private String reginum;
    
    private String investVirAccntNum;
	
}
