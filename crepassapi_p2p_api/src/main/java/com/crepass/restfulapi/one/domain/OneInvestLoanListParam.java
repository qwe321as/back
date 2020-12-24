package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneInvestLoanListParam implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String mid;
    
    private String catg1;
    
    private String catg2;
    
    private String catg3;
    
    private String catg4;
    
    private String catg5;
    
    private String catg6;
    
    private String catg7;
    
    private String catg8;
    
//    #{mid} AND (catg1 = #{catg1} OR catg2 = #{catg2} OR catg3 = #{catg3} OR catg4 = #{catg4} OR catg5 = #{catg5} OR catg6 = #{catg6} OR catg7 = #{catg7} OR catg8 = #{catg8})
    
    public OneInvestLoanListParam() {

    }
    
}
