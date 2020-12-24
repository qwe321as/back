package com.crepass.restfulapi.cre.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class CreSocialBanner implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String id;
    
    private String corpName;
    
    private String bannerUrl;
    
    private String memo;
    
    private String sort;
    
    public CreSocialBanner() {

    }
    
}
