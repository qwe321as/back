package com.crepass.restfulapi.v2.controller;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.inside.service.DepositService;
import com.crepass.restfulapi.one.domain.OneEmoneyInvestPay;
import com.crepass.restfulapi.one.service.EmoneyService;
import com.crepass.restfulapi.one.service.InvestService;
import com.crepass.restfulapi.one.service.LoanService;
import com.crepass.restfulapi.one.service.OneMemberService;
import com.crepass.restfulapi.one.service.ScheService;
import com.crepass.restfulapi.one.service.StatisticsService;
import com.crepass.restfulapi.one.service.WishService;
import com.crepass.restfulapi.v2.domain.InvestMember;
import com.crepass.restfulapi.v2.domain.LoanMember;
import com.crepass.restfulapi.v2.domain.MemberInvestInfo;
import com.google.gson.Gson;

import io.swagger.annotations.ApiOperation;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@CrossOrigin
@RestController
@RequestMapping(path = "/api2", method = {RequestMethod.POST, RequestMethod.GET})
public class StatisticsControllerV2 {
    
	@Autowired
    private StatisticsService statService;
    
	@Autowired
    private EmoneyService emoneyService;
	
	@Autowired
    private WishService wishService;
    
    @Autowired
    private InvestService investService;

	@Autowired
    private LoanService loanService;
	
    @Autowired
    private ScheService scheService;

	@Autowired
    private DepositService depositService;
	
	@Autowired
    private OneMemberService oneMemberService;
	
	@Autowired
    private CommonUtil commonUtil;
    
    @Autowired(required=true)
	private HttpServletRequest request;
	
    @ApiOperation(value = "투자자 메인")
    @RequestMapping("/statistics/invest")
    public ResponseEntity<ResponseResult> getInvestMain(@RequestBody String requestString) throws Exception {
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
 	    ResponseResult response = new ResponseResult();
    
 	    JSONObject jsonMember = new JSONObject(requestString);

        String mid = jsonMember.getJSONObject("request").get("mid").toString();
        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        InvestMember investMember = statService.getInvestMember(mid);								// 투자자 정보선택(투자잔액 등 정보)
        // getInvestMember의 현재잔액이 투자이력이 없으면 0으로 나오는 이슈 발생, emoney 다시 선택해서 오류 임시해결  
        String emoneyBalance = emoneyService.selectEmoneyInvestBalance(mid);						// ctl-현재잔액(모든 입금내용-출금내용)
        investMember.setTotDepositPay(Long.parseLong(emoneyBalance));
        
        OneEmoneyInvestPay oneEmoneyInvestPay = emoneyService.selectEmoneyPayInfo(mid);	// 현재 펀딩중인 투자액의 합
        if (oneEmoneyInvestPay == null) {
        	oneEmoneyInvestPay = new OneEmoneyInvestPay();
        }
        
        String withdrawPay = emoneyService.selectEmoneyWithdrawPay2(mid);				// 출금예정인 금액의 합

        // ctl에서 현재 펀딩중인 투자액의 합과 출금 예정인 금액의 합을 제한 금액
        long toDepositPay = investMember.getTotDepositPay() - Long.parseLong(oneEmoneyInvestPay.getIpay()) - Long.parseLong(withdrawPay);
        investMember.setTotDepositPay(toDepositPay); 
        
        
        MemberInvestInfo memberInvestInfo = statService.selectMemberInvestInfo(mid);
        
        if(investMember == null)
        	investMember = new InvestMember();
        
        int isCheckInvestInfo = 0;
        
        //
                int wishTotalCount = wishService.selectWishByIdSize(mid);
                int investingTotalCount = investService.selectInvestingTotalCount(mid);
                
                investMember.setWishTotalCount(wishTotalCount);
                investMember.setInvestingTotalCount(investingTotalCount);
                
                
                // 기존 대출자 조회한정보 가지고 오기
                String investListCheckTime = scheService.selectInvestListCheckTime(mid);		// cvs-투자내역
                String investTranCheckTime = scheService.selectInvestTranCheckTime(mid);		// cvs-투자거래내역

                
                if( (investListCheckTime == null) || (investTranCheckTime == null) ) {
                	SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
                	Date time = new Date();
                	String currentTime = sdf.format(time);
                	
                	scheService.insertViewStatus(mid, currentTime);								// 없으면 생성
                	investListCheckTime = currentTime;
                	investTranCheckTime = currentTime;
                }
                
                // 가장 최근 생긴 정보 가지고 오기
                String investLastedListTime = investService.selectRecentRegTime(mid);			// mi-가장최근 투자한 시간정보
                String investLastedTranTime = investService.selectRecentInvestTime(mid);		// ctl-가장최근  거래 시간정보
                // String investLastedCondTime = depositService.selectRecentDepositTime("loanId");
                	
                if (investLastedListTime.compareTo(investListCheckTime) > 0)
                	investMember.setInvestNewFlag("Y");
                
                if (investLastedTranTime.compareTo(investTranCheckTime) > 0)
                	investMember.setTranNewFlag("Y");
                
        
        if(memberInvestInfo != null) {
        	if(memberInvestInfo.getBankAccntNum() != null && !memberInvestInfo.getBankAccntNum().isEmpty()
			&& memberInvestInfo.getInvestVirAccntNum() != null && !memberInvestInfo.getInvestVirAccntNum().isEmpty()
			&& memberInvestInfo.getReginum() != null && !memberInvestInfo.getReginum().isEmpty()) {
        		isCheckInvestInfo = 1;
        	}
        }
        
        investMember.setIsCheckInvestInfo(isCheckInvestInfo);
        
        response.setResult(investMember);
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "대출자 메인")
    @RequestMapping("/statistics/loan")
    public ResponseEntity<ResponseResult> getLoanMain(@RequestBody String requestString) throws Exception {
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
    	ResponseResult response = new ResponseResult();
    	
        JSONObject jsonMember = new JSONObject(requestString);

        String mid = jsonMember.getJSONObject("request").get("mid").toString();
        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        LoanMember loanMember = statService.getLoanMember(mid);
        
        // 기존 대출자 조회한정보 가지고 오기
         String investCondCheckTime = scheService.selectInvestCondCheckTime(mid);
         
         if(investCondCheckTime == null) {
         	SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
         	Date time = new Date();
         	String currentTime = sdf.format(time);
         	
         	scheService.insertViewStatus(mid, currentTime);								// 없으면 생성
         	investCondCheckTime = currentTime;
         }
         // 가장 최근 생긴 정보 가지고 오기
         String investLastedCondTime = loanService.selectRecentLoanExecTime(mid);		// 최신 대출내용 시간선택
         if (investLastedCondTime == null)
        	 investLastedCondTime = "0000-00-00";
        	
        if (investLastedCondTime.compareTo(investCondCheckTime) > 0)
        	loanMember.setLoanContNewFlag("Y");
        
        String loanCount = loanService.selectLoanCount(mid);
        loanMember.setLoanCount(loanCount);
    		
        if( loanCount == null ) 
        	loanMember.setLoanCount("0");
        
        if(loanMember == null)
        	loanMember = new LoanMember();
        
        response.setResult(loanMember);
        
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
    	return new ResponseEntity<ResponseResult>(response, HttpStatus.OK); 
    }
}