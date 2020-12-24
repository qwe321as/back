package com.crepass.restfulapi.one.service;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.crepass.restfulapi.one.dao.LoanMapper;
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
import com.crepass.restfulapi.v2.domain.LoanStepEmergency;
import com.crepass.restfulapi.v2.domain.LoanStepInfo;
import com.crepass.restfulapi.v2.domain.PaymentHistoryInfo;
import com.crepass.restfulapi.v2.domain.PaymentHistoryItem;

@Service
public class LoanService {

    @Autowired
    LoanMapper loanMapper;
    
    public List<OneLoanCustInfo> selectLoanCustInfo(String key, String iv) throws Exception {
        return loanMapper.selectLoanCustInfo(key, iv);
    }
    
    public List<OneLoanCustInfo> selectLoanCustInfoTest(String key, String iv) throws Exception {
        return loanMapper.selectLoanCustInfoTest(key, iv);
    }
        
    public boolean selectLoanPaymentIsOK(String loanId) throws Exception {
    	return loanMapper.selectLoanPaymentIsOK(loanId);
    }
    
    public OneLoanVirAccntInfo selectLoanVirAccntInfo(String mid) throws Exception {
    	return loanMapper.selectLoanVirAccntInfo(mid);
    }
    
    public List<OneLoanAddInvestInfo> selectLoanAddInvestInfo(String loanId) throws Exception {
    	return loanMapper.selectLoanAddInvestInfo(loanId);
    }
    
    public int insertLoanHistory(OneInvest oneInvest) throws Exception {
    	return loanMapper.insertLoanHistory(oneInvest);
    }
    
    public int updateLoanState(String loanId) throws Exception {
    	return loanMapper.updateLoanState(loanId);
    }
    
    public boolean updateLoanState2(String loanId) throws Exception {
    	return loanMapper.updateLoanState2(loanId);
    }
    
    public String selectLoanRecentId(String mid) throws Exception {
    	return loanMapper.selectLoanRecentId(mid);
    }
    
    public boolean insertLoanCategory(OneLoanCategory oneLoanCategory) throws Exception {
    	return loanMapper.insertLoanCategory(oneLoanCategory);
    } 
    
    public boolean insertLoanCategoryInfo(String loanId, String categoryId) throws Exception {
    	return loanMapper.insertLoanCategoryInfo(loanId, categoryId);
    }
    
    public OneLoanInvestInfoDetail selectLoanInvestInfoDetail(String loanId) throws Exception {
    	return loanMapper.selectLoanInvestInfoDetail(loanId);
    }
    
    public List<OneLoanInvestInfoDetailReply> selectLoanInvestInfoDetailReply(String loanId, String mid) throws Exception {
    	return loanMapper.selectLoanInvestInfoDetailReply(loanId, mid);
    }
    
    public String selectElementByLonId(String oId) throws Exception {
    	return loanMapper.selectElementByLonId(oId);
    }
    
    public String selectIsCommentRow(String loanId, String oId) throws Exception {
    	return loanMapper.selectIsCommentRow(loanId, oId);
    }
    
    public boolean insertLoanMemo(OneLoanMemo oneLoanMemo) throws Exception {
    	return loanMapper.insertLoanMemo(oneLoanMemo);
    }
    
    public boolean updateLoanMemo(String memoId, String memo) throws Exception {
    	return loanMapper.updateLoanMemo(memoId, memo);
    }
    
    public OneLoanMemoInfo selectLoanMemo(String oId) throws Exception {
    	return loanMapper.selectLoanMemo(oId);
    }
    
    public List<OneLoanMemoHeart> selectLoanHeart(String oId) throws Exception {
    	return loanMapper.selectLoanHeart(oId);
    }
    
    public String selectLoanHeartInfo(String memoId, String mid) throws Exception {
    	return loanMapper.selectLoanHeartInfo(memoId, mid);
    }
    
    public boolean insertLoanHeart(OneLoanHeart oneLoanHeart) throws Exception {
    	return loanMapper.insertLoanHeart(oneLoanHeart);
    }
    
    public boolean updateLoanHeart(String memoId, String mid, String heart) throws Exception {
    	return loanMapper.updateLoanHeart(memoId, mid, heart);
    }
    
