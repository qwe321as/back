package com.crepass.restfulapi.one.dao;

import com.crepass.restfulapi.one.domain.OneStatistics;
import com.crepass.restfulapi.one.domain.OneStatisticsInvest;
import com.crepass.restfulapi.v2.domain.InvestMember;
import com.crepass.restfulapi.v2.domain.LoanMember;
import com.crepass.restfulapi.v2.domain.MemberInvestInfo;

public interface StatisticsMapper {
    
    public OneStatistics selectStatisticsById(String mid) throws Exception;
    
    public OneStatistics selectStatisticsWebById() throws Exception;

    public String selectStatisticsIsInvest(String mid) throws Exception;
    
    public OneStatisticsInvest selectStatisticsInvest(String mid) throws Exception;
    
    
    // api v2 start
    public InvestMember getInvestMember(String mid) throws Exception;
    
    public LoanMember getLoanMember(String mid) throws Exception;
    
    public MemberInvestInfo selectMemberInvestInfo(String mid) throws Exception;
}
