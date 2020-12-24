package com.crepass.restfulapi.cre.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crepass.restfulapi.cre.dao.SocialMapper;
import com.crepass.restfulapi.cre.domain.CreSocial;
import com.crepass.restfulapi.cre.domain.CreSocialBanner;

@Service
public class SocialService {

    @Autowired
    SocialMapper socialMapper;
    
    public List<CreSocial> selectSocialList() throws Exception {
        return socialMapper.selectSocialList();
    }
    
    public List<CreSocialBanner> selectSocialBannerList() throws Exception {
    	return socialMapper.selectSocialBannerList();
    }
}