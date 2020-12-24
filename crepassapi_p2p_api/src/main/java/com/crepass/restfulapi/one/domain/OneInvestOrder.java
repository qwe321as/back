package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneInvestOrder implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String count;
    
    private String investamount;
    
    private String inMoneyTo;
    
    private String interest;
    
//    private String ipaySaleamount;
//    
//    private String balance;
    
    private String tax;
    
    private String fee;
    
    public OneInvestOrder() {

    }
    
}
