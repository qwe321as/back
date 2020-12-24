package com.crepass.restfulapi.creone.controller;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.crepass.restfulapi.common.CommonUtil;
import com.crepass.restfulapi.cre.service.CreMemberService;

@Component
public class PushController {
    
	@Autowired
    private CommonUtil commonUtil;
	
    @Autowired
    private CreMemberService creMemberService;
    
    @Value("${crepas.url.push}")
    private String pushUrl;
    
    @Value("${crepas.push.authorization}")
    private String authorization;
    
    @Value("${crepas.push.app.id}")
    private String appId;
    
    @Scheduled(cron = "0 0 8-21 * * *")
    public void getTdairyNotification() {
        
        try {
        	if(commonUtil.getHostNameLinux().equals("p2p-bat01-live")) {
	            String jsonResponse;
	            
	            URL url = new URL(pushUrl);
	            HttpURLConnection con = (HttpURLConnection) url.openConnection();
	            con.setUseCaches(false);
	            con.setDoOutput(true);
	            con.setDoInput(true);
	
	            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
	            con.setRequestProperty("Authorization", authorization);
	            con.setRequestMethod("POST");
	            
	            String playerIds = creMemberService.selectPushTarget().stream()
	                    .collect(Collectors.joining(","));
	
	            String strJsonBody = "{"
	                               +   "\"app_id\": \"" + appId + "\","
	                               +   "\"include_player_ids\": [" + playerIds + "],"
	                               +   "\"headings\": {\"en\": \"시간일기\"},"
	                               +   "\"contents\": {\"en\": \"현재 내가 하고 있는 일 선택하기\"},"
	                               +   "\"small_icon\": \"icon_small_push\","
	                               +   "\"large_icon\": \"icon_large_push\","
	                               +   "\"data\": {\"type\": 1}"
	                               + "}";
	            
	            System.out.println("strJsonBody:\n" + strJsonBody);
	
	            byte[] sendBytes = strJsonBody.getBytes("UTF-8");
	            con.setFixedLengthStreamingMode(sendBytes.length);
	
	            OutputStream outputStream = con.getOutputStream();
	            outputStream.write(sendBytes);
	
	            int httpResponse = con.getResponseCode();
	            System.out.println("httpResponse: " + httpResponse);
	
	            if (httpResponse >= HttpURLConnection.HTTP_OK && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
	               Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
	               jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
	               scanner.close();
	            } else {
	               Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
	               jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
	               scanner.close();
	            }
	            System.out.println("jsonResponse:\n" + jsonResponse);
        	}
         } catch (Throwable t) {
            t.printStackTrace();
         }
        
    }
    
}