package com.crepass.restfulapi.one.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.Slack;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.cre.service.InvestP2pService;
import com.crepass.restfulapi.inside.domain.InsideIPJIInfo;
import com.crepass.restfulapi.inside.domain.OneRepaymentException;
import com.crepass.restfulapi.inside.service.DepositService;
import com.crepass.restfulapi.one.domain.OneEmoneyInvestPay;
import com.crepass.restfulapi.one.domain.OneLoanExcuteTotal;
import com.crepass.restfulapi.one.domain.OneNewMemberEventCnt;
import com.crepass.restfulapi.one.domain.OneNotiRepaymentUserInfo;
import com.crepass.restfulapi.one.domain.OneRepaymentCheckCount;
import com.crepass.restfulapi.one.domain.OneRepaymentDataInfo;
import com.crepass.restfulapi.one.domain.OneRepaymentManage;
import com.crepass.restfulapi.one.domain.OneRepaymentUserInfo;
import com.crepass.restfulapi.one.domain.OneSendEmailContents;
import com.crepass.restfulapi.one.domain.OneSendEmailInfo;
import com.crepass.restfulapi.one.domain.OneSendSMS;
import com.crepass.restfulapi.one.domain.OneSendSMSByCMID;
import com.crepass.restfulapi.one.domain.OneStartInvestUserInfo;
import com.crepass.restfulapi.one.service.BankAccntInfoService;
import com.crepass.restfulapi.one.service.EmoneyService;
import com.crepass.restfulapi.one.service.NotifyScheService;
import com.crepass.restfulapi.one.service.OneMemberService;
import com.crepass.restfulapi.one.service.RepaymentService;
import com.crepass.restfulapi.one.service.ScheService;

import net.gpedro.integrations.slack.SlackMessage;

@Component
public class NotifyScheController {

	@Autowired
	private NotifyScheService notifyScheService;
	
	@Autowired
    private DepositService depositService;
    
    @Autowired
    private RepaymentService repaymentService;
    
    @Autowired
    private EmoneyService emoneyService;
    
	@Autowired
    private ScheService scheService;
	
    @Autowired
    private InvestP2pService investP2pService;

    @Autowired
    private OneMemberService oneMemberService;

	@Autowired
    private BankAccntInfoService bankAccntInfoService;
    
	@Autowired
    private CommonUtil commonUtil;
	
//	@Value("${crepas.sms.api.key}")
//    private String smsApiKey;
//	
//	@Value("${crepas.sms.api.url}")
//    private String smsApiUrl;
	
	@Value("${email.host}")
    private String emailHost;
	
	@Value("${email.id}")
    private String emailId;
	
	@Value("${email.pw}")
    private String emailPw;
	
	@Value("${email.port}")
    private int emailPort;

	// 1. 정산처리 알림 스케줄러
	// 정산처리가 필요한 대출정보 관리자 이메일로 전송(from info@crepass.com to tech@crepass.com)
	@Transactional("oneTransactionManager")
    @Scheduled(cron = "0 0 9 * * *")					
//	@Scheduled(cron = "0 30 13 * * *")
    public void sendRepaymentManager() {	
		try {
			if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) {
				List<OneRepaymentManage> repaymentManageNomal = notifyScheService.selectRepaymentManageNomal();			// crs-연체가 아닌경우(r_delq_state=0)
				List<OneRepaymentManage> repaymentManageOrverDue = notifyScheService.selectRepaymentManageOrverDue();	// crs-상환일자가 지났는데, 연체인 경우(r_delq_state=1)
				List<OneRepaymentManage> repaymentManagePrePay = notifyScheService.selectRepaymentManagePrePay();		// crs-기한이익상실인경우(r_delq_state=2)
		
				String subject = "[청년5.5] 정산처리가 필요한 건이 있습니다.";
				String emailBody = "";
				OneRepaymentManage oneRepaymentManage = null;
				
				if(repaymentManageNomal.size() > 0) {
					emailBody += setHtmlTableFormHeader("상환", repaymentManageNomal.size());
					for(int i = 0; i < repaymentManageNomal.size(); i++) {
						oneRepaymentManage = repaymentManageNomal.get(i);
						emailBody += setHtmlTableFormBody(oneRepaymentManage.getLoanId(), oneRepaymentManage.getSubject(), oneRepaymentManage.getMid(), oneRepaymentManage.getName(), oneRepaymentManage.getLoanPay());
						if(i != repaymentManageNomal.size() - 1)
							emailBody += "<br>";
					}
					emailBody += setHtmlTableFormFooter();
				}
				
				if(repaymentManageOrverDue.size() > 0) {
					emailBody += setHtmlTableFormHeader("연체", repaymentManageOrverDue.size());
					for(int i = 0; i < repaymentManageOrverDue.size(); i++) {
						oneRepaymentManage = repaymentManageOrverDue.get(i);
						emailBody += setHtmlTableFormBody(oneRepaymentManage.getLoanId(), oneRepaymentManage.getSubject(), oneRepaymentManage.getMid(), oneRepaymentManage.getName(), oneRepaymentManage.getLoanPay());
						if(i != repaymentManageOrverDue.size() - 1)
							emailBody += "<br>";
					}
					emailBody += setHtmlTableFormFooter();
				}
				
				if(repaymentManagePrePay.size() > 0) {
					emailBody += setHtmlTableFormHeader("기한이익상실", repaymentManagePrePay.size());
					for(int i = 0; i < repaymentManagePrePay.size(); i++) {
						oneRepaymentManage = repaymentManagePrePay.get(i);
						emailBody += setHtmlTableFormBody(oneRepaymentManage.getLoanId(), oneRepaymentManage.getSubject(), oneRepaymentManage.getMid(), oneRepaymentManage.getName(), oneRepaymentManage.getLoanPay());
						if(i != repaymentManagePrePay.size() - 1)
							emailBody += "<br>";
					} 
					emailBody += setHtmlTableFormFooter();
				}
				
				if(emailBody.length() > 0)
					commonUtil.sendLoggingEmail2(subject, emailBody);
			}
		} catch(Exception e) {
			commonUtil.sendBatchLogging("sendRepaymentManager", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
		}
	}
	
	
	// 2. 상환 전문 정보 알림 스케줄러
	@Transactional("oneTransactionManager")
	@Scheduled(cron = "0 30 10,17 * * *")
// 	@Scheduled(cron = "0 0/1 * * * *")
    public void sendInSideBankManager() {	//상환배치가 9시 4시에 도는데 실패했는지 여부를 확인해서 실패 할 경우 테크로 메일보냄 
		try {
			if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) 
			{
				List<OneRepaymentException> oneRepaymentExceptions = depositService.selectRepaymentException();	// IB_FB_P2P_REPAY_REQ에서 처리 실패한 데이터(exec_status <> 02) 선택
				List<OneRepaymentException> oneRepaymentSuccess = depositService.selectRepaymentSuccess();		// IB_FB_P2P_REPAY_REQ에서 오늘 처리 성공한 데이터(exec_status = 02) 선택
		
				String emailBody = "";

				JSONObject jsonTitle = new JSONObject();
				jsonTitle.put("mailTitle", "전문");
				jsonTitle.put("titleType", "InSideBank 상환 에러 ");
				jsonTitle.put("titleMsg", oneRepaymentExceptions.size() + "건이 있습니다.");
				
				JSONArray jsonBody = new JSONArray();
				JSONObject body = new JSONObject();
				
				for(int i = 0; i < oneRepaymentExceptions.size(); i++) {
					body = new JSONObject();
					body.put("keyName", "날&nbsp;&nbsp;&nbsp;&nbsp;짜");
					body.put("keyValue", oneRepaymentExceptions.get(i).getSDate());
					jsonBody.put(body);
					
					body = new JSONObject();
					body.put("keyName", "시&nbsp;&nbsp;&nbsp;&nbsp;간");
					body.put("keyValue", oneRepaymentExceptions.get(i).getSTime());
					jsonBody.put(body);
					
					body = new JSONObject();
					body.put("keyName", "회&nbsp;&nbsp;&nbsp;&nbsp;차");
					body.put("keyValue", oneRepaymentExceptions.get(i).getRegSeq());
					jsonBody.put(body);
					
					body = new JSONObject();
					body.put("keyName", "상&nbsp;&nbsp;&nbsp;&nbsp;태");
					body.put("keyValue", oneRepaymentExceptions.get(i).getExecStatus());
					jsonBody.put(body);
					
					body = new JSONObject();
					body.put("keyName", "총정산금");
					body.put("keyValue", commonUtil.getAmountUnit2(Long.parseLong(oneRepaymentExceptions.get(i).getTotalTrAmt())));
					jsonBody.put(body);
					
					body = new JSONObject();
					body.put("keyName", "세&nbsp;&nbsp;&nbsp;&nbsp;금");
					body.put("keyValue", commonUtil.getAmountUnit2(Long.parseLong(oneRepaymentExceptions.get(i).getTotalCtaxAmt())));
					jsonBody.put(body);
					
					body = new JSONObject();
					body.put("keyName", "총수수료");
					body.put("keyValue", commonUtil.getAmountUnit2(Long.parseLong(oneRepaymentExceptions.get(i).getTotalFee())));
					jsonBody.put(body);
					
					body = new JSONObject();
					body.put("keyName", " ");
					body.put("keyValue", " ");
					jsonBody.put(body);
				}
				
				emailBody += commonUtil.setHtmlTableForm(jsonTitle, jsonBody);
				
				jsonTitle = new JSONObject();
				jsonTitle.put("mailTitle", "전문");
				jsonTitle.put("titleType", "InSideBank 상환 정상 ");
				jsonTitle.put("titleMsg", oneRepaymentSuccess.size() + "건이 있습니다.");
				
				jsonBody = new JSONArray();
				
				for(int i = 0; i < oneRepaymentSuccess.size(); i++) {
					body = new JSONObject();
					body.put("keyName", "날&nbsp;&nbsp;&nbsp;&nbsp;짜");
					body.put("keyValue", oneRepaymentSuccess.get(i).getSDate());
					jsonBody.put(body);
					
					body = new JSONObject();
					body.put("keyName", "시&nbsp;&nbsp;&nbsp;&nbsp;간");
					body.put("keyValue", oneRepaymentSuccess.get(i).getSTime());
					jsonBody.put(body);
					
					body = new JSONObject();
					body.put("keyName", "회&nbsp;&nbsp;&nbsp;&nbsp;차");
					body.put("keyValue", oneRepaymentSuccess.get(i).getRegSeq());
					jsonBody.put(body);
					
					body = new JSONObject();
					body.put("keyName", "상&nbsp;&nbsp;&nbsp;&nbsp;태");
					body.put("keyValue", oneRepaymentSuccess.get(i).getExecStatus());
					jsonBody.put(body);
					
					body = new JSONObject();
					body.put("keyName", "총정산금");
					body.put("keyValue", commonUtil.getAmountUnit2(Long.parseLong(oneRepaymentSuccess.get(i).getTotalTrAmt())));
					jsonBody.put(body);
					
					body = new JSONObject();
					body.put("keyName", "세&nbsp;&nbsp;&nbsp;&nbsp;금");
					body.put("keyValue", commonUtil.getAmountUnit2(Long.parseLong(oneRepaymentSuccess.get(i).getTotalCtaxAmt())));
					jsonBody.put(body);
					
					body = new JSONObject();
					body.put("keyName", "총수수료");
					body.put("keyValue", commonUtil.getAmountUnit2(Long.parseLong(oneRepaymentSuccess.get(i).getTotalFee())));
					jsonBody.put(body);
					
					body = new JSONObject();
					body.put("keyName", " ");
					body.put("keyValue", " ");
					jsonBody.put(body);
				}
				
				emailBody += commonUtil.setHtmlTableForm(jsonTitle, jsonBody);
				commonUtil.sendLoggingEmail2("[전문]InSideBank 상환 처리 결과", emailBody);
			}
		} catch(Exception e) {
			commonUtil.sendBatchLogging("sendInSideBankManager", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
		}
	}
	
