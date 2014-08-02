package com.panicstyle.Moojigae;

import java.util.regex.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class RecentItemsActivity extends ListActivity implements Runnable {
        TextView selection;
//	String[] items;
	
	protected String itemsTitle;
	protected String itemsLink;
	protected String mBpardID;
	protected HttpClient httpClient;
	protected HttpContext httpContext;
    private List<HashMap<String, String>> arrayItems;
    private EfficientAdapter adapter;
    private ProgressDialog pd;

    private static class EfficientAdapter extends BaseAdapter{
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
                convertView = mInflater.inflate(R.layout.list_item_itemsview, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.date = (TextView) convertView.findViewById(R.id.date);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.subject = (TextView) convertView.findViewById(R.id.subject);
                holder.comment = (TextView) convertView.findViewById(R.id.comment);
                holder.iconnew = (ImageView) convertView.findViewById(R.id.iconnew);

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
            if (comment.length() > 0) {
            	holder.comment.setBackgroundResource(R.drawable.circle);
            } else {
            	holder.comment.setBackgroundResource(R.drawable.icon_none);
            }

            return convertView; 
        }
        
        static class ViewHolder {
            TextView date;
            TextView name;
            TextView subject;
            TextView comment;
            ImageView iconnew;
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

        intenter();

        arrayItems = new ArrayList<HashMap<String, String>>();

        LoadingData();
        
    }

    public void LoadingData() {
        pd = ProgressDialog.show(this, "", "로딩중", true,
                false);

        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        getData(httpClient, httpContext);
        handler.sendEmptyMessage(0);
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
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
        adapter = new EfficientAdapter(RecentItemsActivity.this, arrayItems);
        setListAdapter(adapter);
    }

    public void intenter() {
    	Bundle extras = getIntent().getExtras();
    	
    	itemsTitle = extras.getString("ITEMS_TITLE").toString();
    	itemsLink = extras.getString("ITEMS_LINK").toString();
    }
    
    protected boolean getData(HttpClient httpClient, HttpContext httpContext) {
		
		String url = "";
		HttpRequest httpRequest = new HttpRequest();

		if (itemsLink.equalsIgnoreCase("maul")) {
			url = "http://121.134.211.159/Mboard-recent.do?part=index&rid=50&pid=mvTopic,mvEduBasicRight,mvTopic10Year,mvTopicGoBackHome,mvGongi,mvGongDong,mvGongDongFacility,mvGongDongEvent,mvGongDong,Localcommunity,mvDonghowhe,mvPoomASee,mvPoomASeeWantBiz,mvPoomASeeBized,mvEduLove,mvEduVillageSchool,mvEduDream,mvEduSpring,mvEduSpring,mvMarketBoard,mvHorizonIntroduction,mvHorizonLivingStory,mvSecretariatAddress,mvSecretariatOldData,mvMinutes,mvBuildingComm,mvDonationGongi,mvDonationQnA";
		} else if (itemsLink.equalsIgnoreCase("school1")) {
			url = "http://121.134.211.159/Mboard-recent.do?part=index&rid=50&pid=mjGongi,mjFreeBoard,mjTeacher,mjTeachingData,mjJunior,mjParent,mjParentMinutes,mjAmaDiary,mjData";
		} else {
			url = "http://121.134.211.159/Mboard-recent.do?part=index&rid=50&pid=msGongi,msFreeBoard,msFreeComment,msTeacher,msSenior,msStudent,msStudentAssociation,msParent,msRepresentative,msMinutes,msPhoto,msData";
		}
		
        String result = httpRequest.requestPost(httpClient, httpContext, url, null, "http://121.134.211.159/board-list.do", "euc-kr");

        // ?? ??? a??
        HashMap<String, String> item;
//        List<HashMap<String, String>> arrayItems = new ArrayList<HashMap<String, String>>();

        Pattern p = Pattern.compile("(?<=<td width=\\\"2\\\")(.|\\n)*?(?=<th height=\\\"1\\\")", Pattern.CASE_INSENSITIVE); 
        Matcher m = p.matcher(result);
        while (m.find()) { // Find each match in turn; String can't do this.     
            item = new HashMap<String, String>();
            String matchstr = m.group(0);
            
            // isNew
            if (matchstr.indexOf("/img/town/icon6.GIF") >= 0) {
                item.put("isNew", "1");
            } else {
            	item.put("isNew", "0");
            }
            
	        // link
            Pattern p2 = Pattern.compile("(?<=setMainBody\\(\\\'contextTableMainBody\\\',\\\')(.|\\n)*?(?=\\\')", Pattern.CASE_INSENSITIVE); 
            Matcher m2 = p2.matcher(matchstr);
	        if (m2.find()) { // Find each match in turn; String can't do this.     
	//        	items.add(m.group(0)); // Access a submatch group; String can't do this. }
	            item.put("link", m2.group(0));
	        } else {
	            item.put("link", "");
	        }
	        
            // subject	
	        p2 = Pattern.compile("(?<=target=_self class=\\\"list\\\">)(.|\\n)*?(?=</a>)", Pattern.CASE_INSENSITIVE); 
	        m2 = p2.matcher(matchstr);
	        String subject;
	        if (m2.find()) { // Find each match in turn; String can't do this.     
	//        	items.add(m.group(0)); // Access a submatch group; String can't do this. }
	        	subject = m2.group(0);
	        } else {
	        	subject = "";
	        }
	        // <[a-zA-Z0-9\\s\\\"/&_=\\.\\?;\\-:]+>
	        subject = subject.replaceAll("&nbsp;", "");
	        subject = subject.replaceAll("&lt;", "<");
	        subject = subject.replaceAll("&gt;", ">");
	        subject = subject.replaceAll("&amp;", "&");
	        subject = subject.replaceAll("&quot;", "\"");
	        subject = subject.replaceAll("&apos;", "'");
	        subject = subject.trim();
            item.put("subject", subject);

	        // writer
	        p2 = Pattern.compile("(?<=<font style=font-family:Dotum;font-size:8pt;color:royalblue>\\[)(.|\\n)*?(?=\\]</font>)", Pattern.CASE_INSENSITIVE); 
	        m2 = p2.matcher(matchstr);
	        String writer;
	        if (m2.find()) { // Find each match in turn; String can't do this.     
	//        	items.add(m.group(0)); // Access a submatch group; String can't do this. }
	            writer = m2.group(0);
	        } else {
	            writer = "";
	        }
//	        writer = writer.replaceAll("<((.|\\n)*?)+>", "");
//	        writer = writer.trim();
            item.put("name", writer);
            
	        // comment
	        p2 = Pattern.compile("(?<=<span class=\\\"board-comment\\\">\\()(.|\\n)*?(?=\\)</spen>)", Pattern.CASE_INSENSITIVE);
	        m2 = p2.matcher(matchstr);
	        if (m2.find()) { // Find each match in turn; String can't do this.     
	//        	items.add(m.group(0)); // Access a submatch group; String can't do this. }
	            item.put("comment", m2.group(0));
	        } else {
	            item.put("comment", "");
	        }
            
	        // date
	        p2 = Pattern.compile("(?<=<span class=\\\"board-inlet\\\">)\\d\\d\\d\\d-\\d\\d-\\d\\d(?=</span>)", Pattern.CASE_INSENSITIVE); 
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
        HashMap<String, String> item = new HashMap<String, String>();
        item = (HashMap<String, String>)arrayItems.get(position);
        String subject = (String)item.get("subject");
        String date = (String)item.get("date");
        String username = (String)item.get("name");
        String link = (String)item.get("link");

        Pattern p = Pattern.compile("(?<=boardId=)(.|\\n)*?(?=&)", Pattern.CASE_INSENSITIVE); 
        Matcher m = p.matcher(link);
        String strBoardID;
        if (m.find()) {
        	strBoardID = m.group(0);
        } else {
    		strBoardID = "";
		}
        
        Intent intent = new Intent(this, ArticleView.class);
        intent.putExtra("SUBJECT", subject);
        intent.putExtra("DATE", date);
        intent.putExtra("USERNAME", username);
        intent.putExtra("LINK", link);
        intent.putExtra("BOARDID", strBoardID);
        startActivity(intent);    
    }

}