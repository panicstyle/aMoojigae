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

public class ArticleView extends Activity implements Runnable {
	/** Called when the activity is first created. */
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
	
	String g_isPNotice;
	String g_isNotice;
	String g_Subject;
	String g_UserName;
	String g_UserID;
	String g_Date;
	String g_Link;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main2);
        webView = (WebView) findViewById(R.id.webView);

        // Look up the AdView as a resource and load a request.
        AdView adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        MoojigaeApplication app = (MoojigaeApplication)getApplication();
        httpClient = app.httpClient;
        httpContext = app.httpContext;
        mUserID = app.mUserID;
		
        intenter();
        
        Pattern p = Pattern.compile("(?<=boardNo=)(.|\\n)*?(?=&)", Pattern.CASE_INSENSITIVE); 
        Matcher m = p.matcher(g_Link);
        
        if (m.find()) { // Find each match in turn; String can't do this.     
        	mBoardNo = m.group(0);
        } else {
        	mBoardNo = "";
        }
        
        LoadData();
    }
        
    public void LoadData() {
		pd = ProgressDialog.show(this, "", "로딩중", true, false);

        Thread thread = new Thread(this);
        thread.start();

        nThreadMode = 1;

        return;
    }

    public void run() {
        if (nThreadMode == 1) {     // Load Data
            if (!getData(httpClient, httpContext)) {
                // Login
                Login login = new Login();

                mLoginStatus = login.LoginTo(httpClient, httpContext, ArticleView.this);

                if (mLoginStatus > 0) {
                    if (getData(httpClient, httpContext)) {
                        mLoginStatus = 1;
                    } else {
                        mLoginStatus = -2;
                    }
                }
            } else {
                mLoginStatus = 1;
            }
        } else if (nThreadMode == 2) {      // Delete Article
            runDeleteArticle();
        } else if (nThreadMode == 3) {      // Delete Comment
            runDeleteComment();
        }
    	handler.sendEmptyMessage(0);
    }

    private Handler handler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
            if (pd != null) {
                if (pd.isShowing()) {
                    pd.dismiss();
                }
            }
            if (nThreadMode == 1) {
                displayData();
            } else {
                if (!bDeleteStatus) {
                    AlertDialog.Builder ab = null;
                    ab = new AlertDialog.Builder( ArticleView.this );
                    ab.setMessage(mErrorMsg);
                    ab.setPositiveButton(android.R.string.ok, null);
                    ab.setTitle( "확인" );
                    ab.show();
                    return;
                } else {
                    if (nThreadMode == 2) {
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
		if (mLoginStatus == -1) {
			AlertDialog.Builder ab = null;
			ab = new AlertDialog.Builder( this );
			ab.setMessage( "로그인 정보가 설정되지 않았습니다. 설정 메뉴를 통해 로그인 정보를 설정하십시오.");
			ab.setPositiveButton(android.R.string.ok, null);
			ab.setTitle( "로그인 오류" );
			ab.show();
		} else if (mLoginStatus == -2){
			AlertDialog.Builder ab = null;
			ab = new AlertDialog.Builder( this );
			ab.setMessage( "게시판을 볼 권한이 없습니다.");
			ab.setPositiveButton(android.R.string.ok, null);
			ab.setTitle( "권한 오류" );
			ab.show();
		} else if (mLoginStatus == 0){
			AlertDialog.Builder ab = null;
			ab = new AlertDialog.Builder( this );
			ab.setMessage( "로그인을 실패했습니다 설정 메뉴를 통해 로그인 정보를 변경하십시오.");
			ab.setPositiveButton(android.R.string.ok, null);
			ab.setTitle( "로그인 오류" );
			ab.show();
		} else {
			webView.getSettings().setJavaScriptEnabled(true);
            webView.loadDataWithBaseURL("http://121.134.211.159", htmlDoc, "text/html", "utf-8", "");
		}
    }

    public void intenter() {
//    	Intent intent = getIntent();  // 값을 가져오는 인텐트 객체생성
    	Bundle extras = getIntent().getExtras();
    	// 가져온 값을 set해주는 부분
    	g_Subject = extras.getString("SUBJECT").toString();
    	g_UserName = extras.getString("USERNAME").toString();
    	g_Date = extras.getString("DATE").toString();
    	g_Link = extras.getString("LINK").toString();
    	mBoardID = extras.getString("BOARDID").toString();
    }

    protected boolean getData(HttpClient httpClient, HttpContext httpContext) {		
		String url = "http://121.134.211.159/" + g_Link;
		HttpRequest httpRequest = new HttpRequest();

        String result = httpRequest.requestGet(httpClient, httpContext, url, "", "euc-kr");

        if (result.indexOf("onclick=\"userLogin()") > 0) {
        	return false;
        }
        mContentOrig = result;
        

/*
        #1 일부 알수 없는 게시글에 대해서 패턴매칭이 안됨
//      Pattern p = Pattern.compile("(?<=<!-- 게시물 레코드 반복-->)(.|\\n)*?(?=<!-- 메모 입력 -->)", Pattern.CASE_INSENSITIVE); 
        Pattern p = Pattern.compile("(<!-- 내용 -->)(.|\\n)*?(<!-- 메모 입력 -->)", Pattern.CASE_INSENSITIVE);

        Matcher m = p.matcher(result);
        
        if (m.find()) { // Find each match in turn; String can't do this.     
        	mContent = m.group(0);
        } else {
        	mContent = "";
        }
*/

        Pattern p = Pattern.compile("(?<=<font class=fTitle><b>제목 : <font size=3>)(.|\\n)*?(?=</font>)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(result);

        String strSubject;
        if (m.find()) { // Find each match in turn; String can't do this.
            strSubject = m.group(0);
        } else {
            strSubject = "";
        }

        int match1, match2;
        String strTitle;

        match1 = result.indexOf("<td class=fSubTitle>");
        if (match1 < 0) return false;
        match2 = result.indexOf("<td class=lReadTop></td>", match1);
        if (match2 < 0) return false;
        strTitle = result.substring(match1, match2);

        p = Pattern.compile("(?<=textDecoration='none'>)(.|\\n)*?(?=</font>)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(strTitle);

        String strUser;
        if (m.find()) { // Find each match in turn; String can't do this.
            strUser = m.group(0);
        } else {
            strUser = "";
        }

        p = Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\d.\\d\\d:\\d\\d:\\d\\d", Pattern.CASE_INSENSITIVE);
        m = p.matcher(strTitle);

        String strUserDate;
        if (m.find()) { // Find each match in turn; String can't do this.
            strUserDate = m.group(0);
        } else {
            strUserDate = "";
        }

        p = Pattern.compile("(?<=<font style=font-style:italic>)(.|\\n)*?(?=</font>)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(strTitle);

        String strHit;
        if (m.find()) { // Find each match in turn; String can't do this.
            strHit = m.group(0);
        } else {
            strHit = "";
        }

        strTitle = "<div class='title'>" + strSubject + "</div><div class='name'><span>" + strUser + "</span>&nbsp;&nbsp;<span>" + strUserDate + "</span>&nbsp;&nbsp;<span>" + strHit + "</span>명이 읽음</div>";


        match1 = result.indexOf("<!-- 내용 -->");
        if (match1 < 0) return false;
        match2 = result.indexOf("<!-- 투표 -->", match1);
        if (match2 < 0) return false;
        mContent = result.substring(match1, match2);

//        mContent = mContent.replaceAll("<meta http-equiv=\\\"Content-Type\\\" content=\\\"text/html; charset=euc-kr\\\">", "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=euc-kr\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no, target-densitydpi=medium-dpi\">");
        mContent = mContent.replaceAll("<td width=200 align=right class=fMemoSmallGray>", "<!--");
        mContent = mContent.replaceAll("<td width=10></td>", "-->");
        mContent = mContent.replaceAll("<!-- 메모에 대한 답변 -->", "<!--");
        mContent = mContent.replaceAll("<!-- <font class=fMemoSmallGray>", "--><!--");
        mContent = mContent.replaceAll("<nobr class=bbscut id=subjectTtl name=subjectTtl>", "");
        mContent = mContent.replaceAll("</nobr>", "");
        mContent = "<div class='content'>" + mContent + "</div>";

        p = Pattern.compile("(<IMG style=)(.|\\n)*?(>)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(mContent);
        while (m.find()) { // Find each match in turn; String can't do this.     
            String matchstr = m.group(0);
            
            Pattern p2 = Pattern.compile("(?<=src=\\\")(.|\\n)*?(?=\\\")", Pattern.CASE_INSENSITIVE); 
            Matcher m2 = p2.matcher(matchstr);
            
            if (m2.find()) { // Find each match in turn; String can't do this.     
            	String imgSrc = m2.group(0);
            	
            	String img = "<img onload=\"resizeImage2(this)\" onclick=\"image_open('" + imgSrc + "', this);\" style=\"CURSOR:hand;\" src=\"" + imgSrc + "\" >";
            	mContent = mContent.replaceFirst("(<IMG style=)(.|\\n)*?(>)", img);
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

        p = Pattern.compile("(?<=<td class=cContent>)(.|\\n)*?(?=</td>)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(strProfile_str);
        String strProfile;
        if (m.find()) { // Find each match in turn; String can't do this.
            strProfile = m.group(0);
        } else {
            strProfile = "None";
        }
        strProfile = "<div class='profile'>" + strProfile + "</div>";

        match1 = result.indexOf("<!-- 메모글 반복 -->");
        if (match1 < 0) return false;
        match2 = result.indexOf("<!-- 메모 입력 -->", match1);
        if (match2 < 0) return false;
        String mComment_str = result.substring(match1, match2);

        String mComment = "";

        String[] items = mComment_str.split("<tr onMouseOver=this.style.backgroundColor='#F0F8FF'; onMouseOut=this.style.backgroundColor=''; class=bMemo>");
        int i = 0;
        for (i = 1; i < items.length; i++) { // Find each match in turn; String can't do this.
            String matchstr = items[i];

            // is Re
            if (matchstr.indexOf("i_memo_reply.gif") >= 0) {
                mComment = mComment + "<div class='re_reply'>";
            } else {
                mComment = mComment + "<div class='reply'>";
            }

            // Name
            p = Pattern.compile("(<font onclick=\\\"viewCharacter)(.|\\n)*?(</font>)", Pattern.CASE_INSENSITIVE);
            m = p.matcher(matchstr);

            String strName;
            if (m.find()) { // Find each match in turn; String can't do this.
                strName = m.group(0);
            } else {
                strName = "";
            }
            strName = strName.replaceAll("<((.|\\n)*?)+>", "");
            mComment = mComment + "<div class='reply_header'>" + strName + " (";

            // Date
            p = Pattern.compile("(?<=<td width=200 align=right class=fMemoSmallGray>)(.|\\n)*?(?=</td>)", Pattern.CASE_INSENSITIVE);
            m = p.matcher(matchstr);

            String strDate;
            if (m.find()) { // Find each match in turn; String can't do this.
                strDate =  m.group(0);
            } else {
                strDate = "";
            }
            strDate = strDate.replaceAll("\n", "");
            strDate = strDate.replaceAll("\r", "");
            strDate = strDate.trim();
            mComment = mComment + strDate + ")</div>";

            // comment
            p = Pattern.compile("(<span id=memoReply_)(.|\\n)*?(<!-- 메모에 대한 답변 -->\n)", Pattern.CASE_INSENSITIVE);
            m = p.matcher(matchstr);

            String strComment;
            if (m.find()) { // Find each match in turn; String can't do this.
                strComment = m.group(0);
            } else {
                strComment = "";
            }
            strComment = strComment.replaceAll("\n", "");
            strComment = strComment.replaceAll("\r", "");
            strComment = strComment.replaceAll("<br>", "\n");
            strComment = strComment.replaceAll("&nbsp;", " ");
//            strComment = strComment.replaceAll("(<)(.|\\n)*?(>)", "");
            mComment = mComment + "<div class='reply_content'>" + strComment + "</div></div>";
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

    	htmlDoc = strHeader + strTitle + strResize + strBody + mContent + strAttach + strProfile + mComment + strBottom;

        return true;
    }

    @Override  
    public boolean onCreateOptionsMenu(Menu menu) {  
        super.onCreateOptionsMenu(menu);  
          
        menu.add(0, 0, 0, "글답변");  
        menu.add(0, 1, 0, "글수정");
        menu.add(0, 2, 0, "글삭제");  
        menu.add(0, 3, 0, "댓글쓰기");  
        menu.add(0, 4, 0, "답변댓글쓰기");  
        menu.add(0, 5, 0, "댓글삭제");  
          
        return true;  
    }  
      
    @Override  
    public boolean onOptionsItemSelected(MenuItem item) {  
        
    	if (item.getItemId() == 0) {	// 글답변
            Intent intent = new Intent(this, ArticleWrite.class);
	        intent.putExtra("BOARDID", mBoardID);
	        intent.putExtra("BOARDNO",  mBoardNo);
            startActivityForResult(intent, REQUEST_WRITE);
    	} else if (item.getItemId() == 1) {	// 글수정
            Intent intent = new Intent(this, ArticleModify.class);
	        intent.putExtra("BOARDID", mBoardID);
	        intent.putExtra("LINK", g_Link);
            startActivityForResult(intent, REQUEST_MODIFY);
            return true;  
    	} else if (item.getItemId() == 2) {	// 글삭제
    		DeleteArticleConfirm();
    	} else if (item.getItemId() == 3) {	// 댓글쓰기
            Intent intent = new Intent(this, CommentWrite.class);
	        intent.putExtra("BOARDID", mBoardID);
	        intent.putExtra("BOARDNO",  mBoardNo);
	        intent.putExtra("COMMENTNO", "");
	        intent.putExtra("COMMENT", "");
            startActivityForResult(intent, REQUEST_COMMENT_WRITE);
    	} else if (item.getItemId() == 4) {	// 답변댓글쓰기
            Intent intent = new Intent(this, CommentView.class);
	        intent.putExtra("CONTENT", mContentOrig);
            startActivityForResult(intent, REQUEST_COMMENT_REPLY_VIEW);
    	} else if (item.getItemId() == 5) {	// 댓글삭제
            Intent intent = new Intent(this, CommentView.class);
	        intent.putExtra("CONTENT", mContentOrig);
            startActivityForResult(intent, REQUEST_COMMENT_DELETE_VIEW);
    	}   
        return false;  
    }
    
    protected void DeleteArticleConfirm() {
		AlertDialog.Builder ab = null;
		ab = new AlertDialog.Builder( this );
		ab.setMessage( "정말 삭제하시겠습니까?");
		ab.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				DeleteArticle();
			}
		});
		ab.setNegativeButton(android.R.string.cancel, null);
		ab.setTitle( "확인" );
		ab.show();
		return;    	
    }
    
    protected void DeleteArticle() {
        pd = ProgressDialog.show(this, "", "삭제중", true, false);

        Thread thread = new Thread(this);
        thread.start();

        nThreadMode = 2;

        return;
    }

    protected void runDeleteArticle() {
		HttpRequest httpRequest = new HttpRequest();
		
		String url = "http://121.134.211.159/board-save.do";
		String referer = "http://121.134.211.159/board-edit.do";

		/*
		boardId=mvHorizonLivingStory&
		page=1&
		categoryId=-1&
		time=1334217622773&
		returnBoardNo=133404944519504&
		boardNo=133404944519504&
		command=DELETE&
		totalPage=0&
		totalRecords=0&
		serialBadNick=&
		serialBadContent=&
		htmlImage=%2Fout&
		thumbnailSize=50&
		memoWriteable=true&
		list_yn=N&
		replyList_yn=N&
		defaultBoardSkin=default&
		boardWidth=710&
		multiView_yn=Y&
		titleCategory_yn=N&
		category_yn=N&
		titleNo_yn=Y&
		titleIcon_yn=N&
		titlePoint_yn=N&
		titleMemo_yn=Y&
		titleNew_yn=Y&
		titleThumbnail_yn=N&
		titleNick_yn=Y&
		titleTag_yn=Y&
		anonymity_yn=N&
		titleRead_yn=Y&
		boardModel_cd=A&
		titleDate_yn=Y&
		tag_yn=Y&
		thumbnailSize=50&
		readOver_color=%23336699&
		boardSerialBadNick=&
		boardSerialBadContent=&
		userPw=&
		userNick=&
		memoContent=&
		memoSeq=&
		pollSeq=&
		returnURI=&
		beforeCommand=&
		starPoint=&
		provenance=board-read.do&
		tagsName=&
		pageScale=&
		searchOrKey=&
		searchType=&
		tag=1
		*/
		
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("boardId", mBoardID));
		nameValuePairs.add(new BasicNameValuePair("page=", "1"));
		nameValuePairs.add(new BasicNameValuePair("categoryId", "-1"));
		nameValuePairs.add(new BasicNameValuePair("time", "1334217622773"));
		nameValuePairs.add(new BasicNameValuePair("returnBoardNo", mBoardNo));
		nameValuePairs.add(new BasicNameValuePair("boardNo", mBoardNo));
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
        nameValuePairs.add(new BasicNameValuePair("Uid", mUserID));

		String result = httpRequest.requestPost(httpClient, httpContext, url, nameValuePairs, referer, "euc-kr");

        bDeleteStatus = true;
        if (result.indexOf("parent.checkLogin()") < 0) {
            Pattern p = Pattern.compile("(?<=<b>시스템 메세지입니다</b></font><br>)(.|\\n)*?(?=<br>)", Pattern.CASE_INSENSITIVE); 
            Matcher m = p.matcher(result);

            bDeleteStatus = false;
            String strErrorMsg;
            if (m.find()) { // Find each match in turn; String can't do this.     
            	strErrorMsg = m.group(0);
            } else {
            	strErrorMsg = "";
            }
			mErrorMsg = "글 삭제중 오류가 발생했습니다. \n" + strErrorMsg;

			return;
        }
/*
        if (getParent() == null) {
           	setResult(Activity.RESULT_OK, new Intent());
        } else {
        	getParent().setResult(Activity.RESULT_OK, new Intent());
        }
        finish();
*/
    }
    
    protected void DeleteCommentConfirm() {
		AlertDialog.Builder ab = null;
		ab = new AlertDialog.Builder( this );
		ab.setMessage( "정말 삭제하시겠습니까?");
		ab.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				DeleteComment();
			}
		});
		ab.setNegativeButton(android.R.string.cancel, null);
		ab.setTitle( "확인" );
		ab.show();
		return;    	
    }
    
    protected void DeleteComment() {
        pd = ProgressDialog.show(this, "", "삭제중", true, false);

        Thread thread = new Thread(this);
        thread.start();

        nThreadMode = 3;

        return;
    }

    protected void runDeleteComment() {
		HttpRequest httpRequest = new HttpRequest();
		
		String url = "http://121.134.211.159/memo-save.do";
		String referer = "http://121.134.211.159/board-read.do?boardId=" + mBoardID + "&boardNo=" + mBoardNo + "&command=READ&page=1&categoryId=-1";
		
		// boardId=mvHorizonLivingStory&page=1&categoryId=-1&time=1374840174050&returnBoardNo=137482716411890&boardNo=137482716411890&command=MEMO_DELETE&totalPage=0&totalRecords=0&serialBadNick=&serialBadContent=&htmlImage=%2Fout&thumbnailSize=50&memoWriteable=true&list_yn=N&replyList_yn=N&defaultBoardSkin=default&boardWidth=710&multiView_yn=Y&titleCategory_yn=N&category_yn=N&titleNo_yn=Y&titleIcon_yn=N&titlePoint_yn=N&titleMemo_yn=Y&titleNew_yn=Y&titleThumbnail_yn=N&titleNick_yn=Y&titleTag_yn=Y&anonymity_yn=N&titleRead_yn=Y&boardModel_cd=A&titleDate_yn=Y&tag_yn=Y&thumbnailSize=50&readOver_color=%23336699&boardSerialBadNick=&boardSerialBadContent=&userPw=&userNick=&memoContent=&memoSeq=1&pollSeq=&returnURI=&beforeCommand=&starPoint=&provenance=board-read.do&tagsName=&pageScale=&searchOrKey=&searchType=&tag=1&Uid=panicstyle
		// page=1&categoryId=-1&time=1374840174050&totalPage=0&totalRecords=0&serialBadNick=&serialBadContent=&htmlImage=%2Fout&thumbnailSize=50&memoWriteable=true&list_yn=N&replyList_yn=N&defaultBoardSkin=default&boardWidth=710&multiView_yn=Y&titleCategory_yn=N&category_yn=N&titleNo_yn=Y&titleIcon_yn=N&titlePoint_yn=N&titleMemo_yn=Y&titleNew_yn=Y&titleThumbnail_yn=N&titleNick_yn=Y&titleTag_yn=Y&anonymity_yn=N&titleRead_yn=Y&boardModel_cd=A&titleDate_yn=Y&tag_yn=Y&thumbnailSize=50&readOver_color=%23336699&boardSerialBadNick=&boardSerialBadContent=&userPw=&userNick=&memoContent=&memoSeq=1&pollSeq=&returnURI=&beforeCommand=&starPoint=&provenance=board-read.do&tagsName=&pageScale=&searchOrKey=&searchType=&tag=1&Uid=panicstyle
		String strParam = "";
		

		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("boardId", mBoardID));
		nameValuePairs.add(new BasicNameValuePair("page", "1"));
		nameValuePairs.add(new BasicNameValuePair("categoryId", "-1"));
		nameValuePairs.add(new BasicNameValuePair("time", "1374840174050"));
		nameValuePairs.add(new BasicNameValuePair("returnBoardNo", mBoardNo));
		nameValuePairs.add(new BasicNameValuePair("boardNo", mBoardNo));
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
		nameValuePairs.add(new BasicNameValuePair("memoSeq", mCommentNo));
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
		nameValuePairs.add(new BasicNameValuePair("Uid", mUserID));

		String result = httpRequest.requestPost(httpClient, httpContext, url, nameValuePairs, referer, "euc-kr");

        bDeleteStatus = true;
        if (result.indexOf("function redirect") < 0) {
            Pattern p = Pattern.compile("(?<=<b>시스템 메세지입니다</b></font><br>)(.|\\n)*?(?=<br>)", Pattern.CASE_INSENSITIVE); 
            Matcher m = p.matcher(result);

            bDeleteStatus = false;
            String strErrorMsg;
            if (m.find()) { // Find each match in turn; String can't do this.     
            	strErrorMsg = m.group(0);
            } else {
            	strErrorMsg = "";
            }
			mErrorMsg = "댓글 삭제중 오류가 발생했습니다. \n" + strErrorMsg;
			return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	Bundle extras;
    	String strCommentNo;
    	Intent newIntent;
    	switch(requestCode) {
    	case REQUEST_WRITE:
    	case REQUEST_MODIFY:
    	case REQUEST_COMMENT_WRITE:
    		if (resultCode == RESULT_OK) {	// resultCode 가 항상 0 으로 넘어옴. 해결책 못 찾음. 일단 SetView 가 실행되면 다시 로딩하자.
    			LoadData();
    	    }
    		break;
    	case REQUEST_COMMENT_REPLY_VIEW:
    		if (resultCode == RESULT_OK) {	// resultCode 가 항상 0 으로 넘어옴. 해결책 못 찾음. 일단 SetView 가 실행되면 다시 로딩하자.
	        	// 가져온 값을 set해주는 부분
    			extras = intent.getExtras();
    			strCommentNo = extras.getString("COMMENTNO").toString();
	
	        	newIntent = new Intent(this, CommentWrite.class);
		        newIntent.putExtra("BOARDID", mBoardID);
		        newIntent.putExtra("BOARDNO", mBoardNo);
		        newIntent.putExtra("COMMENTNO", strCommentNo);
		        startActivityForResult(newIntent, REQUEST_COMMENT_WRITE);
    		}
    		break;
    	case REQUEST_COMMENT_DELETE_VIEW:
    		if (resultCode == RESULT_OK) {	// resultCode 가 항상 0 으로 넘어옴. 해결책 못 찾음. 일단 SetView 가 실행되면 다시 로딩하자.
	        	// 가져온 값을 set해주는 부분
    			extras = intent.getExtras();
	        	mCommentNo = extras.getString("COMMENTNO").toString();
	        	DeleteCommentConfirm();
    		}
    		break;
    	}
    }

}
