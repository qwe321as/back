package com.crepass.restfulapi.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONException;
import org.json.JSONObject;

import javacryption.aes.AesCtr;

public class PayGateGuidKey {

	public String CreateKeySet(String mno, String mid, String phoneNumber) {
		final String GUID = "GAdQV1vcm2disLtM4k8tai";
		final String KEY = "Ouw6Xqc1cznfWPyacrgDwFfoBsOgMRd0uy8cHt2tpaK5cO1O63D2rHNKIXoMXMwW";
		final String COMMON_ENC = "UTF-8";
	    final int FIXED_BITS = 256;
	    String cipherEncoded = null;
		
		try {
			String eachEncodedByParameter = "&_method=POST&reqMemGuid="+GUID+"&desc=desc&nonce=CPAS"+Calendar.getInstance().getTimeInMillis()+"&emailAddrss="+mid+"&emailTp=PERSONAL&fullname=CrePASS&nmLangCd=ko&phoneCntryCd=KOR&phoneNo="+phoneNumber+"&phoneTp=MOBILE";
	        String cipher = AesCtr.encrypt(eachEncodedByParameter, KEY, FIXED_BITS);
			cipherEncoded = URLEncoder.encode(cipher, COMMON_ENC);
			
			return postCreateKey(GUID, cipherEncoded);
			
		} catch (UnsupportedEncodingException e) { e.printStackTrace(); }
         
		return null;
	}
	
	public String postCreateKey(String reqMemGuid, String encReq) {
		StringBuffer sb = new StringBuffer();
        String result = null;
        
		try {
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				
				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					
				}
				
				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					
				}
			}};
			
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			
			String param = "_method=POST&reqMemGuid=" + reqMemGuid + "&encReq=" + encReq;
			
			URL url = new URL("https://v5.paygate.net/v5a/member/createMember?" + param);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

            InputStreamReader in = null;
            
            try { in = new InputStreamReader(conn.getInputStream(), "UTF-8"); }
            catch (Exception e) { in = new InputStreamReader(conn.getErrorStream(), "UTF-8"); }
            BufferedReader br = new BufferedReader(in);

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            result = sb.toString();
	        
			JSONObject jsonResult = new JSONObject(result);
			String memGuid = null;
			try { memGuid = ((JSONObject)jsonResult.get("data")).getString("memGuid"); }
            catch (Exception e) { memGuid = null; }
			
			in.close();
			
			return memGuid;
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (KeyManagementException e1) {
			e1.printStackTrace();
		}
		
		return null;
	}
}
