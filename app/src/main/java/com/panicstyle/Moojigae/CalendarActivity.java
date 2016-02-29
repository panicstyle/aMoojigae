package com.panicstyle.Moojigae;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

public class CalendarActivity extends Activity {
    protected String itemsTitle;
    protected String itemsLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient()); // 응룡프로그램에서 직접 url 처리

        intenter();

        String strUrl = "";
        if (itemsLink.equalsIgnoreCase("ama-cal")) {
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
        itemsTitle = extras.getString("ITEMS_TITLE");
        itemsLink = extras.getString("ITEMS_LINK");
    }
}