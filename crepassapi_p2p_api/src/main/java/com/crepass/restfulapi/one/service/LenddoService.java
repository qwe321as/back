package com.crepass.restfulapi.one.service;

import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crepass.restfulapi.one.dao.LenddoMapper;
import com.crepass.restfulapi.one.domain.LenddoInterface;
import com.crepass.restfulapi.one.domain.OneLenddoWebhookInfo;

@Service
public class LenddoService {
    @Autowired
    LenddoMapper lenddoMapper;
    
	public LenddoInterface insertLenddoTransResult(JSONObject jsonMember) throws Exception {
        
        JSONObject request = (JSONObject) jsonMember.get("request");
        
        LenddoInterface lenddoTrans = new LenddoInterface();
        lenddoTrans.setSendId(request.get("sendId").toString());
        lenddoTrans.setMid(request.get("mid").toString());
        lenddoTrans.setStep(request.get("step").toString());
        lenddoTrans.setStatusCode(request.get("statusCode").toString());
        lenddoTrans.setStatusDesc(request.get("statusDesc").toString());
        
        System.out.println("LenddoInterface: " + lenddoTrans.toString());
        
        int rtnvalue = lenddoMapper.insertLenddoTrans(lenddoTrans);
        
        if (rtnvalue == 0) {
        	lenddoTrans = null;
        } 
        
        return lenddoTrans;
	}

	public String selectLenddoById(String appId) throws Exception {
		return lenddoMapper.selectLenddoById(appId);
	}
	
	public boolean insertLenddoSendHistory(String appId, String mid) throws Exception {
		return lenddoMapper.insertLenddoSendHistory(appId, mid);
	}
	
	public boolean insertLenddoWebhoook(String appId, String time) throws Exception {
		return lenddoMapper.insertLenddoWebhoook(appId, time);
	}
    
    public boolean deleteLenddoWebhoook(String appId) throws Exception {
    	return lenddoMapper.deleteLenddoWebhoook(appId);
    }
    
    public List<OneLenddoWebhookInfo> selectLenddoWebhoook() throws Exception {
    	return lenddoMapper.selectLenddoWebhoook();
    }
}
