package com.crepass.restfulapi.v2.controller;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.cre.domain.CreIntro;
import com.crepass.restfulapi.cre.service.IntroService;

import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@RequestMapping(path = "/api2", method = {RequestMethod.POST, RequestMethod.GET})
public class IntroControllerV2 {
    
    @Autowired
    private IntroService introService;

    @Autowired(required=true)
	private HttpServletRequest request;
    
    @Autowired
    private CommonUtil commonUtil;
    
    @ApiOperation(value = "Intro 조회")
    @RequestMapping("/intro")
    public ResponseEntity<ResponseResult> introInfo(@RequestBody String requestString, @RequestHeader String apkVersion) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    	commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        // null처리 자체가 안되서 count사용해서 null처리 대체
        int introCount = introService.selectIntroInfoCount(apkVersion);
        if(introCount == 0) {
        	boolean liveFlag = getType(apkVersion);
        	if(liveFlag)
        		introService.insertIntroAppV2(apkVersion,"Y");
        	else 
        		introService.insertIntroAppV2(apkVersion,"N");
        		
        }
        CreIntro creIntro = introService.selectIntroInfoV2(apkVersion);
        creIntro.setNewAppVersion(introService.selectNewAppVersion());
        
        response.setResult(creIntro);
        
        // 디버깅시 에러
//        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "App정보 등록")
    @RequestMapping("/intro/add")
    public ResponseEntity<ResponseResult> introAdd(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        int result = introService.insertIntroApp(json);
        if (result == 0) {
            response.setState(201);
            response.setMessage("파라미터 오류");
        }
        response.setResult(json.get("request").toString());
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    private boolean getType(String apkVersion) {
		return Pattern.matches("^[0-9.]*$", apkVersion);
	}
}
