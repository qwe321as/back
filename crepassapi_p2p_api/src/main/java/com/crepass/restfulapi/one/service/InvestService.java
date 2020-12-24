package com.crepass.restfulapi.one.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crepass.restfulapi.one.dao.InvestMapper;
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

@Service
public class InvestService {

    @Autowired
    InvestMapper investMapper;
    
    public List<?> selectInvestLoanById(String mid) throws Exception {
        return investMapper.selectInvestLoanById(mid);
    }

    public List<OneWishInvest> selectInvestLoanListById(String mid, String categoryId, String keyword) throws Exception {
    	return investMapper.selectInvestLoanListById(mid, categoryId, keyword);
    }
    
    public List<OneWishInvest> selectInvestLoanListAllById(String mid, String keyword) throws Exception {
    	return investMapper.selectInvestLoanListAllById(mid, keyword);
    }
    
    public OneWishInvest selectInvestLoanListItemById(String mid, String loanId) throws Exception {
    	return investMapper.selectInvestLoanListItemById(mid, loanId);
    }
    
    public OneInvestLoan selectLoanById(String loanId) throws Exception {
        return investMapper.selectLoanById(loanId);
    }

    public String selectDebtById(String loanId) throws Exception {
        return investMapper.selectDebtById(loanId);
    }

    public OneInvestCredit selectCreditById(String loanId) throws Exception {
        return investMapper.selectCreditById(loanId);
    }

    public OneInvestInfo selectInvestById(String loanId, String mid) throws Exception {
        return investMapper.selectInvestById(loanId, mid);
    }

    public OneInvestAccount selectAccountById(String mid) throws Exception {
        OneInvestAccount oneInvestAccount = new OneInvestAccount();
        oneInvestAccount = investMapper.selectAccountById(mid);
        
        if (oneInvestAccount != null) {
            String str = oneInvestAccount.getMyBankName();
            if (str != null && !str.isEmpty()) {
                String[] txtArr = str.split("_");
                oneInvestAccount.setMyBankcode(txtArr[0].toString());
                oneInvestAccount.setMyBankName(txtArr[1].toString());
            }
        }
        
        return oneInvestAccount;
    }
    
    public OneInvestAccount selectAccountById2(String mid) throws Exception {
    	OneInvestAccount oneInvestAccount = new OneInvestAccount();
        oneInvestAccount = investMapper.selectAccountById2(mid);
        
        if (oneInvestAccount != null) {
            String str = oneInvestAccount.getMyBankName();
            if (str != null && !str.isEmpty()) {
                String[] txtArr = str.split("_");
                oneInvestAccount.setMyBankcode(txtArr[0].toString());
                oneInvestAccount.setMyBankName(txtArr[1].toString());
            }
        }
        
        return oneInvestAccount;
    }

    public OneInvestOrderUnit selectInvestOrderUnitById(String mid, String loanId) throws Exception {
        return investMapper.selectInvestOrderUnitById(mid, loanId);
    }

    public List<?> selectInvestOrderById(String mid, String loanId) throws Exception {
        return investMapper.selectInvestOrderById(mid, loanId);
    }

    public Object selectLoanContract(String mid) throws Exception {
        return investMapper.selectLoanContract(mid);
    }

    public Object selectInvestCerti(String mid) throws Exception {
        return investMapper.selectInvestCerti(mid);
    }

    public int updateiGrade(OneCrepassCredit oneCrepassCredit) throws Exception {
        return investMapper.updateiGrade(oneCrepassCredit);
    }
    
    public String selectInvestMinPay(String loan_id, String i_pay) throws Exception {
    	return investMapper.selectInvestMinPay(loan_id, i_pay);
    }
    
    public String selectInvestPossiblePay(String loan_id) throws Exception {
    	return investMapper.selectInvestPossiblePay(loan_id);
    }

    public String selectInvestPossiblePay2(String loan_id, String mid) throws Exception {
    	return investMapper.selectInvestPossiblePay2(loan_id, mid);
    }
    
    public OneInvestLimitPay selectInvestLimitPay(String mid) throws Exception {
    	return investMapper.selectInvestLimitPay(mid);
    }
    
    public OneInvestLimitPay selectInvestLimitPay2(String mid, String loanId) throws Exception {
    	return investMapper.selectInvestLimitPay2(mid, loanId);
    }
    
    public String selectInvestDuplicate(String mid, String loan_id) throws Exception {
    	return investMapper.selectInvestDuplicate(mid, loan_id);
    }

    public OneInvestTitle selectInvestTitle(String mid, String loan_id) throws Exception {
    	return investMapper.selectInvestTitle(mid, loan_id);
    }
    
    public int insertInvest(OneInvest oneInvest) throws Exception {
    	return investMapper.insertInvest(oneInvest);
    }
    
    public int updateInvest(String tid, String mid, String loanId) throws Exception {
    	return investMapper.updateInvest(tid, mid, loanId);
    }
 
    public OneInvestLoanDefault selectInvestLoan(String loan_id) throws Exception {
    	return investMapper.selectInvestLoan(loan_id);
    }
    
