package com.crepass.restfulapi.v2.controller;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.one.domain.OneEmoneyBank;
import com.crepass.restfulapi.one.domain.OneEmoneyInvestPay;
import com.crepass.restfulapi.one.domain.OneInvest;
import com.crepass.restfulapi.one.domain.OneOutPay;
import com.crepass.restfulapi.one.domain.OneVirtualRealAccnt;
import com.crepass.restfulapi.one.service.EmoneyService;
import com.crepass.restfulapi.one.service.OneMemberService;
import com.crepass.restfulapi.one.service.VirtualAccntService;

import io.swagger.annotations.ApiOperation;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@CrossOrigin
@RestController
@RequestMapping(path = "/api2", method = {RequestMethod.POST, RequestMethod.GET})
public class EmoneyControllerV2 {

    @Autowired
    private EmoneyService emoneyService;
    
    @Autowired
    private OneMemberService oneMemberService;
    
    @Autowired
    private CommonUtil commonUtil;
    
    @Value("${crepas.inside.url}")
    private String insideUrl;
    
    @Autowired
    private VirtualAccntService virtualAccntService;

    @Autowired(required=true)
	private HttpServletRequest request;
    
    @ApiOperation(value = "예치금관리")
    @RequestMapping("/emoney/retreive")
    public ResponseEntity<ResponseResult> getEmoneyRetreive(@RequestBody String requestString) throws Exception {
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);

        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");

        final String mid = ((JSONObject) json.get("request")).get("mid").toString();
        
        String investPayment = emoneyService.selectEmoneyInvestIsPlaying(mid);				// 투자중인 금액
        String withdrawPay = emoneyService.selectEmoneyWithdrawPay2(mid);					// 투자자가 받을 금액 
        
        if(investPayment == null)
        	investPayment = "0";
        
        OneEmoneyInvestPay oneEmoneyInvestPay = emoneyService.selectEmoneyPayInfo(mid);
        
        if(oneEmoneyInvestPay != null) {
	        long setEmoney = 0;
	        
	        String emoneyInvestWithdrawPay = emoneyService.selectEmoneyInvestWithdrawPay(mid);		//
	        if(emoneyInvestWithdrawPay == null)
	        	emoneyInvestWithdrawPay = "0";
	        
	        String emoneyBalance = emoneyService.selectEmoneyInvestBalance(mid);
	        
	        if(oneEmoneyInvestPay != null) {
	        	setEmoney = Long.parseLong(emoneyBalance) - Long.parseLong(oneEmoneyInvestPay.getIpay()) - Long.parseLong(withdrawPay);
	        	emoneyService.updateEmoney(String.valueOf(setEmoney), mid);
	        }
	        
	        long emoneyInvestWithdrawPayReal = setEmoney - (long)Double.parseDouble(emoneyInvestWithdrawPay);
	        
	        if(emoneyInvestWithdrawPayReal < 0)
	        	emoneyInvestWithdrawPayReal = 0;
	        
            OneEmoneyBank oneEmoneyBank = null;
            oneEmoneyBank = emoneyService.selectEmoneyRetreive(mid);
            if (oneEmoneyBank != null) {
            	oneEmoneyBank.setWithdrawRealPay(String.valueOf(emoneyInvestWithdrawPayReal));		// 출금가능금액
            	oneEmoneyBank.setMyBankNameVb(virtualAccntService.selectBankById(oneEmoneyBank.getMyBankCodeVb()));
                oneEmoneyBank.setMyBankName(oneEmoneyBank.getMyBankName());
                oneEmoneyBank.setMyBankCode(oneEmoneyBank.getMyBankCode());
            } else {
            	oneEmoneyBank = new OneEmoneyBank();
            	oneEmoneyBank.setWithdrawRealPay(String.valueOf(emoneyInvestWithdrawPayReal));
            	oneEmoneyBank.setMyName("");
                oneEmoneyBank.setMyBankacc("");
            }
            
            response.setResult(oneEmoneyBank);
        }  else {
        	response.setState(304);
            response.setMessage("고객ID가 존재하지 않습니다. 고객센터에 문의하세요.");
        }
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
    