	// 3. 내일 상환일인 대출자에게 알림메세지 추가하는 스케줄러
	@Transactional("oneTransactionManager")
    @Scheduled(cron = "0 0 9 * * *")
//	@Scheduled(cron = "0 20 10 * * *")
	public void addRepaymentBeforeLMS() {									// 상환일 하루 전에 미리 검사해서 알림메세지 보내는 스케줄러(금액에 대한 체크 포함), 총입금액에서 정산된 금액을 빼감 그러면 빼서 즉 0보다 크면 메일발송을 안함, 그런데 얘가 0보다 크면 메일발송이 있다, 즉 납부해야할 금액이 있다.(입금했는데 문자오는걸 막아 달라고 해서;) 그래서 여기는.. 메일도 가고 
		try {																// 배치가 N인거에 한해서.. 발송보낼 날짜를 미리정해서 그날 발송할 수 있음
			if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) {	// 비회원의 경우 새로 만들어야 함;
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String today = sdf.format(cal.getTime());
				
				if(!commonUtil.isHoliday(today)) {
					cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
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
																													//
																													// 연체자 제외, 상환 전날인 채권 호차에 대한 정보 선택, 파산(RBS)제외
					List<OneRepaymentUserInfo> oneRepaymentUserInfos = notifyScheService.selectRepaymentUserInfo(payDate);
					JSONObject jsonSmsData = null;
					
					for(int i = 0; i < oneRepaymentUserInfos.size(); i++) {
						String name = oneRepaymentUserInfos.get(i).getName();
						String loanId = oneRepaymentUserInfos.get(i).getLoanId();
	
						String[] repayDays = oneRepaymentUserInfos.get(i).getRepayDate().split("-");
						String[] payDates = oneRepaymentUserInfos.get(i).getPayDate().split("-");
																													// 해당 채권에 대한 원리금(원금+이자)+연체이자와 내일이후 회차정보들 선택
						List<OneRepaymentCheckCount> oneRepaymentCheckCounts = repaymentService.selectRepaymentCheckCount2(loanId, payDate);
						String depositPayment = getDepositPayment2(loanId, payDate, oneRepaymentCheckCounts);		// 이번달 결제금액(맞는데... 확인필요!)
						//long diffPay = Long.parseLong(oneRepaymentUserInfos.get(i).getPayAmount()) - Long.parseLong(depositPayment);
						long diffPay = Long.parseLong(depositPayment);
						
						if(diffPay > 0) {
							jsonSmsData = new JSONObject();
							jsonSmsData.put("name", name);
							jsonSmsData.put("payDate", payDates[payDates.length - 2] + "월" + payDates[payDates.length - 1] + "일");
							jsonSmsData.put("payWeek", commonUtil.getDayOfWeekWord(oneRepaymentUserInfos.get(i).getPayDate()));	//요일
							jsonSmsData.put("repayDate", repayDays[repayDays.length - 1]);
							jsonSmsData.put("payment", commonUtil.getAmountUnit3(diffPay));							//결제금액
							jsonSmsData.put("loanAccntNo", oneRepaymentUserInfos.get(i).getLoanAccntNo());
							
							String msg = commonUtil.getFormSMS(1, jsonSmsData);																	
							commonUtil.setRequestSMSData(name, "B", oneRepaymentUserInfos.get(i).getCustId(), oneRepaymentUserInfos.get(i).getHp(), msg);
						}
					}
				}
			}
			
		} catch(Exception e) {
			commonUtil.sendBatchLogging("addRepaymentBeforeLMS", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
		}
	}
	
