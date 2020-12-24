package com.crepass.restfulapi.common;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.crepass.restfulapi.common.domain.ResponseResult;

import io.swagger.annotations.ApiOperation;

@Component
@Aspect
public class HttpHeaderAspect {
    
    //@Around("@annotation(someAnnotation)") 
    public Object doSomethingAround(final ProceedingJoinPoint joinPoint, final ApiOperation someAnnotation) throws Throwable { 
        ServletRequestAttributes servletContainer = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = servletContainer.getRequest();
        
        if (request.getHeader("authToken") == null) {
            
            ResponseResult result = new ResponseResult();
            result.setState(444);
            result.setMessage("인증토큰이 존재하지 않습니다.");
            result.setResult("");
            
            return new ResponseEntity<ResponseResult>(result, HttpStatus.OK);
            
        }
        return joinPoint.proceed();
    }

}