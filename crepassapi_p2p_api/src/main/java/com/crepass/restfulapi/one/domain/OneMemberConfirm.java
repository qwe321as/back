package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneMemberConfirm implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String hp;
	
	private String name;
	
	private String newsagency;
	
    public OneMemberConfirm() {

    }
}
