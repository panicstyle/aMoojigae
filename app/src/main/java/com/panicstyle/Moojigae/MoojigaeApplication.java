package com.panicstyle.Moojigae;

import android.app.Application;

import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

public class MoojigaeApplication extends Application {

    HttpRequest m_httpRequest;

    @Override
    public void onCreate() {
        /*
         * This populates the default values from the preferences XML file. See
         * {@link DefaultValues} for more details.
         */
//        PreferenceManager.setDefaultValues(this, android.R.xml.default_values, false);
        super.onCreate();

        m_httpRequest = new HttpRequest();
        m_httpRequest.httpClient = new DefaultHttpClient();
        m_httpRequest.httpContext = new BasicHttpContext();
        CookieStore cookieStore = new BasicCookieStore();
        m_httpRequest.httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
    
}
