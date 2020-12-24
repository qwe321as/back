package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneVirtualAccnt implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String account;
    
    private String type;
    
    private String is_use;

}
