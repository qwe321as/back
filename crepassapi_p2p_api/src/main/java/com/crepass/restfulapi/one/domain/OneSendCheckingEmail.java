package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneSendCheckingEmail implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String loanId;
    
    private String repayCount;
    
    private String repayDate;
    
    private String payAmount;
    
    private String lnAmount;
    
    private String interestAmount;
    
    private String balance;
    
    private String tax;
    
    private String taxLocal;
    
    
    
    // 검증용
    
    private String overDue;
    private String payStatus;
    private String tax_real_afterOverdue;		// 연체가 적용된 세금
    private String tax_local_real_afterOverdue;	// 연체가 적용된 지방세금
    private String tax_inDB;		// 프로그램으로 계산한 값과 비교하기 위해  DB값 넣을 변수   
    private String taxLocal_inDB;	// 프로그램으로 계산한 값과 비교하기 위해  DB값 넣을 변수
    
    
    
    
    public OneSendCheckingEmail() {

    }
    
}
