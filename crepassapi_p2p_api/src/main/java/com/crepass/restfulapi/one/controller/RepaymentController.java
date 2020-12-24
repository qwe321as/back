package com.crepass.restfulapi.one.controller;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.one.domain.OneOrderPrePayment;
import com.crepass.restfulapi.one.domain.OneOverdueNumberOfCount;
import com.crepass.restfulapi.one.domain.OnePaymentInvestSchedule;
import com.crepass.restfulapi.one.domain.OnePrePayment;
import com.crepass.restfulapi.one.domain.OnePrePaymentProvide;
import com.crepass.restfulapi.one.domain.OneRepayScheduleAdd;
import com.crepass.restfulapi.one.domain.OneRepaymentDataInfo;
import com.crepass.restfulapi.one.domain.OneRepaymentScheduleItem;
import com.crepass.restfulapi.one.domain.OneSendCheckingEmail;
import com.crepass.restfulapi.one.service.RepaymentService;
import com.crepass.restfulapi.one.service.ScheService;

import io.swagger.annotations.ApiOperation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@CrossOrigin
@RestController
@RequestMapping(path = "/api", method = RequestMethod.POST)
public class RepaymentController {
    
    @Autowired
    private RepaymentService repaymentService;
    
    @Autowired
    private ScheService scheService;
    
    @Autowired
    private CommonUtil commonUtil;
    
