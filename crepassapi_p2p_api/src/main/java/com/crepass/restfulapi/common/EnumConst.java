package com.crepass.restfulapi.common;

public enum EnumConst {
	API_MEMBERCHECK("/crapas/plugin/api/v1/memberck.php");
	
	private String code;
	
	private EnumConst (String code) {
		this.code  = code;	
	}
	
	public String getCode() { return code;}
	
}
