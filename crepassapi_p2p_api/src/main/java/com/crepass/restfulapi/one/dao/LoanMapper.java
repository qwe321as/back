package com.crepass.restfulapi.one.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.crepass.restfulapi.one.domain.OneInvest;
import com.crepass.restfulapi.one.domain.OneLoan;
import com.crepass.restfulapi.one.domain.OneLoanAddInvestInfo;
import com.crepass.restfulapi.one.domain.OneLoanCategory;
import com.crepass.restfulapi.one.domain.OneLoanContract;
import com.crepass.restfulapi.one.domain.OneLoanCustInfo;
import com.crepass.restfulapi.one.domain.OneLoanDataInfo;
import com.crepass.restfulapi.one.domain.OneLoanHeart;
import com.crepass.restfulapi.one.domain.OneLoanInvestInfoDetail;
import com.crepass.restfulapi.one.domain.OneLoanInvestInfoDetailReply;
import com.crepass.restfulapi.one.domain.OneLoanMemo;
import com.crepass.restfulapi.one.domain.OneLoanMemoHeart;
import com.crepass.restfulapi.one.domain.OneLoanMemoInfo;
import com.crepass.restfulapi.one.domain.OneLoanRepaymentSchedule;
import com.crepass.restfulapi.one.domain.OneLoanRepaymentSchedule2;
import com.crepass.restfulapi.one.domain.OneLoanTelecomConfirm;
import com.crepass.restfulapi.one.domain.OneLoanVirAccntInfo;
import com.crepass.restfulapi.one.domain.OneOrderDataInfo;
import com.crepass.restfulapi.v2.domain.LoanContractStep01;
import com.crepass.restfulapi.v2.domain.LoanContractStep02;
import com.crepass.restfulapi.v2.domain.LoanRepayAccntItem;
import com.crepass.restfulapi.v2.domain.LoanScheduleInfo;
import com.crepass.restfulapi.v2.domain.LoanScheduleItem;
import com.crepass.restfulapi.v2.domain.LoanStepInfo;
import com.crepass.restfulapi.v2.domain.PaymentHistoryInfo;
import com.crepass.restfulapi.v2.domain.PaymentHistoryItem;

public interface LoanMapper {
    
    public List<OneLoanCustInfo> selectLoanCustInfo(String key, String iv) throws Exception;
    
    public List<OneLoanCustInfo> selectLoanCustInfoTest(String key, String iv) throws Exception;
    
    public boolean selectLoanPaymentIsOK(String loanId) throws Exception;
    
    public OneLoanVirAccntInfo selectLoanVirAccntInfo(String mid) throws Exception;
    
    public List<OneLoanAddInvestInfo> selectLoanAddInvestInfo(String loanId) throws Exception;
    
    public int insertLoanHistory(OneInvest oneInvest) throws Exception;
    
    public int updateLoanState(String loanId) throws Exception;
    
    public boolean updateLoanState2(String loanId) throws Exception;
 
    public String selectLoanRecentId(String mid) throws Exception;
    
    public boolean insertLoanCategory(OneLoanCategory oneLoanCategory) throws Exception;
    
    public boolean insertLoanCategoryInfo(String loanId, String categoryId) throws Exception;
    
    public OneLoanInvestInfoDetail selectLoanInvestInfoDetail(String loanId) throws Exception;
    
    public List<OneLoanInvestInfoDetailReply> selectLoanInvestInfoDetailReply(String loanId, String mid) throws Exception;
    
    public String selectElementByLonId(String oId) throws Exception;
    
    public String selectIsCommentRow(String loanId, String oId) throws Exception;
    
    public boolean insertLoanMemo(OneLoanMemo oneLoanMemo) throws Exception;
    
    public boolean updateLoanMemo(String memoId, String memo) throws Exception;
    
    public OneLoanMemoInfo selectLoanMemo(String oId) throws Exception;
    
    public List<OneLoanMemoHeart> selectLoanHeart(String oId) throws Exception;
    
    public String selectLoanHeartInfo(String memoId, String mid) throws Exception;
    
    public boolean insertLoanHeart(OneLoanHeart oneLoanHeart) throws Exception;
    
