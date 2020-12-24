package com.crepass.restfulapi.cre.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crepass.restfulapi.cre.dao.AgreeMapper;
import com.crepass.restfulapi.cre.domain.CreAgree;
import com.crepass.restfulapi.cre.domain.CreDocument;
import com.crepass.restfulapi.cre.domain.CreInvestAgreed;
import com.crepass.restfulapi.cre.domain.CreLoanAgreed;
import com.crepass.restfulapi.cre.domain.CreLoanAgreed2;
import com.crepass.restfulapi.cre.domain.CreSetting;

@Service
public class AgreeService {

    @Autowired
    AgreeMapper agreeMapper;
    
    public CreAgree insertCreAgree(JSONObject jsonMember) throws Exception {
        
        JSONObject request = (JSONObject) jsonMember.get("request");
        
        CreAgree creAgree = new CreAgree();
        creAgree.setMid(request.get("mid").toString());
        creAgree.setUinfProcess(request.get("uinfProcess").toString());
        creAgree.setCinfProvide(request.get("cinfProvide").toString());
        creAgree.setCinfRetrieve(request.get("cinfRetrieve").toString());
        creAgree.setCinfGather(request.get("cinfGather").toString());
        creAgree.setMinfReceive(request.get("minfReceive").toString());
        
        System.out.println("CreAgree: " + creAgree.toString());
        
        int rtnvalue = agreeMapper.insertCreAgree(creAgree);
        if (rtnvalue == 0) {
            creAgree = null;
        } 
        
        return creAgree;
    }

    public CreSetting updateCreSetting(JSONObject json) throws Exception {
        
        JSONObject request = (JSONObject) json.get("request");
        
        CreSetting creSetting = new CreSetting();
        creSetting.setMid(request.get("mid").toString());
        creSetting.setAlarm(request.get("alarm").toString());
        creSetting.setTdiaryMsg(request.get("tdiaryMsg").toString());
        
        int rtnvalue = agreeMapper.updateCreSetting(creSetting);
        if (rtnvalue != 1) {
            creSetting = null;
        } 
        
        return creSetting;
    }
    
    public CreDocument insertDocument(JSONObject json) throws Exception {
        
        JSONObject request = (JSONObject) json.get("request");
        
        CreDocument creDocument = new CreDocument();
        creDocument.setDocCode(request.get("docCode").toString());
        creDocument.setDocName(request.get("docName").toString());
        creDocument.setDocContents(request.get("docContents").toString());
        
        int rtnvalue = agreeMapper.insertDocument(creDocument);
        if (rtnvalue == 0) {
            creDocument = null;
        } 
        
        return creDocument;
    }

    public CreDocument selectDocumentById(JSONObject json) throws Exception {
        
        JSONObject request = (JSONObject) json.get("request");
        
        CreDocument creDocument = agreeMapper.selectDocumentById(request.get("docCode").toString());
        
        return creDocument;
    }

    public void insertCreLoanAgreed(JSONObject json) throws Exception {
        
        JSONObject request = (JSONObject) json.get("request");
        JSONObject agreed = (JSONObject) request.get("agreement");
        
        CreLoanAgreed creLoanAgreed = new CreLoanAgreed();
        creLoanAgreed.setMid(request.get("mid").toString());
        creLoanAgreed.setUinfProcess(agreed.get("uinfProcess").toString());
        creLoanAgreed.setCinfProvide(agreed.get("cinfProvide").toString());
        creLoanAgreed.setCinfRetrieve(agreed.get("cinfRetrieve").toString());
        creLoanAgreed.setCinfGather(agreed.get("cinfGather").toString());
        creLoanAgreed.setMinfReceive(agreed.get("minfReceive").toString());
        creLoanAgreed.setLinfSms(agreed.get("linfSms").toString());
        creLoanAgreed.setLinfEmail(agreed.get("linfEmail").toString());
        creLoanAgreed.setLinfHp(agreed.get("linfHp").toString());
        creLoanAgreed.setPinfGather(agreed.get("pinfGather").toString());
        creLoanAgreed.setUinfGather(agreed.get("uinfGather").toString());
        
        agreeMapper.insertCreLoanAgreed(creLoanAgreed);
    }

    public void insertCreInvestAgreed(JSONObject json) throws Exception {
        
        JSONObject request = (JSONObject) json.get("request");
        JSONObject agreed = (JSONObject) request.get("agreement");
        
        CreInvestAgreed creInvestAgreed = new CreInvestAgreed();
        creInvestAgreed.setMid(request.get("mid").toString());
        creInvestAgreed.setInriskGuide(agreed.get("inriskGuide").toString());
        creInvestAgreed.setPinfGather(agreed.get("pinfGather").toString());
        creInvestAgreed.setInvestUse(agreed.get("investUse").toString());
        creInvestAgreed.setInriskNotice(agreed.get("inriskNotice").toString());
        
        agreeMapper.insertCreInvestAgreed(creInvestAgreed);
    }
    
    public void insertCreLoanAgreed2(CreLoanAgreed2 creLoanAgreed2) throws Exception {
    	agreeMapper.insertCreLoanAgreed2(creLoanAgreed2);
    }
    
}
