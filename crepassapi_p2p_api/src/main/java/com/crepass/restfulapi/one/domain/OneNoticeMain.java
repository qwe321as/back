package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneNoticeMain implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String mNoticeMsg;
    
    private String mNoticeImg;
    
    private String event_code;
    
    private String bannerFlag;
    
    private String fileImg;
    
    public OneNoticeMain() {

    }
    
}
