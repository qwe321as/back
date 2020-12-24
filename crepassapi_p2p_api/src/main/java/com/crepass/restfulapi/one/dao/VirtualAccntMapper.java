package com.crepass.restfulapi.one.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.crepass.restfulapi.one.domain.OneInvest;
import com.crepass.restfulapi.one.domain.OneLoanVirtualAccntInfo;
import com.crepass.restfulapi.one.domain.OneOutPay;
import com.crepass.restfulapi.one.domain.OneSeyfertyVirtual;
import com.crepass.restfulapi.one.domain.OneSeyfertyVirtualUpdate;
import com.crepass.restfulapi.one.domain.OneVirtualAccnt;
import com.crepass.restfulapi.one.domain.OneVirtualAccntWithdraw;
import com.crepass.restfulapi.one.domain.OneVirtualRealAccnt;

@Component
public interface VirtualAccntMapper {
    
	public String selectVirtualaccnt(String mid) throws Exception;
	
    public OneVirtualAccnt selectVirtualAccntInfo() throws Exception;
    
    public OneVirtualAccnt selectVirtualAccntLoanInfo() throws Exception;

    public int updateMemberAccnt(OneSeyfertyVirtual oneSeyfertyVirtual) throws Exception;
    
    public int updateMemberLoanAccnt(OneSeyfertyVirtualUpdate oneSeyfertyVirtualUpdate) throws Exception;
    
    public int updateVirtualaccntUse(String account) throws Exception;
    
    public OneVirtualRealAccnt selectAccountById(String mid) throws Exception;
    
    public int insertAccntWithdraw(OneInvest oneInvest) throws Exception;
    
    public int insertAccntWithdrawHistory(OneOutPay oneOutPay) throws Exception;
    
    public int updateSeyfertOrder(String s_tid, String s_refId) throws Exception;
    
    public int updateOutPay(String o_refId) throws Exception;
    
    public int updateUserMoney(String mid, String pay) throws Exception;
    
    public boolean insertAccntWithdrawSchedule(String mid, String trxAmt, String typeFlag) throws Exception;
    
    public boolean insertAccntWithdrawSchedule2(String mid, String loanId, String trxAmt, String typeFlag) throws Exception;
    
    public boolean insertAccntWithdrawSchedule3(String mid, String loanId, String trxAmt, String typeFlag, String trxFlag) throws Exception;
    
    public List<OneVirtualAccntWithdraw> selectAccntWithdrawSchedule() throws Exception;
    
    public boolean updateWithdrawSchedule(String mid, String trxFlag, String trxNo) throws Exception;
   
    public String selectBankById(String bankCode) throws Exception;
    
    public OneLoanVirtualAccntInfo selectLoanVirtualaccnt(String loanId) throws Exception;

	public boolean insertAccntWithdrawSchedule4(String mid, String loanId, String sumPayln, String typeFlag, String repayCount) throws Exception;

	public List<OneVirtualAccntWithdraw> selectAccntWithdrawSchedule_temp() throws Exception;
}
