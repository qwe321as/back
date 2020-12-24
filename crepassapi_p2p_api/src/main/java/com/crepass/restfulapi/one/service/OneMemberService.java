package com.crepass.restfulapi.one.service;

import java.io.File;
import java.util.List;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.json.JSONObject;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.one.dao.OneMemberMapper;
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
import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OneMemberService {

    @Autowired
    private OneMemberMapper oneMemberMapper;
    
    @Autowired
    private CommonUtil commonUtil;

    public int addOneMember(JSONObject jsonMember) throws Exception {

        JSONObject request = (JSONObject) jsonMember.get("request");
        OneMember oneMember = new OneMember();
    
        oneMember.setName(request.get("name").toString());
        oneMember.setMid(request.get("mid").toString());
        oneMember.setPasswd(request.get("passwd").toString());
        oneMember.setTelhp(request.get("telhp").toString());
        oneMember.setZip(request.get("zip").toString());
        oneMember.setAddr1(request.get("addr1").toString());
        oneMember.setAddr2(request.get("addr2").toString());
        oneMember.setBirth(request.get("birth").toString());
        oneMember.setXes(request.get("xes").toString());
        oneMember.setSignPurpose(request.get("signPurpose").toString());
        oneMember.setBlindness(request.get("blindness").toString());
        oneMember.setCustId(oneMemberMapper.selectCustSeq());
        
        JSONObject agreed = (JSONObject) request.get("agreement");
        if (agreed.get("sms").toString().equals("Y")) {
            oneMember.setSms(1);
        } else if (agreed.get("sms").toString().equals("N")) {
            oneMember.setSms(0);
        }
		oneMember.setTelcoGb(agreed.get("telcoGb").toString());
        oneMember.setInfluxType(request.get("influxType").toString());

        System.out.println("OneMember: " + oneMember.toString());
        
        oneMember.setAddr2(request.get("addr2").toString());
        
        int rtnvalue = oneMemberMapper.insertOneMember(oneMember);
        
        return rtnvalue;
        
    }
    
    public int addOneSeyfert(JSONObject jsonSeyfert) throws Exception {

        JSONObject request = (JSONObject) jsonSeyfert.get("request");
        OneSeyfert oneSeyfert = new OneSeyfert();
        oneSeyfert.setMid(request.get("mid").toString());
        oneSeyfert.setName(request.get("name").toString());
        oneSeyfert.setMemGuid(request.get("memGuid").toString());
        oneSeyfert.setIpAddr(request.get("ipAddr").toString());
        oneSeyfert.setMemUse(request.get("memUse").toString());
        oneSeyfert.setTelhp(request.get("telhp").toString());
        
        int rtnvalue = oneMemberMapper.insertOneSeyfert(oneSeyfert);
        
        return rtnvalue;
        
    }
    
    public boolean checkOneMember(JSONObject jsonMember) throws Exception {

        final String checkMemberId = ((JSONObject) jsonMember.get("request")).get("mid").toString();
        final String resultMemberId = oneMemberMapper.selectMemberIdCheck(checkMemberId);
        if (resultMemberId != null && resultMemberId.equals(checkMemberId)) { 
            return false; //non-pass
        } else { 
            return true; //pass
        }
        
    }
    
    public boolean checkPasswordById(JSONObject jsonMember) throws Exception {

        JSONObject request = (JSONObject) jsonMember.get("request");
    
        final String mid = request.get("mid").toString();
    
        String password = null;
        if (request.has("passwd")) {
            password = request.get("passwd").toString();
        } else {
            password = request.get("oldpasswd").toString();
        }

        final String resultMemberId = oneMemberMapper.checkPasswordById(mid, password);
        if (resultMemberId != null && resultMemberId.equals(mid)) { 
            return true; //pass
        } else { 
            return false; //non-pass
        }
        
    }
    
    public MariMember selectMemberById(String mid) throws Exception {
        return oneMemberMapper.selectMemberById(mid);
    }
    
    public int updateMemberById(JSONObject jsonMember) throws Exception {

        JSONObject request = (JSONObject) jsonMember.get("request");
        OneMember oneMember = new OneMember();
    
        oneMember.setNewpasswd(request.get("newpasswd").toString());
        oneMember.setOldpasswd(request.get("oldpasswd").toString());
        oneMember.setSmsInvest(request.get("smsInvest").toString());
        oneMember.setMid(request.get("mid").toString());

        return oneMemberMapper.updateMemberById(oneMember);
    }

    public int deleteMemberById(String mid) throws Exception {
        final int rtn = oneMemberMapper.deleteBackupMemberById(mid, commonUtil.getRemoteAddrs());
        int rtnValue = 0;
        if (rtn == 1) {
            rtnValue = oneMemberMapper.deleteMemberById(mid);
        }
        return rtnValue;
    }

    public String selectCustSeq() throws Exception {
    	return oneMemberMapper.selectCustSeq();
    }
    
    public int updateMemberMoney(String m_emoney, String mid) throws Exception {
    	return oneMemberMapper.updateMemberMoney(m_emoney, mid);
    }
    
    public String selectByCustIdToMid(String custId) throws Exception {
    	return oneMemberMapper.selectByCustIdToMid(custId);
    }
    
    public String selectByLoanIdToMid(String loanId) throws Exception {
    	return oneMemberMapper.selectByLoanIdToMid(loanId);
    }
    
    public String selectCustID(String mid) throws Exception {
    	return oneMemberMapper.selectCustID(mid);
    }
    
    public OneMemberCustId selectCustID2(String mid) throws Exception {
    	return oneMemberMapper.selectCustID2(mid);
    }
    
    public String selectUserConfirm(String mid, String mpw) throws Exception {
    	return oneMemberMapper.selectUserConfirm(mid, mpw);
    }
    
    public OneMemberCustAddInfo selectCustAddInfo(String mid) throws Exception {
    	return oneMemberMapper.selectCustAddInfo(mid);
    }
    
    public OneMemberCustAddInfo2 selectCustAddInfo2(String mid) throws Exception {
    	return oneMemberMapper.selectCustAddInfo2(mid);
    }
    
    public boolean insertOneCertify(OneCertify oneCertify) throws Exception {
    	return oneMemberMapper.insertOneCertify(oneCertify);
    }
    
    public List<String> selectCustConfirm(OneMemberConfirm oneMemberConfirm) throws Exception {
    	return oneMemberMapper.selectCustConfirm(oneMemberConfirm);
    }
    
    public OneCertifyWebDump selectCertifyWebDump(String bi) throws Exception {
    	return oneMemberMapper.selectCertifyWebDump(bi);
    }
    
    public boolean deleteCertifyWebDump(String bi) throws Exception {
    	return oneMemberMapper.deleteCertifyWebDump(bi);
    }
    
    public String selectAutoInvestAgree(String mid) throws Exception {
    	return oneMemberMapper.selectAutoInvestAgree(mid);
    }
    
    public String selectUserAuthToken(String authToken) throws Exception {
    	return oneMemberMapper.selectUserAuthToken(authToken);
    }
    
    public String selectUserIdCheck(String mid) throws Exception {
    	return oneMemberMapper.selectUserIdCheck(mid);
    }
    
    public List<String> selectTradeCheck(String mid) throws Exception {
    	return oneMemberMapper.selectTradeCheck(mid);
    }
    
    public boolean updateMemberAuthToken(String mid, String token) throws Exception {
    	return oneMemberMapper.updateMemberAuthToken(mid, token);
    }
    
    public boolean updateMemberPW(String mid, String pw) throws Exception {
    	return oneMemberMapper.updateMemberPW(mid, pw);
    }
    
    
    
    // api v2 start
    public int isDuplicate(JSONObject request) throws Exception {
		JSONObject json = request.getJSONObject("request");
		
		Member member = new Member();
		member.setName(json.getString("name"));
		member.setTelhp(json.getString("telhp"));
		member.setTelcoGb(json.getString("telcoGb"));
		
		int duplicateCnt = oneMemberMapper.isDuplicate(member);

		return duplicateCnt;
	}
    
    public int insertMember(JSONObject jsonMember, String token) throws Exception {
		
    	int rtnvalue = 0;
    	
    	JSONObject request = jsonMember.getJSONObject("request");
    	Member member = new Member();
    	
    	String pwd = request.get("pwd").toString();
    	String pwdOk = request.get("pwdOk").toString();
    	
    	if(!pwd.equals(pwdOk)) {
    		rtnvalue = -1;
    		return rtnvalue;
    	}
    	
    	member.setMid(request.getString("mid"));
    	member.setName(request.getString("name"));
    	member.setPwd(request.getString("pwd"));
    	member.setTelhp(request.getString("telhp"));
    	member.setBirth(request.getString("birth"));
    	member.setGender(request.getString("gender"));
    	member.setCustId(oneMemberMapper.selectCustSeq());
    	member.setToken(token);
    	member.setTelcoGb(request.getString("telcoGb"));
    	member.setSignPurpose(request.getString("signPurpose"));
    	member.setBlindness(request.getString("blindness"));
    	
    	JSONObject agreed = request.getJSONObject("agreement");
        
    	if (agreed.getString("sms").equals("Y")) member.setSms(1);
        else if (agreed.getString("sms").equals("N")) member.setSms(0);
    	
    	//member에 level 추가
    	if (request.has("userGb") && request.getString("userGb").equals("C"))	// 사용자구분 코드가 있고, 법인이면
    		member.setLevel("4");
	    else 
	    	member.setLevel("1");
    	
    	member.setInfluxType(request.getString("influxType"));
    	
        rtnvalue = oneMemberMapper.insertMember(member);
        
        return rtnvalue;
	}
    
    public MemberInfo getMember(String mid) {
		return oneMemberMapper.getMember(mid);
	}
    
    public int updateMember(Member member) throws Exception {
		int updateCount = 0;
		
		String mid = member.getMid();
		String custId = member.getCustId();
		String pwd = member.getPwd();
		String pwdNew = member.getPwdNew();
		String pwdNewOk = member.getPwdNewOk();
		String isDefaultProfile = member.getIsDefaultProfile();
		MultipartFile multipartfile = member.getProfile();
		
		MemberModInfo memberModInfo = oneMemberMapper.selectMemberModInfo(mid);
		
		if(pwd!=null && !pwd.isEmpty()) {
			String isMemberChecked = oneMemberMapper.checkPasswordById(mid, pwd);
			
			if(isMemberChecked == null || isMemberChecked.isEmpty()) {
				updateCount = -4;
				return updateCount;
			}
		}
		
		int checkPwd = 0;
		
		if((pwd != null && !pwd.isEmpty()) || (pwdNew != null && !pwdNew.isEmpty()) || (pwdNewOk != null && !pwdNewOk.isEmpty())) {
			if((pwd == null || pwd.isEmpty()) || (pwdNew == null || pwdNew.isEmpty()) || (pwdNewOk == null || pwdNewOk.isEmpty())) {
				checkPwd = -3;
			} else if(pwd != null && pwdNew != null && pwdNewOk != null ) {
				if(pwd.equals(pwdNew))
					checkPwd = -1;
				else if(!pwdNew.equals(pwdNewOk))
					checkPwd = -2;
			}
		}
		
		if(checkPwd == 0) {
			member.setPwd(pwd);
			member.setPwdNew(pwdNew);
		} else {
			updateCount = checkPwd;
			return updateCount;
		}
		
		if(multipartfile != null) {
			final String path = File.separator;			
			String uploadPath = path+"var"+path+"lib"+path+"tomcat8"+path+"webapps"+path+"ROOT"+path+"members"+path+custId+path+"profile";
			String domainPath = "https://p2p.crepass.com/members/"+custId+"/profile/"+multipartfile.getOriginalFilename();
			
			File file = new File(uploadPath);
			
			if(!file.exists()) file.mkdirs();
			else FileUtils.cleanDirectory(file);
			
			file = new File(uploadPath, multipartfile.getOriginalFilename());
			multipartfile.transferTo(file);
			member.setFileName(domainPath);
		} else {
			if(isDefaultProfile.equals("N"))
				member.setFileName(memberModInfo.getProfilePath());
		}

		if(isDefaultProfile.equals("Y")) {
			final String path = File.separator;			
			String uploadPath = path+"var"+path+"lib"+path+"tomcat8"+path+"webapps"+path+"ROOT"+path+"members"+path+custId+path+"profile";
			
			File file = new File(uploadPath);
			
			if(file.exists())
				FileUtils.cleanDirectory(file);
			member.setFileName("");
		}
		
		if(member.getIsPublicName().equals("Y"))
			member.setNickName("");
		else
			member.setNickName(memberModInfo.getMname().substring(0, 1) + "**");
		
		if(pwdNewOk == null || pwdNewOk.isEmpty()) {
			member.setPwd(memberModInfo.getPwd());
			member.setPwdNew(memberModInfo.getPwd());
			member.setPwdNewOk(memberModInfo.getPwd());
		}
		
		boolean isMemberUpdate = oneMemberMapper.updateMember(member);
		
		commonUtil.sendBatchLogging("updateMember", new Gson().toJson(member), String.valueOf(isMemberUpdate));
		
		if(!isMemberUpdate) {
			updateCount = -6;
			return updateCount;
		}
		
		return updateCount;
	}
    
    public int updateMemberBasic(String requestPram) throws Exception {
		int updateCount = 0;
		
		JSONObject jsonParam = new JSONObject(requestPram);
		
		Member member = new Member();
		String mid = jsonParam.getString("mid");
		String custId = jsonParam.getString("custId");
		String pwd = jsonParam.getString("pwd");
		String pwdNew = jsonParam.getString("pwdNew");
		String pwdNewOk = jsonParam.getString("pwdNewOk");
		String isDefaultProfile = jsonParam.getString("isDefaultProfile");
		String memo = jsonParam.getString("memo");
		String isPublicName = jsonParam.getString("isPublicName");
		
		member.setMid(mid);
		member.setCustId(custId);
		member.setPwd(pwd);
		member.setPwdNew(pwdNew);
		member.setPwdNewOk(pwdNewOk);
		member.setIsDefaultProfile(isDefaultProfile);
		member.setMemo(memo);
		member.setIsPublicName(isPublicName);
		
		MemberModInfo memberModInfo = oneMemberMapper.selectMemberModInfo(mid);
		
		if(pwd!=null && !pwd.isEmpty()) {
			String isMemberChecked = oneMemberMapper.checkPasswordById(mid, pwd);
			
			if(isMemberChecked == null || isMemberChecked.isEmpty()) {
				updateCount = -4;
				return updateCount;
			}
		}
		
		int checkPwd = 0;
		
		if((pwd != null && !pwd.isEmpty()) || (pwdNew != null && !pwdNew.isEmpty()) || (pwdNewOk != null && !pwdNewOk.isEmpty())) {
			if((pwd == null || pwd.isEmpty()) || (pwdNew == null || pwdNew.isEmpty()) || (pwdNewOk == null || pwdNewOk.isEmpty())) {
				checkPwd = -3;
			} else if(pwd != null && pwdNew != null && pwdNewOk != null ) {
				if(pwd.equals(pwdNew))
					checkPwd = -1;
				else if(!pwdNew.equals(pwdNewOk))
					checkPwd = -2;
			}
		}
		
		if(checkPwd == 0) {
			member.setPwd(pwd);
			member.setPwdNew(pwdNew);
		} else {
			updateCount = checkPwd;
			return updateCount;
		}
		
		if(isDefaultProfile.equals("Y")) {
			final String path = File.separator;			
			String uploadPath = path+"var"+path+"lib"+path+"tomcat8"+path+"webapps"+path+"ROOT"+path+"members"+path+custId+path+"profile";
			
			File file = new File(uploadPath);
			
			if(file.exists())
				FileUtils.cleanDirectory(file);
			member.setFileName("");
		} else
			member.setFileName(memberModInfo.getProfilePath());
		
		if(member.getIsPublicName().equals("Y"))
			member.setNickName("");
		else
			member.setNickName(memberModInfo.getMname().substring(0, 1) + "**");
		
		if(pwdNewOk == null || pwdNewOk.isEmpty()) {
			member.setPwd(memberModInfo.getPwd());
			member.setPwdNew(memberModInfo.getPwd());
			member.setPwdNewOk(memberModInfo.getPwd());
		}
		
		boolean isMemberUpdate = oneMemberMapper.updateMember(member);
		
		commonUtil.sendBatchLogging("updateMemberBasic", new Gson().toJson(member), String.valueOf(isMemberUpdate));
		
		if(!isMemberUpdate) {
			updateCount = -6;
			return updateCount;
		}
		
		return updateCount;
	}
    
    public MemberInvestInfo selectMemberInvestInfo(String mid) throws Exception {
    	return oneMemberMapper.selectMemberInvestInfo(mid);
    }

	public int insertEmailValidation(String mid, String emailToken) throws Exception {
		return oneMemberMapper.insertEmailValidation(mid, emailToken);
	}

	public Member selectMemberByPhone(String mobileCorp, String hp) throws Exception {
		return oneMemberMapper.selectMemberByPhone(mobileCorp, hp);
	}

}
