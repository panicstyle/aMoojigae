package com.panicstyle.Moojigae;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;

public class Login {

	/*
	 * 		HttpClient httpClient = new DefaultHttpClient();
		HttpContext httpContext = new BasicHttpContext();
		CookieStore cookieStore = new BasicCookieStore();
		httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

	 */
	private String userID;
	private String userPW;
	
	public String getUserID() {
		return userID;
	}
	
	public Boolean GetUserInfoXML(Context context) {
	    InputStream in = null;
	    String fileName = "LoginInfo.xml";
	    try {
		    FileInputStream input = context.openFileInput(fileName);
	    	in = new BufferedInputStream(input);
	    	StringBuffer out = new StringBuffer();
	    	byte[] buffer = new byte[4094];
	    	int readSize;
	    	while ( (readSize = in.read(buffer)) != -1) {
	    	    out.append(new String(buffer, 0, readSize));
	    	}
	    	String data = out.toString();
	    	
	    	XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
	        XmlPullParser xpp = factory.newPullParser();

	        xpp.setInput( new StringReader(data) );
	        int eventType = xpp.getEventType();
	        int type = 0;
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	        	if(eventType == XmlPullParser.START_DOCUMENT) {
	        		System.out.println("Start document");
	        	} else if(eventType == XmlPullParser.START_TAG) {
	        		System.out.println("Start tag "+xpp.getName());
	        		String strTag = xpp.getName();
	        		if (strTag.equalsIgnoreCase("ID")) {
	        			type = 1;
	        		} else if (strTag.equalsIgnoreCase("Password")) {
	        			type = 2;
	        		} else {
	        			type = 0;
	        		}
	        	} else if (eventType == XmlPullParser.END_TAG) {
	        		System.out.println("End tag "+xpp.getName());
	        		type = 0;
	        	} else if (eventType == XmlPullParser.TEXT) {
	        		System.out.println("Text "+xpp.getText());
	        		if (type == 1) {
	        			userID = xpp.getText();
	        		} else if (type == 2) {
	        			userPW = xpp.getText();
	        		}
	        	}
	        	eventType = xpp.next();
	        }
	        System.out.println("End document");
	        return true;
	     } catch( Exception e ) {
	    	 System.out.println(e.getMessage());
	    	 return false;
	     } finally {
	    	 if (in != null){
	    		 try {
	    			 in.close();
	    		 } catch( IOException ioe ) {
	    		 }
	    	 }
	     }
	}
	
	
	public int LoginTo(HttpClient httpClient, HttpContext httpContext, Context context) {
	
		String url = "http://121.134.211.159/login-process.do";
		String logoutURL = "http://121.134.211.159/logout.do";

		if (!GetUserInfoXML(context)) {
	    	return -1;
		}
		
		HttpRequest httpRequest = new HttpRequest();
		
		String referer = "http://121.134.211.159/MLogin.do";
		
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("userId", userID));
		nameValuePairs.add(new BasicNameValuePair("userPw", userPW));
		nameValuePairs.add(new BasicNameValuePair("boardId", ""));
		nameValuePairs.add(new BasicNameValuePair("boardNo", ""));
		nameValuePairs.add(new BasicNameValuePair("page", "1"));
		nameValuePairs.add(new BasicNameValuePair("categoryId", "-1"));
		nameValuePairs.add(new BasicNameValuePair("returnURI", ""));
		nameValuePairs.add(new BasicNameValuePair("returnBoardNo", ""));
		nameValuePairs.add(new BasicNameValuePair("beforeCommand", ""));
		nameValuePairs.add(new BasicNameValuePair("command", "LOGIN"));
		
		httpRequest.requestGet(httpClient, httpContext, logoutURL, referer, "euc-kr");
		
		String result = httpRequest.requestPost(httpClient, httpContext, url, nameValuePairs, referer, "euc-kr");
		
		if (result.indexOf("<script language=javascript>moveTop()</script>") >= 0) {
	    	System.out.println("Login Success");
			return 1;
		} else {
			String errMsg = "Login Fail";
	    	System.out.println(errMsg);
	        return 0;
		}
	}
}
