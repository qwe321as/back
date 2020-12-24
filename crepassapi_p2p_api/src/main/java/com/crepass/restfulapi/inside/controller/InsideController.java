package com.crepass.restfulapi.inside.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.cre.service.InvestP2pService;
import com.crepass.restfulapi.inside.domain.InsideIPJIInfo;
import com.crepass.restfulapi.inside.service.DepositService;
import com.crepass.restfulapi.one.service.OneMemberService;
import com.crepass.restfulapi.one.service.ScheService;

import io.swagger.annotations.ApiOperation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@CrossOrigin
@RestController
@RequestMapping(path = "/inside")
public class InsideController {

	
	@Autowired(required=true)
	private HttpServletRequest request;
	
	@Autowired
    private CommonUtil commonUtil;

    @Autowired
    private OneMemberService oneMemberService;
	
    @Autowired
    private InvestP2pService investP2pService;

    @Autowired
    private DepositService depositService;

	@Autowired
    private ScheService scheService;
	
	
	//
	//	{
	//		"request":{
	//			"mid":"igothewar@naver.com"
	//		}
	//	}
	//	
	
    @ApiOperation(value = "현재잔액 및 내역")
    @RequestMapping("/balanceList")
    public ResponseEntity<ResponseResult> balanceList(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String mid = jsonRequest.get("mid").toString();
        
        //mid에서 custid가지고 옴;
        String custId = oneMemberService.selectCustID(mid);
        
        //해당 custid 타행에서 실입금액 및 대출자로부터 투자자에게 지급된금액 모두 선택
        // amtGbn=Iip(투자자입금), Lip(대출자입금), Iji(투자자가 대출자로부터 지급받음) 
        
        // 제외목록 : 	20200511 중복으로 3번 더 출금되어져서 돌려받은 내용 
        List<InsideIPJIInfo> insideIPJIInfo = depositService.selectInsideIPJIInfo(custId);
        		
       
        // amtGbn=Ive(출금:투자금) / Wve(출금:타행)
        
        // 제외목록 :  20200403 잔액 부족해서 대출실행 한된건 제외(loanId:3854) 
        //			20200514 중복으로 3번 더 출금되어져서 돌려받은 내용
        // 			투자금 모자라서 투자는 했는데 나가지 않은건 (loanId:3722),,,,,,,,,,,, 다시 확인해보니 투자해서 돈나갔음;
        // 5/29 권관택, 김호민, 6/1 김기영, 6/2 이경재, SK 빼옴처리 이분들 예외처리 해야함!!
        List<InsideIPJIInfo> p2pInvestInfo = investP2pService.selectP2pInvestInfo(custId);
        
        // 두개의 DB가 다르기 때문에 합쳐서 정렬해야함
        List<InsideIPJIInfo> totalInfoList = new ArrayList<InsideIPJIInfo>();
        totalInfoList.addAll(insideIPJIInfo);
        totalInfoList.addAll(p2pInvestInfo);
        
        Collections.sort(totalInfoList);
        
        // 
        
        System.out.println(totalInfoList.size() + ", " + totalInfoList.toString());
        
        long tmtIip=0;		// 투자자입금(타행)
        long tmtLip=0;		// 대출자상환금
        long tmtIji=0;		// 투자자투자금
        long tmtIve=0;		// 투자자출금(타행)
        long tmtWve=0;		// 대출자 입금(타행)
        
      for(int i=0; i<totalInfoList.size(); i++) {
    	//	타입	일시	금액
    	  // 투자자입금(타행)-Iip	대출자상환금(타행)-Lip	(대출자로부터)투자자지급금-Iji		투자자투자금-Ive	투자자타행출금-Wve
    	if(totalInfoList.get(i).getAmtGbn().equals("Iip")) {
    		tmtIip += totalInfoList.get(i).getTrAmt();
    		System.out.println("투자자입금(타행)	" + totalInfoList.get(i).getPaidDate() + "	" + totalInfoList.get(i).getTrAmt());
    	}
//    	else if(totalInfoList.get(i).getAmtGbn().equals("Lip")) {
//    		tmtLip += totalInfoList.get(i).getTrAmt();
//    		System.out.println("대출자입금(타행)	" + totalInfoList.get(i).getPaidDate() + "					" + totalInfoList.get(i).getTrAmt());
//    	} 
    	else if(totalInfoList.get(i).getAmtGbn().equals("Iji")) {
    		tmtIji += totalInfoList.get(i).getTrAmt();
    		System.out.println("투자자지급금	" + totalInfoList.get(i).getPaidDate() + "		" + totalInfoList.get(i).getTrAmt());
    	}
    	else if(totalInfoList.get(i).getAmtGbn().equals("Ive")) {
    		tmtIve += totalInfoList.get(i).getTrAmt();
    		System.out.println("투자자투자금	" + totalInfoList.get(i).getPaidDate() + "			" + totalInfoList.get(i).getTrAmt());
    	}
    	else if(totalInfoList.get(i).getAmtGbn().equals("Wve")) {
    		tmtWve += totalInfoList.get(i).getTrAmt();
    		System.out.println("투자자타행출금	" + totalInfoList.get(i).getPaidDate() + "				" + totalInfoList.get(i).getTrAmt());
    	}
    }
        
        
        
        // 총계와 전문호출한 값까지 해서 검증
//   		System.out.println("투자계좌총합 : " + investIpAmt + ", 대출계좌총합 : " + loanIpAmt + ", 투자자가지급받은금액합 : " + investJiAmt);
        
        // error msg => Handler dispatch failed;
//        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }

    
    

    @ApiOperation(value = "배치용 투자자 한주간 총입출금(예치금) 체크(대출받은 투자자 제외)")
    @RequestMapping("/weekBalanceCheck")
    public ResponseEntity<ResponseResult> weekBalanceCheck(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject jsonRequest = (JSONObject)jsonMember.get("request");
        final String mid = jsonRequest.get("mid").toString();
        
        List<String> allInvestorsList = scheService.selectAllInvestorsList();
        
        int cntTrue=0;	// 검증값 이상없으면 카운트 
        int cntFalse=0;	// 검증값 이상 있으면 카운트
        
        // 어제날짜와 일주일전 날짜 구하기
		SimpleDateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd");    
        Date dateInit = new Date();
        String today =  sdf.format(dateInit);
        Date dateToday = sdf.parse(today);			// Cal에 넣으려면 어쩔수 없이 Date로 변경해야함;
        
        Calendar calYesterday = Calendar.getInstance();
        Calendar calWeekAgo = Calendar.getInstance();
        
        calYesterday.setTime(dateToday);
        calWeekAgo.setTime(dateToday);
        calYesterday.add(Calendar.DATE, -1);
        calWeekAgo.add(Calendar.DATE, -7);
        String yesterday = sdf.format(calYesterday.getTime()); 
        String weekAgo = sdf.format(calWeekAgo.getTime());
        
        SimpleDateFormat  sdfNonUnderline = new SimpleDateFormat("yyyyMMdd");    
        
        for(int i=0; i<allInvestorsList.size(); i++) {
	        
	        //mid에서 custid가지고 옴;
	        String custId = oneMemberService.selectCustID(allInvestorsList.get(i));
	        
	        //해당 custid 타행에서 실입금액 및 대출자로부터 투자자에게 지급된금액 모두 선택
	        // amtGbn=Iip(투자자입금), Lip(대출자입금), Iji(투자자가 대출자로부터 지급받음) 
	        
	        // 제외목록 : 	20200511 중복으로 3번 더 출금되어져서 돌려받은 내용 
	        List<InsideIPJIInfo> insideIPJIInfo = depositService.selectInsideIPJIInfo(custId);
	        		
	       
	        // amtGbn=Ive(출금:투자금) / Wve(출금:타행)
	        
	        // 제외목록 : 	InsidebankController에 balanceList메서드 제외목록 참고
	        List<InsideIPJIInfo> p2pInvestInfo = investP2pService.selectP2pInvestInfo(custId);
	        
	        // 두개의 DB가 다르기 때문에 합쳐서 정렬해야함
	        List<InsideIPJIInfo> totalInfoList = new ArrayList<InsideIPJIInfo>();
	        totalInfoList.addAll(insideIPJIInfo);
	        totalInfoList.addAll(p2pInvestInfo);
	        
	        Collections.sort(totalInfoList);
	        
	        
	        long dailySumIip=0;		// 투자자입금(타행)
//	        long dailySumLip=0;		// 대출자상환금
	        long dailySumIji=0;		// 투자자투자금
	        long dailySumIve=0;		// 투자자출금(타행)
	        long dailySumWve=0;		// 대출자 입금(타행)
	
	        
            for(int j=0; j<totalInfoList.size(); j++) {					
                													
               	Date targetDate =  sdfNonUnderline.parse( totalInfoList.get(j).getPaidDate().substring(0, 8));
               	String targetStr = sdf.format(targetDate);
               	
               	if( getBetweenWeekAgoAndYesterday(weekAgo, yesterday, targetStr) ){		//최근 일주일간 거래내역이면
                
		          	if(totalInfoList.get(j).getAmtGbn().equals("Iip")) {
		          		dailySumIip += totalInfoList.get(j).getTrAmt();
	//	          		System.out.println("투자자입금(타행)	" + totalInfoList.get(j).getPaidDate() + "	" + totalInfoList.get(j).getTrAmt());
		          		
		          	}
	//	          	else if(totalInfoList.get(j).getAmtGbn().equals("Lip")) {
	//	          		dailySumLip += totalInfoList.get(j).getTrAmt();
	//	          		System.out.println("대출자 입금(타행)	" + totalInfoList.get(j).getPaidDate() + "					" + totalInfoList.get(j).getTrAmt());
	//	          		
	//	          	}
		          	else if(totalInfoList.get(j).getAmtGbn().equals("Iji")) {
		          		dailySumIji += totalInfoList.get(j).getTrAmt();
	//	          		System.out.println("투자자지급금	" + totalInfoList.get(j).getPaidDate() + "		" + totalInfoList.get(j).getTrAmt());
		          		
		          	} else if(totalInfoList.get(j).getAmtGbn().equals("Ive")) {
		          		dailySumIve += totalInfoList.get(j).getTrAmt();
	//	          		System.out.println("투자자투자금	" + totalInfoList.get(j).getPaidDate() + "			" + totalInfoList.get(j).getTrAmt());
		          		
		          	} else if(totalInfoList.get(j).getAmtGbn().equals("Wve")) {
		          		dailySumWve += totalInfoList.get(j).getTrAmt();
	//	          		System.out.println("투자자출금(타행)	" + totalInfoList.get(j).getPaidDate() + "				" + totalInfoList.get(j).getTrAmt());
		          	}
		      	}
            }

	      //System.out.println(dailySumIip);
//	      		System.out.println(dailySumLip);
	      //System.out.println(dailySumIji);
	      //System.out.println(dailySumIve);
	      //System.out.println(dailySumWve);

			// 어제 입출금 내역에대한 ctl과 프로그램 검증값 비교
			long dailyTrxTmt = scheService.selectForAWeekSumDW(allInvestorsList.get(i));
		  	// 투자자입금(타행)-Iip	(대출자로부터)투자자지급금-Iji		투자자투자금-Ive	투자자타행출금-Wve
	        long dailyInsideTmt = dailySumIip+dailySumIji-dailySumIve-dailySumWve;
	        
	        if (i==0)
	        	System.out.println("지난 일주일간	m_id	어제 trx입출금 내역	검증값");
	        
	        if (dailyTrxTmt == dailyInsideTmt)
	        	cntTrue++;
	        else cntFalse++;
	        
	        System.out.println(allInvestorsList.get(i) + "	" + dailyTrxTmt + "	"+ dailyInsideTmt);
	        
	        
	        /*
	        System.out.println(totalInfoList.size() + ", " + totalInfoList.toString());
	        
	        long tmtIip=0;		// 투자자입금(타행)
	        long tmtLip=0;		// 대출자상환금
	        long tmtIji=0;		// 투자자투자금
	        long tmtIve=0;		// 투자자출금(타행)
	        long tmtWve=0;		// 대출자 입금(타행)
	        
	      // 데일리 검증리스트  & 총입출금(예치금) 비교
	      // 2. Detail (한 투자자당 세부리스트)  
	        
	      for(int i=0; i<totalInfoList.size(); i++) {
	    	//	타입	일시	금액
	    	  // 투자자입금(타행)	대출자상환금	투자자투자금	투자자출금(타행)	대출자 입금(타행)
	    	if(totalInfoList.get(i).getAmtGbn().equals("Iip")) {
	    		tmtIip += totalInfoList.get(i).getTrAmt();
	    		System.out.println("투자자입금(타행)	" + totalInfoList.get(i).getPaidDate() + "	" + totalInfoList.get(i).getTrAmt());
	    		
	    	} else if(totalInfoList.get(i).getAmtGbn().equals("Lip")) {
	    		tmtLip += totalInfoList.get(i).getTrAmt();
	    		System.out.println("대출자 입금(타행)	" + totalInfoList.get(i).getPaidDate() + "					" + totalInfoList.get(i).getTrAmt());
	    		
	    	} else if(totalInfoList.get(i).getAmtGbn().equals("Iji")) {
	    		tmtIji += totalInfoList.get(i).getTrAmt();
	    		System.out.println("대출자상환금	" + totalInfoList.get(i).getPaidDate() + "		" + totalInfoList.get(i).getTrAmt());
	    		
	    	} else if(totalInfoList.get(i).getAmtGbn().equals("Ive")) {
	    		tmtIve += totalInfoList.get(i).getTrAmt();
	    		System.out.println("투자자투자금	" + totalInfoList.get(i).getPaidDate() + "			" + totalInfoList.get(i).getTrAmt());
	    		
	    	} else if(totalInfoList.get(i).getAmtGbn().equals("Wve")) {
	    		tmtWve += totalInfoList.get(i).getTrAmt();
	    		System.out.println("투자자출금(타행)	" + totalInfoList.get(i).getPaidDate() + "				" + totalInfoList.get(i).getTrAmt());
	    		
	    	}
	    	
	    }
	        */
        }
        
        System.out.println("이상있는 검증값 : " + cntFalse + ", 문제없는 검증값 : " + cntTrue);
        
        // 총계와 전문호출한 값까지 해서 검증
//   		System.out.println("투자계좌총합 : " + investIpAmt + ", 대출계좌총합 : " + loanIpAmt + ", 투자자가지급받은금액합 : " + investJiAmt);
        
        // error msg => Handler dispatch failed;
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    // targetDate가 StartDate와 EndDate사이에 있으면 참, 아니면 거짓 리턴
    public static boolean getBetweenWeekAgoAndYesterday(String startStr, String endStr, String targetStr) {
    	try {
	    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	    	Date startDate = formatter.parse(startStr);
	    	Date endDate = formatter.parse(endStr);
	    	Date targetDate = formatter.parse(targetStr);
	    	
	    	long diff = endDate.getTime() - startDate.getTime();
	    	
	    	if(diff >= 0) {
	    		if (startDate.getTime() <= targetDate.getTime() && targetDate.getTime() <= endDate.getTime()) {
	    			return true;
	    		} else
	    			return false;
	    	}
	    	
    	} catch (Exception e) {
    		System.out.println(e.getMessage());
    	}
		return false;
    }
}
