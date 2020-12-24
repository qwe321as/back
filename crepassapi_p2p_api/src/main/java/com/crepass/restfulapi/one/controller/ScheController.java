package com.crepass.restfulapi.one.controller;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.Slack;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.inside.domain.InsideDeposit;
import com.crepass.restfulapi.inside.domain.InsideDepositInfo;
import com.crepass.restfulapi.inside.domain.InsideDepositInfo2;
import com.crepass.restfulapi.inside.domain.OneInsideDepositCancel;
import com.crepass.restfulapi.inside.domain.OneRepaymentDepositInfo;
import com.crepass.restfulapi.inside.domain.OneRepaymentInfo;
import com.crepass.restfulapi.inside.service.DepositService;
import com.crepass.restfulapi.one.domain.MariMember;
import com.crepass.restfulapi.one.domain.OneEmoneyInvestPay;
import com.crepass.restfulapi.one.domain.OneEventDiscount;
import com.crepass.restfulapi.one.domain.OneHolidayCalendar;
import com.crepass.restfulapi.one.domain.OneInvest;
import com.crepass.restfulapi.one.domain.OneInvestAccount;
import com.crepass.restfulapi.one.domain.OneInvestAutoDivision;
import com.crepass.restfulapi.one.domain.OneInvestDetail;
import com.crepass.restfulapi.one.domain.OneInvestLimitPay;
import com.crepass.restfulapi.one.domain.OneInvestLoanDefault;
import com.crepass.restfulapi.one.domain.OneInvestTitle;
import com.crepass.restfulapi.one.domain.OneLenddoWebhookInfo;
import com.crepass.restfulapi.one.domain.OneMemberCustAddInfo;
import com.crepass.restfulapi.one.domain.OneMemberCustAddInfo2;
import com.crepass.restfulapi.one.domain.OneMemberCustId;
import com.crepass.restfulapi.one.domain.OneOverdueRepaymentItem;
import com.crepass.restfulapi.one.domain.OnePaymentInvestSchedule;
import com.crepass.restfulapi.one.domain.OnePaymentNewInfo;
import com.crepass.restfulapi.one.domain.OnePaymentNewSchedule;
import com.crepass.restfulapi.one.domain.OnePaymentSchedule;
import com.crepass.restfulapi.one.domain.OnePrePaymentSchedule;
import com.crepass.restfulapi.one.domain.OneRateInfo;
import com.crepass.restfulapi.one.domain.OneRepayScheduleAdd;
import com.crepass.restfulapi.one.domain.OneRepayScheduleInfo;
import com.crepass.restfulapi.one.domain.OneRepaymentCheckCount;
import com.crepass.restfulapi.one.domain.OneRepaymentDataInfo;
import com.crepass.restfulapi.one.domain.OneRepaymentManage;
import com.crepass.restfulapi.one.domain.OneRepaymentScheduleItem;
import com.crepass.restfulapi.one.domain.OneSendCheckingEmail;
import com.crepass.restfulapi.one.domain.OneStartInvestUserInfo;
import com.crepass.restfulapi.one.domain.OneStartPaymentInfo;
import com.crepass.restfulapi.one.domain.OneVirtualAccntWithdraw;
import com.crepass.restfulapi.one.domain.OneVirtualRealAccnt;
import com.crepass.restfulapi.one.domain.OneWithdraw;
import com.crepass.restfulapi.one.service.EmoneyService;
import com.crepass.restfulapi.one.service.EventService;
import com.crepass.restfulapi.one.service.InvestService;
import com.crepass.restfulapi.one.service.LenddoService;
import com.crepass.restfulapi.one.service.LoanService;
import com.crepass.restfulapi.one.service.OneMemberService;
import com.crepass.restfulapi.one.service.RepaymentService;
import com.crepass.restfulapi.one.service.ScheService;
import com.crepass.restfulapi.one.service.VirtualAccntService;
import com.crepass.restfulapi.v2.domain.InvestBundleItem2;
import com.crepass.restfulapi.v2.domain.PaymentScheduleItem;
import com.ibm.icu.util.ChineseCalendar;

import net.gpedro.integrations.slack.SlackMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Component
public class ScheController {

	@Autowired
    private OneMemberService oneMemberService;
	
	@Autowired
    private ScheService scheService;
	
    @Autowired
    private DepositService depositService;
    
    @Autowired
    private InvestService investService;
    
    @Autowired
    private VirtualAccntService virtualAccntService;
    
    @Autowired
    private EmoneyService emoneyService;
    
    @Autowired
    private LoanService loanService;
    
    @Autowired
    private RepaymentService repaymentService;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private LenddoService lenddoService;
    
//    @Value("${shinhan.webhook.url}")
//    private String shinhanUrl;
    
    @Autowired
    private CommonUtil commonUtil;
    
    @Value("${crepas.inside.url}")
    private String insideUrl;
    
    // 1. 입금처리 스케줄러(최종수정 V200715)
    // 은행(Inside)에 최근 입금된 정보(IB_FB_P2P_IP)를 가져와서 cpas_deposit_trx, cpas_trx_log에 정보입력, 
    // 정상입력시 cpas_deposit_trx-배치플래그를 성공(batch_flag=S)으로 업데이트(실패시 F)
    @Transactional("oneTransactionManager")
    @Scheduled(cron = "0 0/1 * * * *")    	
    public void startDeposit() {

    	try {
    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) {
	    		List<InsideDeposit> insideDepositById = scheService.selectDepositById();						// 1. cdt-가장 최근 처리된 정보 선택(입금일시-erpTransDt:인사이드뱅크 1개 선택, loanId, 가상계좌 포함) 

    			// 구분 10이면 투자자 20이면 대출자 입금
	    		if(insideDepositById == null || insideDepositById.isEmpty()) {
	    			List<InsideDepositInfo> insideDepositInfos = depositService.selectDepositInfo2();			// inside(IB_FB_P2P_IP)에서 혹시 입금일시 비어있으면 다 가져옴
	    			addDepositInfo(insideDepositInfos);
	    		} else {
			        for(int i = 0; i < insideDepositById.size(); i++) {											// 2. inside(IB_FB_P2P_IP)에서 정산되지 않은정보(1번 조건보다 큰 정보들) 다 가져옴											
				        List<InsideDepositInfo> insideDepositInfos = depositService.selectDepositInfo3(insideDepositById.get(i).getErpTransDt());
				        addDepositInfo(insideDepositInfos);														// cdt에 해당정보 입력
			        }
	    		}
	    		
	    		List<InsideDepositInfo2> selectDepositSheduleById = scheService.selectDepositScheduleById();	// cdt에 실행안한 배치 (batch_flag=N) 모두가져옴
	    		
	    		for(int i = 0; i < selectDepositSheduleById.size(); i++) {
	    			String mid = scheService.selectCustUserId(selectDepositSheduleById.get(i).getCustId());		// custId로 m_id가져옴
	    			
	    			if(mid != null && !mid.equals("null") && mid.length() > 0) {				
	    				// 2. 투자자, 대출자 실제 입금 (loanId정보없음, 회차정보 없음)
//	    				boolean insertDepositHistory = scheService.insertDepositHistory(mid,"D", selectDepositSheduleById.get(i).getTrAmt()
//	    						, selectDepositSheduleById.get(i).getTypeFlag());								// ctl-입금정보 입력
	    				// 적용예정
	    				boolean insertDepositHistory = scheService.insertDepositHistory(mid,"D", selectDepositSheduleById.get(i).getTrAmt()
	    						, selectDepositSheduleById.get(i).getTypeFlag(), null, null);								// ctl-입금정보 입력
	    				
	    				if(!insertDepositHistory) {																// insert 실패시 F로 업데이트
	    					scheService.updateDepositSchedule(selectDepositSheduleById.get(i).getId(), "F");
	    					commonUtil.sendBatchLogging("startDeposit", "mid : " + mid + " , getTrAmt : " + selectDepositSheduleById.get(i).getTrAmt()
	    							, "insertDepositHistory fail : " + insertDepositHistory);
	    				}
	    				else {																					// insert 성공시 S로 업데이트
	    					boolean updateDepositSchedule = scheService.updateDepositSchedule(selectDepositSheduleById.get(i).getId(), "S");
	    					
	    					JSONObject jsonSmsData = new JSONObject();
	            	        OneMemberCustAddInfo2 oneMemberCustAddInfo = oneMemberService.selectCustAddInfo2(mid);
	            			jsonSmsData.put("name", oneMemberCustAddInfo.getName());
//	            			jsonSmsData.put("bankCode", oneMemberCustAddInfo.getVirtualAccnt());
	            			
	            			Long trAmt = (long) Double.parseDouble(selectDepositSheduleById.get(i).getTrAmt());
	            			jsonSmsData.put("trAmt", commonUtil.getAmountUnit3(trAmt));
	    					
	            			String msg = commonUtil.getFormSMS(12, jsonSmsData);
	    					commonUtil.setRequestSMSData(oneMemberCustAddInfo.getName(), "I", selectDepositSheduleById.get(i).getCustId(), oneMemberCustAddInfo.getHp(), msg);

	    					if(!updateDepositSchedule)
	    						commonUtil.sendBatchLogging("startDeposit", "getId : " + selectDepositSheduleById.get(i).getId(), "updateDepositSchedule fail : " + updateDepositSchedule);
	    				}
	    			}
	    		}
    		}
    	} catch (Throwable t) {
    		commonUtil.sendBatchLogging("startDeposit", "exception error!!", t.getMessage());
    		Slack.api.call(new SlackMessage("#young-server","jhlee","2. 투자자, 대출자 실제 입금 실패!!"));
    		throw new RuntimeException(t.getMessage());
    	}
    }
    
    // 2. 입금처리취소 스케줄러(최종수정 V200715)
	// 은행쪽에서 입금되었는데 처리오류시, 우리쪽에서 DB는 쌓여져 있지만 실제 입금이 안된상황에
	// cpas_deposit_cancel_history에 취소된 상황들 정보를 쌓아두고, 입금 취소되었을때 IB_FB_P2P_IP_CANCEL에 정보 쌓고, 우리쪽은 출금처리해서 차감하는 프로세서
	// cpas_deposit_cancel_history에 정보를 입력, cpas_deposit_trx에 배치성공(S)정보 입력, cpas_trx_log에 입금정보 입력
    @Transactional("oneTransactionManager")
    @Scheduled(cron = "0 0/1 * * * *")						 
    public void startDepositCancel() {
    	try {
    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) {
	    		String recentDate = scheService.selectDeposiRecentCancelDate();		//cpas_deposit_cancel_history-가장최근데이터 1개 선택
	    																			//IB_FB_P2P_IP_CANCEL-선택된 1개 데이터 이후의 데이터를 선택 
	    		List<OneInsideDepositCancel> oneInsideDepositCancels = depositService.selectDepositCancel(recentDate);
	    		
	    		for(int i = 0; i < oneInsideDepositCancels.size(); i++) {
	    			OneInsideDepositCancel oneInsideDepositCancel = oneInsideDepositCancels.get(i);
	    			String custId = oneInsideDepositCancel.getCustId();
	    			String acctNb = oneInsideDepositCancel.getAcctNb();
	    			String trxAmt = String.valueOf((long)Double.parseDouble(oneInsideDepositCancel.getTrAmt()) * - 1);	// 취소할 금액만큼을 -처리
	    			String typeFlag = oneInsideDepositCancel.getTypeFlag();
	    			String trOrgDate = oneInsideDepositCancel.getTrOrgDate();
	    			String trOrgSeq = oneInsideDepositCancel.getTrOrgSeq();
	    			String trNb = oneInsideDepositCancel.getTrNb();
	    			
	    			InsideDepositInfo insideDepositInfo = new InsideDepositInfo();
	    			insideDepositInfo.setCustId(custId);
	    			insideDepositInfo.setAccntNb(acctNb);
	    			insideDepositInfo.setTrAmt(trxAmt);
	    			insideDepositInfo.setTrAmtGbn(typeFlag);
	    			
	    			String isCheckDepositCancelData = scheService.selectCheckDepositCancelData(trOrgDate, trOrgSeq, trNb);
	    			
	    			if(isCheckDepositCancelData == null) {										//해당 입금거래정보(trNb)가 있는지 확인후
		    			if(scheService.insertDepositCancelHistory(oneInsideDepositCancel)) {	//없으면 cpas_deposit_cancel_history-정보입력
		    				scheService.insertDeposit2(insideDepositInfo);						//cdt-배치성공(S)으로 정보입력
		    				String mid = scheService.selectCustUserId(custId);
		    				commonUtil.sendBatchLogging("startDepositCancel", "custId : " + custId + " mid : " + mid + " trxAmt : " + trxAmt, "success");
		    				if(mid != null)
		    					// 3. 입금에 대한 취소처리(loanId정보없음, 회차정보 없음)
//		    					scheService.insertDepositHistory(mid, "D", trxAmt, typeFlag);	//ctl-입금정보 입력(기존의 데이터 삭제하지 않고 -된 내용을 입력해서 0으로 맞춰줌)
		    				// 적용예정
		    				scheService.insertDepositHistory(mid, "D", trxAmt, typeFlag, null, null);	//ctl-입금정보 입력(기존의 데이터 삭제하지 않고 -된 내용을 입력해서 0으로 맞춰줌)
		    			}
	    			} else
	    				commonUtil.sendBatchLogging("startDepositCancel", "custId : " + custId + " trxAmt : " + trxAmt, "fail");
	    		}
    		}
//    		Slack.api.call(new SlackMessage("#young-server","jhlee","3. 입금에 대한 취소처리"));
		} catch (Exception e) {
			commonUtil.sendBatchLogging("startDepositCancel", "exception error!!", e.getMessage());
			Slack.api.call(new SlackMessage("#young-server","jhlee","3. 입금에 대한 취소처리 실패!!"));
    		throw new RuntimeException(e.getMessage());
		}
    }

    // 3. 본인인증결과 초기화 스케줄러(최종수정 V200715)
    @Transactional("oneTransactionManager")
    @Scheduled(cron = "0 0 0 * * *")
//    @Scheduled(cron = "0 48 3 * * *")
    public void startDeleteCertifyWebDump() {											
    	try {
    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) {
				if(!scheService.deleteCertifyWebDumpAll()) {
					commonUtil.sendBatchLogging("startDeleteCertifyWebDump", "EMPTY", "deleteCertifyWebDumpAll : " + false);
				}
    		}
		} catch (Exception e) {
			commonUtil.sendBatchLogging("startDeleteCertifyWebDump", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
		}
    }

    // 4. 대출상태 업데이트 스케줄러(최종수정 V200715)
    // 이번달 문제없이 승인된 대출건 선택해서 대출상태를 '승인'으로 변경하고, 대출자가 돈을 받았을때, 해당 투자자 sms테이블에 정보입력
    @Transactional("oneTransactionManager")
    @Scheduled(cron = "0 0/1 * * * *")
    public void startUpdateLoanState() {
    	try {
    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) {
	    		Calendar cal = Calendar.getInstance();
	    		int i_Year = cal.get(Calendar.YEAR);
	    		int i_Month = cal.get(Calendar.MONTH) + 1;
	    		String s_Month = String.valueOf(i_Month);
	    		
	    		if(i_Month < 10)
	    			s_Month = "0" + i_Month;
	    																				// IB_FB_P2P_DC_IP-이번달 문제없이 승인된건 선택 
	    		List<String> selectLoanPayment = depositService.selectLoanPayment(String.valueOf(i_Year) + s_Month);
	    		
	    		for(int i = 0; i < selectLoanPayment.size(); i++) {
	    			String loanId = selectLoanPayment.get(i);
	    			
	    			if(loanService.updateLoanState2(loanId)) {							// mari_loan-해당 ID 실행상태 변경(loan_step4 S=>Y)
	    				commonUtil.sendBatchLogging("startUpdateLoanState", "selectLoanPayment : " + loanId, "updateLoanState2 : " + true);
	    				
	    				List<OneStartInvestUserInfo> oneStartInvestUserInfos = scheService.selectStartInvestUserInfo(loanId); // 투자자 custId, 투자금, 폰번호 선택
	    				
	    				for(int j = 0; j < oneStartInvestUserInfos.size(); j++) {
	    					Calendar calendar = Calendar.getInstance();
	                		SimpleDateFormat sdf = new SimpleDateFormat("MM월dd일 HH:mm");
	        				String date = sdf.format(calendar.getTime());
	    					
		    				JSONObject jsonSmsData = new JSONObject();
	            			jsonSmsData.put("title", oneStartInvestUserInfos.get(j).getSubject());
	                		jsonSmsData.put("name", oneStartInvestUserInfos.get(j).getName());
	                		jsonSmsData.put("payment", commonUtil.getAmountUnit3(Long.parseLong(oneStartInvestUserInfos.get(j).getInvestPay())));
	                		jsonSmsData.put("date", date);
	                		
		    				String msg = commonUtil.getFormSMS(10, jsonSmsData);		// 투자자에게 투차한 금액에 대한 대출시작 알림 csh에 입력
	                		commonUtil.setRequestSMSData(oneStartInvestUserInfos.get(j).getName(), "I", oneStartInvestUserInfos.get(j).getCustId(), oneStartInvestUserInfos.get(j).getHp(), msg);
	    				}
	    			}
	    		}
    		}
    		
		} catch (Exception e) {
			commonUtil.sendBatchLogging("startUpdateLoanState", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
		}
    }

    // 5. 자동투자 스케줄러(최종수정 V200715)
    // 자동투자에 동의한 사람에 대한 정보 불러와서 투자가능한 펀드에 자동투자
    @Transactional("oneTransactionManager")
     @Scheduled(cron = "0 0 11-15 * * *")
