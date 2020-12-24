package com.crepass.restfulapi.one.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import com.crepass.restfulapi.inside.domain.InsideDeposit;
import com.crepass.restfulapi.inside.domain.InsideDepositInfo;
import com.crepass.restfulapi.inside.domain.InsideDepositInfo2;
import com.crepass.restfulapi.inside.domain.OneInsideDepositCancel;
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

@Component
public interface ScheMapper {
    
	public List<InsideDeposit> selectDepositById() throws Exception;
    
	public boolean insertDeposit(InsideDepositInfo insideDepositInfo) throws Exception;
	
	public boolean insertDeposit2(InsideDepositInfo insideDepositInfo) throws Exception;
	
	public List<InsideDepositInfo2> selectDepositScheduleById() throws Exception;
	
	public String selectCustUserId(String custId) throws Exception;
	
	public boolean insertDepositHistory(String mid, String trxType, String trxAmt, String typeFlag, String loanId, String rCount) throws Exception;
	
	public boolean updateDepositSchedule(String id, String batchFlag) throws Exception;

	public List<OneRepayment> selectRepaymentList() throws Exception;
	
	public List<OneHolidayCalendar> selectHolidayCalendar() throws Exception;
	
	public List<OneRepayScheduleInfo> selectRepayScheduleInfo() throws Exception;
	
	public boolean insertRepaySchedule(OneRepayScheduleAdd oneRepayScheduleAdd) throws Exception;
	
	public boolean updateRepayScheduleState(String mid) throws Exception;
	
	public List<OnePaymentInvestSchedule> selectPaymentInvestSchedule(String loanId) throws Exception;
	
	public boolean insertPaymentSchedule(OnePaymentSchedule onePaymentSchedule) throws Exception;

	public List<OnePaymentSchedule> selectPaymentScheduleStart() throws Exception;
	
	public String selectPrincipalNum(String mid, String loanId) throws Exception;
	
	public boolean updateOrderSchedule(String oid) throws Exception;
	
	public boolean updateOrderScheduleFinish(String mid, String loanId, String count) throws Exception;
	
	public boolean updatePaymentSchedule(String pid) throws Exception;

	public boolean deleteCertifyWebDumpAll() throws Exception;
	
	public List<OneInvestAutoDivision> selectInvestAutoDivision() throws Exception;
	
	public List<String> selectInvestAutoPossible(String mid, String aid, String univName) throws Exception;
	
	public String selectInvestAutoTotalPayment(String mid) throws Exception;
	
	public String selectLoanIdByMid(String loanId) throws Exception;
	
	public List<OneWithdraw> selectRepayWithdraw() throws Exception;
	
	public boolean updateRepayWithdrawState(String tid) throws Exception;
	
	public String selectPayScheduleWithdraw(String loanId, String custId) throws Exception;
	
	public List<OneLoanAccntReturn> selectLoanAccntReturn(String currentDt) throws Exception;
	
	public boolean updateAccntITW(String accnt) throws Exception;
	
	public boolean updateInvestAccntReturn(String custId) throws Exception;
	
	public List<OnePrePaymentSchedule> selectPrePaymentScheduleStart() throws Exception;
	
	public boolean updatePrePaymentSchedule(String pid) throws Exception;
	
	public String selectRepayCount(String loanId) throws Exception;
	
	public String selectRepayCount2(String loanId, String mid, String payAmount) throws Exception;
	
	public String selectRepayCount3(String loanId, String mid) throws Exception;
	
	public List<OneStartInvestUserInfo> selectStartInvestUserInfo(String loanId) throws Exception;
	
	public String selectLoanStateOverduePayment() throws Exception;
	
	public String selectLoanStateDefaultPayment() throws Exception;
	
	public String selectLoanStateTotalPayment() throws Exception;
	
	public String selectLoanStateTotalRepayment() throws Exception;
	
	public String selectLoanStateTotalBalance() throws Exception;
	
	public boolean insertLoanStatus(String totLoanAmt, String totRepayAmt, String totBalanceAmt, String avgProfRate, String overdueRate, String defaultRate) throws Exception;
	
	public List<String> selectOverdueByLoanId() throws Exception;
	
	public String selectTotalRepayment(String loanId) throws Exception;
	
	public List<OneOverdueRepaymentItem> selectOverdueRepaymentList(String loanId, String today) throws Exception;
	
	public String selectOverdueByValues(String loanId) throws Exception;
	
	public boolean insertOverduePayment(String loanId, String overdue, String repayment) throws Exception;
	
	public String selectTotalLoanPay() throws Exception;
	
	public String selectLoanStateTotalPrincipal() throws Exception;
	
	public String selectDeposiRecentCancelDate() throws Exception;
	
	public boolean insertDepositCancelHistory(OneInsideDepositCancel oneInsideDepositCancel) throws Exception;
	
	public String selectCheckDepositCancelData(String trOrgDate, String trOrgSeq, String trNb) throws Exception;
	
	public OneRateInfo selectRateInfo(String level, String serviceType, String repayType) throws Exception;
	
	public String selectInvestRate(@Param("loanId") String loanId) throws Exception;
	
	public boolean updateRepayComplete() throws Exception;

	public String selectLoanStateTotalPayment2() throws Exception;

	public List<InvestBundleItem2> selectInvestBundle() throws Exception;

	public boolean updateInvestStackState(@Param("loanId") String loanId, @Param("mid") String mid, @Param("isBatch") String isBatch, @Param("msg") String msg) throws Exception;

//	public boolean insertRepaySchedule(OneRepayScheduleAdd oneRepayScheduleAdd) throws Exception;
//
//	public boolean insertPaymentSchedule(OnePaymentSchedule onePaymentSchedule) throws Exception;
//
//	public List<OneRepayScheduleInfo> selectRepayScheduleInfo() throws Exception;

	public List<OneOverdueNumberOfCount> selectOverdueNumberOfCount(@Param("loanId") String loanId, @Param("mid") String mid) throws Exception;

	public long selectPreFeeInfo(@Param("loanId") String loanId, @Param("mid") String mid, @Param("minCount")String minCount) throws Exception;

	public List<String> selectAllPaymentInfo() throws Exception;

	public List<OnePaymentNewSchedule> selectPaymentInfo(String loanId, String mid) throws Exception;

	public long selectNewFeeInfo(String loanId, String mid, String repayCount) throws Exception;

	public boolean updateNewFeeInfo(OnePaymentNewInfo onePaymentNewInfo) throws Exception;

	public List<OnePaymentFeeInfo> selectAllPaymentFeeInfo() throws Exception;

	public List<OneRepayScheduleInfo> selectRepayScheduleInfoTest(String mid) throws Exception;
	
	public List<OneRepayScheduleInfo> selectRepayScheduleInfoTestAll() throws Exception;

	public int updateRepayWithdrawState2(String tid) throws Exception;

	public String selectLoanSoldInformation() throws Exception;

	public List<OneInvestAccountInform> selectInvestAccntInform() throws Exception;

	public long selectForAWeekSumDW(String mid) throws Exception;

	public List<String> selectAllInvestorsList() throws Exception;

	public String selectLastCountRepaymentDate(String loanId, String repayCount) throws Exception;

	public int selectNumberOfOrderCount(String loanId, String repayCount) throws Exception;

	public int selectNumberOfPaymentCount(String loanId, String repayCount) throws Exception;
	
	public String selectInvestListCheckTime(String mid) throws Exception;

	public String selectInvestTranCheckTime(String mid) throws Exception;

	public String selectInvestCondCheckTime(String mid) throws Exception;

	public int insertViewStatus(String mid, String currentTime) throws Exception;

}
