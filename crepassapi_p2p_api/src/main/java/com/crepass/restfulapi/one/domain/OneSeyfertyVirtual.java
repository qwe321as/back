package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneSeyfertyVirtual implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String accntNo;
    
    private String bnkCd;

    private String mid;
    
}
