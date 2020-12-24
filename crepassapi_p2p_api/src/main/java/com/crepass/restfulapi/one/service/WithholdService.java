package com.crepass.restfulapi.one.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crepass.restfulapi.one.dao.WithholdMapper;
import com.crepass.restfulapi.one.domain.OneWithhold;
import com.crepass.restfulapi.one.domain.OneWithholdAccount;

@Service
public class WithholdService {

    @Autowired
    WithholdMapper withholdMapper;
    
    public OneWithhold selectWithholdById(String mid) throws Exception {
        return withholdMapper.selectWithholdById(mid);
    }

    public OneWithholdAccount selectAccountById(String mid) throws Exception {
        return withholdMapper.selectAccountById(mid);
    }

    public int updateWithdrawAccnt(String mid, String bankcode, String bankname, String bankacc) throws Exception {
    	return withholdMapper.updateWithdrawAccnt(mid, bankcode, bankname, bankacc);
    }
    
    public boolean updateWithholdingInfo(String mid, String reginum, String m_with_zip, String m_with_addr1, String m_with_addr2) throws Exception {
    	return withholdMapper.updateWithholdingInfo(mid, reginum, m_with_zip, m_with_addr1, m_with_addr2);
    }
    
    public boolean updateWithholdingInfo2(String mid, String reginum) throws Exception {
    	return withholdMapper.updateWithholdingInfo2(mid, reginum);
    }
    
    public String selectWithholdByInfo(String mid) throws Exception {
    	return withholdMapper.selectWithholdByInfo(mid);
    }
}
