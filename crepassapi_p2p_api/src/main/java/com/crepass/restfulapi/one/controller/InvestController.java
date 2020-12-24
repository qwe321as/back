package com.crepass.restfulapi.one.controller;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.Slack;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.one.domain.OneCrepassCredit;
import com.crepass.restfulapi.one.domain.OneEmoneyInvestPay;
import com.crepass.restfulapi.one.domain.OneInvest;
import com.crepass.restfulapi.one.domain.OneInvestAccount;
import com.crepass.restfulapi.one.domain.OneInvestAutoDivision;
import com.crepass.restfulapi.one.domain.OneInvestAutoDivisionSet;
import com.crepass.restfulapi.one.domain.OneInvestCredit;
import com.crepass.restfulapi.one.domain.OneInvestDebt;
import com.crepass.restfulapi.one.domain.OneInvestDetail;
import com.crepass.restfulapi.one.domain.OneInvestInfo;
import com.crepass.restfulapi.one.domain.OneInvestInfoData;
import com.crepass.restfulapi.one.domain.OneInvestLimitPay;
import com.crepass.restfulapi.one.domain.OneInvestLoan;
import com.crepass.restfulapi.one.domain.OneInvestLoanDefault;
import com.crepass.restfulapi.one.domain.OneInvestOrderUnit;
import com.crepass.restfulapi.one.domain.OneInvestTitle;
import com.crepass.restfulapi.one.domain.OneInvestUserInfo;
import com.crepass.restfulapi.one.domain.OneLoanUserGrade;
import com.crepass.restfulapi.one.domain.OneMemberCustAddInfo;
import com.crepass.restfulapi.one.domain.OneWishInvest;
import com.crepass.restfulapi.one.service.EmoneyService;
import com.crepass.restfulapi.one.service.InvestService;
import com.crepass.restfulapi.one.service.OneMemberService;
import com.crepass.restfulapi.one.service.VirtualAccntService;

import io.swagger.annotations.ApiOperation;
import net.gpedro.integrations.slack.SlackMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
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
@RequestMapping(path = "/api", method = RequestMethod.POST)
public class InvestController {

    @Autowired
    private InvestService investService;
    
    @Autowired
    private EmoneyService emoneyService;
    
    @Autowired
    private OneMemberService oneMemberService;
    
    @Autowired
    private VirtualAccntService virtualAccntService;
    
    @Autowired
    private CommonUtil commonUtil;
    
    @Autowired(required=true)
	private HttpServletRequest request;
    
    @Value("${crepas.inside.url}")
    private String insideUrl;


    
    @ApiOperation(value = "채권목록조회")
    @RequestMapping("/invest/loanlist")
    public ResponseEntity<ResponseResult> getInvestLoanList(@RequestBody String requestString) throws Exception {

    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);

        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");

        JSONObject jsonRequest = (JSONObject) json.get("request");
        
        final String mid = jsonRequest.get("mid").toString();
        final String categ = jsonRequest.get("categ").toString();
        final String keyword = jsonRequest.get("keyword").toString();

        Map<String, Object> result = new HashMap<String, Object>();
        
        if(Integer.parseInt(categ) > 0) {
        	List<OneWishInvest> oneWishInvests = investService.selectInvestLoanListById(mid, categ, keyword);
        	
        	for(int i = 0; i < oneWishInvests.size(); i++) {
        		if(oneWishInvests.get(i).getInvestCredit() == null || oneWishInvests.get(i).getInvestCredit().equals("null") || oneWishInvests.get(i).getInvestCredit().isEmpty())
        			oneWishInvests.get(i).setInvestCredit("E");
        	}
        	
	        result.put("list", oneWishInvests);
        } else {
    		List<OneWishInvest> oneWishInvests = investService.selectInvestLoanListAllById(mid, keyword);
        	
        	for(int i = 0; i < oneWishInvests.size(); i++) {
        		if(oneWishInvests.get(i).getInvestCredit() == null || oneWishInvests.get(i).getInvestCredit().equals("null") || oneWishInvests.get(i).getInvestCredit().isEmpty())
        			oneWishInvests.get(i).setInvestCredit("E");
        	}
        	
        	result.put("list", oneWishInvests);
        }
        
        response.setResult(result);

        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
//        Slack.api.call(new SlackMessage("#young-server","Invest","조회시 테스트"));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
    
    @ApiOperation(value = "채권 아이템 조회")
    @RequestMapping("/invest/loanitem")
    public ResponseEntity<ResponseResult> getInvestLoanItem(@RequestBody String requestString) throws Exception {

    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);

        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");

        JSONObject jsonRequest = (JSONObject) json.get("request");
      
        final String mid = jsonRequest.get("mid").toString();
        final String loanId = jsonRequest.get("loanId").toString();
        
        response.setResult(investService.selectInvestLoanListItemById(mid, loanId));
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
    
    @ApiOperation(value = "상품상세페이지(채권상세)")
    @RequestMapping("/loan/detail")
    public ResponseEntity<ResponseResult> getInvestLoanDetail(@RequestBody String requestString) throws Exception {

    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);

        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");

        final String mid = ((JSONObject) json.get("request")).get("mid").toString();
        final String loanId = ((JSONObject) json.get("request")).get("loanId").toString();

        Map<String, Object> result = new HashMap<String, Object>();
        
        OneInvestLoan oneInvestLoan = new OneInvestLoan();
        result.put("loan", oneInvestLoan);
        oneInvestLoan = investService.selectLoanById(loanId);
        
        if (oneInvestLoan != null) {
            result.put("loan", oneInvestLoan);
            long loanPay = 1000000L;	//기준 대출금액 100만원
          
            JSONArray loanInfo = createPrincipalSchedule(Double.parseDouble(String.valueOf(loanPay)), Double.parseDouble(oneInvestLoan.getYearPlus()) * 0.01, Integer.parseInt(oneInvestLoan.getLoanDay()));
            long loanInterest = 0L;
            for(int i = 0; i < loanInfo.length(); i++) {
            	JSONObject jsonLoan = loanInfo.getJSONObject(i);
            	loanInterest += jsonLoan.getLong("loanInterest");
            }
            
          result.put("standardLoanPay", loanPay);
          result.put("standardLoanRate", loanInterest);
        }

        OneLoanUserGrade oneLoanUserGrade = investService.selectLoanUserGrade(loanId);
        
        if(oneLoanUserGrade != null) {
	        result.put("gradeKcb", oneLoanUserGrade.getKcbGrade());
	        result.put("gradeLenddo", oneLoanUserGrade.getLenddoGrade());
        } else {
        	result.put("gradeKcb", "0");
	        result.put("gradeLenddo", "E");
        }
        
        final String onedebt = investService.selectDebtById(loanId);
        Map<String, Object> debt = new HashMap<String, Object>();
        
        OneInvestDebt oneInvestDebt = new OneInvestDebt();
        
        
        //은행/보험/학자금[FIELD]한국장학재단[FIELD]903000[FIELD]신용대출[RECORD]
        //은행/보험/학자금[FIELD]중소기업은행[FIELD]303650000[FIELD]담보대출[RECORD]
        //은행/보험/학자금[FIELD]카카오뱅크[FIELD]55500000[FIELD]신용...
        
