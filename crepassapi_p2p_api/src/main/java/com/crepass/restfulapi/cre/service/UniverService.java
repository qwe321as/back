package com.crepass.restfulapi.cre.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crepass.restfulapi.cre.dao.UniverMapper;

@Service
public class UniverService {

    @Autowired
    UniverMapper univerMapper;
    
    public List<?> selectUniverList() throws Exception {
        return univerMapper.selectUniverList();
    }
    
    public List<?> selectUniverMajor(String schoolName) throws Exception {
        return univerMapper.selectUniverMajor(schoolName);
    }

}