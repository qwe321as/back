package com.crepass.restfulapi.one.service;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crepass.restfulapi.one.dao.RepaymentMapper;
import com.crepass.restfulapi.one.domain.OneOrderPrePayment;
import com.crepass.restfulapi.one.domain.OnePrePayment;
import com.crepass.restfulapi.one.domain.OnePrePaymentProvide;
import com.crepass.restfulapi.one.domain.OneRepaymentCheckCount;
import com.crepass.restfulapi.one.domain.OneRepaymentDataInfo;
import com.crepass.restfulapi.one.domain.OneRepaymentScheduleItem;
import com.crepass.restfulapi.one.domain.OneRepaymentScheduleLoanInfo;
import com.crepass.restfulapi.one.domain.OneRepaymentWithdrawInfo;
import com.crepass.restfulapi.one.domain.OneRestOverdueBalance;
import com.crepass.restfulapi.one.domain.OneUnpaidRepayment;
import com.crepass.restfulapi.v2.domain.PaymentScheduleItem;

@Service
public class RepaymentService {
    
    @Autowired
    RepaymentMapper repaymentMapper;
    
    public OneRepaymentDataInfo selectRepaymentDataInfo(String loanId) throws Exception {
    	return repaymentMapper.selectRepaymentDataInfo(loanId);
    }
    
    public List<OneRepaymentWithdrawInfo> selectRepaymentWithdrawInfo(String loanId) throws Exception {
    	return repaymentMapper.selectRepaymentWithdrawInfo(loanId);
    }
    
    public List<OneRepaymentScheduleLoanInfo> selectRepaymentScheduleLoanInfo(String loanId) throws Exception {
    	return repaymentMapper.selectRepaymentScheduleLoanInfo(loanId);
    }
    
    public List<OneRepaymentScheduleItem> selectRepaymentScheduleItem(String loanId, String today) throws Exception {
    	return repaymentMapper.selectRepaymentScheduleItem(loanId, today);
    }
    
    public List<OneRepaymentScheduleItem> selectRepaymentScheduleItem2(String loanId, String today, String count) throws Exception {
    	return repaymentMapper.selectRepaymentScheduleItem2(loanId, today, count);
    }
    
    public int selectOverdueCount(String loanId, String today) throws Exception {
    	return repaymentMapper.selectOverdueCount(loanId, today);
    }
    
    public int selectOverdueCount2(String loanId, String count) throws Exception {
    	return repaymentMapper.selectOverdueCount2(loanId, count);
    }
    
    public String selectOverdueBalance(String loanId) throws Exception {
    	return repaymentMapper.selectOverdueBalance(loanId);
    }
    
    public List<String> selectOverdueLoanInfo() throws Exception {
    	return repaymentMapper.selectOverdueLoanInfo();
    }
    
    public List<String> selectOverdueLoanInfo_ExceptGPM() throws Exception {
    	return repaymentMapper.selectOverdueLoanInfo_ExceptGPM();
    }
    
    public boolean updateRepaymentScheduleOverDue(String loanId, String count, String delqAmount, String delqState) throws Exception {
    	return repaymentMapper.updateRepaymentScheduleOverDue(loanId, count, delqAmount, delqState);
    }
    
    public boolean updatePaymentScheduleOverDue(String mid, String loanId, String count, String delqAmount, String tax, String taxLocal, String fee) throws Exception {
    	return repaymentMapper.updatePaymentScheduleOverDue(mid, loanId, count, delqAmount, tax, taxLocal, fee);
    }
    
    public OneRestOverdueBalance selectRestOverdueBalance(String loanId) throws Exception {
    	return repaymentMapper.selectRestOverdueBalance(loanId);
    }
    
    public String selectRecentRepaymentDate(String loanId) throws Exception {
    	return repaymentMapper.selectRecentRepaymentDate(loanId);
    }
    
    public boolean insertPrePayment(OnePrePayment onePrePayment) throws Exception {
    	return repaymentMapper.insertPrePayment(onePrePayment);
    }
    
    public boolean insertPrePaymentProvide(OnePrePaymentProvide onePrePaymentProvide) throws Exception {
    	return repaymentMapper.insertPrePaymentProvide(onePrePaymentProvide);
    }
    
    public boolean updatePaymentScheduleState(String loanId) throws Exception {
    	return repaymentMapper.updatePaymentScheduleState(loanId);
    }
    
    public String selectCheckOverdue(String loanId) throws Exception {
    	return repaymentMapper.selectCheckOverdue(loanId);
    }
    
    public boolean insertOrderPrePayment(OneOrderPrePayment oneOrderPrePayment) throws Exception {
    	return repaymentMapper.insertOrderPrePayment(oneOrderPrePayment);
    }
    
    public OneOrderPrePayment selectOrderPrePaymentInfo(String loanId, String mid) throws Exception {
    	return repaymentMapper.selectOrderPrePaymentInfo(loanId, mid);
    }
    
