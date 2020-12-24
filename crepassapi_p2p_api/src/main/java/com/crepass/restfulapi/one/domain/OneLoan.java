package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneLoan implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String mid;
	
	private String mname;
	
	private String birth;
	
	private String mhp;
	
	private String sex;
	
	private String businessname;
	
	private String payment;
	
	private String loanPay;
	
	private String loanDay;
	
	private String yearPlus;
	
	private String repay;
	
	private String repayDay;
	
	private String plan;
	
	private String loanPose;
	
	private String newsagency;
	
	private String occu;
	
	private String homeAddress;
	
	private String loanType;
	
	private String mgubun;
	
	private String graduated;
	
	private String officeworkers;
	
    public OneLoan() {

    }
}
