package com.crepass.restfulapi.one.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crepass.restfulapi.inside.domain.InsideDeposit;
import com.crepass.restfulapi.inside.domain.InsideDepositInfo;
import com.crepass.restfulapi.inside.domain.InsideDepositInfo2;
import com.crepass.restfulapi.inside.domain.OneInsideDepositCancel;
import com.crepass.restfulapi.one.dao.ScheMapper;
import com.crepass.restfulapi.one.domain.OneHolidayCalendar;
import com.crepass.restfulapi.one.domain.OneInvestAccountInform;
import com.crepass.restfulapi.one.domain.OneInvestAutoDivision;
import com.crepass.restfulapi.one.domain.OneLoanAccntReturn;
import com.crepass.restfulapi.one.domain.OneOverdueNumberOfCount;
import com.crepass.restfulapi.one.domain.OneOverdueRepaymentItem;
import com.crepass.restfulapi.one.domain.OnePaymentFeeInfo;
import com.crepass.restfulapi.one.domain.OnePaymentInvestSchedule;
import com.crepass.restfulapi.one.domain.OnePaymentNewInfo;
import com.crepass.restfulapi.one.domain.OnePaymentNewSchedule;
import com.crepass.restfulapi.one.domain.OnePaymentSchedule;
import com.crepass.restfulapi.one.domain.OnePrePaymentSchedule;
import com.crepass.restfulapi.one.domain.OneRateInfo;
import com.crepass.restfulapi.one.domain.OneRepayScheduleAdd;
import com.crepass.restfulapi.one.domain.OneRepayScheduleInfo;
import com.crepass.restfulapi.one.domain.OneRepayment;
import com.crepass.restfulapi.one.domain.OneStartInvestUserInfo;
import com.crepass.restfulapi.one.domain.OneWithdraw;
import com.crepass.restfulapi.v2.domain.InvestBundleItem2;

@Service
public class ScheService {

    @Autowired
    ScheMapper scheMapper;
    
    public List<InsideDeposit> selectDepositById() throws Exception {
    	return scheMapper.selectDepositById();
    }
    
    public boolean insertDeposit(InsideDepositInfo insideDepositInfo) throws Exception {
    	return scheMapper.insertDeposit(insideDepositInfo);
    }
    
    public boolean insertDeposit2(InsideDepositInfo insideDepositInfo) throws Exception {
    	return scheMapper.insertDeposit2(insideDepositInfo);
    }
    
    public List<InsideDepositInfo2> selectDepositScheduleById() throws Exception {
    	return scheMapper.selectDepositScheduleById();
    }
    
    public String selectCustUserId(String custId) throws Exception {
    	return scheMapper.selectCustUserId(custId);
    }
    
    public boolean insertDepositHistory(String mid, String trxType, String trxAmt, String typeFlag, String loanId, String rCount) throws Exception {
    	return scheMapper.insertDepositHistory(mid, trxType, trxAmt, typeFlag, loanId, rCount);
    }
    
    public boolean updateDepositSchedule(String id, String batchFlag) throws Exception {
    	return scheMapper.updateDepositSchedule(id, batchFlag);
    }
    
    public List<OneRepayment> selectRepaymentList() throws Exception {
    	return scheMapper.selectRepaymentList();
    }
    
    public List<OneHolidayCalendar> selectHolidayCalendar() throws Exception {
    	return scheMapper.selectHolidayCalendar();
    }
    
    public List<OneRepayScheduleInfo> selectRepayScheduleInfo() throws Exception {
    	return scheMapper.selectRepayScheduleInfo();
    }
    
    public boolean insertRepaySchedule(OneRepayScheduleAdd oneRepayScheduleAdd) throws Exception {
    	return scheMapper.insertRepaySchedule(oneRepayScheduleAdd);
    }
    
    public boolean updateRepayScheduleState(String mid) throws Exception {
    	return scheMapper.updateRepayScheduleState(mid);
    }
    
    public List<OnePaymentInvestSchedule> selectPaymentInvestSchedule(String loanId) throws Exception {
    	return scheMapper.selectPaymentInvestSchedule(loanId);
    }
    
    public boolean insertPaymentSchedule(OnePaymentSchedule onePaymentSchedule) throws Exception {
    	return scheMapper.insertPaymentSchedule(onePaymentSchedule);
    }
    
    public List<OnePaymentSchedule> selectPaymentScheduleStart() throws Exception {
    	return scheMapper.selectPaymentScheduleStart();
    }
    
    public String selectPrincipalNum(String mid, String loanId) throws Exception {
    	return scheMapper.selectPrincipalNum(mid, loanId);
    }
    
    public boolean updateOrderSchedule(String oid) throws Exception {
    	return scheMapper.updateOrderSchedule(oid);
    }
    
    public boolean updateOrderScheduleFinish(String mid, String loanId, String count) throws Exception {
    	return scheMapper.updateOrderScheduleFinish(mid, loanId, count);
    }
    
    public boolean updatePaymentSchedule(String pid) throws Exception {
    	return scheMapper.updatePaymentSchedule(pid);
    }
    
