package com.crepass.restfulapi.config;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;  
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.crepass.restfulapi.common.CommonUtil;
@ControllerAdvice
public class GlobalControllerExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(ExloggerApplication.class);
	 
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ErrorInfo handleBadRequest(HttpServletRequest req, Exception ex) {
    	logger.error("url => " + req.getRequestURL().toString());
    	logger.error("error msg => " + ex.getMessage());
    	
    	CommonUtil commonUtil = new CommonUtil();
		String hostName = "";
    	try {
    		hostName = commonUtil.getHostNameLinux();
		} catch (Exception e) { e.printStackTrace(); }
    	
    	String userAgent = req.getHeader("user-agent");
    	
    	if(userAgent.toUpperCase().matches(".*BOT.*")) {
    		logger.error("---connection bot--- ");
        	logger.error("user-agent => " + userAgent);
        	logger.error("connect bot ip => " + commonUtil.getRemoteAddrs());
        	logger.error("---/connection bot--- ");
    	} else {
	    	SlackClient slackClient = new SlackClient();
			slackClient.sendingMessage("url => " + req.getRequestURL().toString() + "\nuser-agent : "+ userAgent + "\nerror msg => " + ex.getMessage(), hostName);
    	}
    	
        return new ErrorInfo(req.getRequestURL().toString(), ex);
    }
}
