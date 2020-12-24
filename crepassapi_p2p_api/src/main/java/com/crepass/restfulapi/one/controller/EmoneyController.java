package com.crepass.restfulapi.one.controller;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.one.domain.OneEmoneyBank;
import com.crepass.restfulapi.one.domain.OneEmoneyBankName;
import com.crepass.restfulapi.one.domain.OneEmoneyDetailHistory;
import com.crepass.restfulapi.one.domain.OneEmoneyDetailHistory2;
import com.crepass.restfulapi.one.domain.OneEmoneyInvestPay;
import com.crepass.restfulapi.one.service.EmoneyService;
import com.crepass.restfulapi.one.service.OneMemberService;
import com.crepass.restfulapi.one.service.VirtualAccntService;

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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@CrossOrigin
@RestController
@RequestMapping(path = "/api", method = RequestMethod.POST)
public class EmoneyController {

    @Autowired
    private EmoneyService emoneyService;
    
    @Autowired
    private OneMemberService oneMemberService;
    
    @Value("${crepas.inside.url}")
    private String insideUrl;
    
    @Autowired
    private CommonUtil commonUtil;
    
    @Autowired
    private VirtualAccntService virtualAccntService;

    @Autowired(required=true)
	private HttpServletRequest request;
    
    @ApiOperation(value = "예치금내역조회")
    @RequestMapping("/emoney/list")
    public ResponseEntity<ResponseResult> getEmoneyList(@RequestBody String requestString) throws Exception {

    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);

        ResponseResult response = new ResponseResult();

        final String mid = ((JSONObject) json.get("request")).get("mid").toString();

        OneEmoneyInvestPay investPay = emoneyService.selectInvestProgressPay(mid);
        String withdrawPay = emoneyService.selectEmoneyWithdrawPay2(mid);
        
        String custId = oneMemberService.selectCustID(mid);
        String investPayment = "0";
        
