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
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentWriteActivity extends AppCompatActivity implements Runnable {
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

    private MoojigaeApplication m_app;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_write);

        m_app = (MoojigaeApplication)getApplication();

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

    private static class MyHandler extends Handler {
        private final WeakReference<CommentWriteActivity> mActivity;
        public MyHandler(CommentWriteActivity activity) {
            mActivity = new WeakReference<CommentWriteActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            CommentWriteActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    private final MyHandler mHandler = new MyHandler(this);

    public void run() {
        PostData();
        mHandler.sendEmptyMessage(0);
    }

    private void handleMessage(Message msg) {
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

    protected boolean PostData() {
        String url = GlobalConst.m_strServer + "/memo-save.do";
        String referer;

        if (m_CommentNo.length() <= 0) {
            referer = GlobalConst.m_strServer + "/board-read.do";
        } else {
            referer = GlobalConst.m_strServer + "/board-read.do?boardId=" + m_BoardID + "&boardNo=" + m_BoardNo + "&command=READ&page=1&categoryId=-1";
        }

        m_Content = m_Content.replaceFirst("\n", "<div>");
        m_Content = m_Content.replaceAll("\n", "</div><div>");
        m_Content = m_Content + "</div>";
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("boardId", m_BoardID));
        nameValuePairs.add(new BasicNameValuePair("page", "1"));
        nameValuePairs.add(new BasicNameValuePair("categoryId", "-1"));
        nameValuePairs.add(new BasicNameValuePair("time", ""));
        nameValuePairs.add(new BasicNameValuePair("returnBoardNo", m_BoardNo));
        nameValuePairs.add(new BasicNameValuePair("boardNo", m_BoardNo));
        if (m_CommentNo.length() <= 0) {
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
        nameValuePairs.add(new BasicNameValuePair("memoContent", m_Content));
        nameValuePairs.add(new BasicNameValuePair("memoSeq", m_CommentNo));
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

        String result = m_app.m_httpRequest.requestPost(url, nameValuePairs, referer);

        if (result.contains("<b>시스템 메세지입니다</b>")) {
            m_ErrorMsg = Utils.getMatcherFirstString("(?<=<b>시스템 메세지입니다</b></font><br>)(.|\\n)*?(?=<br>)", result);
            m_bSaveStatus = false;
            return false;
        }

        m_bSaveStatus = true;
        finish();
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
