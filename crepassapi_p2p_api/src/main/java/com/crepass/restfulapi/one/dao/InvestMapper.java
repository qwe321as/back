package com.crepass.restfulapi.one.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.crepass.restfulapi.one.domain.OneCrepassCredit;
import com.crepass.restfulapi.one.domain.OneInvest;
import com.crepass.restfulapi.one.domain.OneInvestAccount;
import com.crepass.restfulapi.one.domain.OneInvestAutoDivision;
import com.crepass.restfulapi.one.domain.OneInvestAutoDivisionSet;
import com.crepass.restfulapi.one.domain.OneInvestBalanceState;
import com.crepass.restfulapi.one.domain.OneInvestCategory;
import com.crepass.restfulapi.one.domain.OneInvestCredit;
import com.crepass.restfulapi.one.domain.OneInvestDetail;
import com.crepass.restfulapi.one.domain.OneInvestInfo;
import com.crepass.restfulapi.one.domain.OneInvestInfoData;
import com.crepass.restfulapi.one.domain.OneInvestInfoOrderData;
import com.crepass.restfulapi.one.domain.OneInvestLimitPay;
import com.crepass.restfulapi.one.domain.OneInvestLoan;
import com.crepass.restfulapi.one.domain.OneInvestLoanDefault;
import com.crepass.restfulapi.one.domain.OneInvestOrderUnit;
import com.crepass.restfulapi.one.domain.OneInvestPaymentHistory;
import com.crepass.restfulapi.one.domain.OneInvestTitle;
import com.crepass.restfulapi.one.domain.OneInvestUserInfo;
import com.crepass.restfulapi.one.domain.OneLoanUserGrade;
import com.crepass.restfulapi.one.domain.OneWishInvest;
import com.crepass.restfulapi.v2.domain.InvestAutoSplit;
import com.crepass.restfulapi.v2.domain.InvestBundleList;
import com.crepass.restfulapi.v2.domain.InvestDetailItem;
import com.crepass.restfulapi.v2.domain.InvestDetailItem2;
import com.crepass.restfulapi.v2.domain.InvestReplyList;
import com.crepass.restfulapi.v2.domain.InvestReplyList2;
import com.crepass.restfulapi.v2.domain.InvestScheduleInfo;
import com.crepass.restfulapi.v2.domain.InvestScheduleItem;
import com.crepass.restfulapi.v2.domain.LoansVO;
import com.crepass.restfulapi.v2.domain.LoansVO2;
import com.crepass.restfulapi.v2.domain.PaymentHistoryInfo;
import com.crepass.restfulapi.v2.domain.PaymentHistoryItem;

public interface InvestMapper {
    
    public List<?> selectInvestLoanById(String mid) throws Exception;
    
    public List<OneWishInvest> selectInvestLoanListById(String mid, String categoryId, String keyword) throws Exception;
    
    public List<OneWishInvest> selectInvestLoanListAllById(String mid, String keyword) throws Exception;

    public OneWishInvest selectInvestLoanListItemById(String mid, String loanId) throws Exception;
    
    public OneInvestLoan selectLoanById(String loanId) throws Exception;

    public String selectDebtById(String loanId) throws Exception;

    public OneInvestCredit selectCreditById(String loanId) throws Exception;

    public OneInvestInfo selectInvestById(String loanId, String mid) throws Exception;

    public OneInvestAccount selectAccountById(String mid) throws Exception;
    
    public OneInvestAccount selectAccountById2(String mid) throws Exception;

    public OneInvestOrderUnit selectInvestOrderUnitById(String mid, String loanId) throws Exception;

    public List<?> selectInvestOrderById(String mid, String loanId) throws Exception;

    public Object selectInvestCerti(String mid) throws Exception;

    public Object selectLoanContract(String mid) throws Exception;

    public int updateiGrade(OneCrepassCredit oneCrepassCredit) throws Exception;

