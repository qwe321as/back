package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneWishInvest implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String loanId;
    
    private String investName;
    
    private String investPay;
    
    private String yearPlus;
    
    private String investCredit;
    
    private String investPer;
    
    private String focFlag;
    
    private String medalFlag;
    
    private String companyLogo;
    
    private String eduFlag;
    
    private String corpGrade;
    
    private String categoryId;	// 법인, 개인 구분하기 위함
    
    
    
    public OneWishInvest() {

    }
    
}
