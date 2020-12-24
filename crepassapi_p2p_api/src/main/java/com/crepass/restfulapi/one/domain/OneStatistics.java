package com.crepass.restfulapi.one.domain;

import java.io.Serializable;
import java.util.List;

import com.crepass.restfulapi.cre.domain.CreSocialBanner;

import lombok.Data;

@Data
public class OneStatistics implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private double saveLoanAmt;
    
    private String saveLoanAmtUnit;
    
    private double savePayAmt;
    
    private String savePayAmtUnit;
    
    private String avgProfAmt;
    
    private double loanBalance;
    
    private String loanBalanceUnit;
    
    private String delqRate;
    
    private String defaultRate;
    
    private String wLastDateTime;
    
    private String trAmt;
    
    private String imgPath;
    
    private List<CreSocialBanner> imgPathArray;

    private String textMessage1;
    
    private String textMessage2;
    
    private String textMessage3;
    
    private String isEventCouponShow;

    public OneStatistics() {

    }
    
}
