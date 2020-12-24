package com.crepass.restfulapi.cre.dao;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public interface UniverMapper {
    
    public List<?> selectUniverList() throws Exception;

    public List<?> selectUniverMajor(String schoolName) throws Exception;

}
