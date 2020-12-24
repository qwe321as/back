package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneEventJoin implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String event_discount;
    
    private String event_discount_month;
    
    private String event_name;
    
    private String event_start_dt;
    
    private String event_end_dt;
    
    public OneEventJoin() {

    }
    
}
