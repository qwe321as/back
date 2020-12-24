package com.crepass.restfulapi.v2.domain;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class Member  {
			
	private int mno;
	private String mid;
	private String pwd;			  // 기존 비밀번호
	private String pwdNew;		  // 새 비밀번호
	private String pwdNewOk;	  // 새 비밀번호 확인
	private String name;
	private String nickName;
	private String telhp;
	private String birth;
	private String gender;
	private String custId;
	private String telcoGb;
	private String fileName;
	private String memo;
	private String level;
	private MultipartFile profile;
	private String isDefaultProfile;	// 기본이미지 적용판단값 Y=적용 / N=미적용
	
	private int sms;			  // SMS 수신동의  1=동의 / 0=미동의
	private String token;
	private String isPublicName;  // 이름공개여부 판단값 Y=공개 , N=비공개
	
	private String signPurpose;
	private String blindness;
	
	private String influxType;

}
