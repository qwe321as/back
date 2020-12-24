package com.crepass.restfulapi.v2.controller;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.cre.domain.CreMember;
import com.crepass.restfulapi.cre.service.CreMemberService;
import com.crepass.restfulapi.one.domain.OneEmoneyInvestPay;
import com.crepass.restfulapi.one.domain.OneNoticeMain;
import com.crepass.restfulapi.one.service.BoardService;
import com.crepass.restfulapi.one.service.EmoneyService;
import com.crepass.restfulapi.one.service.SlideService;
import com.crepass.restfulapi.v2.domain.SlideInfo;
import com.crepass.restfulapi.v2.domain.OneEventItem;
import com.google.gson.Gson;

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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@CrossOrigin
@RestController

@RequestMapping(path = "/api2", method = RequestMethod.POST)
public class SlideControllerV2 {
    
    @Autowired
    private SlideService slideService;
    
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
    
    @Value("${crepas.inside.url}")
    private String insideUrl;
//    
//    @ApiOperation(value = "슬라이드 메뉴")
//    @RequestMapping("/slide")
//    public ResponseEntity<ResponseResult> slideMenu(@RequestBody String requestString) throws Exception {
//    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
//        commonUtil.sendRequestLogging(mapping_url, requestString);
//    	
//        JSONObject jsonMember = new JSONObject(requestString);
//        
//        ResponseResult response = new ResponseResult();        
//        response.setState(200);
//        response.setMessage("정상적으로 처리하였습니다.");
//        
//        final String mid = ((JSONObject) jsonMember.get("request")).get("mid").toString();
//        
//        OneEmoneyInvestPay oneEmoneyInvestPay = emoneyService.selectEmoneyPayInfo(mid);
//        String withdrawPay = emoneyService.selectEmoneyWithdrawPay2(mid);
//        String emoneyBalance = emoneyService.selectEmoneyInvestBalance(mid);
//        
//        if(oneEmoneyInvestPay != null) {
//        	long setEmoney = Long.parseLong(emoneyBalance) - Long.parseLong(oneEmoneyInvestPay.getIpay()) - Long.parseLong(withdrawPay);
//        	emoneyService.updateEmoney(String.valueOf(setEmoney), mid);
//        }
//
//        SlideInfo slideInfo = slideService.selectSlideInfo(mid);
//        
//        String alarm = ((CreMember) creMemberService.selectMemberById(mid)).getAlarm();
//        String tdiaryMsg = ((CreMember) creMemberService.selectMemberById(mid)).getTdiaryMsg();
//        slideInfo.setAlarm(alarm);
//        slideInfo.setTdiaryMsg(tdiaryMsg);
//        
//        OneNoticeMain oneNoticeMain = boardService.selectNoticeMain();
//        if(oneNoticeMain != null)
//        	slideInfo.setNoticeMsg(boardService.selectNoticeMain());
//        else
//        	slideInfo.setNoticeMsg(new OneNoticeMain());
//
//        response.setResult(slideInfo);
//        
//        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
//        
//        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
//    }
    
    
    // 200518
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

        SlideInfo slideInfo = slideService.selectSlideInfo(mid);			// 이벤트 관련정보 선택
        if(slideInfo == null)
        	slideInfo = new SlideInfo();
        
//        String alarm=null;
//        if( ((CreMember) creMemberService.selectMemberById(mid)).getAlarm() != null) {
//        	alarm = ((CreMember) creMemberService.selectMemberById(mid)).getAlarm();
//        }
//        
//        String tdiaryMsg = ((CreMember) creMemberService.selectMemberById(mid)).getTdiaryMsg();
//        slideInfo.setAlarm(alarm);
//        slideInfo.setTdiaryMsg(tdiaryMsg);
        
        OneNoticeMain oneNoticeMain = boardService.selectNoticeMain();		// 
        if(oneNoticeMain != null)
        	slideInfo.setNoticeMsg(oneNoticeMain);
        else
        	slideInfo.setNoticeMsg(new OneNoticeMain());
        
