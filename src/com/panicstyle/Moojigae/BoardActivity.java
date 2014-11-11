package com.panicstyle.Moojigae;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

import com.google.android.gms.ads.*;

import com.panicstyle.Moojigae.ItemsActivity;
import com.panicstyle.Moojigae.RecentItemsActivity;

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class BoardActivity extends ListActivity implements Runnable {
	TextView selection;
	protected String boardTitle;
	protected String boardCode;
	protected HttpClient httpClient;
	protected HttpContext httpContext;
    List<HashMap<String, String>> arrayItems;
    private EfficientAdapter adapter;
    private ProgressDialog pd;

    private static class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<HashMap<String,String>> arrayItems;

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

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            HashMap<String, String> item = new HashMap<String, String>();
            item = (HashMap<String, String>)arrayItems.get(position);
            String title = (String)item.get("title");
            String link = (String)item.get("link");
            String isNew = (String)item.get("isNew");

            if (link.equalsIgnoreCase("-")) {
            	convertView = mInflater.inflate(R.layout.list_group_boardview, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                GroupHolder holder;
                holder = new GroupHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);

                convertView.setTag(holder);
	            // Bind the data efficiently with the holder.
	            holder.title.setText(title);
            } else {
            	convertView = mInflater.inflate(R.layout.list_item_boardview, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                ViewHolder holder;
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);

                convertView.setTag(holder);
	            // Bind the data efficiently with the holder.
	            holder.title.setText(title);
	            if (isNew == "1") {
	            	holder.icon.setImageResource(R.drawable.icon_new);
	            } else {
	            	holder.icon.setImageResource(R.drawable.icon_none);
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
		adapter = new EfficientAdapter(BoardActivity.this, arrayItems);
        setListAdapter(adapter);
	}
    
    public void intenter() {
//    	Intent intent = getIntent();  // 값을 가져오는 인텐트 객체생성
    	Bundle extras = getIntent().getExtras();
    	// 가져온 값을 set해주는 부분
    	
    	boardTitle = extras.getString("BOARD_TITLE").toString();
    	boardCode = extras.getString("BOARD_CODE").toString();
    }
    
    protected boolean getData(HttpClient httpClient, HttpContext httpContext) {
    	String boardLink;
    	if (boardCode.equalsIgnoreCase("maul")) {
    		boardLink = "MMain.do";
    	} else if (boardCode.equalsIgnoreCase("school1")) {
    		boardLink = "JMain.do";
    	} else {
    		boardLink = "SMain.do";
    	}
    
    	String url = "http://121.134.211.159/" + boardLink;
		HttpRequest httpRequest = new HttpRequest();

        String result = httpRequest.requestPost(httpClient, httpContext, url, null, "http://121.134.211.159", "euc-kr");

        Pattern p = Pattern.compile("(function getNewIcon\\(menu\\))(.|\\n)*?(return rntVal;)", Pattern.CASE_INSENSITIVE); 
        Matcher m = p.matcher(result);

        String newString;
        if (m.find()) {
        	newString = m.group(0);
        } else {
        	newString = "";
        }

        String[] maul = new String[]{
            "recent", "최근글보기",
            "-", "이야기방",
            "mvTopic", " > 이야기방",
            "mvEduBasicRight", " > 교육기본권",
            "mvTopic10Year", " > 10주년 행사",
            "mvTopicGoBackHome", " > 무지개 촌(村)",
            "mvGongi", "마을 공지사항",
            "-", "공동체사업부",
            "mvGongDong", " > 공동체사업부",
            "mvGongDongFacility", " > 시설기획팀",
            "mvGongDongEvent", " > 행사기획팀",
            "mvGongDongLocalcommunity",  " > 지역사업팀",
            "mvDonghowhe", "마을 동아리방",
            "-",  "어울림품앗이",
            "mvPoomASee", " > 어울림품앗이",
            "mvPoomASeeWantBiz", " > 거래하고싶어요",
            "mvPoomASeeBized", " > 거래했어요",
            "-", "교육사업부",
            "mvEduLove", " > 교육사랑방",
            "mvEduVillageSchool", " > 마을학교",
            "mvEduDream", " > 또하나의꿈",
            "mvEduSpring", " > 교육샘",
            "mvEduSpring", " > 만두",
            "mvMarketBoard", "무지개장터",
            "-", "무지개지평선",
            "mvHorizonIntroduction", " > 가족소개",
            "mvHorizonLivingStory", " > 사는 얘기",
            "-", "사무국",
            "mvSecretariatAddress", " > 마을주민 연락처",
            "mvSecretariatOldData", " > 마을 자료실",
            "mvMinutes", "회의록방",
            "mvDirectors", "이사회",
            "mvCommittee", "운영위",
            "mvHongbo", "홍보위",
            "-", "건축위",
            "mvBuilding", " > 위원방",
            "mvBuildingComm", " > 소통방",
            "-", "기금위",
            "mvDonationGongi", " > 공지사항",
            "mvDonationQnA", " > Q & A",
        };
        String[] school1 = new String[]{
            "recent", "최근글보기",
            "mjGongi", "초등 공지사항",
            "mjFreeBoard", "자유게시판",
            "mjTeacher", "교사방",
            "mjTeachingData", "교사회의록",
            "mjJunior", "아이들방",
            "mjParent", "학부모방",
            "mjParentMinutes", "학부모 회의록",
            "mjAmaDiary", "품앗이분과",
            "mjPhoto", "사진첩&동영상",
            "mjData", "학교 자료실",
        };
        String[] school2 = new String[]{
            "recent", "최근글보기",
            "msGongi","중등 공지사항",
            "msFreeBoard", "학교이야기방",
            "msOverRainbow", "무지개너머",
            "msFreeComment", "자유게시판",
            "msTeacher", "교사방",
            "msSenior", "숙제방",
            "msStudent", "아이들방",
            "msStudentAssociation", "학생회방",
            "msParent", "학부모방",
            "msRepresentative", "대표자회",
            "msMinutes", "회의록",
            "msPhoto", "사진첩&동영상",
            "msData", "학교자료실",
        };
        // 각 항목 찾기
        HashMap<String, String> item;

        if (boardCode.equalsIgnoreCase("maul")) {
            for (int i = 0; i < maul.length; i+=2) {
    	        item = new HashMap<String, String>();
                item.put("link",  maul[i]);
                item.put("title",  maul[i + 1]);
	            arrayItems.add( item );
            }
        } else if (boardCode.equalsIgnoreCase("school1")) {
            for (int i = 0; i < school1.length; i+=2) {
                item = new HashMap<String, String>();
                item.put("link",  school1[i]);
                item.put("title",  school1[i + 1]);
                arrayItems.add( item );
            }
        } else {
            for (int i = 0; i < school2.length; i+=2) {
                item = new HashMap<String, String>();
                item.put("link",  school2[i]);
                item.put("title",  school1[i + 1]);
                arrayItems.add( item );
            }
        }
        
        int i;
        for (i = 0; i < arrayItems.size(); i++) {
            item = (HashMap<String, String>)arrayItems.get(i);
            String link = (String)item.get("link");
            if (newString.indexOf(link) >= 0) {
                item.put("isNew", "1");
            } else {
            	item.put("isNew", "0");
            }
	        arrayItems.set(i, item);
        }
        
        return true;
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        HashMap<String, String> item = new HashMap<String, String>();
        String title = null;
        String link = null;
        item = (HashMap<String, String>)arrayItems.get(position);
        title = (String)item.get("title");
        link = (String)item.get("link");

        if (link.equalsIgnoreCase("-")) return;
        
        if (link.equalsIgnoreCase("recent")) {
        	Intent intent = new Intent(this, RecentItemsActivity.class);
	        intent.putExtra("ITEMS_TITLE", boardTitle);
	        intent.putExtra("ITEMS_LINK", boardCode);
	        startActivity(intent);
        } else {
        	Intent intent = new Intent(this, ItemsActivity.class);
	        intent.putExtra("ITEMS_TITLE", title);
	        intent.putExtra("ITEMS_LINK", link);
	        startActivity(intent);
        }
    }
}