package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneInvestInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private long investPay;
    
    private long investMax;
    
    private String yearPlus;
    
    private String investLevel;
    
    private String grade;

    private String loanDay;
    
    private String loanPay;
    
    private String investUsePay;
    
    private String investMaxType;	// investMax값이 경우에 따라 3가지 값이 들어가기 때문에 경우에 대한 타입 명시
    
    private String investMaxMsg;	// investMax에 대한 메세지
    
    
    
    public OneInvestInfo() {

    }
    
}
