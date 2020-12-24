package com.crepass.restfulapi.cre.dao;

import org.springframework.stereotype.Component;

import com.crepass.restfulapi.cre.domain.CreIntro;

@Component
public interface IntroMapper {
    
    public CreIntro selectIntroInfo() throws Exception;
    
	public CreIntro selectIntroInfoV2(String apkVersion) throws Exception;

    public int insertIntroApp(CreIntro creIntro) throws Exception;

	public int selectIntroInfoCount(String apkVersion) throws Exception;
	
	public int insertIntroAppV2(String apkVersion, String liveFlag) throws Exception;

	public String selectNewAppVersion() throws Exception;
}
