package com.crepass.restfulapi.one.dao;

import java.util.List;

import com.crepass.restfulapi.one.domain.MariMember;
import com.crepass.restfulapi.one.domain.OneCertify;
import com.crepass.restfulapi.one.domain.OneCertifyWebDump;
import com.crepass.restfulapi.one.domain.OneMember;
import com.crepass.restfulapi.one.domain.OneMemberConfirm;
import com.crepass.restfulapi.one.domain.OneMemberCustAddInfo;
import com.crepass.restfulapi.one.domain.OneMemberCustAddInfo2;
import com.crepass.restfulapi.one.domain.OneMemberCustId;
import com.crepass.restfulapi.one.domain.OneSeyfert;
import com.crepass.restfulapi.v2.domain.Member;
import com.crepass.restfulapi.v2.domain.MemberInfo;
import com.crepass.restfulapi.v2.domain.MemberInvestInfo;
import com.crepass.restfulapi.v2.domain.MemberModInfo;

public interface OneMemberMapper {
    
    public int insertOneMember(OneMember oneMember) throws Exception;
    
    public int insertOneSeyfert(OneSeyfert oneSeyfert) throws Exception;
    
    public String selectMemberIdCheck(String memberId) throws Exception;
    
    public MariMember selectMemberById(String memberId) throws Exception;

    public String checkPasswordById(String memberId, String password) throws Exception;

    public int updateMemberById(OneMember oneMember) throws Exception;
    
    public int deleteMemberById(String memberId) throws Exception;

    public int deleteBackupMemberById(String memberId, String ip) throws Exception;

    public String selectCustSeq() throws Exception;
    
    public int updateMemberMoney(String m_emoney, String mid) throws Exception;
    
    public String selectByCustIdToMid(String custId) throws Exception;
    
    public String selectByLoanIdToMid(String loanId) throws Exception;
    
    public String selectCustID(String mid) throws Exception;
    
    public OneMemberCustId selectCustID2(String mid) throws Exception;
    
    public String selectUserConfirm(String mid, String mpw) throws Exception;
    
    public OneMemberCustAddInfo selectCustAddInfo(String mid) throws Exception;
    
    public OneMemberCustAddInfo2 selectCustAddInfo2(String mid) throws Exception;
 
    public boolean insertOneCertify(OneCertify oneCertify) throws Exception;
    
    public List<String> selectCustConfirm(OneMemberConfirm oneMemberConfirm) throws Exception;
    
    public OneCertifyWebDump selectCertifyWebDump(String bi) throws Exception;
    
    public boolean deleteCertifyWebDump(String bi) throws Exception;
    
    public String selectAutoInvestAgree(String mid) throws Exception;
    
    public String selectUserAuthToken(String authToken) throws Exception;
    
    public String selectUserIdCheck(String mid) throws Exception;
    
    public List<String> selectTradeCheck(String mid) throws Exception;
    
    public boolean updateMemberAuthToken(String mid, String token) throws Exception;
    
    public boolean updateMemberPW(String mid, String pw) throws Exception;
    
    // api v2 start
    public int isDuplicate(Member member) throws Exception;
    
    public int insertMember(Member member) throws Exception;
    
    public MemberInfo getMember(String mid);
    
    public boolean updateNickName(Member member);
    
    public boolean updateMember(Member member) throws Exception;
    
    public MemberModInfo selectMemberModInfo(String mid) throws Exception;
    
    public MemberInvestInfo selectMemberInvestInfo(String mid) throws Exception;

	public int insertEmailValidation(String mid, String emailToken) throws Exception;

	public Member selectMemberByPhone(String mobileCorp, String hp) throws Exception;

}
