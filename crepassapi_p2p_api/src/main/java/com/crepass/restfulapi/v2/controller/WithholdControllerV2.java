package com.crepass.restfulapi.v2.controller;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.cre.service.CreMemberService;
import com.crepass.restfulapi.one.service.OneMemberService;
import com.crepass.restfulapi.one.service.WithholdService;
import com.crepass.restfulapi.v2.domain.Agreement;
import com.google.gson.Gson;

import io.swagger.annotations.ApiOperation;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@CrossOrigin
@RestController
@RequestMapping(path = "/api2", method = {RequestMethod.POST, RequestMethod.GET})
public class WithholdControllerV2 {
    
    @Autowired
    private WithholdService withholdService;
    
    @Autowired
    private OneMemberService oneMemberService;
    
    @Autowired
	private CreMemberService creMemberService;
    
    @Autowired
    private CommonUtil commonUtil;
	
	@Autowired(required=true)
	private HttpServletRequest request;
    
    @ApiOperation(value = "원천징수정보저장")
    @RequestMapping("/withholding/set")
    public ResponseEntity<ResponseResult> setWithholdingInfo(@RequestBody String requestString) throws Exception {
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        if(json.has("request") && json.getJSONObject("request").has("mid") && json.getJSONObject("request").has("reginum")) {
        	String mid = json.getJSONObject("request").getString("mid");
        	String reginum = json.getJSONObject("request").getString("reginum");
        	Agreement agreement = new Gson().fromJson(json.getJSONObject("request").getJSONObject("agreement").toString(), Agreement.class);
        	
        	if(withholdService.updateWithholdingInfo2(mid, reginum)) {
        		String custId = oneMemberService.selectCustID(mid);
                creMemberService.addCreAgreeMember3(agreement, custId);
        	}
        } else {
        	response.setState(332);
        	response.setMessage("Parameter Not Found");
        }
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);   
    }
}