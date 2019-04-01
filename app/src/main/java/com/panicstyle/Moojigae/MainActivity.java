package com.panicstyle.Moojigae;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements Runnable {
    private static final String TAG = "MainActivity";
    private ListView m_listView;
    private ProgressDialog m_pd;
    private int m_LoginStatus;
    static final int SETUP_CODE = 1234;
    private String m_strErrorMsg = "";
    private List<HashMap<String, String>> m_arrayItems;
    private MoojigaeApplication m_app;
    private String m_strRecent = "";
    private int m_nMode = GlobalConst.NAVI_RECENT;

    private static class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        List<HashMap<String, String>> arrayItems;

        public EfficientAdapter(Context context, List<HashMap<String, String>> data) {
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
            ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_main, null);

                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            HashMap<String, String> item;;
            String title;
            item = arrayItems.get(position);
            title = item.get("title");

            holder.title.setText(title);

            return convertView;
        }

        static class ViewHolder {
            TextView title;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
//            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
//                Log.v(TAG,"Permission is granted");
                return true;
            } else {

//                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
//            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_recent:
                    m_nMode = GlobalConst.NAVI_RECENT;
                    setTitle("최신글");
                    loadContent();
                    return true;
                case R.id.navigation_board:
                    m_nMode = GlobalConst.NAVI_BOARD;
                    setTitle("게시판");
                    loadContent();
                    return true;
                case R.id.navigation_sites:
                    m_nMode = GlobalConst.NAVI_SITE;
                    setTitle("사이트");
                    loadContent();
                    return true;
                case R.id.navigation_setting:
                    m_nMode = GlobalConst.NAVI_SETUP;
                    setTitle("설정");
                    loadSetup();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, getString(R.string.app_id));

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // DB Create
        final DBHelper db = new DBHelper(this);
        db.delete();

        m_listView = (ListView) findViewById(R.id.listView);
        m_arrayItems = new ArrayList<HashMap<String, String>>();

        m_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> item = new HashMap<String, String>();
                String strTitle = null;
                String strType = null;
                String strValue = null;
                item = (HashMap<String, String>) m_arrayItems.get(position);
                strTitle = (String) item.get("title");
                strType = (String) item.get("type");
                strValue = (String) item.get("value");

                if (strType.contains("recent")) {
                    Intent intent = new Intent(MainActivity.this, RecentItemsActivity.class);
                    intent.putExtra("ITEMS_TITLE", strTitle);
                    intent.putExtra("ITEMS_TYPE", strValue);
                    intent.putExtra("ITEMS_LINK", m_strRecent);
                    startActivity(intent);
                } else if (strType.contains("link")) {
                    Intent intent = new Intent(MainActivity.this, WebActivity.class);
                    intent.putExtra("ITEMS_TITLE", strTitle);
                    intent.putExtra("ITEMS_LINK", strValue);
                    startActivity(intent);
                } else if (strType.contains("menu")) {
                    Intent intent = new Intent(MainActivity.this, BoardActivity.class);
                    intent.putExtra("BOARD_TITLE", strTitle);
                    intent.putExtra("BOARD_CODE", strValue);
                    startActivity(intent);
                } else {    // type is activity
                    if (strValue.equals("setup")) {
                        Intent intent = new Intent(MainActivity.this, SetupActivity.class);
                        startActivity(intent);
                    } else if (strValue.equals("about")) {
                        Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                        startActivity(intent);
                    }
                }
              }
          });

        AdView AdView;
        AdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        AdView.loadAd(adRequest);

        m_app = (MoojigaeApplication)getApplication();
        m_app.curActivity = this;


        SetInfo setInfo = new SetInfo();

        if (!setInfo.CheckVersionInfo(MainActivity.this)) {
/*
            AlertDialog.Builder notice = null;
            notice = new AlertDialog.Builder( MainActivity.this );
            notice.setTitle( "알림" );
            notice.setMessage("그동안 사진 확대가 안되어서 불편하셨죠. 사진을 클릭하면 확대해서 보실 수 있고 저장도 할 수 있어요.\n최신글보기에서 해당글의 게시판이름을 보여줍니다");
            notice.setPositiveButton(android.R.string.ok, null);
            notice.show();
*/
            setInfo.SaveVersionInfo(MainActivity.this);
        }

        isStoragePermissionGranted();

        if (!setInfo.GetUserInfo(MainActivity.this)) {
            m_app.m_strUserID = "";
            m_app.m_strUserPW = "";
            m_app.m_nPushYN = true;
            AlertDialog.Builder ab = null;
            ab = new AlertDialog.Builder( this );
            ab.setMessage( "로그인 정보가 설정되지 않았습니다. 설정->로그인 설정하기에서 로그인 정보를 설정하십시오.");
            ab.setPositiveButton(android.R.string.ok, null);
            ab.setTitle( "로그인 오류" );
            ab.show();
        } else {
            m_app.m_strUserID = setInfo.m_userID;
            m_app.m_strUserPW = setInfo.m_userPW;
            m_app.m_nPushYN = setInfo.m_pushYN;
        }
        System.out.println("UserID = " +  m_app.m_strUserID);

        GetToken();

        loadContent();
    }

    private void loadContent() {
        m_arrayItems.clear();
        m_pd = ProgressDialog.show(this, "", "로딩중", true, false);

        Thread thread = new Thread(this);
        thread.start();
    }

    private void GetToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        Log.d(TAG, "Refreshed token: " + token);

                        MoojigaeApplication app = (MoojigaeApplication)getApplication();
                        app.m_strRegId = token;
                    }
                });
    }

    public void run() {
        LoadData(MainActivity.this);
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
        m_listView.setAdapter(new EfficientAdapter(MainActivity.this, m_arrayItems));
    }

    private boolean LoadData(Context context) {
        return getData();
    }

    protected boolean getData() {
        HashMap<String, String> item;

        String url = "";
        switch (m_nMode) {
            case GlobalConst.NAVI_RECENT:
                url = GlobalConst.m_strServer + "/board-api-menu.do?comm=moo_menu_1";
                break;
            case GlobalConst.NAVI_BOARD:
                url = GlobalConst.m_strServer + "/board-api-menu.do?comm=moo_menu_2";
                break;
            case GlobalConst.NAVI_SITE:
                url = GlobalConst.m_strServer + "/board-api-menu.do?comm=moo_menu_3";
                break;
        }

        String result = m_app.m_httpRequest.requestPost(url, "", url);

        try {
            JSONObject boardObject = new JSONObject(result);

            // recent
            m_strRecent = boardObject.getString("recent");

            JSONArray arrayItem = boardObject.getJSONArray("menu");
            for(int i = 0; i < arrayItem.length(); i++) {
                JSONObject jsonItem = arrayItem.getJSONObject(i);
                item = new HashMap<>();

                String strValue;

                // title
                strValue  = jsonItem.getString("title");
                item.put("title", strValue);

                // type
                strValue  = jsonItem.getString("type");
                item.put("type", strValue);

                // value
                strValue  = jsonItem.getString("value");
                item.put("value", strValue);

                m_arrayItems.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return true;
    }

    private void loadSetup() {
        m_arrayItems.clear();
        HashMap<String, String> item1, item2;
        item1 = new HashMap<>();
        item2 = new HashMap<>();

        item1.put("title", "로그인 설정하기");
        item1.put("type", "activity");
        item1.put("value", "setup");

        item2.put("title", "앱정보 보기");
        item2.put("type", "activity");
        item2.put("value", "about");

        m_arrayItems.add(item1);
        m_arrayItems.add(item2);

        m_listView.setAdapter(new EfficientAdapter(MainActivity.this, m_arrayItems));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SETUP_CODE:
                    loadContent();
                    break;
            }
        }
    }
}
