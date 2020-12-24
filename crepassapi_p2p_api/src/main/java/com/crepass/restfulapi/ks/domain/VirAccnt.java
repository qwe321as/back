package com.crepass.restfulapi.ks.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class VirAccnt implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String BANK_CODE;
    
    private String VR_ACCT_NO;
    
    private String CORP_NAME;
    
    private String USE_FLAG;
    
    private String REF_NO;
    
    public VirAccnt() {

    }
    
}
