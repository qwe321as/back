package com.crepass.restfulapi.v2.domain;

import java.util.List;

import lombok.Data;

@Data
public class Agreement  {
			
	private static final long serialVersionUID = 1L;
    
    private List<AgreementItem> list;
	
}