    public boolean deleteCertifyWebDumpAll() throws Exception {
    	return scheMapper.deleteCertifyWebDumpAll();
    }
    
    public List<OneInvestAutoDivision> selectInvestAutoDivision() throws Exception {
    	return scheMapper.selectInvestAutoDivision();
    }
    
    public List<String> selectInvestAutoPossible(String mid, String aid, String univName) throws Exception {
    	return scheMapper.selectInvestAutoPossible(mid, aid, univName);
    }
    
    public String selectInvestAutoTotalPayment(String mid) throws Exception {
    	return scheMapper.selectInvestAutoTotalPayment(mid);
    }
    
    public String selectLoanIdByMid(String loanId) throws Exception {
    	return scheMapper.selectLoanIdByMid(loanId);
    }
    
    public List<OneWithdraw> selectRepayWithdraw() throws Exception {
    	return scheMapper.selectRepayWithdraw();
    }
    
    public boolean updateRepayWithdrawState(String tid) throws Exception {
    	return scheMapper.updateRepayWithdrawState(tid);
    }
    
    public String selectPayScheduleWithdraw(String loanId, String custId) throws Exception {
    	return scheMapper.selectPayScheduleWithdraw(loanId, custId);
    }
    
    public List<OneLoanAccntReturn> selectLoanAccntReturn(String currentDt) throws Exception {
    	return scheMapper.selectLoanAccntReturn(currentDt);
    }
    
    public boolean updateAccntITW(String accnt) throws Exception {
    	return scheMapper.updateAccntITW(accnt);
    }
    
    public boolean updateInvestAccntReturn(String custId) throws Exception {
    	return scheMapper.updateInvestAccntReturn(custId);
    }
    
    public List<OnePrePaymentSchedule> selectPrePaymentScheduleStart() throws Exception {
    	return scheMapper.selectPrePaymentScheduleStart();
    }
    
    public boolean updatePrePaymentSchedule(String pid) throws Exception {
    	return scheMapper.updatePrePaymentSchedule(pid);
    }
    
    public String selectRepayCount(String loanId) throws Exception {
    	return scheMapper.selectRepayCount(loanId);
    }
    
    public String selectRepayCount2(String loanId, String mid, String payAmount) throws Exception {
    	return scheMapper.selectRepayCount2(loanId, mid, payAmount);
    }
    
    public String selectRepayCount3(String loanId, String mid) throws Exception {
    	return scheMapper.selectRepayCount3(loanId, mid);
    }
    
    public List<OneStartInvestUserInfo> selectStartInvestUserInfo(String loanId) throws Exception {
    	return scheMapper.selectStartInvestUserInfo(loanId);
    }
    
    public String selectLoanStateOverduePayment() throws Exception {
    	return scheMapper.selectLoanStateOverduePayment();
    }
	
	public String selectLoanStateDefaultPayment() throws Exception {
		return scheMapper.selectLoanStateDefaultPayment();
	}
	
	public String selectLoanStateTotalPayment() throws Exception {
		return scheMapper.selectLoanStateTotalPayment();
	}
	
	public String selectLoanStateTotalRepayment() throws Exception {
		return scheMapper.selectLoanStateTotalRepayment();
	}
	
	public String selectLoanStateTotalBalance() throws Exception {
		return scheMapper.selectLoanStateTotalBalance();
	}
	
	public boolean insertLoanStatus(String totLoanAmt, String totRepayAmt, String totBalanceAmt, String avgProfRate, String overdueRate, String defaultRate) throws Exception {
		return scheMapper.insertLoanStatus(totLoanAmt, totRepayAmt, totBalanceAmt, avgProfRate, overdueRate, defaultRate);
	}
	
	public List<String> selectOverdueByLoanId() throws Exception {
    	return scheMapper.selectOverdueByLoanId();
    }
	
	public String selectTotalRepayment(String loanId) throws Exception {
		return scheMapper.selectTotalRepayment(loanId);
	}
	
	public List<OneOverdueRepaymentItem> selectOverdueRepaymentList(String loanId, String today) throws Exception {
		return scheMapper.selectOverdueRepaymentList(loanId, today);
	}
	
	public String selectOverdueByValues(String loanId) throws Exception {
		return scheMapper.selectOverdueByValues(loanId);
	}
	
	public boolean insertOverduePayment(String loanId, String overdue, String repayment) throws Exception {
		return scheMapper.insertOverduePayment(loanId, overdue, repayment);
	}
	
	public String selectTotalLoanPay() throws Exception {
		return scheMapper.selectTotalLoanPay();
	}
	
	public String selectLoanStateTotalPrincipal() throws Exception {
		return scheMapper.selectLoanStateTotalPrincipal();
	}
	
	public String selectDeposiRecentCancelDate() throws Exception {
    	return scheMapper.selectDeposiRecentCancelDate();
    }
    
    public boolean insertDepositCancelHistory(OneInsideDepositCancel oneInsideDepositCancel) throws Exception {
    	return scheMapper.insertDepositCancelHistory(oneInsideDepositCancel);
    }
    
