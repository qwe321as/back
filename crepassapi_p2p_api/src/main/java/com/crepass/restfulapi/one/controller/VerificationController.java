package com.crepass.restfulapi.one.controller;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.Slack;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.cre.service.CreMemberService;
import com.crepass.restfulapi.inside.service.DepositService;
import com.crepass.restfulapi.one.domain.OneEmoneyInvestPay;
import com.crepass.restfulapi.one.domain.OneEventDiscount;
import com.crepass.restfulapi.one.domain.OneHolidayCalendar;
import com.crepass.restfulapi.one.domain.OneInvest;
import com.crepass.restfulapi.one.domain.OneInvestAccount;
import com.crepass.restfulapi.one.domain.OneInvestAccountInform;
import com.crepass.restfulapi.one.domain.OneInvestDetail;
import com.crepass.restfulapi.one.domain.OneInvestLimitPay;
import com.crepass.restfulapi.one.domain.OneInvestLoanDefault;
import com.crepass.restfulapi.one.domain.OneInvestTitle;
import com.crepass.restfulapi.one.domain.OneLoanVirtualAccntInfo;
import com.crepass.restfulapi.one.domain.OneMemberCustAddInfo2;
import com.crepass.restfulapi.one.domain.OnePaymentFeeInfo;
import com.crepass.restfulapi.one.domain.OnePaymentInvestSchedule;
import com.crepass.restfulapi.one.domain.OnePaymentSchedule;
import com.crepass.restfulapi.one.domain.OneRateInfo;
import com.crepass.restfulapi.one.domain.OneRepayScheduleAdd;
import com.crepass.restfulapi.one.domain.OneRepayScheduleInfo;
import com.crepass.restfulapi.one.domain.OneRepaymentCheckCount;
import com.crepass.restfulapi.one.domain.OneRepaymentDataInfo;
import com.crepass.restfulapi.one.domain.OneRepaymentScheduleItem;
import com.crepass.restfulapi.one.domain.OneSendCheckingEmail;
import com.crepass.restfulapi.one.domain.OneUnpaidRepayment;
import com.crepass.restfulapi.one.domain.OneVirtualAccntWithdraw;
import com.crepass.restfulapi.one.domain.OneVirtualRealAccnt;
import com.crepass.restfulapi.one.service.EmoneyService;
import com.crepass.restfulapi.one.service.EventService;
import com.crepass.restfulapi.one.service.InvestService;
import com.crepass.restfulapi.one.service.LoanService;
import com.crepass.restfulapi.one.service.OneMemberService;
import com.crepass.restfulapi.one.service.RepaymentService;
import com.crepass.restfulapi.one.service.ScheService;
import com.crepass.restfulapi.one.service.VirtualAccntService;
import com.crepass.restfulapi.v2.domain.PaymentScheduleItem;
import com.google.gson.Gson;
import com.ibm.icu.util.ChineseCalendar;

import io.swagger.annotations.ApiOperation;
import net.gpedro.integrations.slack.SlackMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@CrossOrigin
@RestController
@RequestMapping(path = "/verification", method = RequestMethod.POST)
public class VerificationController {

	@Autowired
    private LoanService loanService;

	@Autowired
    private ScheService scheService;
	
	@Autowired
    private CommonUtil commonUtil;
	
	@Autowired
    private CreMemberService creMemberService;
	
	@Autowired
    private RepaymentService repaymentService;
    
	@Autowired
    private VirtualAccntService virtualAccntService;
    
	@Autowired
    private OneMemberService oneMemberService;
	
    @Autowired
    private DepositService depositService;

    @Autowired
    private EventService eventService;

	@Autowired
	private InvestService investService;

	@Autowired
    private EmoneyService emoneyService;
	

	
	@Value("${crepas.url.myloan}")
    private String myLoanUrl;
	
	@Value("${crepas.sms.url}")
    private String smsUrl;
	
    @Value("${crepas.inside.url}")
    private String insideUrl;
    
