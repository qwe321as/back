package com.crepass.restfulapi.v2.controller;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.one.service.WishService;
import com.crepass.restfulapi.v2.domain.LoansVO;
import com.crepass.restfulapi.v2.domain.LoansVO2;
import com.google.gson.Gson;

import io.swagger.annotations.ApiOperation;

import java.util.ArrayList;
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
@RequestMapping(path = "/api2", method = {RequestMethod.POST, RequestMethod.GET})
public class WishControllerV2 {
    
    @Autowired
    private WishService wishService;
    
    @Autowired
    private CommonUtil commonUtil;
	
	@Autowired(required=true)
	private HttpServletRequest request;
    
    @Value("${paging.offset}")
	private int rowCount;
    
    @ApiOperation(value = "관심투자목록조회")
    @RequestMapping("/invest/interest/list")
    public ResponseEntity<ResponseResult> getInterestList(@RequestBody String requestString) throws Exception {
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
    	JSONObject json = new JSONObject(requestString);
		JSONObject jsonRequest = json.getJSONObject("request");
		
		int pageNum = jsonRequest.getInt("pageNum");
		
		String mid = "";
		
		if(jsonRequest.has("mid")) {
			mid = jsonRequest.getString("mid");
		}
		
		int offSetNum = (pageNum - 1) * rowCount;
		
		List<LoansVO> loanList = wishService.selectWishById2(offSetNum, rowCount, mid);
		
		if(loanList == null)
			loanList = new ArrayList<LoansVO>();
		
		int totCount = wishService.selectWishByIdSize(mid);
		int totPageCount = (int) Math.ceil((float)totCount/rowCount);
		
		ResponseResult response = new ResponseResult();
		Map<String,Object> result = new HashMap<String,Object>();
		result.put("list", loanList);
		result.put("totPageCount", totPageCount);
		
		response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        response.setResult(result);
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);   
    }

    

    @ApiOperation(value = "관심투자목록조회")
    @RequestMapping("/invest/interest/list2")
    public ResponseEntity<ResponseResult> getInterestList2(@RequestBody String requestString) throws Exception {
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
    	JSONObject json = new JSONObject(requestString);
		JSONObject jsonRequest = json.getJSONObject("request");
		
		int pageNum = jsonRequest.getInt("pageNum");
		
		String mid = "";
		
		if(jsonRequest.has("mid")) {
			mid = jsonRequest.getString("mid");
		}
		
		int offSetNum = (pageNum - 1) * rowCount;
		
		List<LoansVO2> loanList = wishService.selectWishById2_1(offSetNum, rowCount, mid);
		
		if(loanList == null)
			loanList = new ArrayList<LoansVO2>();
		
		int totCount = wishService.selectWishByIdSize(mid);
		int totPageCount = (int) Math.ceil((float)totCount/rowCount);
		
		ResponseResult response = new ResponseResult();
		Map<String,Object> result = new HashMap<String,Object>();
		result.put("list", loanList);
		result.put("totPageCount", totPageCount);
		
		response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        response.setResult(result);
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);   
    }
}