//    @Scheduled(cron = "0 9 20 * * *")
    public void startAutoInvestDivision() {							// 투자한건에 대한 거 제외, 전체한도, 월한도, 투자건의 투자한도, 실제 예치금 확인후 적합하면 자동투자
    																 
    	try {
    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) 
    		{
	    		Calendar cal = Calendar.getInstance();
	    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    		
				String payDate = WorkingDayCalculate(sdf.format(cal.getTime()));
				
				HashMap<String, JSONArray> investCust = new HashMap<>();
				JSONArray jsonSmsDataArray = new JSONArray();
				
				// 휴일이 아니면
				if(payDate == null) {														// 자동투자에 동의한 사람들에 대한 정보선택
		    		List<OneInvestAutoDivision> oneInvestAutoDivisions = scheService.selectInvestAutoDivision();
		    		
		    		for(int i = 0; i < oneInvestAutoDivisions.size(); i++) { 
		    			String i_pay_use = "0";
		    			String aid = oneInvestAutoDivisions.get(i).getAid();
		    			String univName = oneInvestAutoDivisions.get(i).getUnivName();
		    			String mid = oneInvestAutoDivisions.get(i).getMid();
		    			String i_pay = oneInvestAutoDivisions.get(i).getLimitLoan();
		    			String limitMonth = oneInvestAutoDivisions.get(i).getLimitMonth();
		    																				// 현재 투자가 가능한 대출신청건 선택(미혼모대출제외)
		    			List<String> listLoanId = scheService.selectInvestAutoPossible(mid, aid, univName);
		    			
		    			
    			    	// 2-1. 200827~210430  일반투자자가 1천만원이상 투자 불가
		    			String mLevel = investService.selectInvestLevel(mid);									// 투자자 레벨선택
    			    	long investTotalAmount = Long.parseLong(investService.selectInvestTotalAmount(mid));	// mari_invest-투자자 투자총금액    	
    			    	long investLimitation = Long.parseLong(investService.selectInvestLimitation(mid));		// mari_inset-레벨별 투자 한도	
    			    	if (mLevel.equals("1")) {	
    			    		if (investTotalAmount+Long.parseLong(i_pay) > investLimitation) {					// 투자자 투자총금액+신규투자액이 한도를 초과하면 에러
    							
    							System.out.println("투자 한도액을 초과하였습니다." + "투자자계정" + mid + ", 투자자 투자총액 : " + investTotalAmount + ", 신규투자액 : " + i_pay + ", 투자자 한도액 : " + investLimitation);
    							continue;
    			    		}
    			    	}
		    			
		    			jsonSmsDataArray = new JSONArray();
		    			
		    			if(listLoanId != null) {
		    				for(int j = 0; j < listLoanId.size(); j++) {
		    					String loanId = listLoanId.get(j);							// 투자조건 1.
		    					String investRate = scheService.selectInvestRate(loanId);	// 1) 대출건의 투자액의 합이
		    					
		    					// 200827~210430 1.법인 40% 이상 투자할수 없음
		    			    	OneInvestLoanDefault oneInvestLoanDefault = investService.selectInvestLoan(loanId);	// 기본 대출정보
		    			    	
		    			    	if (mLevel.equals("4")) {								// 법인 투자율이
		    				    	long loanPay = Long.parseLong(oneInvestLoanDefault.getLoanPay());
		    				    	if ((loanPay * 0.4) < Long.parseLong(i_pay)) {		// 40%을 넘기면 투자불가
		    				    		
		    				    		System.out.println("법인투자자는 한채권에 40% 이상 투자할수 없습니다." + "대출신청액 : " + loanPay + ", 투자자 투자액: " + i_pay + ", 론아이디 : " + loanId);
		    				    		continue;
		    				    	}
		    			    	}

		    			    	// 200827~210430 2-2. 동일차입자 500만원 이상 투자할수 없음
		    			    	if (mLevel.equals("1")) {	
		    			    		
		    			    		// 그 대출자에 투자한 금액이 있는지 확인!
		    			    		long usedToInvest = Long.parseLong(investService.selectUsedToInvest(oneInvestLoanDefault.getMid(), mid));	// 대출자, 투자자 mid
		    			    		if ( usedToInvest+Long.parseLong(i_pay) > 5000000) {		// 과거 투자액과 신규 투자액이 500만원을 넘으면

		    			    			System.out.println("동일 차입자에 투자할수 있는 투자금액을 초과하였습니다." + "과거 투자액 : " + usedToInvest + ", 신규 투자액 : " + i_pay + ", 론아이디 : " + loanId);
		    			    			continue;
		    			    		}
		    			    	}
		    					
		    					if((int)Float.parseFloat(investRate) < 50) {				// 2) 50% 미만이면 투자
			    					String investTotalPayment = scheService.selectInvestAutoTotalPayment(mid);	// 투자자가 현재 투자한 돈의 합
			    					
			    					if(investTotalPayment == null)
			    						investTotalPayment = "0";
			    																			// 취소되지 않은 대출건중, 실행안된 대출건이 있으면
			    																			// 투자자가 펀딩중인 금액의 합과 투자자의 custId 선택하고 (확인필요)
			    					OneEmoneyInvestPay oneEmoneyInvestPay = emoneyService.selectEmoneyPayInfo(mid);
			    			        String withdrawPay = emoneyService.selectEmoneyWithdrawPay2(mid);	// cwt-투자금액중 출금신청(trx_flag=N) 안된금액 선택해서
			    			        
			    			        // 출금요청 전문보내고, 성공하면 출금한 만큼 예치금에서 공제 
			    			        if(oneEmoneyInvestPay != null) {
			    				        RestTemplate restTemplate = new RestTemplate();
			    				        Map<String, String> vars = new HashMap<String, String>();
			    				        vars.put("CUST_ID", oneEmoneyInvestPay.getCustId());
			    				        
			    				        String resultAMT = restTemplate.postForObject(insideUrl + "/lookup/customer", vars, String.class);
			    				        JSONObject jsonResult = new JSONObject(resultAMT);
			    			        
			    				        int STATE = jsonResult.getInt("STATE");
			    				        long setEmoney = 0;
			    				        
			    				        if(STATE == 200) {		
			    				        	String balanceAMT = ((JSONObject)jsonResult.get("RESULT")).getString("BALANCE_AMT");
			    				        	setEmoney = Long.parseLong(balanceAMT) - Long.parseLong(oneEmoneyInvestPay.getIpay()) - Long.parseLong(withdrawPay);
//			    			
			    				        	emoneyService.updateEmoney(String.valueOf(setEmoney), mid);	// 출금된 내용 업데이트
			    				        	i_pay_use = String.valueOf(setEmoney);
			    				        }
			    			        }
			    			        
			    			        if(Long.parseLong(i_pay_use) < 1) {						// 예치금잔액이 없으면	(동작안함)
			    			        	
			    			        } else if(Long.parseLong(i_pay) <= Long.parseLong(i_pay_use)) {	// 투자조건2. 예치금이 있으면(한개채권 최대투자금액보다 '예치금'이 더 크면) 
						    			if(Long.parseLong(investTotalPayment) + Long.parseLong(i_pay) <= Long.parseLong(limitMonth)) { // 투자조건3. 이달에 최대투자금액확인(최대 투자금 넘지 않았으면)
						        	        String limitPay2 = "0";
						        	        OneInvestLimitPay oneInvestLimitPay2 = investService.selectInvestLimitPay2(mid, loanId);
						        	        															// 해당 채권에 대한 투자최대금액 가져오기 : 
						        	        															// mip-i_look('Y','C','D') 투자진행,마감,상환중인 투자내용중
						        	        if(!oneInvestLimitPay2.getSignpurposeL().equals("false"))	// mm-개인(일반,전문)투자자 중에(m_level<=2), 대출회원이나 일반투자자인경우(m_signpurpose='L','N')	   
						        	        	limitPay2 = oneInvestLimitPay2.getSignpurposeL();	
						        	    	if(!oneInvestLimitPay2.getSignpurposeI().equals("false"))	// mm-개인(일반,전문)투자자 중에(m_level<=2), 소득적격자인경우(m_signpurpose='I')
						        	    		limitPay2 = oneInvestLimitPay2.getSignpurposeI();		
						        	    	if(!oneInvestLimitPay2.getSignpurposeP().equals("false"))	// mm-개인(일반,전문)투자자 중에(m_level<=2), 전문투자자인경우(m_signpurpose='P')
						        	    		limitPay2 = oneInvestLimitPay2.getSignpurposeP();		
						        	    	if(!oneInvestLimitPay2.getSignpurpose3().equals("false")) {
						        	    		limitPay2 = oneInvestLimitPay2.getSignpurpose3();		// mm-개인아닌(m_level>=3), 전문투자자인경우(m_signpurpose='P')
						        		    	if(Long.parseLong(limitPay2) == 0) {
						        		    		limitPay2 = i_pay;									// 해당사항이 없으면 투자자의 채권당 최대한도금액을 넘겨줌
						        		    	}
						        	    	}
						        	        
						        	    	if(Long.parseLong(i_pay) > Long.parseLong(limitPay2)) { 	// 투자조건4. 채권 한도액 확인(투자자가 채권에 투자하는 금액이 '최대 투자가능금액' 보다 크면)
						        	    	} else {													// 아니면
							    		        OneInvestAccount oneInvestAccount = investService.selectAccountById(mid); 	// 투자자 계좌,예치금,은행코드 선택
							    		        
							    		        //state 통신전 처리
							    		        String duplicate = investService.selectInvestDuplicate(mid, loanId);		// 투자자가 해당 채권에 투자한 금액 선택해서
							    		        
							    		        if(duplicate == null) {														// 이미 투자한 건이 아니면
							    		        	String custId = oneMemberService.selectCustID(mid);						// m_id로 cust_id 가져옴
							    		        	RestTemplate restTemplate = new RestTemplate();
							    			        Map<String, String> vars = new HashMap<String, String>();
													//vars.put("CUST_ID", custId);
																			    			        
													//String resultAMT = restTemplate.postForObject(insideUrl + "/lookup/customer", vars, String.class);
													//JSONObject jsonAMT = new JSONObject(resultAMT);
																			    			        
													//String balanceAMT = ((JSONObject)jsonAMT.get("RESULT")).getString("BALANCE_AMT");
							    			        if(Long.parseLong(i_pay) <= Long.parseLong(i_pay_use)) {				// 최대 투자 금액보다 예치금 잔액이 더 크면
							    			            vars = new HashMap<String, String>();
							    			            vars.put("BANK_CD", oneInvestAccount.getMyBankcode());
							    			            vars.put("ACCT_NB", oneInvestAccount.getMyBankacc());
							    			            
							    			            String resultOne = restTemplate.postForObject(insideUrl + "/lookup/reciever", vars, String.class);
							    			            
							    			            JSONObject jsonResult = new JSONObject(resultOne);
							    			            		
							    			            if(jsonResult.getInt("STATE") == 200) {								// 전문 호출이 성공했으면 (계좌정보가 맞으면????????????)
							    			            	String investIsPlaying = investService.selectInvestIsPlaying(loanId);	// 해당 채권에 투자된 금액의 합
							    			            	
							    			            	if(investIsPlaying != null) {									// 해당 채권에 투자된 금액이 있으면
							    					        	String limitPay = "0";
							    					        	OneInvestLimitPay oneInvestLimitPay = investService.selectInvestLimitPay(mid);	// 투자자 한도정보(투자한도, 현재투자금액)선택
							    						        
							    					        	long sumIpay = Long.parseLong(oneInvestLimitPay.getSumIpay()); 		// 투자자의 현재 투자중인금액과
							    					        	
							    					        	if(!oneInvestLimitPay.getSignpurposeL().equals("false"))	// 약관테이블(mari_inset)에서 현재 투자가능금액(대출가능금액-현재투자금액)계산 
							    					        		limitPay = oneInvestLimitPay.getSignpurposeL();
							    					        	if(!oneInvestLimitPay.getSignpurposeI().equals("false"))
							    					        		limitPay = oneInvestLimitPay.getSignpurposeI();
							    					        	if(!oneInvestLimitPay.getSignpurposeP().equals("false"))
							    					        		limitPay = oneInvestLimitPay.getSignpurposeP();
							    					        	if(!oneInvestLimitPay.getSignpurpose3().equals("false"))
							    					        		limitPay = oneInvestLimitPay.getSignpurpose3();
							    					        	
							    						        if(0 < Long.parseLong(limitPay) || sumIpay == 0L) {			// 투자 가능한 상태면(투자가능한도가 있거나, 투자를 한번도 안했으면) 
							    						        	String minPay = investService.selectInvestMinPay(loanId, i_pay); // mip-투자 최소금액 선택
							    						        	
							    							        if(minPay != null) {
							    							        	String possiblePay = investService.selectInvestPossiblePay(loanId); 	// 투자가능금액 선택(대출신청금액-현재투자금액)
							    							        	
							    							        	if(0 < Long.parseLong(possiblePay)) {									// 투자 가능 금액이 있고
							    							        		if(Long.parseLong(i_pay) <= Long.parseLong(possiblePay)) {			// 투자자가 채권에 대한 '투자 최대금액'이 위에 투자가능금액보다 작으면 
							    							        			if(Long.parseLong(i_pay) <= Long.parseLong(limitPay) || sumIpay == 0L) { 	// 투자 가능한 상태인지 한번더 검증해서
							    							        				if(Long.parseLong(i_pay) > Long.parseLong(limitPay2)) { 
							    							        				//return;
							    							        		    	} else {							// 해당 채권에 대한 투자최대금액이, 채권별 최대투자가능금액 보다 크지 않으면 
								    							        				OneInvestTitle oneInvestTitle = investService.selectInvestTitle(mid, loanId);
								    							        				
								    							        				int ranNum = (int)(Math.random() * (999 - 111 + 1)) + 111;	// 111~999 사이값 반환
								    							        				long time = System.currentTimeMillis();
								    							        				String gCode = "P" + String.valueOf(time) + String.valueOf(ranNum);	// 투자자 번호 생성
								    							        				
								    							        				//double eMoney = emoneyService.selectTrAmtBalanceById(mid);
								    							        				i_pay_use = String.valueOf(Long.parseLong(i_pay_use) - Long.parseLong(i_pay));	// 예치금 - 채권에 투자금액
								    							        				
								    							        				if(Long.parseLong(i_pay_use) >= 0) {
									    							        				int isInvestAdd = setInvestAdd(gCode, mid, loanId, i_pay, oneInvestTitle); // mso-예치금 가상계좌 주문테이블에 입력
									    							        																						// mo-예치금 업데이트후 대출정보 정보 반환, mi-대출정보 입력  
									    					        	            		int invsetDetail = 0;
//								  	    
									    					        	            		investService.insertInvestDetailAuto(setInvestDetail(mid, loanId, i_pay, String.valueOf(i_pay_use), oneInvestTitle));
									    					        	            		
									    					        	            		if (invsetDetail > 0 && isInvestAdd > 0) {					
									    					        	            			JSONObject jsonSmsData = new JSONObject();
									    					        	            			jsonSmsData.put("title", oneInvestTitle.getSubject());
									    					        	                		jsonSmsData.put("name", oneInvestTitle.getName());
									    					        	                		jsonSmsData.put("payment", commonUtil.getAmountUnit3(Long.parseLong(i_pay)));
									    					        	                		
//									  
									    					        	                		investService.insertInvestHistory(loanId, custId, i_pay, i_pay, gCode, oneInvestTitle.getSubject(), "I"); // mip-투자정보 입력
									    					        	            			commonUtil.sendBatchLogging("startAutoInvestDivision", "mid : " + mid + " loanId : " + loanId +" i_pay_use : " + i_pay_use
									    					        	            					, "자동투자가 정상적으로 처리하였습니다.");
									    					        	            			
									    					        	            			jsonSmsDataArray.put(jsonSmsData);
									    					        	            			investCust.put(mid, jsonSmsDataArray);								// 대출제목, 이름, 투자액정보 삽입
									    					        	            			
																								//String msg = commonUtil.getFormSMS(9, jsonSmsData);
																								//commonUtil.setRequestSMSData(oneInvestTitle.getName(), "I", oneInvestTitle.getCustId(), oneInvestTitle.getHp(), msg);
									    					        	            		} else 
									    					        	            			commonUtil.sendBatchLogging("startAutoInvestDivision", "mid : " + mid + " loanId : " + loanId +" i_pay_use : " + i_pay_use
									    					        	            					, "자동투자중 에러가 발생하였습니다. invsetDetail : " + invsetDetail + " isInvestAdd : " + isInvestAdd);
								    							        				}
							    							        		    	}
							    							        			}
							    							        		}
							    							        	}
							    							        }
							    						        }
							    			            	} 
							    			            	else {																							// 해당채권에 투자된 금액이 없으면
							    			            		String minPay = investService.selectInvestMinPay(loanId, i_pay);							// mip-투자금액이 최소 투자금액보다 크면 최소투자금액 선택						
							    					        	
							    						        if(minPay != null) {																
							    						        	String possiblePay = investService.selectInvestPossiblePay(loanId);						// 해당 채권에 대출금에서 투자자의 투자금 합을 제외한 값 선택  		
							    						        	
							    						        	if(possiblePay != null) {
							    							        	if(0 < Long.parseLong(possiblePay)) {
							    							        		if(Long.parseLong(i_pay) <= Long.parseLong(possiblePay)) {						// 그 값이 내 투자금보다 크고
							    							        			if(Long.parseLong(i_pay) > Long.parseLong(limitPay2)) {				
							    						        		    	} else {																	// 투자자의 투자금액이 채권 최대 투자금액을 넘지 않으면
								    						        				OneInvestTitle oneInvestTitle = investService.selectInvestTitle(mid, loanId);
								    						        				
								    						        				int ranNum = (int)(Math.random() * (999 - 111 + 1)) + 111;
								    						        				long time = System.currentTimeMillis();
								    						        				String gCode = "P" + String.valueOf(time) + String.valueOf(ranNum);	// s_refId 생성(은행에 들어가기 위한 투자자 번호생성)
								    						        				
								    						        				i_pay_use = String.valueOf(Long.parseLong(i_pay_use) - Long.parseLong(i_pay));	// 예치금 - 채권에 투자금액
								    				        	            		
								    						        				if(Long.parseLong(i_pay_use) >= 0) {
									    						        				int isInvestAdd = setInvestAdd(gCode, mid, loanId, i_pay, oneInvestTitle);	// mso-예치금 가상계좌 주문테이블에 입력
									    						        																							// mo-예치금 업데이트후 대출정보 정보 반환, mi-대출정보 입력  
									    				        	            		int invsetDetail = 0;
//									  
									    				        	            		invsetDetail= investService.insertInvestDetailAuto(setInvestDetail(mid, loanId, i_pay, i_pay_use, oneInvestTitle));
									    				        	            		
									    				        	            		if (invsetDetail > 0 && isInvestAdd > 0) {
									    				        	            			JSONObject jsonSmsData = new JSONObject();
									    				        	            			jsonSmsData.put("title", oneInvestTitle.getSubject());
									    				        	                		jsonSmsData.put("name", oneInvestTitle.getName());
									    				        	                		jsonSmsData.put("payment", commonUtil.getAmountUnit3(Long.parseLong(i_pay)));
									    				        	                		
//								
									    				        	                		investService.insertInvestHistory(loanId, custId, i_pay, i_pay, gCode, oneInvestTitle.getSubject(), "I"); // mip-투자정보 입력
									    				        	            			commonUtil.sendBatchLogging("startAutoInvestDivision", "mid : " + mid + " loanId : " + loanId +" i_pay_use : " + i_pay_use
								    					        	            					, "자동투자가 정상적으로 처리하였습니다.");
									    				        	            			
									    				        	            			jsonSmsDataArray.put(jsonSmsData);
									    				        	            			investCust.put(mid, jsonSmsDataArray);								// 대출제목, 이름, 투자액정보 삽입
									    				        	            			
									    				        	            			//String msg = commonUtil.getFormSMS(9, jsonSmsData);
									    				        	            			//commonUtil.setRequestSMSData(oneInvestTitle.getName(), "I", oneInvestTitle.getCustId(), oneInvestTitle.getHp(), msg);
								    					        	            		} else 
								    					        	            			commonUtil.sendBatchLogging("startAutoInvestDivision", "mid : " + mid + " loanId : " + loanId +" i_pay_use : " + i_pay_use
								    					        	            					, "자동투자중 에러가 발생하였습니다. invsetDetail : " + invsetDetail + " isInvestAdd : " + isInvestAdd);
								    						        				}
							    						        		    	}
							    							        		}
							    							        	}
							    						        	} else {																					// 채권에 투자금이 모두 채워져 있으면 
							    						        		OneInvestTitle oneInvestTitle = investService.selectInvestTitle(mid, loanId);
							    				        				
							    				        				int ranNum = (int)(Math.random() * (999 - 111 + 1)) + 111;
							    				        				long time = System.currentTimeMillis();
							    				        				String gCode = "P" + String.valueOf(time) + String.valueOf(ranNum);
							    				        				
							    				        				//double eMoney = emoneyService.selectTrAmtBalanceById(mid);
							    				        				//double eMoneyCal = eMoney - Double.parseDouble(i_pay);
							    				        				i_pay_use = String.valueOf(Long.parseLong(i_pay_use) - Long.parseLong(i_pay));
							    		        	            		
							    				        				if(Long.parseLong(i_pay_use) >= 0) {
								    				        				int isInvestAdd = setInvestAdd(gCode, mid, loanId, i_pay, oneInvestTitle);
								    		        	            		int invsetDetail = 0;
//								    	
								    		        	            		investService.insertInvestDetailAuto(setInvestDetail(mid, loanId, i_pay, i_pay_use, oneInvestTitle));
								    		        	            		
								    		        	            		if (invsetDetail > 0 && isInvestAdd > 0) {
								    		        	            			JSONObject jsonSmsData = new JSONObject();
											        	            			jsonSmsData.put("title", oneInvestTitle.getSubject());
											        	                		jsonSmsData.put("name", oneInvestTitle.getName());
											        	                		jsonSmsData.put("payment", commonUtil.getAmountUnit3(Long.parseLong(i_pay)));
											        	                		
//					   	              
											        	                		investService.insertInvestHistory(loanId, custId, i_pay, i_pay, gCode, oneInvestTitle.getSubject(), "I");
												        	            		commonUtil.sendBatchLogging("startAutoInvestDivision", "mid : " + mid + " loanId : " + loanId +" i_pay_use : " + i_pay_use
					    					        	            					, "자동투자가 정상적으로 처리하였습니다.");
												        	            		
												        	            		jsonSmsDataArray.put(jsonSmsData);
												        	            		investCust.put(mid, jsonSmsDataArray);
												        	            		
												        	            		//String msg = commonUtil.getFormSMS(9, jsonSmsData);
												        	            		//commonUtil.setRequestSMSData(oneInvestTitle.getName(), "I", oneInvestTitle.getCustId(), oneInvestTitle.getHp(), msg);
					    					        	            		} else 
					    					        	            			commonUtil.sendBatchLogging("startAutoInvestDivision", "mid : " + mid + " loanId : " + loanId +" i_pay_use : " + i_pay_use
					    					        	            					, "자동투자중 에러가 발생하였습니다. invsetDetail : " + invsetDetail + " isInvestAdd : " + isInvestAdd);
							    				        				}
							    						        	}
							    						        }
							    					        }
							    			            }
							    			        }
							    		        }
						        	    	}
						    	        }
			    			        }
		    					}
			    			}
		    			}
		    		}
		    	}
				
				for(String keySet : investCust.keySet()) {
					OneMemberCustAddInfo oneMemberCustAddInfo = oneMemberService.selectCustAddInfo(keySet);
					
//		
					String msg = commonUtil.getFormSMSArray(9, investCust.get(keySet));						// 투자신청 완료 메세지 가지고 와서 csh-에 문자메세지 정보입력
//	     
					commonUtil.setRequestSMSData(oneMemberCustAddInfo.getName(), "I", oneMemberCustAddInfo.getCustId(), oneMemberCustAddInfo.getHp(), msg);
				}
    		}
    		
		} catch (Exception e) {
			commonUtil.sendBatchLogging("startAutoInvestDivision", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
		}
    }
    
    // 5-2. 일괄투자 스케줄러
    @Transactional("oneTransactionManager")
    @Scheduled(cron = "0 0/6 * * * *")
    public void addInvestBundle() {
    	try {
    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live"))
    		{
	    		List<InvestBundleItem2> investBundleItem2s = scheService.selectInvestBundle();
	    		String msg = "";
	    		
	    		if(investBundleItem2s != null) {
	    			
	    			for(int i = 0; i < investBundleItem2s.size(); i++) {
	    				
	    				String loan_id = investBundleItem2s.get(i).getLoanId();
	    				String mid = investBundleItem2s.get(i).getMid();
	    				String i_pay = investBundleItem2s.get(i).getIPay();
	    				
	    		        i_pay = String.valueOf(((Long.parseLong(i_pay) / 10000) * 10000));
	    		        
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
	    		        
	    		    	if(Long.parseLong(i_pay) > Long.parseLong(limitPay2)) {				// 내가 투자한 금액이 건당 최대투자한도액보다 크면
	    		    		Map<String, Object> result = new HashMap<String, Object>();
	    		    		result.put("i_pay", limitPay2);
	    		    		
	    		    		msg = "대출건당 투자가능한 금액한도를 넘었습니다.";
	    		    		scheService.updateInvestStackState(loan_id, mid, "E", msg);
	    		    		
	            			commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id +" limitPay2 : " + limitPay2 , msg);
	    		    		
	    		    		//System.out.println(msg);
	    		    		continue;
	    		    	}
	    		    	
				        OneInvestAccount oneInvestAccount = investService.selectAccountById(mid);
				        
				        //state 통신전 처리
				        String duplicate = investService.selectInvestDuplicate(mid, loan_id);
				        
			        	String custId = oneMemberService.selectCustID(mid);
			        	RestTemplate restTemplate = new RestTemplate();
				        Map<String, String> vars = new HashMap<String, String>();
				        vars.put("CUST_ID", custId);
				        
				        String resultAMT = restTemplate.postForObject(insideUrl + "/lookup/customer", vars, String.class);
				        JSONObject jsonAMT = new JSONObject(resultAMT);
				        
				        String balanceAMT = "0";
				        if(jsonAMT.has("RESULT") && jsonAMT.getJSONObject("RESULT").has("BALANCE_AMT"))
				        	balanceAMT = ((JSONObject)jsonAMT.get("RESULT")).getString("BALANCE_AMT");
				        else {
				        	msg = jsonAMT.getString("MESSAGE");
	    		    		scheService.updateInvestStackState(loan_id, mid, "E", msg);

	    		    		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id , msg);
//	    		    		System.out.println(msg);
				        	continue;
				        }
				        
				        if(Long.parseLong(i_pay) <= Long.parseLong(balanceAMT)) {						// 투자액보다 예치금이 많으면
				        	OneEmoneyInvestPay investPay = emoneyService.selectInvestProgressPay(mid);
				        	
				            vars = new HashMap<String, String>();
				            vars.put("BANK_CD", oneInvestAccount.getMyBankcode());
				            vars.put("ACCT_NB", oneInvestAccount.getMyBankacc());
				            
				            String resultOne = restTemplate.postForObject(insideUrl + "/lookup/reciever", vars, String.class);
				            
				            JSONObject jsonResult = new JSONObject(resultOne);
				            
				            if(jsonResult.getInt("STATE") == 200) {
				            	String investIsPlaying = investService.selectInvestIsPlaying(loan_id);
				            	
				            	if(investIsPlaying != null) {										// 현재 투자중인 금액이 있으면
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
							        	
								        if(minPay != null) {													// mip-i_invest_mini 해당 채권의 최소투자액이 값이 있으면
								        	String possiblePay = investService.selectInvestPossiblePay(loan_id);
								        																		
								        	if(0 < Long.parseLong(possiblePay)) {								// 투자 가능한 금액이 있으면
								        		if(Long.parseLong(i_pay) <= Long.parseLong(possiblePay)) {		// 투자하려는 금액보다 투자가능한 금액이 크면
								        			if(Long.parseLong(i_pay) <= Long.parseLong(limitPay) || sumIpay == 0L) {  // 대출건당 투자가능한 금액한도를 넘지 않았거나, 최대 투자가능한 금액한도가 넘지 않았으면
								        				if(Long.parseLong(i_pay) > Long.parseLong(limitPay2)) {
								        		    		Map<String, Object> result = new HashMap<String, Object>();
								        		    		result.put("i_pay", limitPay2);
								        		    		
								        		    		msg = "대출건당 투자가능한 금액한도를 넘었습니다.";
								        		    		scheService.updateInvestStackState(loan_id, mid, "E", msg);
								        		    		//System.out.println(msg);
									    		    		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id +" limitPay2 : " + limitPay2 , msg);
								        		    		continue;
								        		    	}
								        				
								        				OneInvestTitle oneInvestTitle = investService.selectInvestTitle(mid, loan_id);
								        				
								        				int ranNum = (int)(Math.random() * (999 - 111 + 1)) + 111;
								        				long time = System.currentTimeMillis();
								        				String gCode = "P" + String.valueOf(time) + String.valueOf(ranNum);
								        				
								        				double eMoney = Double.parseDouble(balanceAMT);
								        				double eMoneyCal = (eMoney - Double.parseDouble(i_pay)) - Double.parseDouble(investPay.getIpay());

								        				if(eMoneyCal >= 0) { 				
									        				int isInvestAdd = setInvestAdd(gCode, mid, loan_id, i_pay, oneInvestTitle);
									        				
									        				int invsetDetail = 0;
									        				if(duplicate == null) {													// 해당 채권에 투자가 처음이면 삽입, 아니면 수정 
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
							        	                		
																//String msg = commonUtil.getFormSMS(6, jsonSmsData);
																//commonUtil.setRequestSMSData(oneInvestTitle.getName(), "I", oneInvestTitle.getCustId(), oneInvestTitle.getHp(), msg);
							        	            			
							        	                		msg = "일괄투자가 정상적으로 처리되었습니다.";
									        		    		scheService.updateInvestStackState(loan_id, mid, "Y", msg);
									        		    		// System.out.println(msg);
									        		    		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id +" payment : " + commonUtil.getAmountUnit3(Long.parseLong(i_pay)) , msg);
									        		    		
							        	            		} else {
									        	                msg = "투자 등록에 예치기 못한 에러가 발생하였습니다.";
									        		    		scheService.updateInvestStackState(loan_id, mid, "E", msg);
									        		    		//System.out.println(msg);
									        		    		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id +" payment : " + commonUtil.getAmountUnit3(Long.parseLong(i_pay)) , msg);
									        	                continue;
							        	            		}
								        				} else {
								        					long possible = (((long)eMoney / 10000) * 10000) - Long.parseLong(investPay.getIpay());
						        	            			
								        					if(possible < 0) {
							        	            			Map<String, Object> result = new HashMap<String, Object>();
									        		    		result.put("i_pay", possible);
									        					
									        		    		// possible:투자가능금액 = {예치금(eMoney) - 현재채권 투자하려는 금액(i_pay) - 현재 투자진행중인 금액}
									        		    		
									        		    		msg = "대출건당 투자가능한 금액한도를 넘었습니다.";
									        		    		scheService.updateInvestStackState(loan_id, mid, "E", msg);
									        		    		//System.out.println(msg);
									        		    		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id +" possible : " + possible , msg);
									        		    		continue;
								        					} else {
								        						msg = "예치금액이 적어 투자를 하실 수 없습니다.";
									        		    		scheService.updateInvestStackState(loan_id, mid, "E", msg);
									        		    		//System.out.println(msg);
									        		    		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id +" possible : " + possible  +" balanceAMT : " + balanceAMT , msg);
								        						continue;
								        					}
								        				}
								        				
								        			} else {
								        				if(Long.parseLong(limitPay) > Long.parseLong(limitPay2)) {
								        		    		Map<String, Object> result = new HashMap<String, Object>();
								        		    		result.put("i_pay", limitPay2);
								        					
								        		    		msg = "대출건당 투자가능한 금액한도를 넘었습니다.";
								        		    		scheService.updateInvestStackState(loan_id, mid, "E", msg);
								        		    		//System.out.println(msg);
								        		    		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id +" limitPay2 : " + limitPay2, msg);
								        		    		continue;
								        		    	}
								        				
								        				Map<String, Object> result = new HashMap<String, Object>();
									            		result.put("i_pay", limitPay);
									        			
									            		msg = "최대 투자가능한 금액한도를 넘었습니다.";
									            		scheService.updateInvestStackState(loan_id, mid, "E", msg);
							        		    		//System.out.println(msg);
									            		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id +" limitPay2 : " + limitPay2, msg);
									            		continue;
								        			}
								        			
								        		} else {
								        			if(Long.parseLong(i_pay) - (Long.parseLong(i_pay) - Long.parseLong(possiblePay)) > Long.parseLong(limitPay2)) {
							        		    		Map<String, Object> result = new HashMap<String, Object>();
							        		    		result.put("i_pay", limitPay2);
							        					
							        		    		msg = "대출건당 투자가능한 금액한도를 넘었습니다.";
							        		    		scheService.updateInvestStackState(loan_id, mid, "E", msg);
							        		    		//System.out.println(msg);
							        		    		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id +" limitPay2 : " + limitPay2, msg);
							        		    		continue;
							        		    	}
								        			
								        			Map<String, Object> result = new HashMap<String, Object>();
								            		result.put("i_pay", String.valueOf(Long.parseLong(i_pay) - (Long.parseLong(i_pay) - Long.parseLong(possiblePay))));
								        			
								            		msg = "투자가능한 금액한도를 넘었습니다.";
								            		scheService.updateInvestStackState(loan_id, mid, "E", msg);
						        		    		//System.out.println(msg);
						        		    		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id +" limitPay2 : " + limitPay2, msg);
								            		continue;
								        		}
								        		
								        	} else {
								        		msg = "투자모집이 만료되어 해당 투자상품에 투자가 불가합니다.";
								        		scheService.updateInvestStackState(loan_id, mid, "E", msg);
					        		    		//System.out.println(msg);
					        		    		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id , msg);
								        		continue;
								        	}
								        } else {
								        	msg = "최소투자금액보다 적게 투자하실 수 없습니다.";
								        	scheService.updateInvestStackState(loan_id, mid, "E", msg);
				        		    		//System.out.println(msg);
				        		    		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id , msg);
				        		    		continue;
								        }
							        } else {
							        	msg = "투자가능한 채권한도를 초과하실수 없습니다.";
							        	scheService.updateInvestStackState(loan_id, mid, "E", msg);
			        		    		//System.out.println(msg);
			        		    		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id , msg);
			        		    		continue;
							        }
				            	} else {														// 현재 투자중인 금액이 없으면
				            		String minPay = investService.selectInvestMinPay(loan_id, i_pay);
						        	
							        if(minPay != null) {
							        	String possiblePay = investService.selectInvestPossiblePay(loan_id);
							        	
							        	if(possiblePay != null) {
								        	if(0 < Long.parseLong(possiblePay)) {
								        		if(Long.parseLong(i_pay) <= Long.parseLong(possiblePay)) {
								        			if(Long.parseLong(i_pay) > Long.parseLong(limitPay2)) {
							        		    		Map<String, Object> result = new HashMap<String, Object>();
							        		    		result.put("i_pay", limitPay2);
							        					
							        		    		msg = "대출건당 투자가능한 금액한도를 넘었습니다.";
							        		    		scheService.updateInvestStackState(loan_id, mid, "E", msg);
							        		    		//System.out.println(msg);
							        		    		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id , msg);
							        		    		continue;
							        		    	}
								        			
							        				OneInvestTitle oneInvestTitle = investService.selectInvestTitle(mid, loan_id);
							        				
							        				int ranNum = (int)(Math.random() * (999 - 111 + 1)) + 111;
							        				long time = System.currentTimeMillis();
							        				String gCode = "P" + String.valueOf(time) + String.valueOf(ranNum);
							        				
							        				double eMoney = Double.parseDouble(balanceAMT);
							        				double eMoneyCal = (eMoney - Double.parseDouble(i_pay)) - Double.parseDouble(investPay.getIpay());
					        	            		
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
						        	                		
															//String msg = commonUtil.getFormSMS(6, jsonSmsData);
															//commonUtil.setRequestSMSData(oneInvestTitle.getName(), "I", oneInvestTitle.getCustId(), oneInvestTitle.getHp(), msg);
						        	            			
						        	                		msg = "일괄투자가 정상적으로 처리되었습니다.";
						        	                		scheService.updateInvestStackState(loan_id, mid, "Y", msg);
								        		    		// System.out.println(msg);
								        		    		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id , msg);
						        	            		} else {
						        	            			msg = "투자자 등록에 예치기 못한 에러가 발생하였습니다.";
						        	            			scheService.updateInvestStackState(loan_id, mid, "E", msg);
								        		    		// System.out.println(msg);
						        	            			commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id , msg);
	    						        		    		continue;
						        	            		}
					        	            		} else {
					        	            			long possible = (((long)eMoney / 10000) * 10000) - Long.parseLong(investPay.getIpay());
					        	            			
							        					if(possible < 0) {
						        	            			Map<String, Object> result = new HashMap<String, Object>();
								        		    		result.put("i_pay", possible);
								        					
								        		    		msg = "대출건당 투자가능한 금액한도를 넘었습니다.";
								        		    		scheService.updateInvestStackState(loan_id, mid, "E", msg);
								        		    		//System.out.println(msg);
								        		    		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id , msg);
	    						        		    		continue;
							        					} else {
							        						msg = "예치금액이 적어 투자를 하실 수 없습니다.";
							        						scheService.updateInvestStackState(loan_id, mid, "E", msg);
								        		    		//System.out.println(msg);
								        		    		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id , msg);
	    						        		    		continue;
							        					}
					        	            		}
								        				
								        		} else {
								        			if(Long.parseLong(i_pay) - (Long.parseLong(i_pay) - Long.parseLong(possiblePay)) > Long.parseLong(limitPay2)) {
							        		    		Map<String, Object> result = new HashMap<String, Object>();
							        		    		result.put("i_pay", limitPay2);
	
							        		    		msg = "대출건당 투자가능한 금액한도를 넘었습니다.";
							        		    		scheService.updateInvestStackState(loan_id, mid, "E", msg);
							        		    		//System.out.println(msg);
							        		    		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id , msg);
							        		    		continue;
							        		    	}
								        			
								        			Map<String, Object> result = new HashMap<String, Object>();
								            		result.put("i_pay", String.valueOf(Long.parseLong(i_pay) - (Long.parseLong(i_pay) - Long.parseLong(possiblePay))));
								        			
								            		msg = "투자가능한 금액한도를 넘었습니다.";
								            		scheService.updateInvestStackState(loan_id, mid, "E", msg);
						        		    		//System.out.println(msg);
						        		    		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id , msg);
						        		    		continue;
								        		}
								        		
								        	} else {
								        		msg = "투자모집이 만료되어 해당 투자상품에 투자가 불가합니다.";
								        		scheService.updateInvestStackState(loan_id, mid, "E", msg);
					        		    		//System.out.println(msg);
								        		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id , msg);
					        		    		continue;
								        	}
							        	} else {
							        		OneInvestTitle oneInvestTitle = investService.selectInvestTitle(mid, loan_id);
					        				
					        				int ranNum = (int)(Math.random() * (999 - 111 + 1)) + 111;
					        				long time = System.currentTimeMillis();
					        				String gCode = "P" + String.valueOf(time) + String.valueOf(ranNum);
					        				
					        				double eMoney = Double.parseDouble(balanceAMT);
					        				double eMoneyCal = (eMoney - Double.parseDouble(i_pay)) - Double.parseDouble(investPay.getIpay());
			        	            		
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
				        	                		
														//String msg = commonUtil.getFormSMS(6, jsonSmsData);
														//commonUtil.setRequestSMSData(oneInvestTitle.getName(), "I", oneInvestTitle.getCustId(), oneInvestTitle.getHp(), msg);
				        	            			
				        	                		msg = "일괄투자가 정상적으로 처리되었습니다.";
				        	                		scheService.updateInvestStackState(loan_id, mid, "Y", msg);
						        		    		//System.out.println(msg);
						        		    		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id , msg);
				        	            		}
					        				} else {
			        	            			long possible = (((long)eMoney / 10000) * 10000) - Long.parseLong(investPay.getIpay());
			        	            			
					        					if(possible < 0) {
				        	            			Map<String, Object> result = new HashMap<String, Object>();
						        		    		result.put("i_pay", possible);
						        					
						        		    		msg = "대출건당 투자가능한 금액한도를 넘었습니다.";
						        		    		scheService.updateInvestStackState(loan_id, mid, "E", msg);
						        		    		//System.out.println(msg);
						        		    		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id , msg);
						        		    		continue;
					        					} else {
					        						msg = "예치금액이 적어 투자를 하실 수 없습니다.";
					        						scheService.updateInvestStackState(loan_id, mid, "E", msg);
						        		    		//System.out.println(msg);
						        		    		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id , msg);
						        		    		continue;
					        					}
			        	            		}
							        	}
							        } else {
							        	msg = "최소투자금액보다 적게 투자하실 수 없습니다.";
							        	scheService.updateInvestStackState(loan_id, mid, "E", msg);
			        		    		//System.out.println(msg);
			        		    		commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id , msg);
			        		    		continue;
							        }
						        }
				            } else {
				            	msg = jsonResult.getString("MESSAGE");
				            	scheService.updateInvestStackState(loan_id, mid, "E", msg);
	        		    		//System.out.println(msg);
				            	commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id , msg);
	        		    		continue;
				            }
				        	
				        } else {
				        	msg = "예치금 잔액이 부족합니다.";
				        	scheService.updateInvestStackState(loan_id, mid, "E", msg);
				        	// System.out.println(msg);
				        	commonUtil.sendBatchLogging("addInvestBundle", "mid : " + mid + " loan_id : " + loan_id , msg);
	    		    		continue;
				        }
			        }
				}
    		}
    	} catch (Throwable t) {
    		throw new RuntimeException(t.getMessage());
    	}
    }
    
    
    
    // 6. 예치금 출금을 요청한 고객에게 매일 18시에 출금전문 보내는 스케줄러 (최종수정 V200715)
    @Transactional("oneTransactionManager")
    @Scheduled(cron = "0 0 18 * * *")
    public void startWithdraw() {
    	try {
    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) {
    			//cwt-전문이 호출되지 않은 (trx_flag=N), 투자자 가상계좌(I)정보와 상환받을 합 선택
	    		List<OneVirtualAccntWithdraw> oneVirtualAccntWithdraw = virtualAccntService.selectAccntWithdrawSchedule();
	    		
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
	    	        vars.put("TRAN_MEMO", "크레파스");
	    	        vars.put("GUAR_MEMO", "크레파스");
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
	    	        		commonUtil.sendBatchLogging("startWithdraw", "mid : " + mid + " GUAR_SEQ : " + GUAR_SEQ, "insertDepositHistory : " + insertDepositHistory);
	    	        	}
	    	        	
	    	        	commonUtil.sendBatchLogging("startWithdraw", "mid : " + mid + " GUAR_SEQ : " + GUAR_SEQ, "isDepositSchedule : " + isDepositSchedule);
	    	        } else {
	    	        	boolean isDepositSchedule = virtualAccntService.updateWithdrawSchedule(mid, "F", "");
	                    System.out.println(jsonDeposit.getString("MESSAGE") + " isDepositSchedule : " + isDepositSchedule);
	                    commonUtil.sendBatchLogging("startWithdraw", "fail getCustId : " + oneVirtualRealAccnt.getCustId() + " getMyBankcode : " + oneVirtualRealAccnt.getMyBankcode()
	                    + " getMyBankacc : " + oneVirtualRealAccnt.getMyBankacc() + " getMyBankName : " + oneVirtualRealAccnt.getMyBankName() + " getTrxAmt : " + oneVirtualAccntWithdraw.get(i).getTrxAmt()
	                    , "MESSAGE : " + jsonDeposit.getString("MESSAGE") + " isDepositSchedule : " + isDepositSchedule);
	    	        }
	    		}
    		}
