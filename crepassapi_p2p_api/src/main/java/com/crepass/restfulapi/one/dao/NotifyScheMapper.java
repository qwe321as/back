package com.crepass.restfulapi.one.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

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

public interface NotifyScheMapper {
    
    public List<OneRepaymentManage> selectRepaymentManageNomal() throws Exception;
    
    public List<OneRepaymentManage> selectRepaymentManageOrverDue() throws Exception;
    
    public List<OneRepaymentManage> selectRepaymentManagePrePay() throws Exception;
    
    public List<OneRepaymentUserInfo> selectRepaymentUserInfo(String today) throws Exception;
    
    public List<OneRepaymentUserInfo> selectRepaymentOrverdueUserInfo(String today, String useType) throws Exception;
    
    public boolean insertSendSMS(String name, String type, String custId, String useFlag, String request) throws Exception;
    
    public List<OneSendSMS> selectSendRepaymentSMS() throws Exception;
    
    public boolean updateSendSMS(String createdDt, String name, String type, String batchFlag, String response, String cmid) throws Exception;
    
    public List<OneSendSMSByCMID> selectSendSMSByCMID() throws Exception;
    
    public boolean updateSendSMSResult(String createdDt, String cmid, String result) throws Exception;
    
    public List<OneStartInvestUserInfo> selectLoanCancelInvestUser() throws Exception;
    
    public String selectIsSmsSendingInvestUser(String custId, String subject) throws Exception;
    
    public List<String> selectRepaymentByLoanId() throws Exception;
    
    public String selectBalanceRepayment(String loanId) throws Exception;
    
    public OneNotiRepaymentUserInfo selectNotiRepaymentUserInfo(String loanId) throws Exception;
    
    public String selectNotiRepaymentIsPossible(String loanId, String today) throws Exception;
    
    public List<OneSendEmailInfo> selectSendEmailInfo() throws Exception;
    
    public OneSendEmailContents selectSendEmailContents(long seq) throws Exception;
    
    public boolean updateSendEmailState(OneSendEmailInfo sendEmailInfo) throws Exception;
    
    public List<String> selectEmailFileName(long seq) throws Exception;
    
    public boolean insertEmailHistory(String seq, String emailTitle, String emailBody) throws Exception;
    
    public boolean insertEmailTarget(List<OneSendEmailInfo> oneSendEmailInfos) throws Exception;
    
    public boolean insertEmailTarget2(List<OneSendEmailInfo> oneSendEmailInfos) throws Exception;
    
    public List<String> selectPossibleInvest(@Param("mid") String mid) throws Exception;

	public OneNewMemberEventCnt selectNewMemberEventCnt() throws Exception;

	public List<OneLoanExcuteTotal> selectLoanExcuteTotalFor7Days() throws Exception;

	public boolean insertEmailReserveTarget(OneSendEmailInfo oneSendEmailInfos) throws Exception;

	public List<OneSendEmail> selectAllUserEmailList() throws Exception;

	public boolean insertSendEmailFail(OneSendEmailInfo onSendEmailInfo) throws Exception;

	public int selectDuplicationCheck(String name, String string, String custId, String type, String string2) throws Exception;

	public boolean insertSendSMSFail(String name, String string, String custId, String type, String string2) throws Exception;
	
	
	
}
