package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneLoanInvestInfoDetail implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String birth;
	
	private String businessname;
	
	private String occu;
	
	private String graduated;
	
	private String socialCorp;
	
	private String corpStartDt;
	
	private String corpEndDt;
	
	private String goal;
	
	private String loanPose;
	
	private String plan;
	
	private String gender;
	
    public OneLoanInvestInfoDetail() {

    }
}
