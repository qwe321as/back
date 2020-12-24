package com.crepass.restfulapi.v2.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneEventItem implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String event_code;
    
    private String event_name;
    
//    private String event_discount;
    
//    private String event_discount_month;
    
    private String event_contents;
    
    private String event_img_url;
    
    private String is_use;
    
//    private String is_show;
    
    private String targetFlag;
    
//    private String mainFlag;
    
    private String event_start_dt;
    
    private String event_end_dt;
    
    private String created_dt;
    
    public OneEventItem() {

    }
    
}
