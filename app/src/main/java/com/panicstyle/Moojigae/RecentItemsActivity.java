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
    protected String m_itemsTitle;
    protected String m_itemsLink;
    private HttpRequest m_httpRequest;
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
                holder.date = (TextView) convertView.findViewById(R.id.date);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.subject = (TextView) convertView.findViewById(R.id.subject);
                holder.comment = (TextView) convertView.findViewById(R.id.comment);
                holder.iconnew = (ImageView) convertView.findViewById(R.id.iconnew);
                holder.iconreply = (ImageView) convertView.findViewById(R.id.iconreply);

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
            int isNew = (Integer) item.get("isNew");
            int isReply = (Integer) item.get("isReply");
            // Bind the data efficiently with the holder.
            holder.date.setText(date);
            holder.name.setText(name);
            holder.subject.setText(subject);
            holder.comment.setText(comment);
            if (isNew == 1) {
                holder.iconnew.setImageResource(R.drawable.icon_new);
            } else {
                holder.iconnew.setImageResource(0);
            }
            if (isReply == 1) {
                holder.iconreply.setImageResource(R.drawable.i_re);
            } else {
                holder.iconreply.setImageResource(0);
            }
            if (comment.length() > 0) {
                holder.comment.setBackgroundResource(R.drawable.layout_circle);
            } else {
                holder.comment.setBackgroundResource(0);
            }

            return convertView;
        }

        static class ViewHolder {
            TextView date;
            TextView name;
            TextView subject;
            TextView comment;
            ImageView iconnew;
            ImageView iconreply;
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
                intent.putExtra("LINK", (String) item.get("link"));
                intent.putExtra("HIT", (String) item.get("hit"));
                intent.putExtra("BOARDID", m_itemsLink);
                startActivityForResult(intent, REQUEST_VIEW);
            }
        });

        AdView m_adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        m_adView.loadAd(adRequest);

        MoojigaeApplication app = (MoojigaeApplication)getApplication();
        m_httpRequest = app.m_httpRequest;

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
            m_LoginStatus = login.LoginTo(RecentItemsActivity.this, m_httpRequest);
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
        String url = "";
        if (m_itemsLink.equalsIgnoreCase("maul")) {
            url = GlobalConst.m_strServer + "/Mboard-recent.do?part=index&rid=50&pid=mvTopic,mvTopic10Year,mvTopicGoBackHome,mvEduBasicRight,mvGongi,mvGongDong,mvGongDongFacility,mvGongDongEvent,mvGongDongLocalcommunity,mvDonghowhe,mvDonghowheMoojiageFC,mvPoomASee,mvPoomASeeWantBiz,mvPoomASeeBized,mvEduLove,mvEduVillageSchool,mvEduDream,mvEduSpring,mvEduSpring,mvMarketBoard,mvHorizonIntroduction,mvHorizonLivingStory,mvSecretariatAddress,mvSecretariatOldData,mvMinutes,mvEduResearch,mvBuilding,mvBuildingComm,mvDonationGongi,mvDonationQnA,toHomePageAdmin,mvUpgrade";
        } else if (m_itemsLink.equalsIgnoreCase("school1")) {
            url = GlobalConst.m_strServer + "/Mboard-recent.do?part=index&rid=50&pid=mjGongi,mjFreeBoard,mjTeacher,mjTeachingData,mjJunior,mjParent,mjParentMinutes,mjAmaDiary,mjSchoolFood,mjPhoto,mjData";
        } else {
            url = GlobalConst.m_strServer + "/Mboard-recent.do?part=index&rid=50&pid=msGongi,msFreeBoard,msOverRainbow,msFreeComment,msTeacher,msSenior,msStudent,ms5Class,msStudentAssociation,msParent,msRepresentative,msMinutes,msPhoto,msData";
        }
        String referer = GlobalConst.m_strServer + "/board-list.do";

        String result = m_httpRequest.requestPost(url, "", referer, "euc-kr");

        HashMap<String, Object> item;

        Matcher m = Utils.getMatcher("(?<=<td width=\\\"2\\\")(.|\\n)*?(?=<th height=\\\"1\\\")", result);
        while (m.find()) { // Find each match in turn; String can't do this.
            item = new HashMap<>();
            String matchstr = m.group(0);

            // isNew
            if (matchstr.contains("/img/town/icon6.GIF")) {
                item.put("isNew", 1);
            } else {
                item.put("isNew", 0);
            }

            // link
            String strLink = Utils.getMatcherFirstString("(?<=setMainBody\\(\\\'contextTableMainBody\\\',\\\')(.|\\n)*?(?=\\\')", matchstr);
            item.put("link", strLink);

            // subject
            String strSubject = Utils.getMatcherFirstString("(?<=target=_self class=\\\"list\\\">)(.|\\n)*?(?=</a>)", matchstr);
            strSubject = Utils.repalceHtmlSymbol(strSubject);
            item.put("subject", strSubject);

            // writer
            String strName = Utils.getMatcherFirstString("(?<=<font style=font-family:Dotum;font-size:8pt;color:royalblue>\\[)(.|\\n)*?(?=\\]</font>)", matchstr);
            item.put("name", strName);

            // comment
            String strComment = Utils.getMatcherFirstString("(?<=<span class=\\\"board-comment\\\">\\()(.|\\n)*?(?=\\)</spen>)", matchstr);
            item.put("comment", strComment);

            // date
            String strDate = Utils.getMatcherFirstString("(?<=<span class=\\\"board-inlet\\\">)\\d\\d\\d\\d-\\d\\d-\\d\\d(?=</span>)", matchstr);
            strDate = Utils.repalceTag(strDate);
            item.put("date", strDate);

            item.put("hit", "");
            item.put("isReply", 0);

            m_arrayItems.add( item );
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