package com.crepass.restfulapi.one.controller;

import com.crepass.restfulapi.common.AES256Cipher;
import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.inside.service.DepositService;
import com.crepass.restfulapi.one.domain.OneLoan;
import com.crepass.restfulapi.one.domain.OneLoanCategory;
import com.crepass.restfulapi.one.domain.OneLoanContract;
import com.crepass.restfulapi.one.domain.OneLoanDataInfo;
import com.crepass.restfulapi.one.domain.OneLoanHeart;
import com.crepass.restfulapi.one.domain.OneLoanInvestInfoDetail;
import com.crepass.restfulapi.one.domain.OneLoanMemo;
import com.crepass.restfulapi.one.domain.OneLoanMemoHeart;
import com.crepass.restfulapi.one.domain.OneLoanMemoInfo;
import com.crepass.restfulapi.one.domain.OneLoanRepaymentSchedule2;
import com.crepass.restfulapi.one.domain.OneLoanTelecomConfirm;
import com.crepass.restfulapi.one.domain.OneNotiRepaymentUserInfo;
import com.crepass.restfulapi.one.domain.OneOrderDataInfo;
import com.crepass.restfulapi.one.domain.OneRepaymentCheckCount;
import com.crepass.restfulapi.one.domain.OneRepaymentDataInfo;
import com.crepass.restfulapi.one.domain.OneSendEmail;
import com.crepass.restfulapi.one.service.LoanService;
import com.crepass.restfulapi.one.service.NotifyScheService;
import com.crepass.restfulapi.one.service.OneMemberService;
import com.crepass.restfulapi.one.service.RepaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.swagger.annotations.ApiOperation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
public class SendEmailController {

	@Autowired
    private LoanService loanService;
	
	@Autowired
    private CommonUtil commonUtil;
    
	@Autowired
    private OneMemberService oneMemberService;

	@Autowired
    private RepaymentService repaymentService;
	
	@Autowired
    private DepositService depositService;
	
	@Autowired
	private NotifyScheService notifyScheService;
	
	@Value("${crepas.url.myloan}")
    private String myLoanUrl;
	
	@Value("${crepas.sms.url}")
    private String smsUrl;
	
	@Autowired(required=true)
	private HttpServletRequest request;
	
	
	/////////////////////////////////////////////////
	//		type : 1(개인), 2(전체)
	//	 {
	//		 "request":{
	//		 	"type":"1",
	//		 	"target":"igothewar@naver.com",
	//		 	"wishdate":"2020-04-04-09:00:00",
	//		 	"title":"온라인서비스 이용약관 및 투자 이용약관 개정 안내",
	//		 	"contents":"aaaaaaaaaaaaaaaaaa"
	//		 }
	//	 }
	//	 
	//	 {
	//		 "request":{
	//		 	"type":"2",
	//		 	"target":"all",
	//		 	"wishdate":"2020-04-04-09:00:00",
	//		 	"title":"온라인서비스 이용약관 및 투자 이용약관 개정 안내",
	//		 	"contents":"aaaaaaaaaaaaaaaaaa"
	//		 }
	//	 }
	//	
	///////////////////////////////////////////////
	