//    		Slack.api.call(new SlackMessage("#young-server","jhlee","4. 투자자 출금정보"));
    	} catch (Throwable t) {
    		commonUtil.sendBatchLogging("startWithdraw", "exception error!!", t.getMessage());
    		Slack.api.call(new SlackMessage("#young-server","jhlee","투자자 출금배치 실패"));
    		throw new RuntimeException(t.getMessage());
    	}
    }
    
    // 7. 상환스케줄러(+투자자에게 지급문자발송) (최종수정 V200715) 
    // 인사이드뱅크(rr,rd)에서 처리완료된 오늘날짜 정보를 선택해서 cwt에 대출자 정보에 처리 완료 업데이트하고,
    // cdt에 투자자 입금정보 입력, ctl에 입,출금정보 입력
    @Transactional("oneTransactionManager")       
    @Scheduled(cron = "0 0/1 * * * *")
//    @Scheduled(cron = "0 0/30 * * * *")					// limit 1 추가, 1분에 한번 돌게 세팅, 하나가 에러나면 다음꺼 돌게 예외처리 해줘야함!
    public void startRepaymentWithdraw() {
    	try {
    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live"))
    		{
    			
    			List<OneWithdraw> repayWithdraw = scheService.selectRepayWithdraw();	// cpas_withdraw_trx 전문 호출하지 않은(trx_flag=N) 상환 가상계좌(type_flag=L) 모두 선택
    			
    			Calendar cal = Calendar.getInstance();
        		int i_Year = cal.get(Calendar.YEAR);
        		int i_Month = cal.get(Calendar.MONTH) + 1;
        		int i_Day = cal.get(Calendar.DAY_OF_MONTH);
        		String s_Month = String.valueOf(i_Month);
        		String s_Days = String.valueOf(i_Day);
        		
        		if(i_Month < 10)
        			s_Month = "0" + i_Month;
        		
        		if(i_Day < 10)
        			s_Days = "0" + i_Day; 
        		
    			for(int i = 0; i < repayWithdraw.size(); i++) {
    				String loanId = repayWithdraw.get(i).getLoanId();
    				String tid = repayWithdraw.get(i).getTid();							// 참고로 인사이드뱅크는 하루에 0925분, 1625분쯤 데이터 처리함
    																					// 인사이드뱅크(rr,rd)-응답코드 00000000이고, 처리완료된(exec_status=02) 오늘날짜 정보선택   
    				List<OneRepaymentInfo> repaymentInfo = depositService.selectRepaymentInfo(loanId, String.valueOf(i_Year) + s_Month + s_Days);
    				
    				if(repaymentInfo != null && repaymentInfo.size() > 0) {
    					
    					int repayWithdrawStateCount = scheService.updateRepayWithdrawState2(tid);
    																										// 상환완료처리(cpas_withdraw_trx-trx_flag N=>S로 변환)
    					//commonUtil.sendBatchLogging("startRepaymentWithdraw", "tid : " + tid, "스케줄 상환처리 : " + scheService.updateRepayWithdrawState(tid));
    					
    					for(int j = 0; j < repaymentInfo.size(); j++) {
    						String payScheduleWithdraw = scheService.selectPayScheduleWithdraw(loanId, repaymentInfo.get(j).getCustID());
    						InsideDepositInfo insideDepositInfo = new InsideDepositInfo();
    						insideDepositInfo.setAccntNb(payScheduleWithdraw);
    						insideDepositInfo.setCustId(repaymentInfo.get(j).getCustID());
    						insideDepositInfo.setErpTransDt(repaymentInfo.get(j).getTranDate() + repaymentInfo.get(j).getTranTime());
    						insideDepositInfo.setTrAmt(repaymentInfo.get(j).getTrAmt());
    						insideDepositInfo.setTrAmtGbn("I");
    						
    						String mid = oneMemberService.selectByCustIdToMid(repaymentInfo.get(j).getCustID());
    						
    						boolean isInsertDeposit2 = scheService.insertDeposit2(insideDepositInfo);		// cpas_deposit_trx에 투자자에 입금 정보삽입
    						
    						
    						String repayCount = scheService.selectRepayCount3(loanId, mid);					// mo-최근 상환예정 회차정보를 가지고 와서(flag = P)

    						if(repayCount == null)
    							repayCount = scheService.selectRepayCount(loanId);							// 없으면 상환완료된 최고회차 가지고옴(혹시 P가 상환 완료되었을 경우를 가정한것이라 판단)
    						
    						
    						
    																										// cpas_trx_log에 입,출금 정보삽입
    						// 5. 대출자 출금, 투자자 지급 처리(loanId, 회차정보)
//    						boolean isInsertDepositHistoryW = scheService.insertDepositHistory(oneMemberService.selectByLoanIdToMid(loanId), "W", repaymentInfo.get(j).getTrAmtP(), "L");
//    						boolean isInsertDepositHistoryD = scheService.insertDepositHistory(mid, "D", repaymentInfo.get(j).getTrAmt(), "I");
    				
    						boolean isInsertDepositHistoryW = scheService.insertDepositHistory(oneMemberService.selectByLoanIdToMid(loanId), "W", repaymentInfo.get(j).getTrAmtP(), "L", loanId, repayCount);
    						boolean isInsertDepositHistoryD = scheService.insertDepositHistory(mid, "D", repaymentInfo.get(j).getTrAmt(), "I", loanId, repayCount);
    						
    						
    						OneInvestTitle oneInvestTitle = investService.selectInvestTitle(mid, loanId);
    						
    						//String repayCount = scheService.selectRepayCount2(loanId, mid, repaymentInfo.get(j).getTrAmt());
    						
    						
    						if(repayCount != null && !repayCount.isEmpty()) {
	    						JSONObject jsonSmsData = new JSONObject();
		            			jsonSmsData.put("title", oneInvestTitle.getSubject());
		                		jsonSmsData.put("name", oneInvestTitle.getName());
		                		jsonSmsData.put("count", repayCount);
	    						
		                		scheService.updateOrderScheduleFinish(mid, loanId, repayCount);				// mari_order-o_repayment_status=P 회차정보를 Y로
		                		
		                		String msg = commonUtil.getFormSMS(8, jsonSmsData);							// 해당회차 정산 알림메세지 csh에 입력
		                		commonUtil.setRequestSMSData(oneInvestTitle.getName(), "R", oneInvestTitle.getCustId(), oneInvestTitle.getHp(), msg);
    						}
	                		
    						commonUtil.sendBatchLogging("startRepaymentWithdraw", "insideDepositInfo : " + insideDepositInfo, "isInsertDeposit2 : " + isInsertDeposit2);
    						commonUtil.sendBatchLogging("startRepaymentWithdraw", "loanId : " + loanId + " getTrAmtP :" + repaymentInfo.get(j).getTrAmtP(), "isInsertDepositHistoryW : " + isInsertDepositHistoryW);
    						commonUtil.sendBatchLogging("startRepaymentWithdraw", "getCustID : " + repaymentInfo.get(j).getCustID() + " getTrAmt : " + repaymentInfo.get(j).getTrAmt(), "isInsertDepositHistoryD : " + isInsertDepositHistoryD);
    					}
    					
    					
    				}
    			}
    		}
//    		Slack.api.call(new SlackMessage("#young-server","jhlee","5. 대출자 출금, 투자자 지급 처리"));
    	} catch (Throwable t) {
    		commonUtil.sendBatchLogging("startRepaymentWithdraw", "exception error!!", t.getMessage());
    		Slack.api.call(new SlackMessage("#young-server","jhlee","대출자 출금, 투자자 지급 처리 실패"));
    		throw new RuntimeException(t.getMessage());
    	}
    }

    @Transactional("oneTransactionManager")	// @Scheduled(cron = "0 39 17 * * *")
    @Scheduled(cron = "0 0/1 * * * *")
    // 8. 대출자(repayment), 투자자(payment) 상환스케줄 생성스케줄러  (최종수정 V200715)
    public void addRepaymentSchedule() {
    	try {
    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) 
    		{

    			//대출승인 완료 채권중(ml.loan_step4='Y')에 mip-상환스케줄 생성 배치가 실행 안된것(i_exec_repaybatch=N) 선택
    			List<OneRepayScheduleInfo> oneRepayScheduleInfos = scheService.selectRepayScheduleInfo();
//    			List<OneRepayScheduleInfo> oneRepayScheduleInfos = scheService.selectRepayScheduleInfoTest();
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
	    			
				//	    			double taxRate = 0.25;
				//	    			double taxRateLocal = 0.025;
				//	    			
				//	    			if(execDate.split("-")[0].equals("2020")){
				//	    				taxRate = 0.14;
				//	    				taxRateLocal = 0.025;
				//	    			}
					    			
				//	    			if(repayInfo.equals("만기일시상환"))
				//	    				repayInfo = "0";
				//	    			else if(repayInfo.equals("원금균등상환"))
				//	    				repayInfo = "1";
				//	    			else if(repayInfo.equals("원리금균등상환"))
				//	    				repayInfo = "2";
	    			
	    			//............. 대출일보다 첫번째 상환일이 15일이하면 +1월 해야함;;;  
	    			Calendar calendar = Calendar.getInstance();
	    			int Year = calendar.get(Calendar.YEAR);													//현재년도
					int Month = calendar.get(Calendar.MONTH);												//현재월
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
// 200729
	    				if(j == jsonRepaySchedules.length()-1) { // 마지막 회차 이면, 대출 실행일로 치환
	    					resultDate = resultDate.substring(0,7)+execDate.substring(7,10);
	    				}
	    					
	    	    		OneRepayScheduleAdd oneRepayScheduleAdd = new OneRepayScheduleAdd();
	    	    		oneRepayScheduleAdd.setLoanId(loanId);
	    	    		oneRepayScheduleAdd.setRepayCount(payCount);
	    	    		oneRepayScheduleAdd.setRepayDate(resultDate);
	    	    		oneRepayScheduleAdd.setPayAmount(repayAmount);
	    	    		oneRepayScheduleAdd.setLnAmount(paidAmount);
	    	    		oneRepayScheduleAdd.setInterestAmount(loanInterest);
	    	    		oneRepayScheduleAdd.setBalance(balance);

	    	    		// 이메일 발송을 위한 내용 저장
	    	    		OneSendCheckingEmail oneSendCheckingEmail = new OneSendCheckingEmail();
	    	    		oneSendCheckingEmail.setLoanId(loanId);
	    	    		oneSendCheckingEmail.setRepayCount(payCount);
	    	    		oneSendCheckingEmail.setRepayDate(resultDate);
	    	    		oneSendCheckingEmail.setPayAmount(repayAmount);
	    	    		oneSendCheckingEmail.setLnAmount(paidAmount);
	    	    		oneSendCheckingEmail.setInterestAmount(loanInterest);
	    	    		oneSendCheckingEmail.setBalance(balance);
	    	    		
	    	    		oneSendCheckingEmailList.add(oneSendCheckingEmail);
	    	    		
//	    	    			System.out.println(oneRepayScheduleAdd);    		
	    	    		if(!scheService.insertRepaySchedule(oneRepayScheduleAdd)) {							// 대출자 상환스케줄 생성		
	    	    			commonUtil.sendBatchLogging("addRepaymentSchedule", "loanId : " + loanId + " payCount : " + payCount+ " resultDate : " + resultDate
	    	    					+ " repayAmount : " + repayAmount + " paidAmount : " + paidAmount + " loanInterest : " + loanInterest + " balance : " + balance, "상환 스케줄 등록에 실패하였습니다.");
	    	    			break;
	    	    		} 
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
//		    				
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
	    	    																				// 투자자가 받게 될 실제 금액(생성테이블이기 때문에 연체이자는 제외한 금액)
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
	    	    			
	    	    			//	    	    			 System.out.println(onePaymentSchedule);
	    	    			if(!scheService.insertPaymentSchedule(onePaymentSchedule)) { 		//투자자 스케줄 생성
		    	    			commonUtil.sendBatchLogging("addRepaymentSchedule", "loanId : " + loanId + " payCount : " + payCount+ " inAmount : " + String.valueOf(Math.ceil(inAmount))
		    	    					+ " interest : " + String.valueOf(Math.ceil(interest)) + " tax_real : " + tax_real + " tax_local_real : " + tax_local_real + " fee : " + fee
		    	    					+ " investAmount : " + investAmount + " getMid : " + onePaymentInvestSchedules.get(k).getMid(), "정산 스케줄 등록에 실패하였습니다.");
		    	    			break;
		    	    		}
	    	    			
							//	    	    			System.out.println(loanId + "	" + payCount+ "	" + String.valueOf(Math.ceil(inAmount)) + "	" + String.valueOf(Math.floor(fee)) + "	" + String.valueOf(Math.ceil(interest)) + "	" + String.valueOf(tax_real));
							//					+ " interest : " + String.valueOf(Math.ceil(interest)) + " tax_real : " + tax_real + " tax_local_real : " + tax_local_real + " fee : " + fee
							//					+ " investAmount : " + investAmount + " getMid : " + onePaymentInvestSchedules.get(k).getMid(), "정산 스케줄 등록(200416)에 실패하였습니다.");

	    	    			paidAmountTmt += Math.ceil(inAmount);
	    	    		}
	    			}
		    		
	    			// 상환스케줄 등록(i_exec_repaybatch N=>Y로 변경)
	    			if(scheService.updateRepayScheduleState(loanId)) {
	    				commonUtil.sendBatchLogging("addRepaymentSchedule", "loanId : " + loanId, "상환 스케줄 등록이 완료되었습니다.");
	    			} else {
	    				commonUtil.sendBatchLogging("addRepaymentSchedule", "loanId : " + loanId, "상환 스케줄 상태 업데이트가 실패하였습니다.");
	    				break;
	    			}
	    		}
    		}
    	} catch (Throwable t) {
    		commonUtil.sendBatchLogging("addRepaymentSchedule", "exception error!!", t.getMessage());
    		Slack.api.call(new SlackMessage("#young-server","jhlee","상환 스케줄러 생성배치 실패"));
    		throw new RuntimeException(t.getMessage());
    	}
    }
    
    // 9. 대출자 상환 스케줄(인사이드뱅크에 정산가능한정보 전송하는 스케줄) (최종수정 V200715)
    @Transactional("oneTransactionManager")
    @Scheduled(cron = "30 0 9,16 * * MON-FRI")
