package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneMember implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String name;
    
    private String mid;
       
    private String passwd;
    
    private String newpasswd;
    
    private String oldpasswd;
    
    private String telhp;
    
    private String zip;
    
    private String addr1;
    
    private String addr2;
    
    private String birth;
    
    private int sms;
    
    private String smsInvest;

    private String xes;

    private String blindness;

    private String signPurpose;

    private Long id;
    
    private String telcoGb;
    
    private String custId;
    
    private String influxType;

}
