package com.crepass.restfulapi.one.controller;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.one.service.BoardService;

import io.swagger.annotations.ApiOperation;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@CrossOrigin
@RestController
@RequestMapping(path = "/api", method = RequestMethod.POST)
public class BoardController {
    
    @Autowired
    private BoardService boardService;
    
    @Autowired
    private CommonUtil commonUtil;
    
    @Autowired(required=true)
	private HttpServletRequest request;
    
    
    
    @ApiOperation(value = "게시판별 조회 리스트")
    @RequestMapping("/{table}/list")
    public ResponseEntity<ResponseResult> getBoards(@PathVariable String table) throws Exception {
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, table);
    	
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        Map<String, Object> result = new HashMap<String, Object>();
        
        if (table.equals("qna")) {
            result.put("list", boardService.selectQnaByAll());
        } else {
            result.put("list", boardService.selectBoardByAll(table));
        }
        
        response.setResult(result);
        
//        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);   
    }

    @ApiOperation(value = "게시판별 상세 조회")
    @RequestMapping("/{table}/{id}")
    public ResponseEntity<ResponseResult> retrieveBoard(@PathVariable String table, @PathVariable long id) throws Exception {
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        response.setResult(boardService.selectBoardById(table, Long.toString(id)));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);        
    }

}