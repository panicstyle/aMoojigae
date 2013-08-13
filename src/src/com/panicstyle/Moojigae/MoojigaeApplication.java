package com.panicstyle.Moojigae;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.app.Application;

public class MoojigaeApplication extends Application {
	public HttpClient httpClient;
	public HttpContext httpContext;
	public CookieStore cookieStore;
	public String mUserID;
	
    @Override
    public void onCreate() {
        /*
         * This populates the default values from the preferences XML file. See
         * {@link DefaultValues} for more details.
         */
//        PreferenceManager.setDefaultValues(this, android.R.xml.default_values, false);
		httpClient = new DefaultHttpClient();
		httpContext = new BasicHttpContext();
		cookieStore = new BasicCookieStore();
		httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);    	
    }

    @Override
    public void onTerminate() {
    }
    
}
