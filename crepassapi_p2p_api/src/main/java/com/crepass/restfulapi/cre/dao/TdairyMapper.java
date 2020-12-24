package com.crepass.restfulapi.cre.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.crepass.restfulapi.cre.domain.CreTdairy;
import com.crepass.restfulapi.cre.domain.Tdairy;
import com.crepass.restfulapi.cre.domain.TdairyWeeks;

@Component
public interface TdairyMapper {
    
    public int insertTdairyById(CreTdairy creTdairy) throws Exception;

    public List<Tdairy> selectTdairyById(String mid, String qdate) throws Exception;

    public int updateTdairyById(CreTdairy creTdairy) throws Exception;

    public List<TdairyWeeks> selectTdairyStatisticsById(String mid) throws Exception;

}
