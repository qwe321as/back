package com.crepass.restfulapi.one.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerMapping;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.one.domain.LenddoInterface;
import com.crepass.restfulapi.one.service.LenddoService;

import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@RequestMapping(path = "/api", method = RequestMethod.POST)
public class LenddoController {
    
    @Autowired
    private LenddoService lenddoService;
    
    @Autowired
    private CommonUtil commonUtil;
    
    @Autowired(required=true)
	private HttpServletRequest request;
    
//    @Value("${shinhan.webhook.url}")
//    private String shinhanUrl;
    
//    @ApiOperation(value = "lenddo전송결과저장")
//    @RequestMapping("/lenddo/add")
//    public ResponseEntity<ResponseResult> lenddoAdd(@RequestBody String requestString) throws Exception {
//    	
//    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
//        commonUtil.sendRequestLogging(mapping_url, requestString);
//    	
//        JSONObject jsonMember = new JSONObject(requestString);
//        
//        ResponseResult response = new ResponseResult();
//        response.setState(200);
//        response.setMessage("정상적으로 처리하였습니다.");
//        
//        LenddoInterface lenddoTrans = lenddoService.insertLenddoTransResult(jsonMember);
//        
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
//        String times = formatter.format(new Date());
//        if(lenddoTrans != null ) {
//         	RestTemplate restTemplate = new RestTemplate();
// 	        Map<String, String> vars = new HashMap<String, String>();
// 	        vars.put("app_id", lenddoTrans.getSendId());
// 	        vars.put("created_dt", times);
// 	        
// 	        String result = restTemplate.postForObject(shinhanUrl, vars, String.class);
// 	        
// 	        if(result != null && !result.isEmpty()) {
// 	        	try {
// 		        	JSONObject jsonObject = new JSONObject(result);
// 		        	if(jsonObject.has("status_code")) {
// 		        		if(jsonObject.getInt("status_code") != 1000)
// 		        			lenddoService.insertLenddoWebhoook(lenddoTrans.getSendId(), times);
// 		        	} else
// 		        		lenddoService.insertLenddoWebhoook(lenddoTrans.getSendId(), times);
// 	        	} catch(Exception e) {
// 	        		lenddoService.insertLenddoWebhoook(lenddoTrans.getSendId(), times);
// 	        	}
// 	        }
//        }
//        
//        response.setResult(lenddoTrans);
//        
//        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
//        
//        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
//    }
    
    @ApiOperation(value = "lenddo전송내역 조회")
    @RequestMapping("/lenddo/history/get")
    public ResponseEntity<ResponseResult> getLenddoById(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        JSONObject request = (JSONObject) jsonMember.get("request");
        
        String appId = request.getString("appId");
        
        Map<String, Object> result = new HashMap<String, Object>();
		result.put("appId", lenddoService.selectLenddoById(appId));
        
        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        response.setResult(result);
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "lenddo전송내역 저장")
    @RequestMapping("/lenddo/history/add")
    public ResponseEntity<ResponseResult> addLenddoHistory(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        JSONObject request = (JSONObject) jsonMember.get("request");
        
        String appId = request.getString("appId");
        String mid = request.getString("mid");
        
        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        if(!lenddoService.insertLenddoSendHistory(appId, mid)) {
        	response.setState(509);
            response.setMessage("전송내역 저장에 실패하였습니다.");
        }
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
}
