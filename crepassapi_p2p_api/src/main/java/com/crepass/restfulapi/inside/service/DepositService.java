package com.crepass.restfulapi.inside.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crepass.restfulapi.inside.dao.DepositMapper;
import com.crepass.restfulapi.inside.domain.InsideDeposit;
import com.crepass.restfulapi.inside.domain.InsideDepositInfo;
import com.crepass.restfulapi.inside.domain.InsideIPJIInfo;
import com.crepass.restfulapi.inside.domain.OneInsideDepositCancel;
import com.crepass.restfulapi.inside.domain.OneRepaymentDepositInfo;
import com.crepass.restfulapi.inside.domain.OneRepaymentException;
import com.crepass.restfulapi.inside.domain.OneRepaymentInfo;
import com.crepass.restfulapi.one.domain.OneLoanVirtualAccntInfo;

@Service
public class DepositService {

    @Autowired
    DepositMapper depositMapper;
    
    public List<InsideDepositInfo> selectDepositInfo(InsideDeposit insideDeposit) throws Exception {
    	return depositMapper.selectDepositInfo(insideDeposit);
    }
    
    public List<InsideDepositInfo> selectDepositInfo2() throws Exception {
    	return depositMapper.selectDepositInfo2();
    }
    
    public List<InsideDepositInfo> selectDepositInfo3(String erpTransDt) throws Exception {
    	return depositMapper.selectDepositInfo3(erpTransDt);
    }
    
    public List<String> selectLoanPayment(String erpTransD) throws Exception {
    	return depositMapper.selectLoanPayment(erpTransD);
    }
    
    public List<OneRepaymentInfo> selectRepaymentInfo(String loanId, String tranDate) throws Exception {
    	return depositMapper.selectRepaymentInfo(loanId, tranDate);
    }
    
    public List<InsideDepositInfo> selectRepaymentDepositList(String loanAccnt) throws Exception {
    	return depositMapper.selectRepaymentDepositList(loanAccnt);
    }
    
    public String selectRepaymentDuplicateInfo(String loanId, String custId, String trAmt, String taxAmt, String fee) throws Exception {
    	return depositMapper.selectRepaymentDuplicateInfo(loanId, custId, trAmt, taxAmt, fee);
    }
    
    public long selectRepaymentTotalDepositAmt(String loanAccnt) throws Exception {
    	return depositMapper.selectRepaymentTotalDepositAmt(loanAccnt);
    }
    
    public List<OneRepaymentDepositInfo> selectRepaymentListDepositAmt(String loanAccnt) throws Exception {
    	return depositMapper.selectRepaymentListDepositAmt(loanAccnt);
    }
    
    public List<OneRepaymentException> selectRepaymentException() throws Exception {
    	return depositMapper.selectRepaymentException();
    }
     
    public List<OneRepaymentException> selectRepaymentSuccess() throws Exception {
    	return depositMapper.selectRepaymentSuccess();
    }
    
    public String selectTotalDepositPay(String custId) throws Exception {
    	return depositMapper.selectTotalDepositPay(custId);
    }
    
    public List<OneInsideDepositCancel> selectDepositCancel(String recentDate) throws Exception {
    	return depositMapper.selectDepositCancel(recentDate);
    }

	public List<InsideIPJIInfo> selectInsideIPJIInfo(String custId) throws Exception {
		return depositMapper.selectInsideIPJIInfo(custId);
	}

	public List<InsideIPJIInfo> selectP2pInvestInfo(String custId) throws Exception {
		return depositMapper.selectP2pInvestInfo(custId);
	}

	public double selectPaidTillTodayInside(String loanVirtualAccnt) throws Exception {
		return depositMapper.selectPaidTillTodayInside(loanVirtualAccnt);
	}

	public int selectPrincipalRequestInfo(String today, String custId, String sumPay, String loanId) throws Exception {
		return depositMapper.selectPrincipalRequestInfo(today, custId, sumPay, loanId);
	}
    
    public long selectRepaymentTotalDepositAmt_exceptToday(String loanAccnt) throws Exception {
    	return depositMapper.selectRepaymentTotalDepositAmt_exceptToday(loanAccnt);
    }

	public long selectInvestTotalDepositAmt(String custId) throws Exception {
		return depositMapper.selectInvestTotalDepositAmt(custId);
	}


}
