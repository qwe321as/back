package com.crepass.restfulapi.one.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerMapping;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.cre.dao.CreMemberMapper;
import com.crepass.restfulapi.cre.domain.CreMember;
import com.crepass.restfulapi.ks.service.VirAccntService;
import com.crepass.restfulapi.one.domain.OneVirtualAccnt;
import com.crepass.restfulapi.one.domain.OneVirtualRealAccnt;
import com.crepass.restfulapi.one.domain.OneInvest;
import com.crepass.restfulapi.one.domain.OneLoanVirtualAccntInfo;
import com.crepass.restfulapi.one.domain.OneMemberCustAddInfo;
import com.crepass.restfulapi.one.domain.OneMemberCustAddInfo2;
import com.crepass.restfulapi.one.domain.OneMemberCustId;
import com.crepass.restfulapi.one.domain.OneOutPay;
import com.crepass.restfulapi.one.domain.OneSeyfertyVirtual;
import com.crepass.restfulapi.one.service.EmoneyService;
import com.crepass.restfulapi.one.service.LoanService;
import com.crepass.restfulapi.one.service.OneMemberService;
import com.crepass.restfulapi.one.service.VirtualAccntService;

import io.swagger.annotations.ApiOperation;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@CrossOrigin
@RestController
@RequestMapping(path = "/api", method = RequestMethod.POST)
public class VirtualaccntController {
    
	@Autowired
    private VirtualAccntService virtualAccntService;
	
	@Autowired
    private VirAccntService virAccntService;
	
	@Autowired
    private OneMemberService oneMemberService;
	
	@Autowired
    private EmoneyService emoneyService;
	
	@Autowired
    private LoanService loanService;
	
	@Autowired
    private CreMemberMapper creMemberMapper;
	
	@Autowired
    private CommonUtil commonUtil;
    
    @Autowired(required=true)
	private HttpServletRequest request;
	
	@Value("${crepas.inside.url}")
    private String insideUrl;
	
