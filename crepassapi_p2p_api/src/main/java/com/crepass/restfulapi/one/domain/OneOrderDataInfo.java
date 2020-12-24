package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneOrderDataInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String o_count;
    
    private String deposit;
    
    private String o_maturity;
    
    private String o_id;
    
    private String title;
    
    private String o_date;
    
    private String o_status;
    
    private String memoId;
    
    private String memo;
    
    private String createDt;
    
    private Object heart;
    
    public OneOrderDataInfo() {

    }
    
}
