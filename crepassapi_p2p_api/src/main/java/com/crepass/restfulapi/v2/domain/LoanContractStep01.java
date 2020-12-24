package com.crepass.restfulapi.v2.domain;

import lombok.Data;

@Data
public class LoanContractStep01  {
			
	private static final long serialVersionUID = 1L;
    
    private String mname;
    
    private String birth;
    
    private String address;
    
    private String hp;
    
    private String bankName;
    
    private String bankAccntNum;
    
    private String repayDay;
    
    private String repayWay;
	
}
