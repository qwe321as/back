package com.crepass.restfulapi.cre.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.cre.service.SocialService;
import com.crepass.restfulapi.cre.service.UniverService;

import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@RequestMapping(path = "/api", method = RequestMethod.POST)
public class UniverController {
    
    @Autowired
    private UniverService univerService;
    
    @Autowired
    private SocialService socialService;
    
    @Autowired
    private CommonUtil commonUtil;
    
    @Autowired(required=true)
	private HttpServletRequest request;
    
    @ApiOperation(value = "학교목록")
    @RequestMapping("/univer/list")
    public ResponseEntity<ResponseResult> getUniverList(@RequestBody String requestString) throws Exception {

    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("list", univerService.selectUniverList());
        result.put("social", socialService.selectSocialList());
        
        response.setResult(result);
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "학교과목록")
    @RequestMapping("/univer/major")
    public ResponseEntity<ResponseResult> getUniverMajor(@RequestBody String requestString) throws Exception {
        
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        final String schoolName = ((JSONObject) json.get("request")).get("schoolName").toString();
        
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("list", univerService.selectUniverMajor(schoolName));
        
        response.setResult(result);
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }

}
