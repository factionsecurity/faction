package com.fuse.utils;

import java.util.UUID;

import javax.servlet.http.HttpSession;


public class CSRF {
	
	public static String getOrCreateToken(HttpSession session) {
		String currentToken = (String)session.getAttribute("csrf");
		if(currentToken == null || currentToken.equals("")) {
			return getToken(session);
		}else {
			return currentToken;
		}
		
	}
	
	public  static String getToken(HttpSession session) {
		String token = UUID.randomUUID().toString();
		session.setAttribute("csrf", token);
		return token;
	}
	
	public  static boolean checkToken(HttpSession session, String token) {
		String oldToken = (String)session.getAttribute("csrf");
		getToken(session);
		if(oldToken.equals(token)) {
			return true;
		}else {
			return false;
		}
	}
	
	public  static boolean checkToken(HttpSession session, String token, boolean renew) {
		String oldToken = (String)session.getAttribute("csrf");
		if(renew)
			getToken(session);
		if(oldToken.equals(token)) {
			return true;
		}else {
			return false;
		}
	}
		
	
}