    @Autowired(required=true)
	private HttpServletRequest request;
    
//	  월별 연체율이 달랐을 시기에 적용된 소스   
//    @ApiOperation(value = "중도상환조회")
//    @RequestMapping("/prepayment/schedule/get")
//    public ResponseEntity<ResponseResult> getPrePaymentSchedule(@RequestBody String requestString) throws Exception {
//    	
//    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
//        commonUtil.sendRequestLogging(mapping_url, requestString);
//    	
//        ResponseResult response = new ResponseResult();
//        response.setState(200);
//        response.setMessage("정상적으로 처리하였습니다.");
//        
//        JSONObject jsonRequest = new JSONObject(requestString).getJSONObject("request");
//        
//        String loanId = jsonRequest.getString("loanId");
//        String prepayDate = jsonRequest.getString("preDate");
//        
//        OneRepaymentDataInfo oneRePaymentDataInfo = repaymentService.selectRepaymentDataInfo(loanId);
//        List<OneRepaymentScheduleItem> oneRepaymentScheduleItems = repaymentService.selectRepaymentScheduleItem(loanId, prepayDate);	// crs-검색일자 이전 상환하지 않은 정보선택(id, 회차, 납부예정일, 원금등등)
//        String balance = repaymentService.selectOverdueBalance(loanId);									// crs-남은 원금 조회
//        String interest = "0";
//        
//        if(balance == null) {
//        	response.setState(445);
//            response.setMessage("상환스케줄이 존재하지 않습니다.");
//            
//            commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
//            
//            return new ResponseEntity<ResponseResult>(response, HttpStatus.OK); 
//        }
//        
//        double[] loanRates = {3, 3, 3, 3};
//        double payAmountSum = 0;
//        
//        JSONArray jsonResult = new JSONArray();
//        
//        if(oneRepaymentScheduleItems.size() < 1) {
//        	String recentDate = repaymentService.selectRecentRepaymentDate(loanId);						// crs-최근상환일 선택
//        	
//        	if(recentDate == null) {
//        		recentDate = repaymentService.selectLaonExecuteDate(loanId);							// 없으면 대출 실행일기준
//        		if(recentDate == null) {
//	        		Calendar cal = Calendar.getInstance();
//					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//					recentDate = sdf.format(cal.getTime());
//        		}
//        	}
//        	
//        	interest = createPrePaymentInterest(Double.parseDouble(balance)								// 1. 자투리이자 : 최근상환일부터 오늘(상환예정일자)까지의 이자구하기
//    				, Double.parseDouble(oneRePaymentDataInfo.getInterestRate()) * 0.01
//    				, Integer.parseInt(oneRePaymentDataInfo.getLoanDay()), recentDate, prepayDate);
//        	
//        	JSONObject jsonItem = new JSONObject();
//        	jsonItem.put("payCount", "0");
//        	jsonItem.put("overDueState", "0");
//        	jsonItem.put("prePayment", "0");
//        	jsonItem.put("overDuePayment", "0");
//        	jsonItem.put("interest", interest);
//        	jsonResult.put(jsonItem);
//        }
//
//        interest = "0";
//        for(int i = 0; i < oneRepaymentScheduleItems.size(); i++) {
//        	String payAmount = oneRepaymentScheduleItems.get(i).getPayAmount();
//        	String payCount = oneRepaymentScheduleItems.get(i).getCount();
//        	String payDate = oneRepaymentScheduleItems.get(i).getPayDate();
//        	String nextDate = oneRepaymentScheduleItems.get(i).getNextDate();
//        	String rDelqAmount = oneRepaymentScheduleItems.get(i).getRDelqAmount();
////        	String pDelqAmount = oneRepaymentScheduleItems.get(i).getPDelqAmount();
//        	String delqState = oneRepaymentScheduleItems.get(i).getDelqState();
//        	
//        	if(Integer.parseInt(delqState) == 1)														// 연체 기록이 있으면 지불하지 않은 이자이므로 이자 누적
//        		interest = String.valueOf(Long.parseLong(interest) + Long.parseLong(oneRepaymentScheduleItems.get(i).getInterestAmount()));
//        	
//        	if(nextDate == null)
//        		nextDate = prepayDate;
//        	
//        	String toDay = nextDate;
//        	String overDuePayment = "0";
//        	
//        	int overDueCount = repaymentService.selectOverdueCount(loanId, nextDate);
//        	int overDueState = 0;
//        	
//        	if(overDueCount > 0)
//        		overDueCount--;
//        	
//        	if(overDueCount > 3)
//        		overDueCount = 3;
//        	
//        	long dateDiff = getDiffOfDate(prepayDate, nextDate);
//        	
//        	if(dateDiff > 0)
//        		toDay = prepayDate;
//        	
//        	double loanRate = (loanRates[overDueCount] + Double.parseDouble(oneRePaymentDataInfo.getInterestRate())) * 0.01;
//
//        	if(overDueCount < 2) {
//        		overDueState = 1;
//        		payAmountSum += Double.parseDouble(payAmount);
//	        	overDuePayment = createPrePaymentInterest(payAmountSum
//	    				, loanRate
//	    				, Integer.parseInt(oneRePaymentDataInfo.getLoanDay()), payDate, toDay);			// 2. 연체이자구하기
//        	} else {
//        		// 기한이익상실
//        		overDueState = 2;
//        		overDuePayment = createPrePaymentInterest(Double.parseDouble(balance)
//	    				, loanRate
//	    				, Integer.parseInt(oneRePaymentDataInfo.getLoanDay()), payDate, toDay);
//        	}
//        	
//        	// 상환테이블 업데이트
//        	if(dateDiff < 0 && Integer.parseInt(rDelqAmount) == 0) {
//    	        	JSONObject jsonItem = new JSONObject();
//    	        	jsonItem.put("payCount", payCount);
//    	        	jsonItem.put("overDueState", overDueState);
//    	        	jsonItem.put("prePayment", "0");
//    	        	jsonItem.put("overDuePayment", "0");
//    	        	jsonItem.put("interest", interest);
//    	        	if(overDueState == 2)
//    	        		jsonItem.put("prePayment", overDuePayment);
//    	        	else
//    	        		jsonItem.put("overDuePayment", overDuePayment);
//    	        	jsonResult.put(jsonItem);
//        	} else if(dateDiff >= 0) {
//    	        	JSONObject jsonItem = new JSONObject();
//    	        	jsonItem.put("payCount", payCount);
//    	        	jsonItem.put("overDueState", overDueState);
//    	        	jsonItem.put("prePayment", "0");
//    	        	jsonItem.put("overDuePayment", "0");
//    	        	jsonItem.put("interest", interest);
//    	        	if(overDueState == 2)
//    	        		jsonItem.put("prePayment", overDuePayment);
//    	        	else
//    	        		jsonItem.put("overDuePayment", overDuePayment);
//    	        	jsonResult.put(jsonItem);
//        	}
//        }
//        
////        OneRestOverdueBalance oneRestOverdueBalance = repaymentService.selectRestOverdueBalance(loanId);
////        
//        long overDueSum = 0;//Long.parseLong(oneRestOverdueBalance.getDelqPay());
//        long prePaySum = 0;//Long.parseLong(oneRestOverdueBalance.getPrePay());
//        long interestSum = 0;
//        
//        for(int j = 0; j < jsonResult.length(); j++) {
//    		JSONObject jsonItem = jsonResult.getJSONObject(j);
//    		String overDuePayment = jsonItem.getString("overDuePayment");
//    		String prePayment = jsonItem.getString("prePayment");
//    		interestSum += Long.parseLong(jsonItem.getString("interest"));
//    		
//    		overDueSum += Long.parseLong(overDuePayment);
//    		prePaySum += Long.parseLong(prePayment);
//    	}
//        
//        if(interestSum < 0)
//        	interestSum = 0;
//        
//        Map<String, Object> result = new HashMap<String, Object>();
//    	result.put("overDue", overDueSum);
//    	result.put("prePay", prePaySum);
//    	result.put("interest", interestSum);
//    	result.put("balance", balance);
//    	response.setResult(result);
//    	
//    	commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
//        
//        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);        
//    }

