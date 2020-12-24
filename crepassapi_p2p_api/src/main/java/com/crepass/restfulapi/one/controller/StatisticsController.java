package com.crepass.restfulapi.one.controller;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.cre.domain.CreSocialBanner;
import com.crepass.restfulapi.cre.service.SocialService;
import com.crepass.restfulapi.inside.service.DepositService;
import com.crepass.restfulapi.one.domain.OneStatistics;
import com.crepass.restfulapi.one.domain.OneStatisticsInvest;
import com.crepass.restfulapi.one.service.EventService;
import com.crepass.restfulapi.one.service.OneMemberService;
import com.crepass.restfulapi.one.service.StatisticsService;

import io.swagger.annotations.ApiOperation;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
@RequestMapping(path = "/api", method = RequestMethod.POST)
public class StatisticsController {
    
    @Autowired
    private StatisticsService statService;
    
    @Autowired
    private SocialService socialService;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private OneMemberService oneMemberService;
    
    @Autowired
    private DepositService depositService;
    
    @Autowired
    private CommonUtil commonUtil;
    
    @Autowired(required=true)
	private HttpServletRequest request;
    
    @ApiOperation(value = "Main 통계")
    @RequestMapping("/statistics")
    public ResponseEntity<ResponseResult> getStatistics(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        final String mid = ((JSONObject) jsonMember.get("request")).get("mid").toString();
        OneStatistics oneStatistics = statService.selectStatisticsById(mid);
        
        CreSocialBanner basicBanner = new CreSocialBanner();
        basicBanner.setId("0");
        basicBanner.setMemo("");
        basicBanner.setCorpName("");
        basicBanner.setSort("0");
        basicBanner.setBannerUrl("https://p2p.crepass.com/img/bg.png");
        
        List<CreSocialBanner> creSocialBanners = socialService.selectSocialBannerList();
        creSocialBanners.add(basicBanner);
        
        Collections.sort(creSocialBanners, new AscendingCreSocialBanner());
        
        oneStatistics.setImgPathArray(creSocialBanners);
        oneStatistics.setImgPath("https://p2p.crepass.com/img/bg.png");
        oneStatistics.setTextMessage1("청년의 꿈에 직접투자 <BR> 100%가 청년에게 투자됩니다");
        oneStatistics.setTextMessage2("빅데이터 신용평가 <BR> (20년 금융전문가의 신용평가모델)");
        oneStatistics.setTextMessage3("소셜단체와 함께 임팩트 확장");
        
        String isEvent = eventService.selectIsEvent(mid);
        String eventCouponShow = "N";
        
        if(isEvent == null || Integer.parseInt(isEvent) == 0)
        	eventCouponShow = "Y";
        
        oneStatistics.setIsEventCouponShow(eventCouponShow);
        
        response.setResult(oneStatistics);
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }

    @ApiOperation(value = "Web 통계")
    @RequestMapping("/statistics/web")
    public ResponseEntity<ResponseResult> getStatisticsWeb() throws Exception {
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, "");
    	
        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        OneStatistics oneStatistics = statService.selectStatisticsWebById();
        
        oneStatistics.setImgPath("https://p2p.crepass.com/img/bg.png");
        oneStatistics.setTextMessage1("청년의 꿈에 직접투자 <BR> 100%가 청년에게 투자됩니다");
        oneStatistics.setTextMessage2("빅데이터 신용평가 <BR> (20년 금융전문가의 신용평가모델)");
        oneStatistics.setTextMessage3("소셜단체와 함께 임펙트 확장");
        
        response.setResult(oneStatistics);
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    @ApiOperation(value = "투자자 메인")
    @RequestMapping("/statistics/invest")
    public ResponseEntity<ResponseResult> getInvestMain(@RequestBody String requestString) throws Exception {
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
    	
        ResponseResult response = new ResponseResult();
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        final String mid = ((JSONObject) jsonMember.get("request")).get("mid").toString();
        
        String isStatisticsInvest = statService.selectStatisticsIsInvest(mid);
        String custId = oneMemberService.selectCustID(mid);
        
        OneStatisticsInvest oneStatisticsInvest = new OneStatisticsInvest();
        
        if(isStatisticsInvest != null) {
        	oneStatisticsInvest = statService.selectStatisticsInvest(mid);
        	oneStatisticsInvest.setTotDepositPay2(depositService.selectTotalDepositPay(custId));
        }
        
        response.setResult(oneStatisticsInvest);
        
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    class AscendingCreSocialBanner implements Comparator<CreSocialBanner> {
    	@Override
    	public int compare(CreSocialBanner a, CreSocialBanner b) {
    		return a.getSort().compareTo(b.getSort());
		}
	}
}