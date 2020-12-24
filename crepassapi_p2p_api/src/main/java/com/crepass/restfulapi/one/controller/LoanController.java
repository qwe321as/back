package com.crepass.restfulapi.one.controller;

import com.crepass.restfulapi.common.AES256Cipher;
import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
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
import com.crepass.restfulapi.one.domain.OneOrderDataInfo;
import com.crepass.restfulapi.one.service.LoanService;
import com.crepass.restfulapi.one.service.OneMemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.swagger.annotations.ApiOperation;

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
public class LoanController {

	@Autowired
    private LoanService loanService;
	
	@Autowired
    private CommonUtil commonUtil;
    
	@Autowired
    private OneMemberService oneMemberService;
	
	@Value("${crepas.url.myloan}")
    private String myLoanUrl;
	
	@Value("${crepas.sms.url}")
    private String smsUrl;
	
	@Autowired(required=true)
	private HttpServletRequest request;
	
    @ApiOperation(value = "대출신청 카테고리")
    @RequestMapping("/loan/setcategory")
    public ResponseEntity<ResponseResult> addLoanCategory(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String mid = jsonRequest.get("mid").toString();
        
        String loanRecentId = loanService.selectLoanRecentId(mid);
        
        if(loanRecentId != null) {
        	OneLoanCategory oneLoanCategory = new OneLoanCategory();
        	oneLoanCategory.setLoanId(loanRecentId);
        	oneLoanCategory.setGoal(jsonRequest.getString("goal"));
        	oneLoanCategory.setSocialCorp(jsonRequest.getString("socialCorp"));
        	oneLoanCategory.setCorpStartDt(jsonRequest.getString("corpStartDt"));
        	oneLoanCategory.setCorpEndDt(jsonRequest.getString("corpEndDt"));

        	JSONArray arrayCategory = jsonRequest.getJSONArray("category");
        	
        	for(int i = 0; i < arrayCategory.length(); i++) {
        		JSONObject jsonCategory = arrayCategory.getJSONObject(i);
        		loanService.insertLoanCategoryInfo(loanRecentId, jsonCategory.getString("categoryId"));
        	}
        	
        	boolean insertLoanCategory = loanService.insertLoanCategory(oneLoanCategory);
        	if(!insertLoanCategory) {
        		response.setState(367);
                response.setMessage("카테고리등록에 실패하였습니다.");
        	}
        } else {
        	response.setState(366);
            response.setMessage("대출건이 조회되지 않아 카테고리등록이 불가합니다.");
        }
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "투자내역 상세")
    @RequestMapping("/loan/investdetail")
    public ResponseEntity<ResponseResult> getLoanInvestDetail(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String loanId = jsonRequest.get("loanId").toString();
        final String mid = jsonRequest.get("mid").toString();
        
        OneLoanInvestInfoDetail oneLoanInvestInfoDetail = loanService.selectLoanInvestInfoDetail(loanId);
        
        if(oneLoanInvestInfoDetail != null) {
        	Map<String, Object> result = new HashMap<String, Object>();
        	result.put("birth", oneLoanInvestInfoDetail.getBirth());
        	result.put("gender", oneLoanInvestInfoDetail.getGender());
        	result.put("businessname", oneLoanInvestInfoDetail.getBusinessname());
        	result.put("occu", oneLoanInvestInfoDetail.getOccu());
        	result.put("graduated", oneLoanInvestInfoDetail.getGraduated());
        	result.put("socialCorp", oneLoanInvestInfoDetail.getSocialCorp());
        	result.put("corpStartDt", oneLoanInvestInfoDetail.getCorpStartDt());
        	result.put("corpEndDt", oneLoanInvestInfoDetail.getCorpEndDt());
        	result.put("goal", oneLoanInvestInfoDetail.getGoal());
        	result.put("loanPose", oneLoanInvestInfoDetail.getLoanPose());
        	result.put("plan", oneLoanInvestInfoDetail.getPlan());
        	result.put("reply", loanService.selectLoanInvestInfoDetailReply(loanId, mid));
        	response.setResult(result);
        } 
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "대출자 메모 등록/수정")
    @RequestMapping("/loan/setmemo")
    public ResponseEntity<ResponseResult> setLoanMemo(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String oId = jsonRequest.get("oId").toString();
        final String memo = jsonRequest.get("memo").toString();
        
        String loanId = loanService.selectElementByLonId(oId);
        
        String isCommendRow = loanService.selectIsCommentRow(loanId, oId);
        
        if(isCommendRow != null) {
        	loanService.updateLoanMemo(isCommendRow, memo);
        } else {
        	OneLoanMemo oneLoanMemo = new OneLoanMemo();
        	oneLoanMemo.setLoanId(loanId);
        	oneLoanMemo.setOId(oId);
        	oneLoanMemo.setMemo(memo);
        	loanService.insertLoanMemo(oneLoanMemo);
        }
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "대출자 메모 조회")
    @RequestMapping("/loan/loanmemo")
    public ResponseEntity<ResponseResult> getLoanMemo(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String oId = jsonRequest.get("oId").toString();
        
        OneLoanMemoInfo oneLoanMemoInfo = loanService.selectLoanMemo(oId);
        
        if(oneLoanMemoInfo != null) {
        	Map<String, Object> result = new HashMap<String, Object>();
        	result.put("memoId", oneLoanMemoInfo.getId());
        	result.put("memo", oneLoanMemoInfo.getMemo());
        	result.put("createDt", oneLoanMemoInfo.getCreateDt());
        	result.put("heart", loanService.selectLoanHeart(oId));
        	response.setResult(result);
        }
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @SuppressWarnings("unchecked")
	@ApiOperation(value = "대출내역, 대출상환정보 조회")
    @RequestMapping("/loan/myloan")
    public ResponseEntity<ResponseResult> getMyloan(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String mid = jsonRequest.get("mid").toString();
        
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("m_id", mid);
        
        String resultLoan = restTemplate.postForObject(myLoanUrl, vars, String.class);
        JSONObject jsonLoan = new JSONObject(resultLoan);
        JSONArray jsonOrder = jsonLoan.getJSONArray("order_data");
        
        for(int i = 0; i < jsonOrder.length(); i++) {
        	JSONObject jsonList = jsonOrder.getJSONObject(i);
        	String oId = jsonList.getString("o_id");
        	OneLoanMemoInfo oneLoanMemoInfo = loanService.selectLoanMemo(oId);
            
        	Gson gsonBuilder = new GsonBuilder().create();
        	
            if(oneLoanMemoInfo != null) {
            	List<OneLoanMemoHeart> oneLoanMemoHearts = loanService.selectLoanHeart(oId);
            	jsonList.put("memoId", oneLoanMemoInfo.getId());
            	jsonList.put("memo", oneLoanMemoInfo.getMemo());
            	jsonList.put("createDt", oneLoanMemoInfo.getCreateDt());
            	
            	if(oneLoanMemoHearts != null)
            		jsonList.put("heart", new JSONArray(gsonBuilder.toJson(oneLoanMemoHearts)));
            	else
            		jsonList.put("heart", new JSONArray());
            } else {
            	jsonList.put("memoId", "");
            	jsonList.put("memo", "");
            	jsonList.put("createDt", "");
            	jsonList.put("heart", new JSONArray());
            }
        }
        HashMap<String,Object> result = new ObjectMapper().readValue(jsonLoan.toString(), HashMap.class);
        response.setResult(result);
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
	@ApiOperation(value = "대출내역, 대출상환정보 조회")
    @RequestMapping("/loan/myloan2")
    public ResponseEntity<ResponseResult> getMyloan2(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String mid = jsonRequest.get("mid").toString();
        
        List<OneLoanDataInfo> oneLoanDataInfos = loanService.selectLoanDataInfo(mid);
        List<OneOrderDataInfo> oneOrderDataInfos = loanService.selectOrderDataInfo(mid);
        
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("loan_data", oneLoanDataInfos);
        result.put("order_data", oneOrderDataInfos);
        
        for(int i = 0; i < oneLoanDataInfos.size(); i++) {
        	while(oneLoanDataInfos.size() > 0 && oneLoanDataInfos.get(i).getStatus() == null) {
        		oneLoanDataInfos.subList(i, i + 1).clear();
        	}
        	
        	if(oneLoanDataInfos.size() <= 0)
        		break;
        	
        	switch(oneLoanDataInfos.get(i).getStatus()) {
	        	case "N" :
	        		oneLoanDataInfos.get(i).setStatus("접수(심사대기)");
	        		break;
	        		
	        	case "E" :
	        		oneLoanDataInfos.get(i).setStatus("1차 심사 완료");
	        		break;
	        		
	        	case "A" :
	        		oneLoanDataInfos.get(i).setStatus("투자진행");
	        		break;
	        		
	        	case "Y" :
	        		oneLoanDataInfos.get(i).setStatus("대출실행");
	        		break;
        	}
        }
        
        for(int i = 0; i < oneOrderDataInfos.size(); i++) {
        	String oId = oneOrderDataInfos.get(i).getO_id();
        	
        	List<OneLoanMemoHeart> oneLoanMemoHearts = loanService.selectLoanHeart(oId);
        	oneOrderDataInfos.get(i).setHeart(oneLoanMemoHearts);
        }
        
        response.setResult(result);
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "투자자 하트 등록/수정")
    @RequestMapping("/loan/setheart")
    public ResponseEntity<ResponseResult> setHeart(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String memoId = jsonRequest.get("memoId").toString();
        final String loanId = jsonRequest.get("loanId").toString();
        final String mid = jsonRequest.get("mid").toString();
        final String heart = jsonRequest.get("heart").toString();
        
        String loanHeartInfo = loanService.selectLoanHeartInfo(memoId, mid);
        
        if(loanHeartInfo != null) {
        	loanService.updateLoanHeart(memoId, mid, heart);
        } else {
        	OneLoanHeart oneLoanHeart = new OneLoanHeart();
        	oneLoanHeart.setMemoId(memoId);
        	oneLoanHeart.setLoanId(loanId);
        	oneLoanHeart.setMid(mid);
        	oneLoanHeart.setHeart(heart);
        	
        	loanService.insertLoanHeart(oneLoanHeart);
        }
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }

    @ApiOperation(value = "대출자 본인인증 검증")
    @RequestMapping("/loan/telecom/confirm")
    public ResponseEntity<ResponseResult> confirmCustTelecom(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String mid = jsonRequest.get("mid").toString();
        final String hp = jsonRequest.get("hp").toString();
        final String name = jsonRequest.get("name").toString();
        final String newsagency = jsonRequest.get("newsagency").toString();
        
        OneLoanTelecomConfirm oneLoanTelecomConfirm = new OneLoanTelecomConfirm();
        oneLoanTelecomConfirm.setMid(mid);
        oneLoanTelecomConfirm.setHp(hp);
        oneLoanTelecomConfirm.setName(name);
        oneLoanTelecomConfirm.setNewsagency(newsagency);
        
        String custTelecomConfirm = loanService.selectCustTelecomConfirm(oneLoanTelecomConfirm);
        
        if(custTelecomConfirm == null) {
        	response.setState(429);
            response.setMessage("본인 인증정보와 가입한 정보가 일치하지 않습니다.\n고객센터에 문의하세요.");
        } 
        
        String loanConfirm = loanService.selectLoanConfirm(mid);
        
        if(loanConfirm != null && loanConfirm.equals("N")) {
        	response.setState(428);
            response.setMessage("이미 심사중인 대출건이 있습니다.");
        }
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "간편계약서 조회")
    @RequestMapping("/loan/contract/get")
    public ResponseEntity<ResponseResult> getContract(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String mid = jsonRequest.get("mid").toString();
        
        OneLoanContract oneLoanContract = loanService.selectLoanContract(mid);
        
        if(oneLoanContract != null) {
	        Map<String, Object> result = new HashMap<String, Object>();
	        result.put("loanId", oneLoanContract.getLoanId());
	    	result.put("name", oneLoanContract.getName());
	    	result.put("birth", oneLoanContract.getBirth());
	    	result.put("address", oneLoanContract.getAddress());
	    	result.put("hp", oneLoanContract.getHp());
	    	result.put("bankName", oneLoanContract.getBankName());
	    	result.put("bankAccnt", oneLoanContract.getBankAccnt());
	    	result.put("repayDay", oneLoanContract.getRepayDay());
	    	result.put("repay", oneLoanContract.getRepay());
	    	result.put("loanPay", oneLoanContract.getLoanPay());
	    	result.put("yearPlus", oneLoanContract.getYearPlus());
	    	result.put("overDue", oneLoanContract.getOverDue());
	    	response.setResult(result);
        }
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "간편계약서 작성취소")
    @RequestMapping("/loan/contract/cancel")
    public ResponseEntity<ResponseResult> setContractCancel(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String mid = jsonRequest.get("mid").toString();
        
        OneLoanContract oneLoanContract = loanService.selectLoanContract(mid);
        
        boolean updateLoanContractFlag = loanService.updateLoanContractFlag(oneLoanContract.getLoanId(), "C");
        
        if(updateLoanContractFlag) {
	        String osType = request.getHeader("osType");
	        
	        if(osType != null && !osType.equals("null")) {
	        	switch(osType) {
		        	case "ANDROID" :
		        		loanService.insertConnectChannel(oneLoanContract.getLoanId(), "A");
		        		break;
		        		
		        	case "WEB" :
		        		loanService.insertConnectChannel(oneLoanContract.getLoanId(), "W");
		        		break;
		        		
		        	case "CMS" :
		        		loanService.insertConnectChannel(oneLoanContract.getLoanId(), "C");
		        		break;
	        	}
	        } else {
	        	loanService.insertConnectChannel(oneLoanContract.getLoanId(), "A");
	        }
        }
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "상환스케줄")
    @RequestMapping("/loan/repayment/schedule")
    public ResponseEntity<ResponseResult> getRepaymentSchedule(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String loanId = jsonRequest.get("loanId").toString();
        
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("list", loanService.selectLoanRepaymentSchedule(loanId));
        response.setResult(result);
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "상환현황")
    @RequestMapping("/loan/repayment/state")
    public ResponseEntity<ResponseResult> getRepaymentState(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String mid = jsonRequest.get("mid").toString();
        
        List<OneLoanRepaymentSchedule2> oneLoanRepaymentSchedule2s = loanService.selectLoanRepaymentSchedule2(mid);
        
        JSONArray jsonResultArray = new JSONArray();
        
        for(int i = 0; i < oneLoanRepaymentSchedule2s.size(); i++) {
        	JSONObject jsonList = new JSONObject();
        	String oId = oneLoanRepaymentSchedule2s.get(i).getOid();
        	OneLoanMemoInfo oneLoanMemoInfo = loanService.selectLoanMemo(oId);
            
        	Gson gsonBuilder = new GsonBuilder().create();
        	
        	jsonList.put("o_id", oneLoanRepaymentSchedule2s.get(i).getOid());
        	jsonList.put("o_count", oneLoanRepaymentSchedule2s.get(i).getCount());
        	jsonList.put("title", oneLoanRepaymentSchedule2s.get(i).getSubject());
        	jsonList.put("o_date", oneLoanRepaymentSchedule2s.get(i).getCollectiondate());
        	jsonList.put("deposit", oneLoanRepaymentSchedule2s.get(i).getPayAmount());
        	jsonList.put("status", oneLoanRepaymentSchedule2s.get(i).getRepaymentStatus());
        	
            if(oneLoanMemoInfo != null) {
            	List<OneLoanMemoHeart> oneLoanMemoHearts = loanService.selectLoanHeart(oId);
            	jsonList.put("memoId", oneLoanMemoInfo.getId());
            	jsonList.put("memo", oneLoanMemoInfo.getMemo());
            	jsonList.put("createDt", oneLoanMemoInfo.getCreateDt());
            	
            	if(oneLoanMemoHearts != null)
            		jsonList.put("heart", new JSONArray(gsonBuilder.toJson(oneLoanMemoHearts)));
            	else
            		jsonList.put("heart", new JSONArray());
            } else {
            	jsonList.put("memoId", "");
            	jsonList.put("memo", "");
            	jsonList.put("createDt", "");
            	jsonList.put("heart", new JSONArray());
            }
            
            jsonResultArray.put(jsonList);
        }
        
        JSONObject jsonResult = new JSONObject();
        jsonResult.put("list", jsonResultArray);
        
        HashMap<String,Object> result = new ObjectMapper().readValue(jsonResult.toString(), HashMap.class);
        response.setResult(result);
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "대출등록")
    @RequestMapping("/loan/add")
    public ResponseEntity<ResponseResult> addLoan(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String loanType = jsonRequest.get("i_loan_type").toString();
        final String payment = jsonRequest.get("i_payment").toString();
        final String mid = jsonRequest.get("m_id").toString();
        final String loanPay = jsonRequest.get("i_loan_pay").toString();
        final String loanDay = jsonRequest.get("i_loan_day").toString();
        final String yearPlus = jsonRequest.get("i_year_plus").toString();
        final String repayDay = jsonRequest.get("i_repay_day").toString();
        final String repay = jsonRequest.get("i_repay").toString();
        final String loanPose = jsonRequest.get("i_loan_pose").toString();
        final String plan = jsonRequest.get("i_plan").toString();
        final String m_name = jsonRequest.get("m_name").toString();
        final String birth1 = jsonRequest.get("birth1").toString();
        String birth2 = jsonRequest.get("birth2").toString();
        String birth3 = jsonRequest.get("birth3").toString();
        final String sex = jsonRequest.get("i_sex").toString();
        final String newsagency = jsonRequest.get("i_newsagency").toString();
        final String hp1 = jsonRequest.get("hp1").toString();
        final String hp2 = jsonRequest.get("hp2").toString();
        final String hp3 = jsonRequest.get("hp3").toString();
        final String businessname = jsonRequest.get("i_businessname").toString();
        final String occu = jsonRequest.get("i_occu").toString();
        final String zip1 = jsonRequest.get("zip1").toString();
        final String addr1 = jsonRequest.get("addr1").toString();
        final String addr2 = jsonRequest.get("addr2").toString();
        final String m_gubun = jsonRequest.get("m_gubun").toString();
        final String graduated_year = jsonRequest.get("graduated_year").toString();
        String graduated_mon = jsonRequest.get("graduated_mon").toString();
        String officeworkers = jsonRequest.has("officeworkers") ? jsonRequest.getString("officeworkers") : "";
        
        if(!loanType.isEmpty() && !payment.isEmpty() && !mid.isEmpty() && !loanPay.isEmpty() && !loanDay.isEmpty() && !yearPlus.isEmpty() && 
    		!repayDay.isEmpty() && !repay.isEmpty() && !loanPose.isEmpty() && !plan.isEmpty() && !m_name.isEmpty() && !birth1.isEmpty() && 
    		!birth2.isEmpty() && !birth3.isEmpty() && !sex.isEmpty() && !newsagency.isEmpty() && !hp1.isEmpty() && !businessname.isEmpty() && 
    		!occu.isEmpty() && !zip1.isEmpty() && !addr1.isEmpty() && !addr2.isEmpty() && !m_gubun.isEmpty() && !graduated_year.isEmpty() && !graduated_mon.isEmpty()) {
        	
        	if(Integer.parseInt(birth2) < 10 && birth2.length() < 2)
        		birth2 = "0" + birth2;
        	if(Integer.parseInt(birth3) < 10 && birth3.length() < 2)
        		birth3 = "0" + birth3;
        	
        	if(Integer.parseInt(graduated_mon) < 10 && graduated_mon.length() < 2)
        		graduated_mon = "0" + graduated_mon;
        	
        	String phoneNumber = hp1 + hp2 + hp3;
        	
        	OneLoan oneLoan = new OneLoan();
        	oneLoan.setLoanType(loanType);
        	oneLoan.setPayment(payment);
        	oneLoan.setMid(mid);
        	oneLoan.setLoanPay(loanPay);
        	oneLoan.setLoanDay(loanDay);
        	oneLoan.setYearPlus(yearPlus);
        	oneLoan.setRepayDay(repayDay);
        	oneLoan.setRepay(repay);
        	oneLoan.setLoanPose(loanPose);
        	oneLoan.setPlan(plan);
        	oneLoan.setMname(m_name);
        	oneLoan.setBirth(birth1 + "-" + birth2 + "-" + birth3);
        	oneLoan.setSex(sex.toUpperCase());
        	oneLoan.setNewsagency(newsagency);
        	oneLoan.setMhp(phoneNumber);
        	oneLoan.setBusinessname(businessname);
        	oneLoan.setOccu(occu);
        	oneLoan.setHomeAddress("(" + zip1 + ") " + addr1 + " " + addr2);
        	oneLoan.setMgubun(m_gubun);
        	oneLoan.setGraduated(graduated_year + "-" + graduated_mon);
        	oneLoan.setOfficeworkers(officeworkers);
        	
        	if(loanService.insertLoan(oneLoan)) {
        		JSONObject jsonSmsData = new JSONObject();
        		jsonSmsData.put("name", m_name);
        		jsonSmsData.put("payment", commonUtil.getAmountUnit3(Long.parseLong(loanPay)));
        		
        		String msg = commonUtil.getFormSMS(7, jsonSmsData);																					
        		commonUtil.setRequestSMSData(m_name, "L", oneMemberService.selectCustID(mid), AES256Cipher.getInstance().AES_Decode(phoneNumber), msg);
        	}
        } else {
        	response.setState(372);
            response.setMessage("대출신청에 누락된 정보가 있습니다.\n다시 확인 후 진행해주세요.");
        }
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    public void sendSMS(String phoneNumber, String msg) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String mode = "sendSms";
        if(msg.length() > 80)
        	mode = "sendSms_lms";
        
        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
        map.add("cid", "crepass");
        map.add("from", "15222975");
        map.add("to", phoneNumber);
        map.add("msg", msg);
        map.add("mode", mode);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
        new RestTemplate().postForObject(smsUrl, request, String.class);
    }
}