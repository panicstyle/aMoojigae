package com.panicstyle.Moojigae;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                convertView.setTag(holder);
                holder.title.setText(title);
                if (isNew == 1) {
                    holder.icon.setImageResource(R.drawable.icon_new);
                } else {
                    holder.icon.setImageResource(0);
                }
            }

            return convertView;
        }

        static class ViewHolder {
            TextView title;
            ImageView icon;
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
                    Intent intent = new Intent(BoardActivity.this, CalendarActivity.class);
                    intent.putExtra("ITEMS_TITLE", title);
                    intent.putExtra("ITEMS_LINK", link);
                    startActivity(intent);
                } else if (type.contains("recent")) {
                    Intent intent = new Intent(BoardActivity.this, RecentItemsActivity.class);
                    intent.putExtra("ITEMS_TITLE", title);
                    intent.putExtra("ITEMS_LINK", m_strRecent);
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

    public void run() {
    	getData();
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
        String boardLink = "/board-api-menu.do";
        if (m_boardCode.equalsIgnoreCase("maul")) {
            setTitle("무지개교육마을");
            boardLink += "?comm=1";
        } else if (m_boardCode.equalsIgnoreCase("school1")) {
            setTitle("초등무지개학교");
            boardLink += "?comm=2";
        } else {
            setTitle("중등무지개학교");
            boardLink += "?comm=3";
        }

        String url = GlobalConst.m_strServer + boardLink;
        String result = m_app.m_httpRequest.requestPost(url, "", url, m_app.m_strEncodingOption);

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