package com.crepass.restfulapi.one.controller;

import com.crepass.restfulapi.common.AES256Cipher;
import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.one.domain.OneCertifyWebDump;
import com.crepass.restfulapi.one.service.OneMemberService;

import io.swagger.annotations.ApiOperation;

import java.util.HashMap;

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
@RequestMapping(path = "/api", method = RequestMethod.POST)
public class AESController {
    
	@Autowired
    private OneMemberService oneMemberService;
	
	@Autowired
    private CommonUtil commonUtil;
    
    @Autowired(required=true)
	private HttpServletRequest request;
	
	@Value("${crepas.aes256.eq}")
    private String crePassEq;
	
    @ApiOperation(value = "AES256 암호화")
    @RequestMapping("/aes/enc")
    public ResponseEntity<ResponseResult> setAESEnc(@RequestBody String requestString) throws Exception {
        
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        JSONObject request = json.getJSONObject("request");
        String aesKey = request.getString("aesKey");
        String aesValue = request.getString("aesValue");
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        HashMap<String, Object> result = new HashMap<String, Object>();
        
        if(crePassEq.equals(aesKey)) {
	        AES256Cipher aes256Cipher = AES256Cipher.getInstance();
	        result.put("aes", aes256Cipher.AES_Encode(aesValue));
	        response.setResult(result);
        } else {
        	response.setState(333);
            response.setMessage("암호화 키가 잘 못 되었습니다.");
        }
        
        // 디버깅시 에러
//        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);   
    }
    
    @ApiOperation(value = "AES256 복호화")
    @RequestMapping("/aes/des")
    public ResponseEntity<ResponseResult> setAESDes(@RequestBody String requestString) throws Exception {
        
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        JSONObject request = json.getJSONObject("request");
        String aesKey = request.getString("aesKey");
        String aesValue = request.getString("aesValue");
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        HashMap<String, Object> result = new HashMap<String, Object>();
        
        if(crePassEq.equals(aesKey)) {
	        AES256Cipher aes256Cipher = AES256Cipher.getInstance();
	        result.put("aes", aes256Cipher.AES_Decode(aesValue));
	        response.setResult(result);
        } else {
        	response.setState(333);
            response.setMessage("암호화 키가 잘 못 되었습니다.");
        }
        
        // 디버깅시 에러
//        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);   
    }

    @ApiOperation(value = "본인인증결과 조회")
    @RequestMapping("/aes/certify")
    public ResponseEntity<ResponseResult> getCetifyResult(@RequestBody String requestString) throws Exception {
        
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        JSONObject request = json.getJSONObject("request");
        String bi = request.getString("bi");
        
        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        OneCertifyWebDump oneCertifyWebDump =  oneMemberService.selectCertifyWebDump(bi);
        
        if(oneCertifyWebDump != null) {
	        HashMap<String, Object> result = new HashMap<String, Object>();
	        result.put("certifyResult", oneCertifyWebDump.getCertifyResult());
	        response.setResult(result);
	        
	        oneMemberService.deleteCertifyWebDump(bi);
        } else {
        	response.setState(334);
            response.setMessage("조회내역이 없습니다.");
        }
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);   
    }
}