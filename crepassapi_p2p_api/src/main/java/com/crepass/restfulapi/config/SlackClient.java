package com.crepass.restfulapi.config;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class SlackClient {

	private HttpHeaders headers;
	
    private String slackServiceToken = "Bearer xoxb-331753533190-390557076421-FiqFU5jssEdJFcnORDcybcL9";
    private String slackChatUrl = "https://slack.com/api/chat.postMessage";
	
	public SlackClient() {
		headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", slackServiceToken);
	}
	
	public void sendingMessage(String Message, String HostName) {
		try {
			JSONObject item = new JSONObject();
	        item.put("channel", "server_status");
	        item.put("username", "P2P.API.SERVER [" + HostName + "]");
	        item.put("text", Message);
	
	        HttpEntity<String> requestError = new HttpEntity<String>(item.toString(), headers);
	        new RestTemplate().postForObject(slackChatUrl, requestError, String.class);
		} catch(Exception e) {
			e.getStackTrace();
		}
	}
}
