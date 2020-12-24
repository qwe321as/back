package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneLoanInvestInfoDetailReply implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String id;
	
	private String memo;
	
	private String createDt;
	
	private String heart;
	
    public OneLoanInvestInfoDetailReply() {

    }
}
