package com.panicstyle.Moojigae;

import java.util.regex.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

import com.panicstyle.Moojigae.ArticleView;
import com.panicstyle.Moojigae.HttpRequest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
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

public class CommentView extends ListActivity implements Runnable {
	TextView selection;
    private ProgressDialog pd;
	
    protected String mContent;
    
    private List<HashMap<String, String>> arrayItems;
    private EfficientAdapter adapter;
    
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
            return arrayItems.size();
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
                convertView = mInflater.inflate(R.layout.list_item_comment, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.comment = (TextView) convertView.findViewById(R.id.comment);

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }
            HashMap<String, String> item = new HashMap<String, String>();
            item = (HashMap<String, String>)arrayItems.get(position);
            String name = (String)item.get("name");
            String comment = (String)item.get("comment");
            // Bind the data efficiently with the holder.
            holder.name.setText(name);
            holder.comment.setText(comment);
            return convertView;
        }
        
        static class ViewHolder {
            TextView name;
            TextView comment;
        }
    }
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_main);
        
        intenter();

        arrayItems = new ArrayList<HashMap<String, String>>();
		
        pd = ProgressDialog.show(this, "", "로딩중", true,
                false);

        Thread thread = new Thread(this);
        thread.start();

        return;
    }

    public void run() {
		getData();
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
       		adapter = new EfficientAdapter(CommentView.this, arrayItems);
            setListAdapter(adapter);
    	}
    };        
    
    public void intenter() {
//    	Intent intent = getIntent();  // 값을 가져오는 인텐트 객체생성
    	Bundle extras = getIntent().getExtras();
    	// 가져온 값을 set해주는 부분
    	
    	mContent = extras.getString("CONTENT").toString();
    }

    protected boolean getData() {		
        // 각 항목 찾기
        HashMap<String, String> item;

        String[] items = mContent.split("<tr onMouseOver=this.style.backgroundColor='#F0F8FF'; onMouseOut=this.style.backgroundColor=''; class=bMemo>");
        int i = 0;
        for (i = 1; i < items.length; i++) { // Find each match in turn; String can't do this.     
            String matchstr = items[i];
            item = new HashMap<String, String>();
            
            // Comment ID
            Pattern p = Pattern.compile("(?<=<span id=memoReply_\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d_)(.|\\n)*?(?=>)", Pattern.CASE_INSENSITIVE); 
            Matcher m = p.matcher(matchstr);

	        if (m.find()) { // Find each match in turn; String can't do this.     
	        	item.put("commentno", m.group(0));
	        } else {
	        	item.put("commentno", "");
	        }
	        
            // Name
            p = Pattern.compile("(<font onclick=\\\"viewCharacter)(.|\\n)*?(</font>)", Pattern.CASE_INSENSITIVE); 
            m = p.matcher(matchstr);

            String strName;
	        if (m.find()) { // Find each match in turn; String can't do this.     
	        	strName = m.group(0);
	        } else {
	        	strName = "";
	        }
	        strName = strName.replaceAll("<((.|\\n)*?)+>", "");
	        item.put("name",  strName);
	        
	        // comment
            p = Pattern.compile("(<span id=memoReply_)(.|\\n)*?(</span>)", Pattern.CASE_INSENSITIVE); 
            m = p.matcher(matchstr);

            String strComment;
	        if (m.find()) { // Find each match in turn; String can't do this.     
	        	strComment = m.group(0);
	        } else {
	        	strComment = "";
	        }
	        strComment = strComment.replaceAll("\n", "");
	        strComment = strComment.replaceAll("\r", "");
	        strComment = strComment.replaceAll("<br>", "\n");
	        strComment = strComment.replaceAll("&nbsp;", " ");
	        strComment = strComment.replaceAll("(<)(.|\\n)*?(>)", "");
	        item.put("comment",  strComment);

            arrayItems.add( item );
        }
        
        return true;
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        HashMap<String, String> item = new HashMap<String, String>();
        item = (HashMap<String, String>)arrayItems.get(position);
        String commentno = (String)item.get("commentno");
        String comment = (String)item.get("comment");
        
        Intent intent = getIntent();
        intent.putExtra("COMMENTNO", commentno);
        intent.putExtra("COMMENT",  comment);
        if (getParent() == null) {
           	setResult(Activity.RESULT_OK, intent);
        } else {
         	getParent().setResult(Activity.RESULT_OK, intent);
        }
        finish();
    }
}