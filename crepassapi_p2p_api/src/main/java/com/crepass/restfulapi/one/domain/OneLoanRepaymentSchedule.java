package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneLoanRepaymentSchedule implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String count;
	
	private String payAmount;
	
	private String lnAmount;
	
	private String interestAmount;
	
	private String balance;
	
	private String repayDate;
	
	private String loanAccntNo;
	
    public OneLoanRepaymentSchedule() {

    }
}