    public boolean updateLoanAccnt(String loanId, String loanAccntNo) throws Exception {
    	return loanMapper.updateLoanAccnt(loanId, loanAccntNo);
    }
    
    public boolean updateMemberLoanUse(String mid) throws Exception {
    	return loanMapper.updateMemberLoanUse(mid);
    }
    
    public String selectCustTelecomConfirm(OneLoanTelecomConfirm oneLoanTelecomConfirm) throws Exception {
    	return loanMapper.selectCustTelecomConfirm(oneLoanTelecomConfirm);
    }
    
    public String selectLoanConfirm(String mid) throws Exception {
    	return loanMapper.selectLoanConfirm(mid);
    }
    
    public OneLoanContract selectLoanContract(String mid) throws Exception {
    	return loanMapper.selectLoanContract(mid);
    }
    
    public boolean updateLoanContractFlag(String loanId, String contractFlag) throws Exception {
    	return loanMapper.updateLoanContractFlag(loanId, contractFlag);
    }
    
    public List<OneLoanRepaymentSchedule> selectLoanRepaymentSchedule(String loanId) throws Exception {
    	return loanMapper.selectLoanRepaymentSchedule(loanId);
    }
    
    public List<OneLoanRepaymentSchedule2> selectLoanRepaymentSchedule2(String mid) throws Exception {
    	return loanMapper.selectLoanRepaymentSchedule2(mid);
    }
    
    public boolean insertLoan(OneLoan oneLoan) throws Exception {
    	return loanMapper.insertLoan(oneLoan);
    }
    
    public String selectLoanSendSMS() throws Exception {
    	return loanMapper.selectLoanSendSMS();
    }
    
    public boolean insertConnectChannel(String loanId, String channel) throws Exception {
    	return loanMapper.insertConnectChannel(loanId, channel);
    }
    
    public List<OneLoanDataInfo> selectLoanDataInfo(String mid) throws Exception {
    	return loanMapper.selectLoanDataInfo(mid);
    }
    
    public List<OneOrderDataInfo> selectOrderDataInfo(String mid) throws Exception {
    	return loanMapper.selectOrderDataInfo(mid);
    }
    
    // api v2 start
    public List<Map<String, Object>> selectLoanList(int pageNum, int pageSize,String loanCode, String mid, String keyword) throws Exception {
    	return loanMapper.selectLoanList(pageNum, pageSize, loanCode, mid, keyword);
    }
    
    public int selectLoanListCount(String loanCode, String mid, String keyword) throws Exception {
		return loanMapper.selectLoanListCount(loanCode, mid, keyword);
	}
    
    public Map<String, Object> selectLoanItem(int loanId) throws Exception {
    	return loanMapper.selectLoanItem(loanId);
    }
    
    public List<Map<String, String>> selectInvestorList(int loanId) throws Exception {
    	return loanMapper.selectInvestorList(loanId);
    }
    
    public List<Map<String, String>> selectInvestTargetList(int loanId) throws Exception {
		return loanMapper.selectInvestTargetList(loanId);
	}
    
    public String selectLoanItemSubject(int loanId) throws Exception{
		return loanMapper.selectLoanItemSubject(loanId);
	}
    
    public List<Map<String, Object>> selectPaymentList(int loanId) throws Exception {
    	return loanMapper.selectPaymentList(loanId);
    }
	
	public Map<String, Object> selectLoanBalance(int loanId) throws Exception {
		return loanMapper.selectLoanBalance(loanId);
	}
	
