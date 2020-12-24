package com.crepass.restfulapi.one.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crepass.restfulapi.one.dao.SlideMapper;
import com.crepass.restfulapi.one.domain.OneSlide;
import com.crepass.restfulapi.v2.domain.SlideInfo;

@Service
public class SlideService {

    @Autowired
    SlideMapper slideMapper;
    
    public OneSlide selectWlastById(String mid) throws Exception {
        return slideMapper.selectWlastById(mid);
    }
    
    public SlideInfo selectSlideInfo(String mid) throws Exception {
    	return slideMapper.selectSlideInfo(mid);
    }
}
