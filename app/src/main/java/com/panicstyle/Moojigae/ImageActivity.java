package com.panicstyle.Moojigae;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaderFactory;
import com.bumptech.glide.load.model.LazyHeaders;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Random;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

import static org.apache.commons.io.IOUtils.copy;

public class ImageActivity extends AppCompatActivity {
    private static String TAG = "ImageActivity";
    protected String itemsLink;
    protected String m_fileName;
    protected String m_strBoardID;
    protected String m_strBoardNo;
    private MoojigaeApplication m_app;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        setTitle("이미지보기");

        m_app = (MoojigaeApplication)getApplication();

        intenter();

        GlideUrl glideUrl = new GlideUrl(itemsLink, new LazyHeaders.Builder()
                .addHeader("key1", "value")
                .addHeader("Cookie", m_app.m_httpRequest.m_Cookie)
                .addHeader("Referer", GlobalConst.m_strServer + "/board-api-read.do?boardId=" + m_strBoardID + "&boardNo=" + m_strBoardNo + "&command=READ&categoryId=-1")
                .addHeader("Host", GlobalConst.m_strServerName)
                .build());

        PhotoView photoView = (PhotoView) findViewById(R.id.widget_photoview);
        Glide.with(this).load(glideUrl).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(photoView);

        /*
        ImageView imageView = (ImageView) findViewById(R.id.imageView);


        Drawable drawable = LoadImageFromWebOperations(itemsLink);
        imageView.setImageDrawable(drawable);

        PhotoViewAttacher mAttacher = new PhotoViewAttacher(imageView);
        mAttacher.update();
*/
    }

    public void intenter() {
//    	Intent intent = getIntent();  // 값을 가져오는 인텐트 객체생성
        Bundle extras = getIntent().getExtras();
        // 가져온 값을 set해주는 부분
        itemsLink = extras.getString("ITEMS_LINK");
        m_fileName = extras.getString("FILENAME");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save_image:
                SaveImage();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void SaveImage() {
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(itemsLink));
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, m_fileName);
        request.addRequestHeader("Cookie", m_app.m_httpRequest.m_Cookie);
        request.addRequestHeader("Referer", GlobalConst.m_strServer + "/board-api-read.do?boardId=" + m_strBoardID + "&boardNo=" + m_strBoardNo + "&command=READ&categoryId=-1");
        request.addRequestHeader("Host", GlobalConst.m_strServerName);
// You can change the name of the downloads, by changing "download" to everything you want, such as the mWebview title...
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        dm.enqueue(request);

    }
}