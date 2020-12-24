package com.crepass.restfulapi.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.crepass.restfulapi.config.ExloggerApplication;
import com.crepass.restfulapi.cre.domain.TdairyStatistics;
import com.crepass.restfulapi.one.domain.OneHolidayCalendar;
import com.crepass.restfulapi.one.domain.OneSendCheckingEmail;
import com.crepass.restfulapi.one.domain.OneSendEmail;
import com.crepass.restfulapi.one.domain.OneSendEmailInfo;
import com.crepass.restfulapi.one.service.NotifyScheService;
import com.crepass.restfulapi.one.service.ScheService;
import com.crepass.restfulapi.one.service.VirtualAccntService;
import com.ibm.icu.util.ChineseCalendar;

@Component
public class CommonUtil {
    
	private static final Logger logger = LoggerFactory.getLogger(ExloggerApplication.class);
	
    @Value("${intro.force.Msg.0}")
    private String forceMsg0;
    
    @Value("${intro.force.Msg.1}")
    private String forceMsg1;

    @Value("${intro.force.Msg.2}")
    private String forceMsg2;

    @Value("${intro.force.Msg.3}")
    private String forceMsg3;
    
    @Value("${crepas.url.invest}")
    private String investUrl;
    
    @Value("${crepas.url.stock}")
    private String investStockUrl;
    
    @Autowired
    private VirtualAccntService virtualAccntService;
    
    @Autowired
    private ScheService scheService;
    
    @Autowired
	private NotifyScheService notifyScheService;
    
    private JSONObject jsonRequest = null;
    
    public String getToken(int len) {

        Random random = new Random();
        StringBuffer stringBuffer = new StringBuffer();

        if (len < 1) {
            len = 1;
        }

        for (int i = 0; i < len; i++) {
            if (random.nextBoolean()) {
                stringBuffer.append((char)((int)(random.nextInt(26)) + 97));
            } else {
                stringBuffer.append((random.nextInt(10)));
            }
        }
        return stringBuffer.toString();
    }
    
    public String getForceMsg(int key) {
        
        String result;
        
        switch (key) {
            case 1:
                result = forceMsg1;
                break;
            case 2:
                result = forceMsg2;
                break;
            case 3:
                result = forceMsg3;
                break;
            default:
                result = forceMsg0;
        }
        
        return result;
    }

    public Map<String, Object> getAmountUnit(double amount) {
        
        Map<String, Object> result = new HashMap<String, Object>();
        
		//        if ((amount / 100000000) >= 1) {
		//            result.put("amt", Math.floor(amount / 100000000));
		//            result.put("unit", "억원");
		//        } else 
        
        if ((amount / 10000) >= 1) {
            result.put("amt", (long)Math.floor(amount / 10000));
            result.put("unit", "만원");
        } else {
            result.put("amt", (long)amount);
            result.put("unit", "원");
        }
        
        return result;
    }
    
