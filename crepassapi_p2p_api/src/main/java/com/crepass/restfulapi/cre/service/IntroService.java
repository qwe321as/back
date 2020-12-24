package com.crepass.restfulapi.cre.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.cre.dao.IntroMapper;
import com.crepass.restfulapi.cre.domain.CreIntro;

@Service
public class IntroService {

    @Autowired
    IntroMapper introMapper;
    
    @Autowired
    CommonUtil commonUtil;
    
    public CreIntro selectIntroInfo() throws Exception {
        
        CreIntro creIntro = introMapper.selectIntroInfo();
        
        creIntro.setForceMsg(commonUtil.getForceMsg(creIntro.getForceUpdate()));
        creIntro.setBlockMsg(commonUtil.getForceMsg(creIntro.getForceBlock()));
        
        return creIntro;
        
    }

    public CreIntro selectIntroInfoV2(String apkVersion) throws Exception {
        
        CreIntro creIntro = introMapper.selectIntroInfoV2(apkVersion);
        
        creIntro.setForceMsg(commonUtil.getForceMsg(creIntro.getForceUpdate()));
        creIntro.setBlockMsg(commonUtil.getForceMsg(creIntro.getForceBlock()));
        
        return creIntro;
        
    }

    public int insertIntroApp(JSONObject json) throws Exception {
        
        JSONObject request = (JSONObject) json.get("request");
        CreIntro creIntro = new CreIntro();
    
        creIntro.setPackageName(request.get("packageName").toString());
        creIntro.setAppVersion(request.get("appVersion").toString());
        creIntro.setForceUpdate(request.getInt("forceUpdate"));
        creIntro.setForceBlock(request.getInt("forceBlock"));
        creIntro.setNote(request.get("note").toString());

        return introMapper.insertIntroApp(creIntro);
    }
    
	public int selectIntroInfoCount(String apkVersion) throws Exception {
		 return introMapper.selectIntroInfoCount(apkVersion);
	}

	public int insertIntroAppV2(String apkVersion, String liveFlag) throws Exception {
		 return introMapper.insertIntroAppV2(apkVersion, liveFlag);
	}

	public String selectNewAppVersion() throws Exception {
		 return introMapper.selectNewAppVersion();
	}
}
