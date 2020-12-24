package com.crepass.restfulapi.one.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

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

public interface RepaymentMapper {
    
    public OneRepaymentDataInfo selectRepaymentDataInfo(String loanId) throws Exception;
    
    public List<OneRepaymentWithdrawInfo> selectRepaymentWithdrawInfo(String loanId) throws Exception;
    
    public List<OneRepaymentScheduleLoanInfo> selectRepaymentScheduleLoanInfo(String loanId) throws Exception;
    
    public List<OneRepaymentScheduleItem> selectRepaymentScheduleItem(String loanId, String todayt) throws Exception;
    
    public List<OneRepaymentScheduleItem> selectRepaymentScheduleItem2(String loanId, String today, String count) throws Exception;
    
    public int selectOverdueCount(String loanId, String today) throws Exception;
    
    public int selectOverdueCount2(String loanId, String count) throws Exception;
    
    public String selectOverdueBalance(String loanId) throws Exception;
    
    public List<String> selectOverdueLoanInfo() throws Exception;
    
    public List<String> selectOverdueLoanInfo_ExceptGPM() throws Exception;
    
    public boolean updateRepaymentScheduleOverDue(String loanId, String count, String delqAmount, String delqState) throws Exception;
    
    public boolean updatePaymentScheduleOverDue(String mid, String loanId, String count, String delqAmount, String tax, String taxLocal, String fee) throws Exception;
    
    public OneRestOverdueBalance selectRestOverdueBalance(String loanId) throws Exception;
    
    public String selectRecentRepaymentDate(String loanId) throws Exception;
    
    public boolean insertPrePayment(OnePrePayment onePrePayment) throws Exception;
    
    public boolean insertPrePaymentProvide(OnePrePaymentProvide onePrePaymentProvide) throws Exception;
    
    public boolean updatePaymentScheduleState(String loanId) throws Exception;
    
    public String selectCheckOverdue(String loanId) throws Exception;
    
    public boolean insertOrderPrePayment(OneOrderPrePayment oneOrderPrePayment) throws Exception;
    
    public OneOrderPrePayment selectOrderPrePaymentInfo(String loanId, String mid) throws Exception;
    
    public String selectIsOverDueState(String loanId, String count) throws Exception;
    
    public boolean insertOrverDueHistory(String loanId, String count) throws Exception;
    
    public boolean updateOrverDueState(String loanId, String count) throws Exception;
    
    public List<OneRepaymentCheckCount> selectRepaymentCheckCount(String loanId, String today) throws Exception;
    
    public List<OneRepaymentCheckCount> selectRepaymentCheckCount2(String loanId, String today) throws Exception;
    
    public List<OneRepaymentCheckCount> selectRepaymentCheckCount3(String loanId) throws Exception;
    
    public String selectLaonExecuteDate(String loanId) throws Exception;
    
    public String selectLoanCountByPayStatus(String loanId, String count) throws Exception;
    
    public String selectOverduePayAmountSum(@Param("loanId") String loanId, @Param("payDate") String payDate) throws Exception;
    
    public String selectOverdueCurrentDate(@Param("loanId") String loanId, @Param("count") String count) throws Exception;
    
    public boolean insertOrverDueDetailHistory(@Param("loanId") String loanId, @Param("count") String count
    		, @Param("addtionalRate") String addtionalRate, @Param("overdueAmount") String overdueAmount, @Param("overdueSate") String overdueSate) throws Exception;
    
    public PaymentScheduleItem selectPaymentScheduleItem(@Param("mid") String mid, @Param("loanId") String loanId, @Param("count") String count) throws Exception;

    public PaymentScheduleItem selectPaymentScheduleItemTest(@Param("mid") String mid, @Param("loanId") String loanId, @Param("count") String count) throws Exception;
    
    public boolean insertOrverDueInvestHistory(@Param("loanId") String loanId, @Param("mid") String mid, @Param("count") String count, @Param("overdue") String overdue
    		, @Param("tax") String tax, @Param("taxLocal") String taxLocal, @Param("fee") String fee, @Param("overdueState") String overdueState) throws Exception;

	public String selectOneMid(String loanId) throws Exception;

	public long selectRepaymentTotalRefundAmt(String oneMid, String loanId) throws Exception;

	public List<OneUnpaidRepayment> selectUnpaidRepayment(String string, String today) throws Exception;

	public double selectPaidTillTodayRepayment(String loanId, String payDate) throws Exception;

	public double selectGihanRepayment(String loanId) throws Exception;

	public String selectRecentCountRepaymentDate(String loanId, String prepayDate) throws Exception;

	public String selectRestBalance(String loanId, String count) throws Exception;

	public boolean updatePaymentScheduleTax(String mid, String loanId, String payCount, String tax_real, String tax_local_real) throws Exception;

	public long selectRepaymentPaidAmt(String loanId) throws Exception;

	public long selectHadToBePaidAmt(String loanId) throws Exception;


    
//	public boolean insertPrePayment(OnePrePayment onePrePayment) throws Exception;
//	
//	public boolean insertPrePaymentProvide(OnePrePaymentProvide onePrePaymentProvide) throws Exception;
}
