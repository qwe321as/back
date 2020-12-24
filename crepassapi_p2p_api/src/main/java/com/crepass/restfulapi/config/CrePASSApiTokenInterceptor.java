package com.crepass.restfulapi.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.one.service.OneMemberService;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

@CrossOrigin
public class CrePASSApiTokenInterceptor implements HandlerInterceptor {

	static final String headers = "Accept, Accept-CH, Accept-Charset, Accept-Datetime, Accept-Encoding, Accept-Ext"
			+ ", Accept-Features, Accept-Language, Accept-Params, Accept-Ranges, Access-Control-Allow-Credentials"
			+ ", Access-Control-Allow-Headers, Access-Control-Allow-Methods, Access-Control-Allow-Origin"
			+ ", Access-Control-Expose-Headers, Access-Control-Max-Age, Access-Control-Request-Headers"
			+ ", Access-Control-Request-Method, Age, Allow, Alternates, Authentication-Info, Authorization"
			+ ", C-Ext, C-Man, C-Opt, C-PEP, C-PEP-Info, CONNECT, Cache-Control, Compliance, Connection, Content-Base"
			+ ", Content-Disposition, Content-Encoding, Content-ID, Content-Language, Content-Length, Content-Location"
			+ ", Content-MD5, Content-Range, Content-Script-Type, Content-Security-Policy, Content-Style-Type"
			+ ", Content-Transfer-Encoding, Content-Type, Content-Version, Cookie, Cost, DAV, DELETE, DNT, DPR"
			+ ", Date, Default-Style, Delta-Base, Depth, Derived-From, Destination, Differential-ID, Digest, ETag"
			+ ", Expect, Expires, Ext, From, GET, GetProfile, HEAD, HTTP-date, Host, IM, If, If-Match, If-Modified-Since"
			+ ", If-None-Match, If-Range, If-Unmodified-Since, Keep-Alive, Label, Last-Event-ID, Last-Modified, Link"
			+ ", Location, Lock-Token, MIME-Version, Man, Max-Forwards, Media-Range, Message-ID, Meter, Negotiate"
			+ ", Non-Compliance, OPTION, OPTIONS, OWS, Opt, Optional, Ordering-Type, Origin, Overwrite, P3P, PEP"
			+ ", PICS-Label, POST, PUT, Pep-Info, Permanent, Position, Pragma, ProfileObject, Protocol, Protocol-Query"
			+ ", Protocol-Request, Proxy-Authenticate, Proxy-Authentication-Info, Proxy-Authorization, Proxy-Features"
			+ ", Proxy-Instruction, Public, RWS, Range, Referer, Refresh, Resolution-Hint, Resolver-Location"
			+ ", Retry-After, Safe, Sec-Websocket-Extensions, Sec-Websocket-Key, Sec-Websocket-Origin, Sec-Websocket-Protocol"
			+ ", Sec-Websocket-Version, Security-Scheme, Server, Set-Cookie, Set-Cookie2, SetProfile, SoapAction"
			+ ", Status, Status-URI, Strict-Transport-Security, SubOK, Subst, Surrogate-Capability, Surrogate-Control"
			+ ", TCN, TE, TRACE, Timeout, Title, Trailer, Transfer-Encoding, UA-Color, UA-Media, UA-Pixels, UA-Resolution"
			+ ", UA-Windowpixels, URI, Upgrade, User-Agent, Variant-Vary, Vary, Version, Via, Viewport-Width, WWW-Authenticate"
			+ ", Want-Digest, Warning, Width, X-Content-Duration, X-Content-Security-Policy, X-Content-Type-Options"
			+ ", X-CustomHeader, X-DNSPrefetch-Control, X-Forwarded-For, X-Forwarded-Port, X-Forwarded-Proto, X-Frame-Options"
			+ ", X-Modified, X-OTHER, X-PING, X-PINGOTHER, X-Powered-By, X-Requested-With"
			+ ", appversion,authtoken,content-type,ostype, charset, vs, UA, apkversion";
	
	@Autowired
    private OneMemberService oneMemberService;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		
		String origins = request.getHeader("Origin");
		
		HashMap<String, String> allowedOrigins = new HashMap<>();
		allowedOrigins.put("https://m.crepass.com", "https://m.crepass.com");
		allowedOrigins.put("https://young55.crepass.com", "https://young55.crepass.com");
		allowedOrigins.put("http://mdev.crepass.com", "http://mdev.crepass.com");
		allowedOrigins.put("http://partners.crepass.com", "http://partners.crepass.com");
		allowedOrigins.put("http://p2papp.crepass.com", "http://p2papp.crepass.com");
		allowedOrigins.put("http://itdang.crepass.com", "http://itdang.crepass.com");
		//allowedOrigins.put("http://localhost", "http://localhost");
		allowedOrigins.put("http://localhost:8080", "http://localhost:8080");
		
		http://192.168.1.175:8080/
		
		if(allowedOrigins.containsKey(origins))
			response.setHeader("Access-Control-Allow-Origin", origins);
		
		response.setHeader("Access-Control-Allow-Headers", headers);
		response.setHeader("Access-Control-Expose-Headers", headers);
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT, PATCH");
		response.setHeader("Access-Control-Request-Headers", "X-Custom-Header");
		response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");
 
        if(request.getMethod().toUpperCase().equals("GET")) {
        	response.getOutputStream().println("{}");
        	return false;
        }
        
		switch(request.getServletPath()) {
			case "/api/intro" :
			case "/api/agreed/document" :
			case "/api/members/duplicate" :
			case "/api/members/add" :
			case "/api/members/login" :
			case "/api/statistics/web" :
			case "/api/aes/certify" :
			case "/api/aes/enc" :
			case "/api/members/findpw" :
			case "/api/loan/virtualaccnt/create" :
			case "/api/prepayment/schedule/get" :
			case "/api/prepayment/schedule/send" :
			case "/api/lenddo/history/add" :
			case "/api/lenddo/add" :
			case "/api2/invest/loanList" :
			case "/api2/members/loginSocial" :
			case "/api2/members/add" :
			case "/api2/members/login" :
			case "/api2/intro" :
			case "/api/slide" :
			case "/api2/members/addmvp" :
			case "/api/credit/info" :
			case "/api/member/info" :
				return true;
		}
		
		String authToken = oneMemberService.selectUserAuthToken(request.getHeader("authToken"));
		
//		if (authToken.equals("8007"))
//			return true;						// mhson@crepass.com 토큰값 무시
//		
		if(authToken != null && !authToken.equals("null"))
			return true;
		
		JSONObject jsonAuthToken = new JSONObject();
		jsonAuthToken.put("state", 205);
		jsonAuthToken.put("message", "AuthToken Failed.");
		
		CommonUtil commonUtil = new CommonUtil();
		String hostName = "";
    	try {
    		hostName = commonUtil.getHostNameLinux();
		} catch (Exception e) { e.printStackTrace(); }
    	
    	
		SlackClient slackClient = new SlackClient();
		slackClient.sendingMessage("request url : " + request.getRequestURL() + "\nuser-agent : "
				+ request.getHeader("user-agent") + "\nrequest : "+ getRequestBody(request) + "\nresponse : "
				+ new Gson().toJson(new JsonParser().parse(jsonAuthToken.toString())), hostName);
		
        response.getOutputStream().println(new Gson().toJson(new JsonParser().parse(jsonAuthToken.toString())));
		
		return false;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
	}
	
	public String getRequestBody(HttpServletRequest request) throws IOException {
		String body = null;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
 
        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    throw ex;
                }
            }
        }
 
        body = stringBuilder.toString();
        return body;
	}
}
