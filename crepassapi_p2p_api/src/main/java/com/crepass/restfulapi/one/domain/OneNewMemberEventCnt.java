package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneNewMemberEventCnt implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String newMemberTotal;
	
	private String loanTotal;
	
	private String loanExecTotal;
	
    public OneNewMemberEventCnt() {

    }
}