	// 4. 오늘 상환일인 대출자에게 알림메세지 추가하는 스케줄러
	@Transactional("oneTransactionManager")
    @Scheduled(cron = "0 0 9 * * *")
//	  @Scheduled(cron = "0 20 10 * * *")
	public void addRepaymentTodayLMS() {
		try {
			if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) {
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String today = sdf.format(cal.getTime());
				
				if(!commonUtil.isHoliday(today)) {
					List<OneRepaymentUserInfo> oneRepaymentUserInfos = notifyScheService.selectRepaymentUserInfo(today);	// 오늘 상환해야 하는 대출건 선택, 파산(RBS)제외
					
					JSONObject jsonSmsData = null;
					
					for(int i = 0; i < oneRepaymentUserInfos.size(); i++) {
						String name = oneRepaymentUserInfos.get(i).getName();
						String loanId = oneRepaymentUserInfos.get(i).getLoanId();
	
						String[] repayDays = oneRepaymentUserInfos.get(i).getRepayDate().split("-");
						String[] payDates = oneRepaymentUserInfos.get(i).getPayDate().split("-");
																															// 현재시점까지의 투자자에 모든 원리금+연체금액의 합을 계산
						List<OneRepaymentCheckCount> oneRepaymentCheckCounts = repaymentService.selectRepaymentCheckCount2(loanId, today);
						String depositPayment = getDepositPayment2(loanId, today, oneRepaymentCheckCounts);					// 이번달 결제금액(맞는데... 확인필요!)
						//long diffPay = Long.parseLong(oneRepaymentUserInfos.get(i).getPayAmount()) - Long.parseLong(depositPayment);
						long diffPay = Long.parseLong(depositPayment);
						
						if(diffPay > 0) {
							jsonSmsData = new JSONObject();
							jsonSmsData.put("name", name);
							jsonSmsData.put("payDate", payDates[payDates.length - 2] + "월" + payDates[payDates.length - 1] + "일");
							jsonSmsData.put("payWeek", commonUtil.getDayOfWeekWord(oneRepaymentUserInfos.get(i).getPayDate()));
							jsonSmsData.put("repayDate", repayDays[repayDays.length - 1]);
							jsonSmsData.put("payment", commonUtil.getAmountUnit3(diffPay));
							jsonSmsData.put("loanAccntNo", oneRepaymentUserInfos.get(i).getLoanAccntNo());
							
							String msg = commonUtil.getFormSMS(2, jsonSmsData);						// 오늘이 결제일입니다.
							commonUtil.setRequestSMSData(name, "R", oneRepaymentUserInfos.get(i).getCustId(), oneRepaymentUserInfos.get(i).getHp(), msg);
						}
					}
				}
			}
			
		} catch(Exception e) {
			commonUtil.sendBatchLogging("addRepaymentTodayLMS", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
		}
	}

	// 5. 연체 대출자에게 알림메세지 추가하는 스케줄러
	// 1~5일, 기한이익상실시 문자알림
	@Transactional("oneTransactionManager")
    @Scheduled(cron = "0 0 9 * * *")
//	@Scheduled(cron = "0 33 10 * * *")
	public void addRepaymentOverdueLMS() { 
		try {
			if(commonUtil.getHostNameLinux().equals("p2p-bat01-live"))
			{
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String today = sdf.format(cal.getTime());
				
				if(!commonUtil.isHoliday(today)) {
					String[] typeCase = {"D", "E", "F", "P", "O"};
					
					for(int l = 0; l < typeCase.length; l++) {
						String type = typeCase[l];
						int typeSMS = 0;
						int overDueDays = 0;
						
						switch(type) {
							case "D" :
								overDueDays = 1;				// 1일 되었다는
								typeSMS = 5;					// 연체문자
								break;
								
							case "E" :
								overDueDays = 2;
								typeSMS = 5;
								break;
								
							case "F" :							// 왜 3일이면 F로 안되고 C(운영팀)으로 되는지 확인해야함
								overDueDays = 3;
								typeSMS = 5;
								break;
								
							case "P" :
								overDueDays = 4;
								typeSMS = 3;
								break;
								
							case "O" :
								overDueDays = 5;
								typeSMS = 4;
								break;
						}
														            // 상환되지 않은 오늘날짜 이후의 채권중에, 연체가 하나라도 있는 가장 최근 회차 선택
																	// 기한이익상실은 포함하지 않거나, 플래그(DEFPO)로 문자 보낸지 20일 안됬으면 제외(1~5일중 한개만 문자발송?)
						List<OneRepaymentUserInfo> oneRepaymentUserInfos = notifyScheService.selectRepaymentOrverdueUserInfo(today, type);
						JSONObject jsonSmsData = null;
						
						for(int i = 0; i < oneRepaymentUserInfos.size(); i++) {
							String name = oneRepaymentUserInfos.get(i).getName();
							String payDate = oneRepaymentUserInfos.get(i).getPayDate();
							String loanId = oneRepaymentUserInfos.get(i).getLoanId();
		
							for(int k = 0; k <= overDueDays; k++) {							// 연체일수만큼  
								String[] payDates = payDate.split("-");
								payDate = commonUtil.NextWorkingDayCalculate(sdf.format(cal.getTime()));
								
								while(payDate != null) {									// 날짜를 +1시켜줌(payDate가 today될때까지[연체1~5일 사이 구분하기위함])
									payDates = payDate.split("-");
									cal.set(Calendar.YEAR, Integer.parseInt(payDates[0]));
									cal.set(Calendar.MONTH, Integer.parseInt(payDates[1]) - 1);
									cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(payDates[2]));
									cal.setTimeInMillis(cal.getTimeInMillis());
									payDate = commonUtil.NextWorkingDayCalculate(sdf.format(cal.getTime()));
								}
								
								if(payDate == null) {
									if(k != overDueDays) {
										cal.set(Calendar.YEAR, Integer.parseInt(payDates[0]));
										cal.set(Calendar.MONTH, Integer.parseInt(payDates[1]) - 1);
										cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(payDates[2]) + 1);
										cal.setTimeInMillis(cal.getTimeInMillis());
									}
									
									payDate = sdf.format(cal.getTime());
								}
							}
							
							if(payDate.equals(today)) {
								List<OneRepaymentCheckCount> oneRepaymentCheckCounts = repaymentService.selectRepaymentCheckCount(loanId, payDate);
								String depositPayment = getDepositPayment(loanId, payDate, oneRepaymentCheckCounts);		// 현재 지불해야할 총금액
								
								//long diffPay = Long.parseLong(oneRepaymentUserInfos.get(i).getPayAmount()) - Long.parseLong(depositPayment);
								long diffPay = Long.parseLong(depositPayment);
								
								if(diffPay > 0) {
									jsonSmsData = new JSONObject();
									jsonSmsData.put("name", name);
									jsonSmsData.put("payment", commonUtil.getAmountUnit3(diffPay));
									jsonSmsData.put("loanAccntNo", oneRepaymentUserInfos.get(i).getLoanAccntNo());
									
									if(typeSMS == 5)
										jsonSmsData.put("overDueDays", String.valueOf(overDueDays));
									
									String msg = commonUtil.getFormSMS(typeSMS, jsonSmsData);
									commonUtil.setRequestSMSData(name, type, oneRepaymentUserInfos.get(i).getCustId(), oneRepaymentUserInfos.get(i).getHp(), msg);
								}
							}
						}
					}
				}
			}
			
		} catch(Exception e) {
			commonUtil.sendBatchLogging("addRepaymentOverdueLMS", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
		}
	}
	
	// 6. 대출 실행취소 알림 스케줄러
	// 펀딩이 안되서 대출이 깨진경우 투자자에게 대출취소를 알리는 스케줄러
	@Transactional("oneTransactionManager")
    @Scheduled(cron = "0 0/1 * * * *")
	public void cancelLoan() {																		
		try {
			if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) {
				List<OneStartInvestUserInfo> oneStartInvestUserInfos = notifyScheService.selectLoanCancelInvestUser();	// ml-오늘 취소된(i_loanapproval=C) 대출건 선택
				
				JSONObject jsonSmsData = new JSONObject();
				
				for(int i = 0; i < oneStartInvestUserInfos.size(); i++) {												// cpas_sms_history-취소된(use_flag=J) 대출자번호(cust_id) 선택
					String isSmsSendingInvestUser = notifyScheService.selectIsSmsSendingInvestUser(oneStartInvestUserInfos.get(i).getCustId(), oneStartInvestUserInfos.get(i).getSubject());
					
					if(isSmsSendingInvestUser == null || isSmsSendingInvestUser.isEmpty()) {							// 발송이력이 없으면
						jsonSmsData = new JSONObject();
            			jsonSmsData.put("title", oneStartInvestUserInfos.get(i).getSubject());
                		jsonSmsData.put("name", oneStartInvestUserInfos.get(i).getName());
                		jsonSmsData.put("payment", commonUtil.getAmountUnit3(Long.parseLong(oneStartInvestUserInfos.get(i).getInvestPay())));
						
						String msg = commonUtil.getFormSMS(11, jsonSmsData);											// 펀딩 모금 미완료 안내 및 환급 메세지
						commonUtil.setRequestSMSData(oneStartInvestUserInfos.get(i).getName(), "J", oneStartInvestUserInfos.get(i).getCustId(), oneStartInvestUserInfos.get(i).getHp(), msg);
					}
				}
			}
			
		} catch(Exception e) {
			commonUtil.sendBatchLogging("sendLMS", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
		}
	}
	
	// 7. 정산 가능한 건 알림 스케줄러 
	// 상환 가능한 건이 있음을 운영쪽에 알리는 메일
	@Transactional("oneTransactionManager")
    @Scheduled(cron = "0 30 8,12 * * *")