	@ApiOperation(value = "가상계좌 생성 (투자자) 및 은행에 투자자 등록")
    @RequestMapping("/virtualaccnt/create")
    public ResponseEntity<ResponseResult> setVirtualaccnt(@RequestBody String requestString) throws Exception {
        
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
		JSONObject json = new JSONObject(requestString);
        
        final String mid = ((JSONObject) json.get("request")).get("mid").toString();

        OneVirtualRealAccnt accountById = virtualAccntService.selectAccountById(mid);
        
        ResponseResult response = new ResponseResult();
        
        if(accountById == null) {
        	response.setState(487);
            response.setMessage("회원가입되지 않은 계정입니다.");
        } else {
        	String oneSeyfertyVirtual = virtualAccntService.selectVirtualaccnt(mid);						// ms에서 s_accntNo가 null이면 생성
        	
        	if (oneSeyfertyVirtual == null || oneSeyfertyVirtual.length() < 1) {
        		OneVirtualAccnt oneVertualAccnt = virtualAccntService.selectVirtualAccntInfo();				// crepass에 virtual_account 한개선택;
            	
            	if(oneVertualAccnt == null) {
                    response.setState(488);
                    response.setMessage("가상계좌 생성 오류, 고객센터에 문의 바랍니다.");
            	} else {
            		OneMemberCustAddInfo oneMemberCustAddInfo = oneMemberService.selectCustAddInfo(mid);
            		
            		if(oneMemberCustAddInfo == null || oneMemberCustAddInfo.getMyBankacc().length() < 1) {
            			response.setState(482);
                        response.setMessage("출금계좌를 등록 후 가상계좌생성을 진행해주세요.");
            		} else {
            		
	            		OneMemberCustId oneMemberCustId = oneMemberService.selectCustID2(mid);				// mm 이름과 custId 선택
	            		
	            		OneSeyfertyVirtual oneSeyfertyVirtualUpdate = new OneSeyfertyVirtual();
	            		oneSeyfertyVirtualUpdate.setMid(mid);
	            		oneSeyfertyVirtualUpdate.setAccntNo(oneVertualAccnt.getAccount());
	            		oneSeyfertyVirtualUpdate.setBnkCd("088");
	            		virtualAccntService.updateMemberAccnt(oneSeyfertyVirtualUpdate);					// ms에 새로운 가상계좌로 update
	            		virtualAccntService.updateVirtualaccntUse(oneVertualAccnt.getAccount());			// crepass에 virtual_account 사용중으로 update;
	            		int isVirAccntUse = virAccntService.updateVirAccntUse(oneVertualAccnt.getAccount(), oneMemberCustId.getName() + "_크레파스", oneMemberCustId.getCustId());
	            		
	            		if(isVirAccntUse > 0) {
		            		Map<String, Object> result = new HashMap<String, Object>();
		            		result.put("mid", mid);
		            		result.put("sAccntNo", oneVertualAccnt.getAccount());
		            		result.put("sBnkCd", "088");
		            		
		                    RestTemplate restTemplate = new RestTemplate();
		                    Map<String, String> vars = new HashMap<String, String>();
		                    vars.put("CUST_ID", oneMemberCustAddInfo.getCustId());
		                    
		                    String resultSearch = restTemplate.postForObject(insideUrl + "/customer/search", vars, String.class);
		                    JSONObject jsonSearch = new JSONObject(resultSearch);
		                    
		                    int searchState = jsonSearch.getInt("STATE");
		                    
		                    if(searchState != 200) {
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
		                        vars.put("CUST_NM", oneMemberCustAddInfo.getName());
		                        vars.put("CUST_SUB_NM", "");
		                        vars.put("REP_NM", "");
		                        vars.put("BIRTH_DATE", oneMemberCustAddInfo.getBirth().replace("-", ""));
		                        vars.put("SUP_REG_NB", "");
		                        vars.put("PRI_SUP_GBN", "1");
		                        vars.put("HP_NO1", hpFront);
		                        vars.put("HP_NO2", hpMiddle);
		                        vars.put("HP_NO3", hpBack);
		                        vars.put("BANK_CD", oneMemberCustAddInfo.getMyBankcode());
		                        vars.put("ACCT_NB", oneMemberCustAddInfo.getMyBankacc());
		                        vars.put("CMS_NB", oneVertualAccnt.getAccount());
		                    
		                        String resultCustAdd = restTemplate.postForObject(insideUrl + "/customer/add", vars, String.class);
		                        JSONObject jsonCustAdd = new JSONObject(resultCustAdd);
		                        
		                        int custAddState = jsonCustAdd.getInt("STATE");
		                        System.out.println(jsonCustAdd.getString("MESSAGE"));

		                        
		                        if(custAddState == 200) {
		                        	response.setState(200);
		                            response.setMessage("정상적으로 처리하였습니다.");
		                            response.setResult(result);
		                        } else {
		                        	response.setState(jsonCustAdd.getInt("STATE"));
		                            response.setMessage(jsonCustAdd.getString("MESSAGE"));
		                        }
		                    } else {
			            		response.setState(200);
			    				response.setMessage("정상적으로 처리하였습니다.");
			    				response.setResult(result);
		                    }
	            		} else {
	            			response.setState(488);
	                        response.setMessage("가상계좌 발급 도중 에러가 발생했습니다. 고객센터에 문의하세요.");
	                        throw new RuntimeException("가상계좌 발급 도중 에러가 발생했습니다. 고객센터에 문의하세요.");
	            		}
            		}
            	}
            } else {
            	response.setState(489);
                response.setMessage("이미 가상계좌를 발급 받았습니다.");
            }
        }
        
        // 디버깅시 에러
//        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
	
	@ApiOperation(value = "가상계좌 생성 (법인 투자자)")
    @RequestMapping("/virtualaccnt/company/create")
    public ResponseEntity<ResponseResult> setVirtualaccnt2(@RequestBody String requestString) throws Exception {
        
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
		JSONObject json = new JSONObject(requestString);
        
        final String mid = ((JSONObject) json.get("request")).get("mid").toString();

        OneVirtualRealAccnt accountById = virtualAccntService.selectAccountById(mid);
        
        ResponseResult response = new ResponseResult();
        
        if(accountById == null) {
        	response.setState(487);
            response.setMessage("회원가입되지 않은 계정입니다.");
        } else {
        	String oneSeyfertyVirtual = virtualAccntService.selectVirtualaccnt(mid);
        	
        	if (oneSeyfertyVirtual == null || oneSeyfertyVirtual.length() < 1) {
        		OneVirtualAccnt oneVertualAccnt = virtualAccntService.selectVirtualAccntInfo();
            	
            	if(oneVertualAccnt == null) {
                    response.setState(488);
                    response.setMessage("가상계좌 생성 오류, 고객센터에 문의 바랍니다.");
            	} else {
            		OneMemberCustAddInfo2 oneMemberCustAddInfo = oneMemberService.selectCustAddInfo2(mid);
            		
            		if(oneMemberCustAddInfo == null || oneMemberCustAddInfo.getMyBankacc().length() < 1) {
            			response.setState(482);
                        response.setMessage("출금계좌를 등록 후 가상계좌생성을 진행해주세요.");
            		} else {
            		
            			CreMember creMember = new CreMember();
                        creMember.setMid(mid);
                        creMember.setCharType("1");
                        creMember.setName(oneMemberCustAddInfo.getName());
                    	creMemberMapper.insertCreMember(creMember);
            			
	            		OneMemberCustId oneMemberCustId = oneMemberService.selectCustID2(mid);
	            		
	            		OneSeyfertyVirtual oneSeyfertyVirtualUpdate = new OneSeyfertyVirtual();
	            		oneSeyfertyVirtualUpdate.setMid(mid);
	            		oneSeyfertyVirtualUpdate.setAccntNo(oneVertualAccnt.getAccount());
	            		oneSeyfertyVirtualUpdate.setBnkCd("088");
	            		virtualAccntService.updateMemberAccnt(oneSeyfertyVirtualUpdate);
	            		virtualAccntService.updateVirtualaccntUse(oneVertualAccnt.getAccount());
	            		int isVirAccntUse = virAccntService.updateVirAccntUse(oneVertualAccnt.getAccount(), oneMemberCustId.getName() + "_크레파스", oneMemberCustId.getCustId());
	            		
	            		if(isVirAccntUse > 0) {
		            		Map<String, Object> result = new HashMap<String, Object>();
		            		result.put("mid", mid);
		            		result.put("sAccntNo", oneVertualAccnt.getAccount());
		            		result.put("sBnkCd", "088");
		            		
		                    RestTemplate restTemplate = new RestTemplate();
		                    Map<String, String> vars = new HashMap<String, String>();
		                    vars.put("CUST_ID", oneMemberCustAddInfo.getCustId());
		                    
		                    String resultSearch = restTemplate.postForObject(insideUrl + "/customer/search", vars, String.class);
		                    JSONObject jsonSearch = new JSONObject(resultSearch);
		                    
		                    int searchState = jsonSearch.getInt("STATE");
		                    
		                    if(searchState != 200) {
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
		                        vars.put("CMS_NB", oneVertualAccnt.getAccount());
		                        
		                        String resultCustAdd = restTemplate.postForObject(insideUrl + "/customer/add", vars, String.class);
		                        JSONObject jsonCustAdd = new JSONObject(resultCustAdd);
		                        
		                        int custAddState = jsonCustAdd.getInt("STATE");
		                        
		                        if(custAddState == 200) {
		                        	response.setState(200);
		                            response.setMessage("정상적으로 처리하였습니다.");
		                            response.setResult(result);
		                        } else {
		                        	response.setState(jsonCustAdd.getInt("STATE"));
		                            response.setMessage(jsonCustAdd.getString("MESSAGE"));
		                        }
		                    } else {
			            		response.setState(200);
			    				response.setMessage("정상적으로 처리하였습니다.");
			    				response.setResult(result);
		                    }
	            		} else {
	            			response.setState(488);
	                        response.setMessage("가상계좌 발급 도중 에러가 발생했습니다. 고객센터에 문의하세요.");
	                        throw new RuntimeException("가상계좌 발급 도중 에러가 발생했습니다. 고객센터에 문의하세요.");
	            		}
            		}
            	}
            } else {
            	response.setState(489);
                response.setMessage("이미 가상계좌를 발급 받았습니다.");
            }
        }
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
	
	@ApiOperation(value = "출금신청 (예치금) - 앱")
    @RequestMapping("/virtualaccnt/payWithdraw")
	@Transactional("oneTransactionManager")
    public ResponseEntity<ResponseResult> sendPayWithdraw(@RequestBody String requestString) throws Exception {
		
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
		JSONObject json = new JSONObject(requestString);
		JSONObject request = (JSONObject) json.get("request");
        
        final String mid = request.getString("mid");
        final String o_pay = request.getString("o_pay");
        
        ResponseResult response = new ResponseResult();
        
        OneVirtualRealAccnt oneVirtualRealAccnt = virtualAccntService.selectAccountById(mid);	// 실계좌정보(m_my_bankacc, m_my_bankcode, 예치금:m_emoney등) 선택
        
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("BANK_CD", oneVirtualRealAccnt.getMyBankcode());								
        vars.put("ACCT_NB", oneVirtualRealAccnt.getMyBankacc());
        
        String resultAccnt = restTemplate.postForObject(insideUrl + "/lookup/reciever", vars, String.class);	// 은행코드,계좌번호 확인 전문요청
        JSONObject jsonAccnt = new JSONObject(resultAccnt);
        
        if(jsonAccnt.getInt("STATE") == 200) {
        	if(Long.parseLong(o_pay) <= Long.parseLong(oneVirtualRealAccnt.getEmoney())) {
        		
        		int ranNum = (int)(Math.random() * (999 - 111 + 1)) + 111;
				long time = System.currentTimeMillis();
				String gCode = "P" + String.valueOf(time) + String.valueOf(ranNum);
        		
        		OneInvest oneInvest = new OneInvest();
        		oneInvest.setGCode(gCode);
        		oneInvest.setMid(mid);
        		oneInvest.setMName(oneVirtualRealAccnt.getMname());
        		oneInvest.setSubject("잔액 출금신청");
        		oneInvest.setLoanId(String.valueOf(0));
        		oneInvest.setIPay(o_pay);
        		
        		OneOutPay oneOutPay = new OneOutPay();
        		oneOutPay.setGCode(gCode);
        		oneOutPay.setMid(mid);
        		oneOutPay.setMName(oneVirtualRealAccnt.getMname());
        		oneOutPay.setPay(o_pay);
        		oneOutPay.setIp(commonUtil.getRemoteAddrs());
        		
        		int accntWithdraw = virtualAccntService.insertAccntWithdraw(oneInvest);					// mso-출금정보입력
        		int accntWithdrawHistory = virtualAccntService.insertAccntWithdrawHistory(oneOutPay);	// mari_outpay-출금정보입력
        		
        		if(accntWithdraw > 0 && accntWithdrawHistory > 0) {
    				if(Long.parseLong(oneVirtualRealAccnt.getEmoney()) < Long.parseLong(o_pay)) {
    					response.setState(314);
        				response.setMessage("예치금보다 많은 금액을 출금신청 할 수 없습니다.");
    				} else {
    					// 4. cwt 출금이므로 회차정보 없음
    					if(!virtualAccntService.insertAccntWithdrawSchedule(mid, o_pay, "I")) {
    	        			response.setState(322);
    	                    response.setMessage("출금 처리중 에러가 발생했습니다.");
    	                    return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    	        		}
    					
    					String investPayment = emoneyService.selectEmoneyInvestIsPlaying(mid);
    			        String withdrawPay = emoneyService.selectEmoneyWithdrawPay2(mid);
    			        
    			        if(investPayment == null)
    			        	investPayment = "0";
    			        
//    			        restTemplate = new RestTemplate();
    			        vars = new HashMap<String, String>();
    			        vars.put("CUST_ID", oneVirtualRealAccnt.getCustId());
    			        
    			        String resultAMT = restTemplate.postForObject(insideUrl + "/lookup/customer", vars, String.class);	// cust_id 확인 전문요청
    			        JSONObject jsonResult = new JSONObject(resultAMT);
    		        
    			        int STATE = jsonResult.getInt("STATE");
    			        int isUserMoney = 1;
    			        long payment = 0;
    			        
    			        if(STATE == 200) {
    			        	String balanceAMT = ((JSONObject)jsonResult.get("RESULT")).getString("BALANCE_AMT");
    			        	payment = Long.parseLong(balanceAMT) - Long.parseLong(investPayment) - Long.parseLong(withdrawPay);
    			        	isUserMoney = virtualAccntService.updateUserMoney(mid, String.valueOf(payment));
    			        } else
    			        	payment = (long)Long.parseLong(oneVirtualRealAccnt.getEmoney());
    			        
//    			        long setEmoney = ((long)(emoney) - Long.parseLong(investPayment)) - Long.parseLong(withdrawPay);
//    					long payment = (Long.parseLong(oneVirtualRealAccnt.getEmoney()) - (Long.parseLong(investPayment)) - Long.parseLong(withdrawPay));
    					
//    					int isUserMoney = virtualAccntService.updateUserMoney(mid, String.valueOf(payment));
    					if(isUserMoney > 0) {
            				Map<String, Object> result = new HashMap<String, Object>();
                    		result.put("payment", String.valueOf(payment));
    						
        	        		response.setState(200);
            				response.setMessage("정상적으로 처리하였습니다.");
            				response.setResult(result);
        	        	} else {
        	        		response.setState(322);
                            response.setMessage("출금 처리중 에러가 발생했습니다.");
        	        	}
    				}
    				
        		} else {
        			response.setState(321);
                    response.setMessage("출금 신청중 에러가 발생했습니다.");
        		} 
        	} else {
        		response.setState(320);
                response.setMessage("출금 잔액이 부족합니다.");
        	}
        } else {
    		response.setState(319);
            response.setMessage("전문에러 발생. 잠시 후 다시 이용해주세요.");
    	}
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
		return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
	}
	
	@ApiOperation(value = "가상계좌 생성 (대출자)")
	@Transactional("oneTransactionManager")
    @RequestMapping("/loan/virtualaccnt/create")
    public ResponseEntity<ResponseResult> setLoanVirtualaccnt(@RequestBody String requestString) throws Exception {
        
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
		JSONObject json = new JSONObject(requestString);
        
		ResponseResult response = new ResponseResult();
		
        final String loanId = ((JSONObject) json.get("request")).get("loanId").toString();

        OneLoanVirtualAccntInfo loanVirtualAccnt = virtualAccntService.selectLoanVirtualaccnt(loanId);
        
        if(loanVirtualAccnt.getLoanAccntNo() == null || loanVirtualAccnt.getLoanAccntNo().isEmpty()) {
        	OneVirtualAccnt oneVertualAccntLoan = virtualAccntService.selectVirtualAccntLoanInfo();
			
			if(oneVertualAccntLoan == null) {
                response.setState(312);
                response.setMessage("가상계좌 생성 오류, 조회할 대출자 가상계좌가 없습니다.");
        	} else {
        		boolean isUpdateLoanAccnt = loanService.updateLoanAccnt(loanId, oneVertualAccntLoan.getAccount());
        		if(isUpdateLoanAccnt)
        			loanService.updateMemberLoanUse(loanVirtualAccnt.getMid());
        		
        		virtualAccntService.updateVirtualaccntUse(oneVertualAccntLoan.getAccount());
        		int isVirAccntUse = virAccntService.updateVirAccntUse(oneVertualAccntLoan.getAccount(), loanVirtualAccnt.getName() + "_크레파스", loanVirtualAccnt.getCustId());
        		
        		if(isVirAccntUse <= 0)
        			throw new RuntimeException("대출자 가상계좌 발급 도중 에러가 발생했습니다. mid : " + loanVirtualAccnt.getMid());
        		else {
        			Map<String, Object> result = new HashMap<String, Object>();
            		result.put("loanAccntNo", oneVertualAccntLoan.getAccount());
        			
        			response.setState(200);
        			response.setResult(result);
                    response.setMessage("정상적으로 처리하였습니다.");
        		}
        	}
        	
        } else {
        	response.setState(311);
            response.setMessage("이미 생성된 대출자 가상계좌가 있습니다.");
        }
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
}