package com.crepass.restfulapi.v2.domain;

import lombok.Data;

@Data
public class MemberAccntInfo  {
			
	private static final long serialVersionUID = 1L;
    
    private String mid;
    
    private String bankCode;
       
    private String bankAccntNum;
    
    private String bankAccntName;
    
    private String bankName;
    
}