	public boolean addLoanMultipart(LoanStepInfo loanStepInfo, String custId) throws Exception {
		MultipartFile uploadFile = loanStepInfo.getUploadFile();
		String domainPath = "";
		
		if(uploadFile != null) {															// 파일첨부
			final String path = File.separator;
			String uploadPath = path+"var"+path+"lib"+path+"tomcat8"+path+"webapps"+path+"ROOT"+path+"members"+path+custId+path+"sector";
//			String uploadPath = "C:"+path+"Users"+path+"DEV"+path+"Desktop";
			domainPath = "https://p2p.crepass.com/members/"+custId+"/sector/"+uploadFile.getOriginalFilename();
			
			File file = new File(uploadPath);
			
			if(!file.exists()) file.mkdirs();
			
			file = new File(uploadPath, uploadFile.getOriginalFilename());
			uploadFile.transferTo(file);
		}
		
		boolean isInsertLoanInfo;
		
		
		if(loanStepInfo.getLoanStep01Item().getLoanCate().equals("cate08"))
			isInsertLoanInfo = loanMapper.insertLoanInfoV2(loanStepInfo);	// 200611 미혼모 추가로인해 분기처리 mari_loan insert하는곳
		else 
			isInsertLoanInfo = loanMapper.insertLoanInfo(loanStepInfo);		// mari_loan insert하는곳
		
		
		String loanId = loanMapper.selectIndexLastId();
		List<LoanStepEmergency> loanStepEmergencies = loanStepInfo.getLoanStep03Item().getEmergencyList();
		
		if(loanStepInfo.getSocialId() != null && !loanStepInfo.getSocialId().isEmpty())
			loanMapper.insertLoanSocialSector(loanStepInfo.getSocialId(), loanId, domainPath);		// clss-insert
		
		for(int i = 0; i < loanStepEmergencies.size(); i++) {
			if(!loanStepEmergencies.get(i).getFamilyType().isEmpty() && !loanStepEmergencies.get(i).getContactAddress().isEmpty())
				loanMapper.insertLoanEmergencyHistory(loanId, loanStepEmergencies.get(i).getFamilyType(), loanStepEmergencies.get(i).getContactAddress());	// ceh-insert
		}
		
		return isInsertLoanInfo;
	}
	
	public boolean addLoanBasic(LoanStepInfo loanStepInfo) throws Exception {
		return loanMapper.insertLoanInfo(loanStepInfo);
	}
	
	public String selectMemberIsChecked(String mid, String mname, String birth, String hp, String newsagency) throws Exception {
		return loanMapper.selectMemberIsChecked(mid, mname, birth, hp, newsagency);
	}
	
	public String selectLoanIsChecked(String mid) throws Exception {
		return loanMapper.selectLoanIsChecked(mid);
	}
	
	public LoanScheduleInfo selectLoanScheduleInfo(String loanId) throws Exception {
		return loanMapper.selectLoanScheduleInfo(loanId);
	}
	
	public List<LoanScheduleItem> selectLoanScheduleList(String loanId) throws Exception {
		return loanMapper.selectLoanScheduleList(loanId);
	}
	
	public List<LoanRepayAccntItem> selectLoanRepayAccntList(String mid) throws Exception {
		return loanMapper.selectLoanRepayAccntList(mid);
	}
	
	public List<PaymentHistoryItem> selectLoanPaymentHistoryItem(int pageNum, int pageSize, String loanId) throws Exception {
		return loanMapper.selectLoanPaymentHistoryItem(pageNum, pageSize, loanId);
	}
    
    public PaymentHistoryInfo selectLoanPaymentHistoryInfo(String loanId) throws Exception {
    	return loanMapper.selectLoanPaymentHistoryInfo(loanId);
    }
    
    public int selectLoanPaymentHistoryItemSize(String loanId) throws Exception {
    	return loanMapper.selectLoanPaymentHistoryItemSize(loanId);
    }
    
    public LoanContractStep01 selectLoanContractStep01(String loanId) throws Exception {
    	return loanMapper.selectLoanContractStep01(loanId);
    }
    
    public LoanContractStep02 selectLoanContractStep02(String loanId) throws Exception {
    	return loanMapper.selectLoanContractStep02(loanId);
    }

	public int updateInvestList(String mid) throws Exception {
		return loanMapper.updateInvestList(mid);
	}

	public int updateLoanCond(String mid) throws Exception {
		return loanMapper.updateLoanCond(mid);
	}

	public String selectRecentLoanExecTime(String mid) throws Exception {
		return loanMapper.selectRecentLoanExecTime(mid);
	}

	public String selectLoanCount(String mid) throws Exception {
		return loanMapper.selectLoanCount(mid);
	}

}
