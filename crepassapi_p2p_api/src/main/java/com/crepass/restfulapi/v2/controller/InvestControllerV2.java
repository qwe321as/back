package com.crepass.restfulapi.v2.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerMapping;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.cre.service.CreMemberService;
import com.crepass.restfulapi.one.domain.OneEmoneyInvestPay;
import com.crepass.restfulapi.one.domain.OneInvest;
import com.crepass.restfulapi.one.domain.OneInvestAccount;
import com.crepass.restfulapi.one.domain.OneInvestAutoDivisionSet;
import com.crepass.restfulapi.one.domain.OneInvestDetail;
import com.crepass.restfulapi.one.domain.OneInvestInfo;
import com.crepass.restfulapi.one.domain.OneInvestLimitPay;
import com.crepass.restfulapi.one.domain.OneInvestLoanDefault;
import com.crepass.restfulapi.one.domain.OneInvestTitle;
import com.crepass.restfulapi.one.service.EmoneyService;
import com.crepass.restfulapi.one.service.InvestService;
import com.crepass.restfulapi.one.service.LoanService;
import com.crepass.restfulapi.one.service.OneMemberService;
import com.crepass.restfulapi.one.service.StatisticsService;
import com.crepass.restfulapi.one.service.VirtualAccntService;
import com.crepass.restfulapi.v2.domain.InvestBundleInfo;
import com.crepass.restfulapi.v2.domain.InvestBundleItem;
import com.crepass.restfulapi.v2.domain.InvestBundleList;
import com.crepass.restfulapi.v2.domain.InvestDetailItem;
import com.crepass.restfulapi.v2.domain.InvestDetailItem2;
import com.crepass.restfulapi.v2.domain.PaymentHistoryInfo;
import com.crepass.restfulapi.v2.domain.PaymentHistoryItem;
import com.crepass.restfulapi.v2.domain.InvestReplyList;
import com.crepass.restfulapi.v2.domain.InvestReplyList2;
import com.crepass.restfulapi.v2.domain.InvestScheduleInfo;
import com.crepass.restfulapi.v2.domain.InvestScheduleItem;
import com.crepass.restfulapi.v2.domain.LoansVO;
import com.crepass.restfulapi.v2.domain.LoansVO2;
import com.crepass.restfulapi.v2.domain.MemberInvestInfo;
import com.google.gson.Gson;

import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@RequestMapping(path = "/api2", method = {RequestMethod.POST, RequestMethod.GET})
public class InvestControllerV2 {
	
	@Autowired
	private InvestService investService;

	@Autowired
    private OneMemberService oneMemberService;
	
	@Autowired
    private VirtualAccntService virtualAccntService;
	
	@Autowired
    private EmoneyService emoneyService;
	
	@Autowired
    private CreMemberService creMemberService;
	
	@Autowired
    private StatisticsService statService;

	@Autowired
    private LoanService loanService;
	
	@Autowired
    private CommonUtil commonUtil;
	
	@Autowired(required=true)
	private HttpServletRequest request;
	
	@Value("${crepas.inside.url}")
    private String insideUrl;
	
	@Value("${paging.offset}")
	private int rowCount;
	
	@ApiOperation(value = "채권목록조회")
    @RequestMapping("/invest/loanList")
	public ResponseEntity<ResponseResult> getInvestLoanList(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
		JSONObject json = new JSONObject(requestString);
		
		JSONObject jsonRequest = json.getJSONObject("request");
		
		int pageNum = jsonRequest.getInt("pageNum");
		
		String mid = "";
		String keyword = "";
		
		if(jsonRequest.has("mid"))
			mid = jsonRequest.getString("mid");
		
		if(jsonRequest.has("keyword"))
			keyword = jsonRequest.getString("keyword");
		
		int offSetNum = (pageNum - 1) * rowCount;
		
		List<LoansVO> loanList = investService.selectLoanList(offSetNum, rowCount, mid, keyword);
		
		if(loanList == null)
			loanList = new ArrayList<LoansVO>();
		
		int totCount = investService.selectLoanListCount(keyword);
		int totPageCount = (int) Math.ceil((float)totCount/rowCount);
		
		ResponseResult response = new ResponseResult();
		Map<String,Object> result = new HashMap<String,Object>();
		result.put("list", loanList);
		result.put("totPageCount", totPageCount);
		
		response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        response.setResult(result);
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
	}
	
	@ApiOperation(value = "채권목록조회 apk 2.1.7, code 82")
    @RequestMapping("/invest/loanList2")
	public ResponseEntity<ResponseResult> getInvestLoanList2(@RequestBody String requestString, @RequestHeader String apkVersion) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
		JSONObject json = new JSONObject(requestString);
		
		JSONObject jsonRequest = json.getJSONObject("request");
		
		int pageNum = jsonRequest.getInt("pageNum");
		
		String mid = "";
		String keyword = "";
		
		if(jsonRequest.has("mid"))
			mid = jsonRequest.getString("mid");
		
		if(jsonRequest.has("keyword"))
			keyword = jsonRequest.getString("keyword");
		
		int offSetNum = (pageNum - 1) * rowCount;
		
		if(apkVersion.contains("2.1."))
			System.out.println(apkVersion);

//		
		List<LoansVO2> loanList;
		String fundingStatus;
		JSONObject sortCondition;
		String sortOrder;
		String sortType;
		
		String creditGradeMin;
		String creditGradeMax;
		String crepassGradeMin;
		String crepassGradeMax;
		String loanDayMin;
		String loanDayMax;
		String loanPayMin;
		String loanPayMax;
		String loanRate;
		String socialCorpAll = "Y";
		JSONArray socialCorp;
		JSONArray loanPurpose;
		List<String> socialCodeList = new ArrayList<String>();
		List<String> poseCodeList = new ArrayList<String>();

