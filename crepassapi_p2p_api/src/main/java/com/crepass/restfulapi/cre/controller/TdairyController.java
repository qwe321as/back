package com.crepass.restfulapi.cre.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import com.crepass.restfulapi.cre.service.TdairyService;

import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@RequestMapping(path = "/api", method = RequestMethod.POST)
public class TdairyController {
    
    @Autowired
    private TdairyService tdairyService;
    
    @Autowired
    private CommonUtil commonUtil;
    
    @Autowired(required=true)
	private HttpServletRequest request;
    
    @ApiOperation(value = "시간일기저장")
    @RequestMapping("/tdairy/save")
    public ResponseEntity<ResponseResult> tdairySave(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        tdairyService.insertTdairyById(json);

        final String mid = ((JSONObject) json.get("request")).get("mid").toString();
        final String qdate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("restTime", commonUtil.getRestTime());
        result.put("list", tdairyService.selectTdairyById(mid, qdate));
        
        response.setResult(result);
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "시간일기조회")
    @RequestMapping("/tdairy/retrieve")
    public ResponseEntity<ResponseResult> tdairyRetrieve(@RequestBody String requestString) throws Exception {
               
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
    
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        final String mid = ((JSONObject) json.get("request")).get("mid").toString();
        final String qdate = ((JSONObject) json.get("request")).get("qdate").toString();
        
        Map<String, Object> result = new HashMap<String, Object>();
        
        result.put("restTime", commonUtil.getRestTime());
        result.put("list", tdairyService.selectTdairyById(mid, qdate));
        
        response.setResult(result);
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "시간일기통계조회")
    @RequestMapping("/tdairy/statistics")
    public ResponseEntity<ResponseResult> tdairyStatistics(@RequestBody String requestString) throws Exception {
               
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
    
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        final String mid = ((JSONObject) json.get("request")).get("mid").toString();
        
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("list", tdairyService.selectTdairyStatisticsById(mid));
        
        response.setResult(result);
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
}
