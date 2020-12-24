package com.crepass.restfulapi.cre.service;

import com.crepass.restfulapi.cre.dao.CreMemberMapper;
import com.crepass.restfulapi.cre.domain.CreMember;
import com.crepass.restfulapi.cre.domain.CreMemberAgreed;
import com.crepass.restfulapi.one.domain.OneEventItem;
import com.crepass.restfulapi.v2.domain.Agreement;
import com.crepass.restfulapi.v2.domain.AgreementItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreMemberService {

    @Autowired
    private CreMemberMapper creMemberMapper;
    
    public CreMember addCreMember(JSONObject jsonMember) throws Exception {
        
        JSONObject request = (JSONObject) jsonMember.get("request");
        
        CreMember creMember = new CreMember();
        creMember.setMid(request.get("mid").toString());
        creMember.setCharType(request.get("charType").toString());
        creMember.setName(request.get("name").toString());
        
        System.out.println("OneMember: " + creMember.toString());
        
        JSONObject agreed = (JSONObject) request.get("agreement");

        CreMemberAgreed oneMemberAgreed = new CreMemberAgreed();
        oneMemberAgreed.setMid(request.get("mid").toString());
        oneMemberAgreed.setSelfConfirm(agreed.get("selfConfirm").toString());
        oneMemberAgreed.setPinfGather(agreed.get("pinfGather").toString());
        oneMemberAgreed.setUinfProcess(agreed.get("uinfProcess").toString());
        oneMemberAgreed.setSelfService(agreed.get("selfService").toString());
        oneMemberAgreed.setTelcoGb(agreed.get("telcoGb").toString());
        oneMemberAgreed.setSms(agreed.get("sms").toString());
        oneMemberAgreed.setAgreeTerms(agreed.get("agreeTerms").toString());
        
        System.out.println("CreMemberAgreed: " + oneMemberAgreed.toString());
        
        int rtnvalue = creMemberMapper.insertCreMember(creMember);
        creMemberMapper.insertCreMemberAgreed(oneMemberAgreed);
        
        if (rtnvalue == 0) {
            creMember = null;
        } 
        
        return creMember;
        
    }

    public CreMember selectMemberById(String mid) throws Exception {
        return creMemberMapper.selectMemberById(mid);
    }
    
    public int deleteMemberById(String mid) throws Exception {
        final int rtn = creMemberMapper.deleteBackupMemberById(mid);
        int rtnValue = 0;
        if (rtn == 1) {
            rtnValue = creMemberMapper.deleteMemberById(mid);
        }
        return rtnValue;
    }

    public int updateMemberById(JSONObject jsonMember) throws Exception {
        
        JSONObject request = (JSONObject) jsonMember.get("request");

        return creMemberMapper.updateMemberById(request.get("mid").toString(), request.get("charType").toString());
    }
    
    public List<String> selectPushTarget() throws Exception {
        return creMemberMapper.selectPushTarget();
    }

    public int updateMemberByPlayerId(String mid, String playerId) throws Exception {
        return creMemberMapper.updateMemberByPlayerId(mid, playerId);
    }
    
    // api v2 start
    public CreMember addCreAgreeMember(JSONObject jsonMember, String custId) throws Exception {
    	JSONObject request = jsonMember.getJSONObject("request");
    	
    	CreMember creMember = new CreMember();
        creMember.setMid(request.getString("mid"));
        creMember.setName(request.getString("name"));
        
        JSONObject agreed = request.getJSONObject("agreement");
        JSONArray agreeList = agreed.getJSONArray("list");
        
        creMemberMapper.insertCreMember(creMember);
        
    	for(int i=0; i<agreeList.length(); i++) {
    		Map<String,Object> map = new HashMap<>();
        	map.put("custId",custId);
        	map.put("agreeCodeName", agreeList.getJSONObject(i).getString("agreeCodeName").toUpperCase());
        	map.put("isAgree", agreeList.getJSONObject(i).getString("isAgree"));
        	
        	creMemberMapper.insertAgreeList(map);
        }
    	
    	return creMember;
    }
    
    public void addCreAgreeMember2(JSONObject jsonMember, String custId) throws Exception {
    	JSONObject request = jsonMember.getJSONObject("request");
        JSONObject agreed = request.getJSONObject("agreement");
        JSONArray agreeList = agreed.getJSONArray("list");
        
    	for(int i=0; i<agreeList.length(); i++) {
    		Map<String,Object> map = new HashMap<>();
        	map.put("custId",custId);
        	map.put("agreeCodeName", agreeList.getJSONObject(i).getString("agreeCodeName").toUpperCase());
        	map.put("isAgree", agreeList.getJSONObject(i).getString("isAgree"));
        	
        	creMemberMapper.insertAgreeList(map);
        }
    }
    
    public void addCreAgreeMember3(Agreement agreement, String custId) throws Exception {
        List<AgreementItem> agreeList = agreement.getList();
        
    	for(int i = 0; i < agreeList.size(); i++) {
    		Map<String,Object> map = new HashMap<>();
        	map.put("custId",custId);
        	map.put("agreeCodeName", agreeList.get(i).getAgreeCodeName().toUpperCase());
        	map.put("isAgree", agreeList.get(i).getIsAgree().toUpperCase());
        	
        	creMemberMapper.insertAgreeList(map);
        }
    }


}
