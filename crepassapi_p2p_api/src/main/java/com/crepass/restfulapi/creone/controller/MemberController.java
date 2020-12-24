package com.crepass.restfulapi.creone.controller;

import io.swagger.annotations.ApiOperation;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerMapping;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.cre.domain.CreIntro;
import com.crepass.restfulapi.cre.domain.CreMember;
import com.crepass.restfulapi.cre.service.CreMemberService;
import com.crepass.restfulapi.cre.service.IntroService;
import com.crepass.restfulapi.one.domain.MariMember;
import com.crepass.restfulapi.one.domain.OneCertify;
import com.crepass.restfulapi.one.domain.OneMemberConfirm;
import com.crepass.restfulapi.one.domain.OneMemberCustAddInfo2;
import com.crepass.restfulapi.one.service.EmoneyService;
import com.crepass.restfulapi.one.service.OneMemberService;
import com.google.gson.Gson;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@CrossOrigin
@RestController
@RequestMapping(path = "/api", method = RequestMethod.POST)
public class MemberController {

    @Autowired
    private CreMemberService creMemberService;

    @Autowired
    private OneMemberService oneMemberService;
    
    @Autowired
    private IntroService introService;
    
    @Autowired
    private EmoneyService emoneyService;
    
    @Autowired
    private CommonUtil commonUtil;
    
    @Autowired(required=true)
	private HttpServletRequest request;
    
    @Value("${crepas.url.memberck}")
    private String memberckUrl;
       
    @Value("${crepas.inside.url}")
    private String insideUrl;
    
    @ApiOperation(value = "회원 가입")
    @RequestMapping("/members/add")
    @Transactional("oneTransactionManager")
    public ResponseEntity<ResponseResult> memberAdd(@RequestBody String requestString) throws Exception {
               
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);

		// V1은 web 회원이라고 가정 influx추가 201127
		jsonMember.getJSONObject("request").put("influxType", "Web");

        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject request = jsonMember.getJSONObject("request");
        String mid = request.getString("mid");
    
