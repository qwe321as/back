package com.crepass.restfulapi.one.controller;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.one.domain.OneWish;
import com.crepass.restfulapi.one.service.WishService;

import io.swagger.annotations.ApiOperation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class WishController {
    
    @Autowired
    private WishService wishService;
    
    @Autowired
    private CommonUtil commonUtil;
    
    @Autowired(required=true)
	private HttpServletRequest request;
    
    @ApiOperation(value = "관심투자목록저장")
    @RequestMapping("/invest/interest/save")
    public ResponseEntity<ResponseResult> interestSave(@RequestBody String requestString) throws Exception {
        
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        wishService.insertWishById(json);
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
//        response.setResult(json.get("request").toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);   
    }
    
    @ApiOperation(value = "관심투자목록삭제")
    @RequestMapping("/invest/interest/del")
    public ResponseEntity<ResponseResult> interestDel(@RequestBody String requestString) throws Exception {
        
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        wishService.deleteWishById(json);
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
//        response.setResult(json.get("request"));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "관심투자목록조회")
    @RequestMapping("/invest/interest/list")
    public ResponseEntity<ResponseResult> getInterestList(@RequestBody String requestString) throws Exception {
        
        JSONObject json = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        final String mid = ((JSONObject) json.get("request")).get("mid").toString();
        
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("list", wishService.selectWishById(mid));
        
		List<OneWish> oneWish = (List<OneWish>) wishService.selectWishById(mid);
    	
    	for(int i = 0; i < oneWish.size(); i++) {
    		if(oneWish.get(i).getInvestCredit() == null || oneWish.get(i).getInvestCredit().equals("null") || oneWish.get(i).getInvestCredit().isEmpty())
    			oneWish.get(i).setInvestCredit("E");
    	}
    	
        result.put("list", oneWish);
        
        
        response.setResult(result);
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);   
    }

}