//    @Scheduled(cron = "0 13 11 * * MON-FRI")
    public void startPaymentSchedule() {
    	try {
    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) 
    		{
    			 																				// mari_order-상환되지 않은 mo.o_repayment_status=N 
	    		List<OnePaymentSchedule> onePaymentSchedule = scheService.selectPaymentScheduleStart();
	    		
	    		RestTemplate restTemplate = new RestTemplate();
				HashMap<String, Object> vars = new HashMap<String, Object>();
				List<HashMap<String, String>> listChild = new ArrayList<>();
				HashMap<String, String> listItem = null;// = new HashMap<>();
		         
		        long TOTAL_TR_AMT = 0;
		        long TOTAL_TR_AMT_P = 0;
		        long TOTAL_CTAX_AMT = 0;
		        long TOTAL_FEE = 0;
		        
		        JSONObject jsonFinal = null;
		        JSONArray jsonFinalArray = new JSONArray();
	
		        HashMap<String, String> loanDuplicate = new HashMap<String, String>();
		        HashMap<String, Long> loanPaySum = new HashMap<String, Long>();
		        
		        HashMap<String, OneStartPaymentInfo> loanPaySumTest = new HashMap<String, OneStartPaymentInfo>();	// loanId에 카운트를 넣자!
		        
		        HashMap<String, String> countChecker = new HashMap<String, String>();	// 에러메시지 전송용
		        
		        if(onePaymentSchedule.size() > 0) {
		    		for(int i = 0; i < onePaymentSchedule.size(); i++) {
		    			//////////
		    			String mid = onePaymentSchedule.get(i).getMid();
		    			String loanId = onePaymentSchedule.get(i).getLoanId();
		    			String lnAmount = onePaymentSchedule.get(i).getLnAmount();
		    			String interestAmount = onePaymentSchedule.get(i).getInterestAmount();
		    			String delqAmount = onePaymentSchedule.get(i).getDelqAmount();
		    			String tax = onePaymentSchedule.get(i).getTax();
		    			String taxLocal = onePaymentSchedule.get(i).getTaxLocal();
		    			String fee = onePaymentSchedule.get(i).getFee();
		    			String loanPay = onePaymentSchedule.get(i).getLoanPay();
		    			String loanDay = onePaymentSchedule.get(i).getLoanDay();
		    			String repayCount = onePaymentSchedule.get(i).getRepayCount();
		    	
		    			int numberOfOrderCount = scheService.selectNumberOfOrderCount(loanId, repayCount);
		    			int numberOfPaymentCount = scheService.selectNumberOfPaymentCount(loanId, repayCount);

		    			if (numberOfOrderCount == numberOfPaymentCount) {		// 투자자수와 mo에 수가 맞지 않으면 정산처리 하지 않음
			    			
			    			long sumPay = (Long.parseLong(lnAmount) + Long.parseLong(interestAmount) + Long.parseLong(delqAmount))
			    					- (Long.parseLong(tax) + Long.parseLong(taxLocal) + (long)Double.parseDouble(fee));
			    			
			    			long sumPayln = (Long.parseLong(lnAmount) + Long.parseLong(interestAmount) + Long.parseLong(delqAmount));	// 상환할 총금액(원금+이자+연체이자)과
	
			    			////////////////////////////////////////////////
	
					        // cust_ID	tr_amt, loan_id count 로그 남기고,, 로그 쌓인거 확인해야함
					        // oneMemberCustId.getCustId()  총지급액 sumPay	loanId
					        OneMemberCustId oneMemberCustId = oneMemberService.selectCustID2(mid);
					        // 오늘날짜
					        SimpleDateFormat sdf = new SimpleDateFormat ( "yyyyMMdd");
					        Date todayDate = new Date();
					        String today = sdf.format(todayDate);
					        
							//				        int principalRequestInfo = depositService.selectPrincipalRequestInfo(today, oneMemberCustId.getCustId(), Long.toString(sumPay), loanId);
											        
							//				        if (principalRequestInfo == 0) {	// 중복된 건이 없으면 
			    			
				    			OneStartPaymentInfo oneStartPaymentInfo = new OneStartPaymentInfo();
		
				    			if(loanPaySum.containsKey(loanId)) {							// 해당건(mo-투자자정보)에 loanId가 있으면 금액 합산(대출자가 지불해야 할 돈만큼이 합계가 됨) 
				    				loanPaySum.put(loanId, loanPaySum.get(loanId) + sumPayln);
				    				
				    			}
				    			else {
				    				loanPaySum.put(loanId, sumPayln);
		
				    			}
				    			if(loanPaySumTest.containsKey(loanId)) {
				    				
				    				oneStartPaymentInfo.setLoanId(loanId);									
				    				oneStartPaymentInfo.setRepayCount(repayCount);
				    				oneStartPaymentInfo.setSumPayln(loanPaySumTest.get(loanId).getSumPayln() + sumPayln);		
				    			}
				    			else {
				    				oneStartPaymentInfo.setLoanId(loanId);									
				    				oneStartPaymentInfo.setRepayCount(repayCount);
				    				oneStartPaymentInfo.setSumPayln(sumPayln);								
				    			}
				    			
				    			System.out.println(oneStartPaymentInfo.toString());
				    			
				    			loanPaySumTest.put(loanId, oneStartPaymentInfo);
				    			
				    			TOTAL_TR_AMT += sumPay;
				    			//TOTAL_TR_AMT_P += Long.parseLong(lnAmount);
				    			TOTAL_TR_AMT_P += sumPayln;
				    			//TOTAL_CTAX_AMT += Long.parseLong(tax);
				    			TOTAL_CTAX_AMT += Long.parseLong(tax) + Long.parseLong(taxLocal);
				    			TOTAL_FEE += (long)Double.parseDouble(fee);
				    			
				    			//		    			OneMemberCustId oneMemberCustId = oneMemberService.selectCustID2(mid);
				    			String principalNum = scheService.selectPrincipalNum(mid, loanId);		// mari_invest-수취권 증서번호(invest_proof_no) 가져와서
				    			
				    			listItem = new HashMap<>();
						        listChild.add(listItem);
						        listItem.put("DC_NB", loanId);
						        listItem.put("CUST_ID", oneMemberCustId.getCustId());
						        listItem.put("TR_AMT", String.valueOf(sumPay));
						        listItem.put("TR_AMT_P", String.valueOf(sumPayln));						// 증서번호 전문보낼 리스트에 넣음
						        listItem.put("CTAX_AMT", String.valueOf(Long.parseLong(tax) + Long.parseLong(taxLocal)));
						        listItem.put("FEE", String.valueOf((long)Double.parseDouble(fee)));
						        listItem.put("REPAY_RECEIPT_NB", principalNum);							// 증서번호 전문보낼 리스트에 넣음
						        vars.put("DETAIL", listChild);
						        
						        if(onePaymentSchedule.get(i).getRepayCount().equals(loanDay)) {			// 1. 마지막 회차이면서  
						        	SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat ("yyyyMMdd", Locale.KOREA );
						        	Date currentTime = new Date ();
						        	String mTime = mSimpleDateFormat.format ( currentTime );
						        	
						        	if(!loanDuplicate.containsKey(loanId)) {							// 2. 아직처리 안했으면(중복값이 없으면) 전문으로 보낼 내용에 마지막 회차정보(jsonFinalArray) 넣어둠 
							        	jsonFinal = new JSONObject();
							        	jsonFinal.put("LOAN_SEQ", loanId);
							        	jsonFinal.put("LOAN_AMT", loanPay);
							        	jsonFinal.put("LOAN_EXP_DATE", mTime);
							        	jsonFinalArray.put(jsonFinal);
							        	loanDuplicate.put(loanId, loanId);
						        	}
						        }
						//				        } else { // 중복된 지급건이 있으면
						//				        	commonUtil.sendBatchLogging("startPaymentSchedule", "중복지급에러", "중복된 지급건이 있습니다. : " + ", today : " + today +  ", oneMemberCustId.getCustId() : " + 
						//				        oneMemberCustId.getCustId() +  ", 원금 : " + sumPay + ", loanId : " +  loanId);
						//				        }
					        ///////////////////////////////////////////////////
		    	
		    		
				    		vars.put("TOTAL_CNT", String.valueOf(onePaymentSchedule.size()));
					        vars.put("TOTAL_TR_AMT", String.valueOf(TOTAL_TR_AMT));
					        vars.put("TOTAL_TR_AMT_P", String.valueOf(TOTAL_TR_AMT_P));
					        vars.put("TOTAL_CTAX_AMT", String.valueOf(TOTAL_CTAX_AMT));
					        vars.put("TOTAL_FEE", String.valueOf(TOTAL_FEE));
				
		    			}
		    			else {
		    				countChecker.put(loanId, repayCount);
		    			}
		    		}
			        
		    		for(String key : countChecker.keySet()) {
		    			String value = countChecker.get(key);
		    			System.out.println();
		    			Slack.api.call(new SlackMessage("#young-server","jhlee", "상환처리에러(갯수상이), loanId : " + key + ", 회차 : " + value));
		    		}
			        
			        String resultDeposit=null;
//			        // 돈요청하는 전문 조심해야함
			        resultDeposit = restTemplate.postForObject(insideUrl + "/principal/request", vars, String.class); 
			        commonUtil.sendBatchLogging("startPaymentSchedule", resultDeposit, "원리금 상환 결과");
			        JSONObject jsonDeposit = new JSONObject(resultDeposit);
			        
			        if(jsonDeposit.getInt("STATE") == 200) {
			        	for(int i = 0; i < onePaymentSchedule.size(); i++) {
			        		String loanId = onePaymentSchedule.get(i).getLoanId();
			    			String oid = onePaymentSchedule.get(i).getOid();
			    			String pid = onePaymentSchedule.get(i).getPid();
			    			String repayCount = onePaymentSchedule.get(i).getRepayCount();
			    			
			    			boolean isUpdatePaymentSchedule = scheService.updatePaymentSchedule(pid);			// 해당회차 정산완료로 업데이트(cps-p_pay_status N=>C) 
			    			boolean isUpdateOrderSchedule = scheService.updateOrderSchedule(oid);				// 해당회차 정산완료로 업데이트(mo-o_repayment_status N=>P) 
			    			boolean isUpdateOrverDueState = repaymentService.updateOrverDueState(loanId, repayCount);	// 해당회차가 연체상태로 되어져 있으면 정산처리(coh-state N=>S)
			    			
			    			commonUtil.sendBatchLogging("startPaymentSchedule", "pid : " + pid, "isUpdatePaymentSchedule : " + isUpdatePaymentSchedule);
			    			commonUtil.sendBatchLogging("startPaymentSchedule", "oid : " + oid, "isUpdateOrderSchedule : " + isUpdateOrderSchedule);
			    			commonUtil.sendBatchLogging("startPaymentSchedule", "loanId : " + loanId + ", repayCount : " + repayCount, "isUpdateOrverDueState : " + isUpdateOrverDueState);
			    			
			        	}
			        	 
			        	for(String loanIds : loanPaySum.keySet()) {												// 출금테이블에 대출자 정산정보 입력
			        		// 2. cwt
							//	 boolean isInsertAccntWithdrawSchedule2 = virtualAccntService.insertAccntWithdrawSchedule2(scheService.selectLoanIdByMid(loanIds), loanIds, String.valueOf(loanPaySum.get(loanIds)), "L");
							//	 commonUtil.sendBatchLogging("startPaymentSchedule", "loanIds : " + loanIds, "isInsertAccntWithdrawSchedule2 : " + isInsertAccntWithdrawSchedule2);
			        	}
			        	
			        	for(HashMap.Entry<String, OneStartPaymentInfo> entry : loanPaySumTest.entrySet()) {
			        		String mid = scheService.selectLoanIdByMid(entry.getValue().getLoanId());
			        		
			        		boolean isInsertAccntWithdrawSchedule4 = virtualAccntService.insertAccntWithdrawSchedule4(mid, entry.getValue().getLoanId(),
			        				Long.toString(entry.getValue().getSumPayln()), "L", entry.getValue().getRepayCount());
			        		
			        		commonUtil.sendBatchLogging("startPaymentSchedule", "loanIds : " + entry.getValue().getLoanId(), "isInsertAccntWithdrawSchedule4 : " + isInsertAccntWithdrawSchedule4);
			        		
			        	}
			        	
			        	for(int i = 0; i < jsonFinalArray.length(); i++) {
			        		JSONObject jsonFinally = jsonFinalArray.getJSONObject(i);
			        		HashMap<String, Object> finallys = new HashMap<String, Object>();
			        		finallys.put("LOAN_SEQ", jsonFinally.getString("LOAN_SEQ"));
			        		finallys.put("LOAN_AMT", jsonFinally.getString("LOAN_AMT"));
			        		finallys.put("LOAN_EXP_DATE", jsonFinally.getString("LOAN_EXP_DATE"));
			        		
			        		String resultFinally = null;
			        		resultFinally  = restTemplate.postForObject(insideUrl + "/loan/repay/complete", finallys, String.class);	
					        JSONObject jsonResult = new JSONObject(resultFinally);
					        
					        if(jsonResult.getInt("STATE") == 200)
					        	commonUtil.sendBatchLogging("startPaymentSchedule", finallys.toString(), "대출 상환 완료");
					        else
					        	commonUtil.sendBatchLogging("startPaymentSchedule", finallys.toString(), "대출 상환 완료 에러 : " + jsonResult.getString("MESSAGE"));
			        	}
			        } else {
			        	commonUtil.sendBatchLogging("startPaymentSchedule", vars.toString(), "원리금 상환 에러");
			        	commonUtil.sendLoggingEmail("[정산]원리금 상환 에러", vars.toString());
			        }
		        }
    		}
    	} catch (Throwable t) {
    		commonUtil.sendBatchLogging("startPaymentSchedule", "exception error!!", t.getMessage());
    		Slack.api.call(new SlackMessage("#young-server","jhlee","대출자 상환배치 실패"));
    		throw new RuntimeException(t.getMessage());
    	}
    }
   
    
    
    
    // 10. 대출자가 중도상환하는 스케줄러  (최종수정 V200715)
    //cpas_prepayment_provide-p_status=N / 상환전문처리 + 상환완료전문처리
    @Transactional("oneTransactionManager")
    @Scheduled(cron = "0 5 9,16 * * MON-FRI")
