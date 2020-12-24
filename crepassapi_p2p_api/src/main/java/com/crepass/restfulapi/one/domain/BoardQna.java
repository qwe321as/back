package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class BoardQna implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Long fid;

    private String fsort;
    
    private String fquestion;
    
    private String fanswer;
    
    private String fregidate;
    
    public BoardQna() {

    }
    
}
