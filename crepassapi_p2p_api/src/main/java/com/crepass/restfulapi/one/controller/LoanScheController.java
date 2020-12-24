package com.crepass.restfulapi.one.controller;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.Slack;
import com.crepass.restfulapi.ks.service.VirAccntService;
import com.crepass.restfulapi.one.domain.OneInvest;
import com.crepass.restfulapi.one.domain.OneInvestPaymentHistory;
import com.crepass.restfulapi.one.domain.OneLoanAddInvestInfo;
import com.crepass.restfulapi.one.domain.OneLoanCustInfo;
import com.crepass.restfulapi.one.domain.OneVirtualAccnt;
import com.crepass.restfulapi.one.service.EmoneyService;
import com.crepass.restfulapi.one.service.InvestService;
import com.crepass.restfulapi.one.service.LoanService;
import com.crepass.restfulapi.one.service.ScheService;
import com.crepass.restfulapi.one.service.VirtualAccntService;

import net.gpedro.integrations.slack.SlackMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Component
public class LoanScheController {

    @Autowired
    private InvestService investService;
    
    @Autowired
    private EmoneyService emoneyService;
    
    @Autowired
    private VirAccntService virAccntService;
    
    @Autowired
    private VirtualAccntService virtualAccntService;
    
    @Autowired
    private LoanService loanService;
    
    @Autowired
    private ScheService scheService;
    
    @Autowired
    private CommonUtil commonUtil;
    
    @Value("${crepas.inside.url}")
    private String insideUrl;
    
    @Value("${crepas.aes256.key}")
    private String aes256KEY;
    