//    @Scheduled(cron = "0 20 10 * * MON-FRI")
    //
    public void startPrePaymentSchedule() {				    						
    	try {
    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) {						// 중도상환 신청자중 cpas_prepayment_provide에 처리 안된 대출정보 선택(cpp.p_status=N) 
	    		List<OnePrePaymentSchedule> onePrePaymentSchedules = scheService.selectPrePaymentScheduleStart();
	    		
	    		RestTemplate restTemplate = new RestTemplate();
				HashMap<String, Object> vars = new HashMap<String, Object>();
				List<HashMap<String, String>> listChild = new ArrayList<>();
				HashMap<String, String> listItem = null;// = new HashMap<>();
		         
		        long TOTAL_TR_AMT = 0;
		        long TOTAL_TR_AMT_P = 0;
		        long TOTAL_CTAX_AMT = 0;
		        long TOTAL_FEE = 0;
		        
		        JSONObject jsonFinal = null;
		        JSONArray jsonFinalArray = new JSONArray();
	
		        HashMap<String, String> loanDuplicate = new HashMap<String, String>();
		        HashMap<String, Long> loanPaySum = new HashMap<String, Long>();
		        
		        if(onePrePaymentSchedules.size() > 0) {
		    		for(int i = 0; i < onePrePaymentSchedules.size(); i++) {
		    			String mid = onePrePaymentSchedules.get(i).getMid();
		    			String loanId = onePrePaymentSchedules.get(i).getLoanId();
		    			String interest = onePrePaymentSchedules.get(i).getInterest();
		    			String tax = onePrePaymentSchedules.get(i).getTax();
		    			String taxLocal = onePrePaymentSchedules.get(i).getTaxLocal();
		    			String fee = onePrePaymentSchedules.get(i).getFee();
		    			String payAmount = onePrePaymentSchedules.get(i).getPayAmount();
		    			String loanPay = onePrePaymentSchedules.get(i).getLoanPay();
		    			String repayCount = onePrePaymentSchedules.get(i).getRepayCount();
		    			
		    			
		    			long sumPay = Long.parseLong(payAmount);								// 투자자에게 지급될 총금액
		    			long sumPayln = (Long.parseLong(payAmount) + (Long.parseLong(tax) + Long.parseLong(taxLocal) + Long.parseLong(fee))); // 세금 수수료 제외한 총금액

		    			if(loanPaySum.containsKey(loanId))										// ?
		    				loanPaySum.put(loanId, loanPaySum.get(loanId) + sumPayln);
		    			else
		    				loanPaySum.put(loanId, sumPayln);	
		    			
		    			TOTAL_TR_AMT += sumPay;
		    			TOTAL_TR_AMT_P += sumPayln;
		    			TOTAL_CTAX_AMT += Long.parseLong(tax) + Long.parseLong(taxLocal);
		    			TOTAL_FEE += Long.parseLong(fee);
		    			
		    			OneMemberCustId oneMemberCustId = oneMemberService.selectCustID2(mid);	// custId와
		    			String principalNum = scheService.selectPrincipalNum(mid, loanId);		// 수취권 증서번호 가져와서 
		    			
		    			listItem = new HashMap<>();
				        listChild.add(listItem);
				        listItem.put("DC_NB", loanId);
				        listItem.put("CUST_ID", oneMemberCustId.getCustId());					// 전문에 넣어줌1 
				        listItem.put("TR_AMT", String.valueOf(sumPay));
				        listItem.put("TR_AMT_P", String.valueOf(sumPayln));
				        listItem.put("CTAX_AMT", String.valueOf(Long.parseLong(tax) + Long.parseLong(taxLocal)));
				        listItem.put("FEE", fee);
				        listItem.put("REPAY_RECEIPT_NB", principalNum);							// 전문에 넣어줌2 
				        vars.put("DETAIL", listChild);
				        
				        if(!loanDuplicate.containsKey(loanId)) {								// 아직처리 안했으면(중복값이 없으면) 		
			        		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat ("yyyyMMdd", Locale.KOREA );
				        	Date currentTime = new Date ();
				        	String mTime = mSimpleDateFormat.format ( currentTime );
			        		
				        	jsonFinal = new JSONObject();
				        	jsonFinal.put("LOAN_SEQ", loanId);
				        	jsonFinal.put("LOAN_AMT", loanPay);
				        	jsonFinal.put("LOAN_EXP_DATE", mTime);
				        	jsonFinalArray.put(jsonFinal);
				        	loanDuplicate.put(loanId, loanId);
			        	}
		    		}
		    		
		    		vars.put("TOTAL_CNT", String.valueOf(onePrePaymentSchedules.size()));
			        vars.put("TOTAL_TR_AMT", String.valueOf(TOTAL_TR_AMT));
			        vars.put("TOTAL_TR_AMT_P", String.valueOf(TOTAL_TR_AMT_P));
			        vars.put("TOTAL_CTAX_AMT", String.valueOf(TOTAL_CTAX_AMT));
			        vars.put("TOTAL_FEE", String.valueOf(TOTAL_FEE));
			        
			        String resultDeposit = restTemplate.postForObject(insideUrl + "/principal/request", vars, String.class);	// 상환전문요청
			        JSONObject jsonDeposit = new JSONObject(resultDeposit);
			        
			        if(jsonDeposit.getInt("STATE") == 200) {
			        	for(int i = 0; i < onePrePaymentSchedules.size(); i++) {
			    			boolean isUpdatePrePaymentSchedule = scheService.updatePrePaymentSchedule(onePrePaymentSchedules.get(i).getPid());	// cpp-처리안된정보(p_status=N) Y로 업데이트 
			    			commonUtil.sendBatchLogging("startPrePaymentSchedule", "getPid : " + onePrePaymentSchedules.get(i).getPid(), "isUpdatePrePaymentSchedule : " + isUpdatePrePaymentSchedule);
			        	}
			        	
			        	for(String loanIds : loanPaySum.keySet()) {
			        		// 3. cwt 중도상환에서도 회차정보 넣기!
			        		boolean isInsertAccntWithdrawSchedule2 = virtualAccntService.insertAccntWithdrawSchedule2(scheService.selectLoanIdByMid(loanIds), loanIds, String.valueOf(loanPaySum.get(loanIds)), "L");	// 대출자가 상환한 금액정보 입력
			        		commonUtil.sendBatchLogging("startPrePaymentSchedule", "loanIds : " + loanIds, "isInsertAccntWithdrawSchedule2 : " + isInsertAccntWithdrawSchedule2);
			        	}
			        	
			        	for(int i = 0; i < jsonFinalArray.length(); i++) {
			        		JSONObject jsonFinally = jsonFinalArray.getJSONObject(i);
			        		HashMap<String, Object> finallys = new HashMap<String, Object>();
			        		finallys.put("LOAN_SEQ", jsonFinally.getString("LOAN_SEQ"));
			        		finallys.put("LOAN_AMT", jsonFinally.getString("LOAN_AMT"));
			        		finallys.put("LOAN_EXP_DATE", jsonFinally.getString("LOAN_EXP_DATE"));
			        		
			        		String resultFinally = restTemplate.postForObject(insideUrl + "/loan/repay/complete", finallys, String.class);	// 상환완료전문요청
					        JSONObject jsonResult = new JSONObject(resultFinally);
					        
					        if(jsonResult.getInt("STATE") == 200)
					        	commonUtil.sendBatchLogging("startPrePaymentSchedule", finallys.toString(), "대출 중도상환 완료");
					        //System.out.println("대출 중도상환 완료 : " + finallys.toString());
					        else
					        	commonUtil.sendBatchLogging("startPrePaymentSchedule", finallys.toString(), "대출 중도상환 완료 에러 : " + jsonResult.getString("MESSAGE"));
					        //System.out.println("대출 중도상환 완료 에러 : " + finallys.toString());
			        	}
			        } else {
			        	commonUtil.sendBatchLogging("startPrePaymentSchedule", vars.toString(), "원리금 중도상환 에러");
			        	//System.out.println("원리금 중도상환 에러 : " + vars.toString());
			        }
			        
			        //System.out.println("vars : " + vars.toString());
		        }
    		}
    	} catch (Throwable t) {
    		//t.printStackTrace();
    		commonUtil.sendBatchLogging("startPrePaymentSchedule", "exception error!!", t.getMessage());
    		Slack.api.call(new SlackMessage("#young-server","jhlee","대출자 중도상환배치 실패"));
    		throw new RuntimeException(t.getMessage());
    	}
    }

    // 11. 정산 및 연체내역 업데이트 스케줄러 (최종수정 V200715)
    @Transactional("oneTransactionManager")
    @Scheduled(cron = "0 5 0 * * *")    
