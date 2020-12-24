package com.crepass.restfulapi.config;

import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@EnableAutoConfiguration
public class CustomErrorController implements ErrorController {

	private static final Logger logger = LoggerFactory.getLogger(ExloggerApplication.class);
	
	private String PATH = "/error";
	
	@RequestMapping(value = "/error")
    public String error(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        String statusCode = String.valueOf(status);
        if (statusCode.equalsIgnoreCase(HttpStatus.NOT_FOUND.toString())) {
	        Enumeration<String> names = request.getHeaderNames();
	        logger.error("==========================<PageNotFound>==========================");
	        while(names.hasMoreElements()) {
	        	String headerName = names.nextElement();
	        	logger.error(headerName + " => " + request.getHeader(headerName));
	        }
	        logger.error("==========================</PageNotFound>==========================");
        }
        
        return "error/error";
    }
	
	@Override
	public String getErrorPath() {
		return PATH;
	}

}
