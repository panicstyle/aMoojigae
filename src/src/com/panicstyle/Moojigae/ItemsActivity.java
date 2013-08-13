package com.panicstyle.Moojigae;

import java.util.regex.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.panicstyle.Moojigae.ArticleView;
import com.panicstyle.Moojigae.HttpRequest;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ItemsActivity extends ListActivity implements Runnable {
	TextView selection;
    private ProgressDialog pd;
	
	protected String itemsTitle;
	protected String itemsLink;
	protected String mBoard;
	protected HttpClient httpClient;
	protected HttpContext httpContext;
    private List<HashMap<String, String>> arrayItems;
    private int nPage;
    private EfficientAdapter adapter;
    static final int REQUEST_WRITE = 1;
    static final int REQUEST_VIEW = 2;
    protected int mLoginStatus;
    protected String mErrorMsg;
    
    private static class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<HashMap<String, String>> arrayItems;

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
            return arrayItems.size() + 1;
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
            if (position == arrayItems.size()) {
            	MoreHolder holder;
                convertView = mInflater.inflate(R.layout.list_item_moreitem, null);
	
                holder = new MoreHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(holder);
	            holder.title.setText("더 보 기");
            } else {
                // A ViewHolder keeps references to children views to avoid unneccessary calls
                // to findViewById() on each row.
                ViewHolder holder;

                if (convertView != null) {
	                Object a = convertView.getTag();
	                if (!(a instanceof ViewHolder)) {
	                	convertView = null;
	                }
                }
                
                // When convertView is not null, we can reuse it directly, there is no need
                // to reinflate it. We only inflate a new View when the convertView supplied
                // by ListView is null.
	            if (convertView == null) {
	                convertView = mInflater.inflate(R.layout.list_item_itemsview, null);
	
	                // Creates a ViewHolder and store references to the two children views
	                // we want to bind data to.
	                holder = new ViewHolder();
	                holder.date = (TextView) convertView.findViewById(R.id.date);
	                holder.name = (TextView) convertView.findViewById(R.id.name);
	                holder.subject = (TextView) convertView.findViewById(R.id.subject);
	                holder.comment = (TextView) convertView.findViewById(R.id.comment);
	                holder.iconnew = (ImageView) convertView.findViewById(R.id.iconnew);
	                holder.iconreply = (ImageView) convertView.findViewById(R.id.iconreply);
	
	                convertView.setTag(holder);
	            } else {
	                // Get the ViewHolder back to get fast access to the TextView
	                // and the ImageView.
	                holder = (ViewHolder) convertView.getTag();
	            }
	            HashMap<String, String> item = new HashMap<String, String>();
	            item = (HashMap<String, String>)arrayItems.get(position);
	            String date = (String)item.get("date");
	            String name = (String)item.get("name");
	            String subject = (String)item.get("subject");
	            String comment = (String)item.get("comment");
	            String isNew = (String)item.get("isNew");
	            String isReply = (String)item.get("isReply");
	            // Bind the data efficiently with the holder.
	            holder.date.setText(date);
	            holder.name.setText(name);
	            holder.subject.setText(subject);
	            holder.comment.setText(comment);
	            if (isNew.equalsIgnoreCase("1")) {
	            	holder.iconnew.setImageResource(R.drawable.icon_new);
	            } else {
	            	holder.iconnew.setImageResource(R.drawable.icon_none);
	            }
	            if (isReply.equalsIgnoreCase("1")) {
	            	holder.iconreply.setImageResource(R.drawable.i_re);
	            } else {
	            	holder.iconreply.setImageResource(R.drawable.icon_none);
	            }
	            if (comment.length() > 0) {
	            	holder.comment.setBackgroundResource(R.drawable.circle);
	            } else {
	            	holder.comment.setBackgroundResource(R.drawable.icon_none);
	            }
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
        
        static class MoreHolder {
            TextView title;
        }
    }
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        MoojigaeApplication app = (MoojigaeApplication)getApplication();
        httpClient = app.httpClient;
        httpContext = app.httpContext;

        intenter();

        // Look up the AdView as a resource and load a request.
        AdView adView = (AdView)this.findViewById(R.id.adView);
        adView.loadAd(new AdRequest());

        arrayItems = new ArrayList<HashMap<String, String>>();
        nPage = 1;
        
        LoadingData();
    }
    
    public void LoadingData() {
        pd = ProgressDialog.show(this, "", "로딩중입니다. 잠시만 기다리십시오...", true,
                false);

        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
    	if (!getData(httpClient, httpContext)) {
    		if (mErrorMsg.length() > 0) {
    			mLoginStatus = -2;
    		} else {
	            // Login
	    		Login login = new Login();
	    		
	    		mLoginStatus = login.LoginTo(httpClient, httpContext, ItemsActivity.this);
	    		
	    		if (mLoginStatus > 0) {
	    			if (getData(httpClient, httpContext)) {
	    				mLoginStatus = 1;
	    			} else {
	    				mLoginStatus = -2;
	    			}
	    		}
    		} 
    	} else {
			mLoginStatus = 1;
    	}
    	handler.sendEmptyMessage(0);
    }

    private Handler handler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		pd.dismiss();
    		displayData();
    	}
    };
    
    public void displayData() {
		if (mLoginStatus == -1) {
			AlertDialog.Builder ab = null;
			ab = new AlertDialog.Builder( this );
			ab.setMessage( "로그인 정보가 설정되지 않았습니다. 설정 메뉴를 통해 로그인 정보를 설정하십시오.");
			ab.setPositiveButton(android.R.string.ok, null);
			ab.setTitle( "로그인 오류" );
			ab.show();
		} else if (mLoginStatus == -2){
			AlertDialog.Builder ab = null;
			ab = new AlertDialog.Builder( this );
			ab.setMessage( "게시판을 볼 권한이 없습니다. " + mErrorMsg);
			ab.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					finish();
				}
			});
			ab.setTitle( "권한 오류" );
			ab.show();
		} else if (mLoginStatus == 0){
			AlertDialog.Builder ab = null;
			ab = new AlertDialog.Builder( this );
			ab.setMessage( "로그인을 실패했습니다 설정 메뉴를 통해 로그인 정보를 변경하십시오.");
			ab.setPositiveButton(android.R.string.ok, null);
			ab.setTitle( "로그인 오류" );
			ab.show();
		} else {
			if (nPage == 1) {
	    		adapter = new EfficientAdapter(ItemsActivity.this, arrayItems);
	            setListAdapter(adapter);
			} else {
	    		adapter.notifyDataSetChanged();
			}
		}
    }
    
    public void intenter() {
//    	Intent intent = getIntent();  // 값을 가져오는 인텐트 객체생성
    	Bundle extras = getIntent().getExtras();
    	// 가져온 값을 set해주는 부분
    	
    	itemsTitle = extras.getString("ITEMS_TITLE").toString();
    	itemsLink = extras.getString("ITEMS_LINK").toString();
    }

    protected boolean getData(HttpClient httpClient, HttpContext httpContext) {		
		
    	String Page = Integer.toString(nPage);
    	String url = "http://121.134.211.159/board-list.do?boardId=" + itemsLink + "&Page=" + Page;
		HttpRequest httpRequest = new HttpRequest();

        String result = httpRequest.requestPost(httpClient, httpContext, url, null, "http://121.134.211.159/board-list.do", "euc-kr");

        if (result.indexOf("onclick=\"userLogin()") > 0) {
        	return false;
        } else {
            Pattern p = Pattern.compile("(?<=<b>시스템 메세지입니다</b></font><br>)(.|\\n)*?(?=<br>)", Pattern.CASE_INSENSITIVE); 
            Matcher m = p.matcher(result);
            
            if (m.find()) {     
            	mErrorMsg = m.group(0);
            	return false;
            } else {
            	mErrorMsg = "";
            }
        }
        
        // 각 항목 찾기
        HashMap<String, String> item;

        Pattern p = Pattern.compile("(<tr height=22 align=center class=fAContent>)(.|\\n)*?(<td colspan=8 height=1 background=./img/skin/default/footer_line.gif></td>)", Pattern.CASE_INSENSITIVE); 
        Matcher m = p.matcher(result);
        while (m.find()) { // Find each match in turn; String can't do this.     
            item = new HashMap<String, String>();
            String matchstr = m.group(0);
            
            // isNew
            if (matchstr.indexOf("<img src=./img/skin/default/i_new.gif") >= 0) {
                item.put("isNew", "1");
            } else {
            	item.put("isNew", "0");
            }
            
            // isReply  <img src=./img/skin/default/i_re.gif
            if (matchstr.indexOf("<img src=./img/skin/default/i_re.gif") >= 0) {
                item.put("isReply", "1");
            } else {
            	item.put("isReply", "0");
            }
            
	        // link
            Pattern p2 = Pattern.compile("(?<=<a href=)(.|\\n)*?(?=[ ])", Pattern.CASE_INSENSITIVE); 
            Matcher m2 = p2.matcher(matchstr);
	        if (m2.find()) { // Find each match in turn; String can't do this.     
	//        	items.add(m.group(0)); // Access a submatch group; String can't do this. }
	            item.put("link", m2.group(0));
	        } else {
	            item.put("link", "");
	        }
	        
            // subject	
	        p2 = Pattern.compile("(?<=class=\\\"list\\\">)(.|\\n)*?(?=</a>)", Pattern.CASE_INSENSITIVE); 
	        m2 = p2.matcher(matchstr);
	        String subject;
	        if (m2.find()) { // Find each match in turn; String can't do this.     
	//        	items.add(m.group(0)); // Access a submatch group; String can't do this. }
	        	subject = m2.group(0);
	        } else {
	        	subject = "";
	        }
	        // <[a-zA-Z0-9\\s\\\"/&_=\\.\\?;\\-:]+>
//	        subject = subject.replaceAll("<((.|\\n)*?)+>", "");
	        subject = subject.replaceAll("&nbsp;", "");
	        subject = subject.replaceAll("&lt;", "<");
	        subject = subject.replaceAll("&gt;", ">");
	        subject = subject.replaceAll("&amp;", "&");
	        subject = subject.replaceAll("&quot;", "\"");
	        subject = subject.replaceAll("&apos;", "'");
	        subject = subject.trim();
            item.put("subject", subject);

	        // writer
	        p2 = Pattern.compile("(?<=<td id=tBbsCol7 name=tBbsCol7 width=100 align=center>)(.|\\n)*?(?=</td>)", Pattern.CASE_INSENSITIVE); 
	        m2 = p2.matcher(matchstr);
	        String writer;
	        if (m2.find()) { // Find each match in turn; String can't do this.     
	//        	items.add(m.group(0)); // Access a submatch group; String can't do this. }
	            writer = m2.group(0);
	        } else {
	            writer = "";
	        }
	        writer = writer.replaceAll("<((.|\\n)*?)+>", "");
	        writer = writer.trim();
            writer = item.put("name", writer);
            
	        // comment
	        p2 = Pattern.compile("(?<=<font class=fAMemo>)(.|\\n)*?(?=</font>)", Pattern.CASE_INSENSITIVE); 
	        m2 = p2.matcher(matchstr);
	        if (m2.find()) { // Find each match in turn; String can't do this.     
	//        	items.add(m.group(0)); // Access a submatch group; String can't do this. }
	            item.put("comment", m2.group(0));
	        } else {
	            item.put("comment", "");
	        }
            
	        // date
	        p2 = Pattern.compile("(?<=class=mdlgray>)\\d\\d\\d\\d-\\d\\d-\\d\\d(?=</td>)", Pattern.CASE_INSENSITIVE); 
	        m2 = p2.matcher(matchstr);
	        String date;
	        if (m2.find()) { // Find each match in turn; String can't do this.     
	//        	items.add(m.group(0)); // Access a submatch group; String can't do this. }
	        	date = m2.group(0);
	        } else {
	        	date = "";
	        }
	        date = date.replaceAll("<((.|\\n)*?)+>", "");
	        date = date.trim();
      
            item.put("date", date);

            arrayItems.add( item );
        }
        
        return true;
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
    	if (position == arrayItems.size()) {
    		nPage++;
            pd = ProgressDialog.show(this, "", "로딩중입니다. 잠시만 기다리십시오...", true,
                    false);

            Thread thread = new Thread(this);
            thread.start();
    	} else {
	        HashMap<String, String> item = new HashMap<String, String>();
	        item = (HashMap<String, String>)arrayItems.get(position);
	        String subject = (String)item.get("subject");
	        String date = (String)item.get("date");
	        String username = (String)item.get("name");
	        String link = (String)item.get("link");
	
	        Intent intent = new Intent(this, ArticleView.class);
	        intent.putExtra("SUBJECT", subject);
	        intent.putExtra("DATE", date);
	        intent.putExtra("USERNAME", username);
	        intent.putExtra("LINK", link);
	        intent.putExtra("BOARDID", itemsLink);
	        startActivityForResult(intent, REQUEST_VIEW);
    	}
    }

    @Override  
    public boolean onCreateOptionsMenu(Menu menu) {  
        super.onCreateOptionsMenu(menu);  
          
        menu.add(0, 0, 0, "새글 작성");  
          
        return true;  
    }  
      
    @Override  
    public boolean onOptionsItemSelected(MenuItem item) {  
        if (item.getItemId() == 0) {
            Intent intent = new Intent(this, ArticleWrite.class);
	        intent.putExtra("BOARDID", itemsLink);
	        intent.putExtra("BOARDNO",  "");
            startActivityForResult(intent, REQUEST_WRITE);
            return true;  
        }   
        return false;  
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	switch(requestCode) {
    	case REQUEST_WRITE:
    		if (resultCode == RESULT_OK) {	// resultCode 가 항상 0 으로 넘어옴. 해결책 못 찾음. 일단 SetView 가 실행되면 다시 로딩하자.
    	        arrayItems.clear();
        		adapter.notifyDataSetChanged();
    	        nPage = 1;
    			
    	        LoadingData();
    	    }
    		break;
    	case REQUEST_VIEW:
    		if (resultCode == RESULT_OK) {	// resultCode 가 항상 0 으로 넘어옴. 해결책 못 찾음. 일단 SetView 가 실행되면 다시 로딩하자.
    	        arrayItems.clear();
        		adapter.notifyDataSetChanged();
    	        nPage = 1;
    			
    	        LoadingData();
    	    }
    		break;
    	}
    }
    
    
}