	@Autowired(required=true)
	private HttpServletRequest request;


	
	// 


	
	// 이건 검증용이구... 실제 업데이트 하는건 원래 도는 배치에서 오늘날짜만 예외처리한다음에 돌리고 다시 원복했음
    @ApiOperation(value = "오늘 자정에 연체정보 안돌았음ㅠ")
    @RequestMapping("/verification/loan/overdue")
    public ResponseEntity<ResponseResult> overDueBatch(@RequestBody String requestString) throws Exception {
		
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        ResponseResult response = new ResponseResult();
    	response.setState(200);
    	response.setMessage("overDueBatch, 정상적으로 처리하였습니다.");

    	
    	// 
    	List<String> overdueLoanInfo = repaymentService.selectOverdueLoanInfo_ExceptGPM();					// cps-정산되지 않은 전체 채권 선택(p_pay_status=N)
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");					
		
		for(int l = 0; l < overdueLoanInfo.size(); l++) {
			String loanId = overdueLoanInfo.get(l);
//	        String prepayDate = formatter.format(new Date());
	        																						// ml-대출개월수, 이자율(5.5), 상환계좌 선택  
	        OneRepaymentDataInfo oneRePaymentDataInfo = repaymentService.selectRepaymentDataInfo(loanId);
//	        List<OneRepaymentCheckCount> oneRepaymentCheckCounts = repaymentService.selectRepaymentCheckCount(loanId, prepayDate); //crs-상환해야 하는 회차(오늘날짜이전)에 원리금,회차정보 모두선택
	        																		
	        																						// IB_FB_P2P_IP-해당 대출자 가상계좌의 입금금액 합
	        // 오늘 입금한 내용 포함되면 안되기 때문에 오늘 낸돈 제외;;;
	        long totalDepositAmt = depositService.selectRepaymentTotalDepositAmt_exceptToday(oneRePaymentDataInfo.getLoanAccntNo());
	        long deposit = 0;
	        //String count = "0";
	        
	        // withdraw에서 출금한 내용들 있으면 대출자 가상계좌 입금액의 합(totalDepositAmt) 에서 공제해야함!  
	        // 추가3줄(cwt-혹시 다른경로로 환불해준 금액이 있으면 해당금액 공제)
	    	String oneMid = repaymentService.selectOneMid(loanId);
	    	long totalRefundAmt = repaymentService.selectRepaymentTotalRefundAmt(oneMid, loanId);
	    	if((oneMid != null) && (totalRefundAmt != 0))
	    		totalDepositAmt -=  totalRefundAmt;
	    	
	    	
	    	// 지금까지 낸돈 : totalDepositAmt, 상환처리된 총금액 : repaymentPaidAmt, 이번회차까지 냈어야 할총 금액 : hadToBePaidAmt
	    	
	    	long repaymentPaidAmt = repaymentService.selectRepaymentPaidAmt(loanId);
	    	long hadToBePaidAmt = repaymentService.selectHadToBePaidAmt(loanId);
	    	
	    	String i_subject = loanService.selectLoanItemSubject(Integer.parseInt(loanId));
	    	i_subject = i_subject.split("\\)")[0];
//	    	String i_subject_list [] = i_subject.split("\\)");
//	    	i_subject = i_subject_list[0];
	    	
	    	if (totalDepositAmt < hadToBePaidAmt)	// 연체 : 낸돈보다 내야할돈이 더 많으면
	        System.out.println(loanId + "	" + i_subject + "	" + totalDepositAmt + "	" + repaymentPaidAmt +  "	" + hadToBePaidAmt);
	    	
	    	
	    	
	    	
		}
		
		response.setResult("Done");
		
//    	commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
      
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
  }
	
    
    @ApiOperation(value = "강제출금")
    @RequestMapping("/verification/refund")
    public ResponseEntity<ResponseResult> refund() throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
//        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
//        JSONObject jsonMember = new JSONObject(requestString);
        
//        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
//        final String mid = jsonRequest.get("mid").toString();
    	try {
//    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live"))
    		{
    			//cwt-전문이 호출되지 않은 (trx_flag=N), 투자자 가상계좌(I)정보와 상환받을 합 선택
//	    		List<OneVirtualAccntWithdraw> oneVirtualAccntWithdraw = virtualAccntService.selectAccntWithdrawSchedule();
	    		List<OneVirtualAccntWithdraw> oneVirtualAccntWithdraw = virtualAccntService.selectAccntWithdrawSchedule_temp();
	    		
	    		for(int i = 0; i < oneVirtualAccntWithdraw.size(); i++) {
	    			String mid = oneVirtualAccntWithdraw.get(i).getMid();			//해당 투자자 예치금과 계좌정보 선택
	    			OneVirtualRealAccnt oneVirtualRealAccnt = virtualAccntService.selectAccountById(mid);
	    			
	    			RestTemplate restTemplate = new RestTemplate();
	    			HashMap<String, String> vars = new HashMap<String, String>();
	    	        vars.put("CUST_ID", oneVirtualRealAccnt.getCustId());
	    	        vars.put("TRAN_BANK_CD", oneVirtualRealAccnt.getMyBankcode());
	    	        vars.put("TRAN_ACCT_NB", oneVirtualRealAccnt.getMyBankacc());
	    	        vars.put("TRAN_REMITEE_NM", oneVirtualRealAccnt.getMyBankName());
	    	        vars.put("TRAN_AMT", oneVirtualAccntWithdraw.get(i).getTrxAmt());
//	    	        vars.put("TRAN_MEMO", "크레파스");
//	    	        vars.put("GUAR_MEMO", "크레파스");
	    	        vars.put("TRAN_MEMO", oneVirtualAccntWithdraw.get(i).getMemo());
	    	        vars.put("GUAR_MEMO", oneVirtualAccntWithdraw.get(i).getMemo());
	    	        																//inside에 출금요청
	    	        String resultDeposit = restTemplate.postForObject(insideUrl + "/assets/withdraw/deposit", vars, String.class);
	    	        JSONObject jsonDeposit = new JSONObject(resultDeposit);
	    	        
	    	        if(jsonDeposit.getInt("STATE") == 200) {
	    	        	String GUAR_SEQ = jsonDeposit.getJSONObject("RESULT").getString("GUAR_SEQ");
	    	        	boolean isDepositSchedule = virtualAccntService.updateWithdrawSchedule(mid, "S", GUAR_SEQ); 	// 전문호출 성공된 내용으로 없데이트(trx_flag=S)
	    	        	if(isDepositSchedule) {
	    	        		//4. 투자자 출금정보(loanId정보없음, 회차정보 없음)
//	    	        		boolean insertDepositHistory = scheService.insertDepositHistory(mid,"W", oneVirtualAccntWithdraw.get(i).getTrxAmt(), "I");	// ctl-투자자 출금정보 입력
	    	        		boolean insertDepositHistory = scheService.insertDepositHistory(mid,"W", oneVirtualAccntWithdraw.get(i).getTrxAmt(), "I", null, null);	// ctl-투자자 출금정보 입력
//	    	        		commonUtil.sendBatchLogging("startWithdraw", "mid : " + mid + " GUAR_SEQ : " + GUAR_SEQ, "insertDepositHistory : " + insertDepositHistory);
	    	        	}
	    	        	
//	    	        	commonUtil.sendBatchLogging("startWithdraw", "mid : " + mid + " GUAR_SEQ : " + GUAR_SEQ, "isDepositSchedule : " + isDepositSchedule);
	    	        } else {
	    	        	boolean isDepositSchedule = virtualAccntService.updateWithdrawSchedule(mid, "F", "");
//	                    System.out.println(jsonDeposit.getString("MESSAGE") + " isDepositSchedule : " + isDepositSchedule);
//	                    commonUtil.sendBatchLogging("startWithdraw", "fail getCustId : " + oneVirtualRealAccnt.getCustId() + " getMyBankcode : " + oneVirtualRealAccnt.getMyBankcode()
//	                    + " getMyBankacc : " + oneVirtualRealAccnt.getMyBankacc() + " getMyBankName : " + oneVirtualRealAccnt.getMyBankName() + " getTrxAmt : " + oneVirtualAccntWithdraw.get(i).getTrxAmt()
//	                    , "MESSAGE : " + jsonDeposit.getString("MESSAGE") + " isDepositSchedule : " + isDepositSchedule);
	    	        }
	    		}
    		}
    	} catch (Throwable t) {
    		commonUtil.sendBatchLogging("startWithdraw", "exception error!!", t.getMessage());
    		throw new RuntimeException(t.getMessage());
    	}
  
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("verificationRepayment, 정상적으로 처리하였습니다.");
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
//        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    

    
	
	
	// 주의!! 현재 연체를 고려하지 않은 검증!! 반드시 연체한 테이블정보를 제외하고 수정해야함!!
    @ApiOperation(value = "crs 이자및 수수료검증")
    @RequestMapping("/verification/repayment_200430")
    public ResponseEntity<ResponseResult> verificationRepayment_200430(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String midF = jsonRequest.get("mid").toString();
        
		///////////////////////////////////////////////////////////////////////////////////////
        //        
		//         	기준날짜 만들기
		//         	해당 아이디로 기준날짜포함 이전회차중 N인 상환정보 모두 가지고 옴
		//         	기준일포함 납부금액(원금,이자,연체이자) 합과 기준일포함 이전에 입금한 내역 비교
		//         	입금한 합이 더 크면 정산예정 플래그 ㅎ
		//         	작으면 insert (이미 있으면 업데이트)
		//         	연체플래그(1,2,3), startDt, endDt(0000-00-00), mDiffday, etc, 연체이자, 기한이익 
		//         
		/////////////////////////////////////////////////////////////////////////////////////////
        
        try {
//    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) 
    		{

    	        // 5월이후에 상환해야할 회차가 남아있는 loanId 선택
    			List<OneRepayScheduleInfo> oneRepayScheduleInfos = scheService.selectRepayScheduleInfoTestAll();
	    		
	    		Calendar cal = Calendar.getInstance();
	    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    		
	    		double taxRate = 0;
    			double taxRateLocal = 0;
	    		
	    		for(int i = 0; i < oneRepayScheduleInfos.size(); i++) {
	    			String mid = oneRepayScheduleInfos.get(i).getMid();
	    			String loanId = oneRepayScheduleInfos.get(i).getLoanId();
	    			String loanPay = oneRepayScheduleInfos.get(i).getLoanPay();
	    			String loanDay = oneRepayScheduleInfos.get(i).getLoanDay();
	    			String yearPlus = oneRepayScheduleInfos.get(i).getYearPlus();
	    			String repayInfo = oneRepayScheduleInfos.get(i).getRepayInfo();
	    			String repayDay = oneRepayScheduleInfos.get(i).getRepayDay();
	    			String execDate = oneRepayScheduleInfos.get(i).getExecDate();
	    			
	    			//............. 대출일보다 첫번째 상환일이 15일이하면 +1월 해야함;;;  
	    			Calendar calendar = Calendar.getInstance();
	    			int Year = Integer.parseInt(execDate.split("-")[0]);									//현재년도
					int Month = Integer.parseInt(execDate.split("-")[1]) -1 ;								//현재월
					int Day = Integer.parseInt(execDate.split("-")[2]);										//대출실행일
	    			
					if(Day > Integer.parseInt(repayDay))													//대출실행일 > 상환예정일보다 크면(예:대출실행일 02/16, 상환일 02/15)
						Month += 1;
					
	    	    	cal.set(Calendar.YEAR, Year);
	    			cal.set(Calendar.MONTH, Month);
	    			cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(repayDay));								// 상환예정일로 세팅하고
	    			calendar.setTimeInMillis(cal.getTimeInMillis());
	    	    	
					String startDate = sdf.format(calendar.getTime());										// 상환 시작일로 값 넣어서
	    			long diffDate = getDiffOfDate(execDate, startDate);										// 1회차 일수 계산(1회차상환일-대출시작일)
	    			JSONArray jsonRepaySchedules = null;
	    			
	    			int repayMonth = 0;
	    			int repayInfoType = 0;
	    			double feeRate = 0;
	    			
	    			if(diffDate < 15)
	    				repayMonth = 1;																		//대출일보다 첫번째 상환일이 15일이하면 repayMonth + 1월
	    			
	    			
	    			switch(repayInfo) {
		    			case "원리금균등상환" :
		    				repayInfoType = 1;							//대출액(loanPay), 연이율(yearPlus), 대출기간(loanDay), 대출상환일(repayDay:5,15,25), 상환시작일(startDate), 대출실행일(execDate), 상환일-대출일(diffDate), 추가+1달여부(repayMonth)
		    				jsonRepaySchedules = createPrincipalSchedule(Double.parseDouble(loanPay), Double.parseDouble(yearPlus) * 0.01, Integer.parseInt(loanDay), repayDay, startDate, execDate, diffDate, repayMonth);
		    				break;
	    			
		    			case "만기일시상환" :
		    				repayInfoType = 2;
		    				jsonRepaySchedules = createMaturitySchedule(Double.parseDouble(loanPay), Double.parseDouble(yearPlus) * 0.01, Integer.parseInt(loanDay), repayDay, startDate, execDate, diffDate, repayMonth);
		    				break;
	    			}
	    																									// 투자자 정보선택
	    			List<OnePaymentInvestSchedule> onePaymentInvestSchedules = scheService.selectPaymentInvestSchedule(loanId);
		    		
	    			for(int j = 0 ; j < jsonRepaySchedules.length(); j++) {									// 대출자 정보 입력시작			
	    				JSONObject repaySchedules = jsonRepaySchedules.getJSONObject(j);
	    				String payCount = repaySchedules.getString("payCount");
	    				String repayAmount = repaySchedules.getString("repayAmount");
	    				String paidAmount = repaySchedules.getString("paidAmount");
	    				String loanInterest = repaySchedules.getString("loanInterest");
	    				String balance = repaySchedules.getString("balance");
	    				String resultDate = repaySchedules.getString("resultDate");
	    	    		
	    				resultDate = WorkingDayCalculate(resultDate) == null ? resultDate : WorkingDayCalculate(resultDate);
	    				
	    	    		OneRepayScheduleAdd oneRepayScheduleAdd = new OneRepayScheduleAdd();
	    	    		oneRepayScheduleAdd.setLoanId(loanId);
	    	    		oneRepayScheduleAdd.setRepayCount(payCount);
	    	    		oneRepayScheduleAdd.setRepayDate(resultDate);
	    	    		oneRepayScheduleAdd.setPayAmount(repayAmount);
	    	    		oneRepayScheduleAdd.setLnAmount(paidAmount);
	    	    		oneRepayScheduleAdd.setInterestAmount(loanInterest);
	    	    		oneRepayScheduleAdd.setBalance(balance);
	    			}
	    		
		    		for(int k = 0; k < onePaymentInvestSchedules.size(); k++) {								//이벤트 대상자에 대한 정보 선택
		    			OneEventDiscount oneEventDiscount = eventService.selectEventDiscount(onePaymentInvestSchedules.get(k).getMid(), onePaymentInvestSchedules.get(k).getRegdatetime());
		    			
		    			double discountFee = 100;
		    			int discountMonth = 0;
		    			
		    			if(oneEventDiscount != null) {
		    				discountFee -= Double.parseDouble(oneEventDiscount.getEvent_discount());
		    				discountMonth = Integer.parseInt(oneEventDiscount.getEvent_discount_month());
		    			}
		    			
		    			double paidAmountTmt = 0; // 끝전정리를 위한 원금(paidAmount) 누적
		    			for(int j = 0 ; j < jsonRepaySchedules.length(); j++) {					// 투자자 정보 입력시작
		    				JSONObject repaySchedules = jsonRepaySchedules.getJSONObject(j);
		    				String payCount = repaySchedules.getString("payCount");
		    				String repayAmount = repaySchedules.getString("repayAmount");
		    				String paidAmount = repaySchedules.getString("paidAmount");			//원금(원리금-이자)
		    				String loanInterest = repaySchedules.getString("loanInterest");
		    				String balance = repaySchedules.getString("balance");
		    				String resultDate = repaySchedules.getString("resultDate");
		    				
		    				
		    																					// 세금과 지방세에 대한 정보 선택
		    				OneRateInfo oneRateInfo = scheService.selectRateInfo(onePaymentInvestSchedules.get(k).getLevel(), onePaymentInvestSchedules.get(k).getService(), String.valueOf(repayInfoType));
		    				// feeRate = Double.parseDouble(oneRateInfo.getFee());					// 현재까지는 모두 0.024
		    				taxRate = Double.parseDouble(oneRateInfo.getTax());					// 현재까지는 모두 0.25	
		    				taxRateLocal = Double.parseDouble(oneRateInfo.getTaxLocal());		// 현재까지는 모두 0.025
		    				
		    				resultDate = WorkingDayCalculate(resultDate) == null ? resultDate : WorkingDayCalculate(resultDate);
		    	    		resultDate = WorkingDayCalculate(resultDate) == null ? resultDate : WorkingDayCalculate(resultDate);
		    	    		String[] repayDate = resultDate.split("-");
		    	    		
		    	    																			// 투자자가 투자한 비율(투자액/대출액)
	    	    			double loanRate = Double.parseDouble(onePaymentInvestSchedules.get(k).getPay()) / Double.parseDouble(onePaymentInvestSchedules.get(k).getLoanPay());
	    	    			long loanPayment = Long.parseLong(paidAmount) + Long.parseLong(balance);
	    	    			
	    	    			double inAmount = Math.ceil(Double.parseDouble(paidAmount) * loanRate);	// 투자금액 39원으로 떨어지면 세금 10원아닌 0원되는부분 Math.ceil로 처리 200429 
	    	    			double interest = Math.ceil(Double.parseDouble(loanInterest) * loanRate);	// 
	    	    			
	    	    			double taxPay = (interest * taxRate);
	    	    			double taxPayLocal = (interest * taxRateLocal);
	    	    			
	    	    			//4줄 // 투자자 마지막 회차 끝전정리		4줄추가
	    	    			if(jsonRepaySchedules.length() == j+1) {
	    	    				inAmount = Math.floor((Double.parseDouble(onePaymentInvestSchedules.get(k).getPay()) - paidAmountTmt));
		    				}
	    	    			if(Integer.parseInt(onePaymentInvestSchedules.get(k).getLevel()) == 4) {
	    	        			// 200429 세금이 1000원일 경우도 절삭으로 적용
	    	    				if(taxPay <= 1000) {
	    	    					taxPay = 0;
	    	    					taxPayLocal = 0;
		    	    			}
	    	    			}

	    	    			long tax_real = ((long)Math.floor(taxPay) / 10) * 10;				// 일반적인경우 10원단위 절삭
	    	    			long tax_local_real = ((long)Math.floor(taxPayLocal) / 10) * 10;
	    	    			
	    	    			double result = inAmount + (interest - (tax_real + tax_local_real));	// 원금+(이자-(세금+지방세금))
	    	    			
	    	    			// 잔여투자금 구하는 함수
	    	    			double investBalance = investBalance(jsonRepaySchedules, Integer.parseInt(payCount));
	    	    			double fee = investBalance * loanRate * 0.002;			 								// * 0.024
	    	    			//////////
	    	    			
	    	    			if(discountMonth > 0) {
	    	    				fee *= (discountFee * 0.01);
	    	    				discountMonth--;
	    	    			}
	    	    																				// 투자자가 받게 될 실제 금액
	    	    			long investAmount = ((long)Math.ceil(inAmount) + (long)Math.ceil(interest) - ((long)Math.floor(fee) + tax_real + tax_local_real));
	    	    																							// floor 소숫점 버림
	    	    			String payDate = null;
	    	    			
	    	    			calendar = Calendar.getInstance();
	    	    			Year = (Integer.parseInt(repayDate[0]));
		    				Month = (Integer.parseInt(repayDate[1]) - 1);
	    	    			
		    				int repayDates = Integer.parseInt(repayDate[repayDate.length - 1]);
	    	    			
    	    				cal.set(Calendar.YEAR, Year);
    	    				cal.set(Calendar.MONTH, Month);
    	    				cal.set(Calendar.DAY_OF_MONTH, repayDates);
    	    				calendar.setTimeInMillis(cal.getTimeInMillis());
    	    				
    	    				payDate = WorkingDayCalculate(sdf.format(calendar.getTime()));
    	    				
    	    				while(payDate != null) {
    	    					payDate = WorkingDayCalculate(sdf.format(calendar.getTime()));
    	    					
    	    					if(payDate != null) {
	    	    					String[] payDates = payDate.split("-");
	        	    				
	        	    				Year = Integer.parseInt(payDates[0]);
	        	    				Month = Integer.parseInt(payDates[1]) - 1;
	        	    				repayDates = Integer.parseInt(payDates[2]);
	        	    				
	        	    				cal.set(Calendar.YEAR, Year);	        	    				cal.set(Calendar.MONTH, Month);	        	    				cal.set(Calendar.DAY_OF_MONTH, repayDates);	        	    				calendar.setTimeInMillis(cal.getTimeInMillis());	        	    				
	        	    				payDate = WorkingDayCalculate(sdf.format(calendar.getTime()));
    	    					}
    	    				}
    	    				
    	    				if(payDate == null)
    	    					payDate = sdf.format(calendar.getTime());
    	    				
    	    				String[] payDates = payDate.split("-");
    	    				
    	    				Year = Integer.parseInt(payDates[0]);
    	    				Month = Integer.parseInt(payDates[1]) - 1;
    	    				repayDates = Integer.parseInt(payDates[2]);
	    	    			
	    	    			payDate = WorkingDayCalculate(payDate) == null ? payDate : WorkingDayCalculate(payDate);
	    	    			
	    	    			OnePaymentSchedule onePaymentSchedule = new OnePaymentSchedule();
	    	    			onePaymentSchedule.setMid(onePaymentInvestSchedules.get(k).getMid());
	    	    			onePaymentSchedule.setLoanId(loanId);
	    	    			onePaymentSchedule.setRepayCount(payCount);
	    	    			onePaymentSchedule.setPayDate(payDate);
	    	    			onePaymentSchedule.setPayGubun("A");
	    	    			onePaymentSchedule.setPayStatus("N");
	    	    			onePaymentSchedule.setLnAmount(String.valueOf(Math.ceil(inAmount)));
	    	    			onePaymentSchedule.setInterestAmount(String.valueOf(Math.ceil(interest)));
	    	    			onePaymentSchedule.setDelqAmount("0");
	    	    			onePaymentSchedule.setTax(String.valueOf(tax_real));
	    	    			onePaymentSchedule.setTaxLocal(String.valueOf(tax_local_real));
	    	    			onePaymentSchedule.setFee(String.valueOf(Math.floor(fee)));
	    	    			onePaymentSchedule.setPayAmount(String.valueOf(investAmount));
	    	    			
	    	    			
	    	    			PaymentScheduleItem paymentScheduleItem = repaymentService.selectPaymentScheduleItem(onePaymentInvestSchedules.get(k).getMid(), loanId, payCount);
	    	    			
	    	    			if(paymentScheduleItem != null)
		    	    			if (paymentScheduleItem.getMid().equals(onePaymentInvestSchedules.get(k).getMid()) &&
		    	    					paymentScheduleItem.getLoanId().equals(loanId) &&
		    	    					paymentScheduleItem.getPCount().equals(payCount) && paymentScheduleItem.getPayStatus().equals("N"))
	    	    			
		    	    				if(! (paymentScheduleItem.getTax() == tax_real) && (paymentScheduleItem.getTaxLocal() == tax_local_real) && (paymentScheduleItem.getFee() == Math.floor(fee)) )
//		    	    					if(!repaymentService.updatePaymentScheduleTax(onePaymentInvestSchedules.get(k).getMid(), loanId, payCount, String.valueOf(tax_real))){
//		    	    						System.out.println("실패! 검증값 : 	" + onePaymentInvestSchedules.get(k).getMid() + "	" + loanId + "	" + payCount+ "	" + payDate+ "	" + paymentScheduleItem.getPayStatus()+ "	" + String.valueOf(Math.ceil(inAmount)) + "	" + String.valueOf(Math.ceil(interest)) +"	" + 
//													String.valueOf(tax_real) + "	" + tax_local_real + "	" + String.valueOf(Math.floor(fee)) +
//													"		DB	" + paymentScheduleItem.getTax() + "	" + paymentScheduleItem.getTaxLocal() + "	" + paymentScheduleItem.getFee());
//		    	    					} else {
		    	    					System.out.println("검증값 : 	" + onePaymentInvestSchedules.get(k).getMid() + "	" + loanId + "	" + payCount+ "	" + payDate+ "	" + paymentScheduleItem.getPayStatus()+ "	" + String.valueOf(Math.ceil(inAmount)) + "	" + String.valueOf(Math.ceil(interest)) +"	" + 
		    	    													String.valueOf(tax_real) + "	" + tax_local_real + "	" + String.valueOf(Math.floor(fee)) +
		    	    													"		DB	" + paymentScheduleItem.getTax() + "	" + paymentScheduleItem.getTaxLocal() + "	" + paymentScheduleItem.getFee());
//		    	    					}
	    	    			paidAmountTmt += Math.ceil(inAmount);
	    	    		}
	    			}
//		    		System.out.println(loanId);
	    		}
    		}
    		System.out.println("완료!");
    	} catch (Throwable t) {
    		commonUtil.sendBatchLogging("verificationRepayment_200430", "exception error!!", t.getMessage());
    		throw new RuntimeException(t.getMessage());
    	}

        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("verificationRepayment_200430, 정상적으로 처리하였습니다.");
        
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    

