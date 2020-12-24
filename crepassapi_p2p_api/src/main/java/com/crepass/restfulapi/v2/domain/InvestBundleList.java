package com.crepass.restfulapi.v2.domain;

import lombok.Data;

@Data
public class InvestBundleList  {
			
	private static final long serialVersionUID = 1L;
    
    private String loanId;
    
    private String loanSubject;
    
    private String loanSubjectA;
    private String loanSubjectB;
       
    private long loanMoney;
    
    private String corpName;
    
    private String socialName;
    
    private String isBookMark;
    
    private long investingPay;
    
    private long investMaxPay;
    
    private float investMaxRate;
    
    private String eduFlag;
    
}
