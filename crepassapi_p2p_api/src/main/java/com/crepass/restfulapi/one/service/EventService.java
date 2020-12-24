package com.crepass.restfulapi.one.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crepass.restfulapi.one.dao.EventMapper;
import com.crepass.restfulapi.one.domain.OneEventDiscount;
import com.crepass.restfulapi.one.domain.OneEventItem;
import com.crepass.restfulapi.one.domain.OneEventJoin;

@Service
public class EventService {

    @Autowired
    EventMapper eventMapper;
    
    public List<OneEventItem> selectEventList() throws Exception {
    	return eventMapper.selectEventList();
    }
    
    public OneEventJoin selectEventJoin(String mid) throws Exception {
    	return eventMapper.selectEventJoin(mid);
    }
    
    public boolean updateEventJoinState(String mid) throws Exception {
    	return eventMapper.updateEventJoinState(mid);
    }
    
    public boolean insertEventJoinAdd(String mid, String eventCode) throws Exception {
    	return eventMapper.insertEventJoinAdd(mid, eventCode);
    }
    
    public OneEventDiscount selectEventDiscount(String mid, String regdatetime) throws Exception {
    	return eventMapper.selectEventDiscount(mid, regdatetime);
    }
    
    public String selectEventIsAddById(String mid, String eventCode) throws Exception {
    	return eventMapper.selectEventIsAddById(mid, eventCode);
    }
    
    public String selectIsEvent(String mid) throws Exception {
    	return eventMapper.selectIsEvent(mid);
    }

	public List<com.crepass.restfulapi.v2.domain.OneEventItem> selectEventListV2() throws Exception {
		return eventMapper.selectEventListV2();
	}

	public com.crepass.restfulapi.v2.domain.OneEventItem 
			selectEventDetailV2(String event_code) throws Exception {
		return eventMapper.selectEventDetailV2(event_code);
	}

	public int selectEventListCount() throws Exception {
		return eventMapper.selectEventListCount();
	}
}
