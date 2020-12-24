package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneLoanRepaymentSchedule2 implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String oid;
	
	private String subject;
	
	private String collectiondate;
	
	private String count;
	
	private String repaymentStatus;
	
	private String payAmount;
	
    public OneLoanRepaymentSchedule2() {

    }
}