    // 200428 수정버전
    @ApiOperation(value = "중도상환조회")
    @RequestMapping("/prepayment/schedule/get")
    public ResponseEntity<ResponseResult> getPrePaymentSchedule(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = new JSONObject(requestString).getJSONObject("request");
        
        String loanId = jsonRequest.getString("loanId");
        String prepayDate = jsonRequest.getString("preDate");
        
        
        OneRepaymentDataInfo oneRePaymentDataInfo = repaymentService.selectRepaymentDataInfo(loanId);
        double overRate = 3;

        JSONArray jsonResult = new JSONArray();
        double balance = repaymentService.selectGihanRepayment(loanId); 							// 상환하지 않은 원금 선택
        
        if(balance == 0) {
        	response.setState(445);
            response.setMessage("상환스케줄이 존재하지 않습니다.");
            
//            
            commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
            
            return new ResponseEntity<ResponseResult>(response, HttpStatus.OK); 
        }
        
        // 1. 비연체(결제당일포함), 2. 연체, 3.기한이익
        // balance : 원금, interest : 정상이자+자투리이자, 연체 : overDue, 기한이익 : prePay 
        
    	List<OneRepaymentScheduleItem> oneRepaymentScheduleItems = repaymentService.selectRepaymentScheduleItem(loanId, prepayDate);	// crs-검색일자 이전 상환하지 않은 정보선택(id, 회차, 납부예정일, 원금등등)
    	
    	// 자투리이자 구하기 위한 일수 
    	String recentCountDate = repaymentService.selectRecentCountRepaymentDate(loanId, prepayDate);
    	
    	if(recentCountDate == null) {
    		recentCountDate = repaymentService.selectLaonExecuteDate(loanId);							// 없으면 대출 실행일기준
    		if(recentCountDate == null) {
        		Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				recentCountDate = sdf.format(cal.getTime());
    		}
    	}
    	
    	long dateDiff = getDiffOfDate(recentCountDate, prepayDate);
    	// 상환예정일까지 자투리 정상이자
    	double interestRest = getDelqAmt(balance, Double.parseDouble(oneRePaymentDataInfo.getInterestRate())*0.01, 0*0.01, 365, dateDiff);
    	
    	// 1. 비연체
        if(oneRepaymentScheduleItems.size() < 1) {
        	
        	JSONObject jsonItem = new JSONObject();
        	jsonItem.put("payCount", "0");
        	jsonItem.put("overDueState", "0");
        	jsonItem.put("prePayment", "0");
        	jsonItem.put("overDuePayment", "0");
        	jsonItem.put("interest", interestRest);
        	jsonResult.put(jsonItem);
        }
        
        // 요청한 날짜가 오늘 이후이면 이후 날짜 계산
        Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(cal.getTime());
		

        
    	// 2. 연체 및 기한이익
        for (int i=0; i<oneRepaymentScheduleItems.size(); i++) {
        	double interest=0;
        	double overDue=0;
            double prePay = 0;
        	 
        	if(oneRepaymentScheduleItems.get(i).getDelqState().equals("1")) { 				// 연체
        		
	        	// 정상이자
        		interest = Double.parseDouble(oneRepaymentScheduleItems.get(i).getInterestAmount());
	        	

        		if (oneRepaymentScheduleItems.size() != i+1) {	// 마지막회차가 아닌경우

        			// 연체이자
           		 	overDue = Double.parseDouble(oneRepaymentScheduleItems.get(i).getRDelqAmount());

        		} else {
        	    	// 최근 상환예정일부터 상환일까지 자투리 정상이자(월불입금에 대한 이자가 아닌, 남아 있는 대출잔액에 대한 사용일까지의 이자임)
        	    	interest += getDelqAmt(balance, 
        	    			Double.parseDouble(oneRePaymentDataInfo.getInterestRate())*0.01, 0*0.01, 365, dateDiff);
        	        
        	    	// 현시점부터 상환일까지 자투리 연체이자
        	    	// dateDiff = getDiffOfDate(today, prepayDate);
        	    	overDue += getDelqAmt(Double.parseDouble(oneRepaymentScheduleItems.get(i).getPayAmount()), 
        	    			Double.parseDouble(oneRePaymentDataInfo.getInterestRate())*0.01, overRate*0.01, 365, dateDiff);

        		}

        	} else if (oneRepaymentScheduleItems.get(i).getDelqState().equals("2")) {		// 기한이익
 
        		if (oneRepaymentScheduleItems.size() != i+1) {	// 마지막회차가 아닌경우
            		// 기한이익상실값
            		prePay = Double.parseDouble(oneRepaymentScheduleItems.get(i).getRDelqAmount());
        		} else {
        	    	// 상환일까지 자투리 기한이익이자
        			prePay = getDelqAmt(balance, Double.parseDouble(oneRePaymentDataInfo.getInterestRate())*0.01, overRate*0.01, 365, dateDiff);
        		}
        	}
        	
        	JSONObject jsonItem = new JSONObject();
        	jsonItem.put("payCount", oneRepaymentScheduleItems.get(i).getCount());
        	jsonItem.put("overDueState", oneRepaymentScheduleItems.get(i).getDelqState());
        	jsonItem.put("prePayment", Math.floor(prePay));
        	jsonItem.put("overDuePayment", Math.floor(overDue));					
        	jsonItem.put("interest", Math.floor(interest));
        	jsonResult.put(jsonItem);
        }
        
        
//        System.out.println(jsonResult.toString());
        
        double interestSum = 0;
        double overDueSum = 0;
        double prePaySum = 0;
		
		      
		for(int j = 0; j < jsonResult.length(); j++) {
			JSONObject jsonItem = jsonResult.getJSONObject(j);
			interestSum += jsonItem.getDouble("interest");
			overDueSum += jsonItem.getDouble("overDuePayment");
			prePaySum += jsonItem.getDouble("prePayment");
		}
	  
		if(interestSum < 0)
			interestSum = 0;
	  
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("overDue", Math.floor(overDueSum));						// 연체이자합
		result.put("prePay", Math.floor(prePaySum));						// 기한이익합
		result.put("interest", Math.floor(interestSum));					// 정상이자합
		result.put("balance", balance);										// 원금
		response.setResult(result);
		
//		System.out.println(response.toString());
		
		// 처리해야함!
//
		commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
  
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);        
    }

    
    