		if(jsonRequest.has("fundingStatus")) {
			fundingStatus = jsonRequest.getString("fundingStatus");
			sortCondition = jsonRequest.getJSONObject("sortCondition");
			sortOrder = sortCondition.getString("sortOrder");
			sortType = sortCondition.getString("sortType");
			
			creditGradeMin = sortCondition.getString("creditGradeMin");
			creditGradeMax = sortCondition.getString("creditGradeMax");
			crepassGradeMin = sortCondition.getString("crepassGradeMin");
			crepassGradeMax = sortCondition.getString("crepassGradeMax");
			loanDayMin = sortCondition.getString("loanDayMin");
			loanDayMax = sortCondition.getString("loanDayMax");
			loanPayMin = sortCondition.getString("loanPayMin");
			loanPayMax = sortCondition.getString("loanPayMax");
			loanRate = sortCondition.getString("loanRate");
			
			if (sortCondition.has("socialCorpAll"))						// v2.1.13에 socialCorpAll없어서 nullable 처리
				socialCorpAll = sortCondition.getString("socialCorpAll");
			else {
				loanPayMin="0";
				loanPayMax="10000000";
			}
			if(!sortType.equals("P"))	creditGradeMin = "0";			// 전체,법인인경우 신용등급을 1에서 0으로 변경(기본등급이 1로 되어져있음)
			
			socialCorp = sortCondition.getJSONArray("socialCorp");
			loanPurpose = sortCondition.getJSONArray("loanPurpose");
			
			for(int i=0; i<socialCorp.length(); i++) {
				socialCodeList.add(socialCorp.getJSONObject(i).getString("socialCode"));
			}
			
			for(int i=0; i<loanPurpose.length(); i++) {
				poseCodeList.add(loanPurpose.getJSONObject(i).getString("poseCode"));
			}
		}
		else {
			fundingStatus = "A"; 			// 전체리스트(실행중,실행완료)
			sortOrder = "N";				// 최신순이 기본정렬방식
			sortType = "A";				// 실제 사용하지 않음;
			
			creditGradeMin="0";				
			creditGradeMax="10";
			crepassGradeMin="A";
			crepassGradeMax="E";
			loanDayMin="6";
			loanDayMax="24";
			loanPayMin="0";
			loanPayMax="5000000";
			socialCorpAll="Y";
			loanRate="A";
		}
		loanList = investService.selectLoanList2(offSetNum, rowCount, mid, keyword, fundingStatus, sortOrder, sortType, socialCorpAll, socialCodeList, poseCodeList 
				, creditGradeMin, creditGradeMax, crepassGradeMin, crepassGradeMax, loanDayMin, loanDayMax, loanPayMin, loanPayMax, loanRate);
		
		
		if(loanList == null)
			loanList = new ArrayList<LoansVO2>();
		
//		int totCount = investService.selectLoanListCount(keyword);
		int totCount = investService.selectLoanListCount2(offSetNum, rowCount, mid, keyword, fundingStatus, sortOrder, sortType, socialCorpAll, socialCodeList, poseCodeList 
				, creditGradeMin, creditGradeMax, crepassGradeMin, crepassGradeMax, loanDayMin, loanDayMax, loanPayMin, loanPayMax, loanRate);
		
		int totPageCount = (int) Math.ceil((float)totCount/rowCount);
		
		ResponseResult response = new ResponseResult();
		Map<String,Object> result = new HashMap<String,Object>();
		result.put("list", loanList);
		result.put("totLoanCount", totCount);
		result.put("totPageCount", totPageCount);
		 