    public String selectInvestMinPay(String loan_id, String i_pay) throws Exception;
    
    public String selectInvestPossiblePay(String loan_id) throws Exception;
    
    public String selectInvestPossiblePay2(String loan_id, String mid) throws Exception;
 
    public OneInvestLimitPay selectInvestLimitPay(String mid) throws Exception;
    
    public OneInvestLimitPay selectInvestLimitPay2(String mid, String loanId) throws Exception;
    
    public String selectInvestDuplicate(String mid, String loan_id) throws Exception;
    
    public OneInvestTitle selectInvestTitle(String mid, String loan_id) throws Exception;
    
    public int insertInvest(OneInvest oneInvest) throws Exception;
    
    public int updateInvest(String tid, String mid, String loanId) throws Exception;
    
    public OneInvestLoanDefault selectInvestLoan(String loan_id) throws Exception;
    
    public int insertInvestDetail(OneInvestDetail oneInvestDetail) throws Exception;
    
    public int insertInvestDetailAuto(OneInvestDetail oneInvestDetail) throws Exception;
    
    public int insertInvestPaymentHistory(OneInvestPaymentHistory oneInvestPaymentHistory) throws Exception;
    
    public String selectInvestIsPlaying(String loan_id) throws Exception;
    
    public String selectInvestSumPay(String loan_id) throws Exception;
    
    public String selectInvestId(String loan_id) throws Exception;
    
    public boolean updatePrinRcvNo(String mid, String loanId, String prinRcvNo) throws Exception;
    
    public List<OneInvestBalanceState> selectInvestBalanceState(String mid) throws Exception;
    
    public OneInvestAutoDivision selectInvestAutoDivision(String mid) throws Exception;
    
    public List<OneInvestCategory> selectInvestAutoCategory(String aid) throws Exception;
 
    public boolean insertInvestAutoDivision(OneInvestAutoDivisionSet oneInvestAutoDivisionSet) throws Exception;
    
    public boolean insertInvestAutoDivisionCategory(String aid, String categoryId) throws Exception;
    
    public List<OneInvestInfoData> selectInvestInfoData(String mid) throws Exception;
    
    public List<OneInvestInfoOrderData> selectInvestInfoOrderData(String mid) throws Exception;
    
    public int updateInvestPay(String mid, String loanId, String iPay) throws Exception;
    
    public boolean insertInvestHistory(String loanId, String custId, String transIpay, String iPay, String gcode, String Subject, String workState) throws Exception;
    
    public boolean updateInvestLeave(String mid, String loanId) throws Exception;
    
    public boolean deleteInvest(String mid, String loanId) throws Exception;
    
    public OneInvestUserInfo selectInvestUserInfo(String mid, String loanId) throws Exception;
    
    public OneLoanUserGrade selectLoanUserGrade(String loanId) throws Exception;
    
    // api v2 start
    public List<LoansVO> selectLoanList(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("mid") String mid, @Param("keyword") String keyword) throws Exception;
    
    public List<LoansVO2> selectLoanList2(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, 
    		@Param("mid") String mid, @Param("keyword") String keyword, @Param("fundingStatus") String fundingStatus,
    		@Param("sortOrder") String sortOrder, @Param("sortType") String sortType, @Param("socialCorpAll") String socialCorpAll, @Param("socialCodeList") List<String> socialCodeList, @Param("poseCodeList") List<String> poseCodeList,
    		
    		@Param("creditGradeMin") String creditGradeMin, @Param("creditGradeMax") String creditGradeMax, @Param("crepassGradeMin") String crepassGradeMin,
    		@Param("crepassGradeMax") String crepassGradeMax, @Param("loanDayMin") String loanDayMin, @Param("loanDayMax") String loanDayMax, 
    		@Param("loanPayMin") String loanPayMin, @Param("loanPayMax") String loanPayMax, @Param("loanRate") String loanRate
    		) throws Exception;
    
    public int selectLoanListCount(String keyword) throws Exception;

