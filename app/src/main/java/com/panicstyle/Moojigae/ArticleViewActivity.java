package com.panicstyle.Moojigae;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ArticleViewActivity extends AppCompatActivity implements Runnable {
    private String TAG = "ArticleViewActivity";
	/** Called when the activity is first created. */
    private ProgressDialog m_pd;
    private List<HashMap<String, Object>> m_arrayItems;
    private int m_nThreadMode = 0;
    private boolean m_bDeleteStatus;
    private String m_strErrorMsg;

    private String m_strCommID;
    private String m_strBoardID;
    private String m_strBoardNo;
    private String m_strBoardName;
    private String m_strCommentNo;
    private String m_strComment;
    protected int m_nLoginStatus;

    private int m_nPNotice;
    private int m_nNotice;
    private int m_nMode;
    private String m_strSubject;
    private String m_strName;
    private String m_strID;
    private String m_strHit;
    private String m_strDate;
    private String m_boardNo;

    private String m_strContent;
    private String m_strProfile;
    private String m_strHTML;

    private String m_strUrl;

    private HashMap<String, String> m_mapFileName;
    private MoojigaeApplication m_app;

    WebView webContent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_view);

        // Look up the AdView as a resource and load a request.
        AdView adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        m_app = (MoojigaeApplication)getApplication();

        intenter();

        setTitle(m_strBoardName);

        m_strBoardNo = m_boardNo;

        m_arrayItems = new ArrayList<>();

        webContent = (WebView) findViewById(R.id.webView);

        webContent.setWebViewClient(new myWebClient());

        webContent.addJavascriptInterface(this, "MyApp");
        webContent.getSettings().setJavaScriptEnabled(true);
        webContent.setBackgroundColor(0);

        webContent.clearView();
        webContent.requestLayout();

        String htmlData = "<h3 align='center'>Loading....</h3>";
        webContent.loadData(htmlData, "text/html", "utf-8");

        // m_Cookie 를 각각 배열로 구분하여 처리
        if (m_app.m_httpRequest.m_Cookie != null) {
            String[] cookies = m_app.m_httpRequest.m_Cookie.split(";");
            for (int i = 0; i < cookies.length; i++) {
                CookieManager.getInstance().setCookie(GlobalConst.m_strServer, cookies[i]);
            }
        }
        m_nThreadMode = 1;
        LoadData("로딩중");
    }

    public void LoadData(String strMsg) {
        m_pd = ProgressDialog.show(this, "", strMsg, true, false);

        m_arrayItems.clear();

        Thread thread = new Thread(this);
        thread.start();

    }

    private static class MyHandler extends Handler {
        private final WeakReference<ArticleViewActivity> mActivity;
        public MyHandler(ArticleViewActivity activity) {
            mActivity = new WeakReference<ArticleViewActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            ArticleViewActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    private final MyHandler mHandler = new MyHandler(this);

    public void run() {
        if (m_nThreadMode == 1) {         // LoadData

            if (!getData()) {
                // Login
                SetInfo setInfo = new SetInfo();
                if (!setInfo.GetUserInfo(ArticleViewActivity.this)) {
                    m_app.m_strUserID = "";
                    m_app.m_strUserPW = "";
                    m_app.m_nPushYN = true;
                } else {
                    m_app.m_strUserID = setInfo.m_userID;
                    m_app.m_strUserPW = setInfo.m_userPW;
                    m_app.m_nPushYN = setInfo.m_pushYN;
                }

                Login login = new Login();

                m_nLoginStatus = login.LoginTo(ArticleViewActivity.this, m_app.m_httpRequest, m_app.m_strUserID, m_app.m_strUserPW);
                // m_Cookie 를 각각 배열로 구분하여 처리
                if (m_app.m_httpRequest.m_Cookie != null) {
                    String[] cookies = m_app.m_httpRequest.m_Cookie.split(";");
                    for (int i = 0; i < cookies.length; i++) {
                        CookieManager.getInstance().setCookie(GlobalConst.m_strServer, cookies[i]);
                    }
                }
                if (m_nLoginStatus > 0) {
                    if (getData()) {
                        m_nLoginStatus = 1;
                    } else {
                        m_nLoginStatus = -2;
                    }
                }
            } else {
                m_nLoginStatus = 1;
            }
        } else if (m_nThreadMode == 2) {      // Delete Article
            runDeleteArticle();
        } else if (m_nThreadMode == 3) {      // DeleteComment
            runDeleteComment();
        }
        mHandler.sendEmptyMessage(0);
    }

    private void handleMessage(Message msg) {
        if (m_pd != null) {
            if (m_pd.isShowing()) {
                m_pd.dismiss();
            }
        }
        if (m_nThreadMode == 1) {
            displayData();
        } else {
            if (!m_bDeleteStatus) {
                AlertDialog.Builder ab = null;
                ab = new AlertDialog.Builder( ArticleViewActivity.this );
                ab.setMessage(m_strErrorMsg);
                ab.setPositiveButton(android.R.string.ok, null);
                ab.setTitle( "확인" );
                ab.show();
                return;
            } else {
                if (m_nThreadMode == 2) {
                    if (getParent() == null) {
                        setResult(GlobalConst.RESULT_DELETE, new Intent());
                    } else {
                        getParent().setResult(GlobalConst.RESULT_DELETE, new Intent());
                    }
                    finish();
                } else {
                    m_nThreadMode = 1;
                    LoadData("로딩중");
                }
            }
        }
    }

    public void displayData() {
		if (m_nLoginStatus == -1) {
			AlertDialog.Builder ab = null;
			ab = new AlertDialog.Builder( this );
			ab.setMessage( "로그인 정보가 설정되지 않았습니다. 설정 메뉴를 통해 로그인 정보를 설정하십시오.");
			ab.setPositiveButton(android.R.string.ok, null);
			ab.setTitle( "로그인 오류" );
			ab.show();
		} else if (m_nLoginStatus == -2){
			AlertDialog.Builder ab = null;
			ab = new AlertDialog.Builder( this );
			ab.setMessage( "게시판을 볼 권한이 없습니다.");
			ab.setPositiveButton(android.R.string.ok, null);
			ab.setTitle( "권한 오류" );
			ab.show();
		} else if (m_nLoginStatus == 0){
			AlertDialog.Builder ab = null;
			ab = new AlertDialog.Builder( this );
			ab.setMessage( "로그인을 실패했습니다 설정 메뉴를 통해 로그인 정보를 변경하십시오.");
			ab.setPositiveButton(android.R.string.ok, null);
			ab.setTitle( "로그인 오류" );
			ab.show();
		} else {
            TextView tvSubject;
            TextView tvName;
            TextView tvDate;
            TextView tvHit;
            TextView tvProfile;
            TextView tvCommentCnt;
            ScrollView scrollView;
            LinearLayout ll;

            tvSubject = (TextView) findViewById(R.id.subject);
            tvSubject.setText(m_strSubject);

            tvName = (TextView) findViewById(R.id.name);
            tvName.setText(m_strName);

            tvDate = (TextView) findViewById(R.id.date);
            tvDate.setText(m_strDate);

            tvHit = (TextView) findViewById(R.id.hit);
            tvHit.setText(m_strHit);

            webContent.clearView();
            webContent.requestLayout();
            webContent.loadDataWithBaseURL(GlobalConst.m_strServer, m_strHTML, "text/html", "utf-8", "");

            tvProfile = (TextView) findViewById(R.id.profile);
            tvProfile.setText(m_strProfile);

            String strCommentCnt = String.valueOf(m_arrayItems.size()) + " 개의 댓글";
            tvCommentCnt = (TextView) findViewById(R.id.commentcnt);
            tvCommentCnt.setText(strCommentCnt);

            ll = (LinearLayout) findViewById(R.id.ll);
            ll.removeAllViews();
            for (int i = 0; i < m_arrayItems.size(); i++)
            {
                String cnt = String.valueOf(i);
                HashMap<String, Object> item;
                item = m_arrayItems.get(i);
                String date = (String)item.get("date");
                String name = (String)item.get("name");
                String subject = (String)item.get("comment");
                String isReply = (String)item.get("isReply");
                String commentNo = (String)item.get("commentno");

                LayoutInflater inflater =  (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view;

                if (isReply.equals("1")) {
                    view = inflater.inflate(R.layout.list_article_comment, null);
                } else {
                    view = inflater.inflate(R.layout.list_article_recomment, null);
                }

                TextView dateView = (TextView) view.findViewById(R.id.date);
                TextView nameView = (TextView) view.findViewById(R.id.name);
                TextView subjectView = (TextView) view.findViewById(R.id.subject);
                TextView commentnoView = (TextView) view.findViewById(R.id.commentno);
                ImageButton iconMore = (ImageButton) view.findViewById(R.id.iconmore);

                // Bind the data efficiently with the holder.
                dateView.setText(date);
                nameView.setText(name);
                subjectView.setText(subject);
                commentnoView.setText(commentNo);
                iconMore.setContentDescription(cnt);

                ll.addView(view);
            }

            // DB에 해당 글 번호를 저장한다.
            final DBHelper db = new DBHelper(this);
            ArticleRead read = new ArticleRead(m_strBoardNo, m_strBoardID);
            db.add(read);

            if (getParent() == null) {
                setResult(RESULT_OK, new Intent());
            } else {
                getParent().setResult(RESULT_OK, new Intent());
            }
		}
    }

    public class myWebClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            webContent.loadUrl("javascript:MyApp.resize(document.body.getBoundingClientRect().height)");
            super.onPageFinished(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading (WebView view, String url) {
            boolean shouldOverride = false;
            // We only want to handle requests for mp3 files, everything else the webview
            // can handle normally

            if (url.indexOf("moojigae.or.kr") >= 0 && url.indexOf("downManager?") >= 0) {
                m_strUrl = url;
                AlertDialog.Builder notice = null;
                notice = new AlertDialog.Builder( ArticleViewActivity.this );
//                        notice.setTitle( "" );
                notice.setMessage("첨부파일을 다운로드 하시겠습니까?");
                notice.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        String nKey = Utils.getMatcherFirstString("(?<=&c=)(.|\\n)*?(?=&)", m_strUrl);
                        String fileName = m_mapFileName.get(nKey);
                        DownloadManager.Request request = new DownloadManager.Request(
                                Uri.parse(m_strUrl));
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                        request.addRequestHeader("Cookie", m_app.m_httpRequest.m_Cookie);
                        request.addRequestHeader("Referer", GlobalConst.m_strServer + "/board-api-read.do?boardId=" + m_strBoardID + "&boardNo=" + m_strBoardNo + "&command=READ&categoryId=-1");
                        request.addRequestHeader("Host", GlobalConst.m_strServerName);
// You can change the name of the downloads, by changing "download" to everything you want, such as the mWebview title...
                        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        dm.enqueue(request);
                    }
                });
                notice.setNegativeButton(android.R.string.cancel, null);
                notice.show();

            } else {
                if (url != null ) {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return false;
                }
            }
            return shouldOverride;
        }
    }

    @JavascriptInterface
    public void resize(final float height) {
        ArticleViewActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webContent.setLayoutParams(new LinearLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels - 60, (int) (height * getResources().getDisplayMetrics().density)));
            }
        });
    }

    @JavascriptInterface
    public void invokeImg(final String img_src) {
        Log.d(TAG, img_src);
        try {
            String nKey = Utils.getMatcherFirstString("(?<=&c=)(.|\\n)*?(?=&)", img_src);
            String fileName = m_mapFileName.get(nKey);
            if (fileName == null || fileName.equals("")) {
                URL url = new URL(img_src);
                fileName = FilenameUtils.getName(url.getPath());
            }

            Intent intent = new Intent(ArticleViewActivity.this, ImageActivity.class);
            intent.putExtra("ITEMS_LINK", img_src);
            intent.putExtra("FILENAME", fileName);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void intenter() {
//    	Intent intent = getIntent();  // 값을 가져오는 인텐트 객체생성
    	Bundle extras = getIntent().getExtras();
    	// 가져온 값을 set해주는 부분
    	m_strSubject = extras.getString("SUBJECT");
    	m_strName = extras.getString("USERNAME");
    	m_strID = extras.getString("USERID");
    	m_strDate = extras.getString("DATE");
    	m_boardNo = extras.getString("boardNo");
        m_strHit = extras.getString("HIT");
        m_strBoardID = extras.getString("BOARDID");
        m_strBoardName = extras.getString("boardName");
    }

    protected boolean getData() {
        String url = GlobalConst.m_strServer + "/board-api-read.do?boardId=" + m_strBoardID + "&boardNo=" + m_strBoardNo + "&command=READ&categoryId=-1";
        String result = m_app.m_httpRequest.requestGet(url, "");

        if (result.indexOf("<b>시스템 메세지입니다</b>") > 0) {
            return false;
        }

        try {
            JSONObject boardObject = new JSONObject(result);

            m_strSubject = boardObject.getString("boardTitle");
            m_strSubject = Utils.repalceHtmlSymbol(m_strSubject);

            m_strName =  boardObject.getString("userNick");
            m_strDate = boardObject.getString("boardRegister_dt");
            m_strHit = boardObject.getString("boardRead_cnt");

            m_strContent = boardObject.getString("boardContent");
            m_strContent = "<div>" + m_strContent + "</div>";
            m_strContent += "<div>-</div>";

            int i = 0;
            JSONArray arrayImage = boardObject.getJSONArray("image");
            for (i = 0; i < arrayImage.length(); i++) {
                JSONObject image = arrayImage.getJSONObject(i);
                String fileName = image.getString("fileName");
                fileName = fileName.toLowerCase();
                if (fileName.contains(".jpg")
                        || fileName.contains(".jpeg")
                        || fileName.contains(".png")
                        || fileName.contains(".gif")
                        ) {
                    m_strContent = m_strContent + "<div>" + image.getString("link") + "</div>";
                }
            }
            m_strContent = m_strContent.replaceAll("<img ", "<img onclick=\"myapp_clickImg(this)\" width=300 ");

            String strAttach = "";
            JSONArray arrayAttach = boardObject.getJSONArray("attachment");
            if (arrayAttach.length() > 0) {
                strAttach = "<table boader=1><tr><th>첨부파일</th></tr>";
            }
            m_mapFileName = new HashMap<>();
            for (i = 0; i < arrayAttach.length(); i++) {
                JSONObject attach = arrayAttach.getJSONObject(i);
                strAttach = strAttach + "<tr><td>" + attach.getString("link") + "</td></tr>";

                String n = attach.getString("fileSeq");
                String f = attach.getString("fileName");
                m_mapFileName.put(n, f);
            }
            if (arrayAttach.length() > 0) {
                strAttach = strAttach + "</tr></table>";
            }

            m_strProfile = boardObject.getString("userComment");
            m_strProfile = m_strProfile.replaceAll("<br><br>", "\n");
            m_strProfile = m_strProfile.replaceAll("<br>", "\n");
            m_strProfile = m_strProfile.replaceAll("(<)(.|\\n)*?(>)", "");
            m_strProfile = m_strProfile.replaceAll("&nbsp;", " ");

            HashMap<String, Object> item;
            JSONArray arrayMemo = boardObject.getJSONArray("memo");
            for (i = 0; i < arrayMemo.length(); i++) {
                JSONObject memo = arrayMemo.getJSONObject(i);
                item = new HashMap<>();

                // Comment ID
                item.put("commentno", memo.getString("memoSeq"));

                // is Re
                item.put("isReply", memo.getString("memoDep"));

                // Name
                item.put("name", memo.getString("userNick"));

                // Date
                item.put("date", memo.getString("memoRegister_dt"));

                // comment
                String strComment = memo.getString("memoContent");
                strComment = Utils.repalceHtmlSymbol(strComment);
                item.put("comment", strComment);

                m_arrayItems.add(item);
            }

            String strHeader = "";
//            strHeader += "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">";
            strHeader += "<html><head>";
            strHeader += "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">";
            strHeader += "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no, target-densitydpi=medium-dpi\">";
//            strHeader += "<style>body {font-family:\"고딕\";font-size:medium;}.title{text-margin:10px 0px;font-size:large}.name{color:gray;margin:10px 0px;font-size:small}.profile {text-align:center;color:white;background: lightgray; margin:10px0px;border-radius:5px;font-size:small}.reply{border-bottom:1px solid gray;margin:10px 0px}.reply_header {color:gray;;font-size:small}.reply_content {margin:10px 0px}.re_reply{border-bottom:1px solid gray;margin:10px 0px 0px 20px;background:lightgray}</style>";
//            strHeader += "<style>body {font-family:\"고딕\";font-size:medium;}.title{text-margin:10px 0px;font-size:large}.name{color:gray;margin:10px 0px;font-size:small}.profile {text-align:center;color:white;background: lightgray; margin:10px0px;border-radius:5px;font-size:small}.reply{border-bottom:1px solid gray;margin:10px 0px}.reply_header {color:gray;;font-size:small}.reply_content {margin:10px 0px}.re_reply{border-bottom:1px solid gray;margin:10px 0px 0px 20px;background:lightgray}</style>";
            strHeader += "<script>function myapp_clickImg(obj){MyApp.invokeImg(obj.src);}</script>";
//            strHeader += "<script>function imageResize() { var boardWidth = 300; if (document.cashcow && document.cashcow.boardWidth) boardWidth = document.cashcow.boardWidth.value - 70; var obj = document.getElementsByName('unicornimage'); for (var i = 0; i < obj.length; i++) { if (obj[i].width > boardWidth) obj[i].width = boardWidth; } }</script>";
//            strHeader += "<script>window.onload=imageResize;</script>";
            strHeader += "</head>";
            String strBottom = "<br /><br /></body></html>";

//        String cssStr = "<link href=\"./css/default.css\" rel=\"stylesheet\">";
            String strBody = "<body>";

//    	htmlDoc = strHeader + strTitle + strResize + strBody + mContent + strAttach + strProfile + mComment + strBottom;
            m_strHTML = strHeader + strBody + m_strContent + strAttach + strBottom;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_article_view, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                addComment();
                return true;
            case R.id.menu_more:
                View menuItemView = findViewById(R.id.menu_more); // SAME ID AS MENU ID
                showPopup(menuItemView);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        Menu menu = popup.getMenu();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 0:        // 댓글쓰기
                        addComment();
                        return true;
                    case 1:         // 글답변
                        addReArticle();
                        return true;
                    case 2:        // 글수정
                        modifyArticle();
                        return true;
                    case 3:         // 글삭제
                        DeleteArticleConfirm();
                        return true;
                }
                return true;
            }
        });

        menu.add(0, 0, 0, "댓글쓰기");
        if (m_nPNotice == 0) {
            menu.add(0, 1, 0, "글답변");
            menu.add(0, 2, 0, "글수정");
            menu.add(0, 3, 0, "글삭제");
        }
        popup.show();
    }

    public void addReArticle() {
        Intent intent = new Intent(this, ArticleWriteActivity.class);
        int nMode = 0;      // i is modify article
        intent.putExtra("COMMID", m_strCommID);
        intent.putExtra("BOARDID", m_strBoardID);
        intent.putExtra("BOARDNO", m_strBoardNo);
        intent.putExtra("TITLE", "");
        intent.putExtra("CONTENT", "");
        startActivityForResult(intent, GlobalConst.REQUEST_WRITE);
    }

    public void modifyArticle() {
        Intent intent = new Intent(this, ArticleWriteActivity.class);
        int nMode = 1;      // i is modify article
        intent.putExtra("MODE", nMode);
        intent.putExtra("COMMID", m_strCommID);
        intent.putExtra("BOARDID", m_strBoardID);
        intent.putExtra("BOARDNO", m_strBoardNo);
        intent.putExtra("TITLE", m_strSubject);
        intent.putExtra("CONTENT", m_strContent);
        startActivityForResult(intent, GlobalConst.REQUEST_MODIFY);
    }

    public void addComment() {
        Intent intent = new Intent(this, CommentWriteActivity.class);
        int nMode = 0;      // i is modify article
        intent.putExtra("MODE", nMode);
        intent.putExtra("ISPNOTICE", m_nPNotice);
        intent.putExtra("COMMID", m_strCommID);
        intent.putExtra("BOARDID", m_strBoardID);
        intent.putExtra("BOARDNO", m_strBoardNo);
        intent.putExtra("COMMENTNO", "");
        intent.putExtra("COMMENT", "");
        startActivityForResult(intent, GlobalConst.REQUEST_COMMENT_WRITE);
    }

    protected void DeleteArticleConfirm() {
		AlertDialog.Builder ab = null;
		ab = new AlertDialog.Builder( this );
		ab.setMessage("정말 삭제하시겠습니까?");
		ab.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                DeleteArticle();
            }
        });
		ab.setNegativeButton(android.R.string.cancel, null);
		ab.setTitle("확인");
		ab.show();
    }
    
    protected void DeleteArticle() {
        m_nThreadMode = 2;
        LoadData("삭제중");
    }

    protected void runDeleteArticle() {
        String url = GlobalConst.m_strServer + "/board-save.do";
        String referer = GlobalConst.m_strServer + "/board-edit.do";

        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("boardId", m_strBoardID));
        nameValuePairs.add(new BasicNameValuePair("page=", "1"));
        nameValuePairs.add(new BasicNameValuePair("categoryId", "-1"));
        nameValuePairs.add(new BasicNameValuePair("time", "1334217622773"));
        nameValuePairs.add(new BasicNameValuePair("returnBoardNo", m_strBoardNo));
        nameValuePairs.add(new BasicNameValuePair("boardNo", m_strBoardNo));
        nameValuePairs.add(new BasicNameValuePair("command", "DELETE"));
        nameValuePairs.add(new BasicNameValuePair("totalPage", "0"));
        nameValuePairs.add(new BasicNameValuePair("totalRecords", "0"));
        nameValuePairs.add(new BasicNameValuePair("serialBadNick", ""));
        nameValuePairs.add(new BasicNameValuePair("serialBadContent", ""));
        nameValuePairs.add(new BasicNameValuePair("htmlImage", "%2Fout"));
        nameValuePairs.add(new BasicNameValuePair("thumbnailSize", "50"));
        nameValuePairs.add(new BasicNameValuePair("memoWriteable", "true"));
        nameValuePairs.add(new BasicNameValuePair("list_yn", "N"));
        nameValuePairs.add(new BasicNameValuePair("replyList_yn", "N"));
        nameValuePairs.add(new BasicNameValuePair("defaultBoardSkin", "default"));
        nameValuePairs.add(new BasicNameValuePair("boardWidth", "710"));
        nameValuePairs.add(new BasicNameValuePair("multiView_yn", "Y"));
        nameValuePairs.add(new BasicNameValuePair("titleCategory_yn", "N"));
        nameValuePairs.add(new BasicNameValuePair("category_yn", "N"));
        nameValuePairs.add(new BasicNameValuePair("titleNo_yn", "Y"));
        nameValuePairs.add(new BasicNameValuePair("titleIcon_yn", "N"));
        nameValuePairs.add(new BasicNameValuePair("titlePoint_yn", "N"));
        nameValuePairs.add(new BasicNameValuePair("titleMemo_yn", "Y"));
        nameValuePairs.add(new BasicNameValuePair("titleNew_yn", "Y"));
        nameValuePairs.add(new BasicNameValuePair("titleThumbnail_yn", "N"));
        nameValuePairs.add(new BasicNameValuePair("titleNick_yn", "Y"));
        nameValuePairs.add(new BasicNameValuePair("titleTag_yn", "Y"));
        nameValuePairs.add(new BasicNameValuePair("anonymity_yn", "N"));
        nameValuePairs.add(new BasicNameValuePair("titleRead_yn", "Y"));
        nameValuePairs.add(new BasicNameValuePair("boardModel_cd", "A"));
        nameValuePairs.add(new BasicNameValuePair("titleDate_yn", "Y"));
        nameValuePairs.add(new BasicNameValuePair("tag_yn", "Y"));
        nameValuePairs.add(new BasicNameValuePair("thumbnailSize", "50"));
        nameValuePairs.add(new BasicNameValuePair("readOver_color", "%23336699"));
        nameValuePairs.add(new BasicNameValuePair("boardSerialBadNick", ""));
        nameValuePairs.add(new BasicNameValuePair("boardSerialBadContent", ""));
        nameValuePairs.add(new BasicNameValuePair("userPw", ""));
        nameValuePairs.add(new BasicNameValuePair("userNick", ""));
        nameValuePairs.add(new BasicNameValuePair("memoContent", ""));
        nameValuePairs.add(new BasicNameValuePair("memoSeq", ""));
        nameValuePairs.add(new BasicNameValuePair("pollSeq", ""));
        nameValuePairs.add(new BasicNameValuePair("returnURI", ""));
        nameValuePairs.add(new BasicNameValuePair("beforeCommand", ""));
        nameValuePairs.add(new BasicNameValuePair("starPoint", ""));
        nameValuePairs.add(new BasicNameValuePair("provenance", "board-read.do"));
        nameValuePairs.add(new BasicNameValuePair("tagsName", ""));
        nameValuePairs.add(new BasicNameValuePair("pageScale", ""));
        nameValuePairs.add(new BasicNameValuePair("searchOrKey", ""));
        nameValuePairs.add(new BasicNameValuePair("searchType", ""));
        nameValuePairs.add(new BasicNameValuePair("tag", "1"));
        nameValuePairs.add(new BasicNameValuePair("tagsName", ""));
        nameValuePairs.add(new BasicNameValuePair("Uid", m_app.m_strUserID));

        String result = m_app.m_httpRequest.requestPost(url, nameValuePairs, referer);

        m_bDeleteStatus = true;
        if (result.contains("<b>시스템 메세지입니다</b>")) {
            String strErrorMsg = Utils.getMatcherFirstString("(?<=<b>시스템 메세지입니다</b></font><br>)(.|\\n)*?(?=<br>)", result);
            m_bDeleteStatus = false;
            m_strErrorMsg = "글 삭제중 오류가 발생했습니다. \n" + strErrorMsg;
        }
    }

    public void clickComment(View v) {
        ImageView iv = (ImageView)v;
        String strCnt;
        int cnt;
        strCnt  = (String)iv.getContentDescription();
        cnt = Integer.parseInt(strCnt);
        HashMap<String, Object> item;;
        item = m_arrayItems.get(cnt);
        m_strComment = (String)item.get("comment");
        m_strCommentNo = (String)item.get("commentno");
        PopupMenu popup = new PopupMenu(this, v);
        Menu menu = popup.getMenu();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 0:        // 댓글삭제
                        DeleteCommentConfirm();
                        return true;
                    case 1:         // 댓글답변
                        ReplayComment();
                        return true;
                }
                return true;
            }
        });

        menu.add(0, 0, 0, "삭제");
        menu.add(0, 1, 0, "답변");
        popup.show();
    }

    public void ReplayComment() {
        Intent intent = new Intent(this, CommentWriteActivity.class);
        int nMode = 0;
        intent.putExtra("MODE", nMode);
        intent.putExtra("COMMID", m_strCommID);
        intent.putExtra("BOARDID", m_strBoardID);
        intent.putExtra("BOARDNO", m_strBoardNo);
        intent.putExtra("COMMENTNO", m_strCommentNo);
        intent.putExtra("COMMENT", "");
        startActivityForResult(intent, GlobalConst.REQUEST_COMMENT_WRITE);
    }


    protected void DeleteCommentConfirm() {
		AlertDialog.Builder ab = null;
		ab = new AlertDialog.Builder( this );
		ab.setMessage("정말 삭제하시겠습니까?");
		ab.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                DeleteComment();
            }
        });
		ab.setNegativeButton(android.R.string.cancel, null);
		ab.setTitle("확인");
		ab.show();
    }
    
    protected void DeleteComment() {
        m_nThreadMode = 3;
        LoadData("삭제중");
    }

    protected void runDeleteComment() {
        String url = GlobalConst.m_strServer + "/memo-save.do";
        String referer = GlobalConst.m_strServer + "/board-read.do?boardId=" + m_strBoardID + "&boardNo=" + m_strBoardNo + "&command=READ&page=1&categoryId=-1";

        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("boardId", m_strBoardID));
        nameValuePairs.add(new BasicNameValuePair("page", "1"));
        nameValuePairs.add(new BasicNameValuePair("categoryId", "-1"));
        nameValuePairs.add(new BasicNameValuePair("time", "1374840174050"));
        nameValuePairs.add(new BasicNameValuePair("returnBoardNo", m_strBoardNo));
        nameValuePairs.add(new BasicNameValuePair("boardNo", m_strBoardNo));
        nameValuePairs.add(new BasicNameValuePair("command", "MEMO_DELETE"));
        nameValuePairs.add(new BasicNameValuePair("totalPage", "0"));
        nameValuePairs.add(new BasicNameValuePair("totalRecords", "0"));
        nameValuePairs.add(new BasicNameValuePair("serialBadNick", ""));
        nameValuePairs.add(new BasicNameValuePair("serialBadContent", ""));
        nameValuePairs.add(new BasicNameValuePair("htmlImage", "%%2Fout"));
        nameValuePairs.add(new BasicNameValuePair("thumbnailSize", "50"));
        nameValuePairs.add(new BasicNameValuePair("memoWriteable", "true"));
        nameValuePairs.add(new BasicNameValuePair("list_yn", "N"));
        nameValuePairs.add(new BasicNameValuePair("replyList_yn", "N"));
        nameValuePairs.add(new BasicNameValuePair("defaultBoardSkin", "default"));
        nameValuePairs.add(new BasicNameValuePair("boardWidth", "710"));
        nameValuePairs.add(new BasicNameValuePair("multiView_yn", "Y"));
        nameValuePairs.add(new BasicNameValuePair("titleCategory_yn", "N"));
        nameValuePairs.add(new BasicNameValuePair("category_yn", "N"));
        nameValuePairs.add(new BasicNameValuePair("titleNo_yn", "Y"));
        nameValuePairs.add(new BasicNameValuePair("titleIcon_yn", "N"));
        nameValuePairs.add(new BasicNameValuePair("titlePoint_yn", "N"));
        nameValuePairs.add(new BasicNameValuePair("titleMemo_yn", "Y"));
        nameValuePairs.add(new BasicNameValuePair("titleNew_yn", "Y"));
        nameValuePairs.add(new BasicNameValuePair("titleThumbnail_yn", "N"));
        nameValuePairs.add(new BasicNameValuePair("titleNick_yn", "Y"));
        nameValuePairs.add(new BasicNameValuePair("titleTag_yn", "Y"));
        nameValuePairs.add(new BasicNameValuePair("anonymity_yn", "N"));
        nameValuePairs.add(new BasicNameValuePair("titleRead_yn", "Y"));
        nameValuePairs.add(new BasicNameValuePair("boardModel_cd", "A"));
        nameValuePairs.add(new BasicNameValuePair("titleDate_yn", "Y"));
        nameValuePairs.add(new BasicNameValuePair("tag_yn", "Y"));
        nameValuePairs.add(new BasicNameValuePair("thumbnailSize", "50"));
        nameValuePairs.add(new BasicNameValuePair("readOver_color", "%%23336699"));
        nameValuePairs.add(new BasicNameValuePair("boardSerialBadNick", ""));
        nameValuePairs.add(new BasicNameValuePair("boardSerialBadContent", ""));
        nameValuePairs.add(new BasicNameValuePair("userPw", ""));
        nameValuePairs.add(new BasicNameValuePair("userNick", ""));
        nameValuePairs.add(new BasicNameValuePair("memoContent", ""));
        nameValuePairs.add(new BasicNameValuePair("memoSeq", m_strCommentNo));
        nameValuePairs.add(new BasicNameValuePair("pollSeq", ""));
        nameValuePairs.add(new BasicNameValuePair("returnURI", ""));
        nameValuePairs.add(new BasicNameValuePair("beforeCommand", ""));
        nameValuePairs.add(new BasicNameValuePair("starPoint", ""));
        nameValuePairs.add(new BasicNameValuePair("provenance", "board-read.do"));
        nameValuePairs.add(new BasicNameValuePair("tagsName", ""));
        nameValuePairs.add(new BasicNameValuePair("pageScale", ""));
        nameValuePairs.add(new BasicNameValuePair("searchOrKey", ""));
        nameValuePairs.add(new BasicNameValuePair("searchType", ""));
        nameValuePairs.add(new BasicNameValuePair("tag", "-1"));
        nameValuePairs.add(new BasicNameValuePair("Uid", m_app.m_strUserID));

        String result = m_app.m_httpRequest.requestPost(url, nameValuePairs, referer);

        m_bDeleteStatus = true;
        if (result.contains("<b>시스템 메세지입니다</b>")) {
            String strErrorMsg = Utils.getMatcherFirstString("(?<=<b>시스템 메세지입니다</b></font><br>)(.|\\n)*?(?=<br>)", result);
            m_bDeleteStatus = false;
            m_strErrorMsg = "댓글 삭제중 오류가 발생했습니다. \n" + strErrorMsg;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GlobalConst.REQUEST_MODIFY:
                case GlobalConst.REQUEST_COMMENT_WRITE:
                case GlobalConst.REQUEST_COMMENT_MODIFY:
                    m_nThreadMode = 1;
                    LoadData("로딩중");
                    break;
                case GlobalConst.REQUEST_WRITE:
                    if (getParent() == null) {
                        setResult(RESULT_OK, new Intent());
                    } else {
                        getParent().setResult(RESULT_OK, new Intent());
                    }
                    finish();
                    break;
                default:
                    break;
            }
        }
    }
}
