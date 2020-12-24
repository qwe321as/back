package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneSlide implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String appVersion;
    
    private String wLastDateTime;
    
    private String trAmt;
    
    private String charType;
    
    private String contractUrl;
    
    private String receiptUrl;
    
    private String loanapproval;
    
    private String alarm;
    
    private String tdiaryMsg;
    
    private OneNoticeMain noticeMsg;
    
    private String isAutoInvest;

    public OneSlide() {

    }
    
}
