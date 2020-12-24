package com.crepass.restfulapi.v2.domain;

import java.io.Serializable;

import com.crepass.restfulapi.one.domain.OneNoticeMain;

import lombok.Data;

@Data
public class SlideInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String mid;
    
    private String mname;
       
    private int mlevel;
    
    private String mProfile;
    
    private String trxInvestAmt;
    
    private String trxLoanAmt;
    
//    private String alarm;
    
//    private String tdiaryMsg;
    
    private String companyNum;
    
    private String isTester;
    
    private OneNoticeMain noticeMsg;
    
}
