package com.crepass.restfulapi.one.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneHolidayCalendar implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String Hdate;
    
    private String Hlunar;
    
    public OneHolidayCalendar() {

    }
    
}
