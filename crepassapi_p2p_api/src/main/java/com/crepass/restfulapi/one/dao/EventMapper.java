package com.crepass.restfulapi.one.dao;

import java.util.List;

import com.crepass.restfulapi.one.domain.OneEventDiscount;
import com.crepass.restfulapi.one.domain.OneEventItem;
import com.crepass.restfulapi.one.domain.OneEventJoin;

public interface EventMapper {
    
    public List<OneEventItem> selectEventList() throws Exception;
    
    public OneEventJoin selectEventJoin(String mid) throws Exception;
    
    public boolean updateEventJoinState(String mid) throws Exception;
    
    public boolean insertEventJoinAdd(String mid, String eventCode) throws Exception;
    
    public OneEventDiscount selectEventDiscount(String mid, String regdatetime) throws Exception;
 
    public String selectEventIsAddById(String mid, String eventCode) throws Exception;
    
    public String selectIsEvent(String mid) throws Exception;

	public List<com.crepass.restfulapi.v2.domain.OneEventItem> selectEventListV2() throws Exception;

	public com.crepass.restfulapi.v2.domain.OneEventItem selectEventDetailV2(String event_code) throws Exception;

	public int selectEventListCount() throws Exception;
}
