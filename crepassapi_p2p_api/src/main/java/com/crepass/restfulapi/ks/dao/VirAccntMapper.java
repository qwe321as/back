package com.crepass.restfulapi.ks.dao;

import org.springframework.stereotype.Component;

import com.crepass.restfulapi.ks.domain.VirAccnt;

@Component
public interface VirAccntMapper {
    
	public VirAccnt selectVirAccntInfo(String vrAccnt) throws Exception;
	
	public int updateVirAccntUse(String vrAccnt, String corpName, String custId) throws Exception;
	
	public boolean updateVirAccntExpire(String vrAccnt) throws Exception;
	
}
