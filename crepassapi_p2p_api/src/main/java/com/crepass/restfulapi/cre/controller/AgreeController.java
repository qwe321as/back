package com.crepass.restfulapi.cre.controller;

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
import com.crepass.restfulapi.cre.domain.CreAgree;
import com.crepass.restfulapi.cre.domain.CreDocument;
import com.crepass.restfulapi.cre.domain.CreSetting;
import com.crepass.restfulapi.cre.service.AgreeService;
import com.crepass.restfulapi.one.domain.MariMember;
import com.crepass.restfulapi.one.domain.OneCertify;
import com.crepass.restfulapi.one.service.OneMemberService;

import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@RequestMapping(path = "/api", method = RequestMethod.POST)
public class AgreeController {
    
	@Autowired
    private OneMemberService oneMemberService;
	
    @Autowired
    private AgreeService agreeService;
    
    @Autowired
    private CommonUtil commonUtil;
    
    @Autowired(required=true)
	private HttpServletRequest request;
    
//		작업도중 동의정보저장방식이 변경되어 crep2p_agreed_history테이블로 대체됨(crep2p_agreed 테이블을 사용하지 않음)   
//    
//    @ApiOperation(value = "동의정보저장")
//    @RequestMapping("/agree/add")
//    public ResponseEntity<ResponseResult> agreeAdd(@RequestBody String requestString) throws Exception {
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
//        CreAgree creAgree = agreeService.insertCreAgree(jsonMember);
//        
//        response.setResult(creAgree);
//        
//        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
//        
//        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
//    }
    
    @ApiOperation(value = "설정정보저장")
    @RequestMapping("/setting/add")
    public ResponseEntity<ResponseResult> settingAdd(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        CreSetting creSetting = agreeService.updateCreSetting(json);
        
        response.setResult(creSetting);
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "약관등록")
    @RequestMapping("/agreed/document/add")
    public ResponseEntity<ResponseResult> documentAdd(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        CreDocument creDocument = agreeService.insertDocument(json);
        
        response.setResult(creDocument);
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "약관조회")
    @RequestMapping("/agreed/document")
    public ResponseEntity<ResponseResult> documentInfo(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        CreDocument creDocument = agreeService.selectDocumentById(json);
        
        response.setResult(creDocument);
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "대출신청동의정보저장")
    @RequestMapping("/loan/agreed/add")
    public ResponseEntity<ResponseResult> loanAgreeAdd(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        agreeService.insertCreLoanAgreed(json);
        
        JSONObject request = (JSONObject) json.get("request");
        String mid = request.getString("mid");
        
        MariMember mariMember = oneMemberService.selectMemberById(mid);
        OneCertify oneCertify = new OneCertify();
        oneCertify.setMno(mariMember.getMno());
        oneCertify.setCertifyType(request.getString("certifyType"));
        oneCertify.setCertifyResult(request.getString("certifyResult").toString());
        oneMemberService.insertOneCertify(oneCertify);
        
        response.setResult("");
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "투자신청동의정보저장")
    @RequestMapping("/invest/agreed/add")
    public ResponseEntity<ResponseResult> investAgreeAdd(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        agreeService.insertCreInvestAgreed(json);
        
        response.setResult("");
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
}