    @ApiOperation(value = "예약메일발송 컨트롤러") 
    @RequestMapping("/email")
    public ResponseEntity<ResponseResult> bookingEmail(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String type = jsonRequest.get("type").toString();
        final String target = jsonRequest.get("target").toString();
        final String wishdate = jsonRequest.get("wishdate").toString();
        final String title = jsonRequest.get("title").toString();
  //      final String contents = jsonRequest.get("contents").toString();
        
        List<OneSendEmail> oneSendEmailList = new ArrayList<OneSendEmail>();
        
        if( (type != null) && (target != null) && (wishdate != null) ) {
        	
        	if (type.equals("1")) {

        		JSONObject jsonTitle = new JSONObject();
				jsonTitle.put("mailTitle", title);
				
				OneSendEmail oneSendEmail = new OneSendEmail();
				oneSendEmail.setM_id(target);
				oneSendEmailList.add(oneSendEmail);
				
				//temp
				OneSendEmail oneSendEmail1 = new OneSendEmail();
				oneSendEmail1.setM_id("jhlee@crepass.com");
				oneSendEmailList.add(oneSendEmail1);
				
				JSONArray jsonBody = new JSONArray();
				JSONObject body = new JSONObject();
	        	
//				String emailBody = commonUtil.setHtmlInfoMailForm();
				String emailBody = commonUtil.setHtmlInfoMailForm_temp();
				
				
				body.put("keyValue", emailBody);
				jsonBody.put(body);
				
				commonUtil.sendLoggingEmail5("["+jsonTitle.getString("mailTitle")+"]", wishdate, oneSendEmailList, emailBody);
			
        	} else if (type.equals("2") && target.equals("all")) {
        		

        		JSONObject jsonTitle = new JSONObject();
				jsonTitle.put("mailTitle", title);
				
        		oneSendEmailList = notifyScheService.selectAllUserEmailList();
        		
        		JSONArray jsonBody = new JSONArray();
				JSONObject body = new JSONObject();
	        	
				String emailBody = commonUtil.setHtmlInfoMailForm();
				
				body.put("keyValue", emailBody);
				jsonBody.put(body);
				
				commonUtil.sendLoggingEmail5("["+jsonTitle.getString("mailTitle")+"]", wishdate, oneSendEmailList, emailBody);
				
        	}
        	
        } else {
        	response.setState(366);
            response.setMessage("입력되지 않은 항목이 있습니다.");
        }
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    
    

    @ApiOperation(value = "정산처리건 수동으로 관리자에게 정보전달")
    @RequestMapping("/email/tome")
    public ResponseEntity<ResponseResult> sendingPosibleRepaymentEmail(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String target = jsonRequest.get("target").toString();
        
        if( ( target != null) ) {
        
        	Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
			cal.setTimeInMillis(cal.getTimeInMillis());
			
			String payDate = commonUtil.NextWorkingDayCalculate(sdf.format(cal.getTime()));
			
			while(payDate != null) {
				String[] payDates = payDate.split("-");
				cal.set(Calendar.YEAR, Integer.parseInt(payDates[0]));
				cal.set(Calendar.MONTH, Integer.parseInt(payDates[1]) - 1);
				cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(payDates[2]));
				cal.setTimeInMillis(cal.getTimeInMillis());
				payDate = commonUtil.NextWorkingDayCalculate(sdf.format(cal.getTime()));
			}
			
			if(payDate == null)
				payDate = sdf.format(cal.getTime());
			
			List<String> loanIds = notifyScheService.selectRepaymentByLoanId();				// cps-상환하지 않은 모든 loanId중, 가장 최근 회차 선택 
			
			JSONArray aryResult = new JSONArray();
			
			for(int i = 0; i < loanIds.size(); i++) {										// 검증에 대한 프로세서
				String loanId = loanIds.get(i);
				List<OneRepaymentCheckCount> oneRepaymentCheckCounts = repaymentService.selectRepaymentCheckCount3(loanId); // crs-대출자의 상환한 회차정보(원리금+연제이자) 모두선택  
				String depositPayment = getDepositPayment(loanId, payDate, oneRepaymentCheckCounts);	// 현재 지불해야할 총금액(?) 마이너스값
				
				long diffPay = Long.parseLong(depositPayment); 
				
				if(diffPay < 0) {															// 상환해야 할 금액이 있으면
					String balanceRepayment = notifyScheService.selectBalanceRepayment(loanId);	// cps-상환하지 않은 가장 최근 회차의 상환액 선택 
					
					if((Long.parseLong(balanceRepayment) + diffPay) <= 0) {					// 최근회차에 상환값보다 상환해야 할 금액이 여유있으면
						String isPossible = notifyScheService.selectNotiRepaymentIsPossible(loanId, payDate);	// cps-상환한 가장 최근 회차 부터 오늘까지의 날짜 차이 일수 선택
						if(isPossible == null)
							isPossible = "1";												// 이력이 없으면 1
						
						if(Integer.parseInt(isPossible) > 0) {
							OneNotiRepaymentUserInfo oneNotiRepaymentUserInfo = notifyScheService.selectNotiRepaymentUserInfo(loanId);	// ml-제목 선택
							JSONObject obRepayUserInfo = new JSONObject();
							obRepayUserInfo.put("title", oneNotiRepaymentUserInfo.getSubject());
							obRepayUserInfo.put("mid", oneNotiRepaymentUserInfo.getMid());
							obRepayUserInfo.put("name", oneNotiRepaymentUserInfo.getName());
							obRepayUserInfo.put("repayment", balanceRepayment);				// 상환금액
							obRepayUserInfo.put("deposit", (diffPay * -1));					// 입금잔액
							aryResult.put(obRepayUserInfo);
						}
					}
				}
			}
			
//			if(aryResult.length() > 0) {													// 메일 발송에 대한 프로세서
				String emailBody = "";

				JSONObject jsonTitle = new JSONObject();
				jsonTitle.put("mailTitle", "상환");
				jsonTitle.put("titleType", "처리가능건");
				jsonTitle.put("titleMsg", aryResult.length() + "건이 있습니다.");
				
				JSONArray jsonBody = new JSONArray();
				JSONObject body = new JSONObject();
				
				for(int i = 0; i < aryResult.length(); i++) {
					JSONObject item = aryResult.getJSONObject(i);
					
					body = new JSONObject();
					body.put("keyName", "제&nbsp;&nbsp;&nbsp;&nbsp;목");
					body.put("keyValue", item.getString("title"));
					jsonBody.put(body);
					
					body = new JSONObject();
					body.put("keyName", "계&nbsp;&nbsp;&nbsp;&nbsp;정");
					body.put("keyValue", item.getString("mid"));
					jsonBody.put(body);
					
					body = new JSONObject();
					body.put("keyName", "이&nbsp;&nbsp;&nbsp;&nbsp;름");
					body.put("keyValue", item.getString("name"));
					jsonBody.put(body);
					
					body = new JSONObject();
					body.put("keyName", "상환금액");
					body.put("keyValue", commonUtil.getAmountUnit2(Long.parseLong(item.getString("repayment"))));
					jsonBody.put(body);
					
					body = new JSONObject();
					body.put("keyName", "입금잔액");
					body.put("keyValue", commonUtil.getAmountUnit2(Long.parseLong(item.getString("deposit"))));
					jsonBody.put(body);
					
					body = new JSONObject();
					body.put("keyName", " ");
					body.put("keyValue", " ");
					jsonBody.put(body);
				}
				
				emailBody += commonUtil.setHtmlTableForm(jsonTitle, jsonBody);
				commonUtil.sendLoggingEmailTest("[수동알림]정산 가능한 건이 " + aryResult.length() + "건 있습니다.", emailBody, target);
//			}
        	
        	
    
        	
        } else {
        	response.setState(366);
            response.setMessage("입력되지 않은 항목이 있습니다.");
        }
        
//        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    

	private String getDepositPayment(String loanId, String payDate, List<OneRepaymentCheckCount> oneRepaymentCheckCounts) {
		try {																			// 대출기간, 이율, 계좌정보
			OneRepaymentDataInfo oneRePaymentDataInfo = repaymentService.selectRepaymentDataInfo(loanId);
			long totalDepositAmt = depositService.selectRepaymentTotalDepositAmt(oneRePaymentDataInfo.getLoanAccntNo());	//IB_FB_P2P_IP-대출자 입금합 선택
			//String count = "0";
	        
	    	for(int i = 0; i < oneRepaymentCheckCounts.size(); i++) {
	        	String payAmount = oneRepaymentCheckCounts.get(i).getPayAmount();
	        	totalDepositAmt -= Long.parseLong(payAmount);
	        	
				//if(totalDepositAmt < 0) {
				//	count = oneRepaymentCheckCounts.get(i).getCount();
				//}
	        	
	        	if(totalDepositAmt >= 0) {
	        		oneRepaymentCheckCounts.subList(i, i + 1).clear();
	        		i--;
	        	}
	    	}
	    	
	    	// 추가3줄(혹시 다른경로로 환불해준 금액이 있으면 해당금액 공제)
	    	String oneMid = repaymentService.selectOneMid(loanId);
	    	long totalRefundAmt = repaymentService.selectRepaymentTotalRefundAmt(oneMid, loanId);
	    	if((oneMid != null) && (totalRefundAmt != 0))
	    		totalDepositAmt -=  totalRefundAmt;
	    	
	    	
	    	totalDepositAmt *= -1;
	    	
	    	return String.valueOf(totalDepositAmt);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return "0";
	}
	
    
}