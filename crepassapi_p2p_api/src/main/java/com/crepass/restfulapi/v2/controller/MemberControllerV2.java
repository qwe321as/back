package com.crepass.restfulapi.v2.controller;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.Slack;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.cre.domain.CreIntro;
import com.crepass.restfulapi.cre.domain.CreMember;
import com.crepass.restfulapi.cre.service.CreMemberService;
import com.crepass.restfulapi.cre.service.IntroService;
import com.crepass.restfulapi.inside.service.DepositService;
import com.crepass.restfulapi.one.domain.MariMember;
import com.crepass.restfulapi.one.domain.OneCertify;
import com.crepass.restfulapi.one.service.EmoneyService;
import com.crepass.restfulapi.one.service.OneMemberService;
import com.crepass.restfulapi.v2.domain.Member;
import com.google.gson.Gson;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;

import io.swagger.annotations.ApiOperation;
import net.gpedro.integrations.slack.SlackMessage;

@CrossOrigin
@RestController
@RequestMapping(path = "/api2", method = {RequestMethod.POST, RequestMethod.GET})
public class MemberControllerV2 {
	
	@Autowired
    private OneMemberService oneMemberService;
	
	@Autowired
    private CreMemberService creMemberService;
	
	@Autowired
    private IntroService introService;
	
	@Autowired
    private DepositService depositService;
	
    @Autowired
    private EmoneyService emoneyService;
    
	@Autowired(required=true)
	private HttpServletRequest request;
	
	@Autowired
    private CommonUtil commonUtil;
	
	@ApiOperation(value = "로그인")
	@RequestMapping("/members/loginSocial")
	public ResponseEntity<ResponseResult> memberLogin(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
		JSONObject jsonMember = new JSONObject(requestString);
		ResponseResult response = new ResponseResult();
		response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
		String mid = ((JSONObject) jsonMember.get("request")).get("mid").toString();
		String playerId = ((JSONObject) jsonMember.get("request")).get("playerId").toString();
		
		String m_no = oneMemberService.selectUserIdCheck(mid);
		
		if(m_no != null && !m_no.equals("null") && m_no.length() > 0) {
			JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload(mid));
		
			byte[] sharedKey = new byte[32];
			new SecureRandom().nextBytes(sharedKey);
			
			MACSigner macSigner = new MACSigner(sharedKey);
			
			jwsObject.sign(macSigner);
			String token = jwsObject.serialize();
			
			if (!token.isEmpty()) {
				
				boolean isToken=false; 
				
				if(!m_no.equals("8007"))						// 해당계정은 토큰을 업데이트 하지 않음(8007 : mhson@crepass.com), CrePASSApiTokenInterceptor 여기도 무시
					isToken = oneMemberService.updateMemberAuthToken(mid, token);
				else
					isToken = true;
				
				final boolean isTokenUpdate = isToken;
				
				if(isTokenUpdate) {
					CreIntro creIntro = introService.selectIntroInfo();
					
					Map<String, Object> result = new HashMap<String, Object>();
					result.put("appVersion", creIntro.getAppVersion());
					result.put("authToken", token);
					
					String isAutoInvset = oneMemberService.selectAutoInvestAgree(mid);
					if(isAutoInvset == null || isAutoInvset.equals("null") || isAutoInvset.length() < 1) {
	                	isAutoInvset = "N";
					}
					
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

		            resultInfo.put("alarm",creMember.getAlarm());
		            resultInfo.put("tdiaryMsg",creMember.getTdiaryMsg());
		                
		            result.put("memberInfo", resultInfo);
		            response.setResult(result);
					
		            creMemberService.updateMemberByPlayerId(mid, playerId);
				} else {
	                response.setState(204);
	                response.setMessage("계정정보가 존재하지 않습니다.");
	                response.setResult(jsonMember.get("request").toString());
	            }
			} else {
	            response.setState(203);
	            response.setMessage("계정정보가 존재하지 않습니다.");
	            response.setResult(jsonMember.get("request").toString());
	        }
		} else {
        	response.setState(202);
            response.setMessage("등록된 계정이 아닙니다.\n회원가입후 이용해주세요.");
        }
		
		// 디버깅시 에러
//		commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
		commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
		
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
	}