    public String selectCheckDepositCancelData(String trOrgDate, String trOrgSeq, String trNb) throws Exception {
    	return scheMapper.selectCheckDepositCancelData(trOrgDate, trOrgSeq, trNb);
    }
    
    public OneRateInfo selectRateInfo(String level, String serviceType, String repayType) throws Exception {
    	return scheMapper.selectRateInfo(level, serviceType, repayType);
    }
    
    public String selectInvestRate(String loanId) throws Exception {
    	return scheMapper.selectInvestRate(loanId);
    }
    
    public boolean updateRepayComplete() throws Exception {
    	return scheMapper.updateRepayComplete();
    }

	public String selectLoanStateTotalPayment2() throws Exception {
		return scheMapper.selectLoanStateTotalPayment2();
	}

    public List<InvestBundleItem2> selectInvestBundle() throws Exception {
    	return scheMapper.selectInvestBundle();
    }
    
    public boolean updateInvestStackState(String loanId, String mid, String isBatch, String msg) throws Exception {
    	return scheMapper.updateInvestStackState(loanId, mid, isBatch, msg);
    }

//	public boolean insertRepaySchedule(OneRepayScheduleAdd oneRepayScheduleAdd) throws Exception {
//		return scheMapper.insertRepaySchedule(oneRepayScheduleAdd);
//	}
//
//	public boolean insertPaymentSchedule(OnePaymentSchedule onePaymentSchedule) throws Exception {
//		return scheMapper.insertPaymentSchedule(onePaymentSchedule);
//	}
//
//	public List<OneRepayScheduleInfo> selectRepayScheduleInfo() throws Exception {
//		return scheMapper.selectRepayScheduleInfo();
//	}

	public long selectPreFeeInfo(String loanId, String mid, String minCount) throws Exception {
		return scheMapper.selectPreFeeInfo(loanId, mid, minCount);
	}

	public List<OneOverdueNumberOfCount> selectOverdueNumberOfCount(String loanId, String mid) throws Exception {
		return scheMapper.selectOverdueNumberOfCount(loanId, mid);
	}

	public List<String> selectAllPaymentInfo() throws Exception {
		return scheMapper.selectAllPaymentInfo();
	}

	public List<OnePaymentNewSchedule> selectPaymentInfo(String loanId, String mid) throws Exception {
		return scheMapper.selectPaymentInfo(loanId, mid);
	}

	public long selectNewFeeInfo(String loanId, String mid, String repayCount) throws Exception {
		return scheMapper.selectNewFeeInfo(loanId, mid, repayCount);
	}

	public boolean updateNewFeeInfo(OnePaymentNewInfo onePaymentNewInfo) throws Exception {
		return scheMapper.updateNewFeeInfo(onePaymentNewInfo);
	}

	public List<OnePaymentFeeInfo> selectAllPaymentFeeInfo() throws Exception {
		return scheMapper.selectAllPaymentFeeInfo();
	}

	public List<OneRepayScheduleInfo> selectRepayScheduleInfoTest(String mid) throws Exception {
		return scheMapper.selectRepayScheduleInfoTest(mid);
	}

	public List<OneRepayScheduleInfo> selectRepayScheduleInfoTestAll() throws Exception {
		return scheMapper.selectRepayScheduleInfoTestAll();
	}

	public int updateRepayWithdrawState2(String tid) throws Exception {
		return scheMapper.updateRepayWithdrawState2(tid);
	}

	public String selectLoanSoldInformation() throws Exception {
		return scheMapper.selectLoanSoldInformation();
	}

	public List<OneInvestAccountInform> selectInvestAccntInform() throws Exception {
		return scheMapper.selectInvestAccntInform();
	}

	public long selectForAWeekSumDW(String mid) throws Exception {
		return scheMapper.selectForAWeekSumDW(mid);
	}

	public List<String> selectAllInvestorsList() throws Exception {
		return scheMapper.selectAllInvestorsList();
	}

	public String selectLastCountRepaymentDate(String loanId, String repayCount) throws Exception {
		return scheMapper.selectLastCountRepaymentDate(loanId, repayCount);
	}

	public int selectNumberOfOrderCount(String loanId, String repayCount) throws Exception {
		return scheMapper.selectNumberOfOrderCount(loanId, repayCount);
	}

	public int selectNumberOfPaymentCount(String loanId, String repayCount) throws Exception {
		return scheMapper.selectNumberOfPaymentCount(loanId, repayCount);
	}
	
	public String selectInvestListCheckTime(String mid) throws Exception {
		return scheMapper.selectInvestListCheckTime(mid);
	}

	public String selectInvestTranCheckTime(String mid) throws Exception {
		return scheMapper.selectInvestTranCheckTime(mid);
	}

	public String selectInvestCondCheckTime(String mid) throws Exception {
		return scheMapper.selectInvestCondCheckTime(mid);
	}
	
	public int insertViewStatus(String mid, String currentTime) throws Exception {
		return scheMapper.insertViewStatus(mid, currentTime); 
	}

}