    public String selectIsOverDueState(String loanId, String count) throws Exception {
    	return repaymentMapper.selectIsOverDueState(loanId, count);
    }
    
    public boolean insertOrverDueHistory(String loanId, String count) throws Exception {
    	return repaymentMapper.insertOrverDueHistory(loanId, count);
    }
    
    public boolean updateOrverDueState(String loanId, String count) throws Exception {
    	return repaymentMapper.updateOrverDueState(loanId, count);
    }
    
    public List<OneRepaymentCheckCount> selectRepaymentCheckCount(String loanId, String today) throws Exception {
    	return repaymentMapper.selectRepaymentCheckCount(loanId, today);
    }
    
    public List<OneRepaymentCheckCount> selectRepaymentCheckCount2(String loanId, String today) throws Exception {
    	return repaymentMapper.selectRepaymentCheckCount2(loanId, today);
    }
    
    public List<OneRepaymentCheckCount> selectRepaymentCheckCount3(String loanId) throws Exception {
    	return repaymentMapper.selectRepaymentCheckCount3(loanId);
    }
    
    public String selectLaonExecuteDate(String loanId) throws Exception {
    	return repaymentMapper.selectLaonExecuteDate(loanId);
    }
    
    public String selectLoanCountByPayStatus(String loanId, String count) throws Exception {
    	return repaymentMapper.selectLoanCountByPayStatus(loanId, count);
    }
    
    public String selectOverduePayAmountSum(String loanId, String payDate) throws Exception {
    	return repaymentMapper.selectOverduePayAmountSum(loanId, payDate);
    }
    
    public String selectOverdueCurrentDate(String loanId, String count) throws Exception {
    	return repaymentMapper.selectOverdueCurrentDate(loanId, count);
    }
    
    public boolean insertOrverDueDetailHistory(String loanId, String count, String addtionalRate, String overdueAmount, String overdueSate) throws Exception {
    	return repaymentMapper.insertOrverDueDetailHistory(loanId, count, addtionalRate, overdueAmount, overdueSate);
    }
    
    public PaymentScheduleItem selectPaymentScheduleItem(String mid, String loanId, String count) throws Exception {
    	return repaymentMapper.selectPaymentScheduleItem(mid, loanId, count);
    }
    
    public PaymentScheduleItem selectPaymentScheduleItemTest(String mid, String loanId, String count) throws Exception {
    	return repaymentMapper.selectPaymentScheduleItemTest(mid, loanId, count);
    }
    
    public boolean insertOrverDueInvestHistory(String loanId, String mid, String count, String overdue, String tax, String taxLocal, String fee, String overdueState) throws Exception {
    	return repaymentMapper.insertOrverDueInvestHistory(loanId, mid, count, overdue, tax, taxLocal, fee, overdueState);
    }

	public String selectOneMid(String loanId) throws Exception {
		return repaymentMapper.selectOneMid(loanId);
	}

	public long selectRepaymentTotalRefundAmt(String oneMid, String loanId) throws Exception {
		return repaymentMapper.selectRepaymentTotalRefundAmt(oneMid, loanId);
	}

	public List<OneUnpaidRepayment> selectUnpaidRepayment(String string, String today) throws Exception {
		return repaymentMapper.selectUnpaidRepayment(string, today);
	}

	public double selectPaidTillTodayRepayment(String loanId, String payDate) throws Exception {
		return repaymentMapper.selectPaidTillTodayRepayment(loanId, payDate);
	}

	public double selectGihanRepayment(String loanId) throws Exception {
		return repaymentMapper.selectGihanRepayment(loanId);
	}

	public String selectRecentCountRepaymentDate(String loanId, String prepayDate) throws Exception {
		return repaymentMapper.selectRecentCountRepaymentDate(loanId, prepayDate);
	}

	public String selectRestBalance(String loanId, String count) throws Exception {
		return repaymentMapper.selectRestBalance(loanId, count);
	}

	public boolean updatePaymentScheduleTax(String mid, String loanId, String payCount, String tax_real, String tax_local_real) throws Exception {
		return repaymentMapper.updatePaymentScheduleTax(mid, loanId, payCount, tax_real, tax_local_real);
	}

	public long selectRepaymentPaidAmt(String loanId) throws Exception {
		return repaymentMapper.selectRepaymentPaidAmt(loanId);
	}

	public long selectHadToBePaidAmt(String loanId) throws Exception {
		return repaymentMapper.selectHadToBePaidAmt(loanId);
		}


	
//    public boolean insertPrePayment(OnePrePayment onePrePayment) throws Exception {
//    	return repaymentMapper.insertPrePayment(onePrePayment);
//    }
//    
//    public boolean insertPrePaymentProvide(OnePrePaymentProvide onePrePaymentProvide) throws Exception {
//    	return repaymentMapper.insertPrePaymentProvide(onePrePaymentProvide);
//    }

    
}
