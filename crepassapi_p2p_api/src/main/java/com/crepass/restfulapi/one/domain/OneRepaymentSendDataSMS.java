package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneRepaymentSendDataSMS implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String phone;
    
    private String callback;
    
    private String reqdate;
    
    private String msg;
    
    private String template_code;
    
    private String failed_type;
    
    private String failed_subject;
    
    private String failed_msg;
    
    private String btn_types;
    
    private String btn_txts;
    
    private String btn_urls1;
    
    public OneRepaymentSendDataSMS() {

    }
    
}