//	@ApiOperation(value = "MVP 예치금 정보")
//	@RequestMapping("/members/invest/payment/mvp")
//	public ResponseEntity<ResponseResult> memberInvestPaymentMVP(@RequestBody String requestString) throws Exception {
//		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
//        commonUtil.sendRequestLogging(mapping_url, requestString);
//		
//		JSONObject jsonMember = new JSONObject(requestString);
//		ResponseResult response = new ResponseResult();
//		response.setState(200);
//        response.setMessage("정상적으로 처리하였습니다.");
//
//		String mid = ((JSONObject) jsonMember.get("request")).get("mid").toString();
//		
//		String custId = oneMemberService.selectCustID(mid);
//		
//		// 입금:실제 입금 총액
//		long totalDepositAmt = depositService.selectInvestTotalDepositAmt(custId);	//IB_FB_P2P_IP-투자자 총 입금합
//
//		// 입금:정산된 수입의 총액
//		
//		// 출금:투자(펀딩중) 총액
//		String investPayment = emoneyService.selectEmoneyInvestIsPlaying(mid);							// mm,mi,mip-현재 투자한 금액중 회수되지 않은 투자액의 합
//		// 출금:투자(펀딩완료) 총액
//		// 출금:타행실출금예정금액
//		String withdrawPay = emoneyService.selectEmoneyWithdrawPay2(mid);								// cwt-투자자의 출금처리되지 않은(trx_flag=N) 금액의 합
//		// 출금:타행실출금액
//		
//		// 디버깅시 에러
//		//commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
//		
//        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
//	}
	
	@ApiOperation(value = "회원 가입")
    @RequestMapping(value="/members/add",produces="application/json;charset=UTF-8")
    @Transactional("oneTransactionManager")
	public ResponseEntity<ResponseResult> memberAdd(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
        
		JSONObject jsonMember = new JSONObject(requestString);

		// 1차 수정(구분자 추가)
		// V2는 Android 회원이라고 가정 influx추가 201127
		if (jsonMember.getJSONObject("request").has("influxType")) {
			jsonMember.getJSONObject("request").put("influxType", "Updang");
		} else {
			jsonMember.getJSONObject("request").put("influxType", "Android");
		}
			
		ResponseResult response = new ResponseResult();
		response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject request = jsonMember.getJSONObject("request");
        
        String mid = request.getString("mid");
        
        // 회원중복가입체크(이름,핸드폰,통신사)
        if(oneMemberService.isDuplicate(jsonMember) > 0) {
        	response.setState(438);
			response.setMessage("이미 등록된 회원입니다.");
			commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
			return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
        }
 
        
        final boolean isPass = oneMemberService.checkOneMember(jsonMember);
        
        if(isPass) {

        	CreMember creMemberCheck = creMemberService.selectMemberById(mid);
        	
        	if(creMemberCheck==null) {
        		
        		JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload(request.get("mid").toString()));
     			
     			byte[] sharedKey = new byte[32];
     			new SecureRandom().nextBytes(sharedKey);
     			
     			MACSigner macSigner = new MACSigner(sharedKey);
     			
     			jwsObject.sign(macSigner);
     			String token = jwsObject.serialize();
     			
        		// 회원등록
        		int retOne = oneMemberService.insertMember(jsonMember, token);
        		if(retOne>0) {
        			CommonUtil commonUtil = new CommonUtil();
	                    
	                JSONObject requestSyfert = new JSONObject();
	                JSONObject jsonSyfert = new JSONObject();
        				
	                String name = request.getString("name");
	                    
	                jsonSyfert.put("mid", mid);
	                jsonSyfert.put("name", name);
	                jsonSyfert.put("memGuid", "N");
	                jsonSyfert.put("ipAddr", commonUtil.getRemoteAddrs());
	                jsonSyfert.put("memUse", "Y");
	                jsonSyfert.put("telhp", request.getString("telhp"));
	                requestSyfert.put("request", jsonSyfert);
	                oneMemberService.addOneSeyfert(requestSyfert);
	                	
	                MariMember mariMember = oneMemberService.selectMemberById(mid);
		            
	                // 약관동의서 등록
	                CreMember creMember = creMemberService.addCreAgreeMember(jsonMember, mariMember.getCustId());
	                
	                if(creMember!=null) {
		               	OneCertify oneCertify = new OneCertify();
			            oneCertify.setMno(mariMember.getMno());
			            oneCertify.setCertifyType(request.getString("certifyType"));
			            oneCertify.setCertifyResult(request.getString("certifyResult").toString());
			        
			            oneMemberService.insertOneCertify(oneCertify);
			                
			            CreIntro creIntro = introService.selectIntroInfo();
							
						Map<String, Object> result = new HashMap<String, Object>();
						result.put("appVersion", creIntro.getAppVersion());
						result.put("authToken", token);
							
						String isAutoInvset = oneMemberService.selectAutoInvestAgree(mid);
						if(isAutoInvset == null || isAutoInvset.equals("null") || isAutoInvset.length() < 1) {
			               	isAutoInvset = "N";
						}
							
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
				                
				        CreMember member = creMemberService.selectMemberById(mid);
				           
				        resultInfo.put("alarm",member.getAlarm());
				        resultInfo.put("tdiaryMsg",member.getTdiaryMsg());
				                
				        result.put("memberInfo", resultInfo);
				        response.setResult(result);
	                }
        		} else {
        			response.setState(211);
    	            response.setMessage("패스워드가 일치하지 않습니다.");
        		}
        	} else {
        		response.setState(206);
	            response.setMessage("기존에 가입한 내역이 있는 계정입니다.");
        	}
        } else {

        	response.setState(203);
            response.setMessage("ID가 이미 존재합니다.");
            response.setResult(jsonMember.get("request").toString());
        }
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK); 
	}
	
	@ApiOperation(value = "회원 가입(MVP)")
    @RequestMapping(value="/members/addmvp",produces="application/json;charset=UTF-8")
    @Transactional("oneTransactionManager")
	public ResponseEntity<ResponseResult> memberMVPAdd(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
        
		JSONObject jsonMember = new JSONObject(requestString);

		// Itdang 회원이라고 가정 influx추가 201127
		jsonMember.getJSONObject("request").put("influxType", "Itdang");
				
		ResponseResult response = new ResponseResult();
		response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject request = jsonMember.getJSONObject("request");
        
        String mid = request.getString("mid");
        
        // 법인일경우
		String userGb = request.getString("userGb");
		if (userGb.equals("C")){
			jsonMember.getJSONObject("request").put("gender", "");
			jsonMember.getJSONObject("request").put("birth", "0000-00-00");
			jsonMember.getJSONObject("request").put("blindness", "N");
			jsonMember.getJSONObject("request").put("telcoGb", "");
		}
		else {
			jsonMember.getJSONObject("request").put("blindness", "N"); // 개인일 경우 본인인증 Y
		}
		jsonMember.getJSONObject("request").put("signPurpose", "N");	// 개인 기업 모두 일반투자자로 구분
		
        // 회원중복가입체크
        if(oneMemberService.isDuplicate(jsonMember) > 0) {
        	response.setState(438);
			response.setMessage("이미 등록된 회원입니다.");
			return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
        }
        
        final boolean isPass = oneMemberService.checkOneMember(jsonMember);
        
        if(isPass) {
        	CreMember creMemberCheck = creMemberService.selectMemberById(mid);
        	
        	if(creMemberCheck==null) {
        		
        		JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload(request.get("mid").toString()));
     			
     			byte[] sharedKey = new byte[32];
     			new SecureRandom().nextBytes(sharedKey);
     			
     			MACSigner macSigner = new MACSigner(sharedKey);
     			
     			jwsObject.sign(macSigner);
     			String token = jwsObject.serialize();
     			
     			// 회원등록
        		int retOne = oneMemberService.insertMember(jsonMember, token);
        		if(retOne>0) {
        			CommonUtil commonUtil = new CommonUtil();
	                    
	                JSONObject requestSyfert = new JSONObject();
	                JSONObject jsonSyfert = new JSONObject();
        				
	                String name = request.getString("name");
	                    
	                jsonSyfert.put("mid", mid);
	                jsonSyfert.put("name", name);
	                jsonSyfert.put("memGuid", "N");
	                jsonSyfert.put("ipAddr", commonUtil.getRemoteAddrs());
	                jsonSyfert.put("memUse", "Y");
	                jsonSyfert.put("telhp", request.getString("telhp"));
	                requestSyfert.put("request", jsonSyfert);
	                oneMemberService.addOneSeyfert(requestSyfert);
	                
	                MariMember mariMember = oneMemberService.selectMemberById(mid);
		            
	                // 약관동의서 등록
	                CreMember creMember = creMemberService.addCreAgreeMember(jsonMember, mariMember.getCustId());
	                
	                if(creMember!=null) {
	                	
	                	if (!userGb.equals("C")){						// 법인이 아니면
			               	OneCertify oneCertify = new OneCertify();
				            oneCertify.setMno(mariMember.getMno());
				            oneCertify.setCertifyType(request.getString("certifyType"));
				            oneCertify.setCertifyResult(request.getString("certifyResult").toString());
				            
				            oneMemberService.insertOneCertify(oneCertify);
	                	}    
			            CreIntro creIntro = introService.selectIntroInfo();
							
						Map<String, Object> result = new HashMap<String, Object>();
						result.put("appVersion", creIntro.getAppVersion());
						result.put("authToken", token);
							
						String isAutoInvset = oneMemberService.selectAutoInvestAgree(mid);
						if(isAutoInvset == null || isAutoInvset.equals("null") || isAutoInvset.length() < 1) {
			               	isAutoInvset = "N";
						}
							
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
				                
				        CreMember member = creMemberService.selectMemberById(mid);
				           
				        resultInfo.put("alarm",member.getAlarm());
				        resultInfo.put("tdiaryMsg",member.getTdiaryMsg());
				                
				        result.put("memberInfo", resultInfo);
				        response.setResult(result);
	                }
        		} else {
        			response.setState(211);
    	            response.setMessage("패스워드가 일치하지 않습니다.");
        		}
        	} else {
        		response.setState(206);
	            response.setMessage("기존에 가입한 내역이 있는 계정입니다.");
        	}
        } else {
        	response.setState(203);
            response.setMessage("ID가 이미 존재합니다.");
            response.setResult(jsonMember.get("request").toString());
        }
        
        if (userGb.equals("C")){
        	Slack.apiItdangMVP.call(new SlackMessage("#잇당mvp-유입채널","이재형", mid + "계정으로 잇당MVP 법인회원이 가입하셨습니다. 컨텍해서 사업자정보(사업자번호, 주소), 실계좌정보 요청 부탁드립니다.")); 
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
        
        response.setState(404);
		response.setMessage("파라미터명이 잘못되었습니다");
		response.setResult("");
		 
        String mid = jsonMember.getJSONObject("request").getString("mid");
        
        if(mid!=null) {
            response.setState(200);
            response.setMessage("정상적으로 처리하였습니다.");
            response.setResult(oneMemberService.getMember(mid));
        }
       
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
	
	@ApiOperation(value = "회원 정보 수정(파일전송)")
    @RequestMapping("/members/mod")
	public ResponseEntity<ResponseResult> memberModNProfileUpload(@ModelAttribute Member member, HttpServletRequest request) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, new Gson().toJson(member));
		
		ResponseResult response = new ResponseResult(); 
		
		int updateCount = oneMemberService.updateMember(member);
		response.setState(200);
		response.setMessage("정상적으로 처리하였습니다");
		
		switch(updateCount) {
			case -1 :
				response.setState(207);
				response.setMessage("현재 비밀번호와 동일합니다.");
				break;
				
			case -2 :
				response.setState(209);
				response.setMessage("새 비밀번호가 맞지 않습니다.");
				break;
				
			case -3 :
				response.setState(210);
				response.setMessage("비밀번호 변경에 누락된 값이 있습니다.");
				break;
				
			case -4 :
				response.setState(208);
				response.setMessage("비밀번호가 맞지 않습니다.");
				break;
				
			case -5 :
				response.setState(212);
				response.setMessage("닉네임 비공개처리에 실패하였습니다.");
				break;
				
			case -6 :
				response.setState(213);
				response.setMessage("회원정보 변경에 실패하였습니다.");
				break;
		}
		
		commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
		
		return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
	}
	
	@ApiOperation(value = "회원 정보 수정(기본)")
    @RequestMapping("/members/mod/basic")
	public ResponseEntity<ResponseResult> memberMod(@RequestBody String requestString, HttpServletRequest request) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
		ResponseResult response = new ResponseResult(); 
		
		int updateCount = oneMemberService.updateMemberBasic(requestString);
		response.setState(200);
		response.setMessage("정상적으로 처리하였습니다");
		
		switch(updateCount) {
			case -1 :
				response.setState(207);
				response.setMessage("현재 비밀번호와 동일합니다.");
				break;
				
			case -2 :
				response.setState(209);
				response.setMessage("새 비밀번호가 맞지 않습니다.");
				break;
				
			case -3 :
				response.setState(210);
				response.setMessage("비밀번호 변경에 누락된 값이 있습니다.");
				break;
				
			case -4 :
				response.setState(208);
				response.setMessage("비밀번호가 맞지 않습니다.");
				break;
				
			case -5 :
				response.setState(212);
				response.setMessage("닉네임 비공개처리에 실패하였습니다.");
				break;
				
			case -6 :
				response.setState(213);
				response.setMessage("회원정보 변경에 실패하였습니다.");
				break;
		}
		
		commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
		
		return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
	}
	
	@ApiOperation(value = "투자자 기본 등록 정보")
    @RequestMapping("/members/invest/info/get")
	public ResponseEntity<ResponseResult> getMemberInvestInfo(@RequestBody String requestString, HttpServletRequest request) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
		ResponseResult response = new ResponseResult(); 

		JSONObject jsonMember = new JSONObject(requestString);
		String mid = jsonMember.getJSONObject("request").getString("mid");
		
		Map<String, Object> resultInfo = new HashMap<String, Object>();
        resultInfo.put("info", oneMemberService.selectMemberInvestInfo(mid));
		
		response.setState(200);
		response.setMessage("정상적으로 처리하였습니다");
		response.setResult(resultInfo);
		
		commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
		
		return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
	}
	
	
}
