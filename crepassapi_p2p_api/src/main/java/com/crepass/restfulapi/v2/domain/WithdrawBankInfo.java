package com.crepass.restfulapi.v2.domain;

import lombok.Data;

@Data
public class WithdrawBankInfo  {
			
	private static final long serialVersionUID = 1L;
    
    private String bankCode;
    
    private String bankName;
       
    private String bankAccntName;
    
    private String bankAccntNum;
    
}
