package com.crepass.restfulapi.one.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crepass.restfulapi.one.dao.VirtualAccntMapper;
import com.crepass.restfulapi.one.domain.OneInvest;
import com.crepass.restfulapi.one.domain.OneLoanVirtualAccntInfo;
import com.crepass.restfulapi.one.domain.OneOutPay;
import com.crepass.restfulapi.one.domain.OneSeyfertyVirtual;
import com.crepass.restfulapi.one.domain.OneSeyfertyVirtualUpdate;
import com.crepass.restfulapi.one.domain.OneVirtualAccnt;
import com.crepass.restfulapi.one.domain.OneVirtualAccntWithdraw;
import com.crepass.restfulapi.one.domain.OneVirtualRealAccnt;

@Service
public class VirtualAccntService {

    @Autowired
    VirtualAccntMapper virtualAccntMapper;
    
    public String selectVirtualaccnt(String mid) throws Exception {
        return virtualAccntMapper.selectVirtualaccnt(mid);
    }
    
    public OneVirtualAccnt selectVirtualAccntInfo() throws Exception {
        return virtualAccntMapper.selectVirtualAccntInfo();
    }
    
    public OneVirtualAccnt selectVirtualAccntLoanInfo() throws Exception {
        return virtualAccntMapper.selectVirtualAccntLoanInfo();
    }
    
    public int updateMemberAccnt(OneSeyfertyVirtual oneSeyfertyVirtual) throws Exception {
    	return virtualAccntMapper.updateMemberAccnt(oneSeyfertyVirtual);
    }
    
    public int updateMemberLoanAccnt(OneSeyfertyVirtualUpdate oneSeyfertyVirtualUpdate) throws Exception {
    	return virtualAccntMapper.updateMemberLoanAccnt(oneSeyfertyVirtualUpdate);
    }
    
    public int updateVirtualaccntUse(String account) throws Exception {
    	return virtualAccntMapper.updateVirtualaccntUse(account);
    }
    
    public OneVirtualRealAccnt selectAccountById(String mid) throws Exception {
    	return virtualAccntMapper.selectAccountById(mid);
    }
    
    public int insertAccntWithdraw(OneInvest oneInvest) throws Exception {
    	return virtualAccntMapper.insertAccntWithdraw(oneInvest);
    }
    
    public int insertAccntWithdrawHistory(OneOutPay oneOutPay) throws Exception {
    	return virtualAccntMapper.insertAccntWithdrawHistory(oneOutPay);
    }
    
    public int updateSeyfertOrder(String s_tid, String s_refId) throws Exception {
    	return virtualAccntMapper.updateSeyfertOrder(s_tid, s_refId);
    }
    
    public int updateOutPay(String o_refId) throws Exception {
    	return virtualAccntMapper.updateOutPay(o_refId);
    }
    
    public int updateUserMoney(String mid, String pay) throws Exception {
    	return virtualAccntMapper.updateUserMoney(mid, pay);
    }
    
    public boolean insertAccntWithdrawSchedule(String mid, String trxAmt, String typeFlag) throws Exception {
    	return virtualAccntMapper.insertAccntWithdrawSchedule(mid, trxAmt, typeFlag);
    }
    
    public boolean insertAccntWithdrawSchedule2(String mid, String loanId, String trxAmt, String typeFlag) throws Exception {
    	return virtualAccntMapper.insertAccntWithdrawSchedule2(mid, loanId, trxAmt, typeFlag);
    }
    
    public boolean insertAccntWithdrawSchedule3(String mid, String loanId, String trxAmt, String typeFlag, String trxFlag) throws Exception {
    	return virtualAccntMapper.insertAccntWithdrawSchedule3(mid, loanId, trxAmt, typeFlag, trxFlag);
    }
    
    public List<OneVirtualAccntWithdraw> selectAccntWithdrawSchedule() throws Exception {
    	return virtualAccntMapper.selectAccntWithdrawSchedule();
    }
    
    public boolean updateWithdrawSchedule(String mid, String trxFlag, String trxNo) throws Exception {
    	return virtualAccntMapper.updateWithdrawSchedule(mid, trxFlag, trxNo);
    }
    
    public String selectBankById(String bankCode) throws Exception {
    	return virtualAccntMapper.selectBankById(bankCode);
    }
    
    public OneLoanVirtualAccntInfo selectLoanVirtualaccnt(String loanId) throws Exception {
    	return virtualAccntMapper.selectLoanVirtualaccnt(loanId);
    }

	public boolean insertAccntWithdrawSchedule4(String mid, String loanId, String sumPayln, String typeFlag, String repayCount) throws Exception {
		return virtualAccntMapper.insertAccntWithdrawSchedule4(mid, loanId, sumPayln, typeFlag, repayCount);
}

	public List<OneVirtualAccntWithdraw> selectAccntWithdrawSchedule_temp() throws Exception {
		return virtualAccntMapper.selectAccntWithdrawSchedule_temp();
	}
}