        if (onedebt != null) {
            String[] txtArr = onedebt.split("\\[RECORD\\]") ;
            for (int i = 0; i < txtArr.length; i++) {
                String[] txtResultArr = txtArr[i].split("\\[FIELD\\]") ;
                    
                long intAmt = 0;
                if (txtResultArr.length > 2) {
                    intAmt = Integer.parseInt(txtResultArr[2]);
                }
                        
                if (commonUtil.getChangeDept(txtResultArr[0]).equals("firstBank")) {
                	long firstBankAmt = 0;
                	if(debt.containsKey(commonUtil.getChangeDept(txtResultArr[0])))
                		firstBankAmt = ((long) debt.get("firstBank")) +  intAmt;
                	else
                		firstBankAmt = intAmt;
                    debt.put(commonUtil.getChangeDept(txtResultArr[0]), firstBankAmt);
                } else if (commonUtil.getChangeDept(txtResultArr[0]).equals("secondBank")) {
                	long secondBankAmt = 0;
                	if(debt.containsKey(commonUtil.getChangeDept(txtResultArr[0])))
                		secondBankAmt = ((long) debt.get("secondBank")) +  intAmt;
                	else
                		secondBankAmt = intAmt;
                    debt.put(commonUtil.getChangeDept(txtResultArr[0]), secondBankAmt);
                } else if (commonUtil.getChangeDept(txtResultArr[0]).equals("cash")) {
                	long cashAmt = 0;
                	if(debt.containsKey(commonUtil.getChangeDept(txtResultArr[0])))
                		cashAmt = ((long) debt.get("cash")) +  intAmt;
                	else
                		cashAmt = intAmt;
                    debt.put(commonUtil.getChangeDept(txtResultArr[0]), cashAmt);
                } else if (commonUtil.getChangeDept(txtResultArr[0]).equals("lendding")) {
                	long lenddingAmt = 0;
                	if(debt.containsKey(commonUtil.getChangeDept(txtResultArr[0])))
                		lenddingAmt = ((long) debt.get("lendding")) +  intAmt;
                	else
                		lenddingAmt = intAmt;
                    debt.put(commonUtil.getChangeDept(txtResultArr[0]), lenddingAmt);
                } else if (commonUtil.getChangeDept(txtResultArr[0]).equals("ptop")) {
                	long ptopAmt = 0;
                	if(debt.containsKey(commonUtil.getChangeDept(txtResultArr[0])))
                		ptopAmt = ((long) debt.get("ptop")) +  intAmt;
                	else
                		ptopAmt = intAmt;
                    debt.put(commonUtil.getChangeDept(txtResultArr[0]), ptopAmt);
                } else if (commonUtil.getChangeDept(txtResultArr[0]).equals("guarantee")) {
                	long guaranteeAmt = 0;
                	if(debt.containsKey(commonUtil.getChangeDept(txtResultArr[0])))
                		guaranteeAmt = ((long) debt.get("guarantee")) +  intAmt;
                	else
                		guaranteeAmt = intAmt;
                    debt.put(commonUtil.getChangeDept(txtResultArr[0]), guaranteeAmt);
                } else {
                    debt.put(commonUtil.getChangeDept(txtResultArr[0]), intAmt);
                }
                
//                txtResultArr[0]
                
                if (commonUtil.getChangeDept(txtResultArr[0]).equals("firstBank")) {
                    oneInvestDebt.setFirstBank(commonUtil.getAmountUnit2(Long.parseLong(debt.get("firstBank").toString())));
                } else if (commonUtil.getChangeDept(txtResultArr[0]).equals("secondBank")) {
                	oneInvestDebt.setSecondBank(commonUtil.getAmountUnit2(Long.parseLong(debt.get("secondBank").toString())));
                } else if (commonUtil.getChangeDept(txtResultArr[0]).equals("cash")) {
                	oneInvestDebt.setCash(commonUtil.getAmountUnit2(Long.parseLong(debt.get("cash").toString())));
                } else if (commonUtil.getChangeDept(txtResultArr[0]).equals("lendding")) {
                	oneInvestDebt.setLendding(commonUtil.getAmountUnit2(Long.parseLong(debt.get("lendding").toString())));
                } else if (commonUtil.getChangeDept(txtResultArr[0]).equals("ptop")) {
                    oneInvestDebt.setPtop(commonUtil.getAmountUnit2(Long.parseLong(debt.get("ptop").toString())));
                } else if (commonUtil.getChangeDept(txtResultArr[0]).equals("guarantee")) {
                	oneInvestDebt.setGuarantee(commonUtil.getAmountUnit2(Long.parseLong(debt.get("guarantee").toString())));
                }
            } //for
        }
        result.put("debt", oneInvestDebt);
        
        OneInvestCredit oneInvestCredit = new OneInvestCredit();
        result.put("credit", oneInvestCredit);
        oneInvestCredit = investService.selectCreditById(loanId);
        if (oneInvestCredit != null) {
            result.put("credit", oneInvestCredit);
        }

        response.setResult(result);

        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
    
    @ApiOperation(value = "투자상품정보조회")
    @RequestMapping("/invest")
    public ResponseEntity<ResponseResult> getInvestInfo(@RequestBody String requestString) throws Exception {

    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);

        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");

        final String mid = ((JSONObject) json.get("request")).get("mid").toString();
        final String loanId = ((JSONObject) json.get("request")).get("loanId").toString();

        Map<String, Object> result = new HashMap<String, Object>();
        
        OneInvestInfo oneInvestInfo = new OneInvestInfo();
        result.put("invest", oneInvestInfo);
        oneInvestInfo = investService.selectInvestById(loanId, mid);
        if (oneInvestInfo != null) {
        	String limitPay = "0";
        	OneInvestLimitPay oneInvestLimitPay = investService.selectInvestLimitPay(mid);
	        
        	long sumIpay = Long.parseLong(oneInvestLimitPay.getSumIpay());
        	
        	if(!oneInvestLimitPay.getSignpurposeL().equals("false"))
        		limitPay = oneInvestLimitPay.getSignpurposeL();
        	if(!oneInvestLimitPay.getSignpurposeI().equals("false"))
        		limitPay = oneInvestLimitPay.getSignpurposeI();
        	if(!oneInvestLimitPay.getSignpurposeP().equals("false"))
        		limitPay = oneInvestLimitPay.getSignpurposeP();
        	if(!oneInvestLimitPay.getSignpurpose3().equals("false"))
        		limitPay = oneInvestLimitPay.getSignpurpose3();
        	
        	String investPossiblePay = investService.selectInvestPossiblePay(loanId);
        	String investingPay = investService.selectInvestDuplicate(mid, loanId);
        	
        	if(investingPay == null || investingPay.isEmpty())
        		investingPay = "0";
        	
        	if(investPossiblePay != null) {
        		long InvestPay = (long)(Long.parseLong(investPossiblePay));
        		long InvestPayPosit = (long)(Long.parseLong(oneInvestInfo.getLoanPay()) * (oneInvestInfo.getInvestMax() * 0.01)) - Long.parseLong(investingPay);
        		
        		if(InvestPayPosit < 0)
        			InvestPayPosit = 0;
	        	
        		if(InvestPay > InvestPayPosit)
        			InvestPay = InvestPayPosit;
        		
	        	if(InvestPay <= Long.parseLong(limitPay) || sumIpay == 0L) {
					oneInvestInfo.setInvestMax((int)InvestPay);
				} else {
					oneInvestInfo.setInvestMax(Integer.parseInt(limitPay));
				}
        	} else {
        		long InvestPay = (long)(Long.parseLong(investPossiblePay));
        		long InvestPayPosit = (long)(Long.parseLong(oneInvestInfo.getLoanPay()) * (oneInvestInfo.getInvestMax() * 0.01)) - Long.parseLong(investingPay);
        		
        		if(InvestPayPosit < 0)
        			InvestPayPosit = 0;
        		
        		if(InvestPay > InvestPayPosit)
        			InvestPay = InvestPayPosit;
        		
        		if(InvestPay <= Long.parseLong(limitPay)) {
					oneInvestInfo.setInvestMax((int)InvestPay);
				} else {
					oneInvestInfo.setInvestMax(Integer.parseInt(limitPay));
				}
        	}
        	
            result.put("invest", oneInvestInfo);
        }
        
        OneInvestAccount oneInvestAccount = new OneInvestAccount();
        result.put("account", oneInvestAccount);
        oneInvestAccount = investService.selectAccountById2(mid);
        if (oneInvestAccount != null) {
            oneInvestAccount.setMyBankName(virtualAccntService.selectBankById(oneInvestAccount.getMyBankcode()));
            
            String selectCustID = oneMemberService.selectCustID(mid);
            
            if(selectCustID != null) {
    	        RestTemplate restTemplate = new RestTemplate();
    	        Map<String, String> vars = new HashMap<String, String>();
    	        vars.put("CUST_ID", selectCustID);
    	        
    	        String resultAMT = restTemplate.postForObject(insideUrl + "/lookup/customer", vars, String.class);
    	        JSONObject jsonResult = new JSONObject(resultAMT);
    	        int State = jsonResult.getInt("STATE");
	    	        
    	        if(State == 200) {
	    	        String balanceAMT = ((JSONObject)jsonResult.get("RESULT")).getString("BALANCE_AMT");
	    	        
	    	        int updateEmoney = emoneyService.updateEmoney(String.valueOf(balanceAMT), mid);
	    	        
	    	        if(updateEmoney > 0) {
	    	        	String investPayment = emoneyService.selectEmoneyInvestIsPlaying(mid);
	    	        	String withdrawPay = emoneyService.selectEmoneyWithdrawPay2(mid);
	    	        	long payment = ((long)emoneyService.selectTrAmtBalanceById(mid) - Long.parseLong(investPayment)) - Long.parseLong(withdrawPay);
	    	            oneInvestAccount.setMEmoney(payment);
	    	            
	    	            
	    	            
	    	            
	    	            result.put("account", oneInvestAccount);
	    	        } else {
	    	        	response.setState(303);
	    	            response.setMessage("예치금 조회에 실패하였습니다.");
	    	        }
    	        } else {
    	        	response.setState(304);
                    response.setMessage("고객ID가 존재하지 않습니다. 고객센터에 문의하세요.");
    	        }
            } else {
            	response.setState(304);
                response.setMessage("고객ID가 존재하지 않습니다. 고객센터에 문의하세요.");
            }
        }
        
        response.setResult(result);

        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
    
    @ApiOperation(value = "투자수익조회")
    @RequestMapping("/invest/profit")
    public ResponseEntity<ResponseResult> getInvestProfit(@RequestBody String requestString) throws Exception {

    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);

        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");

        final String mid = ((JSONObject) json.get("request")).get("mid").toString();
        final String loanId = ((JSONObject) json.get("request")).get("loanId").toString();

