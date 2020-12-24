package com.crepass.restfulapi.one.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crepass.restfulapi.one.dao.NotifyScheMapper;
import com.crepass.restfulapi.one.domain.OneLoanExcuteTotal;
import com.crepass.restfulapi.one.domain.OneNewMemberEventCnt;
import com.crepass.restfulapi.one.domain.OneNotiRepaymentUserInfo;
import com.crepass.restfulapi.one.domain.OneRepaymentManage;
import com.crepass.restfulapi.one.domain.OneRepaymentUserInfo;
import com.crepass.restfulapi.one.domain.OneSendEmail;
import com.crepass.restfulapi.one.domain.OneSendEmailContents;
import com.crepass.restfulapi.one.domain.OneSendEmailInfo;
import com.crepass.restfulapi.one.domain.OneSendSMS;
import com.crepass.restfulapi.one.domain.OneSendSMSByCMID;
import com.crepass.restfulapi.one.domain.OneStartInvestUserInfo;

@Service
public class NotifyScheService {
    
    @Autowired
    NotifyScheMapper notifyScheMapper;
    
    public List<OneRepaymentManage> selectRepaymentManageNomal() throws Exception {
    	return notifyScheMapper.selectRepaymentManageNomal();
    }
    
    public List<OneRepaymentManage> selectRepaymentManageOrverDue() throws Exception {
    	return notifyScheMapper.selectRepaymentManageOrverDue();
    }
    
    public List<OneRepaymentManage> selectRepaymentManagePrePay() throws Exception {
    	return notifyScheMapper.selectRepaymentManagePrePay();
    }
    
    public List<OneRepaymentUserInfo> selectRepaymentUserInfo(String today) throws Exception {
    	return notifyScheMapper.selectRepaymentUserInfo(today);
    }
    
    public List<OneRepaymentUserInfo> selectRepaymentOrverdueUserInfo(String today, String useType) throws Exception {
    	return notifyScheMapper.selectRepaymentOrverdueUserInfo(today, useType);
    }
    
    public boolean insertSendSMS(String name, String type, String custId, String useFlag, String request) throws Exception {
    	return notifyScheMapper.insertSendSMS(name, type, custId, useFlag, request);
    }
    
    public List<OneSendSMS> selectSendRepaymentSMS() throws Exception {
    	return notifyScheMapper.selectSendRepaymentSMS();
    }
    
    public boolean updateSendSMS(String createdDt, String name, String type, String batchFlag, String response, String cmid) throws Exception {
    	return notifyScheMapper.updateSendSMS(createdDt, name, type, batchFlag, response, cmid);
    }
    
    public List<OneSendSMSByCMID> selectSendSMSByCMID() throws Exception {
    	return notifyScheMapper.selectSendSMSByCMID();
    }
    
    public boolean updateSendSMSResult(String createdDt, String cmid, String result) throws Exception {
    	return notifyScheMapper.updateSendSMSResult(createdDt, cmid, result);
    }
    
    public List<OneStartInvestUserInfo> selectLoanCancelInvestUser() throws Exception {
    	return notifyScheMapper.selectLoanCancelInvestUser();
    }
    
    public String selectIsSmsSendingInvestUser(String custId, String subject) throws Exception {
    	return notifyScheMapper.selectIsSmsSendingInvestUser(custId, subject);
    }
    
    public List<String> selectRepaymentByLoanId() throws Exception {
    	return notifyScheMapper.selectRepaymentByLoanId();
    }
    
    public String selectBalanceRepayment(String loanId) throws Exception {
    	return notifyScheMapper.selectBalanceRepayment(loanId);
    }
    
    public OneNotiRepaymentUserInfo selectNotiRepaymentUserInfo(String loanId) throws Exception {
    	return notifyScheMapper.selectNotiRepaymentUserInfo(loanId);
    }
    
    public String selectNotiRepaymentIsPossible(String loanId, String today) throws Exception {
    	return notifyScheMapper.selectNotiRepaymentIsPossible(loanId, today);
    }
    
    public List<OneSendEmailInfo> selectSendEmailInfo() throws Exception {
    	return notifyScheMapper.selectSendEmailInfo();
    }
    
    public OneSendEmailContents selectSendEmailContents(long seq) throws Exception {
    	return notifyScheMapper.selectSendEmailContents(seq);
    }
    
    public boolean updateSendEmailState(OneSendEmailInfo sendEmailInfo) throws Exception {
    	return notifyScheMapper.updateSendEmailState(sendEmailInfo);
    }
    
    public List<String> selectEmailFileName(long seq) throws Exception {
    	return notifyScheMapper.selectEmailFileName(seq);
    }
    
    public boolean insertEmailHistory(String seq, String emailTitle, String emailBody) throws Exception {
    	return notifyScheMapper.insertEmailHistory(seq, emailTitle, emailBody);
    }
    
    public boolean insertEmailTarget(List<OneSendEmailInfo> oneSendEmailInfos) throws Exception {
    	return notifyScheMapper.insertEmailTarget(oneSendEmailInfos);
    }
    
    public boolean insertEmailTarget2(List<OneSendEmailInfo> oneSendEmailInfos) throws Exception {
    	return notifyScheMapper.insertEmailTarget2(oneSendEmailInfos);
    }
    
    public List<String> selectPossibleInvest(String mid) throws Exception {
    	return notifyScheMapper.selectPossibleInvest(mid);
    }

	public OneNewMemberEventCnt selectNewMemberEventCnt() throws Exception {
		return notifyScheMapper.selectNewMemberEventCnt();
	}

	public List<OneLoanExcuteTotal> selectLoanExcuteTotalFor7Days() throws Exception {
		return notifyScheMapper.selectLoanExcuteTotalFor7Days();
	}

	public boolean insertEmailReserveTarget(OneSendEmailInfo oneSendEmailInfo) throws Exception {
//	public boolean insertEmailReserveTarget(List<OneSendEmailInfo> oneSendEmailInfos) throws Exception {
		return notifyScheMapper.insertEmailReserveTarget(oneSendEmailInfo);
	}

	public List<OneSendEmail> selectAllUserEmailList() throws Exception  {
		return notifyScheMapper.selectAllUserEmailList();
	}

	public boolean insertSendEmailFail(OneSendEmailInfo onSendEmailInfo) throws Exception {
		return notifyScheMapper.insertSendEmailFail(onSendEmailInfo);
		
	}

	public int selectDuplicationCheck(String name, String string, String custId, String type, String string2) throws Exception {
		return notifyScheMapper.selectDuplicationCheck(name, string, custId, type, string2);
	}

	public boolean insertSendSMSFail(String name, String string, String custId, String type, String string2) throws Exception {
		return notifyScheMapper.insertSendSMSFail(name, string, custId, type, string2);
	}


}