//        @Scheduled(cron = "0 25 10 * * *")
    public void startOverdueUpdate() {														// 연체일에 대해서 일일계산해서 금액을 더함,,, 투자자의 연체비율을 나눔,, 세금이랑 수수료도 매일 다시 구함,, 연체히스토리 금액도 매일 쌓임,,,,
    	try {
    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) 
    		{
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

		    	        	System.out.println("loanId, payCount, String.valueOf(overdue), String.valueOf(overDueState))" + "	" + loanId + "	" + payCount
		    	        			 + "	" + String.valueOf((long) Math.floor(overdue)) + "	" + String.valueOf(overDueState));
																			// cpas_overdue_detail_history-일별정보입력
		    	        	repaymentService.insertOrverDueDetailHistory(loanId, payCount, String.valueOf(additionalRate), String.valueOf((long) Math.floor(overdue)), String.valueOf(overDueState));

		    	        	if(isOverDue == null)
    	    	        		repaymentService.insertOrverDueHistory(loanId, payCount);						// coh-연체 기록이 안되있으면 입력  
		    	        	
		    	        	////상환테이블 업데이트////																// crs-r_delq_amount와 r_delq_state정보 업데이트
		    	        	if(repaymentService.updateRepaymentScheduleOverDue(loanId, payCount, String.valueOf((long) Math.floor(overdue)), String.valueOf(overDueState))) {
	    	    	        	JSONObject jsonItem = new JSONObject();
	    	    	        	jsonItem.put("payCount", payCount);
	    	    	        	jsonItem.put("overDueState", overDueState);
	    	    	        	jsonItem.put("overDuePayment", String.valueOf((long) Math.floor(overdue)));
	    	    	        	jsonItem.put("overDuePayIncrement", String.valueOf(overDuePayment));
	    	    	        	jsonResult.put(jsonItem);
	        	        	} else 
	        	        		commonUtil.sendBatchLogging("startOverdueUpdate", "loanId : " + loanId + " payCount : " + payCount + " overDuePayment : " + overDuePayment + " overDueState : " + overDueState
	        	        				, "상환테이블 업데이트 에러"); 
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
								long overDue = (long) Math.ceil(Double.parseDouble(overDuePayment) * overDuePartRate);	// 소숫점 반올림, 200917 올림으로 변경
		    	        		
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
		    	    			repaymentService.insertOrverDueInvestHistory(loanId, mid, payCount, String.valueOf(overDue)
		    	    					, String.valueOf(tax_diff), String.valueOf(taxLocal_diff), "0", //String.valueOf(fee_diff), // 200424이후 플랫폼 이용수수료는 매일쌓이는 DB와 상관이 없어서 "0"처리  
		    	    					overDueStates);
		    	    			
		    	    			
		    	    																							// cps-연체정보 업데이트
		    	        		if(!repaymentService.updatePaymentScheduleOverDue(mid, loanId, payCount, String.valueOf(overDue), String.valueOf(tax_real)
		    	        				, String.valueOf(tax_local_real), String.valueOf((long)Math.floor(fee))))					// 200805 소숫점 정리 
		    	        			commonUtil.sendBatchLogging("startOverdueUpdate", "loanId : " + loanId + " payCount : " + payCount + " overDue : " + overDue, "지급테이블 업데이트 에러");
		    	        	}
		    	        }
		    		}
    			}
    			
    		}
    	} catch (Throwable t) {
    		//t.printStackTrace();
    		commonUtil.sendBatchLogging("startOverdueUpdate", "exception error!!", t.getMessage());
    		Slack.api.call(new SlackMessage("#young-server","jhlee","정산 및 연체내역 업데이트 배치 실패"));
    		
    		// 임시로 메일만 발송(추후 수정 예정)
    		List<OneSendCheckingEmail> oneSendCheckingEmailList = new ArrayList<OneSendCheckingEmail>();	// 메일 보내기 위한 리스트
    		OneSendCheckingEmail oneSendCheckingEmail = new OneSendCheckingEmail();
			oneSendCheckingEmail.setLoanId("론");
			oneSendCheckingEmailList.add(oneSendCheckingEmail);
			commonUtil.sendCheckManager(oneSendCheckingEmailList, "[긴급]연체정산에러!!");												// 메일발송 테이블에 쌓는 메서드 호출
    		
    		throw new RuntimeException(t.getMessage());
    	}
    }
    
    
    // 12. 대출정보 업데이트 스케줄러 (최종수정 V200715)
    // 현재 대출이 얼마나 이뤄젔고, 상환이 얼마나 이뤄졌고 등등의 정보를 매일입력(레포트 용이기 때문에 중요도 낮음)
    @Transactional("oneTransactionManager")
    @Scheduled(cron = "0 0 1 * * *")
//        @Scheduled(cron = "0 9 20  * * *")
    public void updateLoanState() {									
    	try {
//    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live"))
    		{
    			String loanStateOverduePayment = scheService.selectLoanStateOverduePayment();	// 30일이상 연체한 채권 원금의합
    			String loanStateDefaultPayment = scheService.selectLoanStateDefaultPayment();	//	90일 이상 연체한 
    			String loanStateTotalPayment = scheService.selectLoanStateTotalPayment();		// 상환하지 않은 모든 채권 원금의 합
    			String loanStateTotalRepayment = scheService.selectLoanStateTotalRepayment();
    			String loanStateTotalBalance = scheService.selectLoanStateTotalBalance();
    			String loanTotalLoanPay = scheService.selectTotalLoanPay();
    			String loanStateTotalPrincipal = scheService.selectLoanStateTotalPrincipal();
    			String loanSoldInformation = scheService.selectLoanSoldInformation();			// 매각채권에 대한 상환하지 않은 원금의 합 
    			
    																											//200511 운영팀 요청 일시 매각정보 제외
    			long loanTotalBalance = Long.parseLong(loanTotalLoanPay) - Long.parseLong(loanStateTotalPrincipal) - Long.parseLong(loanSoldInformation);
    			//long loanStateTotal = Long.parseLong(loanStateTotalRepayment) + Long.parseLong(loanStateTotalBalance);
    			double overdueRate = (Double.parseDouble(loanStateOverduePayment) / Double.parseDouble(loanStateTotalPayment)) * 100;
    			double defaultRate = (Double.parseDouble(loanStateDefaultPayment) / Double.parseDouble(loanStateTotalPayment)) * 100;
    			
    			// 1.누적대출액 : 대출한 금액의 총 합
    			// 2.누적상환액 : 상환한 회차의 원금
    			// 3.대출잔액 : 1번-2번
    			// 5.(30일이상 90일미만 연체한 채권 원금의합) / (상환하지 않은 모든 채권 원금의 합) * 100
    			
    			scheService.insertLoanStatus(loanTotalLoanPay, loanStateTotalPrincipal, String.valueOf(loanTotalBalance)
    					, "5.50", String.format("%.2f", overdueRate), String.format("%.2f", defaultRate));		// cpas_loan_status에 정보삽입
    		}
    	} catch (Throwable t) {
    		commonUtil.sendBatchLogging("updateLoanState", "exception error!!", t.getMessage());
    		throw new RuntimeException(t.getMessage());
    	}
    }					
    
    // 13. 연체정보 추가 스케줄러(연체 발생일부터 30일 지나면 KCB에 보고 해야함 거기에 보내주는 스케줄러) (최종수정 V200715)
    // cpas_overdue_payment에 연체정보 저장(어느시기에 연체가 얼마였는지까지, 또 상환하면 얼마를 상환했고 얼마나 연체된 상태인지 알수있음, 수정이 아닌 입력만 되있음)
    @Transactional("oneTransactionManager")
     @Scheduled(cron = "0 10 0 * * *")
//    @Scheduled(cron = "0 22 0 * * *")
    public void addOverduePayment() {													  
    	try {
    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live"))
    		{
    			List<String> overdueByLoanIds = scheService.selectOverdueByLoanId();	// crs-연체중이면서 cps-정산되지 않은 정보 모두 선택 
    			
    			Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String today = sdf.format(cal.getTime());
    			
				JSONArray jsonCreateData = new JSONArray();
				
    			for(int i = 0; i < overdueByLoanIds.size(); i++) {
    				String loanId = overdueByLoanIds.get(i);
    				JSONArray jsonResult = getDepositPayment(loanId, today);			// 얼마나 상환했는지등에 대한 정보 가지고 옴
    																					// 해당 채권에 상환하지 않은 최근회차 및 상환일, 월불입금+연체이자, 지난달  상환일 선택
    				List<OneOverdueRepaymentItem> oneOverdueRepaymentItem = scheService.selectOverdueRepaymentList(loanId, today);
    					
					for(int k = 0; k < oneOverdueRepaymentItem.size(); k++) {

						long totOverdue = 0;
    					long repayment = 0;
						
						for(int j = 0; j < jsonResult.length(); j++) {
	    					String clcRepayment = jsonResult.getJSONObject(j).getString("clcRepayment");
	    					String TrAmt = jsonResult.getJSONObject(j).getString("TrAmt");
	    					String ErpTransDt = jsonResult.getJSONObject(j).getString("ErpTransDt");
	    					
    						if(ErpTransDt.equals("0000-00-00")) {
    							if(k == 0)
    								totOverdue += Long.parseLong(oneOverdueRepaymentItem.get(k).getPayAmount()) - Long.parseLong(clcRepayment);
    							else
    								totOverdue += Long.parseLong(oneOverdueRepaymentItem.get(k).getPayAmount());
    							
    						} else {
	    						if(sdf.parse(ErpTransDt).getTime() >= sdf.parse(oneOverdueRepaymentItem.get(k).getPayDate()).getTime()
	    								&& sdf.parse(ErpTransDt).getTime() < sdf.parse(oneOverdueRepaymentItem.get(k).getPrevPayDate()).getTime()) {
	    							if(k == 0 && j == 0) {
	    								totOverdue += Long.parseLong(oneOverdueRepaymentItem.get(k).getPayAmount()) - Long.parseLong(clcRepayment);
	    								totOverdue += Long.parseLong(oneOverdueRepaymentItem.get(k).getPayAmount()) - Long.parseLong(TrAmt);
	    								
	    								if(totOverdue > 0) {
	    									repayment = Long.parseLong(oneOverdueRepaymentItem.get(k).getPayAmount()) - totOverdue;
	    								}
	    									
	    							} else {
	    								totOverdue += Long.parseLong(oneOverdueRepaymentItem.get(k).getPayAmount()) - Long.parseLong(TrAmt);
	    								
	    								if(totOverdue > 0) {
	    									repayment = Long.parseLong(oneOverdueRepaymentItem.get(k).getPayAmount()) - totOverdue;
	    								}
	    							}
	    							
	    						}
    						}
    						
    						JSONObject jsonItem = new JSONObject();
							jsonItem.put("loanId", loanId);
							jsonItem.put("Repayment", String.valueOf(repayment));
							jsonItem.put("Overdue", String.valueOf(totOverdue));
							jsonCreateData.put(jsonItem);
						
						}
						
    				}
    			}
    			
    			HashMap<String, String> mapOverdue = new HashMap<>();
    			
    			for(int i = 0; i < jsonCreateData.length(); i++) {
    				String loanId = jsonCreateData.getJSONObject(i).getString("loanId");
    				//String Repayment = jsonCreateData.getJSONObject(i).getString("Repayment");
    				String Overdue = jsonCreateData.getJSONObject(i).getString("Overdue");
    				
    				if(mapOverdue.containsKey(loanId)) 
    					mapOverdue.put(loanId, String.valueOf(Long.parseLong(mapOverdue.get(loanId)) + Long.parseLong(Overdue)));
    				else 
    					mapOverdue.put(loanId, Overdue);
    			}
    			
    			for(String key : mapOverdue.keySet()) {
    				String Overdue = scheService.selectOverdueByValues(key);
    				long repayment = 0;
    				
    				if(Overdue == null)
    					Overdue = "0";
    				
    				if(Long.parseLong(Overdue) > Long.parseLong(mapOverdue.get(key))) {
    					repayment = Long.parseLong(Overdue) - Long.parseLong(mapOverdue.get(key));
    					scheService.insertOverduePayment(key, mapOverdue.get(key), String.valueOf(repayment)); // cpas_overdue_payment에 연체정보(연체액, 남은잔액)입력
    				} else if(Long.parseLong(Overdue) < Long.parseLong(mapOverdue.get(key))) {
    					scheService.insertOverduePayment(key, mapOverdue.get(key), String.valueOf(repayment));
    				}
    			}
    			
    		}
    	} catch (Throwable t) {
    		commonUtil.sendBatchLogging("addOverduePayment", "exception error!!", t.getMessage());
    		throw new RuntimeException(t.getMessage());
    	}
    } 
    
    // 14. 신한웹훅 검증 스케줄러 (최종수정 V200715)
    // 신한 웹훅에 보낸 데이터를 확인하기 위해서 보낸 데이터를 cpas_lenddo_webhook_stack에도 삽입한후, 전송이 제대로 된것 확인하면 해당 데이터 삭제 
    // P2P 시스템을 위한게 아니라 인서트 되는 배치 필요해서 만든 스케줄러, 중요도 낮음
