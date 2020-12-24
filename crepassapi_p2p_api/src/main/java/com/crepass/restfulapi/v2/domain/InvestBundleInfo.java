package com.crepass.restfulapi.v2.domain;

import java.util.List;

import lombok.Data;

@Data
public class InvestBundleInfo  {
			
	private static final long serialVersionUID = 1L;
    
    private long investAmt;
    
    private long totInvestMaxPay;
    
    List<InvestBundleList> list;
    
}
