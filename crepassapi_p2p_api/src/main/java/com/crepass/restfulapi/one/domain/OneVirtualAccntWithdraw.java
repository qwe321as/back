package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneVirtualAccntWithdraw implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    
    private String mid;
    
    private String trxAmt;
    
    private String memo;

}