    public String getAmountUnit2(long amount) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(amount) + "원";
    }
    
    public String getAmountUnit3(long amount) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(amount);
    }
    
    public String getDayOfWeekWord(String date) {
    	String result = null;
    	String[] dates = date.split("-");
    	
    	Calendar cal = Calendar.getInstance();
    	cal.set(Calendar.YEAR, Integer.parseInt(dates[0]));
    	cal.set(Calendar.MONTH, Integer.parseInt(dates[1]) - 1);
    	cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dates[2]));
    	
    	switch(cal.get(Calendar.DAY_OF_WEEK)) {
    		case 1:
    			result = "일";
    			break;
    		case 2:
    			result = "월";
    			break;
    		case 3:
    			result = "화";
    			break;
    		case 4:
    			result = "수";
    			break;
    		case 5:
    			result = "목";
    			break;
    		case 6:
    			result = "금";
    			break;
    		case 7:
    			result = "토";
    			break;
    	}
    	
        return result;
    }
    
    public String getRandPW() {
    	StringBuffer temp = new StringBuffer();
    	Random rnd = new Random();
    	for (int i = 0; i < 6; i++) {
    	    int rIndex = rnd.nextInt(3);
    	    switch (rIndex) {
    	    case 0:
    	        temp.append((char) ((int) (rnd.nextInt(26)) + 97));
    	        break;
    	    case 1:
    	        temp.append((char) ((int) (rnd.nextInt(26)) + 65));
    	        break;
    	    case 2:
    	        temp.append((rnd.nextInt(10)));
    	        break;
    	    }
    	}

        return temp.toString();
    }
    
    public String setSHA256(String str){

    	String SHA = ""; 

    	try {
    		MessageDigest sh = MessageDigest.getInstance("SHA-256"); 
    		sh.update(str.getBytes()); 

    		byte byteData[] = sh.digest();
    		StringBuffer sb = new StringBuffer(); 

    		for(int i = 0 ; i < byteData.length ; i++)
    			sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));

    		SHA = sb.toString();
    	}catch(NoSuchAlgorithmException e){
    		e.printStackTrace(); 
    		SHA = null; 
    	}

    	return SHA;
    }
    
    public boolean isValidEmail(String email) {
		boolean err = false;
		String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";   
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(email);
		if(m.matches())
			err = true; 
		return err;
    }
    
    public String getChangeDept(String key) {
        
        String result;
        
        switch (key) {
            case "은행/보험/학자금":
                result = "firstBank";
                break;
            case "저축은행/카드/캐피탈":
                result = "secondBank";
                break;
            case "현금서비스":
                result = "cash";
                break;
            case "대부업체":
                result = "lendding";
                break;
            case "P2P":
                result = "ptop";
                break;
            case "연대보증":
                result = "guarantee";
                break;
            default:
                result = "etc";
        }
        
        return result;
    }
    
    public String getUrlBankName(String bankCode) throws Exception {
//        String result = "";
        
//        result = getUrlBankCombine(null, bankCode, investUrl);
//        result = getUrlBankCombine(result, bankCode, investStockUrl);

        return virtualAccntService.selectBankById(bankCode);
    }
    
    public String getUrlBankCombine(String beforeName, String bankCode, String url) throws Exception {
        String result = "";
        
        RestTemplate restTemplate = new RestTemplate();
        String resultOne = restTemplate.getForObject(url, String.class);
        
        JSONObject resultJson = new JSONObject(resultOne);
        System.out.println("getInvestInfo general: bankCode=" + bankCode + " / " + resultJson);
        
        if ("200".equals(resultJson.getString("result_cd"))) { //pass 
            JSONArray arr = (JSONArray)resultJson.get("data"); 
            
            for (int i = 0; i < arr.length(); i++) {
                if (((JSONObject) arr.get(i)).get("cdKey").toString().equals(bankCode)) {
                    result = ((JSONObject) arr.get(i)).get("cdNm").toString();
                }
            }
        }
        
        if (result == null) {
            result = beforeName;
        }
        
        return result;
    }
    
    public Object getActDays(String actCd, int days, int weekTime, TdairyStatistics returnObject) {
        
        returnObject.setActCd(actCd);
        if (days == 0) {
            returnObject.setMon(weekTime);
        } else if (days == 1) {
            returnObject.setTue(weekTime);
        } else if (days == 2) {
            returnObject.setWed(weekTime);
        } else if (days == 3) {
            returnObject.setThi(weekTime);
        } else if (days == 4) {
            returnObject.setFri(weekTime);
        } else if (days == 5) {
            returnObject.setSat(weekTime);
        } else if (days == 6) {
            returnObject.setSun(weekTime);
        }
        return returnObject;
    }
    
    public String getRestTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime targetDateTime = currentDateTime.plusHours(1).withMinute(0).withSecond(0);
        long minute = currentDateTime.until(targetDateTime, ChronoUnit.MINUTES); 
        long second = currentDateTime.until(targetDateTime, ChronoUnit.SECONDS); 
        String result = minute + "분 " + second % 60 + "초";
        
        return result;
    }
    
    public String getRemoteAddrs() {
    	ServletRequestAttributes servletContainer = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = servletContainer.getRequest();
        
    	return request.getRemoteAddr();
    }
    
    public String getHostName() throws Exception {
    	return InetAddress.getLocalHost().getHostName();
    }
    
    public String getHostNameLinux() throws Exception {
    	String hostname = "";
        String lineStr  = "";
        Process process;
    	
    	try {
            process = Runtime.getRuntime().exec("hostname");
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while((lineStr = br.readLine()) != null ){
                 hostname = lineStr;
            }
        } catch (IOException e) {
            e.printStackTrace();
            hostname = "none";
        }
        return hostname;

    }
    
    public Map<String, Long> getDeptList(String dept) {
		Map<String, Long> result = new HashMap<>();
		
		if(dept != null && !dept.isEmpty()) {
			String[] deptRecord = dept.split("\\[RECORD\\]");
			
			long amt = 0;
			for(int i = 0; i < deptRecord.length; i++) {
				
				String[] deptRecordField = deptRecord[i].split("\\[FIELD\\]");
				
				
	            if (deptRecordField.length > 3) {
	            	amt = Long.parseLong(deptRecordField[2]);					
	            }
				
				String deptFieldType = getDeptFieldType(deptRecordField[0]);
				
				//만약에 result에 값이 있으면 누적값 더하기
				if( result.get(deptFieldType) != null ) {
					amt += result.get(deptFieldType);
				}
				
				result.put(deptFieldType, amt);
			}
			
			// 200408 이전버전(누적이 안되서 수정)
//				for(int i = 0; i < deptRecord.length; i++) {
//				
//				String[] deptRecordField = deptRecord[i].split("\\[FIELD\\]");
//				
//				
//	            if (deptRecordField.length > 3) {
//	            	amt = Long.parseLong(deptRecordField[2]);					
//	            }
//				
//				String deptFieldType = getDeptFieldType(deptRecordField[0]);
//				result.put(deptFieldType, amt);
//			}
		}
		
		return result;
	}
	
	private String getDeptFieldType(String field) {
		String result = null;
		
		switch(field) {
			case "은행/보험/학자금":
	            result = "firstBank";
	            break;
	        case "저축은행/카드/캐피탈":
	            result = "secondBank";
	            break;
	        case "현금서비스":
	            result = "cash";
	            break;
	        case "대부업체":
	            result = "lendding";
	            break;
	        case "P2P":
	            result = "ptop";
	            break;
	        case "연대보증":
	            result = "guarantee";
	            break;
	        default:
	            result = "etc";
		}
		
		return result;
	}
    
    public void sendRequestLogging(String api, String requestParam) {
    	logger.info("<==========================" + api + "=============================>");
        logger.info("request parameter : " + requestParam);
    }
    
    public void sendResultLogging(String api, String resultParam) {
        logger.info("response parameter : " + resultParam);
        logger.info("<==========================" + api + "=============================>");
    }
    
    public void sendBatchLogging(String methodName, String inputParam, String result) {
    	logger.info("<==========================" + methodName + "=============================>");
        logger.info("input : " + inputParam);
        logger.info("result : " + result);
        logger.info("<==========================" + methodName + "=============================>");
    }
    
    public String getFormSMS(int type, JSONObject data) throws JSONException {
    	String result = "";
    	
    	switch(type) {
	    	case 1:
	    		result = "안녕하세요. " + data.getString("name") + "님.\r\n" + 
						 "청년5.5  크레파스입니다. \r\n" + 
						 "다음 영업일이 결제일이라 사전에 안내드립니다.\r\n" + 
						 "\r\n" +
						 "결제일 : " + data.getString("payDate") + "(" + data.getString("payWeek") + ") (매월 " + data.getString("repayDate") + "일) \r\n" +
						 "결제금액: " + data.getString("payment") + "원 \r\n" +
						 "가상계좌: 신한 " + data.getString("loanAccntNo") + "\r\n" +
						 "예금주: " + data.getString("name") + "_크레파스\r\n" +
						 "\r\n" +
						 "감사합니다. 오늘도 화이팅하세요.";
	    		break;
	    		
	    	case 2:
	    		result = "안녕하세요. " + data.getString("name") + "님.\r\n" + 
						 "청년5.5  크레파스입니다. \r\n" + 
						 "오늘이 청년5.5 결제일입니다.\r\n" + 
						 "\r\n" + 
						 "결제일 : " + data.getString("payDate") + "(" + data.getString("payWeek") + ") (매월 " + data.getString("repayDate") + "일) \r\n" +
						 "결제금액: " + data.getString("payment") + "원 \r\n" +
						 "가상계좌: 신한 " + data.getString("loanAccntNo") + "\r\n" +
						 "예금주: " + data.getString("name") + "_크레파스\r\n" +
						 "\r\n" +
						 "감사합니다.";
	    		break;
	    		
	    	case 3:
				result = "안녕하세요. " + data.getString("name") + "님.\r\n" + 
						 "청년5.5  크레파스입니다. \r\n" + 
						 "\r\n" + 
						 "결제일이 4영업일 지났습니다.\r\n" + 
						 "다음 영업일에 신용정보집중기관에 미납 통보를 해야합니다. \r\n" + 
						 "외부 정보공유시 금융이용에 제한을 받으실수 있습니다. \r\n" + 
						 "\r\n" + 
						 "결제금액: " + data.getString("payment") + "원 \r\n" +
						 "가상계좌: 신한 " + data.getString("loanAccntNo") + "\r\n" +
						 "예금주: " + data.getString("name") + "_크레파스\r\n" + 
						 "\r\n" + 
						 "청년5.5는 선한의지로 공급되는 청년을 지원하는 금융프로그램으로 향후 연속성의 동력을 가질 수 있도록 도움을 주시길 부탁 드립니다.\r\n" + 
						 "\r\n" + 
						 "감사합니다.\r\n" + 
						 "\r\n" + 
						 "# 문자 확인 전 납부하셨으면 연락주세요.";
	    		break;
	    		
	    	case 4:
				result = "안녕하세요. " + data.getString("name") + "님.\r\n" + 
						 "청년5.5  크레파스입니다. \r\n" + 
						 "\r\n" + 
						 "결제일이 5영업일이 지났습니다. \r\n" + 
						 "금일은 신용정보 집중기관에 미납통보를 해야 합니다. \r\n" + 
						 "외부 정보공유시 금융이용에 제한을 받으실 수 있습니다. \r\n" + 
						 "\r\n" + 
						 "결제금액: " + data.getString("payment") + "원 \r\n" +
						 "가상계좌: 신한 " + data.getString("loanAccntNo") + "\r\n" +
						 "예금주: " + data.getString("name") + "_크레파스\r\n" + 
						 "\r\n" + 
						 "청년5.5는 선한의지로 공급되는 청년을 지원하는 금융프로그램으로 향후 연속성의 동력을 가질 수 있도록 도움을 주시면 감사하겠습니다.\r\n" + 
						 "\r\n" + 
						 "감사합니다.\r\n" + 
						 "\r\n" + 
						 "# 문자 확인 전 납부하셨으면 연락주세요.";
	    		break;
	    		
	    	case 5:
				result = "안녕하세요. " + data.getString("name") + "님.\r\n" + 
						 "청년5.5  크레파스입니다. \r\n" + 
						 "결제일이 " + data.getString("overDueDays") + "영업일 지났습니다.\r\n" + 
						 "\r\n" + 
						 "결제금액: " + data.getString("payment") + "원 \r\n" +
						 "가상계좌: 신한 " + data.getString("loanAccntNo") + "\r\n" +
						 "예금주: " + data.getString("name") + "_크레파스\r\n" + 
						 "\r\n" + 
						 "감사합니다.";
	    		break;
	    		
	    	case 6:
	    		result = "[청년5.5] " + data.getString("name") + "님의 청년지원 투자신청이 완료되었습니다. 펀딩금액 100% 모금 완료시 대출이 실행되며 모금 미완료시 신청금은 예치금 계좌로 환급됩니다.\r\n" + 
	    				"\r\n" + 
	    				"▶펀딩명 : [" + data.getString("title") + "]\r\n" + 
	    				"▶ 신청금 : " + data.getString("payment") + "원";
	    		break;
	    		
	    	case 7:
				result = data.getString("name") + "님의 " + data.getString("payment") + "원 대출신청이 정상적으로 접수 되었습니다.- 청년5.5";
	    		break;
	    		
	    	case 8:
	    		result = data.getString("name") + "님께서 투자하신 [" + data.getString("title") + "]의 "+data.getString("count")+"회차 정산이 완료되었습니다.\r\n" + 
						 "▷청년 5.5 > 三(왼쪽상단) > 투자정보 > 정산현황";
	    		break;
	    		
	    	case 9:
				result = "[" + data.getString("title") + "]에 " + data.getString("name") + "님의 " + data.getString("payment") + "원 자동투자신청이 완료되었습니다.\r\n" + 
						 "-청년5.5";
	    		break;
	    		
	    	case 10:
				result = "[청년5.5] " + data.getString("name") + "님께서 지원해주신 펀딩은 모금 완료되어 " + data.getString("date") + "에 대출이 시작되었습니다 \r\n" + 
						"‘메뉴>투자정보’에서 확인할 수 있습니다.\r\n" + 
						"\r\n" + 
						"▶펀딩명 : [" + data.getString("title") + "]\r\n" + 
						"▶ 신청금 : " + data.getString("payment") + "원";
	    		break;
	    		
	    	case 11:
	    		result = "[청년5.5] " + data.getString("name") + "님께서 지원해주신 펀딩이 모금 미완료되어 신청금을 예치금 계좌로 환급했습니다.\r\n" + 
	    				"‘메뉴>예치금관리’에서 확인할 수 있습니다.\r\n" + 
						"\r\n" + 
						"▶펀딩명 : [" + data.getString("title") + "]\r\n" + 
						"▶ 신청금 : " + data.getString("payment") + "원";
	    		break;
	    		
	    	case 12:
	    		result = data.getString("name") + "님께서 예치금 가상계좌로 입금하신 " + data.getString("trAmt") + "원이 정상적으로 처리 되었습니다.- 청년5.5.\r\n" 
	    				;
	    		break;
    	}
    	
    	return result;
    }
    
    public String getFormSMSArray(int type, JSONArray data) throws JSONException {
    	String result = "";
    	
    	switch(type) {
	    	case 9:
	    		String investMsg = "";
	    		String itemMsg = "";
	    		
	    		for(int i = 0; i < data.length(); i++) {
	    			investMsg = "[청년5.5] " + data.getJSONObject(i).getString("name") + "님의 청년지원 투자신청이 완료되었습니다. 펀딩금액 100% 모금 완료시 대출이 실행되며 모금 미완료시 신청금은 예치금 계좌로 환급됩니다.\r\n"; 
	    			itemMsg += "\r\n" + "▶펀딩명 : [" + data.getJSONObject(i).getString("title") + "]\r\n" + 
		    				"▶ 신청금 : " + data.getJSONObject(i).getString("payment") + "원";
	    		}
	    		
	    		result = investMsg + itemMsg;
	    		break;
    	}
    	
    	return result;
    }
    
    public boolean setRequestSMSData(String name, String type, String custId, String phonNumber, String msg) throws Exception {
    	if(phonNumber.equals("01091619577"))
    		return true;
    	
    	jsonRequest = new JSONObject();
		jsonRequest.put("phone", phonNumber);
		jsonRequest.put("callback", "15222975");
		jsonRequest.put("reqdate", "");
		jsonRequest.put("msg", "청년 5.5");
		jsonRequest.put("template_code", "T0000");
		jsonRequest.put("failed_type", "LMS");
		jsonRequest.put("failed_subject", "청년 5.5");
		jsonRequest.put("failed_msg", msg);
		jsonRequest.put("btn_types", "웹링크");
		jsonRequest.put("btn_txts", "투자하기");
		jsonRequest.put("btn_urls1", "https://young55.crepass.com");
		
		
		int duplicationCheck = notifyScheService.selectDuplicationCheck(name, "L", custId, type, getMD5(jsonRequest.toString()));
		
		if(duplicationCheck > 0) {
			notifyScheService.insertSendSMSFail(name, "L", custId, type, jsonRequest.toString());
			return true;
		}
		
		return notifyScheService.insertSendSMS(name, "L", custId, type, jsonRequest.toString());
    }
    
    
    public void sendCheckManager(List<OneSendCheckingEmail> oneSendCheckingEmailList, String subject) {	
		try {
			
				String emailBody = "";
				
				if(oneSendCheckingEmailList.size() > 0) {
					emailBody += setHtmlTableFormHeader("갯수", oneSendCheckingEmailList.size());
					for(int i = 0; i < oneSendCheckingEmailList.size(); i++) {
						OneSendCheckingEmail oneSendCheckingEmail = oneSendCheckingEmailList.get(i);
//						emailBody += setHtmlTableFormBody(oneSendCheckingEmail.getLoanId(), oneSendCheckingEmail.getRepayCount(), oneSendCheckingEmail.getRepayDate(), oneSendCheckingEmail.getPayAmount(), oneSendCheckingEmail.getLnAmount(), oneSendCheckingEmail.getInterestAmount(), oneSendCheckingEmail.getBalance());
						emailBody += oneSendCheckingEmail.toString();
						if(i != oneSendCheckingEmailList.size() - 1)
							emailBody += "<br>";
					}
					emailBody += setHtmlTableFormFooter();
				}
				
				
				if(emailBody.length() > 0)
					sendLoggingEmailTest(subject, emailBody, "jhlee@crepass.com");
		} catch(Exception e) {
			sendBatchLogging("sendRepaymentManager", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
		}
	}
    
    
    
    public void sendLoggingEmail(String title, String emailBody) {
		try {
			List<OneSendEmailInfo> oneSendEmailInfos = new ArrayList<>();
	        
	        long seq = System.currentTimeMillis();
	        if(notifyScheService.insertEmailHistory(String.valueOf(seq), title, emailBody)) {
	        	OneSendEmailInfo oneSendEmailInfo = new OneSendEmailInfo();
		        oneSendEmailInfo.setEmailFk(seq);
		        oneSendEmailInfo.setFrom("info@crepass.com");
		        oneSendEmailInfo.setTo("tech@crepass.com");
		        oneSendEmailInfos.add(oneSendEmailInfo);
		        notifyScheService.insertEmailTarget(oneSendEmailInfos);
	        }
			
		} catch(Exception e) {
			sendBatchLogging("sendLoggingEmail", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
		}
	}
    
    public void sendLoggingEmail2(String title, String emailBody) {
		try {
	        List<OneSendEmailInfo> oneSendEmailInfos = new ArrayList<>();
	        
	        long seq = System.currentTimeMillis();
	        if(notifyScheService.insertEmailHistory(String.valueOf(seq), title, emailBody)) {
	        	OneSendEmailInfo oneSendEmailInfo = new OneSendEmailInfo();
		        oneSendEmailInfo.setEmailFk(seq);
		        oneSendEmailInfo.setFrom("info@crepass.com");
		        oneSendEmailInfo.setTo("tech@crepassplus.com");
//		         oneSendEmailInfo.setTo("jhlee@crepass.com");
		        oneSendEmailInfos.add(oneSendEmailInfo);
		         
		        oneSendEmailInfo = new OneSendEmailInfo();
		        oneSendEmailInfo.setEmailFk(seq);
		        oneSendEmailInfo.setFrom("info@crepass.com");
		        oneSendEmailInfo.setTo("op@crepassplus.com");
//		         oneSendEmailInfo.setTo("igothewar@naver.com");
		        oneSendEmailInfos.add(oneSendEmailInfo);
		        notifyScheService.insertEmailTarget(oneSendEmailInfos);
	        }
	        
		} catch(Exception e) {
			sendBatchLogging("sendLoggingEmail2", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
		}
	}
    
    public void sendLoggingEmail3(String title, String emailBody, String mid) {
    	try {
	        List<OneSendEmailInfo> oneSendEmailInfos = new ArrayList<>();
	        
	        long seq = System.currentTimeMillis();
	        if(notifyScheService.insertEmailHistory(String.valueOf(seq), title, emailBody)) {
	        	OneSendEmailInfo oneSendEmailInfo = new OneSendEmailInfo();
		        oneSendEmailInfo.setEmailFk(seq);
		        oneSendEmailInfo.setFrom("info@crepass.com");
		        oneSendEmailInfo.setTo(mid);
		        oneSendEmailInfos.add(oneSendEmailInfo);
		        notifyScheService.insertEmailTarget2(oneSendEmailInfos);
	        }
	        
		} catch(Exception e) {
			sendBatchLogging("sendLoggingEmail3", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
		}
    }
    
    public void sendLoggingEmail4(String title, String emailBody) {
    	try {
	        List<OneSendEmailInfo> oneSendEmailInfos = new ArrayList<>();
	        
	        long seq = System.currentTimeMillis();
	        if(notifyScheService.insertEmailHistory(String.valueOf(seq), title, emailBody)) {
	        	OneSendEmailInfo oneSendEmailInfo = new OneSendEmailInfo();
		        oneSendEmailInfo.setEmailFk(seq);
		        oneSendEmailInfo.setFrom("info@crepass.com");
		        oneSendEmailInfo.setTo("op@crepassplus.com");
		        oneSendEmailInfos.add(oneSendEmailInfo);
		        notifyScheService.insertEmailTarget(oneSendEmailInfos);
	        }
	        
		} catch(Exception e) {
			sendBatchLogging("sendLoggingEmail4", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
		}
    }
    
    // 단체메일 발송용
    public void sendLoggingEmail5(String title, String wishdate, List<OneSendEmail> oneSendEmailList, String emailBody) {
    	try {
	        //List<OneSendEmailInfo> oneSendEmailInfos = new ArrayList<>();
	        
	        for(int i=0; i<oneSendEmailList.size(); i++) {
	        	long seq = System.currentTimeMillis();
	        	
		        if(notifyScheService.insertEmailHistory(String.valueOf(seq), title, emailBody)) {
		        	OneSendEmailInfo oneSendEmailInfo = new OneSendEmailInfo();
			        oneSendEmailInfo.setEmailFk(seq);
			        oneSendEmailInfo.setFrom("info@crepass.com");
			        oneSendEmailInfo.setTo(oneSendEmailList.get(i).getM_id());
			        oneSendEmailInfo.setSend_dt(wishdate);
			        // oneSendEmailInfos.add(oneSendEmailInfo);
			        notifyScheService.insertEmailReserveTarget(oneSendEmailInfo);
			        
		        }
	        }
		} catch(Exception e) {
			sendBatchLogging("sendLoggingEmail4", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
		}
    }

    // 이메일 인증용 
    public void sendLoggingEmail6(String title, String emailBody, String target) {
    	try {
	        List<OneSendEmailInfo> oneSendEmailInfos = new ArrayList<>();
	        
	        long seq = System.currentTimeMillis();
	        if(notifyScheService.insertEmailHistory(String.valueOf(seq), title, emailBody)) {
	        	OneSendEmailInfo oneSendEmailInfo = new OneSendEmailInfo();
		        oneSendEmailInfo.setEmailFk(seq);
		        oneSendEmailInfo.setFrom("info@crepass.com");
		        oneSendEmailInfo.setTo(target);
		        oneSendEmailInfos.add(oneSendEmailInfo);
		        notifyScheService.insertEmailTarget(oneSendEmailInfos);
	        }
	        
		} catch(Exception e) {
			sendBatchLogging("sendLoggingEmail6", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
		}
    }

    
    
    public void sendLoggingEmailTech(String title, String emailBody) {
		try {
	        List<OneSendEmailInfo> oneSendEmailInfos = new ArrayList<>();
	        
	        long seq = System.currentTimeMillis();
	        if(notifyScheService.insertEmailHistory(String.valueOf(seq), title, emailBody)) {
	        	OneSendEmailInfo oneSendEmailInfo = new OneSendEmailInfo();
		        oneSendEmailInfo.setEmailFk(seq);
		        oneSendEmailInfo.setFrom("info@crepass.com");
		        oneSendEmailInfo.setTo("tech@crepass.com");
		        //oneSendEmailInfo.setTo("jhlee@crepass.com");
		        oneSendEmailInfos.add(oneSendEmailInfo);
		        
//		        oneSendEmailInfo = new OneSendEmailInfo();
//		        oneSendEmailInfo.setEmailFk(seq);
//		        oneSendEmailInfo.setFrom("info@crepass.com");
//		        //oneSendEmailInfo.setTo("op@crepass.com");
//		        oneSendEmailInfo.setTo("igothewar@naver.com");
//		        oneSendEmailInfos.add(oneSendEmailInfo);
		        notifyScheService.insertEmailTarget(oneSendEmailInfos);
	        }
	        
		} catch(Exception e) {
			sendBatchLogging("sendLoggingEmailTech", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
		}
	}
    
    public void sendLoggingEmailAPIManager(String title, String emailBody) {
		try {
	        List<OneSendEmailInfo> oneSendEmailInfos = new ArrayList<>();
	        
	        long seq = System.currentTimeMillis();
	        if(notifyScheService.insertEmailHistory(String.valueOf(seq), title, emailBody)) {
	        	OneSendEmailInfo oneSendEmailInfo = new OneSendEmailInfo();
		        oneSendEmailInfo.setEmailFk(seq);
		        oneSendEmailInfo.setFrom("info@crepass.com");
		        oneSendEmailInfo.setTo("jhlee@crepass.com");
		        oneSendEmailInfos.add(oneSendEmailInfo);
		        
		        notifyScheService.insertEmailTarget(oneSendEmailInfos);
	        }
	        
		} catch(Exception e) {
			sendBatchLogging("sendLoggingEmailTech", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
		}
	}
    
    
    public void sendLoggingEmailTest(String title, String emailBody, String target) {
		try {
	        List<OneSendEmailInfo> oneSendEmailInfos = new ArrayList<>();
	        
	        long seq = System.currentTimeMillis();
	        if(notifyScheService.insertEmailHistory(String.valueOf(seq), title, emailBody)) {
	        	OneSendEmailInfo oneSendEmailInfo = new OneSendEmailInfo();
		        oneSendEmailInfo.setEmailFk(seq);
		        oneSendEmailInfo.setFrom("info@crepass.com");
//		        oneSendEmailInfo.setTo("tech@crepass.com");
		        oneSendEmailInfo.setTo(target);
		        oneSendEmailInfos.add(oneSendEmailInfo);
		        
		        notifyScheService.insertEmailTarget(oneSendEmailInfos);
	        }
	        
		} catch(Exception e) {
			sendBatchLogging("sendLoggingEmailTest", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
		}
	}
    
    public String setHtmlInfoMailForm_temp() {
			String result = "";
			    	
    	try {
    		result = "<!doctype html>\r\n" + 
    				"<html lang='ko'>\r\n" + 
    				"<head>\r\n" + 
    				"<meta charset='utf-8'>\r\n" + 
    				"<meta http-equiv='content-type' content='text/html; charset=utf-8'>\r\n" + 
    				"<meta http-equiv='Content-Script-Type' content='text/javascript'>\r\n" + 
    				"<meta http-equiv='Content-Style-Type' content='text/css'>\r\n" + 
    				"<meta http-equiv='X-UA-Compatible' content='IE=edge'>\r\n" + 
    				"<title>온라인서비스 이용약관 및 투자 이용약관 개정 안내</title>\r\n" + 
    				"</head>\r\n" + 
    				"<body style='width:100%; height:100%; background:#f6f6f6;'>\r\n" + 
    				"<div class='Wrap' style='width:800px; margin:0 auto; padding:50px; background:#fff'>\r\n" + 
    				"\r\n" + 
    				"<div class='main_info'>\r\n" + 
    				"<img width='200' src='https://i0.wp.com/crepass.com/wp-content/uploads/2019/11/cropped-unnamed-1.png?fit=726%2C136&amp;ssl=1'>\r\n" + 
    				"<p class='maintitle' style='letter-spacing: -1.5px; font-size:25pt;padding-top:30px; font-family: 나눔고딕;'>크레파스 이메일 인증 메일입니다.</p>\r\n" + 
    				"<p class='hi' style='line-height:35px; font-size:14pt; padding-bottom:50px; padding-top:30px'>안녕하세요. P2P금융 크레파스 솔루션입니다. <br>\r\n" + 
    				"크레파스 회원가입 신청을 하셨습니다.<br>\r\n" + 
    				"등록자 본인 확인과 보안을 위해 하단의 인증링크를 클릭하여 계정인증을 해주시기 바랍니다.</p>\r\n" + 
    				"\r\n" + 
    				"<a href='http://solutiondev.crepass.com/crapas/?cms=senddingemail&mid=jhlee@crepass.com&emailToken=b7212g975n7h274009i8xu6r2n8b1z&validationDt=20200727133910'>인증링크</a>\r\n" + 
    				"</div>\r\n" + 
    				"\r\n" + 
    				"\r\n" + 
    				"\r\n" + 
    				"</div>\r\n" + 
    				"\r\n" + 
    				"<div class='service_info' style='width:800px; margin:0 auto; text-align:center; padding-bottom:20px; line-height:30px; font-size:17px; color:#666666'>\r\n" + 
    				"<p class='hi'><span style='font-size: 16px;'><br></span></p><p class='hi'>\r\n" + 
    				"<span style='font-size: 16px;'>본 메일은 법령에 따른 통지의무를 위해 회원님의 수신동의 여부와 무관하게 모든 회원님들께 발송됩니다.</span><br><span style='font-size: 16px;'>\r\n" + 
    				"제 3자가 본인의 이메일 주소를 잘못 입력할 경우 타인의 메일이 전송될 수 있습니다.</span><br>\r\n" + 
    				"</p></div>\r\n" + 
    				"<div class='footer' style='width:800px; margin:0 auto; text-align:center; font-size:13px; line-height:25px; padding-bottom:50px; color:#999999'>\r\n" + 
    				"<span>크레파스솔루션(주) 서울시 영등포구 여의나루로 53-1, 905호 대표전화 : 02-1522-2975 Email : info@crepass.com <br>\r\n" + 
    				"Copyright (C) 2020 CrePASS All rights reserved. </span></div>\r\n" + 
    				"<p><br></p>\r\n" + 
    				"</body>\r\n" + 
    				"</html>";
			    		
    	} catch(Exception e) {
    		result = "";
    		sendBatchLogging("setHtmlTableForm", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
    	}
    	
		return result;
    }
    
    
    public String setHtmlInfoMailForm() {
	String result = "";
    	
    	try {
    		result = "\r\n" + 
    				"<!doctype html>\r\n" + 
    				"<html lang='ko'>\r\n" + 
    				"    <head>\r\n" + 
    				"\r\n" + 
    				"	<meta charset='utf-8'>\r\n" + 
    				"	<meta http-equiv='content-type' content='text/html; charset=utf-8'>\r\n" + 
    				"	<meta http-equiv='Content-Script-Type' content='text/javascript'>\r\n" + 
    				"	<meta http-equiv='Content-Style-Type' content='text/css'>\r\n" + 
    				"	<meta http-equiv='X-UA-Compatible' content='IE=edge'>\r\n" + 
    				"	<title>온라인서비스 이용약관 및 투자 이용약관 개정 안내</title>\r\n" + 
    				"    </head>\r\n" + 
    				"\r\n" + 
    				"    <body style='width:100%; height:100%; background:#f6f6f6;'>\r\n" + 
    				"        <div class='Wrap' style='width:800px; margin:0 auto; padding:50px; background:#fff'>\r\n" + 
    				"            \r\n" + 
    				"            <div class='main_info'>\r\n" + 
    				"                    <img width='200' src='https://i0.wp.com/crepass.com/wp-content/uploads/2019/11/cropped-unnamed-1.png?fit=726%2C136&amp;ssl=1'>\r\n" + 
    				"                    <p class='maintitle' style='letter-spacing: -1.5px; font-size:25pt;padding-top:30px; font-family: 나눔고딕;'>온라인서비스 이용약관 및 투자 이용약관 개정 안내</p>\r\n" + 
    				"                    <p class='hi' style='line-height:35px; font-size:14pt; padding-bottom:50px; padding-top:30px'>안녕하세요. P2P금융 크레파스 솔루션입니다. <br>\r\n" + 
    				"                        크레파스 솔루션을 이용해주시는 고객 여러분께 감사의 말씀드리며, 온라인서비스 이용약관 및 투자 이용약관 개정에 관해 안내드립니다. 새롭게 바뀌는 약관의 변경사항을 확인하시어 서비스<br>\r\n" + 
    				"                        이용에 참고하시기 바랍니다. 변경된 약관은 2020년 4월 20일부터 효력이 발생합니다.</p>\r\n" + 
    				"                </div>\r\n" + 
    				"            <div class='Contents'>\r\n" + 
    				"                    <h2>1. 개정 약관 시행일 : 2020년 4월 20일</h2>\r\n" + 
    				"                    <h2>2. 주요 변경사항<br></h2>\r\n" + 
    				"                    \r\n" + 
    				"            <div class='1' style='width:800px;'>\r\n" + 
    				"                        <p style='font-size:15pt; padding-top:20px;'> - 서비스 이용약관</p>\r\n" + 
    				"                        \r\n" + 
    				"            <table border='1' cellspacing='0' cellpadding='0' style='word-break: break-all; width: 800px; border: 1px none #999999; border-collapse: collapse; height: 533px;'> \r\n" + 
    				"                <tbody>\r\n" + 
    				"                    <tr style='border: 1px solid #999999; width: 400px; height: 57px;'>\r\n" + 
    				"                        <td class='title' style='border: 1px solid #999999; width: 400px; background:#f6f6f6; vertical-align:middle; padding:10px; vertical-align:top'>\r\n" + 
    				"                            <p style='text-align: center; line-height: 1.6; margin-top: 0px; margin-bottom: 0px; text-align:center;'><span style='text-align: center; white-space: nowrap; font-size: 17px;'>개정 전</span><br></p>\r\n" + 
    				"                                    \r\n" + 
    				"                        </td>\r\n" + 
    				"                        <td class='title' style='border: 1px solid #999999; width: 400px; background:#f6f6f6; vertical-align:middle; padding:10px; vertical-align:top'>\r\n" + 
    				"                            <p style='font-weight:bold; text-align: center; line-height: 1.6; margin-top: 0px; margin-bottom: 0px; text-align:center;'><span style='text-align: center; white-space: nowrap; font-size: 17px;'></span>개정 후</span><br></p>\r\n" + 
    				"                                    \r\n" + 
    				"                        </td>\r\n" + 
    				"                    </tr>\r\n" + 
    				"                    <tr style='border: 1px solid #999999; width: 400px; height: 57px;'>\r\n" + 
    				"                        <td class='text' style='padding:10px; vertical-align:top; padding:10px; vertical-align:top'>\r\n" + 
    				"                            <p style='line-height: 1.6; margin-top: 0px; margin-bottom: 0px; text-align: left;'><span style='font-size: 18px;'>제3조 (용어의 정의)<br><br>\r\n" + 
    				"                                            4. <font class='impact' style='font-weight:bold; text-decoration: underline; color:#555555'>투자자</font>: 사이트에 공시된 채권에 대해 투자 신청을 한 회원을 말한다.<br>\r\n" + 
    				"                                            5. <font class='impact' style='font-weight:bold; text-decoration: underline; color:#555555'>채무자</font>: 자금을 빌릴 의사가 있는 자로 온라인으로 제공하는 양식에 맞추어 대출 신청을 한 회원을 말한다.</span></p>\r\n" + 
    				"                                    \r\n" + 
    				"                        </td>\r\n" + 
    				"                        <td class='text' style='padding:10px; vertical-align:top; padding:10px; vertical-align:top'>\r\n" + 
    				"                            <p style='line-height: 1.6; margin-top: 0px; margin-bottom: 0px; text-align: left;'><span style='font-size: 18px;'>제3조 (용어의 정의)<br><br>\r\n" + 
    				"                                            4.  <font class='impact2' style='font-weight:bold; text-decoration: underline; color:#111111'>투자회원</font>: 사이트에 공시된 채권에 대해 투자 신청을 한 회원을 말한다.<br>\r\n" + 
    				"                                            5.  <font class='impact2' style='font-weight:bold; text-decoration: underline; color:#111111'>대출회원</font>: 자금을 빌릴 의사가 있는 자로 온라인으로 제공하는 양식에 맞추어 대출 신청을 한 회원을 말한다.</span></p>\r\n" + 
    				"                                    \r\n" + 
    				"                        </td>\r\n" + 
    				"                    </tr>\r\n" + 
    				"                    <tr style='border: 1px solid #999999; width: 400px; height: 57px;'>\r\n" + 
    				"                        <td class='text' style='padding:10px; vertical-align:top; padding:10px; vertical-align:top'>\r\n" + 
    				"                            <p style='line-height: 1.6; margin-top: 0px; margin-bottom: 0px; text-align: left;'><span style='font-size: 18px;'>제22조 (이용료 등)<br><br>\r\n" + 
    				"                                            2. <font class='impact' style='font-weight:bold; text-decoration: underline; color:#555555'>“대출서비스 이용수수료”</font>는 대출이 실행되었을때, '여신회사'가 “대출회원”에게서 여신심사 대행 및 여신실행대행에 대한 보수로서 대출금액의\r\n" + 
    				"                                            최대 3%이내에 해당하는 금액을 선취할 수 있다.<br><br>\r\n" + 
    				"                                            3. 투자 수수료는 투자회원과 '여신회사' 사이에 대출정보를 중개하는 업무에 대하여 지급하는 “대출정보중개 수수료”와 투자회원이 '원리금수취권'을 \r\n" + 
    				"                                            양수한 '대출채권'을 관리하는 업무에 대하여 지급하는 <font class='impact' style='font-weight:bold; text-decoration: underline; color:#555555'>“관리수수료”</font>로 구분된다.<br><br>\r\n" + 
    				"                                            5. <font class='impact' style='font-weight:bold; text-decoration: underline; color:#555555'>“관리수수료”</font>는 대출회원의 대출원리금이 매월 정상 상환된 경우 '여신회사'는 이자소득에 대하여 원천징수 세액을 선 공제 후 나머지 금액\r\n" + 
    				"                                            (대출원리금 - 원천징수세액)에서 지급 직전 투자금 잔액의 약정된 <font class='impact' style='font-weight:bold; text-decoration: underline; color:#555555'>“관리수수료”</font>를 수취하고 투자회원에게 원리금을 지급한다.<br>\r\n" + 
    				"                                        </span></p>\r\n" + 
    				"                                    \r\n" + 
    				"                        </td>\r\n" + 
    				"                        <td class='text' style='padding:10px; vertical-align:top; padding:10px; vertical-align:top'>\r\n" + 
    				"                            <p style='line-height: 1.6; margin-top: 0px; margin-bottom: 0px; text-align: left;'><span style='font-size: 18px;'>제22조 (이용료 등)<br><br>\r\n" + 
    				"                                            2. <font class='impact2' style='font-weight:bold; text-decoration: underline; color:#111111'>“플랫폼이용수수료”</font>는 대출이 실행되었을때, '여신회사'가 “대출회원”에게서 여신심사 대행 및 여신실행대행에 대한 보수로서 대출금액의 최대 3%이내에 해당하는 금액을\r\n" + 
    				"                                            선취할 수 있다.<br><br>\r\n" + 
    				"                                            3. 투자 수수료는 투자회원과 '여신회사' 사이에 대출정보를 중개하는 업무에 대하여 지급하는 “대출정보중개 수수료”와 투자회원이 '원리금수취권'을 양수한 '대출채권'을 \r\n" + 
    				"                                            관리하는 업무에 대하여 지급하는 <font class='impact2' style='font-weight:bold; text-decoration: underline; color:#111111'>“플랫폼이용수수료”</font>로 구분된다.<br><br>\r\n" + 
    				"                                            5. “플랫폼 이용수수료'는 대출회원의 대출원리금이 매월 정상 상환된 경우 '여신회사'는 이자소득에 대하여 원천징수 세액을 선 공제 후 나머지 금액(대출원리금 - 원천징수세액)\r\n" + 
    				"                                            에서 지급 직전 투자금 잔액의 약정된 <font class='impact2' style='font-weight:bold; text-decoration: underline; color:#111111'>“플랫폼이용수수료”</font>를 수취하고 투자회원에게 원리금을 지급한다.<br>\r\n" + 
    				"                                        </span></p>\r\n" + 
    				"                                    \r\n" + 
    				"                        </td>\r\n" + 
    				"                    </tr>\r\n" + 
    				"                </tbody>\r\n" + 
    				"            </table>\r\n" + 
    				"                    </div>\r\n" + 
    				"            <div class='2'>\r\n" + 
    				"                        <p style='font-size:15pt; padding-top:20px;'> - 투자 이용약관</p>\r\n" + 
    				"                        \r\n" + 
    				"            <table border='1' cellspacing='0' cellpadding='0' style='word-break: break-all; width: 800px; border: 1px none #999999; border-collapse: collapse; height: 533px;'>\r\n" + 
    				"                <tbody>\r\n" + 
    				"                    <tr style='border: 1px solid #999999; width: 400px; height: 57px;'>\r\n" + 
    				"                        <td class='title' style='border: 1px solid #999999; width: 400px; background:#f6f6f6; vertical-align:middle; padding:10px; vertical-align:top'>\r\n" + 
    				"                            <p style='text-align: center; line-height: 1.6; margin-top: 0px; margin-bottom: 0px; text-align:center;'><span style='text-align: center; white-space: nowrap; font-size: 17px;'>개정 전</span><br></p>\r\n" + 
    				"                                    \r\n" + 
    				"                        </td>\r\n" + 
    				"                        <td class='title' style='border: 1px solid #999999; width: 400px; background:#f6f6f6; vertical-align:middle; padding:10px; vertical-align:top'>\r\n" + 
    				"                            <p style='text-align: center; line-height: 1.6; margin-top: 0px; margin-bottom: 0px; text-align:center;'><span style='text-align: center; white-space: nowrap; font-size: 17px;'><strong>개정 후</strong></span><br></p>\r\n" + 
    				"                                    \r\n" + 
    				"                        </td>\r\n" + 
    				"                    </tr>\r\n" + 
    				"                    <tr style='border: 1px solid #999999; width: 400px; height: 57px;'>\r\n" + 
    				"                        <td class='text' style='padding:10px; vertical-align:top; padding:10px; vertical-align:top'>\r\n" + 
    				"                            <p style='line-height: 1.6; margin-top: 0px; margin-bottom: 0px; text-align: left;'><span style='font-size: 18px;'>본 약관은 크레파스솔루션 주식회사(이하 “회사”)가 운영하는 “사이트”(이하 “사이트”)에서 제공하는 금융 서비스 및 기타 정보서비스(이하 “서비스”)<font class='impact' style='font-weight:bold; text-decoration: underline; color:#555555'>와 관련하여</font> \r\n" + 
    				"                                            회사와 귀하 간의 권리와 의무, 책임 사항 규정을 목적으로 합니다. 회사는 시스템에 관한 제반 기술과 운영에 대한 모든 권한을 갖고 있으며, \r\n" + 
    				"                                            회원에게 제공하는 여신업무는 회사와 제휴된 여신회사<font class='impact' style='font-weight:bold; text-decoration: underline; color:#555555'>(이하 “여신회사”)</font>가 전담합니다.</span></p>\r\n" + 
    				"                                    \r\n" + 
    				"                        </td>\r\n" + 
    				"                        <td class='text' style='padding:10px; vertical-align:top; padding:10px; vertical-align:top'>\r\n" + 
    				"                            <p style='line-height: 1.6; margin-top: 0px; margin-bottom: 0px; text-align: left;'><span style='font-size: 18px;'>본 약관은 크레파스솔루션 주식회사(이하 “회사”)가 운영하는 “사이트”(이하 “사이트”)에서 제공하는 금융 서비스 및 기타 정보서비스(이하 “서비스”), \r\n" + 
    				"                                            <font class='impact2' style='font-weight:bold; text-decoration: underline; color:#111111'>“회사”와 연계하여 대출실행 업무를 수행하는 크레파스대부 주식회사(이하 “여신회사”) 와</font> 귀하 간의 권리와 의무, 책임 사항 규정을 목적으로 합니다. 회사는 \r\n" + 
    				"                                            시스템에 관한 제반 기술과 운영에 대한 모든 권한을 갖고 있으며, 회원에게 제공하는 여신업무는 회사와 제휴된 “여신회사”가 전담합니다.</span></p>\r\n" + 
    				"                                    \r\n" + 
    				"                        </td>\r\n" + 
    				"                    </tr>\r\n" + 
    				"                    <tr style='border: 1px solid #999999; width: 400px; height: 57px;'>\r\n" + 
    				"                        <td class='text' style='padding:10px; vertical-align:top; padding:10px; vertical-align:top'>\r\n" + 
    				"                            <p style='line-height: 1.6; margin-top: 0px; margin-bottom: 0px; text-align: left;'><span style='font-size: 18px;'>제1조 (계약의 목적)<br><br>\r\n" + 
    				"                                            본 약관은 “회사”가 운영하는 “사이트”를 통하여 제공하는 '서비스'에서 '회사'와 제휴 계약을 통해 여신업무를 전담하는 <font class='impact' style='font-weight:bold; text-decoration: underline; color:#555555'>크레파스대부 주식회사 (이하 '여신회사')</font>와 \r\n" + 
    				"                                            '투자회원' 간 체결하는 대출참가 계약(이하 '투자계약')의 기본사항을 정하여 당사자 상호간의 원활하고 공정한 계약상의 권리 의무관계를 규율 함을 목적으로 한다.</span></p>\r\n" + 
    				"                                    \r\n" + 
    				"                        </td>\r\n" + 
    				"                        <td class='text' style='padding:10px; vertical-align:top; padding:10px; vertical-align:top'>\r\n" + 
    				"                            <p style='line-height: 1.6; margin-top: 0px; margin-bottom: 0px; text-align: left;'><span style='font-size: 18px;'>제1조 (계약의 목적)<br><br>\r\n" + 
    				"                                            본 약관은 “회사”가 운영하는 “사이트”를 통하여 제공하는 '서비스'에서 '회사'와 제휴 계약을 통해 여신업무를 전담하는 <font class='impact2' style='font-weight:bold; text-decoration: underline; color:#111111'>'여신회사'</font>와 '투자회원' 간 체결하는 대출참가 \r\n" + 
    				"                                            계약(이하 '투자계약')의 기본사항을 정하여 당사자 상호간의 원활하고 공정한 계약상의 권리 의무관계를 <font class='impact2' style='font-weight:bold; text-decoration: underline; color:#111111'>규정</font> 함을 목적으로 한다.\r\n" + 
    				"                                        </span></p>\r\n" + 
    				"                                    \r\n" + 
    				"                        </td>\r\n" + 
    				"                    </tr>\r\n" + 
    				"                    <tr style='border: 1px solid #999999; width: 400px; height: 57px;'>\r\n" + 
    				"                        <td class='text' style='padding:10px; vertical-align:top; padding:10px; vertical-align:top'>\r\n" + 
    				"                            <p style='line-height: 1.6; margin-top: 0px; margin-bottom: 0px; text-align: left;'><span style='font-size: 18px;'>제5조 (원리금 지급 및 미보장)<br><br>\r\n" + 
    				"                                            1. '대출회원'이 '여신회사'에게 '대출계약'에서 정한 상환일(매월 5일, 15일, 25일 중 하루, 이하 '대출 상환일')에 원리금을 상환하는 경우, <font class='impact' style='font-weight:bold; text-decoration: underline; color:#555555'>'여신회사'는 대출 \r\n" + 
    				"                                            상환일로부터 5영업일이 경과한 날에</font> '투자회원'에게 '대출채권'에 대한 '투자회원'의 '투자금'에 비례하는 원리금을 지급한다. 단, '대출 상환일'이 \r\n" + 
    				"                                            공휴일(대체 공휴일 포함, 이하 같음)인 경우에는 이후 도래하는 첫 영업일을 '대출 상환일'로 보며, <font class='impact' style='font-weight:bold; text-decoration: underline; color:#555555'>대출 상환일로부터 5 영업일이 경과한 날</font>이 공휴일인 경우 \r\n" + 
    				"                                            이후 도래하는 첫 영업일에 '투자회원'에게 원리금을 지급한다.</span></p>\r\n" + 
    				"                                    \r\n" + 
    				"                        </td>\r\n" + 
    				"                        <td class='text' style='padding:10px; vertical-align:top; padding:10px; vertical-align:top'>\r\n" + 
    				"                            <p style='line-height: 1.6; margin-top: 0px; margin-bottom: 0px; text-align: left;'><span style='font-size: 18px;'>제5조 (원리금 지급 및 미보장)<br><br>\r\n" + 
    				"                                            1. '대출회원'이 '여신회사'에게 '대출계약'에서 정한 상환일(매월 5일, 15일, 25일 중 하루, 이하 '대출 상환일')에 원리금을 상환하는 경우, <font class='impact2' style='font-weight:bold; text-decoration: underline; color:#111111'>“여신회사”가 정하는 \r\n" + 
    				"                                            상환처리기간에 따라</font> '투자회원'에게 '대출채권'에 대한 '투자회원'의 '투자금'에 비례하는 원리금을 지급한다. 단, '대출 상환일'이 공휴일\r\n" + 
    				"                                            (대체 공휴일 포함, 이하 같음)인 경우에는 이후 도래하는 첫 영업일을 '대출 상환일'로 보며, <font class='impact2' style='font-weight:bold; text-decoration: underline; color:#111111'>“여신회사”가 정한 상환처리기간</font>이 공휴일인 경우 이후 도래하는 \r\n" + 
    				"                                            첫 영업일에 '투자회원'에게 원리금을 지급한다.\r\n" + 
    				"                                        </span></p>\r\n" + 
    				"                                    \r\n" + 
    				"                        </td>\r\n" + 
    				"                    </tr>\r\n" + 
    				"                    <tr style='border: 1px solid #999999; width: 400px; height: 57px;'>\r\n" + 
    				"                        <td class='text' style='padding:10px; vertical-align:top; padding:10px; vertical-align:top'>\r\n" + 
    				"                            <p style='line-height: 1.6; margin-top: 0px; margin-bottom: 0px; text-align: left;'><span style='font-size: 18px;'>제6조 (투자 수수료)<br><br>\r\n" + 
    				"                                            2. 투자 수수료는 '투자회원'과 '여신회사' 사이에 대출정보를 중개하는 업무에 대하여 지급하는 “대출정보중개 수수료”와 '투자회원'이 '원리금수취권'을 양수한\r\n" + 
    				"                                            '대출채권'을 관리하는 업무에 대하여 지급하는 <font class='impact' style='font-weight:bold; text-decoration: underline; color:#555555'>“관리 수수료”</font>로 구분된다.<br><br><br><br><br>\r\n" + 
    				"                                            3. '대출회원'의 대출원리금이 매월 정상 상환된 경우 '여신회사'는 이자소득에 대하여 원천징수 세액을 선 공제 후 나머지 금액(대출원리금 - 원천징수세액)에서 \r\n" + 
    				"                                            지급 직전 투자금 잔액의 약정된 <font class='impact' style='font-weight:bold; text-decoration: underline; color:#555555'>“관리 수수료”</font>를 수취하고 '투자회원'에게 원리금을 지급한다.<br><br><br><br><br>\r\n" + 
    				"                                            <font class='impact' style='font-weight:bold; text-decoration: underline; color:#555555'>5. “여신회사” 투자자가 원리금수취권을 보유하고 있는 해당 대출 건의 상환약정일이 경과하여 잔여대출원금과 이자, 연체이자가 회수된 경우에, “투자회원”에게 \r\n" + 
    				"                                            연체기간에 따라 “추심성공수수료”를 다음과 지급 할 수 있으며, “여신회사”는 서비스 편의를 위해 해당 수수료를 제하고 약정상환금을 “회사”에 지급 할 수 있으며,\r\n" + 
    				"                                            “회사”는 지급받은 금액을 투자비율에 맞게 “투자회원”에게 분배한다.<br>\r\n" + 
    				"                                                가. 연체기간이 30일 이하인 경우: 수수료 없음<br>\r\n" + 
    				"                                                나. 연체기간이 31일이상 60일이하인 경우: 회수된 이자 (약정이자+연체이자)의 30%<br>\r\n" + 
    				"                                                다. 연체기간이 61일이상: 회수된 이자 (약정이자+연체이자)의 50%</font><br><br>\r\n" + 
    				"                                            6. '여신회사'는 자신의 재량으로 각 '투자회원'에 대하여 투자 수수료 부과 여부 및 수수료율을 결정하거나 변경할 수 있다. 변경시에는 <font class='impact' style='font-weight:bold; text-decoration: underline; color:#555555'>적용일자 및 개정사유를 \r\n" + 
    				"                                            명시하여 그 적용일자 7일 이전부터 적용일자 전일까지(투자자에게 불리하게 변경되는 경우에는 30일 이상) 고지 및 전자메일로 개별 통지한다.</font><br><br>\r\n" + 
    				"                                            8. “투자회원”은 투자가 완료되어 지급된 수수료(“대출정보중개수수료”, <font class='impact' style='font-weight:bold; text-decoration: underline; color:#555555'>“관리 수수료”</font>)는 반환되지 아니하며, 조기중도상환, 원리금의 미상환이나 손실 발생 등의 사유 \r\n" + 
    				"                                            및 기타 사유로 반환을 요청 할 수 없다.<br><br>\r\n" + 
    				"                                            <font class='impact' style='font-weight:bold; text-decoration: underline; color:#555555'>9. 각 종 수수료의 부가가치세는 별도로 수취한다.</font>\r\n" + 
    				"                                            </span></p>\r\n" + 
    				"                                    \r\n" + 
    				"                        </td>\r\n" + 
    				"                        <td class='text' style='padding:10px; vertical-align:top;padding:10px; vertical-align:top'>\r\n" + 
    				"                            <p style='line-height: 1.6; margin-top: 0px; margin-bottom: 0px; text-align: left;'><span style='font-size: 18px;'>제6조 (투자 수수료)<br><br>\r\n" + 
    				"                                            2. 투자 수수료는 '투자회원'과 '여신회사' 사이에 대출정보를 중개하는 업무에 대하여 지급하는 “대출정보중개 수수료”와 '투자회원'이 '원리금수취권'을 양수한 \r\n" + 
    				"                                            '대출채권'을 관리하는 업무에 대하여 지급하는 <font class='impact2' style='font-weight:bold; text-decoration: underline; color:#111111'>“플랫폼 이용 수수료”</font> 로 구분된다.<br>\r\n" + 
    				"                                                <font class='impact2' style='font-weight:bold; text-decoration: underline; color:#111111'>가. 대출정보중개 수수료 : 면제<br>\r\n" + 
    				"                                                나. 플랫폼 이용 수수료 : 매월 원리금상환시 투자금액에 월 0.2%(연 2.4%)</font><br><br>\r\n" + 
    				"                                            3. '대출회원'의 대출원리금이 매월 정상 상환된 경우 '여신회사'는 이자소득에 대하여 원천징수 세액을 선 공제 후 나머지 금액(대출원리금 - 원천징수세액)에서 \r\n" + 
    				"                                            지급 직전 투자금 잔액의 약정된 에 대출정보를 중개하는 업무에 대하여 지급하는 “대출정보중개 수수료”와 '투자회원'이 '원리금수취권'을 양수한 \r\n" + 
    				"                                            '대출채권'을 관리하는 업무에 대하여 지급하는 <font class='impact2' style='font-weight:bold; text-decoration: underline; color:#111111'>“플랫폼 이용 수수료”</font>를 수취하고 '투자회원'에게 원리금을 지급한다.<br><br>\r\n" + 
    				"                                            <font class='impact2' style='font-weight:bold; text-decoration: underline; color:#111111'>(삭제)</font><br><br><br><br><br><br><br><br><br><br><br><br><br><br>\r\n" + 
    				"                                            6. '여신회사'는 자신의 재량으로 각 '투자회원'에 대하여 투자 수수료 부과 여부 및 수수료율을 결정하거나 변경할 수 있다. 변경시에는 <font class='impact2' style='font-weight:bold; text-decoration: underline; color:#111111'>제15조 (약관의 개정)을 준용한다.</font><br><br><br><br>\r\n" + 
    				"                                            8. “투자회원”은 투자가 완료되어 지급된 수수료(“대출정보중개수수료”, <font class='impact2' style='font-weight:bold; text-decoration: underline; color:#111111'>“플랫폼 이용 수수료”</font>)는 반환되지 아니하며, 조기중도상환, 원리금의 미상환이나 손실 발생 등의 \r\n" + 
    				"                                            사유 및 기타 사유로 반환을 요청 할 수 없다.<br><br>\r\n" + 
    				"                                            <font class='impact2' style='font-weight:bold; text-decoration: underline; color:#111111'>(삭제)</font><br><br>\r\n" + 
    				"                                        </span></p>\r\n" + 
    				"                                    \r\n" + 
    				"                        </td>\r\n" + 
    				"                    </tr>\r\n" + 
    				"                </tbody>\r\n" + 
    				"            </table>\r\n" + 
    				"            </div>\r\n" + 
    				"        </div>\r\n" + 
    				"        <p class='hi' style='width:800px; line-height:30px;'><br></p><p class='hi' style='width:800px; line-height:30px;'>고객님께서는 개정된 약관에 대한 동의를 거부할 수 있습니다. 단, 거부 시에는 서비스 이용에 제약을 받을 수 있습니다.\r\n" + 
    				"                약관 개정 공지일인 2020년 4월 19일까지 거부의사를 표시하지 않으시는 경우, 안내되는 개정 약관에 동의하신 것으로 간주합니다.<br></p></div>\r\n" + 
    				"        <div class='service_info' style='width:800px; margin:0 auto; text-align:center; padding-bottom:20px; line-height:30px; font-size:17px; color:#666666'>\r\n" + 
    				"            <p class='hi'><span style='font-size: 16px;'><br></span></p><p class='hi'>\r\n" + 
    				"                <span style='font-size: 16px;'>본 메일은 법령에 따른 통지의무를 위해 회원님의 수신동의 여부와 무관하게 모든 회원님들께 발송됩니다.</span><br><span style='font-size: 16px;'>\r\n" + 
    				"                제 3자가 본인의 이메일 주소를 잘못 입력할 경우 타인의 메일이 전송될 수 있습니다.</span><br>\r\n" + 
    				"            </p></div>\r\n" + 
    				"        <div class='footer' style='width:800px; margin:0 auto; text-align:center; font-size:13px; line-height:25px; padding-bottom:50px; color:#999999'>\r\n" + 
    				"            <span>크레파스솔루션(주)  서울시 영등포구 여의나루로 53-1, 905호  대표전화 : 02-1522-2975  Email : info@crepass.com <br>\r\n" + 
    				"                Copyright (C) 2020 CrePASS All rights reserved.  </span></div>\r\n" + 
    				"        <p><br></p>\r\n" + 
    				"    </body>\r\n" + 
    				"</html>";
    	} catch(Exception e) {
    		result = "";
    		sendBatchLogging("setHtmlTableForm", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
    	}
    	
		return result;
    }
    
    
    public String setHtmlTableForm(JSONObject jsonTitle, JSONArray jsonBody) {

    	String result = "";
    	
    	try {
    		result = "<table width=\"696\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\"><tbody><tr><td style=\"height:4px;font-size:0;line-height:0\">&nbsp;</td></tr>\r\n" + 
        		"<tr><td style=\"height:3px;border-top:3px solid #554f4c;font-size:0;line-height:0\">&nbsp;</td></tr><tr><td style=\"height:25px;font-size:0;line-height:0\">&nbsp;</td></tr>\r\n" + 
        		"<tr><td style=\"font-family:돋움,dotum;font-size:16px;font-weight:bold\"><span>[" + jsonTitle.getString("mailTitle") + "] <font style='color:#559fc6;'>" +
        		jsonTitle.getString("titleType") + "</font> " + jsonTitle.getString("titleMsg") + "</span></td></tr><tr><td style=\"height:22px;font-size:0;line-height:0\">&nbsp;</td></tr>";
    		
        		if(jsonBody.length() > 0) {
        			result +="<tr><td style=\"border:1px solid #e9e9e9;background:#f9f9f9\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\r\n" + 
        	        		"<tbody><tr><td style=\"width:29px;height:22px;font-size:0;line-height:0\">&nbsp;</td><td></td></tr><tr><td>&nbsp;</td><td>\r\n" +
        	        		"<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\"><tbody>";
	    		
	        		for(int i = 0; i < jsonBody.length(); i++) {
	        			if(!jsonBody.getJSONObject(i).getString("keyName").equals(" ")) {
		        			result += "<tr><td style=\"width:71px;font-weight:bold;font-family:Dotum;color:#595959;font-size:12px\">\r\n" + 
						    		"<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\"><tbody><tr>\r\n" + 
						    		"<td style=\"width:63px;font-weight:bold;font-family:Dotum;font-size:12px;color:#666;line-height:19px\">" + jsonBody.getJSONObject(i).getString("keyName")+ "</td><td style=\"font-size:12px;color:#666\">\r\n" + 
						    		"<span style=\"font-weight:bold\">:</span></td></tr></tbody></table></td><td style=\"color:#666;font-family:Dotum;font-size:12px;line-height:19px\">" + jsonBody.getJSONObject(i).getString("keyValue") + "</td></tr>";
	        			} else 
	        				result += "<tr><td colspan=\"2\">&nbsp;</td></tr>";
	        		}

	        		result += "</tbody></table></td></tr><tr><td style=\"height:20px;font-size:0;line-height:0\">&nbsp;</td><td></td></tr></tbody></table></td></tr><tr><td style=\"height:25px;font-size:0;line-height:0\">&nbsp;</td></tr>";
        		}

        	result += "<tr><td style=\"height:11px;font-size:0;line-height:0\">&nbsp;</td></tr><tr><td style=\"height:28px;border-top:2px solid #554f4c;font-size:0;line-height:0\">&nbsp;</td></tr></tbody></table>";
    	} catch(Exception e) {
    		result = "";
    		sendBatchLogging("setHtmlTableForm", "exception error!!", e.getMessage());
    		throw new RuntimeException(e.getMessage());
    	}
    	
		return result;
	}
    
    public boolean isHoliday(String today) {
    	try {
    		List<OneHolidayCalendar> oneHolidayCalendar = scheService.selectHolidayCalendar();
    		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");

    		for(int i = 0; i < oneHolidayCalendar.size(); i++) {
    			String hDate = oneHolidayCalendar.get(i).getHdate();
    			String hlunar = oneHolidayCalendar.get(i).getHlunar();
    			Calendar calendar = Calendar.getInstance();
    			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    			
    			if(hDate.length() <= 5) {
    				int Year = Integer.parseInt(today.substring(0, 4));
    				String holyDay = dt.format(dt.parse(Year + "-" + hDate));
    				
    				if(hlunar.equals("Y")) {
    					if(hDate.equals("01-01")) {
    						String holyDay2 = dt.format(dt.parse((Year - 1) + "-12-30"));
    						String holyDay3 = dt.format(dt.parse(Year + "-01-02"));
    						
    						if(convertLunarToSolar(holyDay).equals(today) || convertLunarToSolar(holyDay2).equals(today) || convertLunarToSolar(holyDay3).equals(today))
    							return true;
    						
    					} else if(hDate.equals("08-15")) {
    						String holyDay2 = dt.format(dt.parse(Year + "-08-14"));
    						String holyDay3 = dt.format(dt.parse(Year + "-08-16"));
    						
    						if(convertLunarToSolar(holyDay).equals(today) || convertLunarToSolar(holyDay2).equals(today) || convertLunarToSolar(holyDay3).equals(today))
    							return true;
    						
    					} else {
    						if(convertLunarToSolar(holyDay).equals(today))
    							return true;
    					}
    					
    					if(getDateDay(today) == 1)
							return true;
    					else if (getDateDay(today) == 7)
    						return true;
    				} else {
    					if(holyDay.substring(5).equals(today.substring(5)))
    						return true;
        				
    					if(getDateDay(today) == 1)
							return true;
						else if (getDateDay(today) == 7)
							return true;
    				}
    			} else {
    				if(hDate.equals(today)) {
	    				calendar.setTimeInMillis(decreaseDate(hDate, 1));
						String resultDate = sdf.format(calendar.getTime());
						
						if(getDateDay(resultDate) == 1)
							return true;
						else if (getDateDay(resultDate) == 7)
							return true;
    				}
    			}
    		}
    		
    		return false;
    		
    	} catch (Throwable t) {
    		t.printStackTrace();
    	}
    	
    	return false;
    }
    
    public String PrevWorkingDayCalculate(String days) {
    	try {
    		String resultDate = null;
    		
    		List<OneHolidayCalendar> oneHolidayCalendar = scheService.selectHolidayCalendar();
    		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");

    		for(int i = 0; i < oneHolidayCalendar.size(); i++) {
    			String hDate = oneHolidayCalendar.get(i).getHdate();
    			String hlunar = oneHolidayCalendar.get(i).getHlunar();
    			Calendar calendar = Calendar.getInstance();
    			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    			
    			if(hDate.length() <= 5) {
    				int Year = Integer.parseInt(days.substring(0, 4));
    				String holyDay = dt.format(dt.parse(Year + "-" + hDate));
    				
    				if(hlunar.equals("Y")) {
    					if(hDate.equals("01-01")) {
    						String holyDay2 = dt.format(dt.parse((Year - 1) + "-12-30"));
    						String holyDay3 = dt.format(dt.parse(Year + "-01-02"));
    						
    						if(convertLunarToSolar(holyDay).equals(days) || convertLunarToSolar(holyDay2).equals(days) || convertLunarToSolar(holyDay3).equals(days)) {
    							calendar.setTimeInMillis(decreaseDate(convertLunarToSolar(holyDay3), 1));
    							resultDate = sdf.format(calendar.getTime());
    						}
    						
    					} else if(hDate.equals("08-15")) {
    						String holyDay2 = dt.format(dt.parse(Year + "-08-14"));
    						String holyDay3 = dt.format(dt.parse(Year + "-08-16"));
    						
    						if(convertLunarToSolar(holyDay).equals(days) || convertLunarToSolar(holyDay2).equals(days) || convertLunarToSolar(holyDay3).equals(days)) {
    							calendar.setTimeInMillis(decreaseDate(convertLunarToSolar(holyDay3), 1));
    							resultDate = sdf.format(calendar.getTime());
    						}
    						
    					} else {
    						if(convertLunarToSolar(holyDay).equals(days)) {
    							calendar.setTimeInMillis(decreaseDate(convertLunarToSolar(holyDay), 1));
    							resultDate = sdf.format(calendar.getTime());
    						}
    					}
    					
    					if(resultDate != null) {
    						if(getDateDay(resultDate) == 1) {
        						calendar.setTimeInMillis(decreaseDate(resultDate, 1));
    							resultDate = sdf.format(calendar.getTime());
    							break;
        					} else if (getDateDay(resultDate) == 7) {
        						calendar.setTimeInMillis(decreaseDate(resultDate, 1));
    							resultDate = sdf.format(calendar.getTime());
    							break;
        					}
    					} else {
    						if(getDateDay(days) == 1) {
        						calendar.setTimeInMillis(decreaseDate(days, 1));
    							resultDate = sdf.format(calendar.getTime());
    							break;
        					} else if (getDateDay(days) == 7) {
        						calendar.setTimeInMillis(decreaseDate(days, 1));
    							resultDate = sdf.format(calendar.getTime());
    							break;
        					}
    					}
    				} else {
    					if(holyDay.substring(5).equals(days.substring(5))) {
        					calendar.setTimeInMillis(decreaseDate(days, 1));
    						resultDate = sdf.format(calendar.getTime());
        				}
        				
    					if(resultDate != null) {
	        				if(getDateDay(resultDate) == 1) {
	    						calendar.setTimeInMillis(decreaseDate(resultDate, 1));
	    						resultDate = sdf.format(calendar.getTime());
	    						break;
	    					} else if (getDateDay(resultDate) == 7) {
	    						calendar.setTimeInMillis(decreaseDate(resultDate, 1));
	    						resultDate = sdf.format(calendar.getTime());
	    						break;
	    					}
    					} else {
    						if(getDateDay(days) == 1) {
        						calendar.setTimeInMillis(decreaseDate(days, 1));
    							resultDate = sdf.format(calendar.getTime());
    							break;
        					} else if (getDateDay(days) == 7) {
        						calendar.setTimeInMillis(decreaseDate(days, 1));
    							resultDate = sdf.format(calendar.getTime());
    							break;
        					}
    					}
    				}
    			} else {
    				if(hDate.equals(days)) {
	    				calendar.setTimeInMillis(decreaseDate(hDate, 1));
						resultDate = sdf.format(calendar.getTime());
						
						if(getDateDay(resultDate) == 1) {
							calendar.setTimeInMillis(decreaseDate(resultDate, 1));
							resultDate = sdf.format(calendar.getTime());
						} else if (getDateDay(resultDate) == 7) {
							calendar.setTimeInMillis(decreaseDate(resultDate, 1));
							resultDate = sdf.format(calendar.getTime());
						}
    				}
    				
					if(resultDate != null)
						break;
    			}
    		}
    		
    		return resultDate;
    		
    	} catch (Throwable t) {
    		t.printStackTrace();
    	}
    	
    	return null;
    }
    
    public String NextWorkingDayCalculate(String days) {
    	try {
    		String resultDate = null;
    		
    		List<OneHolidayCalendar> oneHolidayCalendar = scheService.selectHolidayCalendar();
    		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");

    		for(int i = 0; i < oneHolidayCalendar.size(); i++) {
    			String hDate = oneHolidayCalendar.get(i).getHdate();
    			String hlunar = oneHolidayCalendar.get(i).getHlunar();
    			Calendar calendar = Calendar.getInstance();
    			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    			
    			if(hDate.length() <= 5) {
    				int Year = Integer.parseInt(days.substring(0, 4));
    				String holyDay = dt.format(dt.parse(Year + "-" + hDate));
    				
    				if(hlunar.equals("Y")) {
    					if(hDate.equals("01-01")) {
    						String holyDay2 = dt.format(dt.parse((Year - 1) + "-12-30"));
    						String holyDay3 = dt.format(dt.parse(Year + "-01-02"));
    						
    						if(convertLunarToSolar(holyDay).equals(days) || convertLunarToSolar(holyDay2).equals(days) || convertLunarToSolar(holyDay3).equals(days)) {
    							calendar.setTimeInMillis(increaseDate(convertLunarToSolar(holyDay3), 1));
    							resultDate = sdf.format(calendar.getTime());
    						}
    						
    					} else if(hDate.equals("08-15")) {
    						String holyDay2 = dt.format(dt.parse(Year + "-08-14"));
    						String holyDay3 = dt.format(dt.parse(Year + "-08-16"));
    						
    						if(convertLunarToSolar(holyDay).equals(days) || convertLunarToSolar(holyDay2).equals(days) || convertLunarToSolar(holyDay3).equals(days)) {
    							calendar.setTimeInMillis(increaseDate(convertLunarToSolar(holyDay3), 1));
    							resultDate = sdf.format(calendar.getTime());
    						}
    						
    					} else {
    						if(convertLunarToSolar(holyDay).equals(days)) {
    							calendar.setTimeInMillis(increaseDate(convertLunarToSolar(holyDay), 1));
    							resultDate = sdf.format(calendar.getTime());
    						}
    					}
    					
    					if(resultDate != null) {
    						if(getDateDay(resultDate) == 1) {
        						calendar.setTimeInMillis(increaseDate(resultDate, 1));
    							resultDate = sdf.format(calendar.getTime());
    							break;
        					} else if (getDateDay(resultDate) == 7) {
        						calendar.setTimeInMillis(increaseDate(resultDate, 1));
    							resultDate = sdf.format(calendar.getTime());
    							break;
        					}
    					} else {
    						if(getDateDay(days) == 1) {
        						calendar.setTimeInMillis(increaseDate(days, 1));
    							resultDate = sdf.format(calendar.getTime());
    							break;
        					} else if (getDateDay(days) == 7) {
        						calendar.setTimeInMillis(increaseDate(days, 1));
    							resultDate = sdf.format(calendar.getTime());
    							break;
        					}
    					}
    				} else {
    					if(holyDay.substring(5).equals(days.substring(5))) {
        					calendar.setTimeInMillis(increaseDate(days, 1));
    						resultDate = sdf.format(calendar.getTime());
        				}
        				
    					if(resultDate != null) {
	        				if(getDateDay(resultDate) == 1) {
	    						calendar.setTimeInMillis(increaseDate(resultDate, 1));
	    						resultDate = sdf.format(calendar.getTime());
	    						break;
	    					} else if (getDateDay(resultDate) == 7) {
	    						calendar.setTimeInMillis(increaseDate(resultDate, 1));
	    						resultDate = sdf.format(calendar.getTime());
	    						break;
	    					}
    					} else {
    						if(getDateDay(days) == 1) {
        						calendar.setTimeInMillis(increaseDate(days, 1));
    							resultDate = sdf.format(calendar.getTime());
    							break;
        					} else if (getDateDay(days) == 7) {
        						calendar.setTimeInMillis(increaseDate(days, 1));
    							resultDate = sdf.format(calendar.getTime());
    							break;
        					}
    					}
    				}
    			} else {
    				if(hDate.equals(days)) {
	    				calendar.setTimeInMillis(increaseDate(hDate, 1));
						resultDate = sdf.format(calendar.getTime());
						
						if(getDateDay(resultDate) == 1) {
							calendar.setTimeInMillis(increaseDate(resultDate, 1));
							resultDate = sdf.format(calendar.getTime());
						} else if (getDateDay(resultDate) == 7) {
							calendar.setTimeInMillis(increaseDate(resultDate, 1));
							resultDate = sdf.format(calendar.getTime());
						}
    				}
    				
					if(resultDate != null)
						break;
    			}
    		}
    		
    		return resultDate;
    		
    	} catch (Throwable t) {
    		t.printStackTrace();
    	}
    	
    	return null;
    }
    
    private long decreaseDate(String date, int decrease) {
    	Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, Integer.parseInt(date.substring(0, 4)));
		cal.set(Calendar.MONTH, Integer.parseInt(date.substring(5, 7)) - 1);
		cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.substring(8)) - decrease);
		return cal.getTimeInMillis();
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
    
    private int getDateDay(String date) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd") ;
        Date nDate = dateFormat.parse(date) ;
         
        Calendar cal = Calendar.getInstance() ;
        cal.setTime(nDate);
         
        int dayNum = cal.get(Calendar.DAY_OF_WEEK) ;
        
        return dayNum;
    }
    
    // html테그 제거해주는 메서드
    public static String br2nl(String html) {
        if(html==null)
            return html;
        Document document = Jsoup.parse(html);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
        document.select("br").append("\\n");
        document.select("p").prepend("\\n\\n");
        String s = document.html().replaceAll("\\\\n", "\n");
        return Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
    }
    
	
    //SchController에서 에러발생시 관리자에게 메일 보내주는 메서드
	public String setHtmlTableFormHeader(String type, int count) {
		String header = "<table width=\"696\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\"><tbody><tr><td style=\"height:4px;font-size:0;line-height:0\">&nbsp;</td></tr>\r\n" + 
        		"<tr><td style=\"height:3px;border-top:3px solid #554f4c;font-size:0;line-height:0\">&nbsp;</td></tr><tr><td style=\"height:25px;font-size:0;line-height:0\">&nbsp;</td></tr>\r\n" + 
        		"<tr><td style=\"font-family:돋움,dotum;font-size:16px;font-weight:bold\"><span>[정산] <font style='color:#559fc6;'>" + type + "</font> " + count + "건이 있습니다.</span></td></tr>\r\n" + 
        		"<tr><td style=\"height:22px;font-size:0;line-height:0\">&nbsp;</td></tr><tr><td style=\"border:1px solid #e9e9e9;background:#f9f9f9\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\r\n" + 
        		"<tbody><tr><td style=\"width:29px;height:22px;font-size:0;line-height:0\">&nbsp;</td><td></td></tr><tr><td>&nbsp;</td><td>\r\n";
		return header;
	}
	
	public String setHtmlTableFormBody(String loanId, String payCount, String resultDate, String repayAmount, String paidAmount, String loanInterest, String balance) {
		String body = "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\"><tbody><tr><td style=\"width:71px;font-weight:bold;font-family:'\\\\00b3cb\\\\00c6c0',Dotum;color:#595959;font-size:12px\">\r\n" + 
        		"<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\"><tbody><tr>\r\n" + 
        		"<td style=\"width:63px;font-weight:bold;font-family:'\\\\00b3cb\\\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\">대출제목</td><td style=\"font-size:12px;color:#666\">\r\n" + 
        		"<span style=\"font-weight:bold\">:</span></td></tr></tbody></table></td><td style=\"color:#666;font-family:'\\\\00b3cb\\\\00c6c0',Dotum;font-size:12px;line-height:19px\"><a href = 'http://solution.crepass.com/crapas/?cms=loan_form&type=m&i_id=" + loanId + "&i_loan_type=credit'>" + payCount + "</a></td></tr><tr>\r\n" + 
        		"<td style=\"font-weight:bold;font-family:'\\\\00b3cb\\\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\">\r\n" + 
        		"<tbody><tr><td style=\"width:63px;font-weight:bold;font-family:'\\\\00b3cb\\\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\">계&nbsp;&nbsp;&nbsp;&nbsp;정</td><td style=\"font-size:12px;color:#666\">\r\n" + 
        		"<span style=\"font-weight:bold\">:</span></td></tr></tbody></table></td><td style=\"font-family:'\\\\00b3cb\\\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\">" + resultDate + "</td></tr>\r\n" + 
        		"<tr><td style=\"font-weight:bold;font-family:'\\\\00b3cb\\\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\">\r\n" + 
        		"<tbody><tr><td style=\"width:63px;font-weight:bold;font-family:'\\\\00b3cb\\\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\">이&nbsp;&nbsp;&nbsp;&nbsp;름</td><td style=\"font-size:12px;color:#666\">\r\n" + 
        		"<span style=\"font-weight:bold\">:</span></td></tr></tbody></table></td><td style=\"font-family:'\\\\00b3cb\\\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\">" + repayAmount + "</td></tr>\r\n" + 
        		"<tr><td style=\"vertical-align:top\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\"><tbody><tr>\r\n" + 
        		"<td style=\"width:63px;font-weight:bold;font-family:'\\\\00b3cb\\\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\">대출금액</td><td style=\"font-size:12px;color:#666\">\r\n" + 
        		"<span style=\"font-weight:bold\">:</span></td></tr></tbody></table></td><td style=\"color:#666;font-family:'\\\\00b3cb\\\\00c6c0',Dotum;font-size:12px;line-height:20px\">" + balance + "</td></tr>\r\n" + 
        		"</tbody></table>\r\n";
		return body;
	}
	
	public String setHtmlTableFormFooter() {
		String footer = "</td></tr><tr><td style=\"height:20px;font-size:0;line-height:0\">&nbsp;</td><td></td></tr></tbody></table></td></tr><tr><td style=\"height:25px;font-size:0;line-height:0\">&nbsp;</td></tr>\r\n" + 
        		"<tr><td style=\"height:11px;font-size:0;line-height:0\">&nbsp;</td></tr><tr><td style=\"height:28px;border-top:2px solid #554f4c;font-size:0;line-height:0\">&nbsp;</td></tr></tbody></table>";
		return footer;
	}
	
	public String getMD5(String str){
		String MD5 = ""; 
		try{
			MessageDigest md = MessageDigest.getInstance("MD5"); 
			md.update(str.getBytes()); 
			byte byteData[] = md.digest();
			StringBuffer sb = new StringBuffer(); 
			for(int i = 0 ; i < byteData.length ; i++){
				sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
			}
			MD5 = sb.toString();
			
		}catch(NoSuchAlgorithmException e){
			e.printStackTrace(); 
			MD5 = null; 
		}
		return MD5;
	}

	

}
