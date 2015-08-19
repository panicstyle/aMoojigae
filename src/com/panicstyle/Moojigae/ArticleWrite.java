package com.panicstyle.Moojigae;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class ArticleWrite extends Activity implements Runnable {
	protected HttpClient httpClient;
	protected HttpContext httpContext;
    private ProgressDialog pd;
    private String mTitle;
    private String mContent;
    private String mBoardID;
    private String mBoardNo;
    private boolean bSaveStatus;
    private String mErrorMsg;
	private boolean[] arrayAttached;
	private int nSelected = 0;
	private int nAttached = 0;
	private static final int SELECT_PHOTO = 0;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main4);

        MoojigaeApplication app = (MoojigaeApplication)getApplication();
        httpClient = app.httpClient;
        httpContext = app.httpContext;

		arrayAttached = new boolean[5];
		for (int i = 0; i < 5; i++) arrayAttached[i] = false;

        intenter();
		setTitle("글쓰기");

//        findViewById(R.id.okbtn).setOnClickListener(mClickListener);
//        findViewById(R.id.cancelbtn).setOnClickListener(mClickListener);
    }

    public void intenter() {
//    	Intent intent = getIntent();  // 값을 가져오는 인텐트 객체생성
    	Bundle extras = getIntent().getExtras();
    	// 가져온 값을 set해주는 부분
    	
    	mBoardID = extras.getString("BOARDID").toString();
    	mBoardNo = extras.getString("BOARDNO").toString();
    }
	
    public void SaveData() {
    	EditText textTitle = (EditText)findViewById(R.id.editTitle);
    	EditText textContent = (EditText)findViewById(R.id.editContent);

    	mTitle = textTitle.getText().toString();    	
    	mContent = textContent.getText().toString();
    	
    	if (mTitle.length() <= 0 || mContent.length() <= 0) {
			AlertDialog.Builder notice = null;
			notice = new AlertDialog.Builder( ArticleWrite.this );
			notice.setTitle("알림");
			notice.setMessage("입력된 내용이 없습니다.");
			notice.setPositiveButton(android.R.string.ok, null);
			notice.show();
			return;
    	}

		if (getParent() == null) {
			setResult(Activity.RESULT_OK, new Intent());
		} else {
			getParent().setResult(Activity.RESULT_OK, new Intent());
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
				ab = new AlertDialog.Builder( ArticleWrite.this );
				String strErrorMsg = "글 저장중 오류가 발생했습니다. \n" + mErrorMsg; 
				ab.setMessage(strErrorMsg);
				ab.setPositiveButton(android.R.string.ok, null);
				ab.setTitle( "확인" );
				ab.show();
				return;
    		}

			finish();
    	}
    };        
    	
    protected boolean PostData(HttpClient httpClient, HttpContext httpContext) {

		if (nAttached > 0) {
			PostDataWithAttach(httpClient, httpContext);
		}
		return PostDataSaveDo(httpClient, httpContext);
	}

	protected boolean PostDataWithAttach(HttpClient httpClient, HttpContext httpContext) {
		HttpRequest httpRequest = new HttpRequest();

		String url = "http://121.134.211.159/uploadManager";
		String referer = "http://121.134.211.159/board-edit.do";

		MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
		entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		Charset chars = Charset.forName("euc-kr");
		entityBuilder.setCharset(chars);

		entityBuilder.addTextBody("userEmail", "");
		entityBuilder.addTextBody("userHomepage", "");
		entityBuilder.addTextBody("boardTitle", "");
		entityBuilder.addTextBody("whatmode_uEdit", "on");
		entityBuilder.addTextBody("editContent", "");
		entityBuilder.addTextBody("tagsName", "");

		// ImageView 저장된 파일 저장

		HttpEntity entity = entityBuilder.build();

		String result = httpRequest.requestPostWithAttach(httpClient, httpContext, url, entity, referer, "euc-kr");

		return true;
	}

	protected boolean PostDataSaveDo(HttpClient httpClient, HttpContext httpContext) {
		HttpRequest httpRequest = new HttpRequest();

		String url = "http://121.134.211.159/board-save.do";
		String referer = "http://121.134.211.159/board-edit.do";

        mContent = mContent.replaceFirst("\n", "<div>");
        mContent = mContent.replaceAll("\n", "</div><div>");
        mContent = mContent + "</div>";

		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("boardId", mBoardID));
		nameValuePairs.add(new BasicNameValuePair("page", "1"));
		nameValuePairs.add(new BasicNameValuePair("categoryId", "-1"));
		nameValuePairs.add(new BasicNameValuePair("boardNo", mBoardNo));
		if (mBoardNo.length() > 0) {
			nameValuePairs.add(new BasicNameValuePair("command", "REPLY"));
		} else {
			nameValuePairs.add(new BasicNameValuePair("command", "WRITE"));
		}
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
		bSaveStatus = true;
    	return true;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_write, menu);

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

	public void clickImage(View v) {
		switch (v.getId()) {
			case R.id.attach0:
				nSelected = 0;
				break;
			case R.id.attach1:
				nSelected = 1;
				break;
			case R.id.attach2:
				nSelected = 2;
				break;
			case R.id.attach3:
				nSelected = 3;
				break;
			case R.id.attach4:
				nSelected = 4;
				break;
			default:
				nSelected = -1;
				break;
		}
		AlertDialog.Builder ab = null;
		ab = new AlertDialog.Builder( this );
		ab.setMessage( "사진을 삭제하시겠습니까?");
		ab.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				removeImage();
			}
		});
		ab.setNegativeButton(android.R.string.cancel, null);
		ab.setTitle( "확인" );
		ab.show();
	}

	public void removeImage() {
		ImageView imageView;
		switch (nSelected) {
			case 0:
				imageView = (ImageView) findViewById(R.id.attach0);
				break;
			case 1:
				imageView = (ImageView) findViewById(R.id.attach1);
				break;
			case 2:
				imageView = (ImageView) findViewById(R.id.attach2);
				break;
			case 3:
				imageView = (ImageView) findViewById(R.id.attach3);
				break;
			case 4:
				imageView = (ImageView) findViewById(R.id.attach4);
				break;
			default:
				return;
		}
		imageView.setImageDrawable(null);
		arrayAttached[nSelected]= false;
		nAttached--;
	}

	public void clickAddImage(View v) {
// 다음 버전에 추가할 것.
/*
		nSelected = -1;
		if (nAttached < 5) {
			for (int i = 0; i < 5; i++) {
				if (!arrayAttached[i]) {
					nSelected = i;
					break;
				}
			}
		}
		if (nSelected >= 0 && nSelected < 5) {
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");
			startActivityForResult(photoPickerIntent, SELECT_PHOTO);
		}
*/
		return;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		switch(requestCode) {
			case SELECT_PHOTO:
				if (resultCode == RESULT_OK) {
					try {
						Uri selectedImage = imageReturnedIntent.getData();
						InputStream imageStream = getContentResolver().openInputStream(selectedImage);
						Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
						Drawable d = new BitmapDrawable(getResources(), yourSelectedImage);

						ImageView imageView;
						switch (nSelected) {
							case 0:
								imageView = (ImageView) findViewById(R.id.attach0);
								break;
							case 1:
								imageView = (ImageView) findViewById(R.id.attach1);
								break;
							case 2:
								imageView = (ImageView) findViewById(R.id.attach2);
								break;
							case 3:
								imageView = (ImageView) findViewById(R.id.attach3);
								break;
							case 4:
								imageView = (ImageView) findViewById(R.id.attach4);
								break;
							default:
								return;
						}
						imageView.setImageDrawable(d);
						arrayAttached[nSelected] = true;
						nAttached++;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			default:
				return;
		}
	}

	public void CancelData() {
		if (getParent() == null) {
			setResult(Activity.RESULT_CANCELED, new Intent());
		} else {
			getParent().setResult(Activity.RESULT_CANCELED, new Intent());
		}

		finish();
    }
}
