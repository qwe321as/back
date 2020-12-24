package com.crepass.restfulapi.one.controller;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.config.ExloggerApplication;
import com.crepass.restfulapi.one.domain.OneCreditInfo;
import com.crepass.restfulapi.one.domain.OneCustomerInfo;
import com.crepass.restfulapi.one.service.KCBService;
import com.crepass.restfulapi.one.service.OneMemberService;
import com.crepass.restfulapi.v2.domain.Member;
import com.google.gson.Gson;

import io.swagger.annotations.ApiOperation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@CrossOrigin
@RestController
@RequestMapping(path = "/api", method = {RequestMethod.POST, RequestMethod.GET})
public class KCBController {
    
	private static final Logger logger = LoggerFactory.getLogger(ExloggerApplication.class);
	
	@Autowired
    private KCBService kcbService;
	
    @Autowired
    private OneMemberService oneMemberService;
    
	@Autowired
    private CommonUtil commonUtil;
    
    @Autowired(required=true)
	private HttpServletRequest request;
	
	@Value("${crepas.kcb.url}")
    private String kcbUrl;
	
    @ApiOperation(value = "KCB 신용인증 송부 서비스 신청")
    @RequestMapping("/kcb/certify/request")
    public ResponseEntity<ResponseResult> setKCBCerifyRequest(@RequestBody String requestString) throws Exception {
        
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        JSONObject request = json.getJSONObject("request");
        String mid = request.getString("mid");
        
        OneCustomerInfo oneCustomerInfo = kcbService.selectByCutomerInfo(mid);
        
        int xes = 0;
        
        if(oneCustomerInfo.getSex().toUpperCase().equals("M"))
        	xes = 1;
        else if(oneCustomerInfo.getSex().toUpperCase().equals("W"))
        	xes = 2;
        
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("customerId", oneCustomerInfo.getCustId());
        vars.put("name", oneCustomerInfo.getName());
        vars.put("juminNumer", "");
        vars.put("birth", oneCustomerInfo.getBirth());
        vars.put("xes", String.valueOf(xes));
        vars.put("phone", oneCustomerInfo.getHp());
        vars.put("applyDate", oneCustomerInfo.getApplyDate());
        vars.put("certiConAgree", "Y");
        vars.put("menuDivCd", "200");
        vars.put("scrDivCd", "s08160933241");
        vars.put("reqIp", "13.209.53.139");
        vars.put("reqDomain", "p2p.crepass.com");
        
        ResponseResult response = new ResponseResult();        
        
        String resultKCB = restTemplate.postForObject(kcbUrl + "/0100_360.do", vars, String.class);
        
        JSONObject jsonKCB = new JSONObject(resultKCB);
        
        if(jsonKCB.getString("rspn_cd").equals("0000")) {
        	Map<String, Object> result = new HashMap<String, Object>();
        	result.put("customerId", jsonKCB.getString("customerId"));
        	result.put("applyKey", jsonKCB.getString("applyKey"));
        	result.put("protocol", jsonKCB.getString("protocol"));
        	result.put("domain", jsonKCB.getString("domain"));
        	result.put("uri", jsonKCB.getString("uri"));
        	result.put("secretParam", jsonKCB.getString("secretParam"));
        	
        	kcbService.insertCertifyKCB(oneCustomerInfo.getCustId(), jsonKCB.getString("applyKey"));
        	
        	response.setState(200);
            response.setMessage("정상적으로 처리하였습니다.");
            response.setResult(result);
        } else {
        	response.setState(211);
            response.setMessage(jsonKCB.getString("rspn_msg"));
        }
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);   
    }
    
    @ApiOperation(value = "KCB 신용정보조회")
    @RequestMapping("/kcb/certify/request/get")
    public ResponseEntity<ResponseResult> getKCBCerifyRequest(@RequestBody String requestString) throws Exception {
        
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        JSONObject request = json.getJSONObject("request");
        String customerId = request.getString("customerId");
        String applyKey = request.getString("applyKey");
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        String applyDate = sdf.format(calendar.getTime());
        
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("customerId", customerId);
        vars.put("applyKey", applyKey);
        vars.put("applyDate", applyDate);
        vars.put("retrieveAgree", "Y");
        vars.put("retrieveId", "crepass9");
        
        ResponseResult response = new ResponseResult();
        
        String resultKCB = restTemplate.postForObject(kcbUrl + "/0100_361.do", vars, String.class);
        logger.info("kcb resultKCB : " + resultKCB);
        JSONObject jsonKCB = new JSONObject(resultKCB);
        
        if(jsonKCB.has("rspn_cd")) {
	        if(jsonKCB.getString("rspn_cd").equals("0000")) {
	        	response.setState(200);
	            response.setMessage("정상적으로 처리하였습니다.");
	            kcbService.updateCertifyKCBFlag(applyKey);
	        } else {
	        	response.setState(212);
	            response.setMessage(jsonKCB.getString("rspn_msg"));
	        }
        } else
        	commonUtil.sendRequestLogging(mapping_url, "response : " + jsonKCB.toString());
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);   
    }

    @ApiOperation(value = "전체 신용정보조회(KCB,크레파스)")
    @RequestMapping("/credit/info")
    public ResponseEntity<ResponseResult> getCreditInfo(@RequestBody String requestString) throws Exception {
        
         // {request:{list:[{mid=202012090002}, {mid=202012090003}]}}
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        JSONObject request = json.getJSONObject("request");
        
        JSONArray jsonArray = request.getJSONArray("list");
        
        List<Map<String, Object>> creditInfoList = new ArrayList<Map<String, Object>>();
        
        for(int i=0; i<jsonArray.length(); i++) {
        	String mid = jsonArray.getJSONObject(i).getString("email");		
        	
        	String custId = oneMemberService.selectCustID(mid);
            OneCreditInfo OneCreditInfoCrepass = kcbService.selectCreditCrepassInfo(mid);
//            OneCreditInfo OneCreditInfoKCB = kcbService.selectCreditKCBInfo(custId);
            
            if( OneCreditInfoCrepass != null) {
	            Map<String, Object> creditInfo = new HashMap<>();
	            creditInfo.put("mid", mid);
	            
	            creditInfo.put("kcbScore", OneCreditInfoCrepass.getKcbScore());
	            creditInfo.put("lenndoScore", OneCreditInfoCrepass.getLenndoScore());
	            creditInfo.put("cssScore", OneCreditInfoCrepass.getCssScore());
	            creditInfo.put("creDecision", OneCreditInfoCrepass.getCreDecision());
	            creditInfoList.add(creditInfo);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", creditInfoList);
        
        ResponseResult response = new ResponseResult();
        response.setResult(result);
     	response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);   
        
    }
    

    @ApiOperation(value = "개인정보조회(없당 전송용)")
    @RequestMapping("/member/info")
    public ResponseEntity<ResponseResult> getInfo(@RequestBody String requestString) throws Exception {
        
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        JSONObject request = json.getJSONObject("request");
        
        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        String mobileCorp = request.getString("mobileCorp");
        String hp = request.getString("hp");
        
        Member member = oneMemberService.selectMemberByPhone(mobileCorp, hp);

        Map<String, Object> map = new HashMap<String, Object>(); 
        if (member == null) {
        	map.put("isCheck", "N");
        	
        } else if (member.getTelhp().equals(hp) && member.getTelcoGb().equals(mobileCorp)) {
        	map.put("isCheck", "Y");
        	map.put("memberInfo", member);
        }
        
        response.setResult(map);
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);   
        
    }
    
}