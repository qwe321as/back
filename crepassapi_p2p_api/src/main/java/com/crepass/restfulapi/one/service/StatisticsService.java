package com.crepass.restfulapi.one.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.one.dao.StatisticsMapper;
import com.crepass.restfulapi.one.domain.OneStatistics;
import com.crepass.restfulapi.one.domain.OneStatisticsInvest;
import com.crepass.restfulapi.v2.domain.InvestMember;
import com.crepass.restfulapi.v2.domain.LoanMember;
import com.crepass.restfulapi.v2.domain.MemberInvestInfo;

@Service
public class StatisticsService {

    @Autowired
    StatisticsMapper statisticsMapper;
    
    @Autowired
    CommonUtil commonUtil;
    
    public OneStatistics selectStatisticsById(String mid) throws Exception {
        
        OneStatistics oneStatistics = statisticsMapper.selectStatisticsById(mid);
        
        Map<String, Object> result = new HashMap<String, Object>();
        
        result = commonUtil.getAmountUnit(oneStatistics.getSaveLoanAmt());
        oneStatistics.setSaveLoanAmt(Double.parseDouble(result.get("amt").toString()));
        oneStatistics.setSaveLoanAmtUnit(result.get("unit").toString());
        
        result = commonUtil.getAmountUnit(oneStatistics.getSavePayAmt());
        oneStatistics.setSavePayAmt(Double.parseDouble(result.get("amt").toString()));
        oneStatistics.setSavePayAmtUnit(result.get("unit").toString());

        result = commonUtil.getAmountUnit(oneStatistics.getLoanBalance());
        oneStatistics.setLoanBalance(Double.parseDouble(result.get("amt").toString()));
        oneStatistics.setLoanBalanceUnit(result.get("unit").toString());
 
        return oneStatistics;
    }
    
    public OneStatistics selectStatisticsWebById() throws Exception {
        
        OneStatistics oneStatistics = statisticsMapper.selectStatisticsWebById();
        
        Map<String, Object> result = new HashMap<String, Object>();
        
        result = commonUtil.getAmountUnit(oneStatistics.getSaveLoanAmt());
        oneStatistics.setSaveLoanAmt(Double.parseDouble(result.get("amt").toString()));
        oneStatistics.setSaveLoanAmtUnit(result.get("unit").toString());
        
        result = commonUtil.getAmountUnit(oneStatistics.getSavePayAmt());
        oneStatistics.setSavePayAmt(Double.parseDouble(result.get("amt").toString()));
        oneStatistics.setSavePayAmtUnit(result.get("unit").toString());

        result = commonUtil.getAmountUnit(oneStatistics.getLoanBalance());
        oneStatistics.setLoanBalance(Double.parseDouble(result.get("amt").toString()));
        oneStatistics.setLoanBalanceUnit(result.get("unit").toString());
 
        return oneStatistics;
    }
    
    public String selectStatisticsIsInvest(String mid) throws Exception {
    	return statisticsMapper.selectStatisticsIsInvest(mid);
    }
    
    public OneStatisticsInvest selectStatisticsInvest(String mid) throws Exception {
    	return statisticsMapper.selectStatisticsInvest(mid);
    }
    
    
    // api v2 start
    public InvestMember getInvestMember(String mid) throws Exception {
		return statisticsMapper.getInvestMember(mid);
	}
    
    public LoanMember getLoanMember(String mid) throws Exception {
		return statisticsMapper.getLoanMember(mid);
	}
    
    public MemberInvestInfo selectMemberInvestInfo(String mid) throws Exception {
    	return statisticsMapper.selectMemberInvestInfo(mid);
    }
}
