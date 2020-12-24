package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneLoanMemo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String loanId;
	
	private String oId;
	
	private String memo;
	
    public OneLoanMemo() {

    }
}