		response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        response.setResult(result);
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
	}
	
	
	@ApiOperation(value = "채권 아이템 조회")
    @RequestMapping("/invest/loanitem")
	public ResponseEntity<ResponseResult> getInvestLoanItem(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
        JSONObject json = new JSONObject(requestString);

        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");

        JSONObject jsonRequest = (JSONObject) json.get("request");
      
        String mid = jsonRequest.get("mid").toString();
        int loanId = Integer.parseInt(jsonRequest.get("loanId").toString());
        
        LoansVO loanItem = investService.selectLoanItem(mid, loanId);

        response.setResult(loanItem);
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
	
	@ApiOperation(value = "투자자 리스트 조회") 
	@RequestMapping(value="/invest/investorList",method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public ResponseEntity<ResponseResult> getInvestorList(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
		JSONObject json = new JSONObject(requestString);
		JSONObject jsonRequest = json.getJSONObject("request");
		
		ResponseResult response = new ResponseResult();
		
		String mid = jsonRequest.getString("mid");
		int pageNum = jsonRequest.getInt("pageNum");
		int investLv = jsonRequest.getInt("investLv");
		String keyword = "";
		
		if(jsonRequest.has("keyword"))
			keyword = jsonRequest.getString("keyword");
		
		int offSetNum = (pageNum - 1) * rowCount;
		
		int investorListCount = investService.selectInvestorListCount(investLv, mid, keyword);
		
		int totCount = investorListCount;
		int totPageCount = (int) Math.ceil((float)totCount/rowCount);
		List<Map<String,Object>> investorList = investService.selectInvestorList(offSetNum, rowCount, investLv, mid, keyword);
		
		if(investorList == null)
			investorList = new ArrayList<>();
		
		Map<String,Object> result = new HashMap<>();
		result.put("list", investorList);
		result.put("totPageCount", totPageCount);
		
		response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        response.setResult(result);
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
	}
	
	@ApiOperation(value = "투자 상품리스트 조회") 
	@RequestMapping(value="/invest/investor/loanList",method=RequestMethod.POST)
	public ResponseEntity<ResponseResult> getInvestorItem(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
		JSONObject json = new JSONObject(requestString);
		JSONObject jsonRequest = (JSONObject) json.getJSONObject("request");
		ResponseResult response = new ResponseResult();
		
		int pageNum = jsonRequest.getInt("pageNum");
		String mid = jsonRequest.getString("mid");
		
		int investItemListCount = investService.selectInvestItemListCount(mid);
		
		int offSetNum = (pageNum - 1) * rowCount;
		int totCount = investItemListCount;
		int totPageCount = (int) Math.ceil((float)totCount/rowCount);
		
		List<InvestReplyList> investItemList = investService.selectInvestItemList(offSetNum, rowCount, mid);
		
		if(investItemList==null) 
			investItemList = new ArrayList<InvestReplyList>();
		
		Map<String,Object> result = new HashMap<>();
		result.put("list", investItemList);
		result.put("totPageCount", totPageCount);
		
		response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        response.setResult(result);
		
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
		return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
	}
	
	@ApiOperation(value = "투자 상품리스트 조회  apk 2.1.7, code 82") 
	@RequestMapping(value="/invest/investor/loanList2",method=RequestMethod.POST)
	public ResponseEntity<ResponseResult> getInvestorItem2(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
		JSONObject json = new JSONObject(requestString);
		JSONObject jsonRequest = (JSONObject) json.getJSONObject("request");
		ResponseResult response = new ResponseResult();
		
		int pageNum = jsonRequest.getInt("pageNum");
		String mid = jsonRequest.getString("mid");
		
		int investItemListCount = investService.selectInvestItemListCount(mid);
		
		int offSetNum = (pageNum - 1) * rowCount;
		int totCount = investItemListCount;
		int totPageCount = (int) Math.ceil((float)totCount/rowCount);
		
		List<InvestReplyList2> investItemList = investService.selectInvestItemList2(offSetNum, rowCount, mid);
		
		if(investItemList==null) 
			investItemList = new ArrayList<InvestReplyList2>();
		
		Map<String,Object> result = new HashMap<>();
		result.put("list", investItemList);
		result.put("totPageCount", totPageCount);
		
		response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        response.setResult(result);
		
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
		return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
	}
	
	@ApiOperation(value = "투자자가 투자한 상품리스트")
	@RequestMapping(value="/invest/investList", method=RequestMethod.POST)
	public ResponseEntity<ResponseResult> getInvestList(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
		JSONObject json = new JSONObject(requestString);
		JSONObject jsonRequest = (JSONObject) json.get("request");
		
		ResponseResult response = new ResponseResult();
		
		int pageNum = jsonRequest.getInt("pageNum");
		String mid = jsonRequest.getString("mid");
		String investCode = jsonRequest.getString("investCode"); // investCode   A = 전체 / S = 펀딩중 / R = 정산중 / P = 정산완료
		String keyword = jsonRequest.getString("keyword");
		
		investService.updateInvestList(mid);
		
		int investListCount = investService.selectInvestListCount(mid, investCode, keyword);
		
		int offSetNum = (pageNum - 1) * rowCount;
		int totCount = investListCount;
		int totPageCount = (int) Math.ceil((float)totCount/rowCount);
		
		List<Map<String, Object>> investList = investService.selectInvestList(offSetNum, rowCount, mid, investCode, keyword);
		if(investList == null) {
			investList = new ArrayList<>();
		}
		
		Map<String, Object> result = new HashMap<>();
		result.put("list", investList);
		result.put("totPageCount", totPageCount);
		
		response.setState(200);
        response.setMessage("정상적으로 처리하였습니다");
        response.setResult(result);
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
	}
	
	@ApiOperation(value = "상품상세페이지(채권상세)")
    @RequestMapping("/loan/detail")
    public ResponseEntity<ResponseResult> getInvestLoanDetail(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
        JSONObject json = new JSONObject(requestString);

        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");

        if(json.has("request") && json.getJSONObject("request").has("loanId") && json.getJSONObject("request").has("mid")) {
        	final String loanId = json.getJSONObject("request").getString("loanId");
        	final String mid = json.getJSONObject("request").getString("mid");
        	final String onedebt = investService.selectDebtById2(loanId);
        	
        	InvestDetailItem investDetailItem = investService.selectInvestItemDetail(loanId);
        	Map<String, Long> deptList = commonUtil.getDeptList(onedebt);
        	investDetailItem.setLoanDebt(deptList);
        	
        	//html테그 삭제
        	String loanPose = commonUtil.br2nl(investDetailItem.getLoanPose());
        	loanPose = loanPose.replaceAll(System.getProperty("line.separator"), "");
        	investDetailItem.setLoanPose(loanPose);
        	
        	MemberInvestInfo memberInvestInfo = statService.selectMemberInvestInfo(mid);
            
            boolean isChecked = false;
            if(memberInvestInfo != null) {
            	if(memberInvestInfo.getBankAccntNum() != null && !memberInvestInfo.getBankAccntNum().isEmpty()
    			&& memberInvestInfo.getInvestVirAccntNum() != null && !memberInvestInfo.getInvestVirAccntNum().isEmpty()
    			&& memberInvestInfo.getReginum() != null && !memberInvestInfo.getReginum().isEmpty()) {
            		isChecked = true;
            	}
            }
        	
            investDetailItem.setReady(isChecked);
        	response.setResult(investDetailItem);
        } else {
        	response.setState(332);
        	response.setMessage("Parameter Not Found");
        }

        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
	
	@ApiOperation(value = "상품상세페이지(채권상세) apk 2.1.7, code 82")
    @RequestMapping("/loan/detail2")
    public ResponseEntity<ResponseResult> getInvestLoanDetail2(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
        JSONObject json = new JSONObject(requestString);

        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");

        if(json.has("request") && json.getJSONObject("request").has("loanId") && json.getJSONObject("request").has("mid")) {
        	final String loanId = json.getJSONObject("request").getString("loanId");
        	final String mid = json.getJSONObject("request").getString("mid");
        	final String onedebt = investService.selectDebtById2(loanId);
        	
        	InvestDetailItem2 investDetailItem = investService.selectInvestItemDetail2(loanId);
        	Map<String, Long> deptList = commonUtil.getDeptList(onedebt);
        	investDetailItem.setLoanDebt(deptList);
        	
        	//html테그 삭제
        	String loanPose = commonUtil.br2nl(investDetailItem.getLoanPose());
        	loanPose = loanPose.replaceAll(System.getProperty("line.separator"), "");
        	investDetailItem.setLoanPose(loanPose);
        	
        	MemberInvestInfo memberInvestInfo = statService.selectMemberInvestInfo(mid);
            
            boolean isChecked = false;
            if(memberInvestInfo != null) {
            	if(memberInvestInfo.getBankAccntNum() != null && !memberInvestInfo.getBankAccntNum().isEmpty()
    			&& memberInvestInfo.getInvestVirAccntNum() != null && !memberInvestInfo.getInvestVirAccntNum().isEmpty()
    			&& memberInvestInfo.getReginum() != null && !memberInvestInfo.getReginum().isEmpty()) {
            		isChecked = true;
            	}
            }
        	
            investDetailItem.setReady(isChecked);
        	response.setResult(investDetailItem);
        } else {
        	response.setState(332);
        	response.setMessage("Parameter Not Found");
        }

        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
	
	@ApiOperation(value = "상품상세페이지(투자하기)")
    @RequestMapping("/invest")
    public ResponseEntity<ResponseResult> getInvestInfo(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
        JSONObject json = new JSONObject(requestString);

        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");

        final String mid = ((JSONObject) json.get("request")).get("mid").toString();
        final String loanId = ((JSONObject) json.get("request")).get("loanId").toString();

        Map<String, Object> result = new HashMap<String, Object>();
        
        OneInvestInfo oneInvestInfo = new OneInvestInfo();
        result.put("invest", oneInvestInfo);
        
        oneInvestInfo = investService.selectInvestById(loanId, mid);
        if (oneInvestInfo != null) {
        	String limitPay = "0";
        	OneInvestLimitPay oneInvestLimitPay = investService.selectInvestLimitPay(mid);	// 투자자에 따른 상한금액 선택(최대 투자한도 - 투자자 투자총액)
	        
        	long sumIpay = Long.parseLong(oneInvestLimitPay.getSumIpay());
        	
        	if(!oneInvestLimitPay.getSignpurposeL().equals("false"))
        		limitPay = oneInvestLimitPay.getSignpurposeL();
        	if(!oneInvestLimitPay.getSignpurposeI().equals("false"))
        		limitPay = oneInvestLimitPay.getSignpurposeI();
        	if(!oneInvestLimitPay.getSignpurposeP().equals("false"))
        		limitPay = oneInvestLimitPay.getSignpurposeP();
        	if(!oneInvestLimitPay.getSignpurpose3().equals("false"))
        		limitPay = oneInvestLimitPay.getSignpurpose3();
        	
        	String investPossiblePay = investService.selectInvestPossiblePay(loanId);		// 투자가능 금액 확인
        	long InvestPayPosit = (long)(Long.parseLong(oneInvestInfo.getLoanPay()) * (oneInvestInfo.getInvestMax() * 0.01));
        	
        	long InvestPay = Long.parseLong(limitPay);

			if (InvestPay < 0)
				InvestPay = 0;
    		oneInvestInfo.setInvestMax(InvestPay);					// 3. 투자자 최대한도가 1,2번보다 작으면 InvestPay
			oneInvestInfo.setInvestMaxType("LP");									// (현 채권에 투자가능 한도=investMax)
			oneInvestInfo.setInvestMaxMsg("");										// 결국 세값중에 가장 작은값이 InvestMax로 넘겨줌
				
			if(Long.parseLong(limitPay) <= 0) {
	 			oneInvestInfo.setInvestMaxType("EP");							
    			oneInvestInfo.setInvestMaxMsg("투자 한도액을 초과하실수 없습니다.");		
			}
		
			if(investPossiblePay != null) {
	        	if (InvestPay > (long)(Long.parseLong(investPossiblePay))) {
	        		InvestPay = (long)(Long.parseLong(investPossiblePay));			// 1. 투자자가 투자가능 금액이 있으면(다른 투자자가 해당채권에 투자해서 채권이 다 차지 않았으면)  InvestPay
					if (InvestPay < 0)
	    				InvestPay = 0;
	        		oneInvestInfo.setInvestMax(InvestPay);
	        		oneInvestInfo.setInvestMaxType("PP");
	        		oneInvestInfo.setInvestMaxMsg("");
	        	}
			}	
			
    		if(InvestPay > InvestPayPosit) {										// 2. 채권에 투자자 최대 투자금액이 1번보다 작으면 InvestPay  (예를 들어 100만원에 50%면 50만원)
    			InvestPay = InvestPayPosit;
				if (InvestPay < 0)
    				InvestPay = 0;
    			oneInvestInfo.setInvestMax(InvestPay);
    			oneInvestInfo.setInvestMaxType("RP");
    			oneInvestInfo.setInvestMaxMsg("");
    		}
        		
            result.put("invest", oneInvestInfo);
        }
        
        OneInvestAccount oneInvestAccount = investService.selectAccountById2(mid);			// 투자자 계좌정보 선택
        if (oneInvestAccount != null) {
            oneInvestAccount.setMyBankName(virtualAccntService.selectBankById(oneInvestAccount.getMyBankcode()));
            
            OneEmoneyInvestPay oneEmoneyInvestPay = emoneyService.selectEmoneyPayInfo(mid);
            String withdrawPay = emoneyService.selectEmoneyWithdrawPay2(mid);
            String emoneyBalance = emoneyService.selectEmoneyInvestBalance(mid);
            
            if(oneEmoneyInvestPay != null) {
            	long setEmoney = Long.parseLong(emoneyBalance) - Long.parseLong(oneEmoneyInvestPay.getIpay()) - Long.parseLong(withdrawPay);
            	emoneyService.updateEmoney(String.valueOf(setEmoney), mid);
            	oneInvestAccount.setMEmoney(setEmoney);
            }
        } else
        	oneInvestAccount = new OneInvestAccount();

        result.put("account", oneInvestAccount);
        response.setResult(result);
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));

        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
	
	
	@ApiOperation(value = "투자하기 (대출투자자등록)")
    @RequestMapping("/invest/add")
    @Transactional("oneTransactionManager")
    public ResponseEntity<ResponseResult> addInvest(@RequestBody String requestString) throws Exception {
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);
        JSONObject request = (JSONObject) json.get("request");
        
        final String mid = request.getString("mid");
        String i_pay = request.getString("i_pay");
        final String loan_id = request.getString("loan_id");
        final String m_password = request.getString("m_password");
        
        i_pay = String.valueOf(((Long.parseLong(i_pay) / 10000) * 10000));							// 투자금액(만원단위)
        
        ResponseResult response = new ResponseResult();
        
        String limitPay2 = "0";
        OneInvestLimitPay oneInvestLimitPay2 = investService.selectInvestLimitPay2(mid, loan_id);	// 해당 채권에 투자 가능한 투자최대금액 예) L(일반투자자) 5,000,000원
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
        
    	// 200827~210430 1.법인 40% 이상 투자할수 없음
    	OneInvestLoanDefault oneInvestLoanDefault = investService.selectInvestLoan(loan_id);	// 기본 대출정보
    	String mLevel = investService.selectInvestLevel(mid);									// 투자자 레벨선택
    	
    	if (mLevel.equals("4")) {								// 법인 투자율이
	    	long loanPay = Long.parseLong(oneInvestLoanDefault.getLoanPay());
	    	if ((loanPay * 0.4) < Long.parseLong(i_pay)) {		// 40%을 넘기면 투자불가
	    		Map<String, Object> result = new HashMap<String, Object>();
	    		result.put("i_pay", i_pay);
	    		result.put("loanPay", loanPay);
				
				response.setState(377);
				response.setMessage("법인투자자는 한채권에 40% 이상 투자할수 없습니다.");
				response.setResult(result);
				
				return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
	    		
	    	}
    	}
    	
    	// 2-1. 200827~210430  일반투자자가 1천만원이상 투자 불가, 동일차입자 500만원 이상 투자할수 없음
    	long investTotalAmount = Long.parseLong(investService.selectInvestTotalAmount(mid));	// mari_invest-투자자 투자총금액
    	long returnedPayAmount = Long.parseLong(investService.selectInvestReturnedPayAmount(mid)); // 회수한 금액
    	long investLimitation = Long.parseLong(investService.selectInvestLimitation(mid));		// mari_inset-레벨별 투자 한도	
    	if (mLevel.equals("1")) {	
    		if (investTotalAmount+Long.parseLong(i_pay) > investLimitation) {					// 투자자 투자총금액+신규투자액이 한도를 초과하면 에러
    			Map<String, Object> result = new HashMap<String, Object>();
	    		result.put("i_pay", i_pay);
	    		result.put("investTotalAmount", investTotalAmount);
				
				response.setState(378);
				response.setMessage("투자 한도액을 초과하였습니다.");
				response.setResult(result);
				
				return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    		}
    		
    		// 그 대출자에 투자한 금액이 있는지 확인!
    		long usedToInvest = Long.parseLong(investService.selectUsedToInvest(oneInvestLoanDefault.getMid(), mid));	// 대출자, 투자자 mid
    		if ( usedToInvest+Long.parseLong(i_pay) > 5000000) {		// 과거 투자액과 신규 투자액이 500만원을 넘으면
    			Map<String, Object> result = new HashMap<String, Object>();
	    		result.put("i_pay", i_pay);
	    		result.put("usedToInvest", usedToInvest);
				
				response.setState(379);
				response.setMessage("동일 차입자에 투자할수 있는 투자금액을 초과하였습니다.");
				response.setResult(result);
				
				return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
	    		
    		}
    	}
    	
    	
    	
    	if(Long.parseLong(i_pay) > Long.parseLong(limitPay2)) {										// 투자하려는 액수가 대출건당 투자가능한 한도액 보다 크면
    		Map<String, Object> result = new HashMap<String, Object>();
    		result.put("i_pay", limitPay2);
			
			response.setState(373);
			response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
			response.setResult(result);
			
			return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    	}
    	
        String UserConfirm = oneMemberService.selectUserConfirm(mid, m_password);
        
        if(UserConfirm != null) {
	        OneInvestAccount oneInvestAccount = investService.selectAccountById(mid);				// mm-예치금잔액, 실계좌정보 선택
	        
	        //state 통신전 처리
	        String duplicate = investService.selectInvestDuplicate(mid, loan_id);					// 투자중인지 확인(아니면 null)
	        
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
	        	response.setState(375);
				response.setMessage(jsonAMT.getString("MESSAGE"));
				return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
	        }
	        
            String investPayment = emoneyService.selectEmoneyInvestIsPlaying(mid);				// mm,mi,mip-현재 투자한 금액중 회수되지 않은 투자액의 합
            String withdrawPay = emoneyService.selectEmoneyWithdrawPay2(mid);					// cwt-투자자의 출금처리되지 않은(trx_flag=N) 금액의 합
	       
	        if(Long.parseLong(i_pay) <= (Long.parseLong(balanceAMT) - Long.parseLong(investPayment) - Long.parseLong(withdrawPay)) ) {		// 1. 예치금이 여유가 있으면
	        	OneEmoneyInvestPay investPay = emoneyService.selectInvestProgressPay(mid);		// 현재투자중인금액(출금예정금액)
	        	
	            vars = new HashMap<String, String>();
	            vars.put("BANK_CD", oneInvestAccount.getMyBankcode());
	            vars.put("ACCT_NB", oneInvestAccount.getMyBankacc());
	            
	            String resultOne = restTemplate.postForObject(insideUrl + "/lookup/reciever", vars, String.class);		//?
	            
	            JSONObject jsonResult = new JSONObject(resultOne);
     
	            if(jsonResult.getInt("STATE") == 200) {	
	            	String investIsPlaying = investService.selectInvestIsPlaying(loan_id);		// 해당 대출자에게 투자한 총 합
	            	
	            	if(investIsPlaying != null) {												// 1명 이상 투자했으면
			        	String limitPay = "0";
			        	OneInvestLimitPay oneInvestLimitPay = investService.selectInvestLimitPay(mid);	// 투자자가 투자할수 있는 최대 투자금액(mari_inset)
				        
			        	long sumIpay = Long.parseLong(oneInvestLimitPay.getSumIpay());
			        	
			        	if(!oneInvestLimitPay.getSignpurposeL().equals("false"))
			        		limitPay = oneInvestLimitPay.getSignpurposeL();
			        	if(!oneInvestLimitPay.getSignpurposeI().equals("false"))
			        		limitPay = oneInvestLimitPay.getSignpurposeI();
			        	if(!oneInvestLimitPay.getSignpurposeP().equals("false"))
			        		limitPay = oneInvestLimitPay.getSignpurposeP();
			        	if(!oneInvestLimitPay.getSignpurpose3().equals("false"))
			        		limitPay = oneInvestLimitPay.getSignpurpose3();
			        	
				        if(0 < Long.parseLong(limitPay) || sumIpay == 0L) {						// 투자가능액이 0원 이상이거나, 지금까지 투자금액이 0이면(?)  
				        	String minPay = investService.selectInvestMinPay(loan_id, i_pay);	// mip-최소투자금액
				        	
					        if(minPay != null) {
					        	String possiblePay = investService.selectInvestPossiblePay(loan_id);				// mi,ml-투자가능금액:대출신청금-현재투자액의합
					        	
					        	if(0 < Long.parseLong(possiblePay)) {
					        		if(Long.parseLong(i_pay) <= Long.parseLong(possiblePay)) {						// 투자가능금액이 내 투자하려는 금액보다 크면 
					        			if(Long.parseLong(i_pay) <= Long.parseLong(limitPay) || sumIpay == 0L) {	//	투자자 투자 총 한도액이 넘지 않았으면
					        				if(Long.parseLong(i_pay) > Long.parseLong(limitPay2)) {					// 투자한도보다 투자하려는 금액보다 크면  
					        		    		Map<String, Object> result = new HashMap<String, Object>();
					        		    		result.put("i_pay", limitPay2);
					        					
					        					response.setState(373);
					        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
					        					response.setResult(result);
					        					
					        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
					        		    	}
					        				
					        				OneInvestTitle oneInvestTitle = investService.selectInvestTitle(mid, loan_id);	
					        				
					        				int ranNum = (int)(Math.random() * (999 - 111 + 1)) + 111;
					        				long time = System.currentTimeMillis();
					        				String gCode = "P" + String.valueOf(time) + String.valueOf(ranNum);
					        				
					        				double eMoney = Double.parseDouble(balanceAMT);
					        				double eMoneyCal = (eMoney - Double.parseDouble(i_pay)) - Double.parseDouble(investPay.getIpay());		// 예치금 - 투자하려는금액 - 현재펀딩중인금액
			        	            		
					        				if(eMoneyCal >= 0) { 

					        					int isInvestAdd = 0;
//								      
					        					isInvestAdd = setInvestAdd(gCode, mid, loan_id, i_pay, oneInvestTitle);
						        				
						        				int invsetDetail = 0;
						        				if(duplicate == null) {																				// 예치금값 업데이트(예치금 - 투자하려는금액 - 현재펀딩중인금액)
//						        		
						        					invsetDetail = investService.insertInvestDetail(setInvestDetail(mid, loan_id, i_pay, String.valueOf(eMoneyCal), oneInvestTitle));
//						        		
						        					investService.insertInvestHistory(loan_id, custId, i_pay, i_pay, gCode, oneInvestTitle.getSubject(), "I");
						        				} else {
//						        	
						        					invsetDetail = investService.updateInvestPay(mid, loan_id, String.valueOf(Long.parseLong(i_pay) + Long.parseLong(duplicate)));
//						        	
						        					investService.insertInvestHistory(loan_id, custId, i_pay, String.valueOf(Long.parseLong(i_pay) + Long.parseLong(duplicate)), gCode, oneInvestTitle.getSubject(), "U");
						        				}
				        	            		
				        	            		if (invsetDetail > 0 && isInvestAdd > 0) {
//				        	      
				        	            			creMemberService.addCreAgreeMember2(json, custId);
				        	            			
				        	            			JSONObject jsonSmsData = new JSONObject();
				        	            			jsonSmsData.put("title", oneInvestTitle.getSubject());
				        	                		jsonSmsData.put("name", oneInvestTitle.getName());
				        	                		jsonSmsData.put("payment", commonUtil.getAmountUnit3(Long.parseLong(i_pay)));
				        	                		
//				        
				        	                		String msg = commonUtil.getFormSMS(6, jsonSmsData);
//				      
				        	                		commonUtil.setRequestSMSData(oneInvestTitle.getName(), "I", oneInvestTitle.getCustId(), oneInvestTitle.getHp(), msg);
				        	            			
				        	            			response.setState(200);
					        	            		response.setMessage("정상적으로 처리하였습니다.");
				        	            		} else {
				        	            			response.setState(374);
						        	                response.setMessage("투자자 등록에 예치기 못한 에러가 발생하였습니다. 고객센터에 문의하세요.");
				        	            		}
					        				} else {
					        					long possible = (((long)eMoney / 10000) * 10000) - Long.parseLong(investPay.getIpay());
			        	            			
					        					if(possible < 0) {
				        	            			Map<String, Object> result = new HashMap<String, Object>();
						        		    		result.put("i_pay", possible);
						        					
						        					response.setState(373);
						        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
						        					response.setResult(result);
						        					
						        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK); 
					        					} else {
					        						response.setState(376);
						        					response.setMessage("예치금액이 적어 투자를 하실 수 없습니다.");
						        					
						        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK); 
					        					}
					        				}
					        				
					        			} else {
					        				if(Long.parseLong(limitPay) > Long.parseLong(limitPay2)) {
					        		    		Map<String, Object> result = new HashMap<String, Object>();
					        		    		result.put("i_pay", limitPay2);
					        					
					        					response.setState(373);
					        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
					        					response.setResult(result);
					        					
					        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
					        		    	}
					        				
					        				Map<String, Object> result = new HashMap<String, Object>();
						            		result.put("i_pay", limitPay);
						        			
						        			response.setState(373);
						        			response.setMessage("최대 투자가능한 금액한도를 넘었습니다.");
						        			response.setResult(result);
					        			}
					        			
					        		} else {
					        			if(Long.parseLong(i_pay) - (Long.parseLong(i_pay) - Long.parseLong(possiblePay)) > Long.parseLong(limitPay2)) {
				        		    		Map<String, Object> result = new HashMap<String, Object>();
				        		    		result.put("i_pay", limitPay2);
				        					
				        					response.setState(373);
				        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
				        					response.setResult(result);
				        					
				        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
				        		    	}
					        			
					        			Map<String, Object> result = new HashMap<String, Object>();
					            		result.put("i_pay", String.valueOf(Long.parseLong(i_pay) - (Long.parseLong(i_pay) - Long.parseLong(possiblePay))));
					        			
					        			response.setState(372);
					        			response.setMessage("투자가능한 금액한도를 넘었습니다.");
					        			response.setResult(result);
					        		}
					        		
					        	} else {
					    			response.setState(371);
					    			response.setMessage("투자모집이 만료되어 해당 투자상품에 투자가 불가합니다.");
					        	}
					        } else {
								response.setState(370);
								response.setMessage("최소투자금액보다 적게 투자하실 수 없습니다.");
					        }
				        } else {
				        	response.setState(369);
							response.setMessage("투자가능한 채권한도를 초과하실수 없습니다.");
				        }
	            	} else {	// 투자를 한명도 안한 상태이면
	            		String minPay = investService.selectInvestMinPay(loan_id, i_pay);			// 최소 투자금액설정
			        	
				        if(minPay != null) {
				        	String possiblePay = investService.selectInvestPossiblePay(loan_id);	// 투자가능금액(대출액-현재까지 투자한 합)
				        	
				        	if(possiblePay != null) {
					        	if(0 < Long.parseLong(possiblePay)) {								// 투자가 가능하면
					        		if(Long.parseLong(i_pay) <= Long.parseLong(possiblePay)) {		// 투자하려는 금액이 투자 가능액보다 크지 않으면
					        			if(Long.parseLong(i_pay) > Long.parseLong(limitPay2)) {		// 투자하려는 금액이 투자 한도액보다 작으면
				        		    		Map<String, Object> result = new HashMap<String, Object>();
				        		    		result.put("i_pay", limitPay2);
				        					
				        					response.setState(373);
				        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
				        					response.setResult(result);
				        					
				        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
				        		    	}
					        			
				        				OneInvestTitle oneInvestTitle = investService.selectInvestTitle(mid, loan_id);	//
				        				
				        				int ranNum = (int)(Math.random() * (999 - 111 + 1)) + 111;
				        				long time = System.currentTimeMillis();
				        				String gCode = "P" + String.valueOf(time) + String.valueOf(ranNum);
				        				
				        				double eMoney = Double.parseDouble(balanceAMT);
				        				double eMoneyCal = (eMoney - Double.parseDouble(i_pay)) - Double.parseDouble(investPay.getIpay());
		        	            		
		        	            		if(eMoneyCal >= 0) { 
					        				int isInvestAdd = 0;
//		        	        		
					        				isInvestAdd = setInvestAdd(gCode, mid, loan_id, i_pay, oneInvestTitle);
					        				
					        				int invsetDetail = 0;
					        				if(duplicate == null) {
//					        		
					        					invsetDetail = investService.insertInvestDetail(setInvestDetail(mid, loan_id, i_pay, String.valueOf(eMoneyCal), oneInvestTitle));
//					        		
					        					investService.insertInvestHistory(loan_id, custId, i_pay, i_pay, gCode, oneInvestTitle.getSubject(), "I");
					        				} else {
//					        			
					        					invsetDetail = investService.updateInvestPay(mid, loan_id, String.valueOf(Long.parseLong(i_pay) + Long.parseLong(duplicate)));
//					        			
					        					investService.insertInvestHistory(loan_id, custId, i_pay, String.valueOf(Long.parseLong(i_pay) + Long.parseLong(duplicate)), gCode, oneInvestTitle.getSubject(), "U");
					        				}
			        	            		
			        	            		if (invsetDetail > 0 && isInvestAdd > 0) {
//			        	          
			        	            			creMemberService.addCreAgreeMember2(json, custId);
			        	            			
			        	            			JSONObject jsonSmsData = new JSONObject();
			        	            			jsonSmsData.put("title", oneInvestTitle.getSubject());
			        	                		jsonSmsData.put("name", oneInvestTitle.getName());
			        	                		jsonSmsData.put("payment", commonUtil.getAmountUnit3(Long.parseLong(i_pay)));
			        	                		
//			        	     
			        	                		String msg = commonUtil.getFormSMS(6, jsonSmsData);
//			        	    
			        	                		commonUtil.setRequestSMSData(oneInvestTitle.getName(), "I", oneInvestTitle.getCustId(), oneInvestTitle.getHp(), msg);
			        	            			
			        	            			response.setState(200);
				        	            		response.setMessage("정상적으로 처리하였습니다.");
			        	            		} else {
			        	            			response.setState(374);
					        	                response.setMessage("투자자 등록에 예치기 못한 에러가 발생하였습니다. 고객센터에 문의하세요.");
			        	            		}
		        	            		} else {
		        	            			long possible = (((long)eMoney / 10000) * 10000) - Long.parseLong(investPay.getIpay());
		        	            			
				        					if(possible < 0) {
			        	            			Map<String, Object> result = new HashMap<String, Object>();
					        		    		result.put("i_pay", possible);
					        					
					        					response.setState(373);
					        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
					        					response.setResult(result);
					        					
					        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK); 
				        					} else {
				        						response.setState(376);
					        					response.setMessage("예치금액이 적어 투자를 하실 수 없습니다.");
					        					
					        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK); 
				        					}
		        	            		}
					        				
					        		} else {
					        			if(Long.parseLong(i_pay) - (Long.parseLong(i_pay) - Long.parseLong(possiblePay)) > Long.parseLong(limitPay2)) {
				        		    		Map<String, Object> result = new HashMap<String, Object>();
				        		    		result.put("i_pay", limitPay2);
				        					
				        					response.setState(373);
				        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
				        					response.setResult(result);
				        					
				        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
				        		    	}
					        			
					        			Map<String, Object> result = new HashMap<String, Object>();
					            		result.put("i_pay", String.valueOf(Long.parseLong(i_pay) - (Long.parseLong(i_pay) - Long.parseLong(possiblePay))));
					        			
					        			response.setState(372);
					        			response.setMessage("투자가능한 금액한도를 넘었습니다.");
					        			response.setResult(result);
					        		}
					        		
					        	} else {
					    			response.setState(371);
					    			response.setMessage("투자모집이 만료되어 해당 투자상품에 투자가 불가합니다.");
					        	}
				        	} else {
				        		OneInvestTitle oneInvestTitle = investService.selectInvestTitle(mid, loan_id);
		        				
		        				int ranNum = (int)(Math.random() * (999 - 111 + 1)) + 111;
		        				long time = System.currentTimeMillis();
		        				String gCode = "P" + String.valueOf(time) + String.valueOf(ranNum);
		        				
		        				double eMoney = Double.parseDouble(balanceAMT);
		        				double eMoneyCal = (eMoney - Double.parseDouble(i_pay)) - Double.parseDouble(investPay.getIpay());
        	            		
		        				if(eMoneyCal >= 0) {
			        				int isInvestAdd = 0;
//			        				
			        				isInvestAdd = setInvestAdd(gCode, mid, loan_id, i_pay, oneInvestTitle);
			        				
			        				int invsetDetail = 0;
			        				if(duplicate == null) {
//			        			
			        					invsetDetail = investService.insertInvestDetail(setInvestDetail(mid, loan_id, i_pay, String.valueOf(eMoneyCal), oneInvestTitle));
//			        		
			        					investService.insertInvestHistory(loan_id, custId, i_pay, i_pay, gCode, oneInvestTitle.getSubject(), "I");
			        				} else {
//			        		
			        					invsetDetail = investService.updateInvestPay(mid, loan_id, String.valueOf(Long.parseLong(i_pay) + Long.parseLong(duplicate)));
//			        		
			        					investService.insertInvestHistory(loan_id, custId, i_pay, String.valueOf(Long.parseLong(i_pay) + Long.parseLong(duplicate)), gCode, oneInvestTitle.getSubject(), "U");
			        				}
	        	            		
	        	            		if (invsetDetail > 0 && isInvestAdd > 0) {
//	        	          
	        	            			creMemberService.addCreAgreeMember2(json, custId);
	        	            			
	        	            			JSONObject jsonSmsData = new JSONObject();
	        	            			jsonSmsData.put("title", oneInvestTitle.getSubject());
	        	                		jsonSmsData.put("name", oneInvestTitle.getName());
	        	                		jsonSmsData.put("payment", commonUtil.getAmountUnit3(Long.parseLong(i_pay)));
	        	                		
//	        	                	
	        	                		String msg = commonUtil.getFormSMS(6, jsonSmsData);
//	        	        
	        	                		commonUtil.setRequestSMSData(oneInvestTitle.getName(), "I", oneInvestTitle.getCustId(), oneInvestTitle.getHp(), msg);
	        	            			
	        	            			response.setState(200);
		        	            		response.setMessage("정상적으로 처리하였습니다.");
	        	            		}
		        				} else {
        	            			long possible = (((long)eMoney / 10000) * 10000) - Long.parseLong(investPay.getIpay());
        	            			
		        					if(possible < 0) {
	        	            			Map<String, Object> result = new HashMap<String, Object>();
			        		    		result.put("i_pay", possible);
			        					
			        					response.setState(373);
			        					response.setMessage("대출건당 투자가능한 금액한도를 넘었습니다.");
			        					response.setResult(result);
			        					
			        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK); 
		        					} else {
		        						response.setState(376);
			        					response.setMessage("예치금액이 적어 투자를 하실 수 없습니다.");
			        					
			        					return new ResponseEntity<ResponseResult>(response, HttpStatus.OK); 
		        					}
        	            		}
				        	}
				        } else {
							response.setState(370);
							response.setMessage("최소투자금액보다 적게 투자하실 수 없습니다.");
				        }
			        }
	            } else {
	                response.setState(jsonResult.getInt("STATE"));
	                response.setMessage(jsonResult.getString("MESSAGE"));
	            }
	        	
	        } else {
	        	response.setState(365);
				response.setMessage("예치금 잔액이 부족합니다.");
	        }
        } else {
        	response.setState(350);
			response.setMessage("비밀번호가 틀렸습니다.");
        }
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
	
	@ApiOperation(value = "자동투자 등록정보")
    @RequestMapping("/invest/auto/division/get")
    public ResponseEntity<ResponseResult> getAutoDivision(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String mid = jsonRequest.get("mid").toString();
        
        response.setResult(investService.selectInvestAutoDivision2(mid));
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
	
	@ApiOperation(value = "자동투자 등록")
    @RequestMapping("/invest/auto/division/add")
    public ResponseEntity<ResponseResult> setAutoDivision(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String mid = jsonRequest.get("mid").toString();
        final String isActivate = jsonRequest.get("isActivate").toString();
        final String limitLoan = jsonRequest.get("limitLoan").toString();
        final String limitMonth = jsonRequest.get("limitMonth").toString();
        final String agreedYN = jsonRequest.get("agreedYN").toString();
        
        OneInvestAutoDivisionSet oneInvestAutoDivisionSet = new OneInvestAutoDivisionSet();
        oneInvestAutoDivisionSet.setMid(mid);
        oneInvestAutoDivisionSet.setIsActivate(isActivate);
        oneInvestAutoDivisionSet.setLimitLoan(limitLoan);
        oneInvestAutoDivisionSet.setLimitMonth(limitMonth);
        oneInvestAutoDivisionSet.setAgreedYN(agreedYN);
        
        investService.insertInvestAutoDivision(oneInvestAutoDivisionSet);
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
	
	@ApiOperation(value = "일괄투자 정보조회")
    @RequestMapping("/invest/bundle/list")
    public ResponseEntity<ResponseResult> getInvestBundleList(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);

        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");

        final String mid = json.getJSONObject("request").getString("mid");

        InvestBundleInfo investBundleInfo = new InvestBundleInfo();
        List<InvestBundleList> investBundleLists = investService.selectInvestBundleList(mid);
        investBundleInfo.setList(investBundleLists);
        
        OneEmoneyInvestPay oneEmoneyInvestPay = emoneyService.selectEmoneyPayInfo(mid);
        String withdrawPay = emoneyService.selectEmoneyWithdrawPay2(mid);
        String emoneyBalance = emoneyService.selectEmoneyInvestBalance(mid);
        
        long setEmoney = 0;
        if(oneEmoneyInvestPay != null) {
        	setEmoney = Long.parseLong(emoneyBalance) - Long.parseLong(oneEmoneyInvestPay.getIpay()) - Long.parseLong(withdrawPay);
        	emoneyService.updateEmoney(String.valueOf(setEmoney), mid);
        }
        
        investBundleInfo.setInvestAmt(setEmoney);
        
        if (investBundleLists != null) {
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
        	
        	investBundleInfo.setTotInvestMaxPay(Long.parseLong(limitPay));
        	
        	for(int i = 0; i < investBundleLists.size(); i++) {
        		String loanId = investBundleLists.get(i).getLoanId();
        		long loanPay = investBundleLists.get(i).getLoanMoney();
        		float investMaxRate = investBundleLists.get(i).getInvestMaxRate();
        		
	        	String investPossiblePay = investService.selectInvestPossiblePay(loanId);
	        	String investingPay = investService.selectInvestDuplicate(mid, loanId);
	        	
	        	if(investingPay == null || investingPay.isEmpty())
	        		investingPay = "0";
	        	
	        	investBundleLists.get(i).setInvestingPay(Long.parseLong(investingPay));
	        	
	        	if(investPossiblePay != null) {
	        		long InvestPay = (long)(Long.parseLong(investPossiblePay));
	        		long InvestPayPosit = (long)(loanPay * investMaxRate) - Long.parseLong(investingPay);
	        		
	        		if(InvestPayPosit < 0)
	        			InvestPayPosit = 0;
		        	
	        		if(InvestPay > InvestPayPosit)
	        			InvestPay = InvestPayPosit;
	        		
		        	if(InvestPay <= Long.parseLong(limitPay) || sumIpay == 0L) {
		        		investBundleLists.get(i).setInvestMaxPay(InvestPay);
					} else {
						investBundleLists.get(i).setInvestMaxPay(Long.parseLong(limitPay));
					}
	        	} else {
	        		long InvestPay = (long)(Long.parseLong(investPossiblePay));
	        		long InvestPayPosit = (long)(loanPay * investMaxRate) - Long.parseLong(investingPay);
	        		
	        		if(InvestPayPosit < 0)
	        			InvestPayPosit = 0;
	        		
	        		if(InvestPay > InvestPayPosit)
	        			InvestPay = InvestPayPosit;
	        		
	        		if(InvestPay <= Long.parseLong(limitPay)) {
	        			investBundleLists.get(i).setInvestMaxPay(InvestPay);
					} else {
						investBundleLists.get(i).setInvestMaxPay(Long.parseLong(limitPay));
					}
	        	}
        	}
        }
        
        response.setResult(investBundleInfo);

        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
	
	@ApiOperation(value = "일괄투자 정보 저장")
    @RequestMapping("/invest/bundle/add")
    public ResponseEntity<ResponseResult> addInvestBundle(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject json = new JSONObject(requestString);

        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");

        final String mid = json.getJSONObject("request").getString("mid");
        String custId = oneMemberService.selectCustID(mid);
        
        JSONArray jsonList = json.getJSONObject("request").getJSONArray("list");
        
        for(int i = 0; i < jsonList.length(); i++) {
	        	InvestBundleItem investBundleItem = new Gson().fromJson(jsonList.getJSONObject(i).toString(), InvestBundleItem.class);

	        	if(Integer.parseInt(investBundleItem.getPayment()) != 0) {	// 투자금액이 0이 아니면 
		        	investService.insertInvestStack(investBundleItem.getLoanId(), mid, investBundleItem.getPayment());
		        	creMemberService.addCreAgreeMember3(investBundleItem.getAgreement(), custId);
	        	}
        }
        
        // 디버깅시 에러
        // commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);
    }
	
	@ApiOperation(value = "투자자 정산 내역")
    @RequestMapping("/invest/schedule/info")
    public ResponseEntity<ResponseResult> getInvestScheduleInfo(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.getJSONObject("request");
        final String mid = jsonRequest.getString("mid");
        final String loanId = jsonRequest.getString("loanId");
        
        InvestScheduleInfo investScheduleInfo = investService.selectInvestScheduleInfo(mid, loanId);
        List<InvestScheduleItem> investScheduleItems = investService.selectInvestScheduleList(mid, loanId);
        investScheduleInfo.setList(investScheduleItems);
        
        response.setResult(investScheduleInfo);
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
	
	@ApiOperation(value = "투자자 예치금 내역")		// UI상에서 투자자 입출금 내역
    @RequestMapping("/invest/payment/history")
    public ResponseEntity<ResponseResult> getInvestPaymentHistory(@RequestBody String requestString) throws Exception {
		String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
		
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.getJSONObject("request");
        final String mid = jsonRequest.getString("mid");
        int pageNum = jsonRequest.getInt("pageNum");
        
        investService.updateInvestTran(mid);
        
        int listSize = investService.selectInvestPaymentHistoryItemSize(mid);				// 예치금 내용에 대한 갯수 선택
        
        int offSetNum = (pageNum - 1) * rowCount;
		int totCount = listSize;
		int totPageCount = (int) Math.ceil((float)totCount/rowCount);
        
		OneEmoneyInvestPay oneEmoneyInvestPay = emoneyService.selectEmoneyPayInfo(mid);		// 투자자의 현재 투자금액과 custId 선택
        String withdrawPay = emoneyService.selectEmoneyWithdrawPay2(mid);					// cwt-투자자의 처리되지 않은 출금금액
        String emoneyBalance = emoneyService.selectEmoneyInvestBalance(mid);				// ctl-현재잔액(모든 입금내용-출금내용)

        long setEmoney = 0;
        if(oneEmoneyInvestPay != null) {
        	setEmoney = Long.parseLong(emoneyBalance) - Long.parseLong(oneEmoneyInvestPay.getIpay()) - Long.parseLong(withdrawPay);
        	emoneyService.updateEmoney(String.valueOf(setEmoney), mid);						// 현재 잔액 업데이트
        }
		
        PaymentHistoryInfo investPaymentHistoryInfo = investService.selectInvestPaymentHistoryInfo(mid);		// mari_seyfert-계좌정보선택
        List<PaymentHistoryItem> investPaymentHistoryItems = investService.selectInvestPaymentHistoryItem(offSetNum, rowCount, mid);
        
        if(investPaymentHistoryItems == null)
        	investPaymentHistoryItems = new ArrayList<>();
        
        if(investPaymentHistoryInfo == null)
        	investPaymentHistoryInfo = new PaymentHistoryInfo();
        
		investPaymentHistoryInfo.setList(investPaymentHistoryItems);
		investPaymentHistoryInfo.setTrxAmt(String.valueOf(setEmoney));
		investPaymentHistoryInfo.setTotPageCount(totPageCount);
        
        response.setResult(investPaymentHistoryInfo);
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
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
}
