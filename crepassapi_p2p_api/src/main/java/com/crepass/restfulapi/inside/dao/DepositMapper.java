package com.crepass.restfulapi.inside.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.crepass.restfulapi.inside.domain.InsideDeposit;
import com.crepass.restfulapi.inside.domain.InsideDepositInfo;
import com.crepass.restfulapi.inside.domain.InsideIPJIInfo;
import com.crepass.restfulapi.inside.domain.OneInsideDepositCancel;
import com.crepass.restfulapi.inside.domain.OneRepaymentDepositInfo;
import com.crepass.restfulapi.inside.domain.OneRepaymentException;
import com.crepass.restfulapi.inside.domain.OneRepaymentInfo;
import com.crepass.restfulapi.one.domain.OneLoanVirtualAccntInfo;

@Component
public interface DepositMapper {
    
	public List<InsideDepositInfo> selectDepositInfo(InsideDeposit insideDeposit) throws Exception;
	
	public List<InsideDepositInfo> selectDepositInfo2() throws Exception;

	public List<InsideDepositInfo> selectDepositInfo3(String erpTransDt) throws Exception;
	
	public List<String> selectLoanPayment(String erpTransD) throws Exception;
	
	public List<OneRepaymentInfo> selectRepaymentInfo(String loanId, String tranDate) throws Exception;
	
	public List<InsideDepositInfo> selectRepaymentDepositList(String loanAccnt) throws Exception;
	
	public String selectRepaymentDuplicateInfo(String loanId, String custId, String trAmt, String taxAmt, String fee) throws Exception;
	
	public long selectRepaymentTotalDepositAmt(String loanAccnt) throws Exception;
	
	public List<OneRepaymentDepositInfo> selectRepaymentListDepositAmt(String loanAccnt) throws Exception;
	
	public List<OneRepaymentException> selectRepaymentException() throws Exception;
	
	public List<OneRepaymentException> selectRepaymentSuccess() throws Exception;
	
	public String selectTotalDepositPay(String custId) throws Exception;
	
	public List<OneInsideDepositCancel> selectDepositCancel(String recentDate) throws Exception;

	public List<InsideIPJIInfo> selectInsideIPJIInfo(String custId) throws Exception;

	public List<InsideIPJIInfo> selectP2pInvestInfo(String custId) throws Exception;

	public double selectPaidTillTodayInside(String loanVirtualAccnt) throws Exception;

	public int selectPrincipalRequestInfo(String today, String custId, String sumPay, String loanId) throws Exception;

	public long selectRepaymentTotalDepositAmt_exceptToday(String loanAccnt) throws Exception;

	public long selectInvestTotalDepositAmt(String custId) throws Exception;

}
