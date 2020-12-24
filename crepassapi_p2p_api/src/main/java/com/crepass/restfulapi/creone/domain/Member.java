package com.crepass.restfulapi.creone.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class Member implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String name;
    
    private String mid;
       
    private String passwd;
    
    private String telhp;
    
    private String zip;
    
    private String addr1;
    
    private String addr2;
    
    private String sms;

    private String birth;
    
    private String xes;
    
    private String blindness;
    
	private String signPurpose;
	
    private String charType;
		
    //
    private Long id;
        
    @Override
    public String toString() {
        return getId() 
        		+ "," + getName() 
        		+ "," + getMid() 
        		+ "," + getTelhp() 
        		+ "," + getSms() 
        		+ "," + getZip()
        		+ "," + getAddr1()
        		+ "," + getAddr2()
        		+ "," + getBirth()
        		+ "," + getXes()
        		+ "," + getBlindness()
        		+ "," + getSignPurpose()
        		;
    }
}
