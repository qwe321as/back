package com.crepass.restfulapi.v2.domain;

import lombok.Data;

@Data
public class InvestMember {
	
	private long investingPay;		
	private long totDepositPay;		
	private long investPay;			 
	private float profitsRate;		// 수익률
	private int isCheckInvestInfo;  // 가상계좌존재여부
	
	private String investNewFlag="N";		
	private String tranNewFlag="N";

	private int wishTotalCount;
	private int investingTotalCount;
}
