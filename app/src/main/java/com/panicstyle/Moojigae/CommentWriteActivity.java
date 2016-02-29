package com.panicstyle.Moojigae;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

public class CommentWriteActivity extends AppCompatActivity implements Runnable {
    private HttpRequest m_httpRequest;
    private ProgressDialog m_pd;
    private int m_nMode;
    private int m_nPNotice;
    private String m_CommID;
    private String m_BoardID;
    private String m_BoardNo;
    private String m_CommentNo;
    private boolean m_bSaveStatus;
    private String m_ErrorMsg;
    private String m_Content;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_write);

        MoojigaeApplication app = (MoojigaeApplication)getApplication();
        m_httpRequest = app.m_httpRequest;

        intenter();

        if (m_nMode == 1) {
            setTitle("댓글수정");
            m_Content = Utils.repalceHtmlSymbol(m_Content);
            EditText tContent = (EditText) findViewById(R.id.editContent);
            tContent.setText(m_Content);
        } else {
            setTitle("댓글쓰기");
        }
    }

    public void intenter() {
//    	Intent intent = getIntent();  // 값을 가져오는 인텐트 객체생성
    	Bundle extras = getIntent().getExtras();
    	// 가져온 값을 set해주는 부분
        m_nMode = extras.getInt("MODE");
        m_nPNotice = extras.getInt("ISPNOTICE");
    	m_CommID = extras.getString("COMMID");
    	m_BoardID = extras.getString("BOARDID");
    	m_BoardNo = extras.getString("BOARDNO");
    	m_CommentNo = extras.getString("COMMENTNO");
        m_Content = extras.getString("COMMENT");

    }
	
    public void SaveData() {
    	
    	EditText textContent = (EditText)findViewById(R.id.editContent);

    	m_Content = textContent.getText().toString();
    	
    	if (m_Content.length() <= 0) {
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

        if (getParent() == null) {
            setResult(Activity.RESULT_OK, new Intent());
        } else {
            getParent().setResult(Activity.RESULT_OK, new Intent());
        }

        m_pd = ProgressDialog.show(this, "", "저장중", true, false);

        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        if (m_nMode == 0) {
            if (m_nPNotice == 0) {
                PostData();
            } else {
                PostDataPNotice();
            }
        } else {
            if (m_nPNotice == 0) {
                PostModifyData();
            } else {
                PostModifyDataPNotice();
            }
        }
    	handler.sendEmptyMessage(0);
    }

    private Handler handler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
            if(m_pd != null){
                if(m_pd.isShowing()){
                    m_pd.dismiss();
                }
            }
    		if (!m_bSaveStatus) {
	    		AlertDialog.Builder ab = null;
				ab = new AlertDialog.Builder( CommentWriteActivity.this );
				String strErrorMsg = "댓글 저장중 오류가 발생했습니다. \n" + m_ErrorMsg;
				ab.setMessage(strErrorMsg);
				ab.setPositiveButton(android.R.string.ok, null);
				ab.setTitle( "확인" );
				ab.show();
				return;
    		}
    		finish();
    	}
    };        
    	
    protected boolean PostData() {
		String url = "http://cafe.gongdong.or.kr/cafe.php?mode=up_add&sub_sort=&p2=&p1=" + m_CommID + "&sort=" + m_BoardID;
        String strParam = "number=" + m_BoardNo + "&content=" + m_Content;
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("number", m_BoardNo));
        if (m_CommentNo.length() > 0) {
            nameValuePairs.add(new BasicNameValuePair("number_re", m_CommentNo));
        }
        nameValuePairs.add(new BasicNameValuePair("content", m_Content));
		String result = m_httpRequest.requestPost(url, nameValuePairs, url, "utf-8");

        if (result.contains("<meta http-equiv=\"refresh\" content=\"0;url=/cafe.php?sort=")) {
            m_ErrorMsg = Utils.getMatcherFirstString("(?<=window.alert\\(\\\")(.|\\n)*?(?=\\\")", result);
			m_bSaveStatus = false;
			return false;
        }
        
		m_bSaveStatus = true;
    	return true;
    }

    protected boolean PostModifyData() {
        String url = "http://cafe.gongdong.or.kr/cafe.php?mode=edit_reply&p2=&p1=" + m_CommID + "&sort=" + m_BoardID;
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("number", m_CommentNo));
        nameValuePairs.add(new BasicNameValuePair("content", m_Content));
        String result = m_httpRequest.requestPost(url, nameValuePairs, url, "utf-8");

        if (result.indexOf("<meta http-equiv=\"refresh\" content=\"0;url=/cafe.php?sort=") < 0) {
            m_ErrorMsg = Utils.getMatcherFirstString("(?<=window.alert\\(\\\")(.|\\n)*?(?=\\\")", result);
            m_bSaveStatus = false;
            return false;
        }

        m_bSaveStatus = true;
        return true;
    }

    protected boolean PostDataPNotice() {
        String url = "http://www.gongdong.or.kr/index.php";
        String strPostParam = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                "<methodCall>\n" +
                "<params>\n" +
                "<_filter><![CDATA[insert_comment]]></_filter>\n" +
                "<error_return_url><![CDATA[/notice/" + m_BoardNo + "]]></error_return_url>\n" +
                "<mid><![CDATA[notice]]></mid>\n" +
                "<document_srl><![CDATA[" + m_BoardNo + "]]></document_srl>\n" +
                "<comment_srl><![CDATA[0]]></comment_srl>\n" +
                "<content><![CDATA[<p>" + m_Content + "</p>\n" +
                "]]></content>\n" +
                "<module><![CDATA[board]]></module>\n" +
                "<act><![CDATA[procBoardInsertComment]]></act>\n" +
                "</params>\n" +
                "</methodCall>";

        String strReferer = "http://www.gongdong.or.kr/notice/" + m_BoardNo;
        String result = m_httpRequest.requestPost(url, strPostParam, strReferer, "utf-8");

        if (result.contains("<error>0</error>")) {
            m_ErrorMsg = Utils.getMatcherFirstString("(?<=<message>)(.|\\n)*?(?=</message>)", result);
            m_bSaveStatus = false;
            return false;
        }

        m_bSaveStatus = true;
        return true;
    }

    protected boolean PostModifyDataPNotice() {
        String url = "http://www.gongdong.or.kr/index.php";
        String strPostParam = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                "<methodCall>\n" +
                "<params>\n" +
                "<_filter><![CDATA[insert_comment]]></_filter>\n" +
                "<error_return_url><![CDATA[/index.php?mid=notice&document_srl=" + m_BoardNo + "&act=dispBoardModifyComment&comment_srl=" + m_CommentNo +"]]></error_return_url>\n" +
                "<act><![CDATA[procBoardInsertComment]]></act>\n" +
                "<mid><![CDATA[notice]]></mid>\n" +
                "<document_srl><![CDATA[" + m_BoardNo + "]]></document_srl>\n" +
                "<comment_srl><![CDATA[" + m_CommentNo + "]]></comment_srl>\n" +
                "<content><![CDATA[<p>" + m_Content + "</p>\n" +
                "]]></content>\n" +
                "<parent_srl><![CDATA[0]]></parent_srl>\n" +
                "<module><![CDATA[board]]></module>\n" +
                "</params>\n" +
                "</methodCall>";
        String strReferer = "http://www.gongdong.or.kr/index.php?mid=notice&document_srl=" + m_BoardNo + "&act=dispBoardModifyComment&comment_srl=" + m_CommentNo;
        String result = m_httpRequest.requestPost(url, strPostParam, strReferer, "utf-8");

        if (result.contains("<error>0</error>")) {
            m_ErrorMsg = Utils.getMatcherFirstString("(?<=<message>)(.|\\n)*?(?=</message>)", result);
            m_bSaveStatus = false;
            return false;
        }

        m_bSaveStatus = true;
        return true;
    }

    public void CancelData() {
        if (getParent() == null) {
            setResult(Activity.RESULT_CANCELED, new Intent());
        } else {
            getParent().setResult(Activity.RESULT_CANCELED, new Intent());
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_article_write, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_cancel:
                CancelData();
                return true;
            case R.id.menu_save:
                SaveData();
                return true;
        }
        return true;
    }
}
