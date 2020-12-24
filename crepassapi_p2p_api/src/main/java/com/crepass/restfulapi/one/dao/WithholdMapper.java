package com.crepass.restfulapi.one.dao;

import org.apache.ibatis.annotations.Param;

import com.crepass.restfulapi.one.domain.OneWithhold;
import com.crepass.restfulapi.one.domain.OneWithholdAccount;

public interface WithholdMapper {
    
    public OneWithhold selectWithholdById(String mid) throws Exception;

    public OneWithholdAccount selectAccountById(String mid) throws Exception;

    public int updateWithdrawAccnt(String mid, String bankcode, String bankname, String bankacc) throws Exception;
    
    public boolean updateWithholdingInfo(String mid, String reginum, String m_with_zip, String m_with_addr1, String m_with_addr2) throws Exception;
    
    public boolean updateWithholdingInfo2(@Param("mid") String mid, @Param("reginum") String reginum) throws Exception;
    
    public String selectWithholdByInfo(String mid) throws Exception;
}
