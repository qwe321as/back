package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneCreditInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String kcbScore;
    
    private String lenndoScore;
       
    private String cssScore;

    private String creDecision;
    
    private String mid;
       
}
