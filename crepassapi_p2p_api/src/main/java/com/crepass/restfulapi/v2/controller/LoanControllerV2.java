package com.crepass.restfulapi.v2.controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.cre.service.CreMemberService;
import com.crepass.restfulapi.one.domain.OneCertify;
import com.crepass.restfulapi.one.service.LoanService;
import com.crepass.restfulapi.one.service.OneMemberService;
import com.crepass.restfulapi.v2.domain.LoanRepayAccntItem;
import com.crepass.restfulapi.v2.domain.LoanScheduleInfo;
import com.crepass.restfulapi.v2.domain.LoanScheduleItem;
import com.crepass.restfulapi.v2.domain.LoanStepInfo;
import com.crepass.restfulapi.v2.domain.PaymentHistoryInfo;
import com.crepass.restfulapi.v2.domain.PaymentHistoryItem;
import com.google.gson.Gson;

import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@RequestMapping(path = "/api2", method = {RequestMethod.POST, RequestMethod.GET})
public class LoanControllerV2 {
	
	@Autowired
	private LoanService loanService;
	
	@Autowired
	private OneMemberService oneMemberService;
	
	@Autowired
	private CreMemberService creMemberService;
	
	@Autowired
    private CommonUtil commonUtil;
	
	@Autowired(required=true)
	private HttpServletRequest request;
	
	@Value("${paging.offset}")
	private int rowCount;
	
	@ApiOperation(value = "대출현황 리스트")	// 채권리스트 아님
    @RequestMapping("/loan/loanList")
	public ResponseEntity<ResponseResult> getInvestLoanList(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
		ResponseResult response = new ResponseResult();
		
		JSONObject json = new JSONObject(requestString);
		JSONObject jsonRequest = (JSONObject) json.get("request");
		int pageNum = jsonRequest.getInt("pageNum");
		String mid = jsonRequest.getString("mid");
		String loanCode = jsonRequest.getString("loanCode");	// A=전체 / S=펀딩중 / R=상환중 / P=상환완료
		String keyword = "";
		
		loanService.updateLoanCond(mid);
		
		if(jsonRequest.has("keyword"))
			keyword = jsonRequest.getString("keyword");
		
		int loanListCount = loanService.selectLoanListCount(loanCode, mid, keyword);
		
		int offSetNum = (pageNum - 1) * rowCount;
		int totCount = loanListCount;
		int totPageCount = (int) Math.ceil((float)totCount/rowCount);
		
		List<Map<String,Object>> loanList = loanService.selectLoanList(offSetNum, rowCount, loanCode, mid, keyword);

		if(loanList == null)
			loanList = new ArrayList<>();
		
		Map<String,Object> result = new HashMap<>();
		result.put("list", loanList);
		result.put("totPageCount", totPageCount);
		
		response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        response.setResult(result);
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
		return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
	}
	
	
	@ApiOperation(value = "대출현황 상세조회")
    @RequestMapping("/loan/loanItem")
	public ResponseEntity<ResponseResult> getInvestLoanItem(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
		ResponseResult response = new ResponseResult();
		
		JSONObject json = new JSONObject(requestString);
		JSONObject jsonRequest = json.getJSONObject("request");
		int loanId = jsonRequest.getInt("loanId");
		
		Map<String,Object> loanItem = loanService.selectLoanItem(loanId); // 대출상품
		
		List<Map<String,String>> investorList = loanService.selectInvestorList(loanId); // 투자자리스트
		if(investorList == null) 
			investorList = new ArrayList<>();
		
		loanItem.put("list", investorList);
		
		response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        response.setResult(loanItem);
		
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
		return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
	}
	