        String mType = null;			// mType이 있을경우 메인이벤트 선택
        OneNoticeMain oneEventItem = null;

        if( ((JSONObject) jsonMember.get("request")).has("mType") ) {
        	
        	mType = ((JSONObject)jsonMember.get("request")).get("mType").toString();        
        	// null값 달라고 안드로이드 개발자 요청
//        	if(mType.equals("L")) mType="대출자";
//        	else if(mType.equals("I")) mType="투자자";
        	oneEventItem = boardService.selectOneEventItemV2(mType);
        	
        	if (oneEventItem != null) {
        		slideInfo.setNoticeMsg(oneEventItem);
        	}
        } 
        
        // 투자자, 대줄자 배너가 있어도, 공지사항 배너가 우선순위이므로 한번더 체크 
        String bannerId = boardService.selectGetBannerId();
        if (bannerId != null)
        	slideInfo.getNoticeMsg().setEvent_code(bannerId);
        
        response.setResult(slideInfo);
        
        
        // 여기는 에러 안남, 다른데는 에러남;
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }

    
 // 200518
    @ApiOperation(value = "슬라이드 메뉴 ")
    @RequestMapping("/slide2")
    public ResponseEntity<ResponseResult> slideMenuV2(@RequestBody String requestString, @RequestHeader String apkVersion) throws Exception {
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

        SlideInfo slideInfo = slideService.selectSlideInfo(mid);			// 이벤트 관련정보 선택
        if(slideInfo == null)
        	slideInfo = new SlideInfo();
        
//        String alarm=null;
//        if( ((CreMember) creMemberService.selectMemberById(mid)).getAlarm() != null) {
//        	alarm = ((CreMember) creMemberService.selectMemberById(mid)).getAlarm();
//        }
//        
//        String tdiaryMsg = ((CreMember) creMemberService.selectMemberById(mid)).getTdiaryMsg();
//        slideInfo.setAlarm(alarm);
//        slideInfo.setTdiaryMsg(tdiaryMsg);
//        
//        OneNoticeMain oneNoticeMain = boardService.selectNoticeMain();		// 
//        if(oneNoticeMain != null)
//        	slideInfo.setNoticeMsg(oneNoticeMain);
//        else
//        	slideInfo.setNoticeMsg(new OneNoticeMain());
        
        String mType = null;			// mType이 있을경우 메인이벤트 선택
        OneNoticeMain oneEventItem = null;

        if( ((JSONObject) jsonMember.get("request")).has("mType") ) {
        	
        	mType = ((JSONObject)jsonMember.get("request")).get("mType").toString();        	
        	if(mType.equals("L")) mType="대출자";
        	else if(mType.equals("I")) mType="투자자";
        	
        	oneEventItem = boardService.selectOneEventItemV2(mType);
        	
            if( oneEventItem.getFileImg().equals("") || oneEventItem.getFileImg() == null)
            	oneEventItem.setMNoticeImg(null);
        	
        	if (oneEventItem != null) {
        		slideInfo.setNoticeMsg(oneEventItem);
        	} else
        		slideInfo.setNoticeMsg(new OneNoticeMain());		// 널값 들어가는 부분 에러처리
        } 
        
        OneNoticeMain oneNoticeMain = boardService.selectNoticeMainV2();		// 공지사항이 있으면 이벤트 덮어쓰고 입력 
        if(oneNoticeMain != null)
        	slideInfo.setNoticeMsg(oneNoticeMain);
        
        // 파일명이 없으면 mNoticeImg에 null반환
        if( oneNoticeMain.getFileImg().equals("") || oneNoticeMain.getFileImg() == null)
        	oneNoticeMain.setMNoticeImg(null);
        
        response.setResult(slideInfo);
        
        
        // 여기는 에러 안남, 다른데는 에러남;
        commonUtil.sendResultLogging(mapping_url, new Gson().toJson(response));
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }

}