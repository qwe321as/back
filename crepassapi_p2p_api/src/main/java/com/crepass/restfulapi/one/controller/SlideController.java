package com.crepass.restfulapi.one.controller;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.cre.domain.CreIntro;
import com.crepass.restfulapi.cre.domain.CreMember;
import com.crepass.restfulapi.cre.service.CreMemberService;
import com.crepass.restfulapi.cre.service.IntroService;
import com.crepass.restfulapi.one.domain.OneEmoneyInvestPay;
import com.crepass.restfulapi.one.domain.OneNoticeMain;
import com.crepass.restfulapi.one.domain.OneSlide;
import com.crepass.restfulapi.one.service.BoardService;
import com.crepass.restfulapi.one.service.EmoneyService;
import com.crepass.restfulapi.one.service.OneMemberService;
import com.crepass.restfulapi.one.service.SlideService;

import io.swagger.annotations.ApiOperation;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@RequestMapping(path = "/api", method = RequestMethod.POST)
public class SlideController {
    
    @Autowired
    private SlideService slideService;
    
    @Autowired
    private IntroService introService;
    
    @Autowired
    private EmoneyService emoneyService;
    
    @Autowired
    private CreMemberService creMemberService;
    
    @Autowired
    private CommonUtil commonUtil;
    
    @Autowired(required=true)
	private HttpServletRequest request;
    
    @Autowired
    private BoardService boardService;
    
    @Autowired
    private OneMemberService oneMemberService;
    
    @Value("${crepas.inside.url}")
    private String insideUrl;
    
    @ApiOperation(value = "슬라이드 메뉴")
    @RequestMapping("/slide")
    public ResponseEntity<ResponseResult> slideMenu(@RequestBody String requestString) throws Exception {
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        final String mid = ((JSONObject) jsonMember.get("request")).get("mid").toString();
        
        OneEmoneyInvestPay oneEmoneyInvestPay = emoneyService.selectEmoneyPayInfo(mid);
        String withdrawPay = emoneyService.selectEmoneyWithdrawPay2(mid);
        String emoneyBalance = emoneyService.selectEmoneyInvestBalance(mid);
        
        if(oneEmoneyInvestPay != null) {
        	long setEmoney = Long.parseLong(emoneyBalance) - Long.parseLong(oneEmoneyInvestPay.getIpay()) - Long.parseLong(withdrawPay);
        	emoneyService.updateEmoney(String.valueOf(setEmoney), mid);
        }
        
        String isAutoInvest = oneMemberService.selectAutoInvestAgree(mid);
        
        if(isAutoInvest == null || isAutoInvest.equals("null") || isAutoInvest.length() < 1)
        	isAutoInvest = "N";
        
        OneSlide oneSlide = slideService.selectWlastById(mid);
        CreIntro creIntro = introService.selectIntroInfo();
        oneSlide.setAppVersion(creIntro.getAppVersion());
        
        String alarm = ((CreMember) creMemberService.selectMemberById(mid)).getAlarm();
        String tdiaryMsg = ((CreMember) creMemberService.selectMemberById(mid)).getTdiaryMsg();
        oneSlide.setAlarm(alarm);
        oneSlide.setTdiaryMsg(tdiaryMsg);
        oneSlide.setIsAutoInvest(isAutoInvest);
        
        OneNoticeMain oneNoticeMain = boardService.selectNoticeMain();
        if(oneNoticeMain != null)
        	oneSlide.setNoticeMsg(boardService.selectNoticeMain());
        else
        	oneSlide.setNoticeMsg(new OneNoticeMain());

        response.setResult(oneSlide);
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }

}