//    @Transactional("oneTransactionManager")
//    @Scheduled(cron = "0 0/1 * * * *")
//    public void sendShinhanWebhhok() {												 
//    	try {
//    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) {
//    			List<OneLenddoWebhookInfo> oneLenddoWebhookInfos = lenddoService.selectLenddoWebhoook();	// cpas_lenddo_webhook_stack 10개 선택
//
//    			RestTemplate restTemplate = new RestTemplate();
//    			
//    			for(int i = 0; i < oneLenddoWebhookInfos.size(); i++) {
//    				OneLenddoWebhookInfo oneLenddoWebhookInfo = oneLenddoWebhookInfos.get(i);
//    				
//    		        Map<String, String> vars = new HashMap<String, String>();
//    		        vars.put("app_id", oneLenddoWebhookInfo.getApp_id());
//    		        vars.put("created_dt", oneLenddoWebhookInfo.getCreated_dt());
//    		        
//    		        String result = restTemplate.postForObject(shinhanUrl, vars, String.class);				// shinhanUrl에 전문 보내서
//    		        
//    		        if(result != null && !result.isEmpty()) {
//    		        	try {
//	    		        	JSONObject jsonObject = new JSONObject(result);
//	    		        	if(jsonObject.has("status_code")) {
//	    		        		if(jsonObject.getInt("status_code") == 1000) {								// 상태코드가 1000이면 전송 잘 됬다고 판단한뒤
//	    		        			lenddoService.deleteLenddoWebhoook(oneLenddoWebhookInfo.getApp_id());	// 해당 데이터 삭제
//	    		        		}
//	    		        	}
//    		        	} catch(Exception e) {}
//    		        }
//    			}
//    		}
//    	} catch (Throwable t) {
//    		commonUtil.sendBatchLogging("sendShinhanWebhhok", "exception error!!", t.getMessage());
//    		throw new RuntimeException(t.getMessage());
//    	}
//    }
    
    // 15. 상환완료건 자동처리 스케줄러 (최종수정 V200715)
    //테스트 서버에 존재하지 않음
    @Transactional("oneTransactionManager")
    @Scheduled(cron = "0 5 19 * * MON-FRI")
    //@Scheduled(cron = "0 0/1 * * * *")
    //cps에서 상환처리 대출건인데 mip에서 완료(i_look=F)처리 되지 않은 정보들 완료(F)처리 하는 스케줄러
    public void updateRepayComplete() {
    	try {
    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) {
    			boolean updateRepayComplete = scheService.updateRepayComplete();
    			commonUtil.sendBatchLogging("updateRepayComplete", "updateRepayComplete : " + updateRepayComplete, "");
    		}
    	} catch (Throwable t) {
    		commonUtil.sendBatchLogging("sendShinhanWebhhok", "exception error!!", t.getMessage());
    		throw new RuntimeException(t.getMessage());
    	}
    }
   



    // 16. 잔액 업데이트(현재는 검수중..)
    @Transactional("oneTransactionManager")
    @Scheduled(cron = "0 10 0 * * *")    
    public void startEmoneyUpdate() {														// 연체일에 대해서 일일계산해서 금액을 더함,,, 투자자의 연체비율을 나눔,, 세금이랑 수수료도 매일 다시 구함,, 연체히스토리 금액도 매일 쌓임,,,,
    	try {
    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) 
    		{
//    			List<String> overdueLoanInfo = repaymentService.selectOverdueLoanInfo();					// cps-정산되지 않은 전체 채권 선택(p_pay_status=N)
//    			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    			RestTemplate restTemplate = new RestTemplate();
    			Map<String, String> vars = new HashMap<String, String>();
 		       
    			vars = new HashMap<String, String>();
		        vars.put("CUST_ID", "201910310001");
    			
    			String resultAMT = restTemplate.postForObject(insideUrl + "/lookup/customer", vars, String.class);
		        JSONObject jsonResult = new JSONObject(resultAMT);
		        
		        int STATE = jsonResult.getInt("STATE");
		        
		        if(STATE == 200) {
		        	String balanceAMT = ((JSONObject)jsonResult.get("RESULT")).getString("BALANCE_AMT");
		        	
		        	
		        	System.out.println(balanceAMT);
		        } else {
//		        	payment = (long)Long.parseLong(oneVirtualRealAccnt.getEmoney());
		        }
	        
    		}
    	} catch (Throwable t) {
    		//t.printStackTrace();
    		commonUtil.sendBatchLogging("startOverdueUpdate", "exception error!!", t.getMessage());
    		throw new RuntimeException(t.getMessage());
    	}
    }

    
    // 18. 투자자 세금, 지방세 검증	(최종수정 V200805)
    @Transactional("oneTransactionManager")
    @Scheduled(cron = "0 10 1 * * *")
    public void investTaxVerification() {	
    	try {
    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) 
    		{
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
	    	    			
	    			Calendar calendar = Calendar.getInstance();

	    			// 현재가 아닌 과거 데이터 검증이기 떄문에 	    			
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
	    	    			
	    	    			// 잔여투자금 구하는 함수
	    	    			double investBalance = investBalance(jsonRepaySchedules, Integer.parseInt(payCount));
	    	    			double fee = 0;

	    	    			/* 마지막 회차 끝전정리한 원금에 대한 수수료 부과해야함	7/1 적용예정	*/  
	    	    			if(!(jsonRepaySchedules.length() == j+1))
	    	    				fee = investBalance * loanRate * 0.024 * dayEOM/365;
	    	    			else
	    	    				fee = inAmount * 0.024 * dayEOM/365;
	    	    			
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
	    	    				
	    	    				
	    	    				if( (paymentScheduleItem.getTax() != tax_real_afterOverdue) || (paymentScheduleItem.getTaxLocal() != tax_local_real_afterOverdue) ) { 
				    	    				System.out.println("단순검증값 : 	" + onePaymentInvestSchedules.get(k).getMid() + "	" + loanId + "	" + payCount+ "	" + payDate+ "	" + paymentScheduleItem.getPayStatus()+ "	" + String.valueOf(Math.ceil(inAmount)) + "	" + 
				    	    				String.valueOf(interest) +"	" + paymentScheduleItem.getOverdue() + "	" +  (interest + paymentScheduleItem.getOverdue()) + "	" +
				    	    				String.valueOf(tax_real_afterOverdue) + "	" + tax_local_real_afterOverdue + "	" + String.valueOf(Math.floor(fee)) +
											"		DB	" + paymentScheduleItem.getInterest() +"	" + paymentScheduleItem.getOverdue() + "	" + paymentScheduleItem.getTax() + "	" + paymentScheduleItem.getTaxLocal() + "	" + paymentScheduleItem.getFee());
				    	    	    		
				    	    				OneSendCheckingEmail oneSendCheckingEmail = new OneSendCheckingEmail();
				    	    	    		oneSendCheckingEmail.setLoanId(loanId);	
				    	    	    		oneSendCheckingEmail.setRepayCount(payCount);
				    	    	    		oneSendCheckingEmail.setRepayDate(payDate);
				    	    	    		oneSendCheckingEmail.setPayStatus(paymentScheduleItem.getPayStatus());			// 상환유무
				    	    	    		oneSendCheckingEmail.setLnAmount(String.valueOf(Math.ceil(inAmount)));			// 원금
				    	    	    		oneSendCheckingEmail.setInterestAmount(String.valueOf(interest));				// 이자
				    	    	    		oneSendCheckingEmail.setOverDue(String.valueOf(paymentScheduleItem.getOverdue()));			// DB에 연체이자
				    	    	    		oneSendCheckingEmail.setTax_real_afterOverdue(String.valueOf(tax_real_afterOverdue));		// 연체가 적용된 세금
				    	    	    		oneSendCheckingEmail.setTax_local_real_afterOverdue(String.valueOf(tax_local_real_afterOverdue));	// 연체가 적용된 지방세금
				    	    	    		oneSendCheckingEmail.setTax_inDB(String.valueOf(paymentScheduleItem.getTax()));		// 연체가 적용된 세금
				    	    	    		oneSendCheckingEmail.setTaxLocal_inDB(String.valueOf(paymentScheduleItem.getTaxLocal()));	// 연체가 적용된 지방세금

				    	    	    		oneSendCheckingEmailList.add(oneSendCheckingEmail);

	    	    				}
	    	    				
	    	    			
	    	    			}
	    	    			paidAmountTmt += Math.ceil(inAmount);
	    	    		}
	    			}
		    		
		    		if(oneSendCheckingEmailList != null)
		    			commonUtil.sendCheckManager(oneSendCheckingEmailList,"[알림]세금검증에러");
	    		}

    			
//				commonUtil.sendBatchLogging("investTaxVerification", "200420", "업데이트가 성공적으로 완료되었습니다..");
    		}
    	} catch (Throwable t) {
    		commonUtil.sendBatchLogging("investTaxVerification", "exception error!!", t.getMessage());
    		throw new RuntimeException(t.getMessage());
    	}
    }


    
    
    
    
    // 17-1. 새로운 투자자 플랫폼 수수료 업데이트!!!
    @Transactional("oneTransactionManager")
//    @Scheduled(cron = "0 5 0 * * MON")
    //@Scheduled(cron = "30 0 9,16 * * MON-FRI")
    public void newFeeUpdate_200420() {	
    	try {
    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) 
    		{
    			// 전체 리스트를 가지고 와서
				List<String> allPaymentRenewInfo = scheService.selectAllPaymentInfo();

				for (int i = 0; i < allPaymentRenewInfo.size(); i++) {									// loan_id만큼 반복

					// 론아이디에서도 투자자 한명씩 불러오기			
					List<OnePaymentInvestSchedule> onePaymentInvestSchedules = scheService.selectPaymentInvestSchedule(allPaymentRenewInfo.get(i));
					
					for (int j = 0; j < onePaymentInvestSchedules.size(); j++) {						// 투자자 만큼 반복
						
						List<OnePaymentNewSchedule> paymentRenewInfo = scheService.selectPaymentInfo(allPaymentRenewInfo.get(i), onePaymentInvestSchedules.get(j).getMid());
						
						for (int k = 0; k < paymentRenewInfo.size(); k++) {								// 회차만큼 반복
							
							// 잔여투자금 구하는 함수
							long investBalance = scheService.selectNewFeeInfo(allPaymentRenewInfo.get(i), paymentRenewInfo.get(k).getMid(), paymentRenewInfo.get(k).getRepayCount());
		 	    			double fee = investBalance * 0.002;											// * 0.024
		
		 	    			OnePaymentNewInfo onePaymentNewInfo = new OnePaymentNewInfo();
		 	    			onePaymentNewInfo.setLoanId(allPaymentRenewInfo.get(i));
		 	    			onePaymentNewInfo.setFee((long) Math.floor(fee));
		 	    			onePaymentNewInfo.setRepayCount(paymentRenewInfo.get(k).getRepayCount()); 	    			
		 	    			onePaymentNewInfo.setMid(paymentRenewInfo.get(k).getMid());
		 	    			
			    			if(scheService.updateNewFeeInfo(onePaymentNewInfo)) {
	//		    				commonUtil.sendBatchLogging("updateNewFeeInfo", "loanId : " + loanId, "업데이트 완료, " + onePaymentNewInfo.getFee());
			    			} else {
	//		    				commonUtil.sendBatchLogging("addRepaymentSchedule", "loanId : " + loanId, "업데이트 실패, " + onePaymentNewInfo.getFee());
			    				break;
			    			}
		 	    			
//							System.out.println(paymentRenewInfo.get(k).getMid() + "	"
//									+ paymentRenewInfo.get(k).getLoanId() + "	"
//									+ paymentRenewInfo.get(k).getRepayCount() + "	"
//									+ paymentRenewInfo.get(k).getPayDate() + "	" + paymentRenewInfo.get(k).getPayGubun()
//									+ "	" + paymentRenewInfo.get(k).getPayStatus() + "	"
//									+ paymentRenewInfo.get(k).getLnAmount() + "	"
//									+ paymentRenewInfo.get(k).getInterestAmount() + "	"
//									+ paymentRenewInfo.get(k).getDelqAmount() + "	" + paymentRenewInfo.get(k).getTax()
//									+ "	" + paymentRenewInfo.get(k).getTaxLocal() + "	"
//									+ onePaymentNewInfo.getFee() + "	" + paymentRenewInfo.get(k).getPayAmount());
		
						}
//						System.out.println();
					}
				}

    			
				commonUtil.sendBatchLogging("newFeeUpdate", "200420", "업데이트가 성공적으로 완료되었습니다..");
    		}
    	} catch (Throwable t) {
    		commonUtil.sendBatchLogging("newFeeUpdate_200420", "exception error!!", t.getMessage());
    		throw new RuntimeException(t.getMessage());
    	}
    }

    // 17-2A. 새로운 투자자 플랫폼 수수료 업데이트!!!
    @Transactional("oneTransactionManager")
//    @Scheduled(cron = "0 30 11 * * *")
//    @Scheduled(cron = "30 0 9,16 * * MON-FRI")
    public void newFeeUpdate_200701A() {	
    	try {
//    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) 
    		{
    			// 전체 리스트를 가지고 와서
				List<String> allPaymentRenewInfo = scheService.selectAllPaymentInfo();

				for (int i = 0; i < allPaymentRenewInfo.size(); i++) {									// loan_id만큼 반복

					// 론아이디에서도 투자자 한명씩 불러오기			
					List<OnePaymentInvestSchedule> onePaymentInvestSchedules = scheService.selectPaymentInvestSchedule(allPaymentRenewInfo.get(i));
					
					for (int j = 0; j < onePaymentInvestSchedules.size(); j++) {						// 투자자 만큼 반복
						
						List<OnePaymentNewSchedule> paymentRenewInfo = scheService.selectPaymentInfo(allPaymentRenewInfo.get(i), onePaymentInvestSchedules.get(j).getMid());
						
						for (int k = 0; k < paymentRenewInfo.size(); k++) {								// 회차만큼 반복 
							  
							// 잔여투자금 구하는 함수
							long investBalance = scheService.selectNewFeeInfo(allPaymentRenewInfo.get(i), paymentRenewInfo.get(k).getMid(), paymentRenewInfo.get(k).getRepayCount());
		 	    			
							// 월수 구하기! 지난달+결제일(5,15,25)과 이번달+결제일(5,15,25) 날짜 차이 비교, 첫회차면 대출실행일, 마지막회차면 지난달+결제일(5,15,25)과 이번달 결제일
							
							long dayEOM;
							String repayCount = paymentRenewInfo.get(k).getRepayCount();
							String payDate = paymentRenewInfo.get(k).getPayDate();
							SimpleDateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd"); 
							Date datePayDate = sdf.parse(payDate);
							String PrePayDate;
							
							Calendar cal = Calendar.getInstance();
							cal.setTime(datePayDate);
							
							if(repayCount.equals("1")) {											// 1회차면
								
								dayEOM = getDiffOfDate(paymentRenewInfo.get(k).getExecDate(),paymentRenewInfo.get(k).getPayDate());
							}
							else if(k+1 == paymentRenewInfo.size()) {	// 마지막 회차면 지난 회차 정보 가져오기
								

//								cal.set(cal.DATE, Integer.parseInt(paymentRenewInfo.get(k).getRepayDay()));
								PrePayDate = scheService.selectLastCountRepaymentDate(allPaymentRenewInfo.get(i), paymentRenewInfo.get(k).getRepayCount());
								
								if(PrePayDate.substring(5, 7).equals(payDate.substring(5, 7))) {					// 마지막 전회차 월과 마지막 회차 월이 같으면 
									dayEOM = getDiffOfDate(PrePayDate,payDate);			
								}
								else {
									cal.add(cal.MONTH, -1);			
									cal.set(cal.DATE, Integer.parseInt(paymentRenewInfo.get(k).getRepayDay()));	// 몇일인지
									PrePayDate = sdf.format(cal.getTime());
									dayEOM = getDiffOfDate(PrePayDate,payDate);
								}
							}
							else {
								cal.add(cal.MONTH, -1);																// 지난달이
//								cal.set(cal.DAY_OF_MONTH, Integer.parseInt(paymentRenewInfo.get(k).getRepayDay()));	// 몇일인지
								// PrePayDate = ;
								dayEOM = cal.getActualMaximum(cal.DAY_OF_MONTH);// getDiffOfDate(PrePayDate,paymentRenewInfo.get(k).getPayDate());
							}

							// 이전 수수료 방식															
//							double fee = investBalance * 0.002;											// * 0.024
							double fee = investBalance * 0.024 * dayEOM/365;
							String fee_ori = paymentRenewInfo.get(k).getFee();
							
		 	    			OnePaymentNewInfo onePaymentNewInfo = new OnePaymentNewInfo();
		 	    			onePaymentNewInfo.setLoanId(allPaymentRenewInfo.get(i));
		 	    			onePaymentNewInfo.setFee((long) Math.floor(fee));
		 	    			onePaymentNewInfo.setRepayCount(paymentRenewInfo.get(k).getRepayCount()); 	    			
		 	    			onePaymentNewInfo.setMid(paymentRenewInfo.get(k).getMid());
		 	    			
//			    			if(scheService.updateNewFeeInfo(onePaymentNewInfo)) {
//			    				//commonUtil.sendBatchLogging("updateNewFeeInfo", "loanId : " + loanId, "업데이트 완료, " + onePaymentNewInfo.getFee());
//			    				System.out.println("성공 : " + paymentRenewInfo.get(k).getMid() + "	"									+ paymentRenewInfo.get(k).getLoanId() + "	"									+ paymentRenewInfo.get(k).getRepayCount() + "	"									+ paymentRenewInfo.get(k).getPayDate() + "	" + paymentRenewInfo.get(k).getPayGubun()									+ "	" + paymentRenewInfo.get(k).getPayStatus() + "	"									+ paymentRenewInfo.get(k).getLnAmount() + "	"									+ paymentRenewInfo.get(k).getInterestAmount() + "	"									+ paymentRenewInfo.get(k).getDelqAmount() + "	" + paymentRenewInfo.get(k).getTax()								+ "	" + paymentRenewInfo.get(k).getTaxLocal() + "	"									+ onePaymentNewInfo.getFee() + "	" + fee_ori + "	" + paymentRenewInfo.get(k).getPayAmount() + "	" + paymentRenewInfo.get(k).getRepayDay());								
//			    			
//			    			} else {
//			    				//commonUtil.sendBatchLogging("addRepaymentSchedule", "loanId : " + loanId, "업데이트 실패, " + onePaymentNewInfo.getFee());
//			    				System.out.println("실패 : " + paymentRenewInfo.get(k).getMid() + "	"									+ paymentRenewInfo.get(k).getLoanId() + "	"									+ paymentRenewInfo.get(k).getRepayCount() + "	"									+ paymentRenewInfo.get(k).getPayDate() + "	" + paymentRenewInfo.get(k).getPayGubun()									+ "	" + paymentRenewInfo.get(k).getPayStatus() + "	"									+ paymentRenewInfo.get(k).getLnAmount() + "	"									+ paymentRenewInfo.get(k).getInterestAmount() + "	"									+ paymentRenewInfo.get(k).getDelqAmount() + "	" + paymentRenewInfo.get(k).getTax()								+ "	" + paymentRenewInfo.get(k).getTaxLocal() + "	"									+ onePaymentNewInfo.getFee() + "	" + fee_ori + "	" + paymentRenewInfo.get(k).getPayAmount() + "	" + paymentRenewInfo.get(k).getRepayDay());								
//			    				break;
//			    			}
		 	    			
							 System.out.println(paymentRenewInfo.get(k).getMid() + "	"+ paymentRenewInfo.get(k).getLoanId() + "	"
							+ paymentRenewInfo.get(k).getRepayCount() + "	"
			    			+ paymentRenewInfo.get(k).getPayDate() + "	" + paymentRenewInfo.get(k).getPayGubun()
			    			+ "	" + paymentRenewInfo.get(k).getPayStatus() + "	" + paymentRenewInfo.get(k).getLnAmount() + "	"
			    			+ paymentRenewInfo.get(k).getInterestAmount() + "	"	
			    			+ paymentRenewInfo.get(k).getDelqAmount() + "	" + paymentRenewInfo.get(k).getTax()
			    			+ "	" + paymentRenewInfo.get(k).getTaxLocal() + "	"		
			    			+ onePaymentNewInfo.getFee() + "	" + fee_ori + "	" + paymentRenewInfo.get(k).getPayAmount()
			    			+ "	" + paymentRenewInfo.get(k).getRepayDay());
							 
						}
						//						System.out.println();
					}
				}

    			
				commonUtil.sendBatchLogging("newFeeUpdate", "200701", "업데이트가 성공적으로 완료되었습니다..");
    		}
    	} catch (Throwable t) {
    		commonUtil.sendBatchLogging("newFeeUpdate_200701", "exception error!!", t.getMessage());
    		throw new RuntimeException(t.getMessage());
    	}
    }


    // 17-2B. 새로운 투자자 플랫폼 수수료 업데이트!!!
    // 17-2A에서 첫회차 마지막회차에 문제 발생하여 첫회차 마지막회차만 선택해서 처리
    // 미혼모건은 수수료가 다르기 때문에 제외해야 함!!!
    @Transactional("oneTransactionManager")
