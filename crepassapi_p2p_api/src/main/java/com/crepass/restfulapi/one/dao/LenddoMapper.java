package com.crepass.restfulapi.one.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.crepass.restfulapi.one.domain.LenddoInterface;
import com.crepass.restfulapi.one.domain.OneLenddoWebhookInfo;

@Component
public interface LenddoMapper {
    
    public int insertLenddoTrans(LenddoInterface lenddoTrans) throws Exception;

    public String selectLenddoById(String appId) throws Exception;
    
    public boolean insertLenddoSendHistory(String appId, String mid) throws Exception;
    
    public boolean insertLenddoWebhoook(String appId, String time) throws Exception;
    
    public boolean deleteLenddoWebhoook(String appId) throws Exception;
    
    public List<OneLenddoWebhookInfo> selectLenddoWebhoook() throws Exception;
    
}