	@ApiOperation(value = "본인 확인 및 대출중복 검사")
    @RequestMapping("/loan/user/checked")
	@Transactional("oneTransactionManager")
    public ResponseEntity<ResponseResult> getCheckUserLoan(@RequestBody String requestString, HttpServletRequest request) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, new Gson().toJson(requestString));
        
        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject json = new JSONObject(requestString);
        
        if(json.has("request") && json.getJSONObject("request").has("mid") && json.getJSONObject("request").has("birth") && json.getJSONObject("request").has("newsagency")
        		 && json.getJSONObject("request").has("hp") && json.getJSONObject("request").has("mname")
        		 && json.getJSONObject("request").has("certifyType")  && json.getJSONObject("request").has("certifyResult")) {

        	JSONObject jsonRequest = json.getJSONObject("request");
        	String mid = jsonRequest.getString("mid");
        	String birth = jsonRequest.getString("birth");
        	String newsagency = jsonRequest.getString("newsagency");
        	String hp = jsonRequest.getString("hp");
        	String mname = jsonRequest.getString("mname");
        	String certifyType = jsonRequest.getString("certifyType");
        	String certifyResult = jsonRequest.getString("certifyResult");
        	
        	String mno = loanService.selectMemberIsChecked(mid, mname, birth, hp, newsagency);
        	
        	if(mno == null) {
        		response.setState(429);
                response.setMessage("본인 인증정보와 가입한 정보가 일치하지 않습니다.\n고객센터에 문의하세요.");
        	} else {
	        	String chekedLoan = loanService.selectLoanIsChecked(mid);
	        	
	        	if(chekedLoan == null || chekedLoan.isEmpty() || Integer.parseInt(chekedLoan) < 1) {
	        		OneCertify oneCertify = new OneCertify();
	                oneCertify.setMno(mno);
	                oneCertify.setCertifyType(certifyType);
	                oneCertify.setCertifyResult(certifyResult);
	                oneMemberService.insertOneCertify(oneCertify);
	        	} else {
	        		response.setState(428);
	                response.setMessage("이미 심사중인 대출건이 있습니다.");
	        	}
        	}
            
        } else {
        	response.setState(332);
        	response.setMessage("Parameter Not Found");
        }
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
	
	// ml, clss, ceh, crep2p_agreed_history insert
	@ApiOperation(value = "대출등록(multipart)")
    @RequestMapping("/loan/add")
	@Transactional("oneTransactionManager")
    public ResponseEntity<ResponseResult> addLoan(@ModelAttribute LoanStepInfo loanStepInfo, HttpServletRequest request) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, new Gson().toJson(loanStepInfo));
        
        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        if(loanStepInfo != null && loanStepInfo.getMid() != null && loanStepInfo.getBirth() != null && loanStepInfo.getGender() != null
        		&& loanStepInfo.getNewsagency() != null && loanStepInfo.getLoanStep01Item() != null && loanStepInfo.getLoanStep03Item() != null
        		&& loanStepInfo.getAgreement() != null 
        		&& loanStepInfo.getLoanStep01Item().getAttention() != null && loanStepInfo.getLoanStep01Item().getJobName() != null
        		&& loanStepInfo.getLoanStep01Item().getLoanDay() != null && loanStepInfo.getLoanStep01Item().getLoanPay() != null
        		&& loanStepInfo.getLoanStep01Item().getLoanPose() != null && loanStepInfo.getLoanStep01Item().getRepayDay() != null
        		&& loanStepInfo.getLoanStep01Item().getRepayWay() != null && loanStepInfo.getLoanStep03Item().getPostCode() != null
				&& loanStepInfo.getLoanStep03Item().getAddress() != null && loanStepInfo.getLoanStep03Item().getAddressDetail() != null 
				&& loanStepInfo.getLoanStep03Item().getEmergencyList() != null && loanStepInfo.getLoanStep03Item().getRepayPlan() != null
        		&& loanStepInfo.getLoanStep03Item().getIsEmail() != null && loanStepInfo.getLoanStep03Item().getIsPone() != null
        		&& loanStepInfo.getLoanStep03Item().getIsSMS() != null && loanStepInfo.getLoanStep03Item().getRepayJob() != null ) {
        	
        	String custId = oneMemberService.selectCustID(loanStepInfo.getMid());					// mid로 custId선택
            boolean isLoanMultipart = loanService.addLoanMultipart(loanStepInfo, custId);			// mari_loan insert하러 가는 메서드
            
            if(!isLoanMultipart) {
            	response.setState(333);
            	response.setMessage("예치기 못한 문제가 발생하여 대출등록이 불가합니다. 잠시 후 다시 시도해주세요.");
            } else {
            	creMemberService.addCreAgreeMember3(loanStepInfo.getAgreement(), custId);
            }
            
        } else {
        	response.setState(332);
        	response.setMessage("Parameter Not Found");
        }
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
	
	@ApiOperation(value = "대출 상환 스케줄 정보")
    @RequestMapping("/loan/schedule/info")
    public ResponseEntity<ResponseResult> getLoancheduleInfo(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.getJSONObject("request");
        final String loanId = jsonRequest.getString("loanId");
        
        LoanScheduleInfo loanScheduleInfo = loanService.selectLoanScheduleInfo(loanId);
        List<LoanScheduleItem> loanScheduleItems = loanService.selectLoanScheduleList(loanId);
        loanScheduleInfo.setList(loanScheduleItems);
        
        response.setResult(loanScheduleInfo);
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
	
	@ApiOperation(value = "대출 상환계좌 내역")
    @RequestMapping("/loan/repay/accnt/info")
    public ResponseEntity<ResponseResult> getLoanRepayAccntInfo(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.getJSONObject("request");
        final String mid = jsonRequest.getString("mid");
        
        List<LoanRepayAccntItem> loanRepayAccntItems = loanService.selectLoanRepayAccntList(mid);
        Map<String,Object> result = new HashMap<>();
		result.put("list", loanRepayAccntItems);

        response.setResult(result);
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
	
	@ApiOperation(value = "대출자 상환금 내역")
    @RequestMapping("/loan/payment/history")
    public ResponseEntity<ResponseResult> getloanPaymentHistory(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.getJSONObject("request");
        final String loanId = jsonRequest.getString("loanId");
        int pageNum = jsonRequest.getInt("pageNum");
        
        int listSize = loanService.selectLoanPaymentHistoryItemSize(loanId);
        
        int offSetNum = (pageNum - 1) * rowCount;
		int totCount = listSize;
		int totPageCount = (int) Math.ceil((float)totCount/rowCount);
        
        PaymentHistoryInfo loanPaymentHistoryInfo = loanService.selectLoanPaymentHistoryInfo(loanId);
        List<PaymentHistoryItem> loanPaymentHistoryItems = loanService.selectLoanPaymentHistoryItem(offSetNum, rowCount, loanId);
        
        if(loanPaymentHistoryItems == null)
        	loanPaymentHistoryItems = new ArrayList<>();
        
        if(loanPaymentHistoryInfo == null)
        	loanPaymentHistoryInfo = new PaymentHistoryInfo();
        
        if(Long.parseLong(loanPaymentHistoryInfo.getTrxAmt()) < 0)
        	loanPaymentHistoryInfo.setTrxAmt("0");
        
		loanPaymentHistoryInfo.setList(loanPaymentHistoryItems);
		loanPaymentHistoryInfo.setTotPageCount(totPageCount);
        
        response.setResult(loanPaymentHistoryInfo);
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
	
	@ApiOperation(value = "대출 약정서 스텝 01")
    @RequestMapping("/loan/contract/step/01")
    public ResponseEntity<ResponseResult> getLoanContractStep01(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.getJSONObject("request");
        final String loanId = jsonRequest.getString("loanId");
        
        response.setResult(loanService.selectLoanContractStep01(loanId));
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
	
	@ApiOperation(value = "대출 약정서 스텝 02")
    @RequestMapping("/loan/contract/step/02")
    public ResponseEntity<ResponseResult> getLoanContractStep02(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.getJSONObject("request");
        final String loanId = jsonRequest.getString("loanId");
        
        response.setResult(loanService.selectLoanContractStep02(loanId));
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
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
