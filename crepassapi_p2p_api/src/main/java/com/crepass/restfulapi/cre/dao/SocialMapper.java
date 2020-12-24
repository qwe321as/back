package com.crepass.restfulapi.cre.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.crepass.restfulapi.cre.domain.CreSocial;
import com.crepass.restfulapi.cre.domain.CreSocialBanner;

@Component
public interface SocialMapper {
    
    public List<CreSocial> selectSocialList() throws Exception;

    public List<CreSocialBanner> selectSocialBannerList() throws Exception;
}
