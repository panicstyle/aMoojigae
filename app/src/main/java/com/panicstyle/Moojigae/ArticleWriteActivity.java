package com.panicstyle.Moojigae;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArticleWriteActivity extends AppCompatActivity implements Runnable {
    private ProgressDialog m_pd;
    private int m_nMode;
    private String m_Title;
    private String m_Content;
    private String m_BoardID;
    private String m_BoardNo;
    private boolean m_bSaveStatus;
    private String m_ErrorMsg;
    private boolean[] m_arrayAttached;
    private Uri[] m_arrayUri;
    private int m_nSelected = 0;
    private int m_nAttached = 0;
    private static final int SELECT_PHOTO = 0;

    private MoojigaeApplication m_app;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_write);

        m_app = (MoojigaeApplication)getApplication();

        m_arrayAttached = new boolean[5];
        m_arrayUri = new Uri[5];
        for (int i = 0; i < 5; i++) m_arrayAttached[i] = false;

        intenter();

        if (m_nMode == 1) {
            setTitle("글수정");
            m_Title = Utils.repalceHtmlSymbol(m_Title);
            m_Content = Utils.repalceHtmlSymbol(m_Content);
            EditText textTitle = (EditText)findViewById(R.id.editTitle);
            textTitle.setText(m_Title);
            EditText textContent = (EditText) findViewById(R.id.editContent);
            textContent.setText(m_Content);
        } else {
            setTitle("글쓰기");
        }

    }

    public void intenter() {
//    	Intent intent = getIntent();  // 값을 가져오는 인텐트 객체생성
    	Bundle extras = getIntent().getExtras();
    	// 가져온 값을 set해주는 부분
        m_nMode = extras.getInt("MODE");
        m_BoardID = extras.getString("BOARDID");
        m_BoardNo = extras.getString("BOARDNO");
        m_Title = extras.getString("TITLE");
        m_Content = extras.getString("CONTENT");
    }
	
    public void SaveData() {
    	EditText textTitle = (EditText)findViewById(R.id.editTitle);
    	EditText textContent = (EditText)findViewById(R.id.editContent);

    	m_Title = textTitle.getText().toString();
    	m_Content = textContent.getText().toString();
    	
    	if (m_Title.length() <= 0 || m_Content.length() <= 0) {
            AlertDialog.Builder notice = null;
            notice = new AlertDialog.Builder( ArticleWriteActivity.this );
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

        m_pd = ProgressDialog.show(this, "", "저장중", true, false);

        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        PostData();
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
				ab = new AlertDialog.Builder( ArticleWriteActivity.this );
				String strErrorMsg = "글 저장중 오류가 발생했습니다. \n" + m_ErrorMsg;
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

        if (m_nAttached > 0) {
            return PostDataWithAttach();
        } else {
            return PostDataSaveDo(null, null, null);
        }
    }

    protected boolean PostDataWithAttach() {
        m_bSaveStatus = false;
        String url = GlobalConst.m_strServer + "/uploadManager";
        String referer = GlobalConst.m_strServer + "/board-edit.do";

        String boundary = "-------------" + System.currentTimeMillis();
        ContentType contentType = ContentType.create(HTTP.PLAIN_TEXT_TYPE, m_app.m_strEncodingOption);
//        ByteArrayBody bab = new ByteArrayBody(imageBytes, "pic.png");
//        StringBody sbOwner = new StringBody(StaticData.loggedUserId, ContentType.TEXT_PLAIN);
        StringBody sbUserEmail = new StringBody("", contentType);
        StringBody sbUserHomepage = new StringBody("", contentType);
        StringBody sbBoardTitle = new StringBody(m_Title, contentType);
        StringBody sbWhatMode = new StringBody("on", contentType);
        StringBody sbEditContent = new StringBody("", contentType);
        StringBody sbTagsName = new StringBody("", contentType);
        StringBody sbSubId = new StringBody("sub01", contentType);
        StringBody sbMode = new StringBody("attach", contentType);
        StringBody sbSample = new StringBody("", contentType);

        HttpEntity entity;
        String strFileNameArray[] = new String[m_nAttached];
        String strFileMaskArray[] = new String[m_nAttached];
        String strFileSizeArray[] = new String[m_nAttached];
        try {
            Charset chars = Charset.forName(m_app.m_strEncodingOption);
            MultipartEntityBuilder builder;
            builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.setCharset(chars);
            builder.setBoundary(boundary);
            builder.addPart("userEmail", sbUserEmail);
            builder.addPart("userHomepage", sbUserHomepage);
            builder.addPart("boardTitle", sbBoardTitle);
            builder.addPart("whatmode_uEdit", sbWhatMode);
            builder.addPart("editContent", sbEditContent);
            builder.addPart("tagsName", sbTagsName);
            for (int i = 0; i < 5; i++) {
                if (m_arrayAttached[i]) {
                    InputStream imageStream = getContentResolver().openInputStream(m_arrayUri[i]);
                    String fileName = getPathFromMediaUri(this, m_arrayUri[i]);
                    if (fileName == null) {
                        m_ErrorMsg = "이미지 파일을 읽을 수 없습니다." + m_arrayUri[i];
                        return false;
                    }
                    InputStreamBody inputStreamBody = new InputStreamBody(imageStream, fileName);

                    String strFileName = "file" + (i + 1);
                    builder.addPart(fileName, inputStreamBody);
                }
            }
            builder.addPart("subId", sbSubId);
            builder.addPart("mode", sbMode);

            entity = builder.build();
            String result = m_app.m_httpRequest.requestPostWithAttach(url, entity, referer, m_app.m_strEncodingOption, boundary);
            if (!result.contains("fileNameArray[0] =")) {
                m_ErrorMsg = Utils.getMatcherFirstString("(?<=var message = ')(.|\\n)*?(?=';)", result);
                return false;
            }
            m_ErrorMsg = "첨부파일 정보를 찾을 수 없습니다.";
            Matcher m = Utils.getMatcher("(?<=fileNameArray\\[.\\] = ')(.|\\n)*?(?=';)", result);
            for (int i = 0; i < m_nAttached; i++) {
                if (m.find()) {
                    strFileNameArray[i] = m.group(0);
                } else {
                    return false;
                }
            }
            m = Utils.getMatcher("(?<=fileMaskArray\\[.\\] = ')(.|\\n)*?(?=';)", result);
            for (int i = 0; i < m_nAttached; i++) {
                if (m.find()) {
                    strFileMaskArray[i] = m.group(0);
                } else {
                    return false;
                }
            }
            m = Utils.getMatcher("(?<=fileSizeArray\\[.\\] = )(.|\\n)*?(?=;)", result);
            for (int i = 0; i < m_nAttached; i++) {
                if (m.find()) {
                    strFileSizeArray[i] = m.group(0);
                } else {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return PostDataSaveDo(strFileNameArray, strFileMaskArray, strFileSizeArray);
    }

    protected boolean PostDataSaveDo(String[] strFileNameArray, String[] strFileMaskArray, String[] strFileSizeArray) {
        String url = GlobalConst.m_strServer + "/board-save.do";
        String referer = GlobalConst.m_strServer + "/board-edit.do";

        m_Content = m_Content.replaceFirst("\n", "<div>");
        m_Content = m_Content.replaceAll("\n", "</div><div>");
        m_Content = m_Content + "</div>";

        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("boardId", m_BoardID));
        nameValuePairs.add(new BasicNameValuePair("page", "1"));
        nameValuePairs.add(new BasicNameValuePair("categoryId", "-1"));
        nameValuePairs.add(new BasicNameValuePair("boardNo", m_BoardNo));
        if (m_nMode == 0) {
            if (m_BoardNo.length() > 0) {
                nameValuePairs.add(new BasicNameValuePair("command", "REPLY"));
            } else {
                nameValuePairs.add(new BasicNameValuePair("command", "WRITE"));
            }
        } else {
            nameValuePairs.add(new BasicNameValuePair("command", "MODIFY"));
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
        nameValuePairs.add(new BasicNameValuePair("boardContent", m_Content));
        nameValuePairs.add(new BasicNameValuePair("boardTitle", m_Title));
        nameValuePairs.add(new BasicNameValuePair("boardSecret_fg", "N"));
        nameValuePairs.add(new BasicNameValuePair("boardEdit_fg", "M"));
        nameValuePairs.add(new BasicNameValuePair("userNick", ""));
        nameValuePairs.add(new BasicNameValuePair("userPw", ""));
        if (strFileNameArray == null) {
            nameValuePairs.add(new BasicNameValuePair("fileName", ""));
            nameValuePairs.add(new BasicNameValuePair("fileMask", ""));
            nameValuePairs.add(new BasicNameValuePair("fileSize", ""));
        } else {
            String s = "";
            for (int i = 0; i < m_nAttached; i++) {
                if (i > 0) {
                    s = s + "|";
                }
                s = s + strFileNameArray[i];
            }
            nameValuePairs.add(new BasicNameValuePair("fileName", s));
            s = "";
            for (int i = 0; i < m_nAttached; i++) {
                if (i > 0) {
                    s = s + "|";
                }
                s = s + strFileMaskArray[i];
            }
            nameValuePairs.add(new BasicNameValuePair("fileMask", s));
            s = "";
            for (int i = 0; i < m_nAttached; i++) {
                if (i > 0) {
                    s = s + "|";
                }
                s = s + strFileSizeArray[i];
            }
            nameValuePairs.add(new BasicNameValuePair("fileSize", s));
        }
        nameValuePairs.add(new BasicNameValuePair("pollContent", ""));
        nameValuePairs.add(new BasicNameValuePair("boardPoint", "0"));
        nameValuePairs.add(new BasicNameValuePair("boardTop_fg", ""));
        nameValuePairs.add(new BasicNameValuePair("totalsize", "0"));
        nameValuePairs.add(new BasicNameValuePair("tag", "0"));
        nameValuePairs.add(new BasicNameValuePair("tagsName", ""));
        nameValuePairs.add(new BasicNameValuePair("Uid", m_app.m_strUserID));

        String result = m_app.m_httpRequest.requestPost(url, nameValuePairs, referer, m_app.m_strEncodingOption);

        if (!result.contains("parent.checkLogin()")) {
            m_ErrorMsg = Utils.getMatcherFirstString("(?<=<b>시스템 메세지입니다</b></font><br>)(.|\\n)*?(?=<br>)", result);
            m_bSaveStatus = false;
            return false;
        }
/*
        url = GlobalConst.m_strServer + "/jsp/Ajax/Login.jsp";
        referer = GlobalConst.m_strServer + "/board-save.do";

        nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("TASK", "LOGIN_HTML"));
        nameValuePairs.add(new BasicNameValuePair("_", ""));

        result = m_httpRequest.requestPost(url, nameValuePairs, referer, "euc-kr");

        if (!result.contains("parent.setMainBodyLogin")) {
            m_bSaveStatus = false;
            return false;
        }
*/
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

    public void clickImage(View v) {
        switch (v.getId()) {
            case R.id.attach0:
                m_nSelected = 0;
                break;
            case R.id.attach1:
                m_nSelected = 1;
                break;
            case R.id.attach2:
                m_nSelected = 2;
                break;
            case R.id.attach3:
                m_nSelected = 3;
                break;
            case R.id.attach4:
                m_nSelected = 4;
                break;
            default:
                m_nSelected = -1;
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
        switch (m_nSelected) {
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
        m_arrayAttached[m_nSelected]= false;
        m_nAttached--;
    }

    public void clickAddImage(View v) {
		m_nSelected = -1;
		if (m_nAttached < 5) {
			for (int i = 0; i < 5; i++) {
				if (!m_arrayAttached[i]) {
					m_nSelected = i;
					break;
				}
			}
		}
		if (m_nSelected >= 0 && m_nSelected < 5) {
            CharSequence select_photo[] = new CharSequence[] {"앨범에서 선택하기"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("사진 선택");
            builder.setItems(select_photo, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // the user clicked on colors[which]
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                }
            });
            builder.show();
		}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        Uri selectedImage = imageReturnedIntent.getData();
                        InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                        Bitmap yourSelectedImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(imageStream), 48, 48);
//                        Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
                        Drawable d = new BitmapDrawable(getResources(), yourSelectedImage);

                        ImageView imageView;
                        switch (m_nSelected) {
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
                        m_arrayAttached[m_nSelected] = true;
                        m_arrayUri[m_nSelected] = selectedImage;
                        m_nAttached++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            default:
                return;
        }
    }

    public String getPathFromMediaUri(Context context, Uri uri) {
        String result = null;
        String fileName = null;

        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int col = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            if (col >= 0 && cursor.moveToFirst())
                result = cursor.getString(col);
            cursor.close();

            if (result != null) {
                int index = result.lastIndexOf("/");
                fileName = result.substring(index + 1);
            } else {
                fileName = "default.JPG";
            }
        }
        return fileName;

    }
}
