package com.crepass.restfulapi.one.controller;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.one.service.EventService;

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
@RequestMapping(path = "/api", method = RequestMethod.POST)
public class EventController {

    @Autowired
    private EventService eventService;
    
    @Autowired
    private CommonUtil commonUtil;
    
    @Autowired(required=true)
	private HttpServletRequest request;
    
    @ApiOperation(value = "이벤트내역조회")
    @RequestMapping("/event/list")
    public ResponseEntity<ResponseResult> getEventList() throws Exception {

    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, "");
    	
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        response.setResult(eventService.selectEventList());
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());

        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
    
    @ApiOperation(value = "이벤트참여조회")
    @RequestMapping("/event/join")
    public ResponseEntity<ResponseResult> getEventJoin(@RequestBody String requestString) throws Exception {

    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);

        final String mid = ((JSONObject) json.get("request")).get("mid").toString();
        
        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        response.setResult(eventService.selectEventJoin(mid));
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());

        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
    
    @ApiOperation(value = "이벤트참여")
    @RequestMapping("/event/add")
    public ResponseEntity<ResponseResult> getEventAdd(@RequestBody String requestString) throws Exception {

    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);

        final String mid = ((JSONObject) json.get("request")).get("mid").toString();
        final String eventCode = ((JSONObject) json.get("request")).get("eventCode").toString();
        
        ResponseResult response = new ResponseResult();
        
        String eventIsAddById = eventService.selectEventIsAddById(mid, eventCode);
        
        if(eventIsAddById != null) {
        	response.setState(702);
            response.setMessage("이미 적용된 쿠폰입니다.");
        } else {
	        eventService.updateEventJoinState(mid);
	        
	    	if(eventService.insertEventJoinAdd(mid, eventCode)) {
	    		response.setState(200);
	            response.setMessage("정상적으로 처리하였습니다.");
	            response.setResult(eventService.selectEventJoin(mid));
	    	} else {
	    		response.setState(701);
	            response.setMessage("이벤트 추가에 실패하였습니다.");
	    	}
        }
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());

        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
    
}