//    @Transactional("oneTransactionManager")
//    @ApiOperation(value = "중도상환 등록")
//    @RequestMapping("/prepayment/schedule/send")
//    public ResponseEntity<ResponseResult> sendPrePayment(@RequestBody String requestString) throws Exception {
//    	
//    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
//        commonUtil.sendRequestLogging(mapping_url, requestString);
//    	
//        ResponseResult response = new ResponseResult();
//        response.setState(200);
//        response.setMessage("정상적으로 처리하였습니다.");
//        
//        JSONObject jsonRequest = new JSONObject(requestString).getJSONObject("request");
//        
//        String loanId = jsonRequest.getString("loanId");
//        String overDue = jsonRequest.getString("overDue");
//        String prePay = jsonRequest.getString("prePay");
//        String loanInterest = jsonRequest.getString("interest");
//        String balance = jsonRequest.getString("balance");
//        
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//        String todayDate = formatter.format(new Date());
//        
//        double taxRate = 0.25;
//		double taxRateLocal = 0.025;
//		
//		if(todayDate.split("-")[0].equals("2020")) {
//			taxRate = 0.14;
//			taxRateLocal = taxRate * 0.1;
//		}
//		
//		OnePrePayment onePrePayment = new OnePrePayment();
//		onePrePayment.setLoanId(loanId);
//		onePrePayment.setInterest(loanInterest);
//		onePrePayment.setOverdue(overDue);
//		onePrePayment.setPrepay(prePay);
//		onePrePayment.setBalance(balance);
//
//		double feeRate = 0.024; 
//		
//		String checkOverdue = repaymentService.selectCheckOverdue(loanId);
//		
//		if(checkOverdue == null)
//			feeRate = 0;
//		
//		if(repaymentService.insertPrePayment(onePrePayment)) {					// cpas_prepayment에 정보삽입
//			List<OnePaymentInvestSchedule> onePaymentInvestSchedules = scheService.selectPaymentInvestSchedule(loanId);
//			
//			for(int i = 0; i < onePaymentInvestSchedules.size(); i++) {
//				String mid = onePaymentInvestSchedules.get(i).getMid();
//			
//				double loanRate = Double.parseDouble(onePaymentInvestSchedules.get(i).getPay()) / Double.parseDouble(onePaymentInvestSchedules.get(i).getLoanPay());
////				long loanPayment = Long.parseLong(overDue) + Long.parseLong(prePay) + Long.parseLong(balance) + Long.parseLong(loanInterest);
//				long sumInterest = Long.parseLong(overDue) + Long.parseLong(prePay) + Long.parseLong(loanInterest);
//				
//				double inAmount = Long.parseLong(balance) * loanRate;
//				double interest = (Double.parseDouble(loanInterest) + Double.parseDouble(overDue) + Long.parseLong(prePay)) * loanRate;		// cpp에 이자는 연체이자 포함
//				
//				double taxPay = (interest * taxRate);
//				double taxPayLocal = (interest * taxRateLocal);
//				
//				if(Integer.parseInt(onePaymentInvestSchedules.get(i).getLevel()) == 4) {
//    				if(taxPay <= 1000) {
//    					taxPay = 0;
//    					taxPayLocal = 0;
//	    			}
//    			}
//				
//				long tax_real = ((long)Math.floor(taxPay) / 10) * 10;
//				long tax_local_real = ((long)Math.floor(taxPayLocal) / 10) * 10;
//				
//				
//				double result = (double)(inAmount + (sumInterest - (tax_real + tax_local_real))) * loanRate;
//				double fee = result * feeRate;
//				
//				if(interest == 0)
//					fee = 0;
//																														// cpp-투자자에게 지급될 실제금액(원금+이자-세금 빠진 값)
//				long investAmount = ((long)Math.ceil(inAmount) + (long)Math.ceil(interest) - ((long)Math.floor(fee) + tax_real + tax_local_real));
//	        
//				OnePrePaymentProvide onePrePaymentProvide = new OnePrePaymentProvide();
//				onePrePaymentProvide.setMid(mid);
//				onePrePaymentProvide.setLoanId(loanId);
//				onePrePaymentProvide.setInterest(String.valueOf(Math.ceil(interest)));
//				onePrePaymentProvide.setFee(String.valueOf(Math.floor(fee)));
//				onePrePaymentProvide.setTax(String.valueOf(tax_real));
//				onePrePaymentProvide.setTaxLocal(String.valueOf(tax_local_real));
//				onePrePaymentProvide.setPayAmount(String.valueOf(investAmount));
//				
//				if(!repaymentService.insertPrePaymentProvide(onePrePaymentProvide)) {
//					response.setState(602);
//			        response.setMessage("중도상환 지급 등록중 에러가 발생하였습니다.");
//				} else {
//					OneOrderPrePayment oneOrderPrePayment = repaymentService.selectOrderPrePaymentInfo(loanId, mid);
//					oneOrderPrePayment.setInterest(String.valueOf(Math.ceil(interest)));
//					oneOrderPrePayment.setPayAmount(String.valueOf(Math.ceil(inAmount) + Math.ceil(interest)));
//					oneOrderPrePayment.setLnAmount(String.valueOf(investAmount));
//					
//					repaymentService.insertOrderPrePayment(repaymentService.selectOrderPrePaymentInfo(loanId, mid));
//				}
//			}
//			
//			repaymentService.updatePaymentScheduleState(loanId);						// cps-p_pay_status=N을 P처리
//		} else {
//			response.setState(601);
//	        response.setMessage("중도상환 등록중 에러가 발생하였습니다.");
//		}
//		
//		commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
//        
//        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);        
//    }
//    
    
    @Transactional("oneTransactionManager")
    @ApiOperation(value = "중도상환 등록")
    @RequestMapping("/prepayment/schedule/send")
    public ResponseEntity<ResponseResult> sendPrePayment(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = new JSONObject(requestString).getJSONObject("request");
        
        String loanId = jsonRequest.getString("loanId");
        String overDue = jsonRequest.getString("overDue");
        String prePay = jsonRequest.getString("prePay");
        String loanInterest = jsonRequest.getString("interest");
        String balance = jsonRequest.getString("balance");
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String todayDate = formatter.format(new Date());
        
        double taxRate = 0.25;
		double taxRateLocal = 0.025;
		
		if(todayDate.split("-")[0].equals("2020")) {
			taxRate = 0.14;
			taxRateLocal = taxRate * 0.1;
		}
		
		OnePrePayment onePrePayment = new OnePrePayment();
		onePrePayment.setLoanId(loanId);
		onePrePayment.setInterest(loanInterest);
		onePrePayment.setOverdue(overDue);
		onePrePayment.setPrepay(prePay);
		onePrePayment.setBalance(balance);

		double feeRate = 0.024; 
		
		String checkOverdue = repaymentService.selectCheckOverdue(loanId);
		
		if(checkOverdue == null)
			feeRate = 0;
		
		if(repaymentService.insertPrePayment(onePrePayment)) {					// cpas_prepayment에 정보삽입
			List<OnePaymentInvestSchedule> onePaymentInvestSchedules = scheService.selectPaymentInvestSchedule(loanId);
			
			List<OneSendCheckingEmail> oneSendCheckingEmailList = new ArrayList<OneSendCheckingEmail>();	// 메일 보내기 위한 리스트
			
			for(int i = 0; i < onePaymentInvestSchedules.size(); i++) {
				String mid = onePaymentInvestSchedules.get(i).getMid();
			
				double loanRate = Double.parseDouble(onePaymentInvestSchedules.get(i).getPay()) / Double.parseDouble(onePaymentInvestSchedules.get(i).getLoanPay());
//				long loanPayment = Long.parseLong(overDue) + Long.parseLong(prePay) + Long.parseLong(balance) + Long.parseLong(loanInterest);
				long sumInterest = Long.parseLong(overDue) + Long.parseLong(prePay) + Long.parseLong(loanInterest);

				// 200810 연체이자분리, 연체 정산 스케줄에 따라서 정상이자는 올림, 연체이자는 소숫점 반올림으로 처리  
				double interestNormal = Math.ceil((Double.parseDouble(loanInterest)) * loanRate);
				double interestOverDue = Math.round(Double.parseDouble(overDue) * loanRate);	
				double interestGihan = Math.round(Long.parseLong(prePay) * loanRate);
				double interest = interestNormal + interestOverDue + interestGihan;		// cpp에 이자는 연체이자 포함 /**/
				
				double inAmount = Long.parseLong(balance) * loanRate;
//				double interest = (Double.parseDouble(loanInterest) + Double.parseDouble(overDue) + Long.parseLong(prePay)) * loanRate;		// cpp에 이자는 연체이자 포함 /**/
				
				double taxPay = (interest * taxRate);
				double taxPayLocal = (interest * taxRateLocal);
				
				if(Integer.parseInt(onePaymentInvestSchedules.get(i).getLevel()) == 4) {
        			// 200429 세금이 1000원일 경우도 절삭으로 적용
    				if(taxPay <= 1000) {
    					taxPay = 0;
    					taxPayLocal = 0;
	    			}
    			}
				
				long tax_real = ((long)Math.floor(taxPay) / 10) * 10;
				long tax_local_real = ((long)Math.floor(taxPayLocal) / 10) * 10;
				
				double result = (double)(inAmount + (sumInterest - (tax_real + tax_local_real))) * loanRate;
		
//				double fee = result * feeRate;
//				
//				if(interest == 0)
//					fee = 0;
			
				List<OneOverdueNumberOfCount> overdueNumberOfCount = scheService.selectOverdueNumberOfCount(loanId, onePaymentInvestSchedules.get(i).getMid());
				
				// 연체했던 회차 잔여투자금 선택
				long investBalance = 0;
				double fee = 0;
				for(int j=0; j<overdueNumberOfCount.size(); j++) {
					investBalance = scheService.selectPreFeeInfo(loanId, overdueNumberOfCount.get(j).getMid(), overdueNumberOfCount.get(j).getMinCount());
					fee += investBalance * 0.002;
				}
				
				// 
				
						
				long investAmount = ((long)Math.ceil(inAmount) + (long)Math.ceil(interest) - ((long)Math.floor(fee) + tax_real + tax_local_real));
	        
				OnePrePaymentProvide onePrePaymentProvide = new OnePrePaymentProvide();
				onePrePaymentProvide.setMid(mid);
				onePrePaymentProvide.setLoanId(loanId);
				onePrePaymentProvide.setInterest(String.valueOf(Math.ceil(interest)));
				onePrePaymentProvide.setInterestNormal(String.valueOf(interestNormal));
				onePrePaymentProvide.setInterestOverDue(String.valueOf(interestOverDue));
				onePrePaymentProvide.setInterestGihan(String.valueOf(interestGihan));
				onePrePaymentProvide.setFee(String.valueOf(Math.floor(fee)));
				onePrePaymentProvide.setTax(String.valueOf(tax_real));
				onePrePaymentProvide.setTaxLocal(String.valueOf(tax_local_real));
				onePrePaymentProvide.setPayAmount(String.valueOf(investAmount));
				
				if(!repaymentService.insertPrePaymentProvide(onePrePaymentProvide)) {
					response.setState(602);
			        response.setMessage("중도상환 지급 등록중 에러가 발생하였습니다.");
				} else {
					OneOrderPrePayment oneOrderPrePayment = repaymentService.selectOrderPrePaymentInfo(loanId, mid);
					oneOrderPrePayment.setInterest(String.valueOf(Math.ceil(interest)));
					oneOrderPrePayment.setPayAmount(String.valueOf(Math.ceil(inAmount) + Math.ceil(interest)));
					oneOrderPrePayment.setLnAmount(String.valueOf(investAmount));
					
					repaymentService.insertOrderPrePayment(repaymentService.selectOrderPrePaymentInfo(loanId, mid));
				}
			
				OneSendCheckingEmail oneSendCheckingEmail = new OneSendCheckingEmail();
				oneSendCheckingEmail.setLoanId(loanId);
				oneSendCheckingEmailList.add(oneSendCheckingEmail);
				
				
			}
			
			repaymentService.updatePaymentScheduleState(loanId);						// cps-p_pay_status=N을 P처리
			
			
			
			commonUtil.sendCheckManager(oneSendCheckingEmailList, "중도상환이다!");												// 메일발송 테이블에 쌓는 메서드 호출
	    	
			
		} else {
			response.setState(601);
	        response.setMessage("중도상환 등록중 에러가 발생하였습니다.");
		}
		
		commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);        
    }

    
    private String createPrePaymentInterest(double loanAmt, double interest, int loanPeriod, String startDate, String endDate) {
    	long diffDate = getDiffOfDate(startDate, endDate);
		long loanInterest = (long) Math.floor(loanAmt * interest * ((double)diffDate/365));
    	return String.valueOf((long)loanInterest);
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
    
    public int getDateDay(String date) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd") ;
        Date nDate = dateFormat.parse(date) ;
         
        Calendar cal = Calendar.getInstance() ;
        cal.setTime(nDate);
         
        int dayNum = cal.get(Calendar.DAY_OF_WEEK) ;
        
        return dayNum;
    }
    
    //200501 createPrePaymentInterest 대신 사용
	private double getDelqAmt(double payAmt, double rate, double overRate, int daysOfYear, long diffDate) {
    	return payAmt * ((rate+overRate) / daysOfYear) * diffDate;
	}

}