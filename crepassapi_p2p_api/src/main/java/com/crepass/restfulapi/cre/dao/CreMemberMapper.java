package com.crepass.restfulapi.cre.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.crepass.restfulapi.cre.domain.CreMember;
import com.crepass.restfulapi.cre.domain.CreMemberAgreed;
import com.crepass.restfulapi.one.domain.OneEventItem;

@Component
public interface CreMemberMapper {
    
    public int insertCreMember(CreMember creMember) throws Exception;

    public CreMember selectMemberById(String mid) throws Exception;

    public int deleteMemberById(String mid) throws Exception;

    public int deleteBackupMemberById(String mid) throws Exception;

    public int updateMemberById(String mid, String charType) throws Exception;

    public int insertCreMemberAgreed(CreMemberAgreed creMemberAgreed) throws Exception;
    
    public List<String> selectPushTarget() throws Exception;

    public int updateMemberByPlayerId(String mid, String playerId) throws Exception;

    // api v2 start
    public int insertAgreeList(Map<String, Object> map);
}
