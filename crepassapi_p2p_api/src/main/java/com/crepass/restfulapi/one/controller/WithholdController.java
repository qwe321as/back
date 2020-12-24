package com.crepass.restfulapi.one.controller;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.one.domain.OneWithhold;
import com.crepass.restfulapi.one.domain.OneWithholdAccount;
import com.crepass.restfulapi.one.service.VirtualAccntService;
import com.crepass.restfulapi.one.service.WithholdService;

import io.swagger.annotations.ApiOperation;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@CrossOrigin
@RestController
@RequestMapping(path = "/api", method = RequestMethod.POST)
public class WithholdController {
    
    @Autowired
    private WithholdService withholdService;
    
    @Autowired
    private CommonUtil commonUtil;
    
    @Autowired
    private VirtualAccntService virtualAccntService;
    
    @Autowired(required=true)
	private HttpServletRequest request;
    
    @Value("${crepas.inside.url}")
    private String insideUrl;
    
    @ApiOperation(value = "원천징수정보조회")
    @RequestMapping("/withholding")
    public ResponseEntity<ResponseResult> getWithholdList(@RequestBody String requestString) throws Exception {
        
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        final String mid = ((JSONObject) json.get("request")).get("mid").toString();
        
        Map<String, Object> result = new HashMap<String, Object>();
        
        OneWithhold oneWithhold = new OneWithhold();
        result.put("address", oneWithhold);
        oneWithhold = withholdService.selectWithholdById(mid);
        if (oneWithhold != null) {
            result.put("address", oneWithhold);
        }
        
        OneWithholdAccount oneWithAccount = new OneWithholdAccount();
        result.put("account", oneWithAccount);
        oneWithAccount = withholdService.selectAccountById(mid);
        if (oneWithAccount != null) {
            oneWithAccount.setMyBankcodeName(virtualAccntService.selectBankById(oneWithAccount.getMyBankcode()));
            result.put("account", oneWithAccount);
        }

        response.setResult(result);
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);   
    }

    @ApiOperation(value = "출금계좌등록")
    @RequestMapping("/add/withdraw")
    public ResponseEntity<ResponseResult> addWithdraw(@RequestBody String requestString) throws Exception {
        
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        JSONObject request = (JSONObject) json.get("request");
        
        ResponseResult response = new ResponseResult();
        
        final String mid = request.getString("mid");
        final String bankcode = request.getString("bankcode");
        final String bankname = request.getString("bankname");
        final String bankacc = request.getString("bankacc");
        
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("BANK_CD", bankcode);
        vars.put("ACCT_NB", bankacc);
        
        String result = restTemplate.postForObject(insideUrl + "/lookup/reciever", vars, String.class);
        JSONObject jsonResult = new JSONObject(result);
        
        if(jsonResult.getInt("STATE") == 200) {
        	int withdrawAccnt = withholdService.updateWithdrawAccnt(mid, bankcode, bankname, bankacc);
        	
        	if(withdrawAccnt > 0) {
        		response.setState(200);
        		response.setMessage("정상적으로 처리하였습니다.");
        	} else {
        		response.setState(311);
        		response.setMessage("계좌등록에 실패하였습니다.");
        	}
        } else {
        	response.setState(310);
            response.setMessage("계좌 검증 오류입니다. 계좌를 바르게 입력하세요.");
        }
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);   
    }
    
    @ApiOperation(value = "원천징수정보저장")
    @RequestMapping("/withholding/set")
    public ResponseEntity<ResponseResult> setWithholdingInfo(@RequestBody String requestString) throws Exception {
        
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        final JSONObject request = json.getJSONObject("request");
        final String mid = request.getString("m_id");
        final String reginum = request.getString("reginum");
        final String m_with_zip = request.getString("m_with_zip");
        final String m_with_addr1 = request.getString("m_with_addr1");
        final String m_with_addr2 = request.getString("m_with_addr2");
        
        if(mid.isEmpty() || reginum.isEmpty() || m_with_zip.isEmpty() || m_with_addr1.isEmpty() || m_with_addr2.isEmpty()) {
        	response.setState(332);
            response.setMessage("누락된 입력값이 있습니다.");
        } else {
	        if(!withholdService.updateWithholdingInfo(mid, reginum, m_with_zip, m_with_addr1, m_with_addr2)) {
	        	response.setState(331);
	            response.setMessage("원천징수정보 등록에 실패하였습니다.");
	        }
        }
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);   
    }
}