package com.panicstyle.Moojigae;


import android.content.Context;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Login {
	protected String m_userID;
	protected String m_userPW;

	public String m_strErrorMsg = "";

	public int LoginTo(Context context, HttpRequest httpRequest, String encodingOption) {

		String url = GlobalConst.m_strServer + "/login-process.do";
		String referer = GlobalConst.m_strServer + "/MLogin.do";
		String logoutURL = GlobalConst.m_strServer + "/logout.do";

		SetInfo setInfo = new SetInfo();

		if (!setInfo.GetUserInfo(context)) {
			if (!setInfo.GetUserInfoXML(context)) {
				return -1;
			}
		}

		m_userID = setInfo.m_userID;
		m_userPW = setInfo.m_userPW;
        System.out.println("UserID = " + m_userID);
        System.out.println("UserPW = " + m_userPW);

		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("userId", m_userID));
		nameValuePairs.add(new BasicNameValuePair("userPw", m_userPW));
		nameValuePairs.add(new BasicNameValuePair("boardId", ""));
		nameValuePairs.add(new BasicNameValuePair("boardNo", ""));
		nameValuePairs.add(new BasicNameValuePair("page", "1"));
		nameValuePairs.add(new BasicNameValuePair("categoryId", "-1"));
		nameValuePairs.add(new BasicNameValuePair("returnURI", ""));
		nameValuePairs.add(new BasicNameValuePair("returnBoardNo", ""));
		nameValuePairs.add(new BasicNameValuePair("beforeCommand", ""));
		nameValuePairs.add(new BasicNameValuePair("command", "LOGIN"));

		String result = httpRequest.requestPost(url, nameValuePairs, referer, encodingOption);

		if (result.contains("<script language=javascript>moveTop()</script>")) {
			System.out.println("Login Success");
			return 1;
		} else {
			String errMsg = "Login Fail";
			System.out.println(errMsg);
			return 0;
		}
	}
}
