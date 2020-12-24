package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneCertifyWebDump implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String bi;
    
    private String certifyResult;
       
    private String createDate;
	
    public OneCertifyWebDump() {

    }
}