    @Value("${crepas.aes256.iv}")
    private String aes256IV;

    
    //200630전까지 삭제 예정, 서비스와 매퍼도 삭제예정
    // 미혼모재단 수수료 0%건, 대출 실행 스케줄러 테스트
    @Transactional("oneTransactionManager")
    @Scheduled(cron = "0 23 11 * * *")
    public void startLoan_Test() {
    	try {
    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) {
	    		List<OneLoanCustInfo> loanCustInfo = loanService.selectLoanCustInfoTest(aes256KEY, aes256IV);
	    		
		        for(int i = 0; i < loanCustInfo.size(); i++) {
		        	String loanId = loanCustInfo.get(i).getLoanId();
		        	String loanPay = loanCustInfo.get(i).getLoanPay();
		        	String mid = loanCustInfo.get(i).getMid();
		        	String custId = loanCustInfo.get(i).getCustId();
		        	String name = loanCustInfo.get(i).getName();
		        	String birth = loanCustInfo.get(i).getBirth();
		        	String hp = loanCustInfo.get(i).getHp();
		        	String myBankcode = loanCustInfo.get(i).getMyBankcode();
		        	String myBankacc = loanCustInfo.get(i).getMyBankacc();
		        	String subject = loanCustInfo.get(i).getSubject();
		        	String loanAccntNo = loanCustInfo.get(i).getLoanAccntNo();
		        	String loanCate = loanCustInfo.get(i).getLoanCate();
		        	
	                long interest = (long)getInterest2(Double.parseDouble(loanPay));
	                if (loanCate.equals("cate08"))																				// 미혼모재단의 경우 수수료 0원 공제
	                	interest = 0;
	                
	                long realLoanPay = Long.parseLong(loanPay) - interest;
	                
	                System.out.println("loanId : " + loanId + ", loanPay : " + loanPay + ", interest : " + interest + ", realLoanPay : " + realLoanPay);
		        	
		        }
    		}
    		
    	} catch (Throwable t) {
    		Slack.api.call(new SlackMessage("#young-server","jhlee","대출 실행 스케줄러 테스트"));
    		commonUtil.sendBatchLogging("startLoan", "exception error!!", t.getMessage());
    		throw new RuntimeException(t.getMessage());
    	}
    }
    
    
    
    // 1. 대출 실행 스케줄러
    @Transactional("oneTransactionManager")
    @Scheduled(cron = "0 0/5 * * * *")
    public void startLoan() {

    	try {
    		if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) 
    		{
		        RestTemplate restTemplate = new RestTemplate();
		        																// 승인된 대출건중(i_loanapproval=Y)에  대출실행이 안된(loan_step4=N) 채권정보 선택
		        List<OneLoanCustInfo> loanCustInfo = loanService.selectLoanCustInfo(aes256KEY, aes256IV);
		        
		        for(int i = 0; i < loanCustInfo.size(); i++) {
		        	String loanId = loanCustInfo.get(i).getLoanId();
		        	String loanPay = loanCustInfo.get(i).getLoanPay();
		        	String mid = loanCustInfo.get(i).getMid();
		        	String custId = loanCustInfo.get(i).getCustId();
		        	String name = loanCustInfo.get(i).getName();
		        	String birth = loanCustInfo.get(i).getBirth();
		        	String hp = loanCustInfo.get(i).getHp();
		        	String myBankcode = loanCustInfo.get(i).getMyBankcode();
		        	String myBankacc = loanCustInfo.get(i).getMyBankacc();
		        	String subject = loanCustInfo.get(i).getSubject();
		        	String loanAccntNo = loanCustInfo.get(i).getLoanAccntNo();
		        	String loanCate = loanCustInfo.get(i).getLoanCate();
		        	
		        	if(myBankcode.length() > 1 && myBankacc.length() > 1) {		// 은행 정보가 있고
			        	boolean loanPaymentIsOK = loanService.selectLoanPaymentIsOK(loanId);
			        	
			        	if(loanPaymentIsOK) {									// 투자금이 다 모였으면(ml.i_loan_pay==SUM(mi.i_pay))
			        		if(loanAccntNo == null || loanAccntNo.length() < 1) {
			        			OneVirtualAccnt oneVertualAccntLoan = virtualAccntService.selectVirtualAccntLoanInfo();					// virtual_account-사용안하는 가상계좌 가져옴
			        			
			        			if(oneVertualAccntLoan == null) {
			        				commonUtil.sendBatchLogging("startLoan", "oneVertualAccntLoan => " + oneVertualAccntLoan, "가상계좌 생성 오류, 조회할 대출자 가상계좌가 없습니다.");
			                	} else {
			                		boolean isUpdateLoanAccnt = loanService.updateLoanAccnt(loanId, oneVertualAccntLoan.getAccount());	// ml-대출자 가상계좌 업데이트
			                		if(isUpdateLoanAccnt)
			                			loanService.updateMemberLoanUse(mid);															// mari_seyfert-대출자 발급여부Y로 체크
			                		
			                		virtualAccntService.updateVirtualaccntUse(oneVertualAccntLoan.getAccount());						// virtual_account-대출자 가상계좌 사용중으로 업데이트
			                		int isVirAccntUse = virAccntService.updateVirAccntUse(oneVertualAccntLoan.getAccount(), name + "_크레파스", custId);	// ksnet_live.KSNET_VR_ACCOUNT에 업데이트
			                		
			                		loanAccntNo = oneVertualAccntLoan.getAccount();
			                		
			                		if(isVirAccntUse <= 0) {
			                			commonUtil.sendBatchLogging("startLoan", "isVirAccntUse => " + isVirAccntUse, "대출자 가상계좌 발급 도중 에러가 발생했습니다. mid : " + mid);
			                			throw new RuntimeException("대출자 가상계좌 발급 도중 에러가 발생했습니다. mid : " + mid);
			                		}
			                	}
			        		}
			        		
			        		HashMap<String, String> vars = new HashMap<String, String>();
				            
				            DateFormat df = new SimpleDateFormat("yyyyMMdd");
				            Date date = new Date();
				            Calendar cal = Calendar.getInstance();
			                cal.setTime(date);
			                String TodayDate = df.format(cal.getTime());
			                cal.add(Calendar.MONTH, 24);
			                String LastDate = df.format(cal.getTime());
			
			                List<OneLoanAddInvestInfo> oneLoanAddInvestInfo = loanService.selectLoanAddInvestInfo(loanId);				// 해당 대출건에 대한 투자자 정보 선택
			                
			                long interest = (long)getInterest2(Double.parseDouble(loanPay));
			                if (loanCate.equals("cate08"))																				// 미혼모재단의 경우 수수료 0원 공제
			                	interest = 0;
			                
			                long realLoanPay = Long.parseLong(loanPay) - interest;
			                
			                vars = new HashMap<String, String>();
			                vars.put("LOAN_SEQ", loanId);
			                vars.put("LOAN_AMT", loanPay);
			                vars.put("LOAN_FEE", String.valueOf(interest));
			                vars.put("LOAN_EXEC_DATE", TodayDate);
			                vars.put("LOAN_EXP_DATE", LastDate);
			                vars.put("LOAN_CUST_ID", custId);
			                vars.put("LOAN_CUST_NM", name);
			                vars.put("CMS_NB", loanAccntNo);
			                vars.put("LOAN_DEP_CNT", "1");
			                vars.put("INV_CNT", String.valueOf(oneLoanAddInvestInfo.size()));
			                vars.put("LOAN_DEP_BANK_CD1", myBankcode);
			                vars.put("LOAN_DEP_ACCT_NB1", myBankacc);
			                vars.put("LOAN_DEP_AMT1", String.valueOf(realLoanPay));
			                vars.put("LOAN_DEP_BANK_CD2", "");
			                vars.put("LOAN_DEP_ACCT_NB2", "");
			                vars.put("LOAN_DEP_AMT2", "");
			                vars.put("LOAN_DEP_BANK_CD3", "");
			                vars.put("LOAN_DEP_ACCT_NB3", "");
			                vars.put("LOAN_DEP_AMT3", "");
			                vars.put("LOAN_DEP_BANK_CD4", "");
			                vars.put("LOAN_DEP_ACCT_NB4", "");
			                vars.put("LOAN_DEP_AMT4", "");
			                vars.put("LOAN_DEP_BANK_CD5", "");
			                vars.put("LOAN_DEP_ACCT_NB5", "");
			                vars.put("LOAN_DEP_AMT5", "");
			            
			                String resultCustAdd = restTemplate.postForObject(insideUrl + "/loan/add", vars, String.class);				// 대출등록 전문요청
			                JSONObject jsonCustAdd = new JSONObject(resultCustAdd);
			                
			                int STATE_CUSTADD = jsonCustAdd.getInt("STATE");
			                String custId_And_Message = custId +  jsonCustAdd.getString("MESSAGE");										// 나중에 예쁘게 수정 에정
			                			                
			                if(STATE_CUSTADD != 200) {
			                	commonUtil.sendBatchLogging("startLoan", "STATE_CUSTADD => " + STATE_CUSTADD, "대출등록에 실패하였습니다. 고객ID : " + custId_And_Message);
			                	throw new RuntimeException("대출등록에 실패하였습니다. 고객ID : " + custId);
			                }
		               
			                for(int j = 0; j < oneLoanAddInvestInfo.size(); j++) {
			                	String i_custId = oneLoanAddInvestInfo.get(j).getCustId();
			                	String i_pay = oneLoanAddInvestInfo.get(j).getPay();
			                	String i_subject = oneLoanAddInvestInfo.get(j).getSubject();
			                	String i_mid = oneLoanAddInvestInfo.get(j).getMid();
			                	
			                	Calendar calendar = Calendar.getInstance();
								int year = calendar.get(Calendar.YEAR);
								
								int investSeq = Integer.parseInt(investService.selectInvestId(loanId));									// 대출건에 투자자별로 순번 지정(getInvestSeq프로시저 호출해서 cpas_invest_numering에 정보삽입)
								double eMoney = emoneyService.selectTrAmtBalanceById(mid);												// mm-해당 계정의 예치금선택
								
								String prinRcvNo = "CREDIT" + String.valueOf(year) + "L" + loanId + "I" + String.valueOf(investSeq);	//원천징수번호(원리금 수취권증서)생성 mi-investSeq
								
			    				vars = new HashMap<String, String>();
			    	            vars.put("LOAN_SEQ", loanId);
			    	            vars.put("INV_SEQ", String.valueOf(investSeq));
			    	            vars.put("INV_CUST_ID", i_custId);
			    	            vars.put("PRIN_RCV_NO", prinRcvNo);
			    	            vars.put("INV_AMT", i_pay);
			    	            
			    	            String resultInvestAdd = restTemplate.postForObject(insideUrl + "/loan/invest/add", vars, String.class);	// 등록한 대출에 투자자등록 전문요청
			    	            
			    	            JSONObject jsonResultInvestAdd = new JSONObject(resultInvestAdd);
			    	            
			    	            int STATE_INVEST = jsonResultInvestAdd.getInt("STATE");
			    	            
			    	            if(STATE_INVEST == 200) {																				// 전문요청이 성공했으면
			    	            	boolean isPrinRcvNo = investService.updatePrinRcvNo(i_mid, loanId, prinRcvNo);						// 투자건의 투자자에게  원천징수번호(원리금 수취권증서) 업데이트
			    	            	
			    	            	if(!isPrinRcvNo) {
			    	            		//System.out.println("updatePrinRcvNo : 원리금수취권증서를 업데이트 하지 못했습니다. mid : " + i_mid + " loanId : " + loanId + " prinRcvNo : " + prinRcvNo);
			    	            		commonUtil.sendBatchLogging("startLoan", "mid : " + i_mid + " loanId : " + loanId + " prinRcvNo : " + prinRcvNo, "원리금수취권증서를 업데이트 하지 못했습니다. isPrinRcvNo : " + isPrinRcvNo);
			    	            	}
			    	            	
			    	            	int updateInvset = investService.updateInvest(String.valueOf(investSeq), i_mid, loanId);			// mari_seyfert_order-거래번호(s_tid) 업데이트
			    	            	OneInvestPaymentHistory oneInvestPaymentHistory = new OneInvestPaymentHistory();
			    	            	oneInvestPaymentHistory.setMid(i_mid);
			    	            	oneInvestPaymentHistory.setContent(i_subject + " 투자건 입찰");
			    	            	oneInvestPaymentHistory.setEmoney(i_pay);
			    	            	oneInvestPaymentHistory.setTopEmoney(String.valueOf(eMoney));
			    	            	oneInvestPaymentHistory.setIp("");
			    	            	oneInvestPaymentHistory.setLoanId(loanId);
			    	            	investService.insertInvestPaymentHistory(oneInvestPaymentHistory);				// mari_emoney-정보등록(200709이후 사용하지 않음)
			    	            	
			    	            	if (updateInvset < 0) {
			    	            		//System.out.println("투자자 등록에 예치기 못한 에러가 발생하였습니다. mid : " + i_mid + " loanId : " + loanId + " investSeq : " + investSeq);
			    	            		commonUtil.sendBatchLogging("startLoan", "mid : " + i_mid + " loanId : " + loanId + " investSeq : " + investSeq, "투자자 등록에 예기치 못한 에러가 발생하였습니다.");
			    	            	}
			    	            } else {
			    	            	//System.out.println(jsonResultInvestAdd.getString("MESSAGE"));
			    	                commonUtil.sendBatchLogging("startLoan", "resultInvestAdd : " + resultInvestAdd, jsonResultInvestAdd.getString("MESSAGE"));
			    	            }
			                }
			                
			                vars = new HashMap<String, String>();
				            vars.put("LOAN_SEQ", loanId);
				            
				            String resultLoanStart = restTemplate.postForObject(insideUrl + "/loan/start", vars, String.class);			// 대출 시작 전문
				            
				            JSONObject jsonResultLoanStart = new JSONObject(resultLoanStart);
				            
				            int STATE_LOANSTART = jsonResultLoanStart.getInt("STATE");
				            
				            if(STATE_LOANSTART != 200) {
				            	commonUtil.sendBatchLogging("startLoan", "대출실행에 실패하였습니다. 대출번호 : " + loanId, jsonResultLoanStart.getString("MESSAGE"));
				            } else {
				            	int updateLoanState = loanService.updateLoanState(loanId);												// ml-대출실행시간(i_exec_date), 실행시작(loan_step4=S)으로 업데이트
			            		if (updateLoanState < 0) {
			            			commonUtil.sendBatchLogging("startLoan", "mid : " + mid + " loanId : " + loanId, "대출 스케줄 상태 업데이트 에러 발생");
		    	            	}
				            	
				            	for(int j = 0; j < oneLoanAddInvestInfo.size(); j++) {
				            		// 1. 투자자 출금(loan_id와 대출실행이므로 회차만 없어서 null값)
//				            		boolean insertDepositHistory = scheService.insertDepositHistory(oneLoanAddInvestInfo.get(j).getMid(),"W", oneLoanAddInvestInfo.get(j).getPay(), "I");	// cpas_trx_log-투자자 출금내용 입력
				            		boolean insertDepositHistory = scheService.insertDepositHistory(oneLoanAddInvestInfo.get(j).getMid(),"W", oneLoanAddInvestInfo.get(j).getPay(), "I", loanId, null);	// cpas_trx_log-투자자 출금내용 입력
				            		
				            		// 1. cwt-회차정보 없어서 null
				            		boolean insertAccntWithdrawSchedule3 = virtualAccntService.insertAccntWithdrawSchedule3(oneLoanAddInvestInfo.get(j).getMid(), loanId, oneLoanAddInvestInfo.get(j).getPay(), "I", "S");	// cpas_withdraw_trx-투자자 출금내역 입력
				            		
				            		commonUtil.sendBatchLogging("startLoan", "getMid : " + oneLoanAddInvestInfo.get(j).getMid(), String.valueOf(insertDepositHistory));
				            		commonUtil.sendBatchLogging("startLoan", "getMid : " + oneLoanAddInvestInfo.get(j).getMid() + " loanId : " + loanId + " getPay : " + oneLoanAddInvestInfo.get(j).getPay()
				            				, String.valueOf(insertAccntWithdrawSchedule3));
				            	}
				            	commonUtil.sendBatchLogging("startLoan", "대출번호 : " + loanId, "대출실행이 정상적으로 처리되었습니다.");
				            }
				            
				            int ranNum = (int)(Math.random() * (999 - 111 + 1)) + 111;
							long time = System.currentTimeMillis();
							String gCode = "P" + String.valueOf(time) + String.valueOf(ranNum);											// s_refId 생성(은행에 들어가기 위한 투자자 번호생성)
							
				            OneInvest oneInvest = new OneInvest();
				            oneInvest.setGCode(gCode);
				            oneInvest.setMid(mid);
				            oneInvest.setMName(name);
				            oneInvest.setSubject(subject);
				            oneInvest.setLoanId(loanId);
				            oneInvest.setIPay(loanPay);
				            
				            int loanHistory = loanService.insertLoanHistory(oneInvest);													// mari_seyfert_order-상태값 저장
				            
				            if(loanHistory < 0) {
				            	commonUtil.sendBatchLogging("startLoan", "mid : " + mid + " name : " + name + " subject : " + subject + " loanId : " + loanId + " loanPay : " + loanPay
				            			, "대출실행 거래이력 저장에 실패하였습니다. gCode : " + gCode);
				            }
			        	} else {
			        		commonUtil.sendBatchLogging("startLoan", "loanPaymentIsOK : " + loanPaymentIsOK, "투자금액이 대출금액과 동일하지 않습니다. 대출ID : " + loanId);
			        	}
			        } else {
			        	commonUtil.sendBatchLogging("startLoan", "myBankacc : " + myBankacc, "출금계좌가 등록되어 있지 않습니다. : " + mid);
			        }
		        }
    		}
//    		Slack.api.call(new SlackMessage("#young-server","jhlee","1. 투자자 출금"));	
    	} catch (Throwable t) {
    		Slack.api.call(new SlackMessage("#young-server","jhlee","1. 투자자 출금 실패!"));
    		commonUtil.sendBatchLogging("startLoan", "exception error!!", t.getMessage());
    		throw new RuntimeException(t.getMessage());
    	}
    }
    
    
    
    
    
    public double getInterest(double payment) {
    	double result = 0;
    	double price1 = 5000000;
    	double price2 = 10000000;
    	
    	if(payment <= price1)
    		result = payment * 0.03;
    	else if(payment > price1 && payment <= price2)
    		result = (price1 * 0.03) + ((payment - price1) * 0.02);
    	else
    		result = (price1 * 0.03) + (price1 * 0.02) + ((payment - price2) * 0.01);
    	
    	return result;
    }
    
    public double getInterest2(double payment) {
    	return payment * 0.02;
    }
    
    
}