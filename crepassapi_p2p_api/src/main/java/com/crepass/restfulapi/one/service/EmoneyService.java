package com.crepass.restfulapi.one.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crepass.restfulapi.one.dao.EmoneyMapper;
import com.crepass.restfulapi.one.domain.OneEmoneyBank;
import com.crepass.restfulapi.one.domain.OneEmoneyBankName;
import com.crepass.restfulapi.one.domain.OneEmoneyDetailHistory;
import com.crepass.restfulapi.one.domain.OneEmoneyDetailHistory2;
import com.crepass.restfulapi.one.domain.OneEmoneyInvestPay;

@Service
public class EmoneyService {

    @Autowired
    EmoneyMapper emoneyMapper;
    
    public List<?> selectEmoneyById(String mid) throws Exception {
        return emoneyMapper.selectEmoneyById(mid);
    }

    public double selectTrAmtBalanceById(String mid) throws Exception {
        return emoneyMapper.selectTrAmtBalanceById(mid);
    }

    public OneEmoneyBank selectEmoneyRetreive(String mid) throws Exception {
        return emoneyMapper.selectEmoneyRetreive(mid);
    }

    public int updateEmoneyBankName(OneEmoneyBankName oneEmoneyBankName) throws Exception {
    	return emoneyMapper.updateEmoneyBankName(oneEmoneyBankName);
//        return emoneyMapper.updateEmoneyBankName(oneEmoneyBankName);
    }
    
    public OneEmoneyInvestPay selectInvestProgressPay(String mid) throws Exception {
    	return emoneyMapper.selectInvestProgressPay(mid);
    }
    
    public int updateEmoney(String emoney, String mid) throws Exception {
    	return emoneyMapper.updateEmoney(emoney, mid);
    }
    
    public String selectEmoneyInvestIsPlaying(String mid) throws Exception {
    	return emoneyMapper.selectEmoneyInvestIsPlaying(mid);
    }
    
    public int updateTopEmoney(String emoney, String mid) throws Exception {
    	return emoneyMapper.updateTopEmoney(emoney, mid);
    }
    
    public String selectEmoneyWithdrawPay(String mid) throws Exception {
    	return emoneyMapper.selectEmoneyWithdrawPay(mid);
    }
    
    public String selectEmoneyWithdrawPay2(String mid) throws Exception {
    	return emoneyMapper.selectEmoneyWithdrawPay2(mid);
    }
    
    public OneEmoneyInvestPay selectEmoneyPayInfo(String mid) throws Exception {
    	return emoneyMapper.selectEmoneyPayInfo(mid);
    }
    
    public List<OneEmoneyDetailHistory> selectEmoneyDetailHistory(String mid) throws Exception {
    	return emoneyMapper.selectEmoneyDetailHistory(mid);
    }
    
    public List<OneEmoneyDetailHistory> selectEmoneyDetailHistory2(String mid, String typeFlag) throws Exception {
    	return emoneyMapper.selectEmoneyDetailHistory2(mid, typeFlag);
    }
    
    public String selectEmoneyRepaymentBalance(String mid) throws Exception {
    	return emoneyMapper.selectEmoneyRepaymentBalance(mid);
    }
    
    public List<OneEmoneyDetailHistory2> selectEmoneyDetailHistoryRepayment(String mid) throws Exception {
    	return emoneyMapper.selectEmoneyDetailHistoryRepayment(mid);
    }
    
    public String selectEmoneyInvestBalance(String mid) throws Exception {
    	return emoneyMapper.selectEmoneyInvestBalance(mid);
    }
    
    public String selectEmoneyInvestWithdrawPay(String mid) throws Exception {
    	return emoneyMapper.selectEmoneyInvestWithdrawPay(mid);
    }
}
