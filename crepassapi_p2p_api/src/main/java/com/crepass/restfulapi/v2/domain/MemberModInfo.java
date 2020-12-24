package com.crepass.restfulapi.v2.domain;

import lombok.Data;

@Data
public class MemberModInfo  {
			
	private static final long serialVersionUID = 1L;
    
    private String pwd;
    
    private String mname;
       
    private String profilePath;
    
}
