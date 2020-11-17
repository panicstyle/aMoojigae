package com.panicstyle.Moojigae;


import android.content.Context;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;

public class Login {
	protected String m_userID;
	protected String m_userPW;
	protected String m_regId;
	protected boolean m_pushYN;

	public String m_strErrorMsg = "";

	public int LoginTo(Context context, CustomHttpRequest httpRequest, String userID, String userPW) {

		String url = GlobalConst.m_strServer + "/login-process.do";
		String referer = GlobalConst.m_strServer + "/MLogin.do";
		String logoutURL = GlobalConst.m_strServer + "/logout.do";

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

		String result = httpRequest.requestPost(url, nameValuePairs, referer);

		if (result.contains("<script language=javascript>moveTop()</script>")) {
			System.out.println("Login Success");
			return 1;
		} else {
			if (result.contains("<b>시스템 메세지입니다</b>")) {
				m_strErrorMsg = Utils.getMatcherFirstString("(?<=<b>시스템 메세지입니다</b></font><br>)(.|\\n)*?(?=<br>)", result);
				return 0;
			} else {
				return 1;
			}
		}
	}

	public int Logout(Context context, CustomHttpRequest httpRequest) {

		String referer = GlobalConst.m_strServer + "/MLogin.do";
		String logoutURL = GlobalConst.m_strServer + "/logout.do";

		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

		String result = httpRequest.requestPost(logoutURL, nameValuePairs, referer);

		return 1;
	}

	public int PushRegister(Context context, CustomHttpRequest httpRequest, String userID, String regId) {

		if (userID.isEmpty() || regId.isEmpty()) {
			return 0;
		}

		String url = GlobalConst.m_strServer + "/push/PushRegister";

		JSONObject obj = new JSONObject();

		String strPushYN = "Y";

		try {
			obj.put("uuid", regId);
			obj.put("type", "Android");
			obj.put("userid", userID);
			obj.put("push_yn", strPushYN);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String strBody = obj.toString();

		String result = httpRequest.requestPost(url, strBody, "");

		System.out.println("PushRegister result = " + result);
		return 1;
	}

	public int PushRegisterUpdate(Context context, CustomHttpRequest httpRequest, String userID, String regId, boolean pushYN) {

		if (userID.isEmpty() || regId.isEmpty()) {
			return 0;
		}

		String url = GlobalConst.m_strServer + "/push/PushRegisterUpdate";

		JSONObject obj = new JSONObject();

		String strPushYN = "Y";
		if (pushYN) {
			strPushYN = "Y";
		} else {
			strPushYN = "N";
		}

		try {
			obj.put("uuid", regId);
			obj.put("type", "Android");
			obj.put("userid", userID);
			obj.put("push_yn", strPushYN);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String strBody = obj.toString();

		String result = httpRequest.requestPost(url, strBody, "");

		System.out.println("PushRegister result = " + result);

		return 1;
	}
}
