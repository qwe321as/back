package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class Board implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Long wid;

    private String wtable;
    
    private String wreply;
    
    private String wcatecode;
    
    private String wcomment;
    
    private String wsubject;
    
    private String wcontent;
    
    private String wrink;
    
    private String wfileImg;
    
    private String createdDt;
    
    public Board() {

    }
    
}
