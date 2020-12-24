package com.crepass.restfulapi.cre.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.cre.dao.TdairyMapper;
import com.crepass.restfulapi.cre.domain.CreTdairy;
import com.crepass.restfulapi.cre.domain.Tdairy;
import com.crepass.restfulapi.cre.domain.TdairyStatistics;
import com.crepass.restfulapi.cre.domain.TdairyWeeks;

@Service
public class TdairyService {

    @Autowired
    TdairyMapper tdairyMapper;
    
    @Autowired
    private CommonUtil commonUtil;

    public int insertTdairyById(JSONObject json) throws Exception {
        JSONObject request = (JSONObject) json.get("request");
        JSONArray array = request.getJSONArray("list");

        int rtnvalue = 0;
        for (int i = 0; i < array.length(); i++) {
            CreTdairy creTdairy = new CreTdairy();
            creTdairy.setMid(request.get("mid").toString());
            creTdairy.setActCd(array.getJSONObject(i).getString("actCd"));
            creTdairy.setOnOff(array.getJSONObject(i).getString("onOff"));
            
            if (array.getJSONObject(i).getString("onOff").equals("1")) {
                rtnvalue = tdairyMapper.insertTdairyById(creTdairy);              
            } else if (array.getJSONObject(i).getString("onOff").equals("0")) {
                rtnvalue = tdairyMapper.updateTdairyById(creTdairy); 
            }
        }
        
        return rtnvalue;
    }

    public List<Tdairy> selectTdairyById(String mid, String qdate) throws Exception {
        List<Tdairy> tdairy = tdairyMapper.selectTdairyById(mid, qdate);
        tdairy.add(new Tdairy("01", "0", 0));
        tdairy.add(new Tdairy("02", "0", 0));
        tdairy.add(new Tdairy("03", "0", 0));
        tdairy.add(new Tdairy("04", "0", 0));
        tdairy.add(new Tdairy("05", "0", 0));
        tdairy.add(new Tdairy("06", "0", 0));
        tdairy.add(new Tdairy("07", "0", 0));
            
        List<Tdairy> result = tdairy.stream()
                                    .filter(distinctByKey(e -> e.getActCd()))
                                    .distinct().collect(Collectors.toList());
        
        Collections.sort(result, (p1, p2) -> p1.getActCd().compareTo(p2.getActCd()));
        return result;
    }

    public List<TdairyStatistics> selectTdairyStatisticsById(String mid) throws Exception {
        List<TdairyStatistics> result = new ArrayList<TdairyStatistics>();
        
        List<TdairyWeeks> tdairy = new ArrayList<TdairyWeeks>();
        tdairy.add(new TdairyWeeks("01", 0, 0));
        tdairy.add(new TdairyWeeks("02", 0, 0));
        tdairy.add(new TdairyWeeks("03", 0, 0));
        tdairy.add(new TdairyWeeks("04", 0, 0));
        tdairy.add(new TdairyWeeks("05", 0, 0));
        tdairy.add(new TdairyWeeks("06", 0, 0));
        tdairy.add(new TdairyWeeks("07", 0, 0));

        tdairy.addAll(tdairyMapper.selectTdairyStatisticsById(mid));
        
        TdairyStatistics tdairyStatistics = null;
        String beforeActCd = null;
        
        Collections.sort(tdairy, (p1, p2) -> p1.getActCd().compareTo(p2.getActCd()));

        for (Object object : tdairy) {
            TdairyWeeks tdairyWeeks = (TdairyWeeks) object;
            
            if (tdairyWeeks.getActCd().equals(beforeActCd)) {
                result.remove(tdairyStatistics);
            } else {
                tdairyStatistics = new TdairyStatistics();
            }
            
            tdairyStatistics = (TdairyStatistics) commonUtil.getActDays(tdairyWeeks.getActCd(), tdairyWeeks.getDays(), tdairyWeeks.getWeekTime(), tdairyStatistics);
            beforeActCd = tdairyWeeks.getActCd();
            result.add(tdairyStatistics);
        }
        
        return result;
    }
    
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
    
}