        if(custId != null) {
//	        if(investPay != null) {
		        RestTemplate restTemplate = new RestTemplate();
		        Map<String, String> vars = new HashMap<String, String>();
		        vars.put("CUST_ID", custId);
		        
		        if(investPay != null)
		        	investPayment = investPay.getIpay(); 
		        
		        String resultAMT = restTemplate.postForObject(insideUrl + "/lookup/customer", vars, String.class);
		        JSONObject jsonResult = new JSONObject(resultAMT);
		        String balanceAMT = ((JSONObject)jsonResult.get("RESULT")).getString("BALANCE_AMT");
		        
		        Long l_pay = Long.parseLong(balanceAMT) - Long.parseLong(investPayment) - Long.parseLong(withdrawPay);
		        
		        if (l_pay < 0)
		        	l_pay = 0L;
		        
		        int updateEmoney = emoneyService.updateEmoney(String.valueOf(l_pay), mid);
		        
		        if(updateEmoney > 0) {
		        	response.setState(200);
		            response.setMessage("정상적으로 처리하였습니다.");
		            
			        Map<String, Object> result = new HashMap<String, Object>();
			        result.put("list", emoneyService.selectEmoneyDetailHistory(mid));
			        result.put("trAmtBalance", emoneyService.selectTrAmtBalanceById(mid));
			
			        response.setResult(result);
		        } else {
		        	response.setState(303);
		            response.setMessage("예치금 조회에 실패하였습니다.");
		        }
//	        }
//        else {
//	        	response.setState(306);
//	            response.setMessage("예치금 조회내역이 없습니다.");
//	        }
        } else {
        	response.setState(304);
            response.setMessage("고객ID가 존재하지 않습니다. 고객센터에 문의하세요.");
        }
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());

        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
    
    @ApiOperation(value = "예치금내역조회 - (투자,상환)")
    @RequestMapping("/emoney/list2")
    public ResponseEntity<ResponseResult> getEmoneyList2(@RequestBody String requestString) throws Exception {

    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);

        ResponseResult response = new ResponseResult();

        final String mid = ((JSONObject) json.get("request")).get("mid").toString();
        final String typeFlag = ((JSONObject) json.get("request")).get("typeFlag").toString();

        OneEmoneyInvestPay investPay = emoneyService.selectInvestProgressPay(mid);
        String withdrawPay = emoneyService.selectEmoneyWithdrawPay2(mid);
        
        String custId = oneMemberService.selectCustID(mid);
        String investPayment = "0";
        
        if(custId != null) {
//        if(investPay != null) {
	        RestTemplate restTemplate = new RestTemplate();
	        Map<String, String> vars = new HashMap<String, String>();
	        vars.put("CUST_ID", custId);
	        
	        if(investPay != null)
	        	investPayment = investPay.getIpay();
	        
	        String resultAMT = restTemplate.postForObject(insideUrl + "/lookup/customer", vars, String.class);
	        JSONObject jsonResult = new JSONObject(resultAMT);
	        
	        String balanceAMT = "0";
	        
	        if(jsonResult.getInt("STATE") == 200)
	        	balanceAMT = ((JSONObject)jsonResult.get("RESULT")).getString("BALANCE_AMT");
	        
	        Long l_pay = Long.parseLong(balanceAMT) - Long.parseLong(investPayment) - Long.parseLong(withdrawPay);
	        
	        if (l_pay < 0)
	        	l_pay = 0L;
	        
	        int updateEmoney = emoneyService.updateEmoney(String.valueOf(l_pay), mid);
	        
	        if(updateEmoney > 0) {
	        	response.setState(200);
	            response.setMessage("정상적으로 처리하였습니다.");
	            
		        Map<String, Object> result = new HashMap<String, Object>();
		        if(typeFlag.equals("I")) {
		        	List<OneEmoneyDetailHistory> oneEmoneyDetailHistories = emoneyService.selectEmoneyDetailHistory2(mid, typeFlag);
		        	
		        	long accntMoney = 0;
		        	
		        	for(int i = 0; i < oneEmoneyDetailHistories.size(); i++) {
		        		if(oneEmoneyDetailHistories.get(i).getTrxType().equals("D"))
		        			accntMoney += Double.parseDouble(oneEmoneyDetailHistories.get(i).getTrxAmt());
		        		else
		        			accntMoney -= Double.parseDouble(oneEmoneyDetailHistories.get(i).getTrxAmt());
		        	}
		        	
		        	result.put("list", oneEmoneyDetailHistories);
		        	result.put("trAmtBalance", accntMoney);
		        } else {
		        	
		        	List<OneEmoneyDetailHistory2> oneEmoneyDetailHistory2s = emoneyService.selectEmoneyDetailHistoryRepayment(mid);
		        	
		        	long accntMoney = 0;
		        	
		        	for(int i = 0; i < oneEmoneyDetailHistory2s.size(); i++) {
		        		if(oneEmoneyDetailHistory2s.get(i).getTrxType().equals("D"))
		        			accntMoney += Double.parseDouble(oneEmoneyDetailHistory2s.get(i).getTrxAmt());
		        		else
		        			accntMoney -= Double.parseDouble(oneEmoneyDetailHistory2s.get(i).getTrxAmt());
		        	}
		        	
		        	result.put("list", oneEmoneyDetailHistory2s);
		        	result.put("trAmtBalance", accntMoney);
		        }
		        
		        response.setResult(result);
	        } else {
	        	response.setState(303);
	            response.setMessage("예치금 조회에 실패하였습니다.");
	        }
        } else {
        	response.setState(304);
            response.setMessage("고객ID가 존재하지 않습니다. 고객센터에 문의하세요.");
        }
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());

        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
    
    @ApiOperation(value = "예치금관리")										// 출금 포함?
    @RequestMapping("/emoney/retreive")
    public ResponseEntity<ResponseResult> getEmoneyRetreive(@RequestBody String requestString) throws Exception {

    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);

        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");

        final String mid = ((JSONObject) json.get("request")).get("mid").toString();
        
        String investPayment = emoneyService.selectEmoneyInvestIsPlaying(mid);							// mm,mi,mip-현재 투자한 금액중 회수되지 않은 투자액의 합
        String withdrawPay = emoneyService.selectEmoneyWithdrawPay2(mid);								// cwt-투자자의 출금처리되지 않은(trx_flag=N) 금액의 합
