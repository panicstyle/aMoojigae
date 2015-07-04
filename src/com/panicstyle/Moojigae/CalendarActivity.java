package com.panicstyle.Moojigae;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.panicstyle.Moojigae.HttpRequest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;
import android.webkit.WebView;
import android.widget.Toast;

public class CalendarActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
//	protected String itemsTitle;
//	protected String itemsLink;
    protected String mBoardID;
    protected String mBoardNo;
    protected HttpClient httpClient;
    protected HttpContext httpContext;
    private ProgressDialog pd;
    String htmlDoc;
    String mContent;
    String mContentOrig;
    String mErrorMsg;
    int nThreadMode = 0;
    boolean bDeleteStatus;
    static final int REQUEST_WRITE = 1;
    static final int REQUEST_MODIFY = 2;
    static final int REQUEST_COMMENT_WRITE = 3;
    static final int REQUEST_COMMENT_REPLY_VIEW = 4;
    static final int REQUEST_COMMENT_DELETE_VIEW = 5;
    String mCommentNo;
    String mUserID;
    protected int mLoginStatus;
    private WebView webView;

    protected String itemsTitle;
    protected String itemsLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main2);
        webView = (WebView) findViewById(R.id.webView);

        // Look up the AdView as a resource and load a request.
        AdView adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        MoojigaeApplication app = (MoojigaeApplication) getApplication();
        httpClient = app.httpClient;
        httpContext = app.httpContext;
        mUserID = app.mUserID;

        intenter();

        String strUrl = "";
        if (itemsLink.equalsIgnoreCase("ama")) {
            setTitle("아마표");
            strUrl = "https://www.google.com/calendar/embed?showTitle=0&mode=AGENDA&height=900&wkst=1&bgcolor=%239999ff&src=eltkpocrfnkkrpv9b0m2bkigeg%40group.calendar.google.com&color=%23BE6D00&src=moojigae1004%40gmail.com&color=%230D7813&ctz=Asia%2FSeoul";
        } else if (itemsLink.equalsIgnoreCase("maul-cal")) {
            setTitle("전체일정");
            strUrl = "https://www.google.com/calendar/embed?showTitle=0&height=600&wkst=1&hl=ko&bgcolor=%23388faf&src=vmoojigae%40gmail.com&color=%232F6309&src=ko.south_korea%23holiday%40group.v.calendar.google.com&color=%232952A3&ctz=Asia%2FSeoul";
        } else if (itemsLink.equalsIgnoreCase("school2-cal")) {
            setTitle("전체일정");
            strUrl = "https://www.google.com/calendar/embed?height=600&wkst=1&bgcolor=%23ff9900&src=highmoojigae%40gmail.com&color=%23182C57&src=mrainbow7778%40gmail.com&color=%232F6309&src=ko.south_korea%23holiday%40group.v.calendar.google.com&color=%23691426&src=j53dmgghg69bjl05e2ntq82dro%40group.calendar.google.com&color=%23125A12&ctz=Asia%2FSeoul";
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(strUrl);
    }

    public void intenter() {
//    	Intent intent = getIntent();  // 값을 가져오는 인텐트 객체생성
        Bundle extras = getIntent().getExtras();
        // 가져온 값을 set해주는 부분
        itemsTitle = extras.getString("ITEMS_TITLE").toString();
        itemsLink = extras.getString("ITEMS_LINK").toString();
    }
}