        OneInvestOrderUnit oneInvestOrderUnit = investService.selectInvestOrderUnitById(mid, loanId);
        
        Map<String, Object> result = new HashMap<String, Object>();
        
        if(oneInvestOrderUnit != null) {
	        result.put("ipay", oneInvestOrderUnit.getIpay());
	        result.put("maturity", oneInvestOrderUnit.getMaturity());
	        result.put("lnIyul", oneInvestOrderUnit.getLnIyul());
	        result.put("paytype", oneInvestOrderUnit.getPaytype());
        } else {
        	result.put("ipay", null);
	        result.put("maturity", null);
	        result.put("lnIyul", null);
	        result.put("paytype", null);
        }
        
        /**수정해야함**/
        result.put("list", investService.selectInvestOrderById(mid, loanId));

        response.setResult(result);

        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
    
    @ApiOperation(value = "대출약정조회")
    @RequestMapping("/loan/contract")
    public ResponseEntity<ResponseResult> getLoanContract(@RequestBody String requestString) throws Exception {

    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        
        final String mid = ((JSONObject) json.get("request")).get("mid").toString();
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("list", investService.selectLoanContract(mid));
        
        response.setResult(result);
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "권리증서조회")
    @RequestMapping("/invest/certi")
    public ResponseEntity<ResponseResult> getInvestCerti(@RequestBody String requestString) throws Exception {
        
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        
        final String mid = ((JSONObject) json.get("request")).get("mid").toString();
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        response.setResult(investService.selectInvestCerti(mid));
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "CrePASS평가등급입력")
    @RequestMapping("/invest/igrade")
    public ResponseEntity<ResponseResult> setiGrade(@RequestBody String requestString) throws Exception {
        
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        
        final String mid = ((JSONObject) json.get("request")).get("mid").toString();
        final String grade = ((JSONObject) json.get("request")).get("grade").toString();

        OneCrepassCredit oneCrepassCredit = new OneCrepassCredit();
        oneCrepassCredit.setMid(mid);
        oneCrepassCredit.setGrade(grade);
        
        investService.updateiGrade(oneCrepassCredit);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
//        response.setResult(investService.updateiGrade(oneCrepassCredit));
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }

