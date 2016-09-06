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

public class ItemsActivity extends AppCompatActivity implements Runnable {
	private ListView m_listView;
	private AdView m_adView;
    private ProgressDialog m_pd;
	private String m_strErrorMsg;
	protected String m_itemsTitle;
	protected String m_itemsLink;
	private HttpRequest m_httpRequest;
	private String m_strEncodingOption;
    private List<HashMap<String, Object>> m_arrayItems;
    private int m_nPage;
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
            return arrayItems.size() + 1;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (position == arrayItems.size()) {
            	MoreHolder holder;
                convertView = mInflater.inflate(R.layout.list_item_moreitem, null);

                holder = new MoreHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(holder);
	            holder.title.setText("더 보 기");

				return convertView;
            } else {
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);
		m_listView = (ListView) findViewById(R.id.listView);
		m_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position == m_arrayItems.size()) {
					m_nPage++;
					m_pd = ProgressDialog.show(ItemsActivity.this, "", "로딩중입니다. 잠시만 기다리십시오...", true, false);

					Thread thread = new Thread(ItemsActivity.this);
					thread.start();
				} else {
					HashMap<String, Object> item;
					item = m_arrayItems.get(position);
					Intent intent = new Intent(ItemsActivity.this, ArticleViewActivity.class);

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
			}
		});

		AdView m_adView = (AdView) this.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		m_adView.loadAd(adRequest);

		MoojigaeApplication app = (MoojigaeApplication)getApplication();
		m_httpRequest = app.m_httpRequest;
		m_strEncodingOption = app.m_strEncodingOption;

        intenter();

		setTitle(m_itemsTitle);

        m_nPage = 1;
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
			MoojigaeApplication m_app = (MoojigaeApplication)getApplication();
			m_strEncodingOption = m_app.m_strEncodingOption;

			Login login = new Login();
			m_LoginStatus = login.LoginTo(ItemsActivity.this, m_httpRequest, m_strEncodingOption, m_app.m_strUserID, m_app.m_strUserPW);
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
			ab = new AlertDialog.Builder( ItemsActivity.this );
			ab.setMessage( "로그인 정보가 설정되지 않았습니다. 설정 메뉴를 통해 로그인 정보를 설정하십시오.");
			ab.setPositiveButton(android.R.string.ok, null);
			ab.setTitle( "로그인 오류" );
			ab.show();
		} else if (m_LoginStatus == 0){
			AlertDialog.Builder ab = null;
			ab = new AlertDialog.Builder( ItemsActivity.this );
			ab.setMessage( "로그인을 실패했습니다 설정 메뉴를 통해 로그인 정보를 변경하십시오.");
			ab.setPositiveButton(android.R.string.ok, null);
			ab.setTitle( "로그인 오류" );
			ab.show();
		} else {
    		if (m_nPage == 1) {
    			m_adapter = new EfficientAdapter(ItemsActivity.this, m_arrayItems);
    			m_listView.setAdapter(m_adapter);
    		} else {
        		m_adapter.notifyDataSetChanged();
    		}
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
		String Page = Integer.toString(m_nPage);
		String url = GlobalConst.m_strServer + "/board-list.do?boardId=" + m_itemsLink + "&Page=" + Page;

		String result = m_httpRequest.requestPost(url, "", url, m_strEncodingOption);

		if (result.contains("onclick=\"userLogin()")) {
			return false;
		} else {
			m_strErrorMsg = Utils.getMatcherFirstString("(?<=<b>시스템 메세지입니다</b></font><br>)(.|\\n)*?(?=<br>)", result);
			if (!m_strErrorMsg.equalsIgnoreCase("")) return false;
		}

		// 각 항목 찾기
		HashMap<String, Object> item;

		Matcher m = Utils.getMatcher("(<tr height=22 align=center class=fAContent>)(.|\\n)*?(<td colspan=8 height=1 background=./img/skin/default/footer_line.gif></td>)", result);
		while (m.find()) { // Find each match in turn; String can't do this.
			item = new HashMap<>();
			String matchstr = m.group(0);

			// isNew
			if (matchstr.contains("<img src=./img/skin/default/i_new.gif")) {
				item.put("isNew", 1);
			} else {
				item.put("isNew", 0);
			}

			// isReply  <img src=./img/skin/default/i_re.gif
			if (matchstr.contains("<img src=./img/skin/default/i_re.gif")) {
				item.put("isReply", 1);
			} else {
				item.put("isReply", 0);
			}

			// link
			String strLink = Utils.getMatcherFirstString("(?<=<a href=)(.|\\n)*?(?=[ ])", matchstr);
			item.put("link", strLink);

			// subject
			String strSubject = Utils.getMatcherFirstString("(?<=class=\\\"list\\\">)(.|\\n)*?(?=</a>)", matchstr);
			strSubject = Utils.repalceHtmlSymbol(strSubject);
			item.put("subject", strSubject);

			// writer
			String strName = Utils.getMatcherFirstString("(?<=<td id=tBbsCol7 name=tBbsCol7 width=100 align=center>)(.|\\n)*?(?=</td>)", matchstr);
			strName = Utils.repalceHtmlSymbol(strName);
			item.put("name", strName);

			// comment
			String strComment = Utils.getMatcherFirstString("(?<=<font class=fAMemo>)(.|\\n)*?(?=</font>)", matchstr);
			item.put("comment", strComment);

			// date
			String strDate = Utils.getMatcherFirstString("(?<=class=mdlgray>)\\d\\d\\d\\d-\\d\\d-\\d\\d(?=</td>)", matchstr);
			strDate = Utils.repalceTag(strDate);
			item.put("date", strDate);

			item.put("hit", "");

			m_arrayItems.add( item );
		}

		return true;
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_items, menu);

		return super.onCreateOptionsMenu(menu);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_add:
				addArticle();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void addArticle() {
        Intent intent = new Intent(this, ArticleWriteActivity.class);
		int nMode = 0;	// 0 is New article
		intent.putExtra("MODE", nMode);
	    intent.putExtra("BOARDID", m_itemsLink);
	    intent.putExtra("BOARDNO",  "");
		intent.putExtra("TITLE", "");
		intent.putExtra("CONTENT", "");
        startActivityForResult(intent, REQUEST_WRITE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	switch(requestCode) {
    		case REQUEST_WRITE:
			case REQUEST_VIEW:
				if (resultCode == RESULT_OK) {
					m_arrayItems.clear();
					m_adapter.notifyDataSetChanged();
					m_nPage = 1;

					m_pd = ProgressDialog.show(this, "", "로딩중", true,
							false);

					Thread thread = new Thread(this);
					thread.start();
				}
				break;
			default:
				break;

    	}
    }
}