package com.crepass.restfulapi.one.dao;

import java.util.List;

import com.crepass.restfulapi.one.domain.OneEmoneyBank;
import com.crepass.restfulapi.one.domain.OneEmoneyBankName;
import com.crepass.restfulapi.one.domain.OneEmoneyDetailHistory;
import com.crepass.restfulapi.one.domain.OneEmoneyDetailHistory2;
import com.crepass.restfulapi.one.domain.OneEmoneyInvestPay;

public interface EmoneyMapper {
    
    public List<?> selectEmoneyById(String mid) throws Exception;

    public double selectTrAmtBalanceById(String mid) throws Exception;

    public OneEmoneyBank selectEmoneyRetreive(String mid) throws Exception;
    
    public int updateEmoneyBankName(OneEmoneyBankName oneEmoneyBankName) throws Exception;

    public OneEmoneyInvestPay selectInvestProgressPay(String mid) throws Exception;
    
    public int updateEmoney(String emoney, String mid) throws Exception;
    
    public String selectEmoneyInvestIsPlaying(String mid) throws Exception;
    
    public int updateTopEmoney(String emoney, String mid) throws Exception;
    
    public String selectEmoneyWithdrawPay(String mid) throws Exception;
    
    public String selectEmoneyWithdrawPay2(String mid) throws Exception;
    
    public OneEmoneyInvestPay selectEmoneyPayInfo(String mid) throws Exception;
    
    public List<OneEmoneyDetailHistory> selectEmoneyDetailHistory(String mid) throws Exception;
    
    public List<OneEmoneyDetailHistory> selectEmoneyDetailHistory2(String mid, String typeFlag) throws Exception;
    
    public String selectEmoneyRepaymentBalance(String mid) throws Exception;
    
    public List<OneEmoneyDetailHistory2> selectEmoneyDetailHistoryRepayment(String mid) throws Exception;
    
    public String selectEmoneyInvestBalance(String mid) throws Exception;
    
    public String selectEmoneyInvestWithdrawPay(String mid) throws Exception;
    
}
