package com.crepass.restfulapi.common;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class AES256Cipher {
	private static volatile AES256Cipher INSTANCE;
	
    private static String secretKey = "0982beb15fb2f0c584fa5872527c58b9";
	
	private static String IV;
	
	public static AES256Cipher getInstance(){
	     if(INSTANCE==null){
	         synchronized(AES256Cipher.class){
	             if(INSTANCE==null)
	                 INSTANCE=new AES256Cipher();
	         }
	     }
	     return INSTANCE;
	 }
	 
	 private AES256Cipher() {
	     IV = secretKey.substring(0,16);
	 }
	 
	 //암호화
	 public String AES_Encode(String str) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{
	     byte[] keyData = secretKey.getBytes();
	 
		 SecretKey secureKey = new SecretKeySpec(keyData, "AES");
		 
		 Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		 c.init(Cipher.ENCRYPT_MODE, secureKey, new IvParameterSpec(IV.getBytes()));
		 
		 byte[] encrypted = c.doFinal(str.getBytes("UTF-8"));
		 String enStr = new String(Base64.encodeBase64(encrypted));
		 
		 return enStr;
	 }
	 
	 //복호화
	 public String AES_Decode(String str) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{
		  byte[] keyData = secretKey.getBytes();
		  SecretKey secureKey = new SecretKeySpec(keyData, "AES");
		  Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		  c.init(Cipher.DECRYPT_MODE, secureKey, new IvParameterSpec(IV.getBytes("UTF-8")));
		 
		  byte[] byteStr = Base64.decodeBase64(str.getBytes());
		 
		  return new String(c.doFinal(byteStr),"UTF-8");
	 }
}
