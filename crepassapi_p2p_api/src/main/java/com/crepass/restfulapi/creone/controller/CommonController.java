package com.crepass.restfulapi.creone.controller;

import org.springframework.web.bind.annotation.RestController;

import com.crepass.restfulapi.common.domain.ResponseResult;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

@CrossOrigin
@RestController
@RequestMapping(path = "/common")
public class CommonController {
    
    @RequestMapping("/healthcheck")
    public ResponseEntity<ResponseResult> check() throws Exception {
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        response.setResult("");
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
}