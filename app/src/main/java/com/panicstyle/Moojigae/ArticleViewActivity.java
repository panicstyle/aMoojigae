package com.panicstyle.Moojigae;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArticleViewActivity extends AppCompatActivity implements Runnable {
	/** Called when the activity is first created. */
    private HttpRequest m_httpRequest;
    private ProgressDialog m_pd;
    private List<HashMap<String, Object>> m_arrayItems;
    private int m_nThreadMode = 0;
    private boolean m_bDeleteStatus;
    private String m_strErrorMsg;

    static final int REQUEST_WRITE = 1;
    static final int REQUEST_MODIFY = 2;
    static final int REQUEST_COMMENT_WRITE = 3;
    static final int REQUEST_COMMENT_MODIFY = 4;
    static final int REQUEST_COMMENT_REPLY_VIEW = 5;
    static final int REQUEST_COMMENT_MODIFY_VIEW = 6;
    static final int REQUEST_COMMENT_DELETE_VIEW = 7;

    private String m_strCommID;
    private String m_strBoardID;
    private String m_strBoardNo;
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
    private String m_strLink;

    private String m_strContent;
    private String m_strProfile;
    private String m_strHTML;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_view);
        setTitle("글보기");

        // Look up the AdView as a resource and load a request.
        AdView adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        MoojigaeApplication app = (MoojigaeApplication)getApplication();
        m_httpRequest = app.m_httpRequest;

        intenter();

        m_arrayItems = new ArrayList<>();

        LoadData();
    }

    public void LoadData() {
        m_pd = ProgressDialog.show(this, "", "로딩중", true, false);

        Thread thread = new Thread(this);
        thread.start();

        m_nThreadMode = 1;
    }

    public void run() {
        if (m_nThreadMode == 1) {         // LoadData
            if (!getData()) {
                // Login
                Login login = new Login();

                m_nLoginStatus = login.LoginTo(ArticleViewActivity.this, m_httpRequest);

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
            if (m_nPNotice == 0) {
                runDeleteComment();
            } else {
                runDeleteCommentPNotice();
            }
        }
        handler.sendEmptyMessage(0);
    }

    private Handler handler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
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
                            setResult(Activity.RESULT_OK, new Intent());
                        } else {
                            getParent().setResult(Activity.RESULT_OK, new Intent());
                        }
                        finish();
                    } else {
                        LoadData();
                    }
                }
            }
    	}
    };

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
            WebView webContent;
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

            webContent = (WebView) findViewById(R.id.webView);
            webContent.getSettings().setJavaScriptEnabled(true);
            webContent.setBackgroundColor(0);
            webContent.loadDataWithBaseURL("http://www.moojigae.or.kr", m_strHTML, "text/html", "utf-8", "");

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
                int isReply = (Integer)item.get("isReply");
                String commentNo = (String)item.get("commentno");

                LayoutInflater inflater =  (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view;

                if (isReply == 0) {
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
    	m_strLink = extras.getString("LINK");
        m_strHit = extras.getString("HIT");
        m_strBoardID = extras.getString("BOARDID");
    }

    protected boolean getData() {
        String url = "http://121.134.211.159/" + m_strLink;
        String result = m_httpRequest.requestGet(url, "", "euc-kr");

        if (result.indexOf("onclick=\"userLogin()") > 0) {
            return false;
        }
        m_strSubject = Utils.getMatcherFirstString("(?<=<font class=fTitle><b>제목 : <font size=3>)(.|\\n)*?(?=</font>)", result);

        int match1, match2;
        String strTitle;

        match1 = result.indexOf("<td class=fSubTitle>");
        if (match1 < 0) return false;
        match2 = result.indexOf("<td class=lReadTop></td>", match1);
        if (match2 < 0) return false;
        strTitle = result.substring(match1, match2);

        m_strName = Utils.getMatcherFirstString("(?<=textDecoration='none'>)(.|\\n)*?(?=</font>)", strTitle);
        m_strDate = Utils.getMatcherFirstString("\\d\\d\\d\\d-\\d\\d-\\d\\d.\\d\\d:\\d\\d:\\d\\d", strTitle);
        m_strHit = Utils.getMatcherFirstString("(?<=<font style=font-style:italic>)(.|\\n)*?(?=</font>)", strTitle);

        match1 = result.indexOf("<!-- 내용 -->");
        if (match1 < 0) return false;
        match2 = result.indexOf("<!-- 투표 -->", match1);
        if (match2 < 0) return false;
        m_strContent = result.substring(match1, match2);

//        mContent = mContent.replaceAll("<meta http-equiv=\\\"Content-Type\\\" content=\\\"text/html; charset=euc-kr\\\">", "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=euc-kr\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no, target-densitydpi=medium-dpi\">");
        m_strContent = m_strContent.replaceAll("<td width=200 align=right class=fMemoSmallGray>", "<!--");
        m_strContent = m_strContent.replaceAll("<td width=10></td>", "-->");
        m_strContent = m_strContent.replaceAll("<!-- 메모에 대한 답변 -->", "<!--");
        m_strContent = m_strContent.replaceAll("<!-- <font class=fMemoSmallGray>", "--><!--");
        m_strContent = m_strContent.replaceAll("<nobr class=bbscut id=subjectTtl name=subjectTtl>", "");
        m_strContent = m_strContent.replaceAll("</nobr>", "");
        m_strContent = "<div class='content'>" + m_strContent + "</div>";

        Matcher m = Utils.getMatcher("(<IMG style=)(.|\\n)*?(>)", m_strContent);
        while (m.find()) { // Find each match in turn; String can't do this.
            String matchstr = m.group(0);

            Matcher m2 = Utils.getMatcher("(?<=src=\\\")(.|\\n)*?(?=\\\")", matchstr);
            if (m2.find()) { // Find each match in turn; String can't do this.
                String imgSrc = m2.group(0);
                String img = "<img onload=\"resizeImage2(this)\" onclick=\"image_open('" + imgSrc + "', this);\" style=\"CURSOR:hand;\" src=\"" + imgSrc + "\" >";
                m_strContent = m_strContent.replaceFirst("(<IMG style=)(.|\\n)*?(>)", img);
            }
        }

        match1 = result.indexOf("<!-- 업로드 파일 정보  수정본 Edit By Yang --> ");
        if (match1 < 0) return false;
        match2 = result.indexOf("<!-- 평가 -->", match1);
        if (match2 < 0) return false;
        String strAttach = result.substring(match1, match2);
        strAttach = "<div class='attach'>" + strAttach + "</div>";

        match1 = result.indexOf("<!-- 별점수 -->");
        if (match1 < 0) return false;
        match2 = result.indexOf("<!-- 관련글 -->", match1);
        if (match2 < 0) return false;
        String strProfile_str = result.substring(match1, match2);

        m_strProfile = Utils.getMatcherFirstString("(?<=<td class=cContent>)(.|\\n)*?(?=</td>)", strProfile_str);
        m_strProfile = m_strProfile.replaceAll("<br><br>", "\n");
        m_strProfile = m_strProfile.replaceAll("<br>", "\n");
        m_strProfile = m_strProfile.replaceAll("(<)(.|\\n)*?(>)", "");
        m_strProfile = m_strProfile.replaceAll("&nbsp;", " ");

        match1 = result.indexOf("<!-- 메모글 반복 -->");
        if (match1 < 0) return false;
        match2 = result.indexOf("<!-- 메모 입력 -->", match1);
        if (match2 < 0) return false;
        String mComment_str = result.substring(match1, match2);

        String[] items = mComment_str.split("<tr onMouseOver=this.style.backgroundColor='#F0F8FF'; onMouseOut=this.style.backgroundColor=''; class=bMemo>");
        int i = 0;
        // 각 항목 찾기
        HashMap<String, Object> item;
        for (i = 1; i < items.length; i++) { // Find each match in turn; String can't do this.
            String matchstr = items[i];
            item = new HashMap<>();

            // Comment ID
            String strCommentNo = Utils.getMatcherFirstString("(?<=<span id=memoReply_\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d_)(.|\\n)*?(?=>)", matchstr);
            item.put("commentno", strCommentNo);

            // is Re
            if (matchstr.contains("i_memo_reply.gif")) {
                item.put("isReply", 1);
            } else {
                item.put("isReply", 0);
            }

            // Name
            String strName = Utils.getMatcherFirstString("(<font onclick=\\\"viewCharacter)(.|\\n)*?(</font>)", matchstr);
            strName = Utils.repalceTag(strName);
            item.put("name", strName);

            // Date
            String strDate = Utils.getMatcherFirstString("(?<=<td width=200 align=right class=fMemoSmallGray>)(.|\\n)*?(?=</td>)", matchstr);
            strDate = strDate.replaceAll("\n", "");
            strDate = strDate.replaceAll("\r", "");
            strDate = strDate.trim();
            item.put("date", strDate);

            // comment
            String strComment = Utils.getMatcherFirstString("(<span id=memoReply_)(.|\\n)*?(<!-- 메모에 대한 답변 -->\n)", matchstr);
            strComment = Utils.repalceHtmlSymbol(strComment);
            item.put("comment", strComment);

            m_arrayItems.add(item);
        }

        String strHeader = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">";
        strHeader += "<html><head>";
        strHeader += "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=euc-kr\">";
        strHeader += "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no, target-densitydpi=medium-dpi\">";
        strHeader += "<style>body {font-family:\"고딕\";font-size:medium;}.title{text-margin:10px 0px;font-size:large}.name{color:gray;margin:10px 0px;font-size:small}.content{border-top:1px solid gray}.profile {text-align:center;color:white;background: lightgray; margin:10px0px;border-radius:5px;font-size:small}.reply{border-bottom:1px solid gray;margin:10px 0px}.reply_header {color:gray;;font-size:small}.reply_content {margin:10px 0px}.re_reply{border-bottom:1px solid gray;margin:10px 0px 0px 20px;background:lightgray}</style>";
        strHeader += "</head>";
        String strBottom = "</body></html>";
        String strResize = "<script>function resizeImage2(mm){var width = eval(mm.width);var height = eval(mm.height);if( width > 300 ){var p_height = 300 / width;var new_height = height * p_height;eval(mm.width = 300);eval(mm.height = new_height);}} function image_open(src, mm) { var width = eval(mm.width); window.open(src,'image');}</script>";
//        String cssStr = "<link href=\"./css/default.css\" rel=\"stylesheet\">";
        String strBody = "<body>";

//    	htmlDoc = strHeader + strTitle + strResize + strBody + mContent + strAttach + strProfile + mComment + strBottom;
        m_strHTML = strHeader + strResize + strBody + m_strContent + strAttach + strBottom;

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
        startActivityForResult(intent, REQUEST_WRITE);
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
        startActivityForResult(intent, REQUEST_MODIFY);
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
        startActivityForResult(intent, REQUEST_COMMENT_WRITE);
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
        m_pd = ProgressDialog.show(this, "", "삭제중", true, false);

        Thread thread = new Thread(this);
        thread.start();

        m_nThreadMode = 2;
    }

    protected void runDeleteArticle() {
		String url = "http://cafe.gongdong.or.kr/cafe.php?mode=del&sort=" + m_strBoardID + "&sub_sort=&p1=" + m_strCommID + "&p2=";

        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("number", m_strBoardNo));
        nameValuePairs.add(new BasicNameValuePair("passwd", ""));

		String result = m_httpRequest.requestPost(url, nameValuePairs, url, "utf-8");

        m_bDeleteStatus = true;
        if (result.contains("<meta http-equiv=\"refresh\" content=\"0;url=/cafe.php?sort=")) {
            String strErrorMsg = Utils.getMatcherFirstString("(?<=window.alert\\(\\\")(.|\\n)*?(?=\\\")", result);
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
                        ModifyComment();
                        return true;
                    case 1:        // 댓글삭제
                        DeleteCommentConfirm();
                        return true;
                    case 2:         // 댓글답변
                        ReplayComment();
                        return true;
                }
                return true;
            }
        });

        menu.add(0, 0, 0, "수정");
        menu.add(0, 1, 0, "삭제");
        if (m_nPNotice == 0) {
            menu.add(0, 2, 0, "답변");
        }
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
        intent.putExtra("COMMENT",  "");
        startActivityForResult(intent, REQUEST_COMMENT_WRITE);
    }


    protected void ModifyComment() {
        Intent intent = new Intent(this, CommentWriteActivity.class);
        int nMode = 1;
        intent.putExtra("MODE", nMode);
        intent.putExtra("ISPNOTICE", m_nPNotice);
        intent.putExtra("COMMID", m_strCommID);
        intent.putExtra("BOARDID", m_strBoardID);
        intent.putExtra("BOARDNO",  m_strBoardNo);
        intent.putExtra("COMMENTNO",  m_strCommentNo);
        intent.putExtra("COMMENT",  m_strComment);
        startActivityForResult(intent, REQUEST_COMMENT_MODIFY);
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
        m_pd = ProgressDialog.show(this, "", "삭제중", true, false);

        Thread thread = new Thread(this);
        thread.start();

        m_nThreadMode = 3;
    }

    protected void runDeleteComment() {
		HttpRequest httpRequest = new HttpRequest();
		
		String url = "http://cafe.gongdong.or.kr/cafe.php?mode=del_reply&sort=" + m_strBoardID + "&sub_sort=&p1=" + m_strCommID + "&p2=";

        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("number", m_strCommentNo));

		String result  = m_httpRequest.requestPost(url, nameValuePairs, url, "utf-8");

        m_bDeleteStatus = true;
        if (result.contains("<meta http-equiv=\"refresh\" content=\"0;url=/cafe.php?sort=")) {
            String strErrorMsg = Utils.getMatcherFirstString("(?<=window.alert\\(\\\")(.|\\n)*?(?=\\\")", result);
            m_bDeleteStatus = false;
			m_strErrorMsg = "댓글 삭제중 오류가 발생했습니다. \n" + strErrorMsg;
        }
    }

    protected void runDeleteCommentPNotice() {
        String url = "http://www.gongdong.or.kr/index.php";
        String strPostParam = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                "<methodCall>\n" +
                "<params>\n" +
                "<_filter><![CDATA[delete_comment]]></_filter>\n" +
                "<error_return_url><![CDATA[/index.php?mid=notice&document_srl=" + m_strBoardNo + "&act=dispBoardDeleteComment&comment_srl=" + m_strCommentNo + "]]></error_return_url>\n" +
                "<act><![CDATA[procBoardDeleteComment]]></act>\n" +
                "<mid><![CDATA[notice]]></mid>\n" +
                "<document_srl><![CDATA[" + m_strBoardNo + "]]></document_srl>\n" +
                "<comment_srl><![CDATA[" + m_strCommentNo + "]]></comment_srl>\n" +
                "<module><![CDATA[board]]></module>\n" +
                "</params>\n" +
                "</methodCall>";
        String strReferer = "http://www.gongdong.or.kr/index.php?mid=notice&document_srl=" + m_strBoardNo + "&act=dispBoardDeleteComment&comment_srl=" + m_strCommentNo;
        String result  = m_httpRequest.requestPost(url, strPostParam, strReferer, "utf-8");

        m_bDeleteStatus = true;
        if (result.contains("<error>0</error>")) {
            String strErrorMsg = Utils.getMatcherFirstString("(?<=<message>)(.|\\n)*?(?=</message>)", result);
            m_bDeleteStatus = false;
            m_strErrorMsg = "댓글 삭제중 오류가 발생했습니다. \n" + strErrorMsg;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	switch(requestCode) {
            case REQUEST_MODIFY:
            case REQUEST_COMMENT_WRITE:
            case REQUEST_COMMENT_MODIFY:
                if (resultCode == RESULT_OK) {

                    m_pd = ProgressDialog.show(this, "", "로딩중입니다. 잠시만 기다리십시오...", true, false);

                    Thread thread = new Thread(this);
                    thread.start();
                }
                break;
            case REQUEST_WRITE:
                if (getParent() == null) {
                    setResult(Activity.RESULT_OK, new Intent());
                } else {
                    getParent().setResult(Activity.RESULT_OK, new Intent());
                }
                finish();
                break;
            default:
                break;
    	}
    }
}
