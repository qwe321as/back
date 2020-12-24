package com.crepass.restfulapi.v2.domain;

public class PasswordCheck {
	
	
	public int validCheck(Member member) {
		
		int validCode = 0;
		
		String pwd = member.getPwd();
		String pwdNew = member.getPwdNew();
		String pwdNewOk = member.getPwdNewOk();
		
		if(pwd!=null && "".equals(pwd) && pwd.length()!=0) {
			if(pwdNew==null || pwdNewOk==null) {
				validCode = -3;
				return validCode;
			}
			else if(pwdNew.length()==0 || pwdNewOk.length()==0) {
				validCode = -3;
				return validCode;
			} else if("".equals(pwd) || "".equals(pwdNewOk)) {
				validCode = -3;
				return validCode;
			}
			
		}
		
		if(pwdNew!=null && "".equals(pwdNew) && pwdNew.length()!=0) {
			if(pwd==null || pwdNewOk==null) {
				validCode = -3;
				return validCode;
			}
			else if("".equals(pwd) || "".equals(pwdNewOk)) {
				validCode = -3;
				return validCode;
			}
		}
		
		if(pwdNewOk!=null && pwdNewOk!="" && pwdNewOk.length()!=0) {
			if(pwd==null || pwdNew==null) {
				validCode = -3;
				return validCode;
			}
			else if("".equals(pwd) || "".equals(pwdNew)) {
				validCode = -3;
				return validCode;
			}
		}
		
		if( pwd!=null && pwdNew!=null && pwdNewOk!=null && !"".equals(pwd) && !"".equals(pwdNew) && !"".equals(pwdNewOk) ) {
		
			if(pwd.equals(pwdNew) && pwd.equals(pwdNewOk)) {
				validCode = -1;
				return validCode;
			}
			if(!pwdNew.equals(pwdNewOk)) {
				validCode = -2;
				return validCode;
			} 
		}
		return validCode;
	}
	
	
}