//	@Scheduled(cron = "0 33 16 * * *")
	public void sendingPosibleRepayment() {			
		try {
			if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) 
			{				
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
				
				// 주석 해제하면 0건인 경우에 메일 미발송
//				if(aryResult.length() > 0) {													// 메일 발송에 대한 프로세서
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
					commonUtil.sendLoggingEmail2("[알림]정산 가능한 건이 " + aryResult.length() + "건 있습니다.", emailBody);
				}
//			}
		} catch(Exception e) {
			commonUtil.sendBatchLogging("sendingPosibleRepayment", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
		}
	}
	
	
	
	// 8. 투자할건이 있는 상태에 대한 정보를 운영쪽에 보낼 이메일 입력하는 스케줄러
	// (현재) 행복나눔재단 계정에 한해서 운영
	@Transactional("oneTransactionManager")
    @Scheduled(cron = "0 10 9-17 * * *")
	public void sendingPosibleInvest() {									
		try {
			if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) {
				List<String> mids = new ArrayList<>();
				mids.add("si.table@skhappiness.org");
				
				JSONArray aryResult = new JSONArray();
				
				for(int i = 0; i < mids.size(); i++) {
					String mid = mids.get(i);

					OneEmoneyInvestPay oneEmoneyInvestPay = emoneyService.selectEmoneyPayInfo(mid);	// ml,mm-행복나눔재단이 투자한 건에 대출이 실행되지 않은 투자금액의 합 선택 
			        String withdrawPay = emoneyService.selectEmoneyWithdrawPay2(mid);				// cwt-행복나눔재단이 투자(type_flag=I)한 건에 출금이 이뤄지지 않은(trx_flag=N) 총액 선택
			        String emoneyBalance = emoneyService.selectEmoneyInvestBalance(mid);			// ctl-투자가능한 총액(입금총액-투자총액)
			        long setEmoney = 0;
			        long investPay = 0;
			        
			        if(oneEmoneyInvestPay != null) 													// 투자했는데 실행되지 않은 투자금이 있으면
			        	investPay = Long.parseLong(oneEmoneyInvestPay.getIpay());
			        setEmoney = Long.parseLong(emoneyBalance) - investPay - Long.parseLong(withdrawPay); // 투자 가능한 총액 - 실행 될 투자총액 - 출금 될(아직 이뤄지지 않은) 총액
			        
			        if(setEmoney >= 10000) {														// 투자 가능한 액수가 만원 이상이면
			        	List<String> subject = notifyScheService.selectPossibleInvest(mid);			// ml-투자가능건 선택, 대출 취소가 아닌건중에(ml.i_loanapproval <> 'C') 실행되지 않았고(i_exec_date=null), 투자진행중(mip.i_look=Y)인 
			        																				// 대출건에 투자한 투자율이 50% 이상인 정보들의 제목선택(mi.i_subject)
			        	for(int j = 0; j < subject.size(); j++) {
			        		JSONObject investItemInfo = new JSONObject();
			        		investItemInfo.put("title", subject.get(j));
							aryResult.put(investItemInfo);
			        	}
			        }
				}
				
				if(aryResult.length() > 0) {
					String emailBody = "";
					JSONObject jsonTitle = new JSONObject();
					jsonTitle.put("mailTitle", "행복나눔재단");
					jsonTitle.put("titleType", "투자 진행할 상품이 ");
					jsonTitle.put("titleMsg", aryResult.length() + "건이 있습니다.");
					
					JSONArray jsonBody = new JSONArray();
					JSONObject body = new JSONObject();
					
					for(int i = 0; i < aryResult.length(); i++) {
						JSONObject item = aryResult.getJSONObject(i);
						body = new JSONObject();
						body.put("keyName", "제&nbsp;&nbsp;&nbsp;&nbsp;목");
						body.put("keyValue", item.getString("title"));
						jsonBody.put(body);
					}
					
					emailBody += commonUtil.setHtmlTableForm(jsonTitle, jsonBody);
					commonUtil.sendLoggingEmail4("["+jsonTitle.getString("mailTitle")+"]" + jsonTitle.getString("titleType") + jsonTitle.getString("titleMsg"), emailBody);
				}
			}
		} catch(Exception e) {
			commonUtil.sendBatchLogging("sendingPosibleRepayment", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
		}
	}

	
//	// 9. 실제 문자 발송 스케줄러
//	@Transactional("oneTransactionManager")
//    @Scheduled(cron = "0/10 * * * * *")
//	public void sendLMS() {
//		try {
//			if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) {
//				List<OneSendSMS> oneSendSMS = notifyScheService.selectSendRepaymentSMS();	// cpas_sms_history-실행되지 않은(batch_flag=N) 데이터 5개(asc) 선택
//				
//				RestTemplate restTemplate = new RestTemplate();
//				
//				HttpHeaders header = new HttpHeaders();
//				header.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
//				header.add("x-waple-authorization", smsApiKey);
//				
//				HttpEntity<MultiValueMap<String, String>> request = null;
//				
//				JSONObject jsonResut = null; 
//				JSONObject jsonResult = null;
//				for(int i = 0; i < oneSendSMS.size(); i++) {
//					jsonResut = new JSONObject(oneSendSMS.get(i).getRequest());
//					
//					MultiValueMap<String, String> vars= new LinkedMultiValueMap<String, String>();
//			        vars.add("phone", jsonResut.getString("phone"));
//			        vars.add("callback", jsonResut.getString("callback"));
//			        vars.add("reqdate", jsonResut.getString("reqdate"));
//			        vars.add("msg", jsonResut.getString("msg"));
//			        vars.add("template_code", jsonResut.getString("template_code"));
//			        vars.add("failed_type", jsonResut.getString("failed_type"));
//			        vars.add("failed_subject", jsonResut.getString("failed_subject"));
//			        vars.add("failed_msg", jsonResut.getString("failed_msg"));
//			        vars.add("btn_types", jsonResut.getString("btn_types"));
//			        vars.add("btn_txts", jsonResut.getString("btn_txts"));
//			        vars.add("btn_urls1", jsonResut.getString("btn_urls1"));
//	
//			        request = new HttpEntity<MultiValueMap<String, String>>(vars, header);
//			        String result = restTemplate.postForObject(smsApiUrl + "/msg/crepass", request, String.class);
//			        
//			        jsonResult = new JSONObject(result);
//			        
//			        boolean isUpdateSendSMS = false;
//			        
//			        if(jsonResult.has("result_code")) {													// 처리했으면(결과값이 있으면)
//			        	String cmid = jsonResult.getString("cmid");									
//			        	
//			        	if(jsonResult.getString("result_code").equals("200"))							// 성공유무 업데이트
//			        		isUpdateSendSMS = notifyScheService.updateSendSMS(oneSendSMS.get(i).getCreateDt(), oneSendSMS.get(i).getName(), oneSendSMS.get(i).getType(), "S", jsonResult.toString(), cmid);
//			        	else
//			        		isUpdateSendSMS = notifyScheService.updateSendSMS(oneSendSMS.get(i).getCreateDt(), oneSendSMS.get(i).getName(), oneSendSMS.get(i).getType(), "F", jsonResult.toString(), cmid);
//			        } else 
//			        	isUpdateSendSMS = notifyScheService.updateSendSMS(oneSendSMS.get(i).getCreateDt(), oneSendSMS.get(i).getName(), oneSendSMS.get(i).getType(), "F", jsonResult.toString(), "");
//			        
//			        commonUtil.sendBatchLogging("sendLMS", "updateSendSMS", "state : " + isUpdateSendSMS + " getCreateDt : " + oneSendSMS.get(i).getCreateDt() + " getName : " + oneSendSMS.get(i).getName() + oneSendSMS.get(i).getCreateDt() + " result : " + jsonResult.toString());
//				}
//			}
//			
//		} catch(Exception e) {
//			commonUtil.sendBatchLogging("sendLMS", "exception error!!", e.getMessage());
//			Slack.api.call(new SlackMessage("#young-server","jhlee","문자발송 스케줄러 에러"));
//    		throw new RuntimeException(e.getMessage());
//		}
//	}
	
