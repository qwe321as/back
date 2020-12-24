package com.crepass.restfulapi.cre.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.crepass.restfulapi.cre.domain.CreSocial;
import com.crepass.restfulapi.cre.domain.CreSocialBanner;
import com.crepass.restfulapi.inside.domain.InsideIPJIInfo;

@Component
public interface InvestP2pMapper {

	List<InsideIPJIInfo> selectP2pInvestInfo(String custId) throws Exception;
	
}
