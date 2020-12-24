package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneEventDiscount implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String event_discount;
    
    private String event_discount_month;
    
    public OneEventDiscount() {

    }
    
}