    @ApiOperation(value = "crs 이자검증")
    @RequestMapping("/verification/repayment")
    public ResponseEntity<ResponseResult> verificationRepayment(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String mid = jsonRequest.get("mid").toString();
        
        List<String> overdueLoanInfo = repaymentService.selectOverdueLoanInfo();					// cps-정산되지 않은 전체 채권 선택(p_pay_status=N)
        
		////////////////////////////////////////////////////////////////////////////////////////
		//
		//         	기준날짜 만들기
		//         	해당 아이디로 기준날짜포함 이전회차중 N인 상환정보 모두 가지고 옴
		//         	기준일포함 납부금액(원금,이자,연체이자) 합과 기준일포함 이전에 입금한 내역 비교
		//         	입금한 합이 더 크면 정산예정 플래그 ㅎ
		//         	작으면 insert (이미 있으면 업데이트)
		//         	연체플래그(1,2,3), startDt, endDt(0000-00-00), mDiffday, etc, 연체이자, 기한이익 
		//         
		/////////////////////////////////////////////////////////////////////////////////////////
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");		
        String today = sdf.format(new Date());
        
        for (int i=0; i<overdueLoanInfo.size(); i++) {

           	if(i == 0) {
           		System.out.println("subject	lonaid	회차	연체	기한이익	플래그	연체일수	연체시작일	연체종료일	대출실행일	상환예정일");
           	}
        	
        	List<OneUnpaidRepayment> oneUnpaidRepayment = repaymentService.selectUnpaidRepayment(overdueLoanInfo.get(i), today);
        	
        	for (int j=0; j<oneUnpaidRepayment.size(); j++) {
        																				// 해당 loanId 계좌번호, 대출실행일 선택
                OneLoanVirtualAccntInfo loanVirtualAccnt = virtualAccntService.selectLoanVirtualaccnt(overdueLoanInfo.get(i));
               	
               	// 오늘날짜 포함한 내야할돈과 낸돈 비교
                double repaymentAmt = repaymentService.selectPaidTillTodayRepayment(overdueLoanInfo.get(i), oneUnpaidRepayment.get(j).getPayDate());
               	double paidAmt = depositService.selectPaidTillTodayInside(loanVirtualAccnt.getLoanAccntNo());
                		
               	if(repaymentAmt <= paidAmt) {			// 연체아님
               		
               		if(oneUnpaidRepayment.get(j).getPayStatus().equals("N")){
               			System.out.println(overdueLoanInfo.get(i));
               		}
               		
               		// 실제 입금일자 계산해서 업데이트
               		
               		
               	} else {								// 연체
            
               		// loanId, 회차,  mDiffday, 연체이자, 기한이익, 연체플래그(1,2,3), startDt, endDt(0000-00-00), etc 
               		// 없으면 insert 있으면 update
               		String delqState = "1";
               		long diffDate = 0;
               		double delqAmt = 0;
               		double delqGihanAmt = 0;
               		String startDt;
               		
               		
               		double rate=0.055;
               		double overRate=0.03;
               		int daysOfYear=365;
               		

    	    		
    	    		 
    	    		// 연체시작일 구하기
               		String dateFrom = oneUnpaidRepayment.get(j).getPayDate();
//               		dateFrom = increaseOneDate(dateFrom);
               		
               		if(WorkingDayCalculate(dateFrom) != null)	// 연체시작일이 휴일이 아니면
               			dateFrom = WorkingDayCalculate(dateFrom);

    	    		String dateTo;

    	    		// 다음회차가 넘어가면 안되므로 연제끝날 정해줘야함
    	    		// 연체끝일은 today가 다음회차가 넘어가지 않으면 today로, 다음회차가 null이면 today로
    	    		if( oneUnpaidRepayment.size()-1 > j) {

    	    			diffDate = getDiffOfDate(today, oneUnpaidRepayment.get(j+1).getPayDate());
        	    		
    	    			// + today가 작다는 이야기이므로 오늘날짜
    	    			if(diffDate >= 0 ) 
    	    				dateTo = today;
    	    			else
    	    				dateTo = oneUnpaidRepayment.get(j+1).getPayDate();
    	    		} else 
    	    			dateTo = today;

		    		// 다음회차보다 오늘이 더 작으면
		    		diffDate = getDiffOfDate(dateFrom, dateTo);    	    			
		    		double delGihan;
               		
               		// 현재는 "cpas_overdue_validation"에 연체플래그가 없어서 crs에서 플래그 가지고 옴;
               		// 나중에는 cov에서 가지고 와야 제대로 된 검증 됨;
               		// 첫연체
    	    		if(j == 0) {
    	    		
    	    			// 연체이자 = 원리금 * ( (정상이율+가산이율) / 해당년도 일수 ) * 일수
    	    			delqAmt = getDelqAmt(oneUnpaidRepayment.get(j).getPayAmt(), rate, overRate, daysOfYear, diffDate);
	               			
               		} else {
               		
               			// 이전회차에 상환하지 않고, 현재 연체 1중이면
	               		if(oneUnpaidRepayment.get(j-1).getPayStatus().equals("N") && oneUnpaidRepayment.get(j).getDelqState().equals("1")) {
	               			
	               			// 연체이자 = 원리금 * ( (정상이율+가산이율) / 해당년도 일수 ) * 일수
	    	    			delqAmt = getDelqAmt(oneUnpaidRepayment.get(j).getPayAmt(), rate, overRate, daysOfYear, diffDate);
	    	    			delqState = "2";
	    	    			delqAmt = delqAmt * Double.parseDouble(delqState);
	    	    			
	               			
	               		} else if (oneUnpaidRepayment.get(j-1).getPayStatus().equals("N") && oneUnpaidRepayment.get(j).getDelqState().equals("2")) {
	               			
	               			delGihan = repaymentService.selectGihanRepayment(overdueLoanInfo.get(i)); 
	               			
	               			// 연체이자 = 원리금 * ( (정상이율+가산이율) / 해당년도 일수 ) * 일수
	               			delqGihanAmt = getDelqAmt(delGihan, rate, overRate, daysOfYear, diffDate);
	               			delqState = "3";
	               			
	               		}
               		}
    	    		
    	    		// test
    	    		if(oneUnpaidRepayment.size() == j+1)
    	    		System.out.println(oneUnpaidRepayment.get(j).getPayAmt() + "	" + loanVirtualAccnt.getSubject() + "	" + overdueLoanInfo.get(i) + "	"  + oneUnpaidRepayment.get(j).getPCount()
    	    				+ "	" + (long) delqAmt + "	" + (long) delqGihanAmt + "	" + delqState + "	" + diffDate
    	    				+ "	" +  dateFrom + "	" +  dateTo + "	" +  loanVirtualAccnt.getExecDate() + "	" +  oneUnpaidRepayment.get(j).getPayDate());
               	}
        	}
        }
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("verificationRepayment, 정상적으로 처리하였습니다.");
        
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    

	
	



	private double getDelqAmt(double payAmt, double rate, double overRate, int daysOfYear, long diffDate) {
    	return payAmt * ((rate+overRate) / daysOfYear) * diffDate;
	}



	// 이벤트건도 제외 해야함;
	@ApiOperation(value = "200420 수수료 개정검증값")
    @RequestMapping("/verification/allpaymentfee")
    public ResponseEntity<ResponseResult> addLoanCategory(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String mid = jsonRequest.get("mid").toString();
        
        int cntMatch = 0;
        int cntMisMatch = 0;
        
        List<OnePaymentFeeInfo> selectAllPaymentFeeInfo = scheService.selectAllPaymentFeeInfo();
        List<OnePaymentFeeInfo> differentLoanIdList = new ArrayList<OnePaymentFeeInfo>();
        
        for(int i=0; i<selectAllPaymentFeeInfo.size(); i++) {
        	
        	double fee;
        	
        	if(selectAllPaymentFeeInfo.get(i).getPStatus().equals("N")) {
        		// 잔여투자금 구하는 함수
				long investBalance = scheService.selectNewFeeInfo(selectAllPaymentFeeInfo.get(i).getLoanId(), selectAllPaymentFeeInfo.get(i).getMid(), selectAllPaymentFeeInfo.get(i).getPCount());
	    		fee = investBalance * 0.002;											// * 0.024
        		
        	} else {
        		fee = Double.parseDouble(selectAllPaymentFeeInfo.get(i).getFee());
        	}
        	
        	fee = Math.floor(fee); 
        	long feeL = (long) Math.floor(fee); 
        	if (Double.parseDouble(selectAllPaymentFeeInfo.get(i).getFee()) == feeL)
        		cntMatch++;
        	else {
        		cntMisMatch++;
        		OnePaymentFeeInfo differentLoanId = new OnePaymentFeeInfo();
//        		if(!failedLoanIdList.contains(selectAllPaymentFeeInfo.get(i).getLoanId()))
        		differentLoanId.setMid(selectAllPaymentFeeInfo.get(i).getMid());
        		differentLoanId.setLoanId(selectAllPaymentFeeInfo.get(i).getLoanId());
        		differentLoanId.setPCount(selectAllPaymentFeeInfo.get(i).getPCount());
        		differentLoanId.setPStatus(selectAllPaymentFeeInfo.get(i).getPStatus());
        		differentLoanId.setLnAmount(selectAllPaymentFeeInfo.get(i).getLnAmount());
        		differentLoanId.setFee(selectAllPaymentFeeInfo.get(i).getFee());
        		differentLoanId.setPayDate(selectAllPaymentFeeInfo.get(i).getPayDate());
        		differentLoanIdList.add(differentLoanId);
        	}
        		
        	System.out.println(selectAllPaymentFeeInfo.get(i).getMid() + "\t" + selectAllPaymentFeeInfo.get(i).getLoanId()
        		+ "\t" + selectAllPaymentFeeInfo.get(i).getPCount() + "\t" +selectAllPaymentFeeInfo.get(i).getPStatus()
        		+ "\t" + selectAllPaymentFeeInfo.get(i).getLnAmount() + "\t" + selectAllPaymentFeeInfo.get(i).getFee()
        		+ "\t" + fee + "\t" + selectAllPaymentFeeInfo.get(i).getPayDate());
    
        }
        System.out.println("동일한건 : " + cntMatch + ", 다른건 : " + cntMisMatch );
        
        

        for(int i=0; i<differentLoanIdList.size(); i++) {
    		
        	System.out.println(differentLoanIdList.get(i).getMid() + "\t" + differentLoanIdList.get(i).getLoanId()
        		+ "\t" + differentLoanIdList.get(i).getPCount() + "\t" +differentLoanIdList.get(i).getPStatus()
        		+ "\t" + differentLoanIdList.get(i).getLnAmount() + "\t" + differentLoanIdList.get(i).getFee()
        		+ "\t\t" + differentLoanIdList.get(i).getPayDate());
        
        }
        

        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("동일한건 : " + cntMatch + ", 다른건 : " + cntMisMatch + ", 정상적으로 처리하였습니다.");
        
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    

	// 이벤트건도 제외 해야함;
	@ApiOperation(value = "cust아이디 통해서 mid 구하기")
    @RequestMapping("/verification/getmid")
    public ResponseEntity<ResponseResult> getMid(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String custIds = jsonRequest.get("custIds").toString();		// , 구분으로 한줄로 다받기;
        
        String [] custIdList = custIds.split(",");
        
        
        for(int i=0; i<custIdList.length; i++) {
        	String mid = oneMemberService.selectByCustIdToMid(custIdList[i]);
			
        	System.out.println(mid);
        }
        
        
                

        ResponseResult response = new ResponseResult();        
        response.setState(200);
//        response.setMessage("동일한건 : " + cntMatch + ", 다른건 : " + cntMisMatch + ", 정상적으로 처리하였습니다.");
        
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
	
	


	// 200608 투자자 가상계좌 등록여부 확인하는 메서드
	@ApiOperation(value = "투자자 가상계좌 등록여부 확인")
    @RequestMapping("/verification/checkAccount")
    public ResponseEntity<ResponseResult> accountVerification(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
//        JSONObject jsonMember = new JSONObject(requestString);
//        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        
//        String mid = "igothewar@naver.com";
//        OneMemberCustAddInfo2 oneMemberCustAddInfo = oneMemberService.selectCustAddInfo2(mid);
        List<OneInvestAccountInform> investAccntNoList = scheService.selectInvestAccntInform();
        
        for(int i=0; i<investAccntNoList.size(); i++) {
	        RestTemplate restTemplate = new RestTemplate();
	        Map<String, String> vars = new HashMap<String, String>();
	        vars.put("CUST_ID", investAccntNoList.get(i).getCustId());
	        
	        String resultSearch = restTemplate.postForObject(insideUrl + "/customer/search", vars, String.class);
	        JSONObject jsonSearch = new JSONObject(resultSearch);
	        
	        int searchState = jsonSearch.getInt("STATE");
	        
	        if(searchState == 200) {
	        /* {"STATE":200,"MESSAGE":"정상적으로 처리하였습니다.",
	        	"RESULT":{
	        	"BANK_CD":"004",
	        	"SUP_REG_NB":"",
	        	"ACCT_NB":"24110204025529",
	        	"CMS_NB":"56213097373989",
	        	"CUST_SUB_NM":"","BIRTH_DATE":"19840906",
	        	"HP_NO1":"010","HP_NO2":"4008","HP_NO3":"5034",
	        	"PRI_SUP_GBN":"1",
	        	"CUST_NM":"이재형","REP_NM":""}}	
	        */	
	        	if (jsonSearch.has("RESULT"))
	        		System.out.println("투자자명	" + jsonSearch.getJSONObject("RESULT").getString("CUST_NM") + "	가상계좌	" +
	        			jsonSearch.getJSONObject("RESULT").getString("CMS_NB") + "	실계좌정보	" + jsonSearch.getJSONObject("RESULT").getString("ACCT_NB"));
	        	else 
	        		System.out.println("투자자명" + investAccntNoList.get(i).getMName() + "	RESULT 값없음");
	        	
	        } else {
	        	System.out.println("투자자명	" + investAccntNoList.get(i).getMName() + "	등록되지 않음	오류번호	" + searchState); 
	        }
	        	
        }        
                

        ResponseResult response = new ResponseResult();        
        response.setState(200);
//        response.setMessage("동일한건 : " + cntMatch + ", 다른건 : " + cntMisMatch + ", 정상적으로 처리하였습니다.");
        
        
//        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
	

	// 상환스케줄 생성스케줄러  (최종수정 V200715)
	@ApiOperation(value = "수수료검증_200715")
    @RequestMapping("/verification/fee200715")
    public ResponseEntity<ResponseResult> feeVerification(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
//        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
    	try {
//    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) 
    		{

    			//대출승인 완료 채권중(ml.loan_step4='Y')에 mip-상환스케줄 생성 배치가 실행 안된것(i_exec_repaybatch=N) 선택
//    			List<OneRepayScheduleInfo> oneRepayScheduleInfos = scheService.selectRepayScheduleInfo();
    			List<OneRepayScheduleInfo> oneRepayScheduleInfos = scheService.selectRepayScheduleInfoTest("4313");
	    		Calendar cal = Calendar.getInstance();
	    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    		
	    		double taxRate = 0;
    			double taxRateLocal = 0;
	    		
	    		for(int i = 0; i < oneRepayScheduleInfos.size(); i++) {
	    			String mid = oneRepayScheduleInfos.get(i).getMid();
	    			String loanId = oneRepayScheduleInfos.get(i).getLoanId();
	    			String loanPay = oneRepayScheduleInfos.get(i).getLoanPay();
	    			String loanDay = oneRepayScheduleInfos.get(i).getLoanDay();
	    			String yearPlus = oneRepayScheduleInfos.get(i).getYearPlus();
	    			String repayInfo = oneRepayScheduleInfos.get(i).getRepayInfo();
	    			String repayDay = oneRepayScheduleInfos.get(i).getRepayDay();
	    			String execDate = oneRepayScheduleInfos.get(i).getExecDate();
	    			String loanCate = oneRepayScheduleInfos.get(i).getLoanCate();
	    	    			
	    			//............. 대출일보다 첫번째 상환일이 15일이하면 +1월 해야함;;;  
	    			Calendar calendar = Calendar.getInstance();

	    			// 현재가 아닌 과거 데이터 검증이기 떄문에 	    			
//	    			int Year = calendar.get(Calendar.YEAR);													//현재년도
//					int Month = calendar.get(Calendar.MONTH);												//현재월
	    			int Year = Integer.parseInt(execDate.split("-")[0]);													//현재년도
					int Month = Integer.parseInt(execDate.split("-")[1])-1;												//현재월

	    			
	    			int Day = Integer.parseInt(execDate.split("-")[2]);										//대출실행일
	    			
					if(Day > Integer.parseInt(repayDay))													//대출실행일 > 상환예정일보다 크면(예:대출실행일 02/16, 상환일 02/15)
						Month += 1;
					
	    	    	cal.set(Calendar.YEAR, Year);
	    			cal.set(Calendar.MONTH, Month);
	    			cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(repayDay));								// 상환예정일로 세팅하고
	    			calendar.setTimeInMillis(cal.getTimeInMillis());
	    	    	
					String startDate = sdf.format(calendar.getTime());										// 상환 시작일로 값 넣어서
	    			long diffDate = getDiffOfDate(execDate, startDate);										// 1회차 일수 계산(1회차상환일-대출시작일)
	    			JSONArray jsonRepaySchedules = null;
	    			
	    			int repayMonth = 0;
	    			int repayInfoType = 0;
	    			double feeRate = 0;
	    			
	    			if(diffDate < 15)
	    				repayMonth = 1;																		//대출일보다 첫번째 상환일이 15일이하면 repayMonth + 1월
	    			
	    			
	    			switch(repayInfo) {
		    			case "원리금균등상환" :
		    				repayInfoType = 1;							//대출액(loanPay), 연이율(yearPlus), 대출기간(loanDay), 대출상환일(repayDay:5,15,25), 상환시작일(startDate), 대출실행일(execDate), 상환일-대출일(diffDate), 추가+1달여부(repayMonth)
		    				jsonRepaySchedules = createPrincipalSchedule(Double.parseDouble(loanPay), Double.parseDouble(yearPlus) * 0.01, Integer.parseInt(loanDay), repayDay, startDate, execDate, diffDate, repayMonth);
		    				break;
	    			
		    			case "만기일시상환" :
		    				repayInfoType = 2;
		    				jsonRepaySchedules = createMaturitySchedule(Double.parseDouble(loanPay), Double.parseDouble(yearPlus) * 0.01, Integer.parseInt(loanDay), repayDay, startDate, execDate, diffDate, repayMonth);
		    				break;
	    			}
	    			
	    			List<OneSendCheckingEmail> oneSendCheckingEmailList = new ArrayList<OneSendCheckingEmail>();	// 메일 보내기 위한 리스트
	    			
	    																									// 투자자 정보선택
	    			List<OnePaymentInvestSchedule> onePaymentInvestSchedules = scheService.selectPaymentInvestSchedule(loanId);
		    			    			
	    			for(int j = 0 ; j < jsonRepaySchedules.length(); j++) {									// 대출자 정보 입력시작			
	    				JSONObject repaySchedules = jsonRepaySchedules.getJSONObject(j);
	    				String payCount = repaySchedules.getString("payCount");
	    				String repayAmount = repaySchedules.getString("repayAmount");
	    				String paidAmount = repaySchedules.getString("paidAmount");
	    				String loanInterest = repaySchedules.getString("loanInterest");
	    				String balance = repaySchedules.getString("balance");
	    				String resultDate = repaySchedules.getString("resultDate");
	    	    		
	    				resultDate = WorkingDayCalculate(resultDate) == null ? resultDate : WorkingDayCalculate(resultDate);
	    				
	    	    		OneRepayScheduleAdd oneRepayScheduleAdd = new OneRepayScheduleAdd();
	    	    		oneRepayScheduleAdd.setLoanId(loanId);
	    	    		oneRepayScheduleAdd.setRepayCount(payCount);
	    	    		oneRepayScheduleAdd.setRepayDate(resultDate);
	    	    		oneRepayScheduleAdd.setPayAmount(repayAmount);
	    	    		oneRepayScheduleAdd.setLnAmount(paidAmount);
	    	    		oneRepayScheduleAdd.setInterestAmount(loanInterest);
	    	    		oneRepayScheduleAdd.setBalance(balance);

	    	    		// 이메일 발송을 위한 내용 저장
//	    	    		OneSendCheckingEmail oneSendCheckingEmail = new OneSendCheckingEmail();
//	    	    		oneSendCheckingEmail.setLoanId(loanId);
//	    	    		oneSendCheckingEmail.setRepayCount(payCount);
//	    	    		oneSendCheckingEmail.setRepayDate(resultDate);
//	    	    		oneSendCheckingEmail.setPayAmount(repayAmount);
//	    	    		oneSendCheckingEmail.setLnAmount(paidAmount);
//	    	    		oneSendCheckingEmail.setInterestAmount(loanInterest);
//	    	    		oneSendCheckingEmail.setBalance(balance);
//	    	    		
//	    	    		oneSendCheckingEmailList.add(oneSendCheckingEmail);
//	    	    		
	    	    			System.out.println(oneRepayScheduleAdd);    		
//	    	    		if(!scheService.insertRepaySchedule(oneRepayScheduleAdd)) {							// 대출자 상환스케줄 생성		
//	    	    			commonUtil.sendBatchLogging("addRepaymentSchedule", "loanId : " + loanId + " payCount : " + payCount+ " resultDate : " + resultDate
//	    	    					+ " repayAmount : " + repayAmount + " paidAmount : " + paidAmount + " loanInterest : " + loanInterest + " balance : " + balance, "상환 스케줄 등록에 실패하였습니다.");
//	    	    			break;
//	    	    		} 
	    			}
	    			
	    			commonUtil.sendCheckManager(oneSendCheckingEmailList, "[청년5.5] 상환테이블 생성확인건(테스트용-추후 검수용으로 사용될 예정)-4/15일 확인하기!.");												// 메일발송 테이블에 쌓는 메서드 호출
	    		
		    		for(int k = 0; k < onePaymentInvestSchedules.size(); k++) {								//이벤트 대상자에 대한 정보 선택
		    			OneEventDiscount oneEventDiscount = eventService.selectEventDiscount(onePaymentInvestSchedules.get(k).getMid(), onePaymentInvestSchedules.get(k).getRegdatetime());
		    			
		    			double discountFee = 100;
		    			int discountMonth = 0;
		    			
		    			if(oneEventDiscount != null) {
		    				discountFee -= Double.parseDouble(oneEventDiscount.getEvent_discount());
		    				discountMonth = Integer.parseInt(oneEventDiscount.getEvent_discount_month());
		    			}
		    			
		    			double paidAmountTmt = 0; // 끝전정리를 위한 원금(paidAmount) 누적
		    			for(int j = 0 ; j < jsonRepaySchedules.length(); j++) {					// 투자자 정보 입력시작
		    				JSONObject repaySchedules = jsonRepaySchedules.getJSONObject(j);
		    				String payCount = repaySchedules.getString("payCount");
		    				String repayAmount = repaySchedules.getString("repayAmount");
		    				String paidAmount = repaySchedules.getString("paidAmount");			//원금(원리금-이자)
		    				String loanInterest = repaySchedules.getString("loanInterest");
		    				String balance = repaySchedules.getString("balance");
		    				String resultDate = repaySchedules.getString("resultDate");
		    				int dayEOM = repaySchedules.getInt("dayEOM");	   			
		    				
		    																					// 세금과 지방세에 대한 정보 선택
		    				OneRateInfo oneRateInfo = scheService.selectRateInfo(onePaymentInvestSchedules.get(k).getLevel(), onePaymentInvestSchedules.get(k).getService(), String.valueOf(repayInfoType));
		    				// feeRate = Double.parseDouble(oneRateInfo.getFee());					// 현재까지는 모두 0.024
		    				taxRate = Double.parseDouble(oneRateInfo.getTax());					// 현재까지는 모두 0.25	
		    				taxRateLocal = Double.parseDouble(oneRateInfo.getTaxLocal());		// 현재까지는 모두 0.025
		    				
		    				resultDate = WorkingDayCalculate(resultDate) == null ? resultDate : WorkingDayCalculate(resultDate);
		    	    		resultDate = WorkingDayCalculate(resultDate) == null ? resultDate : WorkingDayCalculate(resultDate);
		    	    		String[] repayDate = resultDate.split("-");
		    	    		
		    	    																			// 투자자가 투자한 비율(투자액/대출액)
	    	    			double loanRate = Double.parseDouble(onePaymentInvestSchedules.get(k).getPay()) / Double.parseDouble(onePaymentInvestSchedules.get(k).getLoanPay());
	    	    			long loanPayment = Long.parseLong(paidAmount) + Long.parseLong(balance);
	    	    			
	    	    			double inAmount = Math.ceil(Double.parseDouble(paidAmount) * loanRate);	// 투자금액 39원으로 떨어지면 세금 10원아닌 0원되는부분 Math.ceil로 처리 200429 
	    	    			double interest = Math.ceil(Double.parseDouble(loanInterest) * loanRate);	// 
	    	    			
	    	    			double taxPay = (interest * taxRate);
	    	    			double taxPayLocal = (interest * taxRateLocal);
	    	    			
	    	    			// 투자자 마지막 회차 끝전정리
	    	    			if(jsonRepaySchedules.length() == j+1) {
	    	    				inAmount = Math.floor((Double.parseDouble(onePaymentInvestSchedules.get(k).getPay()) - paidAmountTmt));
		    				}
	    	    			if(Integer.parseInt(onePaymentInvestSchedules.get(k).getLevel()) == 4) {
	    	        			// 200429 세금이 1000원일 경우도 절삭으로 적용
	    	    				if(taxPay <= 1000) {
	    	    					taxPay = 0;
	    	    					taxPayLocal = 0;
		    	    			}
	    	    			}

	    	    			long tax_real = ((long)Math.floor(taxPay) / 10) * 10;				// 일반적인경우 10원단위 절삭
	    	    			long tax_local_real = ((long)Math.floor(taxPayLocal) / 10) * 10;
	    	    			
	    	    			double result = inAmount + (interest - (tax_real + tax_local_real));	// 원금+(이자-(세금+지방세금))
	    	    			//double fee = result * feeRate;											// * 0.024
	    	    			//double tax = interest * 0.275;
	    	    			
	    	    			// 잔여투자금 구하는 함수
	    	    			double investBalance = investBalance(jsonRepaySchedules, Integer.parseInt(payCount));
	    	    			double fee = 0;

	    	    			/* 마지막 회차 끝전정리한 원금에 대한 수수료 부과해야함	7/1 적용예정	*/  
	    	    			if(!(jsonRepaySchedules.length() == j+1))
	    	    				fee = investBalance * loanRate * 0.024 * dayEOM/365;
	    	    			else
	    	    				fee = inAmount * 0.024 * dayEOM/365;
	    	    			
	    	    			//		7/1 삭제예정
	    	    			//	    	    			fee = investBalance * loanRate * 0.002;
	    	    			
	    	    			if(loanCate.equals("cate08"))														// 미혼모 투자자 수수료 면제
	    	    				fee = 0;
	    	    			
	    	    			if(discountMonth > 0) {
	    	    				fee *= (discountFee * 0.01);
	    	    				discountMonth--;
	    	    			}
	    	    																				// 투자자가 받게 될 실제 금액
	    	    			long investAmount = ((long)Math.ceil(inAmount) + (long)Math.ceil(interest) - ((long)Math.floor(fee) + tax_real + tax_local_real));
	    	    																							// floor 소숫점 버림
	    	    			String payDate = null;
	    	    			
	    	    			calendar = Calendar.getInstance();
	    	    			Year = (Integer.parseInt(repayDate[0]));
		    				Month = (Integer.parseInt(repayDate[1]) - 1);
	    	    			
		    				int repayDates = Integer.parseInt(repayDate[repayDate.length - 1]);
	    	    			
    	    				cal.set(Calendar.YEAR, Year);
    	    				cal.set(Calendar.MONTH, Month);
    	    				cal.set(Calendar.DAY_OF_MONTH, repayDates);
    	    				calendar.setTimeInMillis(cal.getTimeInMillis());
    	    				
    	    				payDate = WorkingDayCalculate(sdf.format(calendar.getTime()));
    	    				
    	    				while(payDate != null) {
    	    					payDate = WorkingDayCalculate(sdf.format(calendar.getTime()));
    	    					
    	    					if(payDate != null) {
	    	    					String[] payDates = payDate.split("-");
	        	    				
	        	    				Year = Integer.parseInt(payDates[0]);
	        	    				Month = Integer.parseInt(payDates[1]) - 1;
	        	    				repayDates = Integer.parseInt(payDates[2]);
	        	    				
	        	    				cal.set(Calendar.YEAR, Year);
	        	    				cal.set(Calendar.MONTH, Month);
	        	    				cal.set(Calendar.DAY_OF_MONTH, repayDates);
	        	    				calendar.setTimeInMillis(cal.getTimeInMillis());	        	    				
	        	    				
	        	    				payDate = WorkingDayCalculate(sdf.format(calendar.getTime()));
    	    					}
    	    				}
    	    				
    	    				if(payDate == null)
    	    					payDate = sdf.format(calendar.getTime());
    	    				
    	    				String[] payDates = payDate.split("-");
    	    				
    	    				Year = Integer.parseInt(payDates[0]);
    	    				Month = Integer.parseInt(payDates[1]) - 1;
    	    				repayDates = Integer.parseInt(payDates[2]);
	    	    			
	    	    			payDate = WorkingDayCalculate(payDate) == null ? payDate : WorkingDayCalculate(payDate);
	    	    			
	    	    			OnePaymentSchedule onePaymentSchedule = new OnePaymentSchedule();
	    	    			onePaymentSchedule.setMid(onePaymentInvestSchedules.get(k).getMid());
	    	    			onePaymentSchedule.setLoanId(loanId);
	    	    			onePaymentSchedule.setRepayCount(payCount);
	    	    			onePaymentSchedule.setPayDate(payDate);
	    	    			onePaymentSchedule.setPayGubun("A");
	    	    			onePaymentSchedule.setPayStatus("N");
	    	    			onePaymentSchedule.setLnAmount(String.valueOf(Math.ceil(inAmount)));
	    	    			onePaymentSchedule.setInterestAmount(String.valueOf(Math.ceil(interest)));
	    	    			onePaymentSchedule.setDelqAmount("0");
	    	    			onePaymentSchedule.setTax(String.valueOf(tax_real));
	    	    			onePaymentSchedule.setTaxLocal(String.valueOf(tax_local_real));
	    	    			onePaymentSchedule.setFee(String.valueOf(Math.floor(fee)));
	    	    			onePaymentSchedule.setPayAmount(String.valueOf(investAmount));
	    	    			
	    	    				    	    			 System.out.println(onePaymentSchedule);
//	    	    			if(!scheService.insertPaymentSchedule(onePaymentSchedule)) { 		//투자자 스케줄 생성
//		    	    			commonUtil.sendBatchLogging("addRepaymentSchedule", "loanId : " + loanId + " payCount : " + payCount+ " inAmount : " + String.valueOf(Math.ceil(inAmount))
//		    	    					+ " interest : " + String.valueOf(Math.ceil(interest)) + " tax_real : " + tax_real + " tax_local_real : " + tax_local_real + " fee : " + fee
//		    	    					+ " investAmount : " + investAmount + " getMid : " + onePaymentInvestSchedules.get(k).getMid(), "정산 스케줄 등록에 실패하였습니다.");
//		    	    			break;
//		    	    		}
	    	    			
							//	    	    			System.out.println(loanId + "	" + payCount+ "	" + String.valueOf(Math.ceil(inAmount)) + "	" + String.valueOf(Math.floor(fee)) + "	" + String.valueOf(Math.ceil(interest)) + "	" + String.valueOf(tax_real));
							//					+ " interest : " + String.valueOf(Math.ceil(interest)) + " tax_real : " + tax_real + " tax_local_real : " + tax_local_real + " fee : " + fee
							//					+ " investAmount : " + investAmount + " getMid : " + onePaymentInvestSchedules.get(k).getMid(), "정산 스케줄 등록(200416)에 실패하였습니다.");

	    	    			paidAmountTmt += Math.ceil(inAmount);
	    	    		}
	    			}
		    		
	    			// 상환스케줄 등록(i_exec_repaybatch N=>Y로 변경)
//	    			if(scheService.updateRepayScheduleState(loanId)) {
//	    				commonUtil.sendBatchLogging("addRepaymentSchedule", "loanId : " + loanId, "상환 스케줄 등록이 완료되었습니다.");
//	    			} else {
//	    				commonUtil.sendBatchLogging("addRepaymentSchedule", "loanId : " + loanId, "상환 스케줄 상태 업데이트가 실패하였습니다.");
//	    				break;
//	    			}
	    		}
    		}
            ResponseResult response = new ResponseResult();        
            response.setState(200);
//            response.setMessage("동일한건 : " + cntMatch + ", 다른건 : " + cntMisMatch + ", 정상적으로 처리하였습니다.");
            
            
//            commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
            
            return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    	} catch (Throwable t) {
    		commonUtil.sendBatchLogging("addRepaymentSchedule", "exception error!!", t.getMessage());
//    		Slack.api.call(new SlackMessage("#young-server","jhlee","상환 스케줄러 생성배치 실패"));
    		throw new RuntimeException(t.getMessage());
    	}
    }

	// 세금 및 지방세 검증  
	// V200722, (최종수정 V200805)
	@ApiOperation(value = "세금검증_200722")
    @RequestMapping("/verification/tax200722")
    public ResponseEntity<ResponseResult> taxVerification(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
//      JSONObject jsonRequest = (JSONObject)jsonMember.get("request");

        try {
//    	if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) 
    		{

//    			List<OneRepayScheduleInfo> oneRepayScheduleInfos = scheService.selectRepayScheduleInfoTest("2038");
//    			List<OneRepayScheduleInfo> oneRepayScheduleInfos = scheService.selectRepayScheduleInfoTest("100");
    			
    			// cpas_payment_schedule_200722
    			// 4400보다 작은 채권 갯수 확인
    			List<OneRepayScheduleInfo> oneRepayScheduleInfos = scheService.selectRepayScheduleInfoTestAll();
    			
	    		Calendar cal = Calendar.getInstance();
	    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    		
	    		double taxRate = 0;
    			double taxRateLocal = 0;
	    		
	    		for(int i = 0; i < oneRepayScheduleInfos.size(); i++) {
	    			String mid = oneRepayScheduleInfos.get(i).getMid();
	    			String loanId = oneRepayScheduleInfos.get(i).getLoanId();
	    			String loanPay = oneRepayScheduleInfos.get(i).getLoanPay();
	    			String loanDay = oneRepayScheduleInfos.get(i).getLoanDay();
	    			String yearPlus = oneRepayScheduleInfos.get(i).getYearPlus();
	    			String repayInfo = oneRepayScheduleInfos.get(i).getRepayInfo();
	    			String repayDay = oneRepayScheduleInfos.get(i).getRepayDay();
	    			String execDate = oneRepayScheduleInfos.get(i).getExecDate();
	    			String loanCate = oneRepayScheduleInfos.get(i).getLoanCate();
	    	    			
	    			//............. 대출일보다 첫번째 상환일이 15일이하면 +1월 해야함;;;  
	    			Calendar calendar = Calendar.getInstance();

	    			// 현재가 아닌 과거 데이터 검증이기 떄문에 	    			
//	    			int Year = calendar.get(Calendar.YEAR);													//현재년도
//					int Month = calendar.get(Calendar.MONTH);												//현재월
	    			int Year = Integer.parseInt(execDate.split("-")[0]);													//현재년도
					int Month = Integer.parseInt(execDate.split("-")[1])-1;												//현재월

	    			
	    			int Day = Integer.parseInt(execDate.split("-")[2]);										//대출실행일
	    			
					if(Day > Integer.parseInt(repayDay))													//대출실행일 > 상환예정일보다 크면(예:대출실행일 02/16, 상환일 02/15)
						Month += 1;
					
	    	    	cal.set(Calendar.YEAR, Year);
	    			cal.set(Calendar.MONTH, Month);
	    			cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(repayDay));								// 상환예정일로 세팅하고
	    			calendar.setTimeInMillis(cal.getTimeInMillis());
	    	    	
					String startDate = sdf.format(calendar.getTime());										// 상환 시작일로 값 넣어서
	    			long diffDate = getDiffOfDate(execDate, startDate);										// 1회차 일수 계산(1회차상환일-대출시작일)
	    			JSONArray jsonRepaySchedules = null;
	    			
	    			int repayMonth = 0;
	    			int repayInfoType = 0;
	    			double feeRate = 0;
	    			
	    			if(diffDate < 15)
	    				repayMonth = 1;																		//대출일보다 첫번째 상환일이 15일이하면 repayMonth + 1월
	    			
	    			
	    			switch(repayInfo) {
		    			case "원리금균등상환" :
		    				repayInfoType = 1;							//대출액(loanPay), 연이율(yearPlus), 대출기간(loanDay), 대출상환일(repayDay:5,15,25), 상환시작일(startDate), 대출실행일(execDate), 상환일-대출일(diffDate), 추가+1달여부(repayMonth)
		    				jsonRepaySchedules = createPrincipalSchedule(Double.parseDouble(loanPay), Double.parseDouble(yearPlus) * 0.01, Integer.parseInt(loanDay), repayDay, startDate, execDate, diffDate, repayMonth);
		    				break;
	    			
		    			case "만기일시상환" :
		    				repayInfoType = 2;
		    				jsonRepaySchedules = createMaturitySchedule(Double.parseDouble(loanPay), Double.parseDouble(yearPlus) * 0.01, Integer.parseInt(loanDay), repayDay, startDate, execDate, diffDate, repayMonth);
		    				break;
	    			}
	    			
	    			List<OneSendCheckingEmail> oneSendCheckingEmailList = new ArrayList<OneSendCheckingEmail>();	// 메일 보내기 위한 리스트
	    			
	    																									// 투자자 정보선택
	    			List<OnePaymentInvestSchedule> onePaymentInvestSchedules = scheService.selectPaymentInvestSchedule(loanId);
		    			    			
	    			for(int j = 0 ; j < jsonRepaySchedules.length(); j++) {									// 대출자 정보 입력시작			
	    				JSONObject repaySchedules = jsonRepaySchedules.getJSONObject(j);
	    				String payCount = repaySchedules.getString("payCount");
	    				String repayAmount = repaySchedules.getString("repayAmount");
	    				String paidAmount = repaySchedules.getString("paidAmount");
	    				String loanInterest = repaySchedules.getString("loanInterest");
	    				String balance = repaySchedules.getString("balance");
	    				String resultDate = repaySchedules.getString("resultDate");
	    	    		
	    				resultDate = WorkingDayCalculate(resultDate) == null ? resultDate : WorkingDayCalculate(resultDate);
	    				
	    	    		OneRepayScheduleAdd oneRepayScheduleAdd = new OneRepayScheduleAdd();
	    	    		oneRepayScheduleAdd.setLoanId(loanId);
	    	    		oneRepayScheduleAdd.setRepayCount(payCount);
	    	    		oneRepayScheduleAdd.setRepayDate(resultDate);
	    	    		oneRepayScheduleAdd.setPayAmount(repayAmount);
	    	    		oneRepayScheduleAdd.setLnAmount(paidAmount);
	    	    		oneRepayScheduleAdd.setInterestAmount(loanInterest);
	    	    		oneRepayScheduleAdd.setBalance(balance);

	    	    		// 이메일 발송을 위한 내용 저장
//	    	    		OneSendCheckingEmail oneSendCheckingEmail = new OneSendCheckingEmail();
//	    	    		oneSendCheckingEmail.setLoanId(loanId);
//	    	    		oneSendCheckingEmail.setRepayCount(payCount);
//	    	    		oneSendCheckingEmail.setRepayDate(resultDate);
//	    	    		oneSendCheckingEmail.setPayAmount(repayAmount);
//	    	    		oneSendCheckingEmail.setLnAmount(paidAmount);
//	    	    		oneSendCheckingEmail.setInterestAmount(loanInterest);
//	    	    		oneSendCheckingEmail.setBalance(balance);
//	    	    		
//	    	    		oneSendCheckingEmailList.add(oneSendCheckingEmail);
//	    	    		
//	    	    			System.out.println(oneRepayScheduleAdd);    		
//	    	    		if(!scheService.insertRepaySchedule(oneRepayScheduleAdd)) {							// 대출자 상환스케줄 생성		
//	    	    			commonUtil.sendBatchLogging("addRepaymentSchedule", "loanId : " + loanId + " payCount : " + payCount+ " resultDate : " + resultDate
//	    	    					+ " repayAmount : " + repayAmount + " paidAmount : " + paidAmount + " loanInterest : " + loanInterest + " balance : " + balance, "상환 스케줄 등록에 실패하였습니다.");
//	    	    			break;
//	    	    		} 
	    			}
	    			
	    			commonUtil.sendCheckManager(oneSendCheckingEmailList, "[청년5.5] 상환테이블 생성확인건(테스트용-추후 검수용으로 사용될 예정)-4/15일 확인하기!.");												// 메일발송 테이블에 쌓는 메서드 호출
	    		
		    		for(int k = 0; k < onePaymentInvestSchedules.size(); k++) {								//이벤트 대상자에 대한 정보 선택
		    			OneEventDiscount oneEventDiscount = eventService.selectEventDiscount(onePaymentInvestSchedules.get(k).getMid(), onePaymentInvestSchedules.get(k).getRegdatetime());
		    			
		    			double discountFee = 100;
		    			int discountMonth = 0;
		    			
		    			if(oneEventDiscount != null) {
		    				discountFee -= Double.parseDouble(oneEventDiscount.getEvent_discount());
		    				discountMonth = Integer.parseInt(oneEventDiscount.getEvent_discount_month());
		    			}
		    			
		    			double paidAmountTmt = 0; // 끝전정리를 위한 원금(paidAmount) 누적
		    			for(int j = 0 ; j < jsonRepaySchedules.length(); j++) {					// 투자자 정보 입력시작
		    				JSONObject repaySchedules = jsonRepaySchedules.getJSONObject(j);
		    				String payCount = repaySchedules.getString("payCount");
		    				String repayAmount = repaySchedules.getString("repayAmount");
		    				String paidAmount = repaySchedules.getString("paidAmount");			//원금(원리금-이자)
		    				String loanInterest = repaySchedules.getString("loanInterest");
		    				String balance = repaySchedules.getString("balance");
		    				String resultDate = repaySchedules.getString("resultDate");
		    				int dayEOM = repaySchedules.getInt("dayEOM");	   			
		    				
		    																					// 세금과 지방세에 대한 정보 선택
		    				OneRateInfo oneRateInfo = scheService.selectRateInfo(onePaymentInvestSchedules.get(k).getLevel(), onePaymentInvestSchedules.get(k).getService(), String.valueOf(repayInfoType));
		    				// feeRate = Double.parseDouble(oneRateInfo.getFee());					// 현재까지는 모두 0.024
		    				taxRate = Double.parseDouble(oneRateInfo.getTax());					// 현재까지는 모두 0.25	
		    				taxRateLocal = Double.parseDouble(oneRateInfo.getTaxLocal());		// 현재까지는 모두 0.025
		    				
		    				resultDate = WorkingDayCalculate(resultDate) == null ? resultDate : WorkingDayCalculate(resultDate);
		    	    		resultDate = WorkingDayCalculate(resultDate) == null ? resultDate : WorkingDayCalculate(resultDate);
		    	    		String[] repayDate = resultDate.split("-");
		    	    		
		    	    																			// 투자자가 투자한 비율(투자액/대출액)
	    	    			double loanRate = Double.parseDouble(onePaymentInvestSchedules.get(k).getPay()) / Double.parseDouble(onePaymentInvestSchedules.get(k).getLoanPay());
	    	    			long loanPayment = Long.parseLong(paidAmount) + Long.parseLong(balance);
	    	    			
	    	    			double inAmount = Math.ceil(Double.parseDouble(paidAmount) * loanRate);	// 투자금액 39원으로 떨어지면 세금 10원아닌 0원되는부분 Math.ceil로 처리 200429 
	    	    			double interest = Math.ceil(Double.parseDouble(loanInterest) * loanRate);	// 
	    	    			
	    	    			// 세금의 10%로 계산하지 않고, 지방세는 따로 계산
	    	    			double taxPay = (interest * taxRate);								// 투자자이자 * 세율
	    	    			double taxPayLocal = (interest * taxRateLocal);						// 투자자이자 * 지방세율
	    	    			
	    	    			// 투자자 마지막 회차 끝전정리
	    	    			if(jsonRepaySchedules.length() == j+1) {
	    	    				inAmount = Math.floor((Double.parseDouble(onePaymentInvestSchedules.get(k).getPay()) - paidAmountTmt));
		    				}
	    	    			if(Integer.parseInt(onePaymentInvestSchedules.get(k).getLevel()) == 4) {
	    	        			// 200429 세금이 1000원일 경우도 절삭으로 적용
	    	    				if(taxPay <= 1000) {
	    	    					taxPay = 0;
	    	    					taxPayLocal = 0;
		    	    			}
	    	    			}

	    	    			long tax_real = ((long)Math.floor(taxPay) / 10) * 10;				// 일반적인경우 10원단위 절삭
	    	    			long tax_local_real = ((long)Math.floor(taxPayLocal) / 10) * 10;
	    	    			
	    	    			double result = inAmount + (interest - (tax_real + tax_local_real));	// 원금+(이자-(세금+지방세금))
	    	    			//double fee = result * feeRate;											// * 0.024
	    	    			//double tax = interest * 0.275;
	    	    			
	    	    			// 잔여투자금 구하는 함수
	    	    			double investBalance = investBalance(jsonRepaySchedules, Integer.parseInt(payCount));
	    	    			double fee = 0;

	    	    			/* 마지막 회차 끝전정리한 원금에 대한 수수료 부과해야함	7/1 적용예정	*/  
	    	    			if(!(jsonRepaySchedules.length() == j+1))
	    	    				fee = investBalance * loanRate * 0.024 * dayEOM/365;
	    	    			else
	    	    				fee = inAmount * 0.024 * dayEOM/365;
	    	    			
	    	    			//		7/1 삭제예정
	    	    			//	    	    			fee = investBalance * loanRate * 0.002;
	    	    			
	    	    			if(loanCate.equals("cate08"))														// 미혼모 투자자 수수료 면제
	    	    				fee = 0;
	    	    			
	    	    			if(discountMonth > 0) {
	    	    				fee *= (discountFee * 0.01);
	    	    				discountMonth--;
	    	    			}
	    	    																				// 투자자가 받게 될 실제 금액
	    	    			long investAmount = ((long)Math.ceil(inAmount) + (long)Math.ceil(interest) - ((long)Math.floor(fee) + tax_real + tax_local_real));
	    	    																							// floor 소숫점 버림
	    	    			String payDate = null;
	    	    			
	    	    			calendar = Calendar.getInstance();
	    	    			Year = (Integer.parseInt(repayDate[0]));
		    				Month = (Integer.parseInt(repayDate[1]) - 1);
	    	    			
		    				int repayDates = Integer.parseInt(repayDate[repayDate.length - 1]);
	    	    			
    	    				cal.set(Calendar.YEAR, Year);
    	    				cal.set(Calendar.MONTH, Month);
    	    				cal.set(Calendar.DAY_OF_MONTH, repayDates);
    	    				calendar.setTimeInMillis(cal.getTimeInMillis());
    	    				
    	    				payDate = WorkingDayCalculate(sdf.format(calendar.getTime()));
    	    				
    	    				while(payDate != null) {
    	    					payDate = WorkingDayCalculate(sdf.format(calendar.getTime()));
    	    					
    	    					if(payDate != null) {
	    	    					String[] payDates = payDate.split("-");
	        	    				
	        	    				Year = Integer.parseInt(payDates[0]);
	        	    				Month = Integer.parseInt(payDates[1]) - 1;
	        	    				repayDates = Integer.parseInt(payDates[2]);
	        	    				
	        	    				cal.set(Calendar.YEAR, Year);
	        	    				cal.set(Calendar.MONTH, Month);
	        	    				cal.set(Calendar.DAY_OF_MONTH, repayDates);
	        	    				calendar.setTimeInMillis(cal.getTimeInMillis());	        	    				
	        	    				
	        	    				payDate = WorkingDayCalculate(sdf.format(calendar.getTime()));
    	    					}
    	    				}
    	    				
    	    				if(payDate == null)
    	    					payDate = sdf.format(calendar.getTime());
    	    				
    	    				String[] payDates = payDate.split("-");
    	    				
    	    				Year = Integer.parseInt(payDates[0]);
    	    				Month = Integer.parseInt(payDates[1]) - 1;
    	    				repayDates = Integer.parseInt(payDates[2]);
	    	    			
	    	    			payDate = WorkingDayCalculate(payDate) == null ? payDate : WorkingDayCalculate(payDate);
	    	    			
	    	    			OnePaymentSchedule onePaymentSchedule = new OnePaymentSchedule();
	    	    			onePaymentSchedule.setMid(onePaymentInvestSchedules.get(k).getMid());
	    	    			onePaymentSchedule.setLoanId(loanId);
	    	    			onePaymentSchedule.setRepayCount(payCount);
	    	    			onePaymentSchedule.setPayDate(payDate);
	    	    			onePaymentSchedule.setPayGubun("A");
	    	    			onePaymentSchedule.setPayStatus("N");
	    	    			onePaymentSchedule.setLnAmount(String.valueOf(Math.ceil(inAmount)));
	    	    			onePaymentSchedule.setInterestAmount(String.valueOf(Math.ceil(interest)));
	    	    			onePaymentSchedule.setDelqAmount("0");
	    	    			onePaymentSchedule.setTax(String.valueOf(tax_real));
	    	    			onePaymentSchedule.setTaxLocal(String.valueOf(tax_local_real));
	    	    			onePaymentSchedule.setFee(String.valueOf(Math.floor(fee)));
	    	    			onePaymentSchedule.setPayAmount(String.valueOf(investAmount));
	    	    			
	    	    			/////////////////////////////////////////////////////
	    	    			// 수정한데 여기밖에 없음!
	    	    			
	    	    			PaymentScheduleItem paymentScheduleItem = repaymentService.selectPaymentScheduleItemTest(onePaymentInvestSchedules.get(k).getMid(), loanId, payCount);

	    	    			if( paymentScheduleItem.getPayStatus().equals("N") ) {

	    	    				// 이자에 연체금액 더한 값에 세금과 지방세 구해야 함!!!!		/////////////////////
	    	    				double interest_afterOverdue = Math.ceil(interest) + paymentScheduleItem.getOverdue();
	    	    				double taxPay_afterOverdue = (interest_afterOverdue * taxRate);
	    	    				double taxPayLocal_afterOverdue = (interest_afterOverdue * taxRateLocal);
	    	    				long tax_real_afterOverdue = ((long)Math.floor(taxPay_afterOverdue) / 10) * 10;
	    	    				long tax_local_real_afterOverdue = ((long)Math.floor(taxPayLocal_afterOverdue) / 10) * 10;
	    	    				
	    	    				if(Integer.parseInt(onePaymentInvestSchedules.get(k).getLevel()) == 4) {
		    	        			if(tax_real_afterOverdue <= 1000) {
		    	    					tax_real_afterOverdue = 0;
		    	    					tax_local_real_afterOverdue = 0;
			    	    			}
		    	    			}
		    	    			/////////////////////////////////////////////////////////////////////
	    	    				// 매각은 빼야지? ㅋㅋㅋㅋㅋ
	    	    				
	    	    				
	    	    				// 이자가 다른상태면 오히려 지금 계산한 세금값이 잘못될수 있음!!!(잘못된 이자 확인후 수정 예정)
	    	    				
	    	    				if( (paymentScheduleItem.getInterest() != Math.ceil(interest)))
	    	    				//if( (paymentScheduleItem.getTax() != tax_real_afterOverdue) || (paymentScheduleItem.getTaxLocal() != tax_local_real_afterOverdue) ) 
// 업데이트용
//	    	    					if(!repaymentService.updatePaymentScheduleTax(onePaymentInvestSchedules.get(k).getMid(), loanId, payCount, String.valueOf(tax_real_afterOverdue), String.valueOf(tax_local_real_afterOverdue))){
//			    	    				System.out.println("실패한 검증값 : 	" + onePaymentInvestSchedules.get(k).getMid() + "	" + loanId + "	" + payCount+ "	" + payDate+ "	" + paymentScheduleItem.getPayStatus()+ "	" + String.valueOf(Math.ceil(inAmount)) + "	" + 
//			    	    				String.valueOf(interest) +"	" + paymentScheduleItem.getOverdue() + "	" +  (interest + paymentScheduleItem.getOverdue()) + "	" +
//			    	    				String.valueOf(tax_real_afterOverdue) + "	" + tax_local_real_afterOverdue + "	" + String.valueOf(Math.floor(fee)) +
//										"		DB	" + paymentScheduleItem.getInterest() +"	" + paymentScheduleItem.getOverdue() + "	" + paymentScheduleItem.getTax() + "	" + paymentScheduleItem.getTaxLocal() + "	" + paymentScheduleItem.getFee());	    	    			
//	    	    					} else {
//			    	    				System.out.println("검증값 : 	" + onePaymentInvestSchedules.get(k).getMid() + "	" + loanId + "	" + payCount+ "	" + payDate+ "	" + paymentScheduleItem.getPayStatus()+ "	" + String.valueOf(Math.ceil(inAmount)) + "	" + 
//			    	    				String.valueOf(interest) +"	" + paymentScheduleItem.getOverdue() + "	" +  (interest + paymentScheduleItem.getOverdue()) + "	" +
//			    	    				String.valueOf(tax_real_afterOverdue) + "	" + tax_local_real_afterOverdue + "	" + String.valueOf(Math.floor(fee)) +
//										"		DB	" + paymentScheduleItem.getInterest() +"	" + paymentScheduleItem.getOverdue() + "	" + paymentScheduleItem.getTax() + "	" + paymentScheduleItem.getTaxLocal() + "	" + paymentScheduleItem.getFee());	    	    			
//	    	    					}
// 확인용	    	    					

				    	    				System.out.println("단순검증값 : 	" + onePaymentInvestSchedules.get(k).getMid() + "	" + loanId + "	" + payCount+ "	" + payDate+ "	" + paymentScheduleItem.getPayStatus()+ "	" + String.valueOf(Math.ceil(inAmount)) + "	" + 
				    	    				String.valueOf(interest) +"	" + paymentScheduleItem.getOverdue() + "	" +  (interest + paymentScheduleItem.getOverdue()) + "	" +
				    	    				String.valueOf(tax_real_afterOverdue) + "	" + tax_local_real_afterOverdue + "	" + String.valueOf(Math.floor(fee)) +
											"		DB	" + paymentScheduleItem.getInterest() +"	" + paymentScheduleItem.getOverdue() + "	" + paymentScheduleItem.getTax() + "	" + paymentScheduleItem.getTaxLocal() + "	" + paymentScheduleItem.getFee());	    	    			
	    	    			
	    	    			}
	    	    			/////////////////////////////////////////////////////
	    	    			
	    	    			
	    	    			
	    	    			
	    	    			//	    	    			 System.out.println(onePaymentSchedule);
//	    	    			if(!scheService.insertPaymentSchedule(onePaymentSchedule)) { 		//투자자 스케줄 생성
//		    	    			commonUtil.sendBatchLogging("addRepaymentSchedule", "loanId : " + loanId + " payCount : " + payCount+ " inAmount : " + String.valueOf(Math.ceil(inAmount))
//		    	    					+ " interest : " + String.valueOf(Math.ceil(interest)) + " tax_real : " + tax_real + " tax_local_real : " + tax_local_real + " fee : " + fee
//		    	    					+ " investAmount : " + investAmount + " getMid : " + onePaymentInvestSchedules.get(k).getMid(), "정산 스케줄 등록에 실패하였습니다.");
//		    	    			break;
//		    	    		}
	    	    			
							//	    	    			System.out.println(loanId + "	" + payCount+ "	" + String.valueOf(Math.ceil(inAmount)) + "	" + String.valueOf(Math.floor(fee)) + "	" + String.valueOf(Math.ceil(interest)) + "	" + String.valueOf(tax_real));
							//					+ " interest : " + String.valueOf(Math.ceil(interest)) + " tax_real : " + tax_real + " tax_local_real : " + tax_local_real + " fee : " + fee
							//					+ " investAmount : " + investAmount + " getMid : " + onePaymentInvestSchedules.get(k).getMid(), "정산 스케줄 등록(200416)에 실패하였습니다.");

	    	    			paidAmountTmt += Math.ceil(inAmount);
	    	    		}
	    			}
		    		
	    			// 상환스케줄 등록(i_exec_repaybatch N=>Y로 변경)
//	    			if(scheService.updateRepayScheduleState(loanId)) {
//	    				commonUtil.sendBatchLogging("addRepaymentSchedule", "loanId : " + loanId, "상환 스케줄 등록이 완료되었습니다.");
//	    			} else {
//	    				commonUtil.sendBatchLogging("addRepaymentSchedule", "loanId : " + loanId, "상환 스케줄 상태 업데이트가 실패하였습니다.");
//	    				break;
//	    			}
	    		}
    		}
            ResponseResult response = new ResponseResult();        
            response.setState(200);
//            response.setMessage("동일한건 : " + cntMatch + ", 다른건 : " + cntMisMatch + ", 정상적으로 처리하였습니다.");
            
            
//            commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
            
            return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    	} catch (Throwable t) {
    		commonUtil.sendBatchLogging("addRepaymentSchedule", "exception error!!", t.getMessage());
//    		Slack.api.call(new SlackMessage("#young-server","jhlee","상환 스케줄러 생성배치 실패"));
    		throw new RuntimeException(t.getMessage());
    	}
        
	}
	
	// 세금 및 지방세 검증 이후 연체시에 세금 및 지방세 검증  
		// V200722, (최종수정 V200804)
		@ApiOperation(value = "연체시_세금검증_200804")
	    @RequestMapping("/verification/overdue_tax")
	    public ResponseEntity<ResponseResult> overduetaxVerification(@RequestBody String requestString) throws Exception {
	    	
	    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
	        commonUtil.sendRequestLogging(mapping_url, requestString);
	    	
	        JSONObject jsonMember = new JSONObject(requestString);
//	      JSONObject jsonRequest = (JSONObject)jsonMember.get("request");

	        try {
//	    	if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) 
		
	        	List<String> overdueLoanInfo = repaymentService.selectOverdueLoanInfo();					// cps-정산되지 않은 전체 채권 선택(p_pay_status=N)
    			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");					
    			
    			for(int l = 0; l < overdueLoanInfo.size(); l++) {
	    			String loanId = overdueLoanInfo.get(l);
	    	        String prepayDate = formatter.format(new Date());
	    	        																						// ml-대출개월수, 이자율(5.5), 상환계좌 선택  
	    	        OneRepaymentDataInfo oneRePaymentDataInfo = repaymentService.selectRepaymentDataInfo(loanId);
	    	        List<OneRepaymentCheckCount> oneRepaymentCheckCounts = repaymentService.selectRepaymentCheckCount(loanId, prepayDate); //crs-상환해야 하는 회차(오늘날짜이전)에 원리금,회차정보 모두선택
	    	        																		
	    	        																						// IB_FB_P2P_IP-해당 대출자 가상계좌의 입금금액 합
	    	        long totalDepositAmt = depositService.selectRepaymentTotalDepositAmt(oneRePaymentDataInfo.getLoanAccntNo());
	    	        long deposit = 0;
	    	        //String count = "0";
	    	        
	    	        // withdraw에서 출금한 내용들 있으면 대출자 가상계좌 입금액의 합(totalDepositAmt) 에서 공제해야함!  
	    	        // 추가3줄(cwt-혹시 다른경로로 환불해준 금액이 있으면 해당금액 공제)
	    	    	String oneMid = repaymentService.selectOneMid(loanId);
	    	    	long totalRefundAmt = repaymentService.selectRepaymentTotalRefundAmt(oneMid, loanId);
	    	    	if((oneMid != null) && (totalRefundAmt != 0))
	    	    		totalDepositAmt -=  totalRefundAmt;
	    	    	
	    	    	
	    	        
    	        	for(int i = 0; i < oneRepaymentCheckCounts.size(); i++) {								// 상환하지 않은 회차에서 가상계좌의 입금액만큼 차감하면서 상환!   
	    	        	String payAmount = oneRepaymentCheckCounts.get(i).getPayAmount();					// 원리금
	    	        	totalDepositAmt -= Long.parseLong(payAmount);  										// 가상계좌합-원리금
	    	        	
	    	        	if(totalDepositAmt < 0) {															// 가상계좌에 0원 이하면 원리금 차감 취소후 반복문 빠져나감															
	    	        		//count = oneRepaymentCheckCounts.get(i).getCount();
	    	        		deposit = totalDepositAmt + Long.parseLong(payAmount);		
	    	        		break;
	    	        	}
	    	        	
	    	        	if(totalDepositAmt >= 0) {															// 가상계좌에 잔액이 원리금 이상이면 
	    	        		if(repaymentService.selectLoanCountByPayStatus(loanId, oneRepaymentCheckCounts.get(i).getCount()).equals("N"))
	    	        			repaymentService.updateOrverDueState(loanId, oneRepaymentCheckCounts.get(i).getCount());	//coh-state=N을 S로 변경 ; cps는 언제 C로 변경?
	    	        		oneRepaymentCheckCounts.subList(i, i + 1).clear();								// 잔고가 남아있으면 최근 N정보를 상환처리 해야하기 때문에, 처리한 N을 삭제하고  
	    	        		i--;																			// i-1을 해서 다음 회차를 가르키게 함(처리한 필드를 지우고 i-1 하면 그 다음 N을 가리키게 됨)  
	    	        	}
    	        	}
    	        	
    	        	
    	        	// 상환해야 하는 최고 횟차(3회차 내고 4회차 안냈으면 maxCount=4)
    	        	int maxCount = 0;
    	        	for(int i = 0; i < oneRepaymentCheckCounts.size(); i++) {
    	        		if(Integer.parseInt(oneRepaymentCheckCounts.get(i).getCount()) > maxCount)
    	        			maxCount = Integer.parseInt(oneRepaymentCheckCounts.get(i).getCount());
    	        	}
    	        	
    	        	String count = String.valueOf(maxCount);
    	        	double[] loanRates = {3, 3, 3, 3};
    	        	int overDueCount = 0;
    	        	int overDueState = 0;
    	        	
    	        	if(Integer.parseInt(count) > 0) {														// 연체가 있으면.. 							
    	        		List<OneRepaymentScheduleItem> oneRepaymentScheduleItems = repaymentService.selectRepaymentScheduleItem2(loanId, prepayDate, count); // 연체건중에 최근회차정보 선택
		    	        String balance = repaymentService.selectOverdueBalance(loanId);						// 상환해야하는 잔금표시(lnAmount+r_balance)
		    	        
		    	        double payAmountSum = 0;
		    	        
		    	        JSONArray jsonResult = new JSONArray();
		    	        
		    	        for(int i = 0; i < oneRepaymentScheduleItems.size(); i++) {
		    	        	String payAmount = oneRepaymentScheduleItems.get(i).getPayAmount();
		    	        	String payCount = oneRepaymentScheduleItems.get(i).getCount();
		    	        	String payDate = oneRepaymentScheduleItems.get(i).getPayDate();
		    	        	String nextDate = oneRepaymentScheduleItems.get(i).getNextDate();
		    	        	String rDelqAmount = oneRepaymentScheduleItems.get(i).getRDelqAmount();
		    	        	//String pDelqAmount = oneRepaymentScheduleItems.get(i).getPDelqAmount();
		    	        	
		    	        	if(nextDate == null)
		    	        		nextDate = prepayDate;
		    	        	
		    	        	String toDay = nextDate;
		    	        	// String overDuePayment = "0";
		    	        	double overDuePayment = 0;
		    	        																					//정산하지 않은 갯수중에, 상환되지 않은, 오늘날짜 이전건이(예:4~10회차).....
		    	        																					//2개 이상 있으면 3반환, 아니면 정산못한 갯수반환(1혹은2) 
		    	        	overDueCount = repaymentService.selectOverdueCount2(loanId, count);	
		    	        	
		    	        	
		    	        	// 2019년도에 가산이자 7,8,8,9였을때 사용했던 수식
		    	        	// 2020년도 현재 가산이자 3%로 동일
		    	        	// 여기서부터 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		    	        	
		    	        	if(overDueCount > 0)				//  기한이익이면 2
		    	        		overDueCount--;
		    	        	
		    	        	if(overDueCount > 3)				//
		    	        		overDueCount = 3;
		    	        	
		    	        	long dateDiff = getDiffOfDate(prepayDate, nextDate);
		    	        	
		    	        	if(dateDiff > 0)
		    	        		toDay = prepayDate;
		    	        	
		    	        	double loanRate = (loanRates[overDueCount] + Double.parseDouble(oneRePaymentDataInfo.getInterestRate())) * 0.01;
		
		    	        	
		    	        	
		    	        	String currentDate = repaymentService.selectOverdueCurrentDate(loanId, payCount);	// 연체에 대한 가장 최근 날짜 선택
	    	        		if(currentDate == null || currentDate.isEmpty()) {
	    	        			currentDate = payDate;
	    	        			rDelqAmount = "0";
	    	        		}
	    	        		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 여기까지, 옛날에 사용하던 방식;;
	    	        		
	    	        		
	    	        		// 상환회차부터 오늘(혹은 다음회차)까지의 일수 구하기
	    	        		String dateFrom = payDate;
	    	        		
	    	        		String dateTo = prepayDate;					// 다음상환회차보다 오늘이 크면 다음상환회차를 dateTo로 
	    	        		if(getDiffOfDate(prepayDate, nextDate) < 0)
		    	        		dateTo = nextDate;
		    	        		
	    	        		long dateDiff2 = getDiffOfDate(dateFrom, dateTo);
	    	        		
		    	        	
		    	        	if(overDueCount < 2) {
		    	        		overDueState = 1;
		    	        		
		    	        		//payAmountSum += Double.parseDouble(payAmount);
		    	        		// payAmountSum = Double.parseDouble(repaymentService.selectOverduePayAmountSum(loanId, prepayDate));	// 오늘 이전에 정산되지 않은 정보 선택
		    	        		
		    	        		
		    	        		if (overDueCount == 0) 						// 1번 연체
		    	        			overDuePayment = getDelqAmt(Double.parseDouble(payAmount), Double.parseDouble(oneRePaymentDataInfo.getInterestRate())*0.01,
			    	        				loanRates[overDueCount]*0.01, 365, dateDiff2);
		    	        		else if (overDueCount == 1){ 				// 2번 연체
		    	        			overDuePayment = getDelqAmt(Double.parseDouble(payAmount), Double.parseDouble(oneRePaymentDataInfo.getInterestRate())*0.01,
			    	        				loanRates[overDueCount]*0.01, 365, dateDiff2);
		    	        			overDuePayment = overDuePayment *2;
		    	        		}
		    	        		
		    	        		
							// overDuePayment = createPrePaymentInterest(payAmountSum
							// , loanRate
							// , Integer.parseInt(oneRePaymentDataInfo.getLoanDay()), currentDate, toDay);					// 연체 일수에 따른 연채율 반환
		    	        	} else {
		    	        		//////기한 이익 상실//////
		    	        		overDueState = 2;
		    	        		

		               			double delGihan = repaymentService.selectGihanRepayment(overdueLoanInfo.get(l)); 
		               			
		    	        		overDuePayment = getDelqAmt(delGihan, Double.parseDouble(oneRePaymentDataInfo.getInterestRate())*0.01,
		    	        				loanRates[overDueCount]*0.01, 365, dateDiff2);
							//	overDuePayment = createPrePaymentInterest(Double.parseDouble(balance)
							//	, loanRate
							//	, Integer.parseInt(oneRePaymentDataInfo.getLoanDay()), currentDate, toDay);
		    	        	}
		    	        	
		    	        	String isOverDue = repaymentService.selectIsOverDueState(loanId, payCount);			// coh-연체 정보가 있는지 확인
		    	        	
		    	        	//	long overdue = (long)(Double.parseDouble(rDelqAmount) + Double.parseDouble(overDuePayment));
		    	        	double overdue = overDuePayment;

		    	        	long additionalRate = (long)loanRates[overDueCount];

//		    	        	System.out.println("loanId, payCount, String.valueOf(overdue), String.valueOf(overDueState))" + "	" + loanId + "	" + payCount
//		    	        			 + "	" + String.valueOf((long) Math.floor(overdue)) + "	" + String.valueOf(overDueState));
																			// cpas_overdue_detail_history-일별정보입력
//		    	        	repaymentService.insertOrverDueDetailHistory(loanId, payCount, String.valueOf(additionalRate), String.valueOf((long) Math.floor(overdue)), String.valueOf(overDueState));

//		    	        	if(isOverDue == null)
//    	    	        		repaymentService.insertOrverDueHistory(loanId, payCount);						// coh-연체 기록이 안되있으면 입력  
		    	        	
		    	        	////상환테이블 업데이트////																// crs-r_delq_amount와 r_delq_state정보 업데이트
//		    	        	if(repaymentService.updateRepaymentScheduleOverDue(loanId, payCount, String.valueOf((long) Math.floor(overdue)), String.valueOf(overDueState))) {
	    	    	        	JSONObject jsonItem = new JSONObject();
	    	    	        	jsonItem.put("payCount", payCount);
	    	    	        	jsonItem.put("overDueState", overDueState);
	    	    	        	jsonItem.put("overDuePayment", String.valueOf((long) Math.floor(overdue)));
	    	    	        	jsonItem.put("overDuePayIncrement", String.valueOf(overDuePayment));
	    	    	        	jsonResult.put(jsonItem);
//	        	        	} else 
//	        	        		commonUtil.sendBatchLogging("startOverdueUpdate", "loanId : " + loanId + " payCount : " + payCount + " overDuePayment : " + overDuePayment + " overDueState : " + overDueState
//	        	        				, "상환테이블 업데이트 에러");
		    	        }
		    	        
		    	        List<OnePaymentInvestSchedule> onePaymentInvestSchedules = scheService.selectPaymentInvestSchedule(loanId);	// 해당 채권으로 투자한 투자자 정보 선택
		    	        
		    	        for(int i = 0; i < onePaymentInvestSchedules.size(); i++) {
		    	        	String mid = onePaymentInvestSchedules.get(i).getMid();
		    	        	double overDuePartRate = Double.parseDouble(onePaymentInvestSchedules.get(i).getPay()) / Double.parseDouble(onePaymentInvestSchedules.get(i).getLoanPay());
		    	        	
		    	        																						//세금정보 선택
		    	        	OneRateInfo oneRateInfo = scheService.selectRateInfo(onePaymentInvestSchedules.get(i).getLevel(), onePaymentInvestSchedules.get(i).getService(), "1");
		    				double feeRate = Double.parseDouble(oneRateInfo.getFee());
		    				double taxRate = Double.parseDouble(oneRateInfo.getTax());
		    				double taxRateLocal = Double.parseDouble(oneRateInfo.getTaxLocal());
		    	        	
		    	        	for(int j = 0; j < jsonResult.length(); j++) {
		    	        		JSONObject jsonItem = jsonResult.getJSONObject(j);
		    	        		String payCount = jsonItem.getString("payCount");
		    	        		String overDuePayment = jsonItem.getString("overDuePayment");
		    	        		String overDueStates = jsonItem.getString("overDueState");
		    	        		String overDuePayIncrement = jsonItem.getString("overDuePayIncrement");	// 안쓰면 삭제 예정;
		    	        		long overDue = Math.round(Double.parseDouble(overDuePayment) * overDuePartRate);
		    	        		
		    	        		PaymentScheduleItem paymentScheduleItem = repaymentService.selectPaymentScheduleItem(mid, loanId, payCount);	// cps-투자자 원금,이자,연체이자,세금, 수수료 등의 정보 선택
		    	        		long inAmount = paymentScheduleItem.getInAmount();
		    	        		int interest = paymentScheduleItem.getInterest();
		    	        		int tax = paymentScheduleItem.getTax();
		    	        		int taxLocal = paymentScheduleItem.getTaxLocal();
		    	        		int fee = paymentScheduleItem.getFee();
		    	        		
		    	        		double taxPay = (interest + overDue) * taxRate;									// 세금: 정상이자+연체이자
		    	        		double taxPayLocal = (interest + overDue) * taxRateLocal;
		    	        		
		    	        		if(Integer.parseInt(onePaymentInvestSchedules.get(i).getLevel()) == 4) {		// 재단이면 1000원 이하 세금 절삭
		    	        			// 200429 세금이 1000원일 경우도 절삭으로 적용 
		    	    				if(taxPay <= 1000) {
		    	    					taxPay = 0;
		    	    					taxPayLocal = 0;
			    	    			}
		    	    			}
		    	        		
								// 잔여투자금 구하는 함수
								long investBalance = scheService.selectNewFeeInfo(loanId, mid, payCount);
								
								// 200619 이후 연체되어도 수수료 변경되지 않음 
			 	    			//fee = (int) (investBalance * 0.002);											// * 0.024

			 	    			
		    	    			long tax_real = ((long)Math.floor(taxPay) / 10) * 10;							// 9원단위 절삭
		    	    			long tax_local_real = ((long)Math.floor(taxPayLocal) / 10) * 10;
		    	    			
		    	    			double result = inAmount + ((interest + overDue) - (tax_real + tax_local_real));
		    	    			// double feePay = result * feeRate;
		    	        		
		    	    			long tax_diff = tax_real - tax;
		    	    			long taxLocal_diff = (long) (tax_local_real - taxLocal);
	    	    				// long fee_diff = (long) (feePay - fee);
		    	    																							// coih-연체정보입력
//		    	    			repaymentService.insertOrverDueInvestHistory(loanId, mid, payCount, String.valueOf(overDue)
//		    	    					, String.valueOf(tax_diff), String.valueOf(taxLocal_diff), "0", //String.valueOf(fee_diff), // 200424이후 플랫폼 이용수수료는 매일쌓이는 DB와 상관이 없어서 "0"처리  
//		    	    					overDueStates);
		    	    			System.out.println("loanId	" + loanId + "	mid	" + mid + "	payCount	" + payCount + "	overDue	" + String.valueOf(overDue)
		    	    			+ " tax_real	" + String.valueOf(tax_real) + "	tax_local_real	" + String.valueOf(tax_local_real)
		    	    			+ "	tax_diff	" + String.valueOf(tax_diff) + "	taxLocal_diff	" + String.valueOf(taxLocal_diff) + "	0	" + 0);
		    	    																							// cps-연체정보 업데이트
//		    	        		if(!repaymentService.updatePaymentScheduleOverDue(mid, loanId, payCount, String.valueOf(overDue), String.valueOf(tax_real)
//		    	        				, String.valueOf(tax_local_real), String.valueOf((long)Math.floor(fee))))					// 200804 DB에 연체정보 업데이트시 세금과 지방세 변경되지 않음
//		    	        			commonUtil.sendBatchLogging("startOverdueUpdate", "loanId : " + loanId + " payCount : " + payCount + " overDue : " + overDue, "지급테이블 업데이트 에러");
		    	        	}
		    	        }
		    		}
    			}

	        	
	        	
	        	
	        	
	        	   ResponseResult response = new ResponseResult();        
	               response.setState(200);
	            return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
	    	} catch (Throwable t) {
	    		commonUtil.sendBatchLogging("addRepaymentSchedule", "exception error!!", t.getMessage());
//	    		Slack.api.call(new SlackMessage("#young-server","jhlee","상환 스케줄러 생성배치 실패"));
	    		throw new RuntimeException(t.getMessage());
	    	}
	        
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
    
    
    private String WorkingDayCalculate(String days) {
    	try {
    		String resultDate = null;
    		
    		List<OneHolidayCalendar> oneHolidayCalendar = scheService.selectHolidayCalendar();
    		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");

    		for(int i = 0; i < oneHolidayCalendar.size(); i++) {
    			String hDate = oneHolidayCalendar.get(i).getHdate();
    			String hlunar = oneHolidayCalendar.get(i).getHlunar();
    			Calendar calendar = Calendar.getInstance();
    			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    			
    			if(hDate.length() <= 5) {															//매년 있는 휴일
    				int Year = Integer.parseInt(days.substring(0, 4));
    				String holyDay = dt.format(dt.parse(Year + "-" + hDate));
    				
    				//음력으로 공휴일일경우 계산
    				if(hlunar.equals("Y")) {
    					if(hDate.equals("01-01")) {													//설연휴면 설연휴 다음날 날짜로 변환
    						String holyDay2 = dt.format(dt.parse((Year - 1) + "-12-30"));			//30일? 31일?
    						String holyDay3 = dt.format(dt.parse(Year + "-01-02"));
    						
    						if(convertLunarToSolar(holyDay).equals(days) || convertLunarToSolar(holyDay2).equals(days) || convertLunarToSolar(holyDay3).equals(days)) {
    							calendar.setTimeInMillis(increaseDate(convertLunarToSolar(holyDay3), 1));
    							resultDate = sdf.format(calendar.getTime());
    						}
    						
    					} else if(hDate.equals("08-15")) {
    						String holyDay2 = dt.format(dt.parse(Year + "-08-14"));					//추석연휴면 추석연휴 다음날 날짜로 변환
    						String holyDay3 = dt.format(dt.parse(Year + "-08-16"));
    						
    						if(convertLunarToSolar(holyDay).equals(days) || convertLunarToSolar(holyDay2).equals(days) || convertLunarToSolar(holyDay3).equals(days)) {
    							calendar.setTimeInMillis(increaseDate(convertLunarToSolar(holyDay3), 1));
    							resultDate = sdf.format(calendar.getTime());
    						}
    						
    					} else {																	//그 외에(추가로 DB에 넣은 음력휴일) 음력연휴면 하루 다음날짜로 변환
    						if(convertLunarToSolar(holyDay).equals(days)) {
    							calendar.setTimeInMillis(increaseDate(convertLunarToSolar(holyDay), 1));
    							resultDate = sdf.format(calendar.getTime());
    						}
    					}
    					
    					if(resultDate != null) {
    						if(getDateDay(resultDate) == 1) {										//일요일이면 다음날짜로 변경
        						calendar.setTimeInMillis(increaseDate(resultDate, 1));
    							resultDate = sdf.format(calendar.getTime());
    							break;
        					} else if (getDateDay(resultDate) == 7) {
        						calendar.setTimeInMillis(increaseDate(resultDate, 2));				//토요일이면 월요일로 변경
    							resultDate = sdf.format(calendar.getTime());
    							break;
        					}
    					} else {																	//음력이면서 위에 사항에 모두 만족하지 않으면
    						if(getDateDay(days) == 1) {												//일요일이면 다음날짜로 변경
        						calendar.setTimeInMillis(increaseDate(days, 1));
    							resultDate = sdf.format(calendar.getTime());
    							break;
        					} else if (getDateDay(days) == 7) {										//토요일이면 월요일로 변경
        						calendar.setTimeInMillis(increaseDate(days, 2));
    							resultDate = sdf.format(calendar.getTime());
    							break;
        					}
    					}
    				} 
    				//양력으로 공휴일일경우 계산
    				else {
    					if(holyDay.substring(5).equals(days.substring(5))) {	//년도를 제외한 월일(mm-dd)가 휴일이면, 휴일 다음날짜로 변경
        					calendar.setTimeInMillis(increaseDate(days, 1));
    						resultDate = sdf.format(calendar.getTime());
        				}
        				
    					if(resultDate != null) {
	        				if(getDateDay(resultDate) == 1) {					//일요일이면 다음날짜로 변경
	    						calendar.setTimeInMillis(increaseDate(resultDate, 1));
	    						resultDate = sdf.format(calendar.getTime());
	    						break;
	    					} else if (getDateDay(resultDate) == 7) {			//토요일이면 월요일로 변경
	    						calendar.setTimeInMillis(increaseDate(resultDate, 2));
	    						resultDate = sdf.format(calendar.getTime());
	    						break;
	    					}
    					} else {												//휴일이 아닌데
    						if(getDateDay(days) == 1) {							//일요일이면 다음날짜로 변경
        						calendar.setTimeInMillis(increaseDate(days, 1));
    							resultDate = sdf.format(calendar.getTime());
    							break;
        					} else if (getDateDay(days) == 7) {					//토요일이면 월요일로 변경
        						calendar.setTimeInMillis(increaseDate(days, 2));
    							resultDate = sdf.format(calendar.getTime());
    							break;
        					}
    					}
    				}
    			} else {														//특정년도에만 있는 휴일
    				if(hDate.equals(days)) {
	    				calendar.setTimeInMillis(increaseDate(hDate, 1));		//휴일이면, 휴일 다음날짜로 변경
						resultDate = sdf.format(calendar.getTime());
						
						if(getDateDay(resultDate) == 1) {						//일요일이면 다음날짜로 변경
							calendar.setTimeInMillis(increaseDate(resultDate, 1));
							resultDate = sdf.format(calendar.getTime());
						} else if (getDateDay(resultDate) == 7) {				//토요일이면 월요일로 변경
							calendar.setTimeInMillis(increaseDate(resultDate, 2));
							resultDate = sdf.format(calendar.getTime());
						}
    				}
    				
					if(resultDate != null)										//값이 들어가 있으면 빠져나옴
						break;
    			}
    		}
    		
    		return resultDate;
    		
    	} catch (Throwable t) {
    		t.printStackTrace();
    	}
    	
    	return null;
    }

    // 다음날짜 생성 메서드
    private String increaseOneDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");		
        
    	Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, Integer.parseInt(date.substring(0, 4)));
		cal.set(Calendar.MONTH, Integer.parseInt(date.substring(5, 7)) - 1);
		cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.substring(8)) + 1);
		return sdf.format(cal.getTime());    	
    }
    
    private long increaseDate(String date, int increase) {
    	Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, Integer.parseInt(date.substring(0, 4)));
		cal.set(Calendar.MONTH, Integer.parseInt(date.substring(5, 7)) - 1);
		cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.substring(8)) + increase);
		return cal.getTimeInMillis();
    }
    
    private static String convertLunarToSolar(String date) {
        ChineseCalendar cc = new ChineseCalendar();
        Calendar cal = Calendar.getInstance();
         
        cc.set(ChineseCalendar.EXTENDED_YEAR, Integer.parseInt(date.substring(0, 4)) + 2637);
        cc.set(ChineseCalendar.MONTH, Integer.parseInt(date.substring(5, 7)) - 1);
        cc.set(ChineseCalendar.DAY_OF_MONTH, Integer.parseInt(date.substring(8)));
         
        cal.setTimeInMillis(cc.getTimeInMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }
    
    //일(1)~토(7) 사이값 반환
    public int getDateDay(String date) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd") ;
        Date nDate = dateFormat.parse(date) ;
         
        Calendar cal = Calendar.getInstance() ;
        cal.setTime(nDate);
         
        int dayNum = cal.get(Calendar.DAY_OF_WEEK) ;
        
        return dayNum;
    }
    
    
    
    
    //원리금균등상환공식-매월 납입금액 (rate:이율, nper:납입횟수, pmt:정기납입액, fv:미래가치, pv:현재가치, type:납입시점-초는1, 말은0)
    public double getPmt(double rate, double nper, double pv, double fv, int type) 	{
    	return rate == 0 ? (- pv - fv) / nper : (- fv * rate - pv * rate * Math.pow(1 + rate, nper)) / ((1 + rate * type) * (Math.pow(1 + rate, nper) - 1));
	}
    
    public boolean isLeapYear(int year) {
    	return year % 4 == 0 && year % 100 != 0 || year % 400 == 0;
    }
    
	private double investBalance(JSONArray jsonRepaySchedules, int payCount) {

		double investBalance = 0;
		for (int i = payCount - 1; i < jsonRepaySchedules.length(); i++) {
			JSONObject repaySchedules = jsonRepaySchedules.getJSONObject(i);

			investBalance += repaySchedules.getDouble("paidAmount");
		}

		return investBalance;
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
	
    private JSONArray createPrincipalSchedule(double loanAmt, double interest, int loanPeriod, String repayDay, String startDate, String execDate, long diffDates, int repayMonth) {
    	try {
	    	double loanAmtOrg = loanAmt;

	    	long repayAmount = (((long)getPmt(interest/12, loanPeriod, loanAmt * -1, 0, 0)) / 10) * 10;	    	//원리금(원금+이자)공식
	    	
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    	Calendar calendar = Calendar.getInstance();
	    	
			int Year = Integer.parseInt(startDate.split("-")[0]);
			int Month = Integer.parseInt(startDate.split("-")[1]) - 1;							//Month -1로 수정(for문에서 1회차에 누적값이 들어가야 해서 한번 더 실행시키기 위해서라고 판단) 
	    	
	    	Calendar cal = Calendar.getInstance();

	    	// diffDates 처음엔 시작일과 상환일 차이나는 일수,  그 이후에는 다음상환회차까지의 일수누적
			long diffSum = diffDates;															// 1회차 상환전까지의 일수(이후 다음회차 날짜 계속 누적)
			long loanCountPaymentSum = 0;
			long loanPayCutSum = 0;
			long loanPaySumTotal = 0;
			boolean leapYear = false;
			
			JSONArray jsonResultArry = new JSONArray();
			
	    	for(int i = 0; i < loanPeriod; i++) {
	    		cal = Calendar.getInstance();
	    		
	    		if(i != 0) {																	//Month값 +1씩 증가
	    			Month += 1;
	    		}
	    		
	        	cal.set(Calendar.YEAR, Year);
	    		cal.set(Calendar.MONTH, (Month + repayMonth));
	    		cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(repayDay));
	    		calendar.setTimeInMillis(cal.getTimeInMillis());
	    		
	    		String endDate = sdf.format(calendar.getTime());
	    		String[] leapDate = endDate.split("-");
	    		if(isLeapYear(Integer.parseInt(leapDate[0])) && (leapDate[1].equals("2") || leapDate[1].equals("02")))	//윤년이면 표시
	    			leapYear = true;
	    		
	    		long diffDate = getDiffOfDate(startDate, endDate);
	    		
	    		if(i == (loanPeriod - 1)) {														// 마지막 회차일 경우
//	    			int yearsCount = loanPeriod / 12;
	    			
//	    			Year = Integer.parseInt(execDate.split("-")[0]) + yearsCount;
	    			Month = Integer.parseInt(execDate.split("-")[1]) - 1;
	    			//마지막 회차 대출 실행일에 맞춤//
	    			int date = Integer.parseInt(execDate.split("-")[execDate.split("-").length - 1]);
	    			
	    			cal.set(Calendar.YEAR, Year);
		    		cal.set(Calendar.MONTH, Month);
		    		cal.add(Calendar.MONTH, loanPeriod);		// 상환개월만큼 추가
		    		cal.set(Calendar.DAY_OF_MONTH, date);
		    		calendar.setTimeInMillis(cal.getTimeInMillis());
		    		endDate = sdf.format(calendar.getTime());
		    		diffDate = getDiffOfDate(startDate, endDate);
	    			
		    		leapDate = endDate.split("-");
		    		if(isLeapYear(Integer.parseInt(leapDate[0])) && (leapDate[1].equals("2") || leapDate[1].equals("02")))
		    			leapYear = true;
		    		
	    			diffSum += diffDate;
	    		} else {
	    			diffSum += diffDate;
	    		}
	    		
	    		//Math.floor((double)diffDate * ((loanAmt * interest) / 365)) <- 이렇게도 표현 가능//
	    		//Math.floor(대출잔액 * 연이율 * ((double)경과일수))//
	    		double loanInterestD = loanAmt * interest * ((double)diffDate/365);		// 대출 원금에 이자 구하는 공식(2회차부터는 
	    		
	    		if(i == 0)
	    			loanInterestD += loanAmt * interest * ((double)diffDates/365);	// 대출 원금에 이자 구하는 공식(1회차는 1회차 상환전까지의 일수)
	    		
	    		long loanInterest = (long) Math.floor(loanInterestD);
	    		// PS. 최초상환일이 15일 이전일경우 30일이 넘어가기 때문에 1회차는 30일치+@로 계산됨, 두번다 버림처리해서 에러건 발생[~200214]
	    		// 합산후 버림처리하는것으로 임시 수정하였고, 추후 전체구조 수정 예정
	    		
	    		long loanCountPayment = (long) Math.floor(repayAmount - loanInterest);					// 회차별 원금 = 원리금-이자
	    		loanCountPaymentSum += loanCountPayment;												// 회차별 원금의 합
	    		
	    		if(i == (loanPeriod - 1)) {																// 마지막 회차에
	    			long repayAmountRest = (long)loanAmtOrg - loanCountPaymentSum;						// 남은원금 = 대출총액-회차별원금의합
	    			loanCountPayment += repayAmountRest;												// 마지막 회차 원금
	    		}
	    		
	    		long loanPaySum = loanCountPayment + loanInterest;
	    		long loanPayCut = ((loanCountPayment + loanInterest) / 10) * 10;
	    		
	    		loanPayCutSum += loanPayCut;
	    		loanPaySumTotal += loanPaySum;
	    		
	    		if(i == (loanPeriod - 1)) {
	    			long diffPay = loanPaySumTotal - loanPayCutSum;
	    			loanPayCut += diffPay;
	    		}
	    		
	    		loanAmt -= loanCountPayment;															// 남은 잔액
	    		

	    		if(i == 0) 
	    			 diffDate = getDiffOfDate(execDate, endDate);
	    		
	    		startDate = endDate;

	    		JSONObject jsonResult = new JSONObject();
				jsonResult.put("payCount", String.valueOf((i + 1)));
				//jsonResult.put("repayAmount", String.valueOf((long)loanPaySum));
				jsonResult.put("repayAmount", String.valueOf((long)loanPayCut));		//원리금
				jsonResult.put("paidAmount", String.valueOf((long)loanCountPayment));	//원금(원리금-이자)
				jsonResult.put("loanInterest", String.valueOf((long)loanInterest));		//이자(원리금-원금)
				jsonResult.put("balance", String.valueOf((long)loanAmt));				//다음달 지불액
				jsonResult.put("resultDate", endDate);									//다음달 상환일
				jsonResult.put("dayEOM", diffDate);										//이전달 월의수
				//{"balance":"962540","repayAmount":"44090","payCount":"1","resultDate":"2020-01-25","loanInterest":"6630","paidAmount":"37460"}
				jsonResultArry.put(jsonResult);
	    	}
	    	
	    	return jsonResultArry;
    	} catch (JSONException e) {
			e.printStackTrace();
		}
    	
    	return null;
    }
    
    
    																		//대출액, 연이율, 대출기간, 대출상환일(5,15,25), 상환시작일, 대출실행일, 상환-대출, 추가+1달여부
    private JSONArray createMaturitySchedule(double loanAmt, double interest, int loanPeriod, String repayDay, String startDate, String execDate, long diffDates, int repayMonth) {
    	try {
	    	double loanAmtOrg = loanAmt;
	    	
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    	Calendar calendar = Calendar.getInstance();
	    	
			int Year = Integer.parseInt(startDate.split("-")[0]);
			int Month = Integer.parseInt(startDate.split("-")[1]) - 1;
	    	
	    	Calendar cal = Calendar.getInstance();
			
			long diffSum = diffDates;
			boolean leapYear = false;
			
			JSONArray jsonResultArry = new JSONArray();
			
	    	for(int i = 0; i < loanPeriod; i++) {
	    		cal = Calendar.getInstance();
	    		
	    		if(i != 0) {
	    			Month += 1;
	    		}
	    		
	        	cal.set(Calendar.YEAR, Year);
	    		cal.set(Calendar.MONTH, (Month + repayMonth));						// 첫상환일 15일 미만이면 다음달로(repayMonth+1)
	    		cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(repayDay));
	    		calendar.setTimeInMillis(cal.getTimeInMillis());
	    		
	    		String endDate = sdf.format(calendar.getTime());
	    		String[] leapDate = endDate.split("-");
	    		if(isLeapYear(Integer.parseInt(leapDate[0])) && (leapDate[1].equals("2") || leapDate[1].equals("02")))	// 윤년이면 
	    			leapYear = true;
	    		
	    		long diffDate = getDiffOfDate(startDate, endDate);
	    		
	    		if(i == (loanPeriod - 1)) {
	    			
	    			Month = Integer.parseInt(execDate.split("-")[1]) - 1;
	    			//마지막 회차 대출 실행일에 맞춤//
	    			int date = Integer.parseInt(execDate.split("-")[execDate.split("-").length - 1]);
	    			
	    			cal.set(Calendar.YEAR, Year);
		    		cal.set(Calendar.MONTH, Month);
		    		cal.add(Calendar.MONTH, loanPeriod);		// 상환개월만큼 추가
		    		cal.set(Calendar.DAY_OF_MONTH, date);
		    		calendar.setTimeInMillis(cal.getTimeInMillis());
		    		endDate = sdf.format(calendar.getTime());
		    		diffDate = getDiffOfDate(startDate, endDate);
	    			
		    		leapDate = endDate.split("-");
		    		if(isLeapYear(Integer.parseInt(leapDate[0])) && (leapDate[1].equals("2") || leapDate[1].equals("02")))
		    			leapYear = true;
		    		
	    			diffSum += diffDate;
	    		} else {
	    			diffSum += diffDate;
	    		}
	    		
	    		//Math.floor(대출잔액 * 연이율 * ((double)경과일수))//
	    		double loanInterestD = loanAmt * interest * ((double)diffDate/365);		// 대출 원금에 이자 구하는 공식(2회차부터는 
	    		
	    		if(i == 0)
	    			loanInterestD += loanAmt * interest * ((double)diffDates/365);	// 대출 원금에 이자 구하는 공식(1회차는 1회차 상환전까지의 일수)
	    		
	    		long loanInterest = (long) Math.floor(loanInterestD);
	    		// PS. 최초상환일이 15일 이전일경우 30일이 넘어가기 때문에 1회차는 30일치+@로 계산됨, 두번다 버림처리해서 에러건 발생[~200214]
	    		// 합산후 버림처리하는것으로 임시 수정하였고, 추후 전체구조 수정 예정
	    		
	    		long repayAmount = 0;
	    		long paidAmount = 0;
	    															
	    		if(i == (loanPeriod - 1)) {
	    			paidAmount = (long)loanAmt;
	    			repayAmount = paidAmount + loanInterest;	// 만기일시상환은 마지막회차에만 원금이 있으므로, 원리금은 대출금+이자
	    		} else												
	    			repayAmount = paidAmount + loanInterest;	// 원리금은 원금(0)+이자
	    		
//
	    		if(i == 0) 
	    			 diffDate = getDiffOfDate(execDate, endDate);
	    		
	    		startDate = endDate;

	    		JSONObject jsonResult = new JSONObject();
				jsonResult.put("payCount", String.valueOf((i + 1)));
				jsonResult.put("repayAmount", String.valueOf(repayAmount));
				jsonResult.put("paidAmount", String.valueOf(paidAmount));
				jsonResult.put("loanInterest", String.valueOf((long)loanInterest));
				jsonResult.put("balance", String.valueOf((long)loanAmt));
				jsonResult.put("resultDate", endDate);
//
				jsonResult.put("dayEOM", diffDate);
				jsonResultArry.put(jsonResult);
	    	}
	    	
	    	return jsonResultArry;
    	} catch (JSONException e) {
			e.printStackTrace();
		}
    	
    	return null;
    }

}