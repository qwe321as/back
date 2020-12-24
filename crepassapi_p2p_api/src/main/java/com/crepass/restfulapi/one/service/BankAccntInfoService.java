package com.crepass.restfulapi.one.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crepass.restfulapi.one.dao.BankAccntInfoMapper;
import com.crepass.restfulapi.one.domain.OneBankAccountInfo;
import com.crepass.restfulapi.v2.domain.MemberAccntInfo;
import com.crepass.restfulapi.v2.domain.WithdrawBankInfo;

@Service
public class BankAccntInfoService {

    @Autowired
    BankAccntInfoMapper bankAccntInfoMapper;
    
    public List<OneBankAccountInfo> selectBankAccntInfo() throws Exception {
    	return bankAccntInfoMapper.selectBankAccntInfo();
    }
    
    public boolean updateMemberBankInfo(MemberAccntInfo memberAccntInfo) throws Exception {
    	return bankAccntInfoMapper.updateMemberBankInfo(memberAccntInfo);
    }
    
    public String selectBankAccntBalance(String mid) throws Exception {
    	return bankAccntInfoMapper.selectBankAccntBalance(mid);
    }
    
    public WithdrawBankInfo selectWithdrawBankInfo(String mid) throws Exception {
    	return bankAccntInfoMapper.selectWithdrawBankInfo(mid);
    }
}
