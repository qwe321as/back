package com.crepass.restfulapi.v2.controller;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.v2.domain.OneEventItem;
import com.crepass.restfulapi.one.service.EventService;

import io.swagger.annotations.ApiOperation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@RequestMapping(path = "/api2", method = RequestMethod.POST)
public class EventControllerV2 {

    @Autowired
    private EventService eventService;
    
    @Autowired
    private CommonUtil commonUtil;
    
    @Autowired(required=true)
	private HttpServletRequest request;
    
	@Value("${paging.offset}")
	private int rowCount;
	
    @ApiOperation(value = "이벤트전체목록조회")
    @RequestMapping("/event/list")
    public ResponseEntity<ResponseResult> getEventList(@RequestBody String requestString) throws Exception {

    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, "");
    	
    	JSONObject json = new JSONObject(requestString);
		JSONObject jsonRequest = json.getJSONObject("request");
		
		int pageNum = 0;
		if(jsonRequest.has("pageNum")) 
			pageNum = jsonRequest.getInt("pageNum");
	
		
		int offSetNum = (pageNum - 1) * rowCount;
		int totCount = eventService.selectEventListCount();
		int totPageCount = (int) Math.ceil((float)totCount/rowCount);
		
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        List<OneEventItem> eventListV2 = eventService.selectEventListV2();
        
        Map<String,Object> result = new HashMap<String,Object>();
		result.put("list", eventListV2);
        
        response.setResult(result);
//        response.setResult(eventService.selectEventListV2(offSetNum, rowCount));
        
//        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());

        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
    
    @ApiOperation(value = "이벤트 한건 세부목록")
    @RequestMapping("/event/detail")
    public ResponseEntity<ResponseResult> getEventDetail(@RequestBody String requestString) throws Exception {

    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, "");
    	
    	JSONObject json = new JSONObject(requestString);

        final String event_code = ((JSONObject) json.get("request")).get("event_code").toString();
        
        String mType = null;
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
//        if( ((JSONObject) json.get("request")).has("mType") ) {
//        	mType = ((JSONObject)json.get("request")).get("mType").toString();
//        	if(mType.equals("L")) mType="대출자";
//        	else if(mType.equals("I")) mType="투자자";
//        	
//        	response.setResult(eventService.selectEventDetailV2(event_code, mType));
//        } else
        	response.setResult(eventService.selectEventDetailV2(event_code));
        
//        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());

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
        
        // 디버깅시 에러
        // commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());

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