    @ApiOperation(value = "출금신청 (예치금) - 앱")
    @RequestMapping("/virtualaccnt/payWithdraw")
	@Transactional("oneTransactionManager")
    public ResponseEntity<ResponseResult> sendPayWithdraw(@RequestBody String requestString) throws Exception {
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
		JSONObject json = new JSONObject(requestString);
		JSONObject request = (JSONObject) json.get("request");
        
        final String mid = request.getString("mid");
        final String o_pay = request.getString("o_pay");
        final String password = request.getString("password");
        
        ResponseResult response = new ResponseResult();
        
        String UserConfirm = oneMemberService.selectUserConfirm(mid, password);
        
        if(UserConfirm != null && !UserConfirm.isEmpty()) {
	        OneVirtualRealAccnt oneVirtualRealAccnt = virtualAccntService.selectAccountById(mid);
	        
	        if(oneVirtualRealAccnt != null && oneVirtualRealAccnt.getMyBankacc() != null && oneVirtualRealAccnt.getMyBankcode() != null
	        		&& !oneVirtualRealAccnt.getMyBankacc().isEmpty() && !oneVirtualRealAccnt.getMyBankcode().isEmpty()) {
		        RestTemplate restTemplate = new RestTemplate();
		        Map<String, String> vars = new HashMap<String, String>();
		        vars.put("BANK_CD", oneVirtualRealAccnt.getMyBankcode());
		        vars.put("ACCT_NB", oneVirtualRealAccnt.getMyBankacc());
		        
		        String resultAccnt = restTemplate.postForObject(insideUrl + "/lookup/reciever", vars, String.class);
		        JSONObject jsonAccnt = new JSONObject(resultAccnt);
		        
		        if(jsonAccnt.getInt("STATE") == 200) {
		        	if(Long.parseLong(o_pay) <= Long.parseLong(oneVirtualRealAccnt.getEmoney())) {
		        		
		        		int ranNum = (int)(Math.random() * (999 - 111 + 1)) + 111;
						long time = System.currentTimeMillis();
						String gCode = "P" + String.valueOf(time) + String.valueOf(ranNum);
		        		
		        		OneInvest oneInvest = new OneInvest();
		        		oneInvest.setGCode(gCode);
		        		oneInvest.setMid(mid);
		        		oneInvest.setMName(oneVirtualRealAccnt.getMname());
		        		oneInvest.setSubject("잔액 출금신청");
		        		oneInvest.setLoanId(String.valueOf(0));
		        		oneInvest.setIPay(o_pay);
		        		
		        		OneOutPay oneOutPay = new OneOutPay();
		        		oneOutPay.setGCode(gCode);
		        		oneOutPay.setMid(mid);
		        		oneOutPay.setMName(oneVirtualRealAccnt.getMname());
		        		oneOutPay.setPay(o_pay);
		        		oneOutPay.setIp(commonUtil.getRemoteAddrs());
		        		
		        		int accntWithdraw = virtualAccntService.insertAccntWithdraw(oneInvest);
		        		int accntWithdrawHistory = virtualAccntService.insertAccntWithdrawHistory(oneOutPay);
		        		
		        		if(accntWithdraw > 0 && accntWithdrawHistory > 0) {
		    				if(Long.parseLong(oneVirtualRealAccnt.getEmoney()) < Long.parseLong(o_pay)) {
		    					response.setState(314);
		        				response.setMessage("예치금보다 많은 금액을 출금신청 할 수 없습니다.");
		    				} else {
		    					if(!virtualAccntService.insertAccntWithdrawSchedule(mid, o_pay, "I")) {
		    	        			response.setState(322);
		    	                    response.setMessage("출금 처리중 에러가 발생했습니다.");
		    	                    return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
		    	        		}
		    					
		    					String investPayment = emoneyService.selectEmoneyInvestIsPlaying(mid);
		    			        String withdrawPay = emoneyService.selectEmoneyWithdrawPay2(mid);
		    			        
		    			        if(investPayment == null)
		    			        	investPayment = "0";
		    			        
		//    			        restTemplate = new RestTemplate();
		    			        vars = new HashMap<String, String>();
		    			        vars.put("CUST_ID", oneVirtualRealAccnt.getCustId());
		    			        
		    			        String resultAMT = restTemplate.postForObject(insideUrl + "/lookup/customer", vars, String.class);
		    			        JSONObject jsonResult = new JSONObject(resultAMT);
		    		        
		    			        int STATE = jsonResult.getInt("STATE");
		    			        int isUserMoney = 1;
		    			        long payment = 0;
		    			        
		    			        if(STATE == 200) {
		    			        	String balanceAMT = ((JSONObject)jsonResult.get("RESULT")).getString("BALANCE_AMT");
		    			        	payment = Long.parseLong(balanceAMT) - Long.parseLong(investPayment) - Long.parseLong(withdrawPay);
		    			        	isUserMoney = virtualAccntService.updateUserMoney(mid, String.valueOf(payment));
		    			        } else
		    			        	payment = (long)Long.parseLong(oneVirtualRealAccnt.getEmoney());
		    			        
		    					if(isUserMoney > 0) {
		            				Map<String, Object> result = new HashMap<String, Object>();
		                    		result.put("payment", String.valueOf(payment));
		    						
		        	        		response.setState(200);
		            				response.setMessage("정상적으로 처리하였습니다.");
		            				response.setResult(result);
		        	        	} else {
		        	        		response.setState(322);
		                            response.setMessage("출금 처리중 에러가 발생했습니다.");
		        	        	}
		    				}
		    				
		        		} else {
		        			response.setState(321);
		                    response.setMessage("출금 신청중 에러가 발생했습니다.");
		        		} 
		        	} else {
		        		response.setState(320);
		                response.setMessage("출금 잔액이 부족합니다.");
		        	}
		        } else {
		    		response.setState(319);
		            response.setMessage("전문에러 발생. 잠시 후 다시 이용해주세요.");
		    	}
	        } else {
	        	response.setState(351);
				response.setMessage("출금계좌 정보가 없습니다.\n 출금계좌 등록 후 출금신청 기능을 이용해주세요.");
	        }
        } else {
        	response.setState(350);
			response.setMessage("비밀번호가 틀렸습니다.");
        }
        
		return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
	}
    
}