package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneCertify implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String mno;
    
    private String certifyType;
       
    private String certifyResult;

}
