package com.crepass.restfulapi.v2.domain;

import java.util.List;

import lombok.Data;

@Data
public class PaymentHistoryInfo  {
			
	private static final long serialVersionUID = 1L;
    
    private String trxAmt;
    
    private String accntNo;
       
    private String bankName;
    
    private int totPageCount;
    
    private List<PaymentHistoryItem> list;
}
