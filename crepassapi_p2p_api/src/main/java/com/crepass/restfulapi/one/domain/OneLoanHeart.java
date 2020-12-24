package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneLoanHeart implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String memoId;
	
	private String loanId;
	
	private String mid;
	
	private String heart;
	
    public OneLoanHeart() {

    }
}
