package com.crepass.restfulapi.cre.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class TdairyWeeks implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String actCd;
    
    private int days;
    
    private int weekTime;

    public TdairyWeeks() {

    }
    
    public TdairyWeeks(String actCd, int days, int weekTime) {
        this.actCd = actCd;
        this.days = days;
        this.weekTime = weekTime;
    }
}
