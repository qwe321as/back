package com.crepass.restfulapi.creone.controller;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.common.domain.ResponseResult;
import com.crepass.restfulapi.cre.domain.CreLoanAgreed2;
import com.crepass.restfulapi.cre.service.AgreeService;
import com.crepass.restfulapi.cre.service.CreMemberService;
import com.crepass.restfulapi.one.domain.MariMember;
import com.crepass.restfulapi.one.domain.OneCertify;
import com.crepass.restfulapi.one.service.LoanService;
import com.crepass.restfulapi.one.service.OneMemberService;
import com.crepass.restfulapi.v2.domain.Agreement;
import com.crepass.restfulapi.v2.domain.InvestBundleItem;
import com.google.gson.Gson;

import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@RequestMapping(path = "/api", method = RequestMethod.POST)
public class ContractAgreeController {
    
    @Autowired
    private AgreeService agreeService;
    
    @Autowired
    private LoanService loanService;
    
    @Autowired
    private OneMemberService oneMemberService;
    
    @Autowired
	private CreMemberService creMemberService;
    
    @Autowired
    private CommonUtil commonUtil;
    
    @Autowired(required=true)
	private HttpServletRequest request;
    
    @ApiOperation(value = "약정서 동의정보저장")
    @RequestMapping("/agree/contract/add")
    public ResponseEntity<ResponseResult> agreeContractAdd(@RequestBody String requestString) throws Exception {
    	
    	String mapping_url = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        commonUtil.sendRequestLogging(mapping_url, requestString);
    	
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject request = (JSONObject) jsonMember.get("request");
        
        String loanId = request.getString("loanId");
        String mid = request.getString("mid");
        
        CreLoanAgreed2 creLoanAgreed2 = new CreLoanAgreed2();
        creLoanAgreed2.setMid(mid);
        creLoanAgreed2.setLoanId(loanId);
        creLoanAgreed2.setCustomerNotice(request.getString("customerNotice"));
        creLoanAgreed2.setLoanContract(request.getString("loanContract"));
        creLoanAgreed2.setBasicContract(request.getString("basicContract"));
        creLoanAgreed2.setMainContract(request.getString("mainContract"));
        creLoanAgreed2.setContractCondition(request.getString("contractCondition"));
        creLoanAgreed2.setDelayInterest(request.getString("delayInterest"));
        creLoanAgreed2.setStampFee(request.getString("stampFee"));
        creLoanAgreed2.setIntermFee(request.getString("intermFee"));
        creLoanAgreed2.setPlatformFee(request.getString("platformFee"));
        creLoanAgreed2.setBrokerFee(request.getString("brokerFee"));
        creLoanAgreed2.setTermsExp(request.getString("termsExp"));
        creLoanAgreed2.setCinfProvide(request.getString("cinfProvide"));
        
        agreeService.insertCreLoanAgreed2(creLoanAgreed2);
        
        boolean isLoanContractFlag = loanService.updateLoanContractFlag(loanId, "A");
        
        MariMember mariMember = oneMemberService.selectMemberById(mid);
        OneCertify oneCertify = new OneCertify();
        oneCertify.setMno(mariMember.getMno());
        oneCertify.setCertifyType("H");
        oneCertify.setCertifyResult(request.getString("certifyResult").toString());
        boolean isOneCertify = oneMemberService.insertOneCertify(oneCertify);
        
        if(!isLoanContractFlag || !isOneCertify) {
            response.setState(476);
            response.setMessage("약정서 신청에 오류가 발생했습니다. 고객센터에 문의해주세요.");
        }
//        response.setResult(creAgree);
        commonUtil.sendResultLogging(mapping_url, new JSONObject(response).toString());
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    

    @ApiOperation(value = "약정서 동의정보저장")
    @RequestMapping("/agree2/contract/add")
    public ResponseEntity<ResponseResult> agreeContractAdd2(@RequestBody String requestString) throws Exception {
        JSONObject jsonMember = new JSONObject(requestString);
        
        ResponseResult response = new ResponseResult();        
        response.setState(200);
        response.setMessage("정상적으로 처리하였습니다.");
        
        JSONObject request = (JSONObject) jsonMember.get("request");
        
        String loanId = request.getString("loanId");
        String mid = request.getString("mid");
        
        Agreement agreement = new Gson().fromJson(request.getJSONObject("agreement").toString(), Agreement.class);
        boolean isLoanContractFlag = loanService.updateLoanContractFlag(loanId, "A");
        
        if(!isLoanContractFlag) {
            response.setState(476);
            response.setMessage("약정서 신청에 오류가 발생했습니다. 고객센터에 문의해주세요.");
        } else {
        	String custId = oneMemberService.selectCustID(mid);
            creMemberService.addCreAgreeMember3(agreement, custId);
        }
        
        return new ResponseEntity<ResponseResult>(response, HttpStatus.OK);  
    }
    
    
    
    
    
}
