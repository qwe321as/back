package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneRestOverdueBalance implements Serializable {

	private static final long serialVersionUID = 1L;

    private String delqPay;
    
    private String prePay;
    
    public OneRestOverdueBalance() {

    }
}
