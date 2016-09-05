package com.panicstyle.Moojigae;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MainActivity extends AppCompatActivity implements Runnable {
    private ListView m_listView;

    private ProgressDialog m_pd;
    private int m_LoginStatus;
    static final int SETUP_CODE = 1234;
    private String m_strErrorMsg = "";

    private List<HashMap<String, String>> m_arrayItems;

    private HttpRequest m_httpRequest;

    private MoojigaeApplication m_app;
    private String m_strEncodingOption;

    // SharedPreferences에 저장할 때 key 값으로 사용됨
    public static final String PROPERTY_REG_ID = "registration_id";

    // SharedPreferences에 저장할 때 key 값으로 사용됨
    public static final String PROPERTY_APP_VERSION = "appVersion";

    static String SENDER_ID = "11111111111"; // 프로젝트 아이디

    GoogleCloudMessaging gcm;
    String regid;
    Context context;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_listView = (ListView) findViewById(R.id.listView);
        m_arrayItems = new ArrayList<HashMap<String, String>>();

        m_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                  HashMap<String, String> item = new HashMap<String, String>();
                  String title = null;
                  String code = null;
                  item = (HashMap<String, String>) m_arrayItems.get(position);
                  title = (String) item.get("title");
                  code = (String) item.get("code");

                  Intent intent = new Intent(MainActivity.this, BoardActivity.class);
                  intent.putExtra("BOARD_TITLE", title);
                  intent.putExtra("BOARD_CODE", code);
                  startActivity(intent);
              }
          });

        AdView AdView;
        AdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        AdView.loadAd(adRequest);

        MoojigaeApplication m_app = (MoojigaeApplication)getApplication();
        m_httpRequest = m_app.m_httpRequest;


        SetInfo setInfo = new SetInfo();

        if (!setInfo.CheckVersionInfo(MainActivity.this)) {
            AlertDialog.Builder notice = null;
            notice = new AlertDialog.Builder( MainActivity.this );
            notice.setTitle( "버전 업데이트 알림" );
            notice.setMessage("1.첨부파일을 다운로드 할 수 잇습니다.\n2.안드로이드 최신버전에서 첨부파일 저장기능을 위한 권한요청이 나타날 수 있습니다.");
            notice.setPositiveButton(android.R.string.ok, null);
            notice.show();

            setInfo.SaveVersionInfo(MainActivity.this);
        }

        isStoragePermissionGranted();

        // gcm 등록
        gcm = GoogleCloudMessaging.getInstance(this); // GoogleCloudMessaging 클래스의 인스턴스를 생성한다
        regid = getRegistrationId(context); // 기존에 발급받은 등록 아이디를 가져온다

        if (regid.isEmpty()) { // 기존에 발급된 등록 아이디가 없으면 registerInBackground 메서드를 호출해 GCM 서버에 발급을 요청한다.
            System.out.println("************************************************* gcm 발급");
            registerInBackground();
        }

        System.out.println("************************************************* gcm regid : " + regid);

        m_pd = ProgressDialog.show(this, "", "로딩중", true, false);

        Thread thread = new Thread(this);
        thread.start();
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
        if (m_LoginStatus == -1) {
            AlertDialog.Builder ab = null;
            ab = new AlertDialog.Builder( MainActivity.this );
            ab.setMessage( "로그인 정보가 설정되지 않았습니다. 설정 메뉴를 통해 로그인 정보를 설정하십시오.");
            ab.setPositiveButton(android.R.string.ok, null);
            ab.setTitle( "로그인 오류" );
            ab.show();
        } else if (m_LoginStatus == 0){
            AlertDialog.Builder ab = null;
            ab = new AlertDialog.Builder( MainActivity.this );
            ab.setMessage( "로그인을 실패했습니다.\n오류내용 : " + m_strErrorMsg + "\n설정 메뉴를 통해 로그인 정보를 변경하십시오.");
            ab.setPositiveButton(android.R.string.ok, null);
            ab.setTitle("로그인 오류");
            ab.show();
        } else {
            m_listView.setAdapter(new EfficientAdapter(MainActivity.this, m_arrayItems));
        }
    }

    private boolean LoadData(Context context) {

        // Encoding 정보를 읽어온다.
        EncodingOption encodingOption = new EncodingOption();
        m_app.m_strEncodingOption = encodingOption.getEncodingOption(context, m_httpRequest);
        m_strEncodingOption = m_app.m_strEncodingOption;

        // Login
        Login login = new Login();
        m_LoginStatus = login.LoginTo(context, m_httpRequest, m_strEncodingOption);
        m_strErrorMsg = login.m_strErrorMsg;
        MoojigaeApplication app = (MoojigaeApplication)getApplication();
        app.m_strUserID = login.m_userID;


        if (m_LoginStatus <= 0) {
            return false;
        }

        if (!getData()) {
            m_LoginStatus = 0;
            return false;
        }
        return true;
    }

    protected boolean getData() {

        HashMap<String, String> item;

        item = new HashMap<>();
        item.put("code",  "maul");
        item.put("title",  "무지개교육마을");
        m_arrayItems.add( item );

        item = new HashMap<String, String>();
        item.put("code",  "school1");
        item.put("title",  "초등무지개학교");
        m_arrayItems.add( item );

        item = new HashMap<String, String>();
        item.put("code",  "school2");
        item.put("title",  "중등무지개학교");
        m_arrayItems.add(item);

        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, SETUP_CODE);
            return true;
        } else if (id == R.id.action_info) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch(requestCode) {
            case SETUP_CODE:
                if (resultCode == RESULT_OK) {
                    m_arrayItems.clear();
                    m_pd = ProgressDialog.show(this, "", "로딩중입니다. 잠시만 기다리십시오...", true, false);

                    Thread thread = new Thread(this);
                    thread.start();
                }
        }
    }

    // 저장된 reg id 조회
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context); // 이전에 저장해둔 등록 아이디를 SharedPreferences에서 가져온다.
        String registrationId = prefs.getString(PROPERTY_REG_ID, ""); // 저장해둔 등록 아이디가 없으면 빈 문자열을 반환한다.
        if (registrationId.isEmpty()) {
            System.out.println("************************************************* Registration not found.");
            return "";
        }

        // 앱이 업데이트 되었는지 확인하고, 업데이트 되었다면 기존 등록 아이디를 제거한다.
        // 새로운 버전에서도 기존 등록 아이디가 정상적으로 동작하는지를 보장할 수 없기 때문이다.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) { // 이전에 등록 아이디를 저장한 앱의 버전과 현재 버전을 비교해 버전이 변경되었으면 빈 문자열을 반환한다.
            System.out.println("************************************************* App version changed.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    // reg id 발급
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // 서버에 발급받은 등록 아이디를 전송한다.
                    // 등록 아이디는 서버에서 앱에 푸쉬 메시지를 전송할 때 사용된다.
                    sendRegistrationIdToBackend();

                    // 등록 아이디를 저장해 등록 아이디를 매번 받지 않도록 한다.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                System.out.println("****************************************************************************** msg : " + msg);
            }

        }.execute(null, null, null);
    }

    // SharedPreferences에 발급받은 등록 아이디를 저장해 등록 아이디를 여러 번 받지 않도록 하는 데 사용
    private void storeRegistrationId(Context context, String regid) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        System.out.println("************************************************* Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regid);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    // 등록 아이디를 서버(앱이랑 통신하는 서버)에 전달
    // 서버는 이 등록 아이디를 사용자마다 따로 저장해두었다가 특정 사용자에게 푸쉬 메시지를 전송할 때 사용할 수 도 있음
    private void sendRegistrationIdToBackend() {
/*
        System.out.println("************************************************* 서버에 regid 전달 : " + regid);

        HttpUtil hu = new HttpUtil(context);
        String[] params = {SERVER_URL, "KEY:1234", "REG:" + regid};
        hu.execute(params);
*/
    }

    // 토스트 생성 함수
    public void printToast(String txt) {
        Toast.makeText(this, txt, Toast.LENGTH_SHORT).show();
    }


}
