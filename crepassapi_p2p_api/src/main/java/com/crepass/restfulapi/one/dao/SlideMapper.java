package com.crepass.restfulapi.one.dao;

import com.crepass.restfulapi.one.domain.OneSlide;
import com.crepass.restfulapi.v2.domain.SlideInfo;

public interface SlideMapper {
    
    public OneSlide selectWlastById(String mid) throws Exception;

    public SlideInfo selectSlideInfo(String mid) throws Exception;
}
