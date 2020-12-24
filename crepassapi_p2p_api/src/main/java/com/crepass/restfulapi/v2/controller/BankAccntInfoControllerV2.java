package com.crepass.restfulapi.v2.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerMapping;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.one.domain.OneMemberCustId;
import com.crepass.restfulapi.one.service.BankAccntInfoService;
import com.crepass.restfulapi.one.service.OneMemberService;
import com.crepass.restfulapi.v2.domain.MemberAccntInfo;
import com.google.gson.Gson;

import io.swagger.annotations.ApiOperation;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@CrossOrigin
@RestController
@RequestMapping(path = "/api2", method = {RequestMethod.POST, RequestMethod.GET})
public class BankAccntInfoControllerV2 {
    
	@Autowired
    private BankAccntInfoService bankAccntInfoService;
	
	@Autowired
    private OneMemberService oneMemberService;
	
	@Autowired
    private CommonUtil commonUtil;
	
	@Autowired(required=true)
	private HttpServletRequest request;
	
	@Value("${crepas.inside.url}")
    private String insideUrl;
	
	@ApiOperation(value = "계좌검증 및 저장")
    @RequestMapping("/bank/accnt/confirm")
    public ResponseEntity<ResponseResult> setBankAccntInfo(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
		JSONObject json = new JSONObject(requestString);

        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");

        Map<String, Object> result = new HashMap<String, Object>();
        JSONObject jsonRequest = json.getJSONObject("request");
        
        final String mid = jsonRequest.getString("mid");
        final String bankCode = jsonRequest.getString("bankCode");
        final String bankAccntNum = jsonRequest.getString("bankAccntNum");
        final String bankAccntName = jsonRequest.getString("bankAccntName");
        final String bankName =jsonRequest.getString("bankName");
		
        OneMemberCustId oneMemberCustId = oneMemberService.selectCustID2(mid);
        
        RestTemplate restTemplate = new RestTemplate();
        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("BANK_CD", bankCode);
        vars.put("ACCT_NB", bankAccntNum);
        
        String resultOne = restTemplate.postForObject(insideUrl + "/lookup/reciever", vars, String.class);
        
        JSONObject jsonResult = new JSONObject(resultOne);
        
        if(jsonResult != null && jsonResult.has("STATE") && jsonResult.getInt("STATE") == 200) {
        	if(jsonResult.getJSONObject("RESULT").getString("ACCT_OWNER_NM").equals(bankAccntName)
        			&& jsonResult.getJSONObject("RESULT").getString("ACCT_OWNER_NM").equals(oneMemberCustId.getName())) {
        		
        		MemberAccntInfo memberAccntInfo = new MemberAccntInfo();
            	memberAccntInfo.setMid(mid);
            	memberAccntInfo.setBankCode(bankCode);
            	memberAccntInfo.setBankAccntName(bankAccntName);
            	memberAccntInfo.setBankAccntNum(bankAccntNum);
            	memberAccntInfo.setBankName(bankName);
        		bankAccntInfoService.updateMemberBankInfo(memberAccntInfo);
        		
        		result.put("matchesName", true);
        		response.setState(200);
        		response.setMessage("정상적으로 처리하였습니다.");
        		response.setResult(result);
        	} else {
        		result.put("matchesName", false);
        		response.setState(303);
        		response.setMessage("계좌명 검증에 실패하였습니다.");
        	}
        	
        } else {
        	response.setState(jsonResult.getInt("STATE"));
    		response.setMessage(jsonResult.getString("MESSAGE"));
        }
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
	
	@ApiOperation(value = "예치금 잔액조회")
    @RequestMapping("/balance/invest")
    public ResponseEntity<ResponseResult> getBalanceInvest(@RequestBody String requestString) throws Exception {
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        final String mid = jsonMember.getJSONObject("request").getString("mid");
        
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("investBalance", bankAccntInfoService.selectBankAccntBalance(mid));
        response.setResult(result);
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
	
	@ApiOperation(value = "출금계좌 조회")
    @RequestMapping("/withdraw/accnt/get")
    public ResponseEntity<ResponseResult> getWithdrawBank(@RequestBody String requestString) throws Exception {
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        final String mid = jsonMember.getJSONObject("request").getString("mid");
        response.setResult(bankAccntInfoService.selectWithdrawBankInfo(mid));
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
}