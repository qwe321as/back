package com.crepass.restfulapi.one.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crepass.restfulapi.one.dao.WishMapper;
import com.crepass.restfulapi.v2.domain.LoansVO;
import com.crepass.restfulapi.v2.domain.LoansVO2;

@Service
public class WishService {

    @Autowired
    WishMapper wishMapper;
    
    public List<?> selectWishById(String mid) throws Exception {
        return wishMapper.selectWishById(mid);
    }

    public int deleteWishById(JSONObject json) throws Exception {
        JSONObject request = (JSONObject) json.get("request");
        JSONArray array = request.getJSONArray("list");
        
        Map<String, Object> paramMap = new HashMap<String, Object>();
        
        paramMap.put("mid", request.get("mid").toString());
        
        Map<String, Object> innerMap;
        List<Map<String, Object>> loanList = new ArrayList<Map<String, Object>>();
        
        for (int i = 0; i < array.length(); i++) {
            innerMap = new HashMap<String, Object>();
            innerMap.put("loanid", array.getJSONObject(i).getString("loanId"));
            loanList.add(innerMap);
        }
        paramMap.put("loanList", loanList);

        int rtnvalue = wishMapper.deleteWishById(paramMap);              
        
        return rtnvalue;
    }
    
    public int insertWishById(JSONObject json) throws Exception {
        JSONObject request = (JSONObject) json.get("request");
        JSONArray array = request.getJSONArray("list");

        int rtnvalue = 0;
        for (int i = 0; i < array.length(); i++) {
            rtnvalue = wishMapper.insertWishById(request.get("mid").toString(), array.getJSONObject(i).getString("loanId"));              
        }
        
        return rtnvalue;
    }
    
    public List<LoansVO> selectWishById2(int pageNum, int pageSize, String mid) throws Exception {
    	return wishMapper.selectWishById2(pageNum, pageSize, mid);
    }
    
    public List<LoansVO2> selectWishById2_1(int pageNum, int pageSize, String mid) throws Exception {
    	return wishMapper.selectWishById2_1(pageNum, pageSize, mid);
    }
    
    public int selectWishByIdSize(String mid) throws Exception {
		return wishMapper.selectWishByIdSize(mid);
	}
}
