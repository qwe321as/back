package com.crepass.restfulapi.one.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.crepass.restfulapi.one.domain.OneBankAccountInfo;
import com.crepass.restfulapi.v2.domain.MemberAccntInfo;
import com.crepass.restfulapi.v2.domain.WithdrawBankInfo;

@Component
public interface BankAccntInfoMapper {
    
	public List<OneBankAccountInfo> selectBankAccntInfo() throws Exception;
    
	public boolean updateMemberBankInfo(MemberAccntInfo memberAccntInfo) throws Exception;
	
	public String selectBankAccntBalance(String mid) throws Exception;
	
	public WithdrawBankInfo selectWithdrawBankInfo(String mid) throws Exception;
}
