package com.panicstyle.Moojigae;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecentItemsActivity extends AppCompatActivity implements Runnable {
    private ListView m_listView;
    private AdView m_adView;
    private ProgressDialog m_pd;
    private String m_strErrorMsg;
    private MoojigaeApplication m_app;

    protected String m_itemsTitle;
    protected String m_itemsLink;
    private List<HashMap<String, Object>> m_arrayItems;
    static final int REQUEST_WRITE = 1;
    static final int REQUEST_VIEW = 2;
    protected int m_LoginStatus;
    public static int m_nMode;
    private EfficientAdapter m_adapter;

    private static class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<HashMap<String, Object>> arrayItems;

        public EfficientAdapter(Context context, List<HashMap<String, Object>> data) {
            mInflater = LayoutInflater.from(context);
            arrayItems = data;
        }

        public int getCount() {
            return arrayItems.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView != null) {
                Object a = convertView.getTag();
                if (!(a instanceof ViewHolder)) {
                    convertView = null;
                }
            }
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_itemsview, null);

                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.subject = (TextView) convertView.findViewById(R.id.subject);
                holder.comment = (TextView) convertView.findViewById(R.id.comment);
                holder.iconnew = (ImageView) convertView.findViewById(R.id.iconnew);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            HashMap<String, Object> item;;
            item = arrayItems.get(position);
            String date = (String) item.get("date");
            String name = (String) item.get("name");
            String subject = (String) item.get("subject");
            String comment = (String) item.get("comment");
            String hit = (String) item.get("hit");
            int isNew = (Integer) item.get("isNew");
            int isReply = (Integer) item.get("isReply");
            // Bind the data efficiently with the holder.
            name = "<b>" + name + "</b>&nbsp;" + date + "&nbsp;(" + hit + "&nbsp;읽음)" ;
            holder.name.setText(Html.fromHtml(name));
            holder.subject.setText(subject);
            holder.comment.setText(comment);
            if (isNew == 1) {
                holder.iconnew.setImageResource(R.drawable.circle);
            } else {
                holder.iconnew.setImageResource(0);
            }
            if (comment.length() > 0) {
                holder.comment.setBackgroundResource(R.drawable.layout_circle);
            } else {
                holder.comment.setBackgroundResource(0);
            }

            return convertView;
        }

        static class ViewHolder {
            TextView name;
            TextView subject;
            TextView comment;
            ImageView iconnew;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);
        m_listView = (ListView) findViewById(R.id.listView);
        m_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, Object> item;
                item = m_arrayItems.get(position);
                Intent intent = new Intent(RecentItemsActivity.this, ArticleViewActivity.class);

                intent.putExtra("MODE", (Integer) m_nMode);
                intent.putExtra("SUBJECT", (String) item.get("subject"));
                intent.putExtra("DATE", (String) item.get("date"));
                intent.putExtra("USERNAME", (String) item.get("name"));
                intent.putExtra("USERID", (String) item.get("id"));
                intent.putExtra("boardNo", (String) item.get("boardNo"));
                intent.putExtra("HIT", (String) item.get("hit"));
                intent.putExtra("BOARDID", (String) item.get("boardId"));
                startActivityForResult(intent, REQUEST_VIEW);
            }
        });

        AdView m_adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        m_adView.loadAd(adRequest);

        m_app = (MoojigaeApplication)getApplication();

        intenter();

        setTitle(m_itemsTitle);

        m_arrayItems = new ArrayList<>();

        LoadingData();
    }

    public void LoadingData() {
        m_pd = ProgressDialog.show(this, "", "로딩중", true,
                false);

        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        if (!getData()) {
            // Login
            Login login = new Login();
            m_LoginStatus = login.LoginTo(RecentItemsActivity.this, m_app.m_httpRequest, m_app.m_strEncodingOption, m_app.m_strUserID, m_app.m_strUserPW);
            m_strErrorMsg = login.m_strErrorMsg;

            if (m_LoginStatus > 0) {
                if (getData()) {
                    m_LoginStatus = 1;
                }
            }
        } else {
            m_LoginStatus = 1;
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
            displayData();
        }
    };

    public void displayData() {
        if (m_LoginStatus == -1) {
            AlertDialog.Builder ab = null;
            ab = new AlertDialog.Builder( RecentItemsActivity.this );
            ab.setMessage( "로그인 정보가 설정되지 않았습니다. 설정 메뉴를 통해 로그인 정보를 설정하십시오.");
            ab.setPositiveButton(android.R.string.ok, null);
            ab.setTitle( "로그인 오류" );
            ab.show();
        } else if (m_LoginStatus == 0){
            AlertDialog.Builder ab = null;
            ab = new AlertDialog.Builder( RecentItemsActivity.this );
            ab.setMessage( "로그인을 실패했습니다 설정 메뉴를 통해 로그인 정보를 변경하십시오.");
            ab.setPositiveButton(android.R.string.ok, null);
            ab.setTitle( "로그인 오류" );
            ab.show();
        } else {
            m_adapter = new EfficientAdapter(RecentItemsActivity.this, m_arrayItems);
            m_listView.setAdapter(m_adapter);
        }
    }

    public void intenter() {
//    	Intent intent = getIntent();  // 값을 가져오는 인텐트 객체생성
        Bundle extras = getIntent().getExtras();
        // 가져온 값을 set해주는 부분

        m_itemsTitle = extras.getString("ITEMS_TITLE");
        m_itemsLink = extras.getString("ITEMS_LINK");
    }

    protected boolean getData() {
        String url = GlobalConst.m_strServer + "/board-api-recent.do?part=index&rid=50&pid=" + m_itemsLink;
        String referer = GlobalConst.m_strServer + "/board-api-list.do";

        String result = m_app.m_httpRequest.requestPost(url, "", referer, m_app.m_strEncodingOption);

        HashMap<String, Object> item;

        try {
            JSONObject boardObject = new JSONObject(result);
            JSONArray arrayItem = boardObject.getJSONArray("item");
            for(int i = 0; i < arrayItem.length(); i++) {
                JSONObject jsonItem = arrayItem.getJSONObject(i);
                item = new HashMap<>();

                // 새글 여부
                String isNew = jsonItem.getString("recentArticle");
                // isNew
                if (isNew.equals("Y")) {
                    item.put("isNew", 1);
                } else {
                    item.put("isNew", 0);
                }

                // 업데이트 여부
                String isUpdated = jsonItem.getString("updatedArticle");
                if (isUpdated.equals("Y")) {
                    item.put("isUpdated", 1);
                } else {
                    item.put("isUpdated", 0);
                }

                // boardId.
                String strBoardId = jsonItem.getString("boardId");
                item.put("boardId", strBoardId);
                // boardNo
                String strBoardNo = jsonItem.getString("boardNo");
                item.put("boardNo", strBoardNo);
                // subject
                String strSubject = jsonItem.getString("boardTitle");
                strSubject = Utils.repalceHtmlSymbol(strSubject);
                item.put("subject", strSubject);
                // writer
                String strName = jsonItem.getString("userNick");
                item.put("name", strName);
                // comment
                String strComment = jsonItem.getString("boardMemo_cnt");
                item.put("comment", strComment);
                // date
                String strDate = jsonItem.getString("boardRegister_dt");
                item.put("date", strDate);
                // 조회수
                item.put("hit", jsonItem.getString("boardRead_cnt"));
                item.put("isReply", 0);

                m_arrayItems.add( item );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch(requestCode) {
            default:
                break;

        }
    }
}