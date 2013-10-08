package com.panicstyle.Moojigae;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CommentWrite extends Activity implements Runnable {
	protected HttpClient httpClient;
	protected HttpContext httpContext;
    private ProgressDialog pd;
    private String mContent;
    private String mBoardID;
    private String mBoardNo;
    private String mCommentNo;
    private boolean bSaveStatus;
    private String mErrorMsg;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment_write);    

        MoojigaeApplication app = (MoojigaeApplication)getApplication();
        httpClient = app.httpClient;
        httpContext = app.httpContext;

        intenter();
        
        findViewById(R.id.okbtn).setOnClickListener(mClickListener);
        findViewById(R.id.cancelbtn).setOnClickListener(mClickListener);
    }

    public void intenter() {
//    	Intent intent = getIntent();  // 값을 가져오는 인텐트 객체생성
    	Bundle extras = getIntent().getExtras();
    	// 가져온 값을 set해주는 부분
    	
    	mBoardID = extras.getString("BOARDID").toString();
    	mBoardNo = extras.getString("BOARDNO").toString();
    	mCommentNo = extras.getString("COMMENTNO").toString();
    }
	
    public void SaveData() {
    	
    	EditText textContent = (EditText)findViewById(R.id.editContent);

    	mContent = textContent.getText().toString();
    	
    	if (mContent.length() <= 0) {
    		AlertDialog.Builder ab = null;
			ab = new AlertDialog.Builder( this );
			ab.setMessage( "입력된 내용이 없습니다. 종료하시겠습니까?");
			ab.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					finish();
				}
			});
			ab.setNegativeButton(android.R.string.cancel, null);
			ab.setTitle( "확인" );
			ab.show();
			return;
    	}
    	
        pd = ProgressDialog.show(this, "", "저장중", true, false);

        Thread thread = new Thread(this);
        thread.start();

        return;
    }

    public void run() {
    	PostData(httpClient, httpContext);
    	handler.sendEmptyMessage(0);
    }

    private Handler handler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
            if(pd != null){
                if(pd.isShowing()){
                    pd.dismiss();
                }
            }
    		if (!bSaveStatus) {
	    		AlertDialog.Builder ab = null;
				ab = new AlertDialog.Builder( CommentWrite.this );
				String strErrorMsg = "글 저장중 오류가 발생했습니다. \n" + mErrorMsg; 
				ab.setMessage(strErrorMsg);
				ab.setPositiveButton(android.R.string.ok, null);
				ab.setTitle( "확인" );
				ab.show();
    		}
    	}
    };        
    	
    protected boolean PostData(HttpClient httpClient, HttpContext httpContext) {
		HttpRequest httpRequest = new HttpRequest();
		
		String url = "http://121.134.211.159/memo-save.do";
		String referer;
		
		if (mCommentNo.length() <= 0) {
			referer = "http://121.134.211.159/board-read.do"; 
		} else {
			referer = "http://121.134.211.159/board-read.do?boardId=" + mBoardID + "&boardNo=" + mBoardNo + "&command=READ&page=1&categoryId=-1"; 
		}

        mContent = mContent.replaceFirst("\n", "<div>");
        mContent = mContent.replaceAll("\n", "</div><div>");
        mContent = mContent + "</div>";
		/* 
		 boardId=%@&
		 page=1&
		 categoryId=-1&
		 time=&
		 returnBoardNo=%@&
		 boardNo=%@&
		 command=%@&
		 totalPage=0&
		 totalRecords=0&
		 serialBadNick=&
		 serialBadContent=&
		 htmlImage=%%2Fout&
		 thumbnailSize=50&
		 memoWriteable=true&
		 list_yn=N&
		 replyList_yn=N&
		 defaultBoardSkin=default&
		 boardWidth=690&
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
		 readOver_color=%%23336699&
		 boardSerialBadNick=&
		 boardSerialBadContent=&
		 userPw=&
		 userNick=&
		 memoContent=%@&
		 memoSeq=%@
		 &pollSeq=&
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
		nameValuePairs.add(new BasicNameValuePair("page", "1"));
		nameValuePairs.add(new BasicNameValuePair("categoryId", "-1"));
		nameValuePairs.add(new BasicNameValuePair("time", ""));
		nameValuePairs.add(new BasicNameValuePair("returnBoardNo", mBoardNo));
		nameValuePairs.add(new BasicNameValuePair("boardNo", mBoardNo));
		if (mCommentNo.length() <= 0) {
			nameValuePairs.add(new BasicNameValuePair("command", "MEMO_WRITE"));
		} else {
			nameValuePairs.add(new BasicNameValuePair("command", "MEMO_REPLY"));
		}
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
		nameValuePairs.add(new BasicNameValuePair("boardWidth", "690"));
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
		nameValuePairs.add(new BasicNameValuePair("memoContent", mContent));
		nameValuePairs.add(new BasicNameValuePair("memoSeq", mCommentNo));
		nameValuePairs.add(new BasicNameValuePair("pollSeq", ""));
		nameValuePairs.add(new BasicNameValuePair("returnURI", ""));
		nameValuePairs.add(new BasicNameValuePair("beforeCommand", ""));
		nameValuePairs.add(new BasicNameValuePair("starPoint", ""));
		nameValuePairs.add(new BasicNameValuePair("provenance", "board-read.do"));
		nameValuePairs.add(new BasicNameValuePair("tagsName", ""));
		nameValuePairs.add(new BasicNameValuePair("pageScale", ""));
		nameValuePairs.add(new BasicNameValuePair("searchOrKey", ""));
		nameValuePairs.add(new BasicNameValuePair("searchOrKey", ""));
		nameValuePairs.add(new BasicNameValuePair("tag", "1"));
		
		String result = httpRequest.requestPost(httpClient, httpContext, url, nameValuePairs, referer, "euc-kr");

        if (result.indexOf("function redirect") < 0) {
            Pattern p = Pattern.compile("(?<=<b>시스템 메세지입니다</b></font><br>)(.|\\n)*?(?=<br>)", Pattern.CASE_INSENSITIVE); 
            Matcher m = p.matcher(result);
            
            if (m.find()) { // Find each match in turn; String can't do this.     
            	mErrorMsg = m.group(0);
            } else {
            	mErrorMsg = "";
            }
			bSaveStatus = false;
			return false;
        }
        
		bSaveStatus = true;
        finish();
    	return true;
    }
    	
    public void CancelData() {
    	finish();
    }
    
    Button.OnClickListener mClickListener = new View.OnClickListener()
    {
      public void onClick(View v)
      {
          switch (v.getId())
          {
          case R.id.okbtn:
               if (getParent() == null) {
               	setResult(Activity.RESULT_OK, new Intent());
               } else {
               	getParent().setResult(Activity.RESULT_OK, new Intent());
               }
               SaveData();
               break;
          case R.id.cancelbtn:
               if (getParent() == null) {
               	setResult(Activity.RESULT_CANCELED, new Intent());
               } else {
               	getParent().setResult(Activity.RESULT_CANCELED, new Intent());
               }
               CancelData();
               break;
          }
      }
    };
}
