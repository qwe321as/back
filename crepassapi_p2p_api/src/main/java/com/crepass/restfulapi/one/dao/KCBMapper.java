package com.crepass.restfulapi.one.dao;

import java.util.List;

import com.crepass.restfulapi.one.domain.OneCreditInfo;
import com.crepass.restfulapi.one.domain.OneCustomerInfo;

public interface KCBMapper {
    
    public OneCustomerInfo selectByCutomerInfo(String mid) throws Exception;

    public boolean insertCertifyKCB(String custId, String applyKey) throws Exception;
    
    public boolean updateCertifyKCBFlag(String applyKey) throws Exception;

//	public OneCreditInfo selectCreditKCBInfo(String custId) throws Exception;

	public OneCreditInfo selectCreditCrepassInfo(String mid) throws Exception;
	
}