    public int insertInvestDetail(OneInvestDetail oneInvestDetail) throws Exception {
    	return investMapper.insertInvestDetail(oneInvestDetail);
    }
    
    public int insertInvestDetailAuto(OneInvestDetail oneInvestDetail) throws Exception {
    	return investMapper.insertInvestDetailAuto(oneInvestDetail);
    }
    
    public int insertInvestPaymentHistory(OneInvestPaymentHistory oneInvestPaymentHistory) throws Exception {
    	return investMapper.insertInvestPaymentHistory(oneInvestPaymentHistory);
    }
    
    public String selectInvestIsPlaying(String loan_id) throws Exception {
    	return investMapper.selectInvestIsPlaying(loan_id);
    }
    
    public String selectInvestSumPay(String loan_id) throws Exception {
    	return investMapper.selectInvestSumPay(loan_id);
    }
    
    public String selectInvestId(String loan_id) throws Exception {
    	return investMapper.selectInvestId(loan_id);
    }
    
    public boolean updatePrinRcvNo(String mid, String loanId, String prinRcvNo) throws Exception {
    	return investMapper.updatePrinRcvNo(mid, loanId, prinRcvNo);
    }
    
    public List<OneInvestBalanceState> selectInvestBalanceState(String mid) throws Exception {
    	return investMapper.selectInvestBalanceState(mid);
    }
    
    public OneInvestAutoDivision selectInvestAutoDivision(String mid) throws Exception {
    	return investMapper.selectInvestAutoDivision(mid);
    }
    
    public List<OneInvestCategory> selectInvestAutoCategory(String aid) throws Exception {
    	return investMapper.selectInvestAutoCategory(aid);
    }
    
    public boolean insertInvestAutoDivision(OneInvestAutoDivisionSet oneInvestAutoDivisionSet) throws Exception {
    	return investMapper.insertInvestAutoDivision(oneInvestAutoDivisionSet);
    }
    
    public boolean insertInvestAutoDivisionCategory(String aid, String categoryId) throws Exception {
    	return investMapper.insertInvestAutoDivisionCategory(aid, categoryId);
    }
    
    public List<OneInvestInfoData> selectInvestInfoData(String mid) throws Exception {
    	return investMapper.selectInvestInfoData(mid);
    }
    
    public List<OneInvestInfoOrderData> selectInvestInfoOrderData(String mid) throws Exception {
    	return investMapper.selectInvestInfoOrderData(mid);
    }
    
    public int updateInvestPay(String mid, String loanId, String iPay) throws Exception {
    	return investMapper.updateInvestPay(mid, loanId, iPay);
    }
    
    public boolean insertInvestHistory(String loanId, String custId, String transIpay, String iPay, String gcode, String Subject, String workState) throws Exception {
    	return investMapper.insertInvestHistory(loanId, custId, transIpay, iPay, gcode, Subject, workState);
    }
    
    public boolean updateInvestLeave(String mid, String loanId) throws Exception {
    	return investMapper.updateInvestLeave(mid, loanId);
    }
    
    public boolean deleteInvest(String mid, String loanId) throws Exception {
    	return investMapper.deleteInvest(mid, loanId);
    }
    
    public OneInvestUserInfo selectInvestUserInfo(String mid, String loanId) throws Exception {
    	return investMapper.selectInvestUserInfo(mid, loanId);
    }
    
    public OneLoanUserGrade selectLoanUserGrade(String loanId) throws Exception {
    	return investMapper.selectLoanUserGrade(loanId);
    }
    
    // api v2 start
    public List<LoansVO> selectLoanList(int pageNum, int pageSize, String mid, String keyword) throws Exception {
    	return investMapper.selectLoanList(pageNum, pageSize, mid, keyword);
    }
    
    public List<LoansVO2> selectLoanList2(int pageNum, int pageSize, String mid, String keyword, String fundingStatus, String sortOrder, String sortType,
    		String socialCorpAll, List<String> socialCodeList, List<String> poseCodeList, String creditGradeMin, String creditGradeMax, 
    		String crepassGradeMin, String crepassGradeMax, String loanDayMin, String loanDayMax, String loanPayMin, String loanPayMax, String loanRate) throws Exception {
    	return investMapper.selectLoanList2(pageNum, pageSize, mid, keyword, fundingStatus, sortOrder, sortType, socialCorpAll, socialCodeList, poseCodeList,
    			creditGradeMin, creditGradeMax, crepassGradeMin, crepassGradeMax, loanDayMin, loanDayMax, loanPayMin, loanPayMax, loanRate);
    }
    
    public int selectLoanListCount(String keyword) throws Exception {
		return investMapper.selectLoanListCount(keyword);
	}
    
    public LoansVO selectLoanItem(String mid, int loanId) throws Exception {
		return investMapper.selectLoanItem(mid, loanId);
	}
    
    public int selectInvestorListCount(int investLv, String mid, String keyword) throws Exception {
    	return investMapper.selectInvestorListCount(investLv, mid, keyword);
    }
    
