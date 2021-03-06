package com.panicstyle.Moojigae;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CustomHttpRequest {
	HttpClient httpClient;
	HttpContext httpContext;
	String m_Cookie;

	public String requestPost(String url, String strParam, String referer) {
		InputStream is = null;
		String result = "";
		String encode = "utf-8";
		try {
			/** 연결 타입아웃내에 연결되는지 테스트, 30초 이내에 되지 않는다면 에러 */
			/** 네트웍 연결해서 데이타 받아오기 */
			HttpParams params = httpClient.getParams();
			HttpConnectionParams.setConnectionTimeout(params, 30000);
			HttpConnectionParams.setSoTimeout(params, 30000);

			HttpPost httppost = new HttpPost(url);
			if (referer.length() > 0) {
				httppost.setHeader("referer", referer);
			}

			if (strParam != null) {
				StringEntity strEntity = new StringEntity(strParam, encode);
				httppost.setEntity(strEntity);
			}

			HttpResponse response = httpClient.execute(httppost, httpContext);
			HttpEntity entityResponse = response.getEntity();
			is = entityResponse.getContent();

			Header[] h_cookies = response.getHeaders("Set-Cookie");
			for (int i = 0; i < h_cookies.length; i++) {
				Header h_cookie = h_cookies[i];
				if (h_cookie != null) {
					if (m_Cookie == null) {
						m_Cookie = h_cookie.getValue();
					} else {
						m_Cookie += ";" +  h_cookie.getValue();
					}
				}
			}
			/** convert response to string */
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, encode), 8);
			String line = "";
			StringBuilder responseOutput = new StringBuilder();
//			System.out.println("output===============" + reader);
			while ((line = reader.readLine()) != null) {
				responseOutput.append(line);
			}
			reader.close();
			result = responseOutput.toString();
			result = result.replaceAll("\r\n", "\n");
			is.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
//			httpclient.getConnectionManager().shutdown();
		}
		return result;
	}

	public String requestPost(String url, ArrayList<NameValuePair> postData, String referer) {
		InputStream is = null;
		String result = "";
		String encode = "utf-8";
		try {
			/** 연결 타입아웃내에 연결되는지 테스트, 30초 이내에 되지 않는다면 에러 */
			/** 네트웍 연결해서 데이타 받아오기 */
			HttpParams params = httpClient.getParams();
			HttpConnectionParams.setConnectionTimeout(params, 30000);
			HttpConnectionParams.setSoTimeout(params, 30000);

			HttpPost httppost = new HttpPost(url);
			if (referer.length() > 0) {
				httppost.setHeader("referer", referer);
			}

			if (postData != null) {
				UrlEncodedFormEntity entityRequest = new UrlEncodedFormEntity(postData, encode);
				httppost.setEntity(entityRequest);
			}

			HttpResponse response = httpClient.execute(httppost, httpContext);
			HttpEntity entityResponse = response.getEntity();
			is = entityResponse.getContent();

			Header[] h_cookies = response.getHeaders("Set-Cookie");
			for (int i = 0; i < h_cookies.length; i++) {
				Header h_cookie = h_cookies[i];
				if (h_cookie != null) {
					if (m_Cookie == null) {
						m_Cookie = h_cookie.getValue();
					} else {
						m_Cookie += ";" +  h_cookie.getValue();
					}
				}
			}

			/** convert response to string */
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, encode), 8);
			String line = "";
			StringBuilder responseOutput = new StringBuilder();
//			System.out.println("output===============" + reader);
			while ((line = reader.readLine()) != null) {
				responseOutput.append(line);
			}
			reader.close();
			result = responseOutput.toString();

			result = result.replaceAll("\r\n", "\n");
			is.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
