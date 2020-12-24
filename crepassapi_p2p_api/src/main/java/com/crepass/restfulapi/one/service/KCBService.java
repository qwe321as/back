package com.crepass.restfulapi.one.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crepass.restfulapi.one.dao.KCBMapper;
import com.crepass.restfulapi.one.domain.OneCreditInfo;
import com.crepass.restfulapi.one.domain.OneCustomerInfo;

@Service
public class KCBService {

    @Autowired
    KCBMapper kcbMapper;
    
    public OneCustomerInfo selectByCutomerInfo(String mid) throws Exception {
    	return kcbMapper.selectByCutomerInfo(mid);
    }
    
    public boolean insertCertifyKCB(String custId, String applyKey) throws Exception {
    	return kcbMapper.insertCertifyKCB(custId, applyKey);
    }
    
    public boolean updateCertifyKCBFlag(String applyKey) throws Exception {
    	return kcbMapper.updateCertifyKCBFlag(applyKey);
    }

//	public OneCreditInfo selectCreditKCBInfo(String custId) throws Exception {
//		return kcbMapper.selectCreditKCBInfo(custId);
//	}

	public OneCreditInfo selectCreditCrepassInfo(String mid) throws Exception {
		return kcbMapper.selectCreditCrepassInfo(mid);
	}
}
