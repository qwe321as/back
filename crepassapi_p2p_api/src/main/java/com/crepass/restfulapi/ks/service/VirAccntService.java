package com.crepass.restfulapi.ks.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crepass.restfulapi.ks.dao.VirAccntMapper;
import com.crepass.restfulapi.ks.domain.VirAccnt;

@Service
public class VirAccntService {

    @Autowired
    VirAccntMapper virAccntMapper;
    
    public VirAccnt selectVirAccntInfo(String VR_ACCT_NO) throws Exception {
    	return virAccntMapper.selectVirAccntInfo(VR_ACCT_NO);
    }
    
    public int updateVirAccntUse(String vrAccnt, String corpName, String custId) throws Exception {
    	return virAccntMapper.updateVirAccntUse(vrAccnt, corpName, custId);
    }
    
    public boolean updateVirAccntExpire(String vrAccnt) throws Exception {
    	return virAccntMapper.updateVirAccntExpire(vrAccnt);
    }
}