    public List<Map<String, Object>> selectInvestorList(int pageNum, int pageSize, int investLv, String mid, String keyword) throws Exception {
    	return investMapper.selectInvestorList(pageNum, pageSize, investLv, mid, keyword);
    }
    
    public List<InvestReplyList> selectInvestItemList(int offSetNum, int rowCount, String mid) throws Exception {
    	return investMapper.selectInvestItemList(offSetNum, rowCount, mid);
    }
    
    public List<InvestReplyList2> selectInvestItemList2(int offSetNum, int rowCount, String mid) throws Exception {
    	return investMapper.selectInvestItemList2(offSetNum, rowCount, mid);
    }
    
    public int selectInvestItemListCount(String mid) throws Exception {
		return investMapper.selectInvestItemListCount(mid);
	}
    
    public int selectInvestListCount(String mid, String investCode, String keyword) throws Exception {
		return investMapper.selectInvestListCount(mid, investCode, keyword);
	}
    
    public List<Map<String, Object>> selectInvestList(int pageNum, int pageSize, String mid, String investCode, String keyword) throws Exception {
    	return investMapper.selectInvestList(pageNum, pageSize, mid, investCode, keyword);
    }
    
    public InvestDetailItem selectInvestItemDetail(String loanId) throws Exception {
    	return investMapper.selectInvestItemDetail(loanId);
    }

    public InvestDetailItem2 selectInvestItemDetail2(String loanId) throws Exception {
    	return investMapper.selectInvestItemDetail2(loanId);
    }
    public String selectDebtById2(String loanId) throws Exception {
    	return investMapper.selectDebtById2(loanId);
    }
    
    public List<InvestBundleList> selectInvestBundleList(String mid) throws Exception {
    	return investMapper.selectInvestBundleList(mid);
    }
    
    public boolean insertInvestStack(String loanId, String mid, String iPay) throws Exception {
    	return investMapper.insertInvestStack(loanId, mid, iPay);
    }
    
    public List<InvestScheduleItem> selectInvestScheduleList(String mid, String loanId) throws Exception {
    	return investMapper.selectInvestScheduleList(mid, loanId);
    }
    
    public InvestScheduleInfo selectInvestScheduleInfo(String mid, String loanId) throws Exception {
    	return investMapper.selectInvestScheduleInfo(mid, loanId);
    }
    
    public List<PaymentHistoryItem> selectInvestPaymentHistoryItem(int pageNum, int pageSize, String mid) throws Exception {
    	return investMapper.selectInvestPaymentHistoryItem(pageNum, pageSize, mid);
    }
    
    public PaymentHistoryInfo selectInvestPaymentHistoryInfo(String mid) throws Exception {
    	return investMapper.selectInvestPaymentHistoryInfo(mid);
    }
    
    public int selectInvestPaymentHistoryItemSize(String mid) throws Exception {
    	return investMapper.selectInvestPaymentHistoryItemSize(mid);
    }
    
    public InvestAutoSplit selectInvestAutoDivision2(String mid) throws Exception {
    	return investMapper.selectInvestAutoDivision2(mid);
    }
    

	public int selectLoanListCount2(int pageNum, int pageSize, String mid, String keyword, String fundingStatus, String sortOrder, String sortType, String socialCorpAll, List<String> socialCodeList, List<String> poseCodeList, String creditGradeMin, String creditGradeMax, String crepassGradeMin, String crepassGradeMax, String loanDayMin, String loanDayMax, String loanPayMin, String loanPayMax, String loanRate) throws Exception {
		return investMapper.selectLoanListCount2(pageNum, pageSize, mid, keyword, fundingStatus, sortOrder, sortType, socialCorpAll, socialCodeList, poseCodeList, creditGradeMin, creditGradeMax, crepassGradeMin, crepassGradeMax, loanDayMin, loanDayMax, loanPayMin, loanPayMax, loanRate);
	}

	public int selectInvestingTotalCount(String mid) throws Exception {
		return investMapper.selectInvestingTotalCount(mid);
	}

	public String selectRecentRegTime(String mid) throws Exception {
		return investMapper.selectRecentRegTime(mid);
	}

	public String selectRecentInvestTime(String mid) throws Exception {
		return investMapper.selectRecentInvestTime(mid);
	}

	public int updateInvestList(String mid) throws Exception {
		return investMapper.updateInvestList(mid);
	}

	public int updateInvestTran(String mid) throws Exception {
		return investMapper.updateInvestTran(mid);
	}

	public String selectInvestLevel(String mid) throws Exception {
		return investMapper.selectInvestLevel(mid);
	}

	public String selectInvestTotalAmount(String mid) throws Exception {
		return investMapper.selectInvestTotalAmount(mid);
	}

	public String selectInvestLimitation(String mid) throws Exception {
		return investMapper.selectInvestLimitation(mid);
	}

	public String selectUsedToInvest(String midLoan, String midInvest) throws Exception {
		return investMapper.selectUsedToInvest(midLoan, midInvest);
	}

	public String selectInvestReturnedPayAmount(String mid) throws Exception {
		return investMapper.selectInvestReturnedPayAmount(mid);
	}

}
