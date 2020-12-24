package com.crepass.restfulapi.common.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class ResponseResult implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Integer state;
    
    private String message;
    
    private Object result;

    public ResponseResult() {

    }
    
}