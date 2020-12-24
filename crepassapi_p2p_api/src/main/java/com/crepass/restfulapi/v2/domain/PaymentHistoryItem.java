package com.crepass.restfulapi.v2.domain;

import lombok.Data;

@Data
public class PaymentHistoryItem  {
			
	private static final long serialVersionUID = 1L;
    
    private String trxAmt;
    
    private String trxType;
       
    private String createDt;
}
