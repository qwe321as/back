package com.crepass.restfulapi.v2.domain;

import lombok.Data;

@Data
public class LoanMember {
	
	private long loanMoney;     // 대출금액
	private long repayMoney;    // 상환금액
	private long balanceMoney;  // 잔액
	private float latePercent;  // 연체율(예전버전, 200526 이후로 사용안함)
	private float repayPercent;  // 상환율
	
	private int contractNum;	// 약정번호
	
	private String loanContNewFlag = "N";	// 대출현황 New표시
	private String cGrade;
	private String scoreLenddo;		// 201221 렌도등급
	private String loanCount;
	
	public String getcGrade() {
		return cGrade;
	}
	public void setcGrade(String cGrade) {
		this.cGrade = cGrade;
	}

}