    public boolean updateLoanHeart(String memoId, String mid, String heart) throws Exception;
    
    public boolean updateLoanAccnt(String loanId, String loanAccntNo) throws Exception;
    
    public boolean updateMemberLoanUse(String mid) throws Exception;
    
    public String selectCustTelecomConfirm(OneLoanTelecomConfirm oneLoanTelecomConfirm) throws Exception;
    
    public String selectLoanConfirm(String mid) throws Exception;
    
    public OneLoanContract selectLoanContract(String mid) throws Exception;
    
    public boolean updateLoanContractFlag(String loanId, String contractFlag) throws Exception;
    
    public List<OneLoanRepaymentSchedule> selectLoanRepaymentSchedule(String loanId) throws Exception;
    
    public List<OneLoanRepaymentSchedule2> selectLoanRepaymentSchedule2(String mid) throws Exception;
    
    public boolean insertLoan(OneLoan oneLoan) throws Exception;
    
    public String selectLoanSendSMS() throws Exception;
    
    public boolean insertConnectChannel(String loanId, String channel) throws Exception;
    
    public List<OneLoanDataInfo> selectLoanDataInfo(String mid) throws Exception;
    
    public List<OneOrderDataInfo> selectOrderDataInfo(String mid) throws Exception;
    
    // api v2 start
    public List<Map<String, Object>> selectLoanList(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("loanCode") String loanCode, @Param("mid") String mid, @Param("keyword") String keyword) throws Exception;
        
    public int selectLoanListCount(@Param("loanCode") String loanCode, @Param("mid") String mid, @Param("keyword") String keyword) throws Exception;
    
    public Map<String, Object> selectLoanItem(int loanId) throws Exception;
    
    public List<Map<String, String>> selectInvestorList(@Param("loanId") int loanId) throws Exception;
    
    public List<Map<String, String>> selectInvestTargetList(@Param("loanId") int loanId) throws Exception;
    
    public String selectLoanItemSubject(@Param("loanId") int loanId) throws Exception;
    
    public List<Map<String, Object>> selectPaymentList(int loanId) throws Exception;
	
	public Map<String, Object> selectLoanBalance(int loanId) throws Exception;
	
	public boolean insertLoanInfo(LoanStepInfo loanStepInfo) throws Exception;
	
	public boolean insertLoanInfoV2(LoanStepInfo loanStepInfo) throws Exception;
	
	public boolean insertLoanSocialSector(@Param("socialId") String socialId, @Param("loanId") String loanId, @Param("filePath") String filePath) throws Exception;
	
	public boolean insertLoanEmergencyHistory(@Param("loanId") String loanId, @Param("familyType") String familyType, @Param("contactAddress") String contactAddress) throws Exception;
	
	public String selectIndexLastId() throws Exception;
	
	public String selectMemberIsChecked(@Param("mid") String mid, @Param("mname") String mname, @Param("birth") String birth, @Param("hp") String hp, @Param("newsagency") String newsagency) throws Exception;
	
	public String selectLoanIsChecked(String mid) throws Exception;
	
	public LoanScheduleInfo selectLoanScheduleInfo(@Param("loanId") String loanId) throws Exception;
	
	public List<LoanScheduleItem> selectLoanScheduleList(@Param("loanId") String loanId) throws Exception;

	public List<LoanRepayAccntItem> selectLoanRepayAccntList(String mid) throws Exception;
	
	public List<PaymentHistoryItem> selectLoanPaymentHistoryItem(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("loanId") String loanId) throws Exception;
    
    public PaymentHistoryInfo selectLoanPaymentHistoryInfo(String loanId) throws Exception;
    
    public int selectLoanPaymentHistoryItemSize(String loanId) throws Exception;
    
    public LoanContractStep01 selectLoanContractStep01(String loanId) throws Exception;
    
    public LoanContractStep02 selectLoanContractStep02(String loanId) throws Exception;

	public int updateInvestList(String mid) throws Exception;

	public int updateLoanCond(String mid) throws Exception;

	public String selectRecentLoanExecTime(String mid) throws Exception;

	public String selectLoanCount(String mid) throws Exception;
	
	
}
