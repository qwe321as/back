package com.crepass.restfulapi.one.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerMapping;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.one.domain.OneMemberCustId;
import com.crepass.restfulapi.one.service.BankAccntInfoService;
import com.crepass.restfulapi.one.service.OneMemberService;

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
@RequestMapping(path = "/api", method = RequestMethod.POST)
public class BankAccntInfoController {
    
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
	
	@ApiOperation(value = "은행리스트")
    @RequestMapping("/bank/accnt/list")
    public ResponseEntity<ResponseResult> setBankAccntInfo() throws Exception {
		
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, "");
		
        ResponseResult response = new ResponseResult();
        
        Map<String, Object> result = new HashMap<String, Object>();
		result.put("bankList", bankAccntInfoService.selectBankAccntInfo());
		
		response.setState(200);
		response.setMessage("정상적으로 처리하였습니다.");
		response.setResult(result);
		
		commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
	
	@ApiOperation(value = "계좌검증")
    @RequestMapping("/bank/accnt/confirm")
    public ResponseEntity<ResponseResult> setBankAccntInfo(@RequestBody String requestString) throws Exception {
		
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
		JSONObject json = new JSONObject(requestString);

        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");

        Map<String, Object> result = new HashMap<String, Object>();
        JSONObject jsonRequest = (JSONObject) json.get("request");
        
        final String mid = jsonRequest.get("mid").toString();
        final String bankCode = jsonRequest.get("bankCode").toString();
        final String bankAccnt = jsonRequest.get("bankAccnt").toString();
        final String accntName = jsonRequest.get("accntName").toString();
		
        OneMemberCustId oneMemberCustId = oneMemberService.selectCustID2(mid);
        
        RestTemplate restTemplate = new RestTemplate();
        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("BANK_CD", bankCode);
        vars.put("ACCT_NB", bankAccnt);
        
        String resultOne = restTemplate.postForObject(insideUrl + "/lookup/reciever", vars, String.class);
        
        JSONObject jsonResult = new JSONObject(resultOne);
        
        if(jsonResult.getInt("STATE") == 200) {
        	if(jsonResult.getJSONObject("RESULT").getString("ACCT_OWNER_NM").equals(accntName)
        			&& jsonResult.getJSONObject("RESULT").getString("ACCT_OWNER_NM").equals(oneMemberCustId.getName())) {
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
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
}