package com.crepass.restfulapi.cre.dao;

import org.springframework.stereotype.Component;

import com.crepass.restfulapi.cre.domain.CreAgree;
import com.crepass.restfulapi.cre.domain.CreDocument;
import com.crepass.restfulapi.cre.domain.CreInvestAgreed;
import com.crepass.restfulapi.cre.domain.CreLoanAgreed;
import com.crepass.restfulapi.cre.domain.CreLoanAgreed2;
import com.crepass.restfulapi.cre.domain.CreSetting;

@Component
public interface AgreeMapper {
    
    public int insertCreAgree(CreAgree creAgree) throws Exception;

    public int updateCreSetting(CreSetting creSetting) throws Exception;

    public int insertDocument(CreDocument creDocument) throws Exception;

    public CreDocument selectDocumentById(String docCode) throws Exception;

    public void insertCreLoanAgreed(CreLoanAgreed creLoanAgreed) throws Exception;
    
    public void insertCreLoanAgreed2(CreLoanAgreed2 creLoanAgreed2) throws Exception;

    public void insertCreInvestAgreed(CreInvestAgreed creInvestAgreed) throws Exception;

}
