package com.panicstyle.Moojigae;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoardActivity extends AppCompatActivity implements Runnable {
    private ListView m_listView;
    private ProgressDialog m_pd;
	protected String m_boardTitle;
	protected String m_boardCode;
    private HttpRequest m_httpRequest;
    List<HashMap<String, Object>> m_arrayItems;

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
            String link = (String)item.get("link");
            int isNew = (Integer)item.get("isNew");

            if (link.equalsIgnoreCase("-")) {
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
                String link = (String) item.get("link");

                if (link.contains("-cal")) {
                    Intent intent = new Intent(BoardActivity.this, CalendarActivity.class);
                    intent.putExtra("ITEMS_TITLE", title);
                    intent.putExtra("ITEMS_LINK", link);
                    startActivity(intent);
                } else if (link.contains("recent")) {
                    Intent intent = new Intent(BoardActivity.this, RecentItemsActivity.class);
                    intent.putExtra("ITEMS_TITLE", title);
                    intent.putExtra("ITEMS_LINK", m_boardCode);
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

        MoojigaeApplication app = (MoojigaeApplication)getApplication();
        m_httpRequest = app.m_httpRequest;
        
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
        String boardLink;
        if (m_boardCode.equalsIgnoreCase("maul")) {
            setTitle("무지개교육마을");
            boardLink = "MMain.do";
        } else if (m_boardCode.equalsIgnoreCase("school1")) {
            setTitle("초등무지개학교");
            boardLink = "JMain.do";
        } else {
            setTitle("중등무지개학교");
            boardLink = "SMain.do";
        }

        String url = GlobalConst.m_strServer + "/" + boardLink;
        String result = m_httpRequest.requestPost(url, "", url, "euc-kr");
        String newString = Utils.getMatcherFirstString("(function getNewIcon\\(menu\\))(.|\\n)*?(return rntVal;)", result);

        String[] maul = new String[]{
                "recent", "최근글보기",
                "-", "이야기방",
                "mvTopic", " > 이야기방",
                "mvTopic10Year", " > 10주년 행사",
                "mvTopicGoBackHome", " > 무지개 촌(村)",
                "mvEduBasicRight", "교육기본권",
                "mvGongi", "마을 공지사항",
                "-", "공동체사업부",
                "mvGongDong", " > 공동체사업부",
                "mvGongDongFacility", " > 시설기획팀",
                "mvGongDongEvent", " > 행사기획팀",
                "mvGongDongLocalcommunity",  " > 지역사업팀",
                "-", "마을 동아리방",
                "mvDonghowhe", " > 마을 동아리방",
                "mvDonghowheMoojigaeFC", " > 무지개FC",
                "-",  "어울림품앗이",
                "mvPoomASee", " > 어울림품앗이",
                "mvPoomASeeWantBiz", " > 거래하고싶어요",
                "mvPoomASeeBized", " > 거래했어요",
                "mvPoomASeeVacation", " > 방학중품나누기",
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
                "mvEduResearch", "교육연구회",
                "-", "건축위",
                "mvBuilding", " > 위원방",
                "mvBuildingComm", " > 소통방",
                "-", "기금위",
                "mvDonationGongi", " > 공지사항",
                "mvDonationQnA", " > Q & A",
                "toHomePageAdmin", "홈피관련질문",
                "mvUpgrade", "등업요청(메일인증)",
                "maul-cal", "전체일정",
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
                "mjSchoolFood", "급식분과",
                "mjPhoto", "사진첩&동영상",
                "mjData", "학교 자료실",
                "ama-cal", "아마표",
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
                "ms5Class", "5학년방",
                "msStudentAssociation", "학생회방",
                "msParent", "학부모방",
                "msRepresentative", "대표자회",
                "msMinutes", "회의록",
                "msPhoto", "사진첩&동영상",
                "msData", "학교자료실",
                "school2-cal", "전체일정",
        };
        // 각 항목 찾기
        HashMap<String, Object> item;

        if (m_boardCode.equalsIgnoreCase("maul")) {
            for (int i = 0; i < maul.length; i+=2) {
                item = new HashMap<>();
                item.put("link",  maul[i]);
                item.put("title",  maul[i + 1]);
                m_arrayItems.add( item );
            }
        } else if (m_boardCode.equalsIgnoreCase("school1")) {
            for (int i = 0; i < school1.length; i+=2) {
                item = new HashMap<>();
                item.put("link",  school1[i]);
                item.put("title",  school1[i + 1]);
                m_arrayItems.add( item );
            }
        } else {
            for (int i = 0; i < school2.length; i+=2) {
                item = new HashMap<>();
                item.put("link",  school2[i]);
                item.put("title",  school2[i + 1]);
                m_arrayItems.add( item );
            }
        }

        for (int i = 0; i < m_arrayItems.size(); i++) {
            item = m_arrayItems.get(i);
            String link = (String)item.get("link");
            if (newString.indexOf(link) >= 0) {
                item.put("isNew", 1);
            } else {
                item.put("isNew", 0);
            }
            m_arrayItems.set(i, item);
        }

        return true;
    }
}