        if(commonUtil.isValidEmail(mid)) {
	        //회원 정보 존재 유무 체크 
	        final boolean isPass = oneMemberService.checkOneMember(jsonMember);
	        
	        if (isPass) {
	        	CreMember creMemberCheck = creMemberService.selectMemberById(mid);
	        	
	        	if(creMemberCheck == null) {
		            //회원 정보 추가 
		            int retOne = oneMemberService.addOneMember(jsonMember);
		            if (retOne > 0) {
		                CreMember creMember = creMemberService.addCreMember(jsonMember);
		                
		                if (creMember != null) {
		                    response.setResult(creMember);
		                    
		                    JSONObject requestSyfert = new JSONObject();
		                    JSONObject jsonSyfert = new JSONObject();
		                    
		                    jsonSyfert.put("mid", mid);
		                	jsonSyfert.put("name", request.getString("name"));
		                	jsonSyfert.put("memGuid", "");
		                	jsonSyfert.put("ipAddr", commonUtil.getRemoteAddrs());
		                	jsonSyfert.put("memUse", "N");
		                	jsonSyfert.put("telhp", request.getString("telhp"));
		                	requestSyfert.put("request", jsonSyfert);
		                    
		                    oneMemberService.addOneSeyfert(requestSyfert);
		                    
		                    MariMember mariMember = oneMemberService.selectMemberById(mid);
		                    OneCertify oneCertify = new OneCertify();
		                    oneCertify.setMno(mariMember.getMno());
		                    oneCertify.setCertifyType(request.getString("certifyType"));
		                    oneCertify.setCertifyResult(request.getString("certifyResult").toString());
		                    oneMemberService.insertOneCertify(oneCertify);
		                }
		            }
	        	} else {
		        	response.setState(205);
		            response.setMessage("기존에 가입한 내역이 있는 계정입니다.");
		        }
		            
	        } else {
	            response.setState(203);
	            response.setMessage("ID가 이미 존재합니다.");
	            response.setResult(jsonMember.get("request").toString());
	        }
        } else {
            response.setState(204);
            response.setMessage("계정이 email형식에 맞지 않습니다.");
        }
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }

    @ApiOperation(value = "로그인")
    @RequestMapping("/members/login")
    public ResponseEntity<ResponseResult> memberLogin(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        final String mid = ((JSONObject) jsonMember.get("request")).get("mid").toString();
        final String passwd = ((JSONObject) jsonMember.get("request")).get("passwd").toString();
        final String playerId = ((JSONObject) jsonMember.get("request")).get("playerId").toString();
        
        String m_no = oneMemberService.selectUserIdCheck(mid);
        
        if(m_no != null && !m_no.equals("null") && m_no.length() > 0) {
	        //로그인 정보 - 통신 확인 (솔루션)
	        Map<String, String> vars = new HashMap<String, String>();
	        vars.put("m_id", mid);
	        vars.put("m_password", passwd);
	        
	        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload(mid));

			byte[] sharedKey = new byte[32];
			new SecureRandom().nextBytes(sharedKey);
			
			MACSigner macSigner = new MACSigner(sharedKey);
			
			jwsObject.sign(macSigner);
			String token = jwsObject.serialize();
	        
			if (!token.isEmpty()) {
				final boolean isTokenUpdate = oneMemberService.updateMemberAuthToken(mid, token);
	            //로그인 정보 - 비밀번호 확인 (DB) 
	            final boolean isPass = oneMemberService.checkPasswordById(jsonMember);
	            if (isPass && isTokenUpdate) {
	                CreIntro creIntro = introService.selectIntroInfo();
	                
	                Map<String, Object> result = new HashMap<String, Object>();
	                result.put("appVersion", creIntro.getAppVersion());
	                result.put("authToken", token);
	                
	                String isAutoInvset = oneMemberService.selectAutoInvestAgree(mid);
	                
	                if(isAutoInvset == null || isAutoInvset.equals("null") || isAutoInvset.length() < 1)
	                	isAutoInvset = "N";
	                
	                MariMember mariMember = oneMemberService.selectMemberById(mid);
	                Map<String, String> resultInfo = new HashMap<String, String>();
	                resultInfo.put("mid", mariMember.getMid());
	                resultInfo.put("name", mariMember.getName());
	                resultInfo.put("birth", mariMember.getBirth());
	                resultInfo.put("xes", mariMember.getXes());
	                resultInfo.put("withholdingZip", mariMember.getWithholdingZip());
	                resultInfo.put("bankcode", mariMember.getBankCode());
	                resultInfo.put("hp", mariMember.getHpNumber());
	                resultInfo.put("newsagency", mariMember.getNewsagency());
	                resultInfo.put("myBankacc", mariMember.getMyBankacc());
	                resultInfo.put("isAutoInvest", isAutoInvset);
	                resultInfo.put("level", mariMember.getLevel());
	                
	                CreMember creMember = creMemberService.selectMemberById(mid);
	                resultInfo.put("charType",creMember.getCharType());
	                resultInfo.put("alarm",creMember.getAlarm());
	                resultInfo.put("tdiaryMsg",creMember.getTdiaryMsg());
	                
	                result.put("memberInfo", resultInfo);
	                response.setResult(result);
	                
	                creMemberService.updateMemberByPlayerId(mid, playerId);
	            } else {
	                response.setState(204);
	                response.setMessage("비밀번호가 일치하지 않습니다.");
	                response.setResult(jsonMember.get("request").toString());
	            }
	
	        } else {
//	            response.setState(resultJson.getInt("result_cd"));
	        	response.setState(203);
	//            response.setMessage(resultJson.getString("result_msg"));
	            response.setMessage("비밀번호가 일치하지 않습니다.");
	            response.setResult(jsonMember.get("request").toString());
	        }
        } else {
        	response.setState(202);
            response.setMessage("등록된 계정이 아닙니다.\n회원가입후 이용해주세요.");
        }
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "회원 정보 조회")
    @RequestMapping("/members/info")
    public ResponseEntity<ResponseResult> memberInfo(@RequestBody String requestString) throws Exception {
               
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
    
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        final String mid = ((JSONObject) jsonMember.get("request")).get("mid").toString();
        MariMember mariMember = oneMemberService.selectMemberById(mid);
        
        CreMember creMember = creMemberService.selectMemberById(mid);
        mariMember.setCharType(creMember.getCharType());
        
        response.setResult(mariMember);
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "회원 정보 수정")
    @RequestMapping("/members/mod")
    public ResponseEntity<ResponseResult> memberMod(@RequestBody String requestString) throws Exception {
               
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);

        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        Map<String, String> resultInfo = new HashMap<String, String>();
        
        final String newPassword = ((JSONObject) jsonMember.get("request")).get("newpasswd").toString();
        
        if (newPassword == null || newPassword.isEmpty()) {
            response.setState(206);
            response.setMessage("비빌번호가 입력되지 않았습니다.");
            resultInfo.put("passwdCheck", "1");
            response.setResult(resultInfo);
        } else {
            //비밀번호 일치 확인 
            final boolean isPass = oneMemberService.checkPasswordById(jsonMember);
            if (isPass) {
            
                //회원 정보 수정 mari
                int oneResult = oneMemberService.updateMemberById(jsonMember);
                //회원 정보 수정 cre
                int creResult = creMemberService.updateMemberById(jsonMember);
                
                if ((oneResult + creResult) == 2) {
                    resultInfo.put("passwdCheck", "0");
                    response.setResult(resultInfo);
                } else {
                    response.setState(205);
                    response.setMessage("시스템 오류입니다.");
                    resultInfo.put("passwdCheck", "1");
                    response.setResult(resultInfo);
                }
                
            } else {
                response.setState(204);
                response.setMessage("비밀번호가 일치하지 않습니다.");
                resultInfo.put("passwdCheck", "1");
                response.setResult(resultInfo);
            }
        }
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
	//실제 바꾸는게 없어서 사용하지 않는다고 판단
    @ApiOperation(value = "회원 정보 수정")
    @RequestMapping("/members/mod2")
    public ResponseEntity<ResponseResult> memberMod2(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
        
        JSONObject jsonMember = new JSONObject(requestString);

        final String mid = ((JSONObject) jsonMember.get("request")).get("mid").toString();
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        
        OneMemberCustAddInfo2 oneMemberCustAddInfo = oneMemberService.selectCustAddInfo2(mid);
		
		if(oneMemberCustAddInfo == null || oneMemberCustAddInfo.getMyBankacc().length() < 1) {
			response.setState(482);
            response.setMessage("출금계좌를 등록 후 가상계좌생성을 진행해주세요.");
		} else {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> vars = new HashMap<String, String>();
            vars.put("CUST_ID", oneMemberCustAddInfo.getCustId());
            
            String resultSearch = restTemplate.postForObject(insideUrl + "/customer/search", vars, String.class);
            JSONObject jsonSearch = new JSONObject(resultSearch);
            
            int searchState = jsonSearch.getInt("STATE");
            
            if(searchState == 200) {
            	String callNum[] = oneMemberCustAddInfo.getHp().replaceFirst("(^02.{0}|^01.{1}|[0-9]{3})([0-9]+)([0-9]{4})","$1-$2-$3").split("-");
            	
            	String hpFront = "";
                String hpBack = "";
                String hpMiddle = "";
            	
            	if(callNum.length == 3) {
            		hpFront = callNum[0];
            		hpMiddle = callNum[1];
            		hpBack = callNum[2];
            	} else {
                	hpFront = oneMemberCustAddInfo.getHp().substring(0, 3);
                    hpBack = oneMemberCustAddInfo.getHp().substring(oneMemberCustAddInfo.getHp().length() - 4, oneMemberCustAddInfo.getHp().length());
                    hpMiddle = oneMemberCustAddInfo.getHp().replace(hpBack, "");
                    hpMiddle = hpMiddle.substring(3, hpMiddle.length());
            	}
                
            	vars = new HashMap<String, String>();
                vars.put("CUST_ID", oneMemberCustAddInfo.getCustId());
                vars.put("CUST_NM", oneMemberCustAddInfo.getCompanyName());
                vars.put("CUST_SUB_NM", oneMemberCustAddInfo.getCompanyName());
                vars.put("REP_NM", oneMemberCustAddInfo.getName());
                vars.put("BIRTH_DATE", "");
                vars.put("SUP_REG_NB", oneMemberCustAddInfo.getCompanyNum());
                vars.put("PRI_SUP_GBN", "2");
                vars.put("HP_NO1", hpFront);
                vars.put("HP_NO2", hpMiddle);
                vars.put("HP_NO3", hpBack);
                vars.put("BANK_CD", oneMemberCustAddInfo.getMyBankcode());
                vars.put("ACCT_NB", oneMemberCustAddInfo.getMyBankacc());
                vars.put("CMS_NB", oneMemberCustAddInfo.getVirtualAccnt());
                
                String resultCustAdd = restTemplate.postForObject(insideUrl + "/customer/mod", vars, String.class);
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
        		response.setState(200);
				response.setMessage("정상적으로 처리하였습니다.");
            }
		}
		
		commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "회원 탈퇴")
    @RequestMapping("/members/termi")
    public ResponseEntity<ResponseResult> memberLeave(@RequestBody String requestString) throws Exception {
               
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
    
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        final String mid = ((JSONObject) jsonMember.get("request")).getString("mid");
        List<String> tradeCheck = oneMemberService.selectTradeCheck(mid);				// 거래중인 건이 있는지 확인
        String emoneyBalance = emoneyService.selectEmoneyInvestBalance(mid);
        
        String custId = oneMemberService.selectCustID(mid);
        
        if(tradeCheck != null && tradeCheck.size() > 0) {
        	response.setState(244);
            response.setMessage("거래중인건이 존재하여 회원 탈퇴가 불가합니다.");
        } else {
        	if(custId != null) {
    	        RestTemplate restTemplate = new RestTemplate();
    	        Map<String, String> vars = new HashMap<String, String>();
    	        vars.put("CUST_ID", custId);
    	        
    	        String resultAMT = restTemplate.postForObject(insideUrl + "/lookup/customer", vars, String.class);
    	        JSONObject jsonResult = new JSONObject(resultAMT);
    	        
    	        if(jsonResult.getInt("STATE") == 200)
    	        	emoneyBalance = ((JSONObject)jsonResult.get("RESULT")).getString("BALANCE_AMT");
    	        else
    	        	emoneyBalance = "0";
            }
        	
        	if(Long.parseLong(emoneyBalance) > 0) {
        		response.setState(245);
                response.setMessage("예치금 잔액이 있어 회원 탈퇴가 불가합니다.");
        	} else
        		oneMemberService.deleteMemberById(mid);
        }
//      creMemberService.deleteMemberById(mid); //보류
        
        response.setResult(jsonMember.get("request").toString());
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "회원 중복검사")
    @RequestMapping("/members/duplicate")
    public ResponseEntity<ResponseResult> isDuplicate(@RequestBody String requestString) throws Exception {
               
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
    
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String hp = jsonRequest.get("hp").toString();
        final String name = jsonRequest.get("name").toString();
        final String newsagency = jsonRequest.get("newsagency").toString();
        
        OneMemberConfirm oneMemberConfirm = new OneMemberConfirm();
        oneMemberConfirm.setHp(hp);
        oneMemberConfirm.setName(name);
        oneMemberConfirm.setNewsagency(newsagency);
        
        List<String> custConfirm = oneMemberService.selectCustConfirm(oneMemberConfirm);
        
        if(custConfirm != null && custConfirm.size() > 0) {
        	response.setState(438);
            response.setMessage("이미 등록된 회원입니다.");
        }
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "회원 비밀번호 찾기")
    @RequestMapping("/members/findpw")
    public ResponseEntity<ResponseResult> sendFindpw(@RequestBody String requestString) throws Exception {
               
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        String mid = jsonMember.getJSONObject("request").getString("mid");
    
        ResponseResult response = new ResponseResult();
        String randPW = commonUtil.getRandPW();

        if(oneMemberService.updateMemberPW(mid, commonUtil.setSHA256(randPW))) {
	        response.setState(200);
	        response.setMessage("정상적으로 처리하였습니다.");
	        
	        String subject = "[청년5.5] 임시비밀번호가 발급되었습니다.";
	        
	        Calendar cal = Calendar.getInstance();
    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss");
    		String transDate = sdf.format(cal.getTime());
    		
	        String htmlForm = "<table width=\"696\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\"><tbody><tr><td style=\"height:4px;font-size:0;line-height:0\">&nbsp;</td></tr>\n" + 
	        		"<tr><td style=\"height:3px;border-top:3px solid #554f4c;font-size:0;line-height:0\">&nbsp;</td></tr><tr><td style=\"height:25px;font-size:0;line-height:0\">&nbsp;</td></tr>\n" + 
	        		"<tr><td style=\"font-family:돋움,dotum;font-size:16px;font-weight:bold\"><span>요청하신 <font style='color:#559fc6;'>임시비밀번호</font>가 발급되었습니다.</span></td></tr>\n" + 
	        		"<tr><td style=\"height:22px;font-size:0;line-height:0\">&nbsp;</td></tr><tr><td style=\"border:1px solid #e9e9e9;background:#f9f9f9\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" + 
	        		"<tbody><tr><td style=\"width:29px;height:22px;font-size:0;line-height:0\">&nbsp;</td><td></td></tr><tr><td>&nbsp;</td><td><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\">\n" + 
	        		"<tbody><tr><td style=\"width:71px;font-weight:bold;font-family:'\\00b3cb\\00c6c0',Dotum;color:#595959;font-size:12px\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\">\n" + 
	        		"<tbody><tr><td style=\"width:63px;font-weight:bold;font-family:'\\00b3cb\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\">아이디</td><td style=\"font-size:12px;color:#666\"><span style=\"font-weight:bold\">:</span></td></tr>\n" + 
	        		"</tbody></table></td><td style=\"color:#666;font-family:'\\00b3cb\\00c6c0',Dotum;font-size:12px;line-height:19px\"> "+ mid +"</td></tr><tr><td style=\"font-weight:bold;font-family:'\\00b3cb\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\">\n" + 
	        		"<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\"><tbody><tr><td style=\"width:63px;font-weight:bold;font-family:'\\00b3cb\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\">비밀 번호</td>\n" + 
	        		"<td style=\"font-size:12px;color:#666\"><span style=\"font-weight:bold\">:</span></td></tr></tbody></table></td><td style=\"font-family:'\\00b3cb\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\"> "+ randPW +"</td></tr>\n" + 
	        		"<tr><td style=\"font-weight:bold;font-family:'\\00b3cb\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\"><tbody><tr>\n" + 
	        		"<td style=\"width:63px;font-weight:bold;font-family:'\\00b3cb\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\">접수 시각</td><td style=\"font-size:12px;color:#666\"><span style=\"font-weight:bold\">:</span></td>\n" + 
	        		"</tr></tbody></table></td><td style=\"font-family:'\\00b3cb\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\"> "+ transDate +"</td></tr><tr><td style=\"vertical-align:top\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\">\n" + 
	        		"<tbody><tr><td style=\"width:63px;font-weight:bold;font-family:'\\00b3cb\\00c6c0',Dotum;font-size:12px;color:#666;line-height:19px\">주의사항</td><td style=\"font-size:12px;color:#666\"><span style=\"font-weight:bold\">:</span></td>\n" + 
	        		"</tr></tbody></table></td><td style=\"color:#666;font-family:'\\00b3cb\\00c6c0',Dotum;font-size:12px;line-height:20px\">로그인 후 <strong style=\"color:#559fc6;\">새로운 비밀번호로 변경</strong>하여 이용하시기 바랍니다.<br></td>\n" + 
	        		"</tr></tbody></table></td></tr><tr><td style=\"height:20px;font-size:0;line-height:0\">&nbsp;</td><td></td></tr></tbody></table></td></tr><tr><td style=\"height:25px;font-size:0;line-height:0\">&nbsp;</td></tr>\n" + 
	        		"<tr><td style=\"font-size:12px;font-family:Dotum,'\\00b3cb\\00c6c0';color:#666;line-height:19px\"><bold style = 'font-style: oblique;'>·</bold> 임시비밀번호는 관리자도 알 수 없도록 암호화되어 있습니다.</td></tr>\n" + 
	        		"<tr><td style=\"height:11px;font-size:0;line-height:0\">&nbsp;</td></tr><tr><td style=\"height:28px;border-top:2px solid #554f4c;font-size:0;line-height:0\">&nbsp;</td></tr><tr>\n" + 
	        		"<td style=\"font-family:'\\00b098\\00b214\\00ace0\\00b515',NanumGothic,'\\00b3cb\\00c6c0',dotum;font-size:11px;color:#7e7e7e;text-align:center\">\n" + 
	        		"본 메일은 발신전용 입니다. 궁금하신 점이나 불편한 사항은 고객센터(1522-2975)에 문의해 주시기 바랍니다.<br>[고객센터 운영시간 : 평일 오전 09:00~오후18:00]</td></tr><tr><td style=\"height:10px;font-size:0;line-height:0\">&nbsp;</td></tr>\n" + 
	        		"<tr><td style=\"height:9px;font-size:0;line-height:0\">&nbsp;</td></tr><tr><td style=\"font-family:verdana;font-size:9px;color:#595959;text-align:center\">COPYRIGHT ⓒ CrePASS. ALL RIGHTS RESERVED.</td></tr></tbody></table>";
	        
	        commonUtil.sendLoggingEmail3(subject, htmlForm, mid);
        } else {
        	response.setState(207);
	        response.setMessage("임시비밀번호를 발급할 계정을 찾을 수 없습니다.\n회원가입한 계정 이메일을 입력해주세요.");
        }
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    

    @ApiOperation(value = "회원 이메일 인증 메일 발송")
    @RequestMapping("/members/senddingemail")
    @Transactional("oneTransactionManager")
    public ResponseEntity<ResponseResult> memberSenddingEmail(@RequestBody String requestString, @RequestHeader String apkVersion) throws Exception {
               
        JSONObject jsonMember = new JSONObject(requestString);
    
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject request = jsonMember.getJSONObject("request");
        String mid = request.getString("mid");
        
        // 이미 가입한 회원?
        /*
         
         https://verification.crepass.com/api/rcvmail/agree/2020-07-09%2015:44:43/pakiki@gmail.com/802e2656b2e6aacd7d4a34fa749fa7a5/

		- 도메인 주소        : https://verification.crepass.com
		- 컨텍스트            : /api/rcvmail/agree
		- 동의를 받은 시간  : /2020-07-09%2015:44:43
		- 메일 주소           : /pakiki@gmail.com
		- 토큰키               : /802e2656b2e6aacd7d4a34fa749fa7a5/
          
          http://solutiondev.crepass.com/crapas/?cms=senddingemail&mid=jhlee@crepass.com&emailToken=b7212g975n7h274009i8xu6r2n8b1z&validationDt=202007091705
         * */
        
        
        String emailToken = commonUtil.getToken(30);
        oneMemberService.insertEmailValidation(mid, emailToken);
        
        String url = "http://p2p.crepass.com/emailvalidation/certifyEmail.jsp";
        SimpleDateFormat sdf = new SimpleDateFormat ( "yyyyMMddHHmmss");
        Date time = new Date();
        String currentTime = sdf.format(time);
        
        String emailBody = setHtmlEmailValidationForm(url, mid, emailToken, currentTime);
        //  title, body, mid
        commonUtil.sendLoggingEmail6("크레파스 인증 메일입니다.", emailBody, mid);
        
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }

    public String setHtmlEmailValidationForm(String url, String mid, String emailToken, String validationDt) {

    	String emailBody = "<!doctype html>\n" + 
    			"<html lang='ko'>\n" + 
    			"    <head>\n" + 
    			"\n" + 
    			"	<meta charset='utf-8'>\n" + 
    			"	<meta http-equiv='content-type' content='text/html; charset=utf-8'>\n" + 
    			"	<meta http-equiv='Content-Script-Type' content='text/javascript'>\n" + 
    			"	<meta http-equiv='Content-Style-Type' content='text/css'>\n" + 
    			"	<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n" + 
    			"	<title>크레파스 이메일 인증 메일입니다.</title>\n" + 
    			"    </head>\n" + 
    			"\n" + 
    			"    <body style='width:100%; height:100%; background:#f6f6f6;'>\n" + 
    			"        <div class='Wrap' style='width:800px; margin:0 auto; padding:50px; background:#fff'>\n" + 
    			"            \n" + 
    			"            <div class='main_info'>\n" + 
    			"                    <img width='200' src='https://i0.wp.com/crepass.com/wp-content/uploads/2019/11/cropped-unnamed-1.png?fit=726%2C136&amp;ssl=1'>\n" + 
    			"                    <p class='maintitle' style='letter-spacing: -1.5px; font-size:25pt;padding-top:30px; font-family: 나눔고딕;'>크레파스 이메일 인증 메일입니다.</p>\n" + 
    			"                    <p class='hi' style='line-height:35px; font-size:14pt; padding-bottom:50px; padding-top:30px'>안녕하세요. P2P금융 크레파스 솔루션입니다. <br>\n" + 
    			"                        크레파스 회원가입 신청을 하셨습니다.<br>\n" + 
    			"                        등록자 본인 확인과 보안을 위해 하단의 인증링크를 클릭하여 계정인증을 해주시기 바랍니다.</p>\n" +
    			"                        본 인증메일은 30분간 유효합니다.<p>\n" +
    			"						\n" +
    			"						<a href='" + url + "?mid=" + mid + "&emailToken=" + emailToken + "&validationDt=" + validationDt + "'>인증링크</a>\n" + 
    			"			</div>\n" + 
    			"                                    \n" + 
    			"            \n" + 
    			"            \n" + 
    			"		</div>\n" + 
    			"        \n" + 
    			"\n" + 
    			"        <div class='service_info' style='width:800px; margin:0 auto; text-align:center; padding-bottom:20px; line-height:30px; font-size:17px; color:#666666'>\n" + 
    			"            <p class='hi'><span style='font-size: 16px;'><br></span></p><p class='hi'>\n" + 
    			"                <span style='font-size: 16px;'>본 메일은 법령에 따른 통지의무를 위해 회원님의 수신동의 여부와 무관하게 모든 회원님들께 발송됩니다.</span><br><span style='font-size: 16px;'>\n" + 
    			"                제 3자가 본인의 이메일 주소를 잘못 입력할 경우 타인의 메일이 전송될 수 있습니다.</span><br>\n" + 
    			"            </p></div>\n" + 
    			"        <div class='footer' style='width:800px; margin:0 auto; text-align:center; font-size:13px; line-height:25px; padding-bottom:50px; color:#999999'>\n" + 
    			"            <span>크레파스솔루션(주)  서울시 영등포구 여의나루로 53-1, 905호  대표전화 : 02-1522-2975  Email : info@crepass.com <br>\n" + 
    			"                Copyright (C) 2020 CrePASS All rights reserved.  </span></div>\n" + 
    			"        <p><br></p>\n" + 
    			"    </body>\n" + 
    			"</html>";
    			

    	return emailBody;
    }
}