    public LoansVO selectLoanItem(@Param("mid") String mid, @Param("loanId") int loanId) throws Exception;
    
    public int selectInvestorListCount(@Param("investLv") int investLv, @Param("mid") String mid, @Param("keyword") String keyword) throws Exception;
    
    public List<Map<String, Object>> selectInvestorList(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("investLv") int investLv, @Param("mid") String mid, @Param("keyword") String keyword) throws Exception;
    
    public List<InvestReplyList> selectInvestItemList(@Param("pageNum") int offSetNum, @Param("pageSize") int rowCount, @Param("mid") String mid) throws Exception;
    
    public List<InvestReplyList2> selectInvestItemList2(@Param("pageNum") int offSetNum, @Param("pageSize") int rowCount, @Param("mid") String mid) throws Exception;
    
    public int selectInvestItemListCount(String mid) throws Exception;
    
    public int selectInvestListCount(@Param("mid") String mid, @Param("investCode") String investCode, @Param("keyword") String keyword) throws Exception;
    
    public List<Map<String, Object>> selectInvestList(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("mid") String mid, @Param("investCode") String investCode, @Param("keyword") String keyword) throws Exception;
    
    public InvestDetailItem selectInvestItemDetail(String loanId) throws Exception;
    
    public InvestDetailItem2 selectInvestItemDetail2(String loanId) throws Exception;
    
    public String selectDebtById2(String loanId) throws Exception;
    
    public List<InvestBundleList> selectInvestBundleList(String mid) throws Exception;
    
    public boolean insertInvestStack(@Param("loanId") String loanId, @Param("mid") String mid, @Param("iPay") String iPay) throws Exception;
    
    public List<InvestScheduleItem> selectInvestScheduleList(@Param("mid") String mid, @Param("loanId") String loanId) throws Exception;
    
    public InvestScheduleInfo selectInvestScheduleInfo(@Param("mid") String mid, @Param("loanId") String loanId) throws Exception;
    
    public List<PaymentHistoryItem> selectInvestPaymentHistoryItem(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("mid") String mid) throws Exception;
    
    public PaymentHistoryInfo selectInvestPaymentHistoryInfo(String mid) throws Exception;
    
    public int selectInvestPaymentHistoryItemSize(String mid) throws Exception;
    
    public InvestAutoSplit selectInvestAutoDivision2(String mid) throws Exception;
    

	public int selectLoanListCount2(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, 
    		@Param("mid") String mid, @Param("keyword") String keyword, @Param("fundingStatus") String fundingStatus,
    		@Param("sortOrder") String sortOrder, @Param("sortType") String sortType, 
    		@Param("socialCorpAll") String socialCorpAll, @Param("socialCodeList") List<String> socialCodeList, @Param("poseCodeList") List<String> poseCodeList, 
    		@Param("creditGradeMin") String creditGradeMin, @Param("creditGradeMax") String creditGradeMax, @Param("crepassGradeMin") String crepassGradeMin,
    		@Param("crepassGradeMax") String crepassGradeMax, @Param("loanDayMin") String loanDayMin, @Param("loanDayMax") String loanDayMax,
    		@Param("loanPayMin") String loanPayMin, @Param("loanPayMax") String loanPayMax, @Param("loanRate") String loanRate) throws Exception;

	public int selectInvestingTotalCount(String mid) throws Exception;

	public String selectRecentRegTime(String mid) throws Exception;

	public String selectRecentInvestTime(String mid) throws Exception;

	public int updateInvestList(String mid) throws Exception;

	public int updateInvestTran(String mid) throws Exception;

	public String selectInvestLevel(String mid) throws Exception;

	public String selectInvestTotalAmount(String mid) throws Exception;

	public String selectInvestLimitation(String mid) throws Exception;

	public String selectUsedToInvest(String midLoan, String midInvest) throws Exception;

	public String selectInvestReturnedPayAmount(String mid) throws Exception;
	

}
