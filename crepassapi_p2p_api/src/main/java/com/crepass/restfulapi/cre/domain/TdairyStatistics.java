package com.crepass.restfulapi.cre.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class TdairyStatistics implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String actCd;
    
    private int mon;

    private int tue;
    
    private int wed;

    private int thi;
    
    private int fri;
    
    private int sat;
    
    private int sun;

    public TdairyStatistics() {

    }
}