    @ApiOperation(value = "투자하기 (대출투자자등록)")
    @RequestMapping("/invest/add")
    @Transactional("oneTransactionManager")
    public ResponseEntity<ResponseResult> addInvest(@RequestBody String requestString) throws Exception {
        
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        JSONObject request = (JSONObject) json.get("request");
        
        final String mid = request.getString("mid");
        String i_pay = request.getString("i_pay");
        final String loan_id = request.getString("loan_id");
        final String m_password = request.getString("m_password");
        
        i_pay = String.valueOf(((Long.parseLong(i_pay) / 10000) * 10000));
        
        ResponseResult response = new ResponseResult();
        
        String limitPay2 = "0";
        OneInvestLimitPay oneInvestLimitPay2 = investService.selectInvestLimitPay2(mid, loan_id);
        if(!oneInvestLimitPay2.getSignpurposeL().equals("false"))
        	limitPay2 = oneInvestLimitPay2.getSignpurposeL();
    	if(!oneInvestLimitPay2.getSignpurposeI().equals("false"))
    		limitPay2 = oneInvestLimitPay2.getSignpurposeI();
    	if(!oneInvestLimitPay2.getSignpurposeP().equals("false"))
    		limitPay2 = oneInvestLimitPay2.getSignpurposeP();
    	if(!oneInvestLimitPay2.getSignpurpose3().equals("false")) {
    		limitPay2 = oneInvestLimitPay2.getSignpurpose3();
	    	if(Long.parseLong(limitPay2) == 0) {
	    		limitPay2 = i_pay;
	    	}
    	}
        

    	// 200827~210430 1.법인 40% 이상 투자할수 없음		운영팀 요청으로 해제...
    	OneInvestLoanDefault oneInvestLoanDefault = investService.selectInvestLoan(loan_id);	// 기본 대출정보
    	String mLevel = investService.selectInvestLevel(mid);									// 투자자 레벨선택
//    	
//    	if (mLevel.equals("4")) {								// 법인 투자율이
//	    	long loanPay = Long.parseLong(oneInvestLoanDefault.getLoanPay());
//	    	if ((loanPay * 0.4) < Long.parseLong(i_pay)) {		// 40%을 넘기면 투자불가
//	    		Map<String, Object> result = new HashMap<String, Object>();
//	    		result.put("i_pay", i_pay);
//	    		result.put("loanPay", loanPay);
//				
//				response.setState(377);
//				response.setMessage("법인투자자는 한채권에 40% 이상 투자할수 없습니다.");
//				response.setResult(result);
//				
//				return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
//	    		
//	    	}
//    	}
    	
    	// 2-1. 200827~210430  일반투자자가 1천만원이상 투자 불가, 동일차입자 500만원 이상 투자할수 없음
    	long investTotalAmount = Long.parseLong(investService.selectInvestTotalAmount(mid));	// mari_invest-투자자 투자총금액    	
    	long investLimitation = Long.parseLong(investService.selectInvestLimitation(mid));		// mari_inset-레벨별 투자 한도	
    	if (mLevel.equals("1")) {	
    		if (investTotalAmount+Long.parseLong(i_pay) > investLimitation) {					// 투자자 투자총금액+신규투자액이 한도를 초과하면 에러
    			Map<String, Object> result = new HashMap<String, Object>();
	    		result.put("i_pay", i_pay);
	    		result.put("investTotalAmount", investTotalAmount);
				
				response.setState(378);
				response.setMessage("투자 한도액을 초과하였습니다.");
				response.setResult(result);
				
				commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
				return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    		}
    		
    		// 그 대출자에 투자한 금액이 있는지 확인!
    		long usedToInvest = Long.parseLong(investService.selectUsedToInvest(oneInvestLoanDefault.getMid(), mid));	// 대출자, 투자자 mid
    		if ( usedToInvest+Long.parseLong(i_pay) > 5000000) {		// 과거 투자액과 신규 투자액이 500만원을 넘으면
    			Map<String, Object> result = new HashMap<String, Object>();
	    		result.put("i_pay", i_pay);
	    		result.put("usedToInvest", usedToInvest);
				
				response.setState(379);
				response.setMessage("동일 차입자에 투자할수 있는 투자금액을 초과하였습니다.");
				response.setResult(result);
				
				commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
				return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
	    		
    		}
    	}
    	
    	if(Long.parseLong(i_pay) > Long.parseLong(limitPay2)) {
    		Map<String, Object> result = new HashMap<String, Object>();
    		result.put("i_pay", limitPay2);
			
			response.setState(373);
			response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
			response.setResult(result);
			
			return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    	}
    	
        String UserConfirm = oneMemberService.selectUserConfirm(mid, m_password);
        
        if(UserConfirm != null) {
	        OneInvestAccount oneInvestAccount = investService.selectAccountById(mid);
	        
	        //state 통신전 처리
	        String duplicate = investService.selectInvestDuplicate(mid, loan_id);
	        
        	String custId = oneMemberService.selectCustID(mid);
        	RestTemplate restTemplate = new RestTemplate();
	        Map<String, String> vars = new HashMap<String, String>();
	        vars.put("CUST_ID", custId);
	        
	        String resultAMT = restTemplate.postForObject(insideUrl + "/lookup/customer", vars, String.class);
	        JSONObject jsonAMT = new JSONObject(resultAMT);
	        
	        String balanceAMT = ((JSONObject)jsonAMT.get("RESULT")).getString("BALANCE_AMT");
	        if(Long.parseLong(i_pay) <= Long.parseLong(balanceAMT)) {
	        	OneEmoneyInvestPay investPay = emoneyService.selectInvestProgressPay(mid);
	        	
	            vars = new HashMap<String, String>();
	            vars.put("BANK_CD", oneInvestAccount.getMyBankcode());
	            vars.put("ACCT_NB", oneInvestAccount.getMyBankacc());
	            
	            String resultOne = restTemplate.postForObject(insideUrl + "/lookup/reciever", vars, String.class);
	            
	            JSONObject jsonResult = new JSONObject(resultOne);
	            
	            if(jsonResult.getInt("STATE") == 200) {
	            	String investIsPlaying = investService.selectInvestIsPlaying(loan_id);
	            	
	            	if(investIsPlaying != null) {
			        	String limitPay = "0";
			        	OneInvestLimitPay oneInvestLimitPay = investService.selectInvestLimitPay(mid);
				        
			        	long sumIpay = Long.parseLong(oneInvestLimitPay.getSumIpay());
			        	
			        	if(!oneInvestLimitPay.getSignpurposeL().equals("false"))
			        		limitPay = oneInvestLimitPay.getSignpurposeL();
			        	if(!oneInvestLimitPay.getSignpurposeI().equals("false"))
			        		limitPay = oneInvestLimitPay.getSignpurposeI();
			        	if(!oneInvestLimitPay.getSignpurposeP().equals("false"))
			        		limitPay = oneInvestLimitPay.getSignpurposeP();
			        	if(!oneInvestLimitPay.getSignpurpose3().equals("false"))
			        		limitPay = oneInvestLimitPay.getSignpurpose3();
			        	
				        if(0 < Long.parseLong(limitPay) || sumIpay == 0L) {
				        	String minPay = investService.selectInvestMinPay(loan_id, i_pay);
				        	
					        if(minPay != null) {
					        	String possiblePay = investService.selectInvestPossiblePay(loan_id);
					        	System.out.println("possiblePay : " + possiblePay);
					        	
					        	if(0 < Long.parseLong(possiblePay)) {
					        		if(Long.parseLong(i_pay) <= Long.parseLong(possiblePay)) {
					        			if(Long.parseLong(i_pay) <= Long.parseLong(limitPay) || sumIpay == 0L) {
					        				if(Long.parseLong(i_pay) > Long.parseLong(limitPay2)) {
					        		    		Map<String, Object> result = new HashMap<String, Object>();
					        		    		result.put("i_pay", limitPay2);
					        					
					        					response.setState(373);
					        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
					        					response.setResult(result);
					        					
					        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
					        		    	}
					        				
					        				OneInvestTitle oneInvestTitle = investService.selectInvestTitle(mid, loan_id);
					        				
					        				int ranNum = (int)(Math.random() * (999 - 111 + 1)) + 111;
					        				long time = System.currentTimeMillis();
					        				String gCode = "P" + String.valueOf(time) + String.valueOf(ranNum);
					        				
					        				double eMoney = Double.parseDouble(balanceAMT);///emoneyService.selectTrAmtBalanceById(mid);
					        				double eMoneyCal = (eMoney - Double.parseDouble(i_pay)) - Double.parseDouble(investPay.getIpay());	// 예치금 - 투자하려는금액 - 현재펀딩중인금액
					        				// double eMoneyCal = eMoney - Double.parseDouble(i_pay);
			        	            		
					        				if(eMoneyCal >= 0) { 
						        				int isInvestAdd = setInvestAdd(gCode, mid, loan_id, i_pay, oneInvestTitle);
						        				
						        				int invsetDetail = 0;
						        				if(duplicate == null) {																// 예치금값 업데이트(예치금 - 투자하려는금액 - 현재펀딩중인금액)
						        					invsetDetail = investService.insertInvestDetail(setInvestDetail(mid, loan_id, i_pay, String.valueOf(eMoneyCal), oneInvestTitle));
						        					investService.insertInvestHistory(loan_id, custId, i_pay, i_pay, gCode, oneInvestTitle.getSubject(), "I");
						        				} else {
						        					invsetDetail = investService.updateInvestPay(mid, loan_id, String.valueOf(Long.parseLong(i_pay) + Long.parseLong(duplicate)));
						        					investService.insertInvestHistory(loan_id, custId, i_pay, String.valueOf(Long.parseLong(i_pay) + Long.parseLong(duplicate)), gCode, oneInvestTitle.getSubject(), "U");
						        				}
				        	            		
				        	            		if (invsetDetail > 0 && isInvestAdd > 0) {
				        	            			JSONObject jsonSmsData = new JSONObject();
				        	            			jsonSmsData.put("title", oneInvestTitle.getSubject());
				        	                		jsonSmsData.put("name", oneInvestTitle.getName());
				        	                		jsonSmsData.put("payment", commonUtil.getAmountUnit3(Long.parseLong(i_pay)));
				        	                		
				        	                		String msg = commonUtil.getFormSMS(6, jsonSmsData);
				        	                		commonUtil.setRequestSMSData(oneInvestTitle.getName(), "I", oneInvestTitle.getCustId(), oneInvestTitle.getHp(), msg);
				        	            			
				        	            			response.setState(200);
					        	            		response.setMessage("정상적으로 처리하였습니다.");
				        	            		} else {
				        	            			response.setState(374);
						        	                response.setMessage("투자자 등록에 예치기 못한 에러가 발생하였습니다. 고객센터에 문의하세요.");
				        	            		}
					        				} else {
					        					long possible = (((long)eMoney / 10000) * 10000) - Long.parseLong(investPay.getIpay());
			        	            			
					        					if(possible < 0) {
				        	            			Map<String, Object> result = new HashMap<String, Object>();
						        		    		result.put("i_pay", possible);
						        					
						        					response.setState(373);
						        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
						        					response.setResult(result);
						        					
						        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK); 
					        					} else {
					        						response.setState(376);
						        					response.setMessage("예치금액이 적어 투자를 하실 수 없습니다.");
						        					
						        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK); 
					        					}
					        				}
					        				
					        			} else {
					        				if(Long.parseLong(limitPay) > Long.parseLong(limitPay2)) {
					        		    		Map<String, Object> result = new HashMap<String, Object>();
					        		    		result.put("i_pay", limitPay2);
					        					
					        					response.setState(373);
					        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
					        					response.setResult(result);
					        					
					        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
					        		    	}
					        				
					        				Map<String, Object> result = new HashMap<String, Object>();
						            		result.put("i_pay", limitPay);
						        			
						        			response.setState(373);
						        			response.setMessage("최대 투자가능한 금액한도를 넘었습니다.");
						        			response.setResult(result);
					        			}
					        			
					        		} else {
					        			if(Long.parseLong(i_pay) - (Long.parseLong(i_pay) - Long.parseLong(possiblePay)) > Long.parseLong(limitPay2)) {
				        		    		Map<String, Object> result = new HashMap<String, Object>();
				        		    		result.put("i_pay", limitPay2);
				        					
				        					response.setState(373);
				        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
				        					response.setResult(result);
				        					
				        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
				        		    	}
					        			
					        			Map<String, Object> result = new HashMap<String, Object>();
					            		result.put("i_pay", String.valueOf(Long.parseLong(i_pay) - (Long.parseLong(i_pay) - Long.parseLong(possiblePay))));
					        			
					        			response.setState(372);
					        			response.setMessage("투자가능한 금액한도를 넘었습니다.");
					        			response.setResult(result);
					        		}
					        		
					        	} else {
					    			response.setState(371);
					    			response.setMessage("투자모집이 만료되어 해당 투자상품에 투자가 불가합니다.");
					        	}
					        } else {
								response.setState(370);
								response.setMessage("최소투자금액보다 적게 투자하실 수 없습니다.");
					        }
				        } else {
				        	response.setState(369);
							response.setMessage("투자가능한 채권한도를 초과하실수 없습니다.");
				        }
	            	} else {
	            		String minPay = investService.selectInvestMinPay(loan_id, i_pay);
			        	
				        if(minPay != null) {
				        	String possiblePay = investService.selectInvestPossiblePay(loan_id);
				        	
				        	if(possiblePay != null) {
					        	if(0 < Long.parseLong(possiblePay)) {
					        		if(Long.parseLong(i_pay) <= Long.parseLong(possiblePay)) {
					        			if(Long.parseLong(i_pay) > Long.parseLong(limitPay2)) {
				        		    		Map<String, Object> result = new HashMap<String, Object>();
				        		    		result.put("i_pay", limitPay2);
				        					
				        					response.setState(373);
				        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
				        					response.setResult(result);
				        					
				        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
				        		    	}
					        			
				        				OneInvestTitle oneInvestTitle = investService.selectInvestTitle(mid, loan_id);
				        				
				        				int ranNum = (int)(Math.random() * (999 - 111 + 1)) + 111;
				        				long time = System.currentTimeMillis();
				        				String gCode = "P" + String.valueOf(time) + String.valueOf(ranNum);
				        				
				        				double eMoney = Double.parseDouble(balanceAMT);//emoneyService.selectTrAmtBalanceById(mid);
				        				double eMoneyCal = (eMoney - Double.parseDouble(i_pay)) - Double.parseDouble(investPay.getIpay());
//		        	            		double eMoneyCal = (eMoney - Double.parseDouble(i_pay)) - duplPay;
		        	            		
		        	            		if(eMoneyCal >= 0) { 
					        				int isInvestAdd = setInvestAdd(gCode, mid, loan_id, i_pay, oneInvestTitle);
					        				
					        				int invsetDetail = 0;
					        				if(duplicate == null) {
					        					invsetDetail = investService.insertInvestDetail(setInvestDetail(mid, loan_id, i_pay, String.valueOf(eMoneyCal), oneInvestTitle));
					        					investService.insertInvestHistory(loan_id, custId, i_pay, i_pay, gCode, oneInvestTitle.getSubject(), "I");
					        				} else {
					        					invsetDetail = investService.updateInvestPay(mid, loan_id, String.valueOf(Long.parseLong(i_pay) + Long.parseLong(duplicate)));
					        					investService.insertInvestHistory(loan_id, custId, i_pay, String.valueOf(Long.parseLong(i_pay) + Long.parseLong(duplicate)), gCode, oneInvestTitle.getSubject(), "U");
					        				}
			        	            		
			        	            		if (invsetDetail > 0 && isInvestAdd > 0) {
			        	            			JSONObject jsonSmsData = new JSONObject();
			        	            			jsonSmsData.put("title", oneInvestTitle.getSubject());
			        	                		jsonSmsData.put("name", oneInvestTitle.getName());
			        	                		jsonSmsData.put("payment", commonUtil.getAmountUnit3(Long.parseLong(i_pay)));
			        	                		
			        	                		String msg = commonUtil.getFormSMS(6, jsonSmsData);
			        	                		commonUtil.setRequestSMSData(oneInvestTitle.getName(), "I", oneInvestTitle.getCustId(), oneInvestTitle.getHp(), msg);
			        	            			
			        	            			response.setState(200);
				        	            		response.setMessage("정상적으로 처리하였습니다.");
			        	            		} else {
			        	            			response.setState(374);
					        	                response.setMessage("투자자 등록에 예치기 못한 에러가 발생하였습니다. 고객센터에 문의하세요.");
			        	            		}
		        	            		} else {
		        	            			long possible = (((long)eMoney / 10000) * 10000) - Long.parseLong(investPay.getIpay());
		        	            			
				        					if(possible < 0) {
			        	            			Map<String, Object> result = new HashMap<String, Object>();
					        		    		result.put("i_pay", possible);
					        					
					        					response.setState(373);
					        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
					        					response.setResult(result);
					        					
					        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK); 
				        					} else {
				        						response.setState(376);
					        					response.setMessage("예치금액이 적어 투자를 하실 수 없습니다.");
					        					
					        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK); 
				        					}
		        	            		}
					        				
					        		} else {
					        			if(Long.parseLong(i_pay) - (Long.parseLong(i_pay) - Long.parseLong(possiblePay)) > Long.parseLong(limitPay2)) {
				        		    		Map<String, Object> result = new HashMap<String, Object>();
				        		    		result.put("i_pay", limitPay2);
				        					
				        					response.setState(373);
				        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
				        					response.setResult(result);
				        					
				        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
				        		    	}
					        			
					        			Map<String, Object> result = new HashMap<String, Object>();
					            		result.put("i_pay", String.valueOf(Long.parseLong(i_pay) - (Long.parseLong(i_pay) - Long.parseLong(possiblePay))));
					        			
					        			response.setState(372);
					        			response.setMessage("투자가능한 금액한도를 넘었습니다.");
					        			response.setResult(result);
					        		}
					        		
					        	} else {
					    			response.setState(371);
					    			response.setMessage("투자모집이 만료되어 해당 투자상품에 투자가 불가합니다.");
					        	}
				        	} else {
				        		OneInvestTitle oneInvestTitle = investService.selectInvestTitle(mid, loan_id);
		        				
		        				int ranNum = (int)(Math.random() * (999 - 111 + 1)) + 111;
		        				long time = System.currentTimeMillis();
		        				String gCode = "P" + String.valueOf(time) + String.valueOf(ranNum);
		        				
		        				double eMoney = Double.parseDouble(balanceAMT);
		        				double eMoneyCal = (eMoney - Double.parseDouble(i_pay)) - Double.parseDouble(investPay.getIpay());
//		        				double eMoney = emoneyService.selectTrAmtBalanceById(mid);
//        	            		double eMoneyCal = eMoney - Double.parseDouble(i_pay);
        	            		
		        				if(eMoneyCal >= 0) {
			        				int isInvestAdd = setInvestAdd(gCode, mid, loan_id, i_pay, oneInvestTitle);
			        				
			        				int invsetDetail = 0;
			        				if(duplicate == null) {
			        					invsetDetail = investService.insertInvestDetail(setInvestDetail(mid, loan_id, i_pay, String.valueOf(eMoneyCal), oneInvestTitle));
			        					investService.insertInvestHistory(loan_id, custId, i_pay, i_pay, gCode, oneInvestTitle.getSubject(), "I");
			        				} else {
			        					invsetDetail = investService.updateInvestPay(mid, loan_id, String.valueOf(Long.parseLong(i_pay) + Long.parseLong(duplicate)));
			        					investService.insertInvestHistory(loan_id, custId, i_pay, String.valueOf(Long.parseLong(i_pay) + Long.parseLong(duplicate)), gCode, oneInvestTitle.getSubject(), "U");
			        				}
	        	            		
	        	            		if (invsetDetail > 0 && isInvestAdd > 0) {
	        	            			JSONObject jsonSmsData = new JSONObject();
	        	            			jsonSmsData.put("title", oneInvestTitle.getSubject());
	        	                		jsonSmsData.put("name", oneInvestTitle.getName());
	        	                		jsonSmsData.put("payment", commonUtil.getAmountUnit3(Long.parseLong(i_pay)));
	        	                		
	        	                		String msg = commonUtil.getFormSMS(6, jsonSmsData);
	        	                		commonUtil.setRequestSMSData(oneInvestTitle.getName(), "I", oneInvestTitle.getCustId(), oneInvestTitle.getHp(), msg);
	        	            			
	        	            			response.setState(200);
		        	            		response.setMessage("정상적으로 처리하였습니다.");
	        	            		}
		        				} else {
        	            			long possible = (((long)eMoney / 10000) * 10000) - Long.parseLong(investPay.getIpay());
        	            			
		        					if(possible < 0) {
	        	            			Map<String, Object> result = new HashMap<String, Object>();
			        		    		result.put("i_pay", possible);
			        					
			        					response.setState(373);
			        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
			        					response.setResult(result);
			        					
			        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK); 
		        					} else {
		        						response.setState(376);
			        					response.setMessage("예치금액이 적어 투자를 하실 수 없습니다.");
			        					
			        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK); 
		        					}
        	            		}
				        	}
				        } else {
							response.setState(370);
							response.setMessage("최소투자금액보다 적게 투자하실 수 없습니다.");
				        }
			        }
	            } else {
	                response.setState(jsonResult.getInt("STATE"));
	                response.setMessage(jsonResult.getString("MESSAGE"));
	            }
	        	
	        } else {
	        	response.setState(365);
				response.setMessage("예치금 잔액이 부족합니다.");
	        }
        } else {
        	response.setState(350);
			response.setMessage("비밀번호가 틀렸습니다.");
        }
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
    
    @ApiOperation(value = "투자금액 수정")
    @RequestMapping("/invest/modify")
    @Transactional("oneTransactionManager")
    public ResponseEntity<ResponseResult> modifyInvest(@RequestBody String requestString) throws Exception {
        
        JSONObject json = new JSONObject(requestString);
        JSONObject request = (JSONObject) json.get("request");
        
        final String mid = request.getString("mid");
        String i_pay = request.getString("i_pay");
        final String loan_id = request.getString("loan_id");
        
        i_pay = String.valueOf(((Long.parseLong(i_pay) / 10000) * 10000));
        
        ResponseResult response = new ResponseResult();
        
        String limitPay2 = "0";
        OneInvestLimitPay oneInvestLimitPay2 = investService.selectInvestLimitPay2(mid, loan_id);
        if(!oneInvestLimitPay2.getSignpurposeL().equals("false"))
        	limitPay2 = oneInvestLimitPay2.getSignpurposeL();
    	if(!oneInvestLimitPay2.getSignpurposeI().equals("false"))
    		limitPay2 = oneInvestLimitPay2.getSignpurposeI();
    	if(!oneInvestLimitPay2.getSignpurposeP().equals("false"))
    		limitPay2 = oneInvestLimitPay2.getSignpurposeP();
    	if(!oneInvestLimitPay2.getSignpurpose3().equals("false")) {
    		limitPay2 = oneInvestLimitPay2.getSignpurpose3();
	    	if(Long.parseLong(limitPay2) == 0) {
	    		limitPay2 = i_pay;
	    	}
    	}
        
    	if(Long.parseLong(i_pay) > Long.parseLong(limitPay2)) {
    		Map<String, Object> result = new HashMap<String, Object>();
    		result.put("i_pay", limitPay2);
			
			response.setState(373);
			response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
			response.setResult(result);
			
			return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    	}
    	
        OneInvestAccount oneInvestAccount = investService.selectAccountById(mid);
        
        String duplicate = investService.selectInvestDuplicate(mid, loan_id);
        
        if(duplicate != null) {
        	if(Long.parseLong(i_pay) == Long.parseLong(duplicate)) {
        		response.setState(200);
        		response.setMessage("정상적으로 처리하였습니다.");
        		return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
        	}
        	
	    	String custId = oneMemberService.selectCustID(mid);
	    	RestTemplate restTemplate = new RestTemplate();
	        Map<String, String> vars = new HashMap<String, String>();
	        vars.put("CUST_ID", custId);
	        
	        String resultAMT = restTemplate.postForObject(insideUrl + "/lookup/customer", vars, String.class);
	        JSONObject jsonAMT = new JSONObject(resultAMT);
	        
	        String balanceAMT = ((JSONObject)jsonAMT.get("RESULT")).getString("BALANCE_AMT");
	        if(Long.parseLong(i_pay) <= Long.parseLong(balanceAMT)) {
	        	OneEmoneyInvestPay investPay = emoneyService.selectInvestProgressPay(mid);
	        	//Inside 계좌검증 - 통신 확인 (솔루션)
	            vars = new HashMap<String, String>();
	            vars.put("BANK_CD", oneInvestAccount.getMyBankcode());
	            vars.put("ACCT_NB", oneInvestAccount.getMyBankacc());
	            
	            String resultOne = restTemplate.postForObject(insideUrl + "/lookup/reciever", vars, String.class);
	            
	            JSONObject jsonResult = new JSONObject(resultOne);
	            
	            if(jsonResult.getInt("STATE") == 200) {
	            	String investIsPlaying = investService.selectInvestIsPlaying(loan_id);
	            	
	            	if(investIsPlaying != null) {
			        	String limitPay = "0";
			        	OneInvestLimitPay oneInvestLimitPay = investService.selectInvestLimitPay(mid);
				        
			        	long sumIpay = Long.parseLong(oneInvestLimitPay.getSumIpay());
			        	
			        	if(!oneInvestLimitPay.getSignpurposeL().equals("false"))
			        		limitPay = oneInvestLimitPay.getSignpurposeL();
			        	if(!oneInvestLimitPay.getSignpurposeI().equals("false"))
			        		limitPay = oneInvestLimitPay.getSignpurposeI();
			        	if(!oneInvestLimitPay.getSignpurposeP().equals("false"))
			        		limitPay = oneInvestLimitPay.getSignpurposeP();
			        	if(!oneInvestLimitPay.getSignpurpose3().equals("false"))
			        		limitPay = oneInvestLimitPay.getSignpurpose3();
			        	
				        if(0 < Long.parseLong(limitPay) || sumIpay == 0L) {
				        	String minPay = investService.selectInvestMinPay(loan_id, i_pay);
				        	
					        if(minPay != null) {
					        	String possiblePay = investService.selectInvestPossiblePay2(loan_id, mid);
					        	
					        	if(0 < Long.parseLong(possiblePay)) {
					        		if(Long.parseLong(i_pay) <= Long.parseLong(possiblePay)) {
					        			if(Long.parseLong(i_pay) <= Long.parseLong(limitPay) || sumIpay == 0L) {
					        				if(Long.parseLong(i_pay) > Long.parseLong(limitPay2)) {
					        		    		Map<String, Object> result = new HashMap<String, Object>();
					        		    		result.put("i_pay", limitPay2);
					        					
					        					response.setState(373);
					        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
					        					response.setResult(result);
					        					
					        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
					        		    	}
					        				
					        				OneInvestTitle oneInvestTitle = investService.selectInvestTitle(mid, loan_id);
					        				
					        				int ranNum = (int)(Math.random() * (999 - 111 + 1)) + 111;
					        				long time = System.currentTimeMillis();
					        				String gCode = "P" + String.valueOf(time) + String.valueOf(ranNum);
					        				
//					        				double eMoney = emoneyService.selectTrAmtBalanceById(mid);
//			        	            		double eMoneyCal = eMoney - Double.parseDouble(i_pay);
					        				double eMoney = Double.parseDouble(balanceAMT);
					        				double eMoneyCal = (eMoney - Double.parseDouble(i_pay)) - Double.parseDouble(investPay.getIpay());
			        	            		
					        				if(eMoneyCal >= 0) {
						        				int isInvestAdd = setInvestAdd(gCode, mid, loan_id, i_pay, oneInvestTitle);
						        				
						        				int invsetDetail = investService.updateInvestPay(mid, loan_id, i_pay);
					        					investService.insertInvestHistory(loan_id, custId, String.valueOf(Long.parseLong(i_pay) - Long.parseLong(duplicate)), i_pay, gCode, oneInvestTitle.getSubject(), "U");
						        				
				        	            		if (invsetDetail > 0 && isInvestAdd > 0) {
				        	            			response.setState(200);
					        	            		response.setMessage("정상적으로 처리하였습니다.");
				        	            		} else {
				        	            			response.setState(374);
						        	                response.setMessage("투자자 등록에 예치기 못한 에러가 발생하였습니다. 고객센터에 문의하세요.");
				        	            		}
					        				} else {
			        	            			long possible = (((long)eMoney / 10000) * 10000) - Long.parseLong(investPay.getIpay());
			        	            			
					        					if(possible < 0) {
				        	            			Map<String, Object> result = new HashMap<String, Object>();
						        		    		result.put("i_pay", possible);
						        					
						        					response.setState(373);
						        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
						        					response.setResult(result);
						        					
						        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK); 
					        					} else {
					        						response.setState(376);
						        					response.setMessage("예치금액이 적어 투자를 하실 수 없습니다.");
						        					
						        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK); 
					        					}
			        	            		}
					        				
					        			} else {
					        				if(Long.parseLong(limitPay) > Long.parseLong(limitPay2)) {
					        		    		Map<String, Object> result = new HashMap<String, Object>();
					        		    		result.put("i_pay", limitPay2);
					        					
					        					response.setState(373);
					        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
					        					response.setResult(result);
					        					
					        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
					        		    	}
					        				
					        				Map<String, Object> result = new HashMap<String, Object>();
						            		result.put("i_pay", limitPay);
						        			
						        			response.setState(373);
						        			response.setMessage("최대 투자가능한 금액한도를 넘었습니다.");
						        			response.setResult(result);
					        			}
					        			
					        		} else {
					        			if(Long.parseLong(i_pay) - (Long.parseLong(i_pay) - Long.parseLong(possiblePay)) > Long.parseLong(limitPay2)) {
				        		    		Map<String, Object> result = new HashMap<String, Object>();
				        		    		result.put("i_pay", limitPay2);
				        					
				        					response.setState(373);
				        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
				        					response.setResult(result);
				        					
				        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
				        		    	}
					        			
					        			Map<String, Object> result = new HashMap<String, Object>();
					            		result.put("i_pay", String.valueOf(Long.parseLong(i_pay) - (Long.parseLong(i_pay) - Long.parseLong(possiblePay))));
					        			
					        			response.setState(372);
					        			response.setMessage("투자가능한 금액한도를 넘었습니다.");
					        			response.setResult(result);
					        		}
					        		
					        	} else {
					    			response.setState(371);
					    			response.setMessage("투자모집이 만료되어 해당 투자상품에 투자가 불가합니다.");
					        	}
					        } else {
								response.setState(370);
								response.setMessage("최소투자금액보다 적게 투자하실 수 없습니다.");
					        }
				        } else {
				        	response.setState(369);
							response.setMessage("투자가능한 채권한도를 초과하실수 없습니다.");
				        }
	            	} else {
	            		String minPay = investService.selectInvestMinPay(loan_id, i_pay);
			        	
				        if(minPay != null) {
				        	String possiblePay = investService.selectInvestPossiblePay2(loan_id, mid);
				        	
				        	if(possiblePay != null) {
					        	if(0 < Long.parseLong(possiblePay)) {
					        		if(Long.parseLong(i_pay) <= Long.parseLong(possiblePay)) {
					        			if(Long.parseLong(i_pay) > Long.parseLong(limitPay2)) {
				        		    		Map<String, Object> result = new HashMap<String, Object>();
				        		    		result.put("i_pay", limitPay2);
				        					
				        					response.setState(373);
				        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
				        					response.setResult(result);
				        					
				        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
				        		    	}
					        			
				        				OneInvestTitle oneInvestTitle = investService.selectInvestTitle(mid, loan_id);
				        				
				        				int ranNum = (int)(Math.random() * (999 - 111 + 1)) + 111;
				        				long time = System.currentTimeMillis();
				        				String gCode = "P" + String.valueOf(time) + String.valueOf(ranNum);
				        				
//				        				double eMoney = emoneyService.selectTrAmtBalanceById(mid);
//		        	            		double eMoneyCal = eMoney - Double.parseDouble(i_pay);
				        				double eMoney = Double.parseDouble(balanceAMT);
				        				double eMoneyCal = (eMoney - Double.parseDouble(i_pay)) - Double.parseDouble(investPay.getIpay());
		        	            		
				        				if(eMoneyCal >= 0) {
					        				int isInvestAdd = setInvestAdd(gCode, mid, loan_id, i_pay, oneInvestTitle);
				        					int invsetDetail = investService.updateInvestPay(mid, loan_id, i_pay);
				        					investService.insertInvestHistory(loan_id, custId, String.valueOf(Long.parseLong(i_pay) - Long.parseLong(duplicate)), i_pay, gCode, oneInvestTitle.getSubject(), "U");
			        	            		
			        	            		if (invsetDetail > 0 && isInvestAdd > 0) {
			        	            			response.setState(200);
				        	            		response.setMessage("정상적으로 처리하였습니다.");
			        	            		} else {
			        	            			response.setState(374);
					        	                response.setMessage("투자자 등록에 예치기 못한 에러가 발생하였습니다. 고객센터에 문의하세요.");
			        	            		}
				        				} else {
				        					long possible = (((long)eMoney / 10000) * 10000) - Long.parseLong(investPay.getIpay());
		        	            			
				        					if(possible < 0) {
			        	            			Map<String, Object> result = new HashMap<String, Object>();
					        		    		result.put("i_pay", possible);
					        					
					        					response.setState(373);
					        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
					        					response.setResult(result);
					        					
					        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK); 
				        					} else {
				        						response.setState(376);
					        					response.setMessage("예치금액이 적어 투자를 하실 수 없습니다.");
					        					
					        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK); 
				        					}
				        				}
					        				
					        		} else {
					        			if(Long.parseLong(i_pay) - (Long.parseLong(i_pay) - Long.parseLong(possiblePay)) > Long.parseLong(limitPay2)) {
				        		    		Map<String, Object> result = new HashMap<String, Object>();
				        		    		result.put("i_pay", limitPay2);
				        					
				        					response.setState(373);
				        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
				        					response.setResult(result);
				        					
				        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
				        		    	}
					        			
					        			Map<String, Object> result = new HashMap<String, Object>();
					            		result.put("i_pay", String.valueOf(Long.parseLong(i_pay) - (Long.parseLong(i_pay) - Long.parseLong(possiblePay))));
					        			
					        			response.setState(372);
					        			response.setMessage("투자가능한 금액한도를 넘었습니다.");
					        			response.setResult(result);
					        		}
					        		
					        	} else {
					    			response.setState(371);
					    			response.setMessage("투자모집이 만료되어 해당 투자상품에 투자가 불가합니다.");
					        	}
				        	} else {
				        		OneInvestTitle oneInvestTitle = investService.selectInvestTitle(mid, loan_id);
		        				
		        				int ranNum = (int)(Math.random() * (999 - 111 + 1)) + 111;
		        				long time = System.currentTimeMillis();
		        				String gCode = "P" + String.valueOf(time) + String.valueOf(ranNum);
		        				
//		        				double eMoney = emoneyService.selectTrAmtBalanceById(mid);
//	    	            		double eMoneyCal = eMoney - Double.parseDouble(i_pay);
		        				double eMoney = Double.parseDouble(balanceAMT);
		        				double eMoneyCal = (eMoney - Double.parseDouble(i_pay)) - Double.parseDouble(investPay.getIpay());
	    	            		
		        				if(eMoneyCal >= 0) {
			        				int isInvestAdd = setInvestAdd(gCode, mid, loan_id, i_pay, oneInvestTitle);
			        				int invsetDetail = investService.updateInvestPay(mid, loan_id, i_pay);
		        					investService.insertInvestHistory(loan_id, custId, String.valueOf(Long.parseLong(i_pay) - Long.parseLong(duplicate)), i_pay, gCode, oneInvestTitle.getSubject(), "U");
		    	            		
		    	            		if (invsetDetail > 0 && isInvestAdd > 0) {
		    	            			response.setState(200);
		        	            		response.setMessage("정상적으로 처리하였습니다.");
		    	            		}
		        				} else {
		        					long possible = (((long)eMoney / 10000) * 10000) - Long.parseLong(investPay.getIpay());
        	            			
		        					if(possible < 0) {
	        	            			Map<String, Object> result = new HashMap<String, Object>();
			        		    		result.put("i_pay", possible);
			        					
			        					response.setState(373);
			        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
			        					response.setResult(result);
			        					
			        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK); 
		        					} else {
		        						response.setState(376);
			        					response.setMessage("예치금액이 적어 투자를 하실 수 없습니다.");
			        					
			        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK); 
		        					}
		        				}
				        	}
				        } else {
							response.setState(370);
							response.setMessage("최소투자금액보다 적게 투자하실 수 없습니다.");
				        }
			        }
	            } else {
	                response.setState(jsonResult.getInt("STATE"));
	                response.setMessage(jsonResult.getString("MESSAGE"));
	            }
	        	
	        } else {
	        	response.setState(365);
				response.setMessage("예치금 잔액이 부족합니다.");
	        }
        } else {
        	response.setState(360);
			response.setMessage("투자 진행 정보가 없습니다.");
        }
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
    
    @ApiOperation(value = "투자 취소")
    @RequestMapping("/invest/delete")
    @Transactional("oneTransactionManager")
    public ResponseEntity<ResponseResult> deleteInvest(@RequestBody String requestString) throws Exception {
        
        JSONObject json = new JSONObject(requestString);
        JSONObject request = (JSONObject) json.get("request");
        
        final String mid = request.getString("mid");
        final String loanId = request.getString("loan_id");
        
        ResponseResult response = new ResponseResult();
        if(investService.updateInvestLeave(mid, loanId)) {									// mil-투자취소정보입력
        	OneInvestUserInfo oneInvestUserInfo = investService.selectInvestUserInfo(mid, loanId);	// mi-취소할 대상 선택
        	if(investService.deleteInvest(mid, loanId)) {									// mi-투자내용 삭제
        		investService.insertInvestHistory(loanId, oneInvestUserInfo.getCustId(), String.valueOf(Long.parseLong(oneInvestUserInfo.getIPay()) * -1)
        				, "0", "", oneInvestUserInfo.getSubject(), "D");					// cih-투자정보 입력
        		response.setState(200);
        		response.setMessage("정상적으로 처리하였습니다.");
        	} else {
        		response.setState(361);
        		response.setMessage("투자내역 삭제처리 불가.\n개발팀에 문의 바랍니다.");
        	}
        } else {
    		response.setState(362);
    		response.setMessage("해당 고객의 투자건이 존재하지 않아 취소 할 수 없습니다.");
    	}
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
    
    public OneInvestDetail setInvestDetail(String mid, String loan_id, String i_pay, String eMoneyCal, OneInvestTitle oneInvestTitle) throws Exception {
		oneMemberService.updateMemberMoney(String.valueOf(eMoneyCal), mid);
		
		OneInvestLoanDefault oneInvestLoanDefault = investService.selectInvestLoan(loan_id);
		
		OneInvestDetail oneInvestDetail = new OneInvestDetail();
		oneInvestDetail.setLoanId(loan_id);
		oneInvestDetail.setMid(mid);
		oneInvestDetail.setMname(oneInvestTitle.getName());
		oneInvestDetail.setUserName(oneInvestLoanDefault.getMname());
		oneInvestDetail.setUserId(oneInvestLoanDefault.getMid());
		oneInvestDetail.setPay(i_pay);
		oneInvestDetail.setSubject(oneInvestTitle.getSubject());
		oneInvestDetail.setGoods(oneInvestLoanDefault.getPayMent());
		oneInvestDetail.setLoanPay(oneInvestLoanDefault.getLoanPay());
		oneInvestDetail.setMaxPay(oneInvestLoanDefault.getLoanDay());
		oneInvestDetail.setDay(oneInvestLoanDefault.getLoanDay());
		oneInvestDetail.setProfitRate(oneInvestLoanDefault.getYearPlus());
		oneInvestDetail.setIp(commonUtil.getRemoteAddrs());
		
		return oneInvestDetail;
    }
    
    public int setInvestAdd(String gCode, String mid, String loan_id, String i_pay, OneInvestTitle oneInvestTitle) throws Exception {
    	OneInvest oneInvest = new OneInvest();
    	oneInvest.setGCode(gCode);
    	oneInvest.setMid(mid);
    	oneInvest.setMName(oneInvestTitle.getName());
    	oneInvest.setSubject(oneInvestTitle.getSubject());
    	oneInvest.setLoanId(loan_id);
    	oneInvest.setIPay(i_pay);
    	
		return investService.insertInvest(oneInvest);
    }
    
    @ApiOperation(value = "투자자 고객등록")
    @RequestMapping("/invest/custId/add")
    public ResponseEntity<ResponseResult> getInvestCustIdAdd(@RequestBody String requestString) throws Exception {

    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);

        ResponseResult response = new ResponseResult();

        final String mid = ((JSONObject) json.get("request")).get("mid").toString();

        OneMemberCustAddInfo oneMemberCustAddInfo = oneMemberService.selectCustAddInfo(mid);
        
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("CUST_ID", oneMemberCustAddInfo.getCustId());
        
        String resultSearch = restTemplate.postForObject(insideUrl + "/customer/search", vars, String.class);
        JSONObject jsonSearch = new JSONObject(resultSearch);
        
        int searchState = jsonSearch.getInt("STATE");
        
        if(searchState != 200) {
        	String hpFront = oneMemberCustAddInfo.getHp().substring(0, 3);
            String hpBack = oneMemberCustAddInfo.getHp().substring(oneMemberCustAddInfo.getHp().length() - 4, oneMemberCustAddInfo.getHp().length());
            String hpMiddle = oneMemberCustAddInfo.getHp().replace(hpBack, "");
            hpMiddle = hpMiddle.substring(3, hpMiddle.length());
            
        	vars = new HashMap<String, String>();
            vars.put("CUST_ID", oneMemberCustAddInfo.getCustId());
            vars.put("CUST_NM", oneMemberCustAddInfo.getName());
            vars.put("CUST_SUB_NM", "");
            vars.put("REP_NM", "");
            vars.put("BIRTH_DATE", oneMemberCustAddInfo.getBirth().replace("-", ""));
            vars.put("SUP_REG_NB", "");
            vars.put("PRI_SUP_GBN", "1");
            vars.put("HP_NO1", hpFront);
            vars.put("HP_NO2", hpMiddle);
            vars.put("HP_NO3", hpBack);
            vars.put("BANK_CD", oneMemberCustAddInfo.getMyBankcode());
            vars.put("ACCT_NB", oneMemberCustAddInfo.getMyBankacc());
            vars.put("CMS_NB", oneMemberCustAddInfo.getVirtualAccnt());
        
            String resultCustAdd = restTemplate.postForObject(insideUrl + "/customer/add", vars, String.class);
            JSONObject jsonCustAdd = new JSONObject(resultCustAdd);
            
            int custAddState = jsonCustAdd.getInt("STATE");
            
            if(custAddState == 200) {
            	response.setState(200);
                response.setMessage("정상적으로 처리하였습니다.");
            } else {
            	response.setState(jsonCustAdd.getInt("STATE"));
                response.setMessage(jsonCustAdd.getString("MESSAGE"));
            }
        } else {
        	response.setState(204);
            response.setMessage("고객이 정상적으로 등록되어있습니다.");
        }
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());

        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
    
    @ApiOperation(value = "정산현황")
    @RequestMapping("/invest/balance/state")
    public ResponseEntity<ResponseResult> getBalanceState(@RequestBody String requestString) throws Exception {
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String mid = jsonRequest.get("mid").toString();
        
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("list", investService.selectInvestBalanceState(mid));
        response.setResult(result);
        
//        investService.selectInvestBalanceState(mid, loanId);
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "자동투자 등록정보")
    @RequestMapping("/invest/auto/division/get")
    public ResponseEntity<ResponseResult> getAutoDivision(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String mid = jsonRequest.get("mid").toString();
        
        OneInvestAutoDivision oneInvestAutoDivision = investService.selectInvestAutoDivision(mid);
        if(oneInvestAutoDivision != null)
        	oneInvestAutoDivision.setCategory(investService.selectInvestAutoCategory(oneInvestAutoDivision.getAid()));
        
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("division", oneInvestAutoDivision);
        response.setResult(result);
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "자동투자 등록")
    @RequestMapping("/invest/auto/division/add")
    public ResponseEntity<ResponseResult> setAutoDivision(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String mid = jsonRequest.get("mid").toString();
        final String m_password = jsonRequest.getString("m_password");
        final String isActivate = jsonRequest.get("isActivate").toString();
        final String limitLoan = jsonRequest.get("limitLoan").toString();
        final String limitMonth = jsonRequest.get("limitMonth").toString();
        final String univName = jsonRequest.get("univName").toString();
        final String agreedYN = jsonRequest.get("agreedYN").toString();
        JSONArray jsonCategory = jsonRequest.getJSONArray("category");
        
        String UserConfirm = oneMemberService.selectUserConfirm(mid, m_password);
        
        if(UserConfirm != null) {
	        OneInvestAutoDivisionSet oneInvestAutoDivisionSet = new OneInvestAutoDivisionSet();
	        oneInvestAutoDivisionSet.setMid(mid);
	        oneInvestAutoDivisionSet.setIsActivate(isActivate);
	        oneInvestAutoDivisionSet.setLimitLoan(limitLoan);
	        oneInvestAutoDivisionSet.setLimitMonth(limitMonth);
	        oneInvestAutoDivisionSet.setUnivName(univName);
	        oneInvestAutoDivisionSet.setAgreedYN(agreedYN);
	        
	        investService.insertInvestAutoDivision(oneInvestAutoDivisionSet);
	        OneInvestAutoDivision oneInvestAutoDivision = investService.selectInvestAutoDivision(mid);
	        
	        for(int i = 0; i < jsonCategory.length(); i++) {
	        	JSONObject category = jsonCategory.getJSONObject(i);
	        	
	        	String categoryId = category.getString("categoryId");
	        	investService.insertInvestAutoDivisionCategory(oneInvestAutoDivision.getAid(), categoryId);
	        }
        } else {
        	response.setState(350);
			response.setMessage("비밀번호가 틀렸습니다.");
        }
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "자동투자 취소")
    @RequestMapping("/invest/auto/division/cancel")
    public ResponseEntity<ResponseResult> setAutoDivisionCancel(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String mid = jsonRequest.get("mid").toString();
        
        OneInvestAutoDivisionSet oneInvestAutoDivisionSet = new OneInvestAutoDivisionSet();
        oneInvestAutoDivisionSet.setMid(mid);
        oneInvestAutoDivisionSet.setIsActivate("N");
        oneInvestAutoDivisionSet.setLimitLoan(null);
        oneInvestAutoDivisionSet.setLimitMonth(null);
        oneInvestAutoDivisionSet.setUnivName(null);
        oneInvestAutoDivisionSet.setAgreedYN("N");
        investService.insertInvestAutoDivision(oneInvestAutoDivisionSet);
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "투자내역, 투자상환정보 조회")
    @RequestMapping("/invest/info/get")
    public ResponseEntity<ResponseResult> getMyInvestInfo(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String mid = jsonRequest.get("mid").toString();
        
        List<OneInvestInfoData> oneInvestInfoDatas = investService.selectInvestInfoData(mid);
        
        for(int i = 0; i < oneInvestInfoDatas.size(); i++) {
        	switch(oneInvestInfoDatas.get(i).getI_look()) {
	        	case "Y" :
	        		oneInvestInfoDatas.get(i).setStatus("투자진행중");
        			break;
	        	case "C" :
	        		oneInvestInfoDatas.get(i).setStatus("투자마감");
        			break;
	        	case "N" :
	        		oneInvestInfoDatas.get(i).setStatus("투자대기");
        			break;
	        	case "D" :
	        		oneInvestInfoDatas.get(i).setStatus("상환중");
        			break;
	        	case "F" :
	        		oneInvestInfoDatas.get(i).setStatus("상환완료");
        			break;
	        	case "O" :
	        		oneInvestInfoDatas.get(i).setStatus("연체");
        			break;
        	}
        }
        
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("invest_data", oneInvestInfoDatas);
        result.put("order_data", investService.selectInvestInfoOrderData(mid));
        response.setResult(result);
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    private JSONArray createPrincipalSchedule(double loanAmt, double interest, int loanPeriod) {
    	try {
	    	double loanAmtOrg = loanAmt;
	    	long repayAmount = (((long)getPmt(interest/12, loanPeriod, loanAmt * -1, 0, 0)) / 10) * 10;
	    	
			long loanCountPaymentSum = 0;
			long loanPayCutSum = 0;
			long loanPaySumTotal = 0;
			
			JSONArray jsonResultArry = new JSONArray();
			
	    	for(int i = 0; i < loanPeriod; i++) {
	    		long loanInterest = (long) Math.floor(loanAmt * (interest / 12));
	    		long loanCountPayment = (long) Math.floor(repayAmount - loanInterest);
	    		loanCountPaymentSum += loanCountPayment;
	    		
	    		if(i == (loanPeriod - 1)) {
	    			long repayAmountRest = (long)loanAmtOrg - loanCountPaymentSum;
	    			loanCountPayment += repayAmountRest;
	    		}
	    		
	    		long loanPaySum = loanCountPayment + loanInterest;
	    		long loanPayCut = ((loanCountPayment + loanInterest) / 10) * 10;
	    		
	    		loanPayCutSum += loanPayCut;
	    		loanPaySumTotal += loanPaySum;
	    		
	    		if(i == (loanPeriod - 1)) {
	    			long diffPay = loanPaySumTotal - loanPayCutSum;
	    			loanPayCut += diffPay;
	    		}
	    		
	    		loanAmt -= loanCountPayment;
	    		
	    		JSONObject jsonResult = new JSONObject();
				jsonResult.put("payCount", String.valueOf((i + 1)));
				jsonResult.put("repayAmount", String.valueOf((long)loanPayCut));
				jsonResult.put("paidAmount", String.valueOf((long)loanCountPayment));
				jsonResult.put("loanInterest", String.valueOf((long)loanInterest));
				jsonResult.put("balance", String.valueOf((long)loanAmt));
				jsonResultArry.put(jsonResult);
	    	}
	    	
	    	return jsonResultArry;
    	} catch (JSONException e) {
			e.printStackTrace();
		}
    	
    	return null;
    }
    
    public double getPmt(double rate, double nper, double pv, double fv, int type) 	{
    	return rate == 0 ? (- pv - fv) / nper : (- fv * rate - pv * rate * Math.pow(1 + rate, nper)) / ((1 + rate * type) * (Math.pow(1 + rate, nper) - 1));
	}
    
    public long getDiffOfDate(String loanStartDate, String loanEndDate) {
    	try {
	    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	    	Date beginDate = formatter.parse(loanStartDate);
	    	Date endDate = formatter.parse(loanEndDate);
	    	
	    	long diff = endDate.getTime() - beginDate.getTime();
	    	
	    	return diff / (24 * 60 * 60 * 1000);
    	} catch (Exception e) {
    		System.out.println(e.getMessage());
    	}
    			
        return -999;
    }
    
}