//        String withdrawPay = emoneyService.selectEmoneyWithdrawPay(mid);
        
        if(investPayment == null)
        	investPayment = "0";
        double emoney = emoneyService.selectTrAmtBalanceById(mid);										// mm-투자자 예치금의 합
        
//        OneEmoneyInvestPay investPay = emoneyService.selectInvestProgressPay(mid);
        OneEmoneyInvestPay oneEmoneyInvestPay = emoneyService.selectEmoneyPayInfo(mid);					// ml...-투자를 했으나 대출실행이 되지 않은 액수의 합
        
//        String emoneyWithdrawPay = emoneyService.selectEmoneyWithdrawPay2(mid);
        
        if(oneEmoneyInvestPay != null) {
	        RestTemplate restTemplate = new RestTemplate();
	        Map<String, String> vars = new HashMap<String, String>();
	        vars.put("CUST_ID", oneEmoneyInvestPay.getCustId());
	        
	        String resultAMT = restTemplate.postForObject(insideUrl + "/lookup/customer", vars, String.class);
	        JSONObject jsonResult = new JSONObject(resultAMT);
        
	        int STATE = jsonResult.getInt("STATE");
	        long setEmoney = 0;
	        
	        String emoneyInvestWithdrawPay = emoneyService.selectEmoneyInvestWithdrawPay(mid);			// cdt,mm-출금가능예정금액(당일출금불가)
	        if(emoneyInvestWithdrawPay == null)
	        	emoneyInvestWithdrawPay = "0";
	        
	        if(STATE == 200) {
	        	String balanceAMT = ((JSONObject)jsonResult.get("RESULT")).getString("BALANCE_AMT");
	        	setEmoney = Long.parseLong(balanceAMT) - Long.parseLong(oneEmoneyInvestPay.getIpay()) - Long.parseLong(withdrawPay);	//inside - mip투자금액합 - cwt전문호출안된금액합  
	        	emoneyService.updateTopEmoney(String.valueOf(setEmoney), mid);				
	        } else
	        	setEmoney = ((long)(emoney));
	        
	        long emoneyInvestWithdrawPayReal = setEmoney - (long)Double.parseDouble(emoneyInvestWithdrawPay);
	        
	        if(emoneyInvestWithdrawPayReal < 0)
	        	emoneyInvestWithdrawPayReal = 0;
	        
            OneEmoneyBank oneEmoneyBank = null;
            oneEmoneyBank = emoneyService.selectEmoneyRetreive(mid);
            if (oneEmoneyBank != null) {
            	oneEmoneyBank.setWithdrawRealPay(String.valueOf(emoneyInvestWithdrawPayReal));	// 출금 가능금액(WithdrawRealPay), 총 예치금액(TopEmoney=>mm-m_emoney)
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
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }

    @ApiOperation(value = "출금계좌명등록")
    @RequestMapping("/emoney/bankname/add")
    public ResponseEntity<ResponseResult> setEmoneyBankName(@RequestBody String requestString) throws Exception {

    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);

        final String mid = ((JSONObject) json.get("request")).get("mid").toString();
        final String myBankName = ((JSONObject) json.get("request")).get("myBankName").toString();

        OneEmoneyBankName oneEmoneyBankName = new OneEmoneyBankName();
        oneEmoneyBankName.setMid(mid);
        oneEmoneyBankName.setMyBankName(myBankName);
        
        emoneyService.updateEmoneyBankName(oneEmoneyBankName);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());

        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
    
}