//			httpclient.getConnectionManager().shutdown();
		}
		return result;
	}

	public String requestPostWithAttach(String url, HttpEntity entity, String referer, String boundary) {
		InputStream is = null;
		String result = "";
		String encode = "utf-8";
		try {
			/** 연결 타입아웃내에 연결되는지 테스트, 30초 이내에 되지 않는다면 에러 */
			/** 네트웍 연결해서 데이타 받아오기 */
			HttpParams params = httpClient.getParams();
			HttpConnectionParams.setConnectionTimeout(params, 30000);
			HttpConnectionParams.setSoTimeout(params, 30000);
			long contentLength = 0;

			HttpPost httppost = new HttpPost(url);
			if (referer.length() > 0) {
				httppost.setHeader("referer", referer);
			}

			if (entity != null) {
				httppost.setEntity(entity);
//				contentLength = entity.getContentLength();
			}
			httppost.setHeader("Content-type", "multipart/form-data; boundary=" + boundary);
//			httppost.setHeader("Content-Length", String.valueOf(contentLength));

			HttpResponse response = httpClient.execute(httppost, httpContext);
			HttpEntity entityResponse = response.getEntity();
			is = entityResponse.getContent();

			Header[] h_cookies = response.getHeaders("Set-Cookie");
			for (int i = 0; i < h_cookies.length; i++) {
				Header h_cookie = h_cookies[i];
				if (h_cookie != null) {
					if (m_Cookie == null) {
						m_Cookie = h_cookie.getValue();
					} else {
						m_Cookie += ";" +  h_cookie.getValue();
					}
				}
			}

			/** convert response to string */
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, encode), 8);
			String line = "";
			StringBuilder responseOutput = new StringBuilder();
//			System.out.println("output===============" + reader);
			while ((line = reader.readLine()) != null) {
				responseOutput.append(line);
			}
			reader.close();
			result = responseOutput.toString();

			result = result.replaceAll("\r\n", "\n");
			is.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
//			httpclient.getConnectionManager().shutdown();
		}
		return result;
	}

	public String requestGet(String url, String referer) {
		InputStream is = null;
		String result = "";
		String encode = "utf-8";
		try {
			/** 연결 타입아웃내에 연결되는지 테스트, 30초 이내에 되지 않는다면 에러 */
			/** 네트웍 연결해서 데이타 받아오기 */
			HttpParams params = httpClient.getParams();
			HttpConnectionParams.setConnectionTimeout(params, 30000);
			HttpConnectionParams.setSoTimeout(params, 30000);

//		    String cookie = (String)params.getParameter("Cookie");
//            System.out.println("Cookie:" + cookie);

			HttpGet httpget = new HttpGet(url);
			if (referer.length() > 0) {
				httpget.setHeader("referer", referer);
			}

//			UrlEncodedFormEntity entityRequest = new UrlEncodedFormEntity(postData, "UTF-8");
//			httppost.setEntity(entityRequest);

			HttpResponse response = httpClient.execute(httpget, httpContext);
/*
			Header[] headers  = response.getAllHeaders();
            System.out.println("THe header from the httpclient:");

            for(int i=0; i < headers.length; i++){
            Header hd = headers[i];

            System.out.println("Header Name: "+hd.getName()
                    +"       "+" Header Value: "+ hd.getValue());
            }
*/
			HttpEntity entityResponse = response.getEntity();
			is = entityResponse.getContent();

			/** convert response to string */
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, encode), 8);

			Header[] h_cookies = response.getHeaders("Set-Cookie");
			for (int i = 0; i < h_cookies.length; i++) {
				Header h_cookie = h_cookies[i];
				if (h_cookie != null) {
					if (m_Cookie == null) {
						m_Cookie = h_cookie.getValue();
					} else {
						m_Cookie += ";" +  h_cookie.getValue();
					}
				}
			}

/*
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			is.close();
			result = sb.toString();
*/
			String line = "";
			StringBuilder responseOutput = new StringBuilder();
//			System.out.println("output===============" + reader);
			while ((line = reader.readLine()) != null) {
				responseOutput.append(line);
			}
			reader.close();
			result = responseOutput.toString();

			result = result.replaceAll("\r\n", "\n");
			is.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
//			httpclient.getConnectionManager().shutdown();
		}
		return result;
	}
}