//    @Scheduled(cron = "0 57 15 * * *")
//    @Scheduled(cron = "30 0 9,16 * * MON-FRI")
    public void newFeeUpdate_200701B() {	
    	try {
//    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) 
    		{
    			// 전체 리스트를 가지고 와서
				List<String> allPaymentRenewInfo = scheService.selectAllPaymentInfo();

				for (int i = 0; i < allPaymentRenewInfo.size(); i++) {									// loan_id만큼 반복

					// 론아이디에서도 투자자 한명씩 불러오기			
					List<OnePaymentInvestSchedule> onePaymentInvestSchedules = scheService.selectPaymentInvestSchedule(allPaymentRenewInfo.get(i));
					
					for (int j = 0; j < onePaymentInvestSchedules.size(); j++) {						// 투자자 만큼 반복
						
						List<OnePaymentNewSchedule> paymentRenewInfo = scheService.selectPaymentInfo(allPaymentRenewInfo.get(i), onePaymentInvestSchedules.get(j).getMid());
						
						for (int k = 0; k < paymentRenewInfo.size(); k++) {								// 회차만큼 반복 
							  
							// 잔여투자금 구하는 함수
							long investBalance = scheService.selectNewFeeInfo(allPaymentRenewInfo.get(i), paymentRenewInfo.get(k).getMid(), paymentRenewInfo.get(k).getRepayCount());
		 	    			
							// 월수 구하기! 지난달+결제일(5,15,25)과 이번달+결제일(5,15,25) 날짜 차이 비교, 첫회차면 대출실행일, 마지막회차면 지난달+결제일(5,15,25)과 이번달 결제일
							
							long dayEOM;
							String repayCount = paymentRenewInfo.get(k).getRepayCount();
							String payDate = paymentRenewInfo.get(k).getPayDate();
							SimpleDateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd"); 
							Date datePayDate = sdf.parse(payDate);
							String PrePayDate=null;
							
							Calendar cal = Calendar.getInstance();
							cal.setTime(datePayDate);
							
							if(repayCount.equals("1")) {	// 1회차면
								
								String calculatedRepayDate = paymentRenewInfo.get(k).getRepayDay();						// 5일이 상환이면 05로
								if (calculatedRepayDate.equals("5")) calculatedRepayDate = "0" + calculatedRepayDate;	// 붙여줌
								
								String calculatedPayDate = payDate.substring(0, 8) + calculatedRepayDate;
								dayEOM = getDiffOfDate(paymentRenewInfo.get(k).getExecDate(),calculatedPayDate);
								
//								System.out.println(paymentRenewInfo.get(k).getLoanId() + "	" + paymentRenewInfo.get(k).getExecDate() + "	" + investBalance);
							}
							else if(k+1 == paymentRenewInfo.size()) {	// 마지막 회차면 지난 회차 정보 가져오기
								

								PrePayDate = scheService.selectLastCountRepaymentDate(allPaymentRenewInfo.get(i), repayCount);

								String calculatedRepayDate = paymentRenewInfo.get(k).getRepayDay();						// 5일이 상환이면 05로
								if (calculatedRepayDate.equals("5")) calculatedRepayDate = "0" + calculatedRepayDate;	// 붙여줌
								String calculatedPrepayDate = PrePayDate.substring(0, 8) + calculatedRepayDate;
								
								
								if(calculatedPrepayDate.substring(5, 7).equals(payDate.substring(5, 7))) {					// 마지막 전회차 월과 마지막 회차 월이 같으면 
									dayEOM = getDiffOfDate(calculatedPrepayDate,payDate);			
								}
								else {

									dayEOM = getDiffOfDate(calculatedPrepayDate,payDate);
								}

							}
							
							else{
								// 첫회차와 마지막 회차에 대한 건만 처리
								continue;
//								cal.add(cal.MONTH, -1);																// 지난달이
//								cal.set(cal.DAY_OF_MONTH, Integer.parseInt(paymentRenewInfo.get(k).getRepayDay()));	// 몇일인지
//								 PrePayDate = ;
//								dayEOM = cal.getActualMaximum(cal.DAY_OF_MONTH);// getDiffOfDate(PrePayDate,paymentRenewInfo.get(k).getPayDate());
							}

							// 이전 수수료 방식															
//							double fee = investBalance * 0.002;											// * 0.024
							double fee = investBalance * 0.024 * dayEOM/365;
							String fee_ori = paymentRenewInfo.get(k).getFee();
							
		 	    			OnePaymentNewInfo onePaymentNewInfo = new OnePaymentNewInfo();
		 	    			onePaymentNewInfo.setLoanId(allPaymentRenewInfo.get(i));
		 	    			onePaymentNewInfo.setFee((long) Math.floor(fee));
		 	    			onePaymentNewInfo.setRepayCount(paymentRenewInfo.get(k).getRepayCount()); 	    			
		 	    			onePaymentNewInfo.setMid(paymentRenewInfo.get(k).getMid());
		 	    			
			    			if(scheService.updateNewFeeInfo(onePaymentNewInfo)) {
			    				//commonUtil.sendBatchLogging("updateNewFeeInfo", "loanId : " + loanId, "업데이트 완료, " + onePaymentNewInfo.getFee());
			    				System.out.println("성공 : " + paymentRenewInfo.get(k).getMid() + "	"									+ paymentRenewInfo.get(k).getLoanId() + "	"									+ paymentRenewInfo.get(k).getRepayCount() + "	"									+ paymentRenewInfo.get(k).getPayDate() + "	" + paymentRenewInfo.get(k).getPayGubun()									+ "	" + paymentRenewInfo.get(k).getPayStatus() + "	"									+ paymentRenewInfo.get(k).getLnAmount() + "	"									+ paymentRenewInfo.get(k).getInterestAmount() + "	"									+ paymentRenewInfo.get(k).getDelqAmount() + "	" + paymentRenewInfo.get(k).getTax()								+ "	" + paymentRenewInfo.get(k).getTaxLocal() + "	"									+ onePaymentNewInfo.getFee() + "	" + fee_ori + "	" + paymentRenewInfo.get(k).getPayAmount() + "	" + paymentRenewInfo.get(k).getRepayDay());								
			    			
			    			} else {
			    				//commonUtil.sendBatchLogging("addRepaymentSchedule", "loanId : " + loanId, "업데이트 실패, " + onePaymentNewInfo.getFee());
			    				System.out.println("실패 : " + paymentRenewInfo.get(k).getMid() + "	"									+ paymentRenewInfo.get(k).getLoanId() + "	"									+ paymentRenewInfo.get(k).getRepayCount() + "	"									+ paymentRenewInfo.get(k).getPayDate() + "	" + paymentRenewInfo.get(k).getPayGubun()									+ "	" + paymentRenewInfo.get(k).getPayStatus() + "	"									+ paymentRenewInfo.get(k).getLnAmount() + "	"									+ paymentRenewInfo.get(k).getInterestAmount() + "	"									+ paymentRenewInfo.get(k).getDelqAmount() + "	" + paymentRenewInfo.get(k).getTax()								+ "	" + paymentRenewInfo.get(k).getTaxLocal() + "	"									+ onePaymentNewInfo.getFee() + "	" + fee_ori + "	" + paymentRenewInfo.get(k).getPayAmount() + "	" + paymentRenewInfo.get(k).getRepayDay());								
			    				break;
			    			}
		 	    			
//							 System.out.println(paymentRenewInfo.get(k).getExecDate() + "	" + paymentRenewInfo.get(k).getMid() + "	"+ paymentRenewInfo.get(k).getLoanId() + "	"
//							+ paymentRenewInfo.get(k).getRepayCount() + "	" + PrePayDate + "		" 
//			    			+ paymentRenewInfo.get(k).getPayDate() + "	" + paymentRenewInfo.get(k).getPayGubun()
//			    			+ "	" + paymentRenewInfo.get(k).getPayStatus() + "	" + paymentRenewInfo.get(k).getLnAmount() + "	"
//			    			+ paymentRenewInfo.get(k).getInterestAmount() + "	"	
//			    			+ paymentRenewInfo.get(k).getDelqAmount() + "	" + paymentRenewInfo.get(k).getTax()
//			    			+ "	" + paymentRenewInfo.get(k).getTaxLocal() + "	"		
//			    			+ onePaymentNewInfo.getFee() + "	" + fee_ori + "	" + paymentRenewInfo.get(k).getPayAmount()
//			    			+ "	" + paymentRenewInfo.get(k).getRepayDay());
							 
						}
						//						System.out.println();
					}
				}

    			
				commonUtil.sendBatchLogging("newFeeUpdate", "200701", "업데이트가 성공적으로 완료되었습니다..");
    		}
    	} catch (Throwable t) {
    		commonUtil.sendBatchLogging("newFeeUpdate_200701", "exception error!!", t.getMessage());
    		throw new RuntimeException(t.getMessage());
    	}
    }


    
    
    
    private JSONArray getDepositPayment(String loanId, String payDate) {
    	JSONArray jsonResult = new JSONArray();
    	
		try {
			OneRepaymentDataInfo oneRePaymentDataInfo = repaymentService.selectRepaymentDataInfo(loanId);	// ml-해당 loanId, 대출기간(12,24), 이율, 상환계좌 선택
																											// IB_FB_P2P_IP-해당 상환계좌로 상환 총액, ERP전송일시 선택
			List<OneRepaymentDepositInfo> oneRepaymentDepositInfos = depositService.selectRepaymentListDepositAmt(oneRePaymentDataInfo.getLoanAccntNo());
			
			String totalRepayment = scheService.selectTotalRepayment(loanId);		// crs-해당 loanId의 상환 총액 선택 
			
			if(totalRepayment == null)
				totalRepayment = "0";
			
			long clcRepayment = Long.parseLong(totalRepayment); 
			
			for(int i = 0; i < oneRepaymentDepositInfos.size(); i++) {
				if(clcRepayment >= 0) {
					clcRepayment -= Double.parseDouble(oneRepaymentDepositInfos.get(i).getTrAmt());
					oneRepaymentDepositInfos.subList(i, i + 1).clear();
					i--;
				} else {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("clcRepayment", String.valueOf((clcRepayment * -1)));
					jsonObject.put("TrAmt", oneRepaymentDepositInfos.get(i).getTrAmt());
					jsonObject.put("ErpTransDt", oneRepaymentDepositInfos.get(i).getErpTransDt());
					jsonResult.put(jsonObject);
				}
			}
			
			if(jsonResult.length() < 1 && clcRepayment != 0) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("clcRepayment", String.valueOf((clcRepayment * -1)));
				jsonObject.put("TrAmt", "0");
				jsonObject.put("ErpTransDt", "0000-00-00");
				jsonResult.put(jsonObject);
			} else if(jsonResult.length() < 1 && clcRepayment == 0) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("clcRepayment", "0");
				jsonObject.put("TrAmt", "0");
				jsonObject.put("ErpTransDt", "0000-00-00");
				jsonResult.put(jsonObject);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return new JSONArray();
		}
    	
    	return jsonResult;
	}
    										//대출액, 연이율, 대출기간, 대출상환일(5,15,25), 상환시작일, 대출실행일, 상환-대출, 추가+1달여부
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
    
    //원리금균등상환공식-매월 납입금액 (rate:이율, nper:납입횟수, pmt:정기납입액, fv:미래가치, pv:현재가치, type:납입시점-초는1, 말은0)
    public double getPmt(double rate, double nper, double pv, double fv, int type) 	{
    	return rate == 0 ? (- pv - fv) / nper : (- fv * rate - pv * rate * Math.pow(1 + rate, nper)) / ((1 + rate * type) * (Math.pow(1 + rate, nper) - 1));
	}
    
    public boolean isLeapYear(int year) {
    	return year % 4 == 0 && year % 100 != 0 || year % 400 == 0;
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
    
    private String createPrePaymentInterest(double loanAmt, double interest, int loanPeriod, String startDate, String endDate) {
    	long diffDate = getDiffOfDate(startDate, endDate);
		long loanInterest = (long) Math.floor(loanAmt * interest * ((double)diffDate/365));
    	return String.valueOf((long)loanInterest);
    }
    
    //200501 createPrePaymentInterest 대신 사용
	private double getDelqAmt(double payAmt, double rate, double overRate, int daysOfYear, long diffDate) {
    	return payAmt * ((rate+overRate) / daysOfYear) * diffDate;
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
    
    private void addDepositInfo(List<InsideDepositInfo> insideDepositInfos) {
    	try {
	    	for(int i = 0; i < insideDepositInfos.size(); i++) {
	    		if(insideDepositInfos.get(i).getTrAmtGbn().equals("10"))
	    			insideDepositInfos.get(i).setTrAmtGbn("I");
	    		else if(insideDepositInfos.get(i).getTrAmtGbn().equals("20"))
	    			insideDepositInfos.get(i).setTrAmtGbn("L");
	    		
	    		boolean insertDeposit = scheService.insertDeposit(insideDepositInfos.get(i));
	    		
	    		if(!insertDeposit)
	    			commonUtil.sendBatchLogging("addDepositInfo", "insideDepositInfos => " + insideDepositInfos.get(i), "insertDeposit fail : " + insertDeposit);
//	    			System.out.println("addDepositInfo fail : " + insideDepositInfos.get(i));
	    	}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public OneInvestDetail setInvestDetail(String mid, String loan_id, String i_pay, String eMoneyCal, OneInvestTitle oneInvestTitle) throws Exception {
		oneMemberService.updateMemberMoney(String.valueOf(eMoneyCal), mid);							// 투자자 예치금 정보 수정
		
		OneInvestLoanDefault oneInvestLoanDefault = investService.selectInvestLoan(loan_id);		// 대출 정보선택
		
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
		oneInvestDetail.setIp("");
		
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
    
//    public void sendCheckManager(List<OneSendCheckingEmail> oneSendCheckingEmailList, String subject) {	
//		try {
//			
////				List<OneRepaymentManage> repaymentManageNomal = notifyScheService.selectRepaymentManageNomal();			// crs-연체가 아닌경우(r_delq_state=0)
////				List<OneRepaymentManage> repaymentManageOrverDue = notifyScheService.selectRepaymentManageOrverDue();	// crs-상환일자가 지났는데, 연체인 경우(r_delq_state=1)
////				List<OneRepaymentManage> repaymentManagePrePay = notifyScheService.selectRepaymentManagePrePay();		// crs-기한이익상실인경우(r_delq_state=2)
//		
////				String subject = "";
//				String emailBody = "";
//				OneSendCheckingEmail oneSendCheckingEmail = null;
//				
//				if(oneSendCheckingEmailList.size() > 0) {
//					emailBody += commonUtil.setHtmlTableFormHeader("상환", oneSendCheckingEmailList.size());
//					for(int i = 0; i < oneSendCheckingEmailList.size(); i++) {
//						oneSendCheckingEmail = oneSendCheckingEmailList.get(i);
//						emailBody += commonUtil.setHtmlTableFormBody(oneSendCheckingEmail.getLoanId(), oneSendCheckingEmail.getRepayCount(), oneSendCheckingEmail.getRepayDate(), oneSendCheckingEmail.getPayAmount(), oneSendCheckingEmail.getLnAmount(), oneSendCheckingEmail.getInterestAmount(), oneSendCheckingEmail.getBalance());
//						if(i != oneSendCheckingEmailList.size() - 1)
//							emailBody += "<br>";
//					}
//					emailBody += commonUtil.setHtmlTableFormFooter();
//				}
//				
//				
//				if(emailBody.length() > 0)
//					commonUtil.sendLoggingEmailTest(subject, emailBody, "jhlee@crepass.com");
//		} catch(Exception e) {
//			commonUtil.sendBatchLogging("sendRepaymentManager", "exception error!!", e.getMessage());
//    		throw new RuntimeException(e.getMessage());
//		}
//	}
	
	private double investBalance(JSONArray jsonRepaySchedules, int payCount) {
		
		double investBalance = 0;
		for(int i=payCount-1; i<jsonRepaySchedules.length(); i++) {
			JSONObject repaySchedules = jsonRepaySchedules.getJSONObject(i);
			
			investBalance += repaySchedules.getDouble("paidAmount");
		}
		
		return investBalance;
	}

}