//	// 10. 결과 메세지 업데이트
//	@Transactional("oneTransactionManager")
//    @Scheduled(cron = "0 0/1 * * * *")
//	public void sendLMSResult() {											//결과 메세지 업데이트
//		try {
//			if(commonUtil.getHostNameLinux().equals("p2p-bat01-live"))
//			{
//				List<OneSendSMSByCMID> oneSendSMSByCMID = notifyScheService.selectSendSMSByCMID();	// cpas_sms_history-처리되지 않은 20개의 최근 데이터 선택
//																									// (cmid가 있고 결과가 없는, 그리고 생성된지 30초가 지난 데이터)
//				
//				RestTemplate restTemplate = new RestTemplate();
//				
//				HttpHeaders header = new HttpHeaders();
//				header.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
//				header.add("x-waple-authorization", smsApiKey);
//				
//				HttpEntity<MultiValueMap<String, String>> request = null;
//				JSONObject jsonResult = null;
//				
//				for(int i = 0; i < oneSendSMSByCMID.size(); i++) {
//					String createDt = oneSendSMSByCMID.get(i).getCreateDt();
//					String cmid = oneSendSMSByCMID.get(i).getCmid();
//					
//					MultiValueMap<String, String> vars = new LinkedMultiValueMap<String, String>();
//			        vars.add("cmid", cmid);
//	
//			        request = new HttpEntity<MultiValueMap<String, String>>(vars, header);
//			        String result = restTemplate.postForObject(smsApiUrl + "/report/crepass", request, String.class);
//			        
//			        jsonResult = new JSONObject(result);
//			        
//			        if(jsonResult.has("STATUS")) {
//					//int count = 0;
//					//while(jsonResult.getString("STATUS").equals("result is null")) {
//					//result = restTemplate.postForObject(smsApiUrl + "/report/crepass", request, String.class);
//					//Thread.sleep(500);
//					//			        		
//					//if(count >= 3)
//					//break;
//					//			        		
//					//count++;
//					//}
//			        	
//			        	if(!jsonResult.getString("STATUS").equals("result is null")) {
//				        	boolean isUpdateSendSMSResult = notifyScheService.updateSendSMSResult(createDt, cmid, result);
//					        commonUtil.sendBatchLogging("sendLMSResult", "updateSendSMS", "state : " + isUpdateSendSMSResult + " createDt : " + createDt + " cmid : " + cmid + " result : " + result);
//			        	}
//			        }
//				}
//			}
//		} catch(Exception e) {
//			commonUtil.sendBatchLogging("sendLMSResult", "exception error!!", e.getMessage());
//			Slack.api.call(new SlackMessage("#young-server","jhlee","문자발송 업데이트 스케줄러 에러"));
//    		throw new RuntimeException(e.getMessage());
//		}
//	}
	
	// 11. 이메일 발송 스케줄러
	@Transactional("oneTransactionManager")
    @Scheduled(cron = "0/10 * * * * *")
	public void sendEmail() {
		
		// 예외 발생시 catch로 보내서 로그 확인용
        OneSendEmailInfo oneSendEmailInfoToCatch = new OneSendEmailInfo();
        
		try {
			if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) 
			{
				List<OneSendEmailInfo> oneSendEmailInfo = notifyScheService.selectSendEmailInfo();	// cpas_email_target-발송되지 않은 100건 선택
				
				
				if(oneSendEmailInfo != null && oneSendEmailInfo.size() > 0) {
					long emailFK = oneSendEmailInfo.get(0).getEmailFk();
					String from = oneSendEmailInfo.get(0).getFrom();
					OneSendEmailContents oneSendEmailContents = notifyScheService.selectSendEmailContents(emailFK);	// cpas_email_history-email_fk와 메일내용(제목,내용) 선택
					
					Properties props = System.getProperties();
			        props.put("mail.smtp.host", emailHost);
			        props.put("mail.smtp.port", emailPort);
			        props.put("mail.smtp.auth", "true");
			        props.put("mail.smtp.ssl.enable", "true");
			        props.put("mail.smtp.ssl.trust", emailHost);
			        
			        Session session = Session.getDefaultInstance(props, new Authenticator() {
			        	protected PasswordAuthentication getPasswordAuthentication() {
			        		return new PasswordAuthentication(emailId, emailPw); 
			    		}
			    	});
			        //session.setDebug(true);
			        
			        
					for(int i = 0; i < oneSendEmailInfo.size(); i++) {
						OneSendEmailInfo onSendEmailInfo = oneSendEmailInfo.get(i);
						
						oneSendEmailInfoToCatch = onSendEmailInfo;
						
						InternetAddress recipient = new InternetAddress(onSendEmailInfo.getTo());
						MimeMessage mimeMessage = new MimeMessage(session) {
							protected void updateMessageID() throws javax.mail.MessagingException {
								if (getHeader("Message-ID") == null)
				                    super.updateMessageID();
							}
						};
						
						Multipart multipart = new MimeMultipart();
						
						MimeBodyPart message = new MimeBodyPart();
						message.setContent(oneSendEmailContents.getEmailBody(), "text/html; charset=utf-8");
						multipart.addBodyPart(message);

						//첨부파일
//						if(oneSendEmailContents.getFileFk() != null && !oneSendEmailContents.getFileFk().isEmpty()) {
//							List<String> fileNames = notifyScheService.selectEmailFileName(Long.parseLong(oneSendEmailContents.getFileFk()));	// 해당 email_fk관련 파일명 선택
//							
//							MimeBodyPart fileBodyPart = null;
//							FileDataSource fds = null;
//							
//							for(int j = 0; j < fileNames.size(); j++) {
//								fileBodyPart = new MimeBodyPart();
//								fds = new FileDataSource(new File("/shared/email/"+oneSendEmailContents.getFileFk()+"/"+fileNames.get(j)).getAbsolutePath());
//								fileBodyPart.setDataHandler(new DataHandler(fds));
//								fileBodyPart.setFileName(fds.getName());
//								multipart.addBodyPart(fileBodyPart);
//							}
//						}
						
				        mimeMessage.setFrom(new InternetAddress(from));
				        mimeMessage.addRecipient(Message.RecipientType.TO, recipient);
				        mimeMessage.setSubject(oneSendEmailContents.getEmailTitle());
				        mimeMessage.setContent(multipart, "text/html; charset=utf-8");
				        mimeMessage.saveChanges();
				        Transport.send(mimeMessage);
				        
				        if(!notifyScheService.updateSendEmailState(onSendEmailInfo)) {	// cpas_email_target-전송 성공으로 표시(N=>Y)
				        	System.out.println("mail update fail : " + onSendEmailInfo);
				        	
				        	//메일 발송 실패시, 실패 테이블에 입력해서 이후에 발송 안되게 처리
				        	// boolean insertSendEmailFail = ;
//				        	if(!notifyScheService.insertSendEmailFail(onSendEmailInfo)) {
//				        		oneSendEmailInfoToCatch.setResult("Update Error");
//								notifyScheService.insertSendEmailFail(oneSendEmailInfoToCatch);
//				        		System.out.println("Update Error : " + onSendEmailInfo.toString());
//				        	}
				        }
					}
				}
			}
		} catch(Exception e) {
				
			commonUtil.sendBatchLogging("sendEmail", "exception error!!", e.getMessage());
			Slack.api.call(new SlackMessage("#young-server","jhlee","이메일 발송 스케줄러 에러"));
//				try {
//
//					oneSendEmailInfoToCatch.setResult("Exeption Error");
//					notifyScheService.insertSendEmailFail(oneSendEmailInfoToCatch);
//					System.out.println("Exeption Error : " + oneSendEmailInfoToCatch.toString());
					
//				} catch (Exception e1) {
//					e1.printStackTrace();
//				}
//			
			
    		throw new RuntimeException(e.getMessage());
		}
	}

	
	// 12. 지난 7일간 회원가입, 대출신청 건수정보 메일로 발송테이블에 입력
	@Transactional("oneTransactionManager")
	@Scheduled(cron = "0 30 8 * * MON-FRI")
	public void newMemberEventCntFor7Days() {
		try {
			if(commonUtil.getHostNameLinux().equals("p2p-bat01-live"))
			{
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_MONTH, -7);
				Date date = cal.getTime();
				
				String aWeekAgo = new SimpleDateFormat("yyyy-MM-dd").format(date);
				
				cal.add(Calendar.DAY_OF_MONTH, +6);
				date = cal.getTime();
				String aDayAgo = new SimpleDateFormat("yyyy-MM-dd").format(date);
				
				// 지난 일주일간 가입한 회원수 선택		OneNewMemberTotalForLastWeek
				// 일주일동안 대출신청 건수				OneApplyingLoanTotalForLastWeek
				// 일주일동안 실행된 대출신청 건수		OneExcutingLoanTotalForLastWeek
				OneNewMemberEventCnt oneNewMemberEventCnt = notifyScheService.selectNewMemberEventCnt();
				
				String subject = "[청년5.5] 지난주 가입자수, 대출신청수 현황 ["+ aWeekAgo +" ~ "+ aDayAgo + "]";
				
				String emailBody = "<table width=\"696\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\"><tbody><tr><td style=\"height:4px;font-size:0;line-height:0\">&nbsp;</td></tr> " +
						"<tr><td style=\"height:3px;border-top:3px solid #554f4c;font-size:0;line-height:0\">&nbsp;</td></tr> " +  
						"<tr><td style=\"height:25px;font-size:0;line-height:0\">&nbsp;</td></tr> " +
						"<tr><td style=\"font-family:돋움,dotum;font-size:16px;font-weight:bold\"><span><font style=\"color:#559fc6;\"> 지난 일주일간 가입한 회원수는 </font> " + oneNewMemberEventCnt.getNewMemberTotal() + "건이 있습니다.</span></td></tr> " +
						"<tr><td style=\"height:25px;font-size:0;line-height:0\">&nbsp;</td></tr></tbody></table> " +

						"<table width=\"696\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\"><tbody><tr><td style=\"height:4px;font-size:0;line-height:0\">&nbsp;</td></tr> " +
						"<tr><td style=\"height:3px;border-top:3px solid #554f4c;font-size:0;line-height:0\">&nbsp;</td></tr> " +
						"<tr><td style=\"height:25px;font-size:0;line-height:0\">&nbsp;</td></tr> "+
						"<tr><td style=\"font-family:돋움,dotum;font-size:16px;font-weight:bold\"><span><font style=\"color:#559fc6;\"> 일주일동안 대출신청 건수	 </font> " + oneNewMemberEventCnt.getLoanTotal() + "건이 있습니다.</span></td></tr> " +
						"<tr><td style=\"height:25px;font-size:0;line-height:0\">&nbsp;</td></tr></tbody></table> "+

						"<table width=\"696\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\"><tbody><tr><td style=\"height:4px;font-size:0;line-height:0\">&nbsp;</td></tr> " +
						"<tr><td style=\"height:3px;border-top:3px solid #554f4c;font-size:0;line-height:0\">&nbsp;</td></tr> "+
						"<tr><td style=\"height:25px;font-size:0;line-height:0\">&nbsp;</td></tr> "+
						"<tr><td style=\"font-family:돋움,dotum;font-size:16px;font-weight:bold\"><span><font style=\"color:#559fc6;\"> 일주일동안 실행된 대출신청 건수 </font> " + oneNewMemberEventCnt.getLoanExecTotal() + "건이 있습니다.</span></td></tr> " +
						"<tr><td style=\"height:25px;font-size:0;line-height:0\">&nbsp;</td></tr>" +
						"<tr><td style=\"height:3px;border-top:3px solid #554f4c;font-size:0;line-height:0\">&nbsp;</td></tr> "+
						"</tbody></table>";
						

				if(emailBody.length() > 0)
					commonUtil.sendLoggingEmailTech(subject, emailBody);
				
			}
			
		} catch(Exception e) {
			commonUtil.sendBatchLogging("newMemberEventCntForLastWeek", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
		}
	}

	
	// 13. 지난 7일간 대출실행건 검수 결과
	@Transactional("oneTransactionManager")
	//@Scheduled(cron = "0 30 8 * * MON-FRI")
	// @Scheduled(cron = "0 0/5 * * * TUE")
	public void checkManagerLoanExcutingFor7Days() {
		try {
			if(commonUtil.getHostNameLinux().equals("p2p-bat01-live"))
			{
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_MONTH, -7);
				Date date = cal.getTime();
				
				String aWeekAgo = new SimpleDateFormat("yyyy-MM-dd").format(date);
				
				cal.add(Calendar.DAY_OF_MONTH, +5);
				date = cal.getTime();
				String aDayAgo = new SimpleDateFormat("yyyy-MM-dd").format(date);
				
				// 일주일동안 실행된 대출신청 건수		OneLoanExcuteTotalForLastWeek
				List<OneLoanExcuteTotal> OneLoanExcuteTotalForLastWeek = notifyScheService.selectLoanExcuteTotalFor7Days();
				
				
				String subject = "[청년5.5] 지난 7일간 대출실행건 검수 결과 ["+ aWeekAgo +" ~ "+ aDayAgo + "]";
				
//				String emailBody = "<table width=\"696\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\"><tbody><tr><td style=\"height:4px;font-size:0;line-height:0\">&nbsp;</td></tr> " +
//						"<tr><td style=\"height:3px;border-top:3px solid #554f4c;font-size:0;line-height:0\">&nbsp;</td></tr> " +  
//						"<tr><td style=\"height:25px;font-size:0;line-height:0\">&nbsp;</td></tr> " +
//						"<tr><td style=\"font-family:돋움,dotum;font-size:16px;font-weight:bold\"><span><font style=\"color:#559fc6;\"> 지난 일주일간 가입한 회원수는 </font> " + oneNewMemberEventCnt.getNewMemberTotal() + "건이 있습니다.</span></td></tr> " +
//						"<tr><td style=\"height:25px;font-size:0;line-height:0\">&nbsp;</td></tr></tbody></table> " +
//
//						"<table width=\"696\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\"><tbody><tr><td style=\"height:4px;font-size:0;line-height:0\">&nbsp;</td></tr> " +
//						"<tr><td style=\"height:3px;border-top:3px solid #554f4c;font-size:0;line-height:0\">&nbsp;</td></tr> " +
//						"<tr><td style=\"height:25px;font-size:0;line-height:0\">&nbsp;</td></tr> "+
//						"<tr><td style=\"font-family:돋움,dotum;font-size:16px;font-weight:bold\"><span><font style=\"color:#559fc6;\"> 일주일동안 대출신청 건수	 </font> " + oneNewMemberEventCnt.getLoanTotal() + "건이 있습니다.</span></td></tr> " +
//						"<tr><td style=\"height:25px;font-size:0;line-height:0\">&nbsp;</td></tr></tbody></table> "+
//
//						"<table width=\"696\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\"><tbody><tr><td style=\"height:4px;font-size:0;line-height:0\">&nbsp;</td></tr> " +
//						"<tr><td style=\"height:3px;border-top:3px solid #554f4c;font-size:0;line-height:0\">&nbsp;</td></tr> "+
//						"<tr><td style=\"height:25px;font-size:0;line-height:0\">&nbsp;</td></tr> "+
//						"<tr><td style=\"font-family:돋움,dotum;font-size:16px;font-weight:bold\"><span><font style=\"color:#559fc6;\"> 일주일동안 실행된 대출신청 건수 </font> " + oneNewMemberEventCnt.getLoanExecTotal() + "건이 있습니다.</span></td></tr> " +
//						"<tr><td style=\"height:25px;font-size:0;line-height:0\">&nbsp;</td></tr>" +
//						"<tr><td style=\"height:3px;border-top:3px solid #554f4c;font-size:0;line-height:0\">&nbsp;</td></tr> "+
//						"</tbody></table>";
//						
//
//				if(emailBody.length() > 0)
//					commonUtil.sendLoggingEmailTest(subject, emailBody);
//				
			}
			
		} catch(Exception e) {
			commonUtil.sendBatchLogging("newMemberEventCntForLastWeek", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
		}
	}

	// 14. 지난 7일간 투자자 총입출금
		@Transactional("oneTransactionManager")
		@Scheduled(cron = "0 30 8 * * MON-FRI")
