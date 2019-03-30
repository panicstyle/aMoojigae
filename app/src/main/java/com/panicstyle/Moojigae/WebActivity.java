package com.panicstyle.Moojigae;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebActivity extends AppCompatActivity {
    protected String itemsTitle;
    protected String itemsLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient()); // 응룡프로그램에서 직접 url 처리

        intenter();

        setTitle(itemsTitle);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(itemsLink);
    }

    public void intenter() {
//    	Intent intent = getIntent();  // 값을 가져오는 인텐트 객체생성
        Bundle extras = getIntent().getExtras();
        // 가져온 값을 set해주는 부분
        itemsTitle = extras.getString("ITEMS_TITLE");
        itemsLink = extras.getString("ITEMS_LINK");
    }
}