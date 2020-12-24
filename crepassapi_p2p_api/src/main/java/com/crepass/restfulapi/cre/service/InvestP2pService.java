package com.crepass.restfulapi.cre.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crepass.restfulapi.cre.dao.InvestP2pMapper;
import com.crepass.restfulapi.inside.domain.InsideIPJIInfo;


@Service
public class InvestP2pService {

    @Autowired
    InvestP2pMapper investP2pMapper;

	public List<InsideIPJIInfo> selectP2pInvestInfo(String custId) throws Exception {
		return investP2pMapper.selectP2pInvestInfo(custId);
	}
}