//		@Scheduled(cron = "0 29 14 * * MON-FRI")
		public void weekBalanceCheck() {
			try {
				if(commonUtil.getHostNameLinux().equals("p2p-bat01-live"))
				{
					
					 List<String> allInvestorsList = scheService.selectAllInvestorsList();
				        
				        int cntTrue=0;	// 검증값 이상없으면 카운트 
				        int cntFalse=0;	// 검증값 이상 있으면 카운트
				        
				        // 어제날짜와 일주일전 날짜 구하기
						SimpleDateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd");    
				        Date dateInit = new Date();
				        String today =  sdf.format(dateInit);
				        Date dateToday = sdf.parse(today);			// Cal에 넣으려면 어쩔수 없이 Date로 변경해야함;
				        
				        Calendar calYesterday = Calendar.getInstance();
				        Calendar calWeekAgo = Calendar.getInstance();
				        
				        calYesterday.setTime(dateToday);
				        calWeekAgo.setTime(dateToday);
				        calYesterday.add(Calendar.DATE, -1);
				        calWeekAgo.add(Calendar.DATE, -7);
				        String yesterday = sdf.format(calYesterday.getTime()); 
				        String weekAgo = sdf.format(calWeekAgo.getTime());
				        
				        SimpleDateFormat  sdfNonUnderline = new SimpleDateFormat("yyyyMMdd");    
				    
				        String emailBody = null;
				        
				        for(int i=0; i<allInvestorsList.size(); i++) {
					        String mid = allInvestorsList.get(i);
					        String custId = oneMemberService.selectCustID(mid);
					        
					        List<InsideIPJIInfo> insideIPJIInfo = depositService.selectInsideIPJIInfo(custId);
					        
					        // 제외목록 : 	InsidebankController에 balanceList메서드 제외목록 참고
					        List<InsideIPJIInfo> p2pInvestInfo = investP2pService.selectP2pInvestInfo(custId);
					        
					        // 두개의 DB가 다르기 때문에 합쳐서 정렬해야함
					        List<InsideIPJIInfo> totalInfoList = new ArrayList<InsideIPJIInfo>();
					        totalInfoList.addAll(insideIPJIInfo);
					        totalInfoList.addAll(p2pInvestInfo);
					        
					        Collections.sort(totalInfoList);
					        
					        long dailySumIip=0;		// 투자자입금(타행)
					        // long dailySumLip=0;		// 대출자상환금
					        long dailySumIji=0;		// 투자자투자금
					        long dailySumIve=0;		// 투자자출금(타행)
					        long dailySumWve=0;		// 대출자 입금(타행)
					
					        
				            for(int j=0; j<totalInfoList.size(); j++) {					
				                													
				               	Date targetDate =  sdfNonUnderline.parse( totalInfoList.get(j).getPaidDate().substring(0, 8));
				               	String targetStr = sdf.format(targetDate);
				               	
				               	if( getBetweenWeekAgoAndYesterday(weekAgo, yesterday, targetStr) ){		//최근 일주일간 거래내역이면
				                
						          	if(totalInfoList.get(j).getAmtGbn().equals("Iip")) {
						          		dailySumIip += totalInfoList.get(j).getTrAmt();
						          		
						          	}
									//	          	else if(totalInfoList.get(j).getAmtGbn().equals("Lip")) {
									//	          		dailySumLip += totalInfoList.get(j).getTrAmt();
									//	          		System.out.println("대출자 입금(타행)	" + totalInfoList.get(j).getPaidDate() + "					" + totalInfoList.get(j).getTrAmt());
									//	          		
									//	          	}
						          	else if(totalInfoList.get(j).getAmtGbn().equals("Iji")) {
						          		dailySumIji += totalInfoList.get(j).getTrAmt();
						          	} else if(totalInfoList.get(j).getAmtGbn().equals("Ive")) {
						          		dailySumIve += totalInfoList.get(j).getTrAmt();
						          	} else if(totalInfoList.get(j).getAmtGbn().equals("Wve")) {
						          		dailySumWve += totalInfoList.get(j).getTrAmt();
						          	}
						      	}
				            }

							// 어제 입출금 내역에대한 ctl과 프로그램 검증값 비교
							long dailyTrxTmt = scheService.selectForAWeekSumDW(mid);
						  	// 투자자입금(타행)-Iip	(대출자로부터)투자자지급금-Iji		투자자투자금-Ive	투자자타행출금-Wve
					        long dailyInsideTmt = dailySumIip+dailySumIji-dailySumIve-dailySumWve;
					        String trxTotalAmt = bankAccntInfoService.selectBankAccntBalance(mid); 
					        
//					        if (i==0)
//					        	System.out.println("지난 일주일간	m_id	어제 trx입출금 내역	검증값");
					        
					        if (dailyTrxTmt == dailyInsideTmt)
					        	cntTrue++;
					        else cntFalse++;

					        emailBody += setHtmlWeekBalanceCheckFormBody(mid, dailyTrxTmt, dailyInsideTmt, trxTotalAmt); 
					    //    System.out.println(allInvestorsList.get(i) + "	" + dailyTrxTmt + "	"+ dailyInsideTmt);
				        }
				        
				        String emailFooter = setHtmlWeekBalanceCheckFormFooter();
				        String subject = "[청년5.5] 지난 한주간 투자자 입출금 현황 [이상있는 검증값 : " + cntFalse + ", 문제없는 검증값 : " + cntTrue + "]";

				        String emailHeader = setHtmlWeekBalanceCheckFormHeader(cntFalse, cntTrue);
				        
				        StringBuilder sb = new StringBuilder(emailHeader);
				        sb.append(emailBody);
				        sb.append(emailFooter);
				        		
				        		
						if(emailBody.length() > 0)
							commonUtil.sendLoggingEmailAPIManager(subject, sb.toString());
				}
			} catch(Exception e) {
				commonUtil.sendBatchLogging("newMemberEventCntForLastWeek", "exception error!!", e.getMessage());
	    		throw new RuntimeException(e.getMessage());
			}
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
	
											//대출번호, 상환전날, 남은회차정보 
	private String getDepositPayment2(String loanId, String payDate, List<OneRepaymentCheckCount> oneRepaymentCheckCounts) {
		try {
																	// 대출기간, 이율, 계좌정보
			OneRepaymentDataInfo oneRePaymentDataInfo = repaymentService.selectRepaymentDataInfo(loanId);
			
			long totalDepositAmt = depositService.selectRepaymentTotalDepositAmt(oneRePaymentDataInfo.getLoanAccntNo());	//IB_FB_P2P_IP-대출자 입금합 선택
	        
	    	for(int i = 0; i < oneRepaymentCheckCounts.size(); i++) {
	        	String payAmount = oneRepaymentCheckCounts.get(i).getPayAmount();
	        	totalDepositAmt -= Long.parseLong(payAmount);
	        	
	        	if(totalDepositAmt >= 0) {
	        		oneRepaymentCheckCounts.subList(i, i + 1).clear();
	        		i--;
	        	}
	    	}
	    	
	    	// 추가3줄
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
	
	private String setHtmlTableFormHeader(String type, int count) {
		String header = "<table width=\"696\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\"><tbody><tr><td style=\"height:4px;font-size:0;line-height:0\">&nbsp;</td></tr>\r\n" + 
        		"<tr><td style=\"height:3px;border-top:3px solid #554f4c;font-size:0;line-height:0\">&nbsp;</td></tr><tr><td style=\"height:25px;font-size:0;line-height:0\">&nbsp;</td></tr>\r\n" + 
        		"<tr><td style=\"font-family:돋움,dotum;font-size:16px;font-weight:bold\"><span>[정산] <font style='color:#559fc6;'>" + type + "</font> " + count + "건이 있습니다.</span></td></tr>\r\n" + 
        		"<tr><td style=\"height:22px;font-size:0;line-height:0\">&nbsp;</td></tr><tr><td style=\"border:1px solid #e9e9e9;background:#f9f9f9\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\r\n" + 
        		"<tbody><tr><td style=\"width:29px;height:22px;font-size:0;line-height:0\">&nbsp;</td><td></td></tr><tr><td>&nbsp;</td><td>\r\n";
		return header;
	}
	
	private String setHtmlTableFormBody(String loanId, String loanTitle, String mid, String name, String payment) {
		String body = "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\"><tbody><tr><td style=\"width:71px;font-weight:bold;font-family:'\\\\00b3cb\\\\00c6c0',Dotum;color:#595959;font-size:12px\">\r\n" + 
        		"<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\"><tbody><tr>\r\n" + 
        		"<td style=\"width:63px;font-weight:bold;font-family:'\\\\00b3cb\\\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\">대출제목</td><td style=\"font-size:12px;color:#666\">\r\n" + 
        		"<span style=\"font-weight:bold\">:</span></td></tr></tbody></table></td><td style=\"color:#666;font-family:'\\\\00b3cb\\\\00c6c0',Dotum;font-size:12px;line-height:19px\"><a href = 'http://solution.crepass.com/crapas/?cms=loan_form&type=m&i_id=" + loanId + "&i_loan_type=credit'>" + loanTitle + "</a></td></tr><tr>\r\n" + 
        		"<td style=\"font-weight:bold;font-family:'\\\\00b3cb\\\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\">\r\n" + 
        		"<tbody><tr><td style=\"width:63px;font-weight:bold;font-family:'\\\\00b3cb\\\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\">계&nbsp;&nbsp;&nbsp;&nbsp;정</td><td style=\"font-size:12px;color:#666\">\r\n" + 
        		"<span style=\"font-weight:bold\">:</span></td></tr></tbody></table></td><td style=\"font-family:'\\\\00b3cb\\\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\">" + mid + "</td></tr>\r\n" + 
        		"<tr><td style=\"font-weight:bold;font-family:'\\\\00b3cb\\\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\">\r\n" + 
        		"<tbody><tr><td style=\"width:63px;font-weight:bold;font-family:'\\\\00b3cb\\\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\">이&nbsp;&nbsp;&nbsp;&nbsp;름</td><td style=\"font-size:12px;color:#666\">\r\n" + 
        		"<span style=\"font-weight:bold\">:</span></td></tr></tbody></table></td><td style=\"font-family:'\\\\00b3cb\\\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\">" + name + "</td></tr>\r\n" + 
        		"<tr><td style=\"vertical-align:top\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\"><tbody><tr>\r\n" + 
        		"<td style=\"width:63px;font-weight:bold;font-family:'\\\\00b3cb\\\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\">대출금액</td><td style=\"font-size:12px;color:#666\">\r\n" + 
        		"<span style=\"font-weight:bold\">:</span></td></tr></tbody></table></td><td style=\"color:#666;font-family:'\\\\00b3cb\\\\00c6c0',Dotum;font-size:12px;line-height:20px\">" + payment + "</td></tr>\r\n" + 
        		"</tbody></table>\r\n";
		return body;
	}
	
	private String setHtmlTableFormFooter() {
		String footer = "</td></tr><tr><td style=\"height:20px;font-size:0;line-height:0\">&nbsp;</td><td></td></tr></tbody></table></td></tr><tr><td style=\"height:25px;font-size:0;line-height:0\">&nbsp;</td></tr>\r\n" + 
        		"<tr><td style=\"height:11px;font-size:0;line-height:0\">&nbsp;</td></tr><tr><td style=\"height:28px;border-top:2px solid #554f4c;font-size:0;line-height:0\">&nbsp;</td></tr></tbody></table>";
		return footer;
	}
	
	private String setHtmlManagerExcuteFormHeader1() {
		
		return null;
	}
	private String setHtmlManagerExcuteFormHeader2() {
		
		return null;
	}
	private String setHtmlManagerExcuteFormBody() {
		
		return null;
	}
	private String setHtmlManagerExcuteFormFooter() {
		
		return null;
	}
	
	
	private String setHtmlWeekBalanceCheckFormHeader(int cntFalse, int cntTrue ) {
		String emailHeader= "	<table  width=\"696\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\">\r\n" + 
				"		<tbody>\r\n" + 
				"			<tr >\r\n" + 
				"				<td colspan=\"4\" style=\" height: 4px; font-size: 0; line-height: 0\">&nbsp;</td> \r\n" + 
				"			</tr>\r\n" + 
				"			<tr>\r\n" + 
				"				<td colspan=\"4\" style=\"height: 3px; border-top: 3px solid #554f4c; font-size: 0; line-height: 0\">&nbsp;</td>\r\n" + 
				"			</tr>\r\n" + 
				"			<tr>\r\n" + 
				"				<td colspan=\"4\" style=\"height: 25px; font-size: 0; line-height: 0\">&nbsp;</td>\r\n" + 
				"			</tr>\r\n" + 
				"			<tr>\r\n" + 
				"				<td width=\"100%\" colspan=\"4\" style=\"font-family: 돋움, dotum; font-size: 16px; font-weight: bold\">\r\n" + 
				"				<span><font style=\"color: #559fc6;\"> 이상있는 투자자</font> : " + cntTrue + "명, 이상없는 투자자 : " + cntTrue + "명</span> </td>\r\n" + 
				"			</tr>\r\n" + 
				"			\r\n" + 
				"			<tr> <td colspan=\"4\" style=\" height: 4px; font-size: 0; line-height: 0\">&nbsp;</td> </tr>\r\n" + 
				"			<tr> <td colspan=\"4\" style=\"height: 3px; border-top: 3px solid #554f4c; font-size: 0; line-height: 0\">&nbsp;</td> </tr>\r\n" + 
				"			<tr> <td colspan=\"4\" style=\"height: 10px; font-size: 0; line-height: 0\">&nbsp;</td> \r\n" + 
				"			<tr>\r\n" + 
				"			\r\n" + 
				"				<td width=\"40%\" style=\"font-family: 돋움, dotum; font-size: 16px; font-weight: bold; text-align:center;\"> ID</td>\r\n" + 
				"				<td width=\"20%\" style=\"font-family: 돋움, dotum; font-size: 16px; font-weight: bold; text-align:center;\"> TRX</td>\r\n" + 
				"				<td width=\"20%\" style=\"font-family: 돋움, dotum; font-size: 16px; font-weight: bold; text-align:center;\"> 검증값</td>\r\n" + 
				"				<td width=\"20%\" style=\"font-family: 돋움, dotum; font-size: 16px; font-weight: bold; text-align:center;\"> 예치금잔액</td>\r\n" + 
				"			</tr>";
		return emailHeader;
		}
	
	private String setHtmlWeekBalanceCheckFormBody(String mid, long dailyTrxTmt, long dailyInsideTmt, String trxTotalAmt) {
		String emailBody = "<tr> <td colspan=\"4\" style=\"height: 10px; font-size: 0; line-height: 0\">&nbsp;</td> \r\n" + 
				"			<tr>\r\n" + 
				"			\r\n" + 
				"				<td width=\"40%\" style=\"font-family: 돋움, dotum; font-size: 16px; font-weight: bold; text-align:center;\"> " + mid + "</td>\r\n" + 
				"				<td width=\"20%\" style=\"font-family: 돋움, dotum; font-size: 16px; font-weight: bold; text-align:center;\"> " + dailyTrxTmt + "</td>\r\n" + 
				"				<td width=\"20%\" style=\"font-family: 돋움, dotum; font-size: 16px; font-weight: bold; text-align:center;\"> " + dailyInsideTmt + "</td>\r\n" + 
				"				<td width=\"20%\" style=\"font-family: 돋움, dotum; font-size: 16px; font-weight: bold; text-align:center;\"> " + trxTotalAmt + "</td>\r\n" + 
				"			</tr>\r\n" + 
				"			";
		return emailBody;
		}
	
	private String setHtmlWeekBalanceCheckFormFooter( ) {
		String emailFooter = "		</tbody>\r\n" + 
				"	</table>\r\n" + 
				"";
		return emailFooter;
	}
	
	  public static boolean getBetweenWeekAgoAndYesterday(String startStr, String endStr, String targetStr) {
	    	try {
		    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		    	Date startDate = formatter.parse(startStr);
		    	Date endDate = formatter.parse(endStr);
		    	Date targetDate = formatter.parse(targetStr);
		    	
		    	long diff = endDate.getTime() - startDate.getTime();
		    	
		    	if(diff >= 0) {
		    		if (startDate.getTime() <= targetDate.getTime() && targetDate.getTime() <= endDate.getTime()) {
		    			return true;
		    		} else
		    			return false;
		    	}
		    	
	    	} catch (Exception e) {
	    		System.out.println(e.getMessage());
	    	}
			return false;
	    }
}
