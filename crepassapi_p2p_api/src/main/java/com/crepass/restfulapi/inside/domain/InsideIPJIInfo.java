package com.crepass.restfulapi.inside.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class InsideIPJIInfo implements Serializable	, Comparable<InsideIPJIInfo> 
{
    
    private static final long serialVersionUID = 1L;
    
    private String paidDate;
    
    private String custId;
    
    private double trAmt;
    
    private String amtGbn;
    
    private String accNb;
    
    private String loanId;
    
    private double trAmtP;
    
    private String taxAmt;
    
    private String fee;
    
    private String receiptNb;

	@Override
	public int compareTo(InsideIPJIInfo o) {
		return paidDate.compareTo(o.getPaidDate());
	}
    
}
