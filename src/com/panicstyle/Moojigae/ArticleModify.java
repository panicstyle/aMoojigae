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

public class ArticleModify extends Activity implements Runnable {
	protected HttpClient httpClient;
	protected HttpContext httpContext;
    private ProgressDialog pd;
    private String mTitle;
    private String mContent;
    private String mBoardID;
    private String mLink;
    private String mBoardNo;
    private int nMode;
    private boolean bSaveStatus;
    private String mErrorMsg;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article_write);    

        MoojigaeApplication app = (MoojigaeApplication)getApplication();
        httpClient = app.httpClient;
        httpContext = app.httpContext;

        intenter();
        
        findViewById(R.id.okbtn).setOnClickListener(mClickListener);
        findViewById(R.id.cancelbtn).setOnClickListener(mClickListener);
        
		pd = ProgressDialog.show(this, "", "로딩중입니다. 잠시만 기다리십시오...", true, false);

		nMode = 0;
        Thread thread = new Thread(this);
        thread.start();
    }

    public void intenter() {
//    	Intent intent = getIntent();  // 값을 가져오는 인텐트 객체생성
    	Bundle extras = getIntent().getExtras();
    	// 가져온 값을 set해주는 부분
    	mBoardID = extras.getString("BOARDID").toString();
    	mLink = extras.getString("LINK").toString();
    }

    protected boolean getData(HttpClient httpClient, HttpContext httpContext) {		
		String url = "http://121.134.211.159/" + mLink;
		HttpRequest httpRequest = new HttpRequest();

        String result = httpRequest.requestGet(httpClient, httpContext, url, "", "euc-kr");

        Pattern p = Pattern.compile("(?<=<font class=fTitle><b>제목 : <font size=3>)(.|\\n)*?(?=</font>)", Pattern.CASE_INSENSITIVE); 
        Matcher m = p.matcher(result);
        
        if (m.find()) { // Find each match in turn; String can't do this.     
        	mTitle = m.group(0);
        } else {
        	mTitle = "";
        }

        p = Pattern.compile("(?<=<!-- 내용 -->)(.|\\n)*?(?=<!-- 투표 -->)", Pattern.CASE_INSENSITIVE); 
        m = p.matcher(result);
        
        if (m.find()) { // Find each match in turn; String can't do this.     
        	mContent = m.group(0);
        } else {
        	mContent = "";
        }
        
        mContent = mContent.replaceAll("\n", "");
        mContent = mContent.replaceAll("\r", "");
        mContent = mContent.replaceAll("<br>", "\n");
        mContent = mContent.replaceAll("&nbsp;", " ");
        mContent = mContent.replaceAll("(<style)(.|\\n)*?(</style>)", "");
        mContent = mContent.replaceAll("(<)(.|\\n)*?(>)", "");
        mContent = mContent.trim();
        
        // /board-read.do?boardId=mvHorizonLivingStory&boardNo=100000000013982&command=READ&page=1&categoryId=-1 
        p = Pattern.compile("(?<=boardNo=)(.|\\n)*?(?=&)", Pattern.CASE_INSENSITIVE); 
        m = p.matcher(mLink);
        
        if (m.find()) { // Find each match in turn; String can't do this.     
        	mBoardNo = m.group(0);
        } else {
        	mBoardNo = "";
        }
        
        return true;
    }
    
    public void SaveData() {
    	
    	EditText textTitle = (EditText)findViewById(R.id.editTitle);
    	EditText textContent = (EditText)findViewById(R.id.editContent);

    	mTitle = textTitle.getText().toString();    	
    	mContent = textContent.getText().toString();
    	
    	if (mTitle.length() <= 0 || mContent.length() <= 0) {
    		AlertDialog.Builder ab = null;
			ab = new AlertDialog.Builder( this );
			ab.setMessage( "입력된 내용이 없습니다. 종료하시겠습니까?");
			ab.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					finish();
				}
			});
			ab.setPositiveButton(android.R.string.cancel, null);
			ab.setTitle( "확인" );
			ab.show();
			return;
    	}
    	
    	nMode = 1;
        pd = ProgressDialog.show(this, "", "저장중", true, false);

        Thread thread = new Thread(this);
        thread.start();

        return;
    }

    public void run() {
    	if (nMode == 0) {
    		getData(httpClient, httpContext);
    	} else {
    		PostData(httpClient, httpContext);
    	}
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
    		if (nMode == 0) {
    			EditText tTitle = (EditText)findViewById(R.id.editTitle); 
    			tTitle.setText(mTitle);
    			EditText tContent = (EditText)findViewById(R.id.editContent); 
    			tContent.setText(mContent);
    		} else {
	    		if (!bSaveStatus) {
		    		AlertDialog.Builder ab = null;
					ab = new AlertDialog.Builder( ArticleModify.this );
					String strErrorMsg = "글 저장중 오류가 발생했습니다. \n" + mErrorMsg; 
					ab.setMessage(strErrorMsg);
					ab.setPositiveButton(android.R.string.ok, null);
					ab.setTitle( "확인" );
					ab.show();
	    		}
    		}
    	}
    };        
    	
    protected boolean PostData(HttpClient httpClient, HttpContext httpContext) {
		HttpRequest httpRequest = new HttpRequest();
		
		String url = "http://121.134.211.159/board-save.do";
		String referer = "http://121.134.211.159/board-edit.do";
		
        mContent = mContent.replaceFirst("\n", "<div>");
        mContent = mContent.replaceAll("\n", "</div><div>");
        mContent = mContent + "</div>";

        /* 
		 boardId=mvHorizonLivingStory&
		 page=1&
		 categoryId=-1&
		 boardNo=137480518110766&
		 command=MODIFY&
		 htmlImage=%2Fout&
		 file_cnt=5&
		 tag_yn=Y&
		 thumbnailSize=50&
		 boardWidth=710&
		 defaultBoardSkin=default&
		 boardBackGround_color=&
		 boardBackGround_picture=&
		 boardSerialBadNick=&
		 boardSerialBadContent=&
		 totalSize=20&
		 serialBadNick=&
		 serialBadContent=&
		 fileTotalSize=0&
		 simpleFileTotalSize=0+Bytes&
		 serialFileName=&
		 serialFileMask=&
		 serialFileSize=&
		 userPoint=6680&
		 userEmail=panicstyle%40gmail.com&
		 userHomepage=&
		 boardPollFrom_time=&
		 boardPollTo_time=&
		 boardContent=dddddddddd&
		 boardTitle=cccccccccc&
		 boardSecret_fg=N&
		 boardEdit_fg=M&
		 userNick=&
		 userPw=&
		 fileName=&
		 fileMask=&
		 fileSize=&
		 pollContent=&
		 boardPoint=0&
		 boardTop_fg=&
		 totalsize=0&
		 tag=0&
		 tagsName=0 
		 
		 boardId=%@
		 page=1
		 categoryId=-1
		 boardNo=
		 command=WRITE
		 htmlImage=%%2Fout
		 file_cnt=5
		 tag_yn=Y
		 thumbnailSize=50
		 boardWidth=710
		 defaultBoardSkin=default
		 boardBackGround_color=
		 boardBackGround_picture=
		 boardSerialBadNick=
		 boardSerialBadContent=
		 totalSize=20
		 serialBadNick=
		 serialBadContent=
		 fileTotalSize=0
		 simpleFileTotalSize=0+Bytes
		 serialFileName=
		 serialFileMask=
		 serialFileSize=
		 userPoint=2530
		 userEmail=panicstyle%%40gmail.com
		 userHomepage=
		 boardPollFrom_time=
		 boardPollTo_time=
		 boardContent=%@
		 boardTitle=%@
		 boardSecret_fg=N
		 boardEdit_fg=M
		 userNick=
		 userPw=
		 fileName=
		 fileMask=
		 fileSize=
		 pollContent=
		 boardPoint=0
		 boardTop_fg=
		 totalsize=0
		 tag=0
		 tagsName="
		 
		 boardID, newContent, subjectField.text];
		 */
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("boardId", mBoardID));
		nameValuePairs.add(new BasicNameValuePair("page", "1"));
		nameValuePairs.add(new BasicNameValuePair("categoryId", "-1"));
		nameValuePairs.add(new BasicNameValuePair("boardNo", mBoardNo));
		nameValuePairs.add(new BasicNameValuePair("command", "MODIFY"));
		nameValuePairs.add(new BasicNameValuePair("htmlImage", "%%2Fout"));
		nameValuePairs.add(new BasicNameValuePair("file_cnt", "5"));
		nameValuePairs.add(new BasicNameValuePair("tag_yn", "Y"));
		nameValuePairs.add(new BasicNameValuePair("thumbnailSize", "50"));
		nameValuePairs.add(new BasicNameValuePair("boardWidth", "710"));
		nameValuePairs.add(new BasicNameValuePair("defaultBoardSkin", ""));
		nameValuePairs.add(new BasicNameValuePair("boardBackGround_color", ""));
		nameValuePairs.add(new BasicNameValuePair("boardBackGround_picture", ""));
		nameValuePairs.add(new BasicNameValuePair("boardSerialBadNick", ""));
		nameValuePairs.add(new BasicNameValuePair("boardSerialBadContent", ""));
		nameValuePairs.add(new BasicNameValuePair("totalSize", ""));
		nameValuePairs.add(new BasicNameValuePair("serialBadNick", ""));
		nameValuePairs.add(new BasicNameValuePair("serialBadContent", ""));
		nameValuePairs.add(new BasicNameValuePair("fileTotalSize", ""));
		nameValuePairs.add(new BasicNameValuePair("simpleFileTotalSize", "0+Bytes"));
		nameValuePairs.add(new BasicNameValuePair("serialFileName", ""));
		nameValuePairs.add(new BasicNameValuePair("serialFileMask", ""));
		nameValuePairs.add(new BasicNameValuePair("serialFileSize", ""));
		nameValuePairs.add(new BasicNameValuePair("userPoint", "2530"));
		nameValuePairs.add(new BasicNameValuePair("userEmail", ""));
		nameValuePairs.add(new BasicNameValuePair("userHomepage", ""));
		nameValuePairs.add(new BasicNameValuePair("boardPollFrom_time", ""));
		nameValuePairs.add(new BasicNameValuePair("boardPollTo_time", ""));
		nameValuePairs.add(new BasicNameValuePair("boardContent", mContent));
		nameValuePairs.add(new BasicNameValuePair("boardTitle", mTitle));
		nameValuePairs.add(new BasicNameValuePair("boardSecret_fg", "N"));
		nameValuePairs.add(new BasicNameValuePair("boardEdit_fg", "M"));
		nameValuePairs.add(new BasicNameValuePair("userNick", ""));
		nameValuePairs.add(new BasicNameValuePair("userPw", ""));
		nameValuePairs.add(new BasicNameValuePair("fileName", ""));
		nameValuePairs.add(new BasicNameValuePair("fileMask", ""));
		nameValuePairs.add(new BasicNameValuePair("fileSize", ""));
		nameValuePairs.add(new BasicNameValuePair("pollContent", ""));
		nameValuePairs.add(new BasicNameValuePair("boardPoint", "0"));
		nameValuePairs.add(new BasicNameValuePair("boardTop_fg", ""));
		nameValuePairs.add(new BasicNameValuePair("totalsize", "0"));
		nameValuePairs.add(new BasicNameValuePair("tag", "0"));
		nameValuePairs.add(new BasicNameValuePair("tagsName", ""));
		
		String result = httpRequest.requestPost(httpClient, httpContext, url, nameValuePairs, referer, "euc-kr");

        if (result.indexOf("parent.checkLogin()") < 0) {
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
        
		String url2 = "http://121.134.211.159/jsp/Ajax/Login.jsp";
		String referer2 = "http://121.134.211.159/board-save.do";
		
        nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("TASK", "LOGIN_HTML"));
		nameValuePairs.add(new BasicNameValuePair("_", ""));

		result = httpRequest.requestPost(httpClient, httpContext, url2, nameValuePairs, referer2, "euc-kr");
		
        if (result.indexOf("parent.setMainBodyLogin") < 0) {
        	bSaveStatus = false;
			return false;
        }

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
