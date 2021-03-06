package com.panicstyle.Moojigae;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BoardActivity extends AppCompatActivity implements Runnable {
    Toolbar toolbar;
    private ListView m_listView;
    private ProgressDialog m_pd;
    private MoojigaeApplication m_app;

	protected String m_boardTitle;
	protected String m_boardCode;
    List<HashMap<String, Object>> m_arrayItems;

    private String m_strRecent;
    private String m_strNew;

    private static class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<HashMap<String, Object>> arrayItems;

        public EfficientAdapter(Context context, List<HashMap<String, Object>> data) {
            mInflater = LayoutInflater.from(context);
            arrayItems = data;
        }

        public int getCount() {
            return arrayItems.size() ;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            HashMap<String, Object> item;
            item = arrayItems.get(position);
            String title = (String)item.get("title");
            String type = (String)item.get("type");
            String link = (String)item.get("link");
            int isNew = (Integer)item.get("isNew");

            if (type.equalsIgnoreCase("group")) {
                convertView = mInflater.inflate(R.layout.list_group_boardview, null);
                GroupHolder holder;
                holder = new GroupHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(holder);
                holder.title.setText(title);
            } else {
                ViewHolder holder;

                convertView = mInflater.inflate(R.layout.list_item_boardview, null);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.iconnew = (ImageView) convertView.findViewById(R.id.iconnew);
                convertView.setTag(holder);
                holder.title.setText(title);
                if (isNew == 1) {
                    holder.iconnew.setImageResource(R.drawable.ic_brightness_1_red_6dp);
                } else {
                    holder.iconnew.setImageResource(0);
                }
            }

            return convertView;
        }

        static class ViewHolder {
            TextView title;
            ImageView iconnew;
        }
        static class GroupHolder {
            TextView title;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        m_listView = (ListView) findViewById(R.id.listView);
        m_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, Object> item;
                item = m_arrayItems.get(position);
                String title = (String) item.get("title");
                String type = (String) item.get("type");
                String link = (String) item.get("link");

                if (type.contains("link")) {
                    Intent intent = new Intent(BoardActivity.this, WebActivity.class);
                    intent.putExtra("ITEMS_TITLE", title);
                    intent.putExtra("ITEMS_LINK", link);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(BoardActivity.this, ItemsActivity.class);
                    intent.putExtra("ITEMS_TITLE", title);
                    intent.putExtra("ITEMS_LINK", link);
                    startActivity(intent);
                }
            }
        });

        AdView adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        m_app = (MoojigaeApplication)getApplication();

        intenter();

        setTitle(m_boardTitle);

        m_arrayItems = new ArrayList<>();

        m_pd = ProgressDialog.show(this, "", "로딩중", true, false);

        Thread thread = new Thread(this);
        thread.start();
    }

    private static class MyHandler extends Handler {
        private final WeakReference<BoardActivity> mActivity;
        public MyHandler(BoardActivity activity) {
            mActivity = new WeakReference<BoardActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            BoardActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    private final MyHandler mHandler = new MyHandler(this);

    public void run() {
        getData();
        mHandler.sendEmptyMessage(0);
    }

    private void handleMessage(Message msg) {
        if (m_pd != null) {
            if (m_pd.isShowing()) {
                m_pd.dismiss();
            }
        }
        displayData();
    }

    public void displayData() {
        m_listView.setAdapter(new EfficientAdapter(BoardActivity.this, m_arrayItems));
    }

    public void intenter() {
//    	Intent intent = getIntent();  // 값을 가져오는 인텐트 객체생성
    	Bundle extras = getIntent().getExtras();
    	// 가져온 값을 set해주는 부분
    	
    	m_boardTitle = extras.getString("BOARD_TITLE");
    	m_boardCode = extras.getString("BOARD_CODE");
    }

    protected boolean getData() {
        String boardLink = "/board-api-menu.do?comm=" + m_boardCode;

        String url = GlobalConst.m_strServer + boardLink;
        String result = m_app.m_httpRequest.requestPost(url, "", url);

        // 각 항목 찾기
        HashMap<String, Object> item;

        try {
            JSONObject newObject = new JSONObject(result);
            m_strRecent = newObject.getString("recent");
            m_strNew = newObject.getString("new");
            JSONArray arrayItem = newObject.getJSONArray("menu");
            for(int i = 0; i < arrayItem.length(); i++) {
                JSONObject jsonItem = arrayItem.getJSONObject(i);
                item = new HashMap<>();

                item.put("title", jsonItem.getString("title"));
                item.put("type", jsonItem.getString("type"));
                item.put("link", jsonItem.getString("boardId"));

                m_arrayItems.add( item );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < m_arrayItems.size(); i++) {
            item = m_arrayItems.get(i);
            String link = "[" + (String)item.get("link") + "]";
            if (m_strNew.indexOf(link) >= 0) {
                item.put("isNew", 1);
            } else {
                item.put("isNew", 0);
            }
            m_arrayItems.set(i, item);
        }

        return true;
    }
}