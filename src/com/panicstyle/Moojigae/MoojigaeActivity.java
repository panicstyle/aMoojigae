package com.panicstyle.Moojigae;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

import com.panicstyle.Moojigae.AboutView;
import com.panicstyle.Moojigae.BoardActivity;
import com.panicstyle.Moojigae.MoojigaeApplication;
import com.panicstyle.Moojigae.Login;
import com.panicstyle.Moojigae.SetView;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.*;

public class MoojigaeActivity extends ListActivity implements Runnable {
	TextView selection;
	protected HttpClient httpClient;
	protected HttpContext httpContext;
    private ProgressDialog pd;
    private int mLoginStatus;
    static final int REQUEST_CODE = 1;
    private String mUserID;
	
    private List<HashMap<String, String>> arrayItems;
    
    private static class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        List<HashMap<String, String>> arrayItems;
        
        public EfficientAdapter(Context context, List<HashMap<String, String>> data) {
            // Cache the LayoutInflate to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);

            arrayItems = data;
        }

        /**
         * The number of items in the list is determined by the number of speeches
         * in our array.
         *
         * @see android.widget.ListAdapter#getCount()
         */
        public int getCount() {
            return arrayItems.size() ;
        }

        /**
         * Since the data comes from an array, just returning the index is
         * sufficent to get at the data. If we were using a more complex data
         * structure, we would return whatever object represents one row in the
         * list.
         *
         * @see android.widget.ListAdapter#getItem(int)
         */
        public Object getItem(int position) {
            return position;
        }

        /**
         * Use the array index as a unique id.
         *
         * @see android.widget.ListAdapter#getItemId(int)
         */
        public long getItemId(int position) {
            return position;
        }

        /**
         * Make a view to hold each row.
         *
         * @see android.widget.ListAdapter#getView(int, android.view.View,
         *      android.view.ViewGroup)
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            ViewHolder holder;

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_rootview, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }
            HashMap<String,String> item = new HashMap<String, String>();
            String title = null;
            item = (HashMap<String, String>)arrayItems.get(position);
            title = (String)item.get("title");
            // Bind the data efficiently with the holder.
            holder.title.setText(title);

            return convertView;
        }

        static class ViewHolder {
            TextView title;
        }
    }
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Look up the AdView as a resource and load a request.
        AdView adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        MoojigaeApplication app = (MoojigaeApplication)getApplication();
        httpClient = app.httpClient;
        httpContext = app.httpContext;
        
        arrayItems = new ArrayList<HashMap<String, String>>();
        mLoginStatus = -1;

        SetInfo setInfo = new SetInfo();

        if (!setInfo.CheckVersionInfo(MoojigaeActivity.this)) {
            AlertDialog.Builder notice = null;
            notice = new AlertDialog.Builder( MoojigaeActivity.this );
            notice.setTitle( "버전 업데이트 알림" );
            notice.setMessage("-방학중품나누기 게시판이 추가되었습니다.");
            notice.setPositiveButton(android.R.string.ok, null);
            notice.show();

            setInfo.SaveVersionInfo(MoojigaeActivity.this);
        }

        pd = ProgressDialog.show(this, "", "로딩중", true, false);

        Thread thread = new Thread(this);
        thread.start();

        return;
    }

    public void run() {
    	LoadData(MoojigaeActivity.this);
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
    		displayData();
    	}
    };        
    
    public void displayData() {
		if (mLoginStatus == -1) {
			AlertDialog.Builder ab = null;
			ab = new AlertDialog.Builder( MoojigaeActivity.this );
			ab.setMessage( "로그인 정보가 설정되지 않았습니다. 설정 메뉴를 통해 로그인 정보를 설정하십시오.");
			ab.setPositiveButton(android.R.string.ok, null);
			ab.setTitle( "로그인 오류" );
			ab.show(); 
		} else if (mLoginStatus == 0){
			AlertDialog.Builder ab = null;
			ab = new AlertDialog.Builder( MoojigaeActivity.this );
			ab.setMessage( "로그인을 실패했습니다 설정 메뉴를 통해 로그인 정보를 변경하십시오.");
			ab.setPositiveButton(android.R.string.ok, null);
			ab.setTitle( "로그인 오류" );
			ab.show();
		} else {
			setListAdapter(new EfficientAdapter(MoojigaeActivity.this, arrayItems));

	        MoojigaeApplication app = (MoojigaeApplication)getApplication();
	        app.mUserID = mUserID;
		}
    }
    
    private boolean LoadData(Context context) {
        
        // Login
		Login login = new Login();
		
		mLoginStatus = login.LoginTo(httpClient, httpContext, context);
		mUserID = login.getUserID();
		
		if (mLoginStatus <= 0) {
			return false;
		}
		return getData(httpClient, httpContext);
    }

    protected boolean getData(HttpClient httpClient, HttpContext httpContext) {		
		
        // 
        HashMap<String, String> item;

        item = new HashMap<String, String>();
        item.put("code",  "maul");
        item.put("title",  "무지개교육마을");
        arrayItems.add( item );
        
        item = new HashMap<String, String>();
        item.put("code",  "school1");
        item.put("title",  "초등무지개학교");
        arrayItems.add( item );
        
        item = new HashMap<String, String>();
        item.put("code",  "school2");
        item.put("title",  "중등무지개학교");
        arrayItems.add( item );
        
        return true;
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        HashMap<String, String> item = new HashMap<String, String>();
        String title = null;
        String code = null;
        item = (HashMap<String, String>)arrayItems.get(position);
        title = (String)item.get("title");
        code = (String)item.get("code");
        
        Intent intent = new Intent(this, BoardActivity.class);
        intent.putExtra("BOARD_TITLE", title);
        intent.putExtra("BOARD_CODE", code);
        startActivity(intent);    
    }
    
    @Override  
    public boolean onCreateOptionsMenu(Menu menu) {  
        super.onCreateOptionsMenu(menu);  
          
        menu.add(0, 0, 0, "설정");  
        menu.add(0, 1, 0, "앱정보");  
          
        return true;  
    }  
      
    @Override  
    public boolean onOptionsItemSelected(MenuItem item) {  
        if (item.getItemId() == 0) {
            Intent intent = new Intent(this, SetView.class);
            startActivityForResult(intent, REQUEST_CODE);
            return true;  
        } else if (item.getItemId() == 1) {
            Intent intent = new Intent(this, AboutView.class);
            startActivity(intent);    
            return true;  
        }   
        return false;  
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	switch(requestCode) {
    	case REQUEST_CODE:
    		if (resultCode == RESULT_OK) {	// resultCode 가 항상 0 으로 넘어옴. 해결책 못 찾음. 일단 SetView 가 실행되면 다시 로딩하자.
    			arrayItems.clear();
    	        mLoginStatus = -1;
    	        
    	        pd = ProgressDialog.show(this, "", "로딩중입니다. 잠시만 기다리십시오...", true, false);

    	        Thread thread = new Thread(this);
    	        thread.start();    		
    	    }
    	}
    }
}