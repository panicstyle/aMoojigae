package com.panicstyle.Moojigae;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
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

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemsActivity extends AppCompatActivity implements Runnable {
	private ListView m_listView;
	private AdView m_adView;
    private ProgressDialog m_pd;
	private String m_strErrorMsg;
	protected String m_itemsTitle;
	protected String m_itemsLink;
    private List<HashMap<String, Object>> m_arrayItems;
    private int m_nPage;
    protected int m_LoginStatus;
	public static int m_nMode;
	private EfficientAdapter m_adapter;
	private int last_position = 0;

	private MoojigaeApplication m_app;

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
				ViewHolder holder = null;
				ViewHolderNotice holderNotice = null;
				ViewHolderRe holderRe = null;

				HashMap<String, Object> item;;
				item = arrayItems.get(position);
				String boardDep = (String)item.get("boardDep");
				String boardRow = (String)item.get("boardRow");
				String date = (String) item.get("date");
				String name = (String) item.get("name");
				String subject = (String) item.get("subject");
				String comment = (String) item.get("comment");
				String hit = (String) item.get("hit");
				int isNew = (Integer) item.get("isNew");
				int isRead = (Integer) item.get("read");
				name = "<b>" + name + "</b>&nbsp;" + date + "&nbsp;(" + hit + "&nbsp;읽음)" ;

				if (convertView != null) {
					Object a = convertView.getTag();

					if (boardRow.equals("0")) {
						if (!(a instanceof ViewHolderNotice)) {
							convertView = null;
						} else {
							holderNotice = (ViewHolderNotice) convertView.getTag();
						}
					} else {
						if (boardDep.equals("1")) {
							if (!(a instanceof ViewHolder)) {
								convertView = null;
							} else {
								holder = (ViewHolder) convertView.getTag();
							}
						} else {
							if (!(a instanceof ViewHolderRe)) {
								convertView = null;
							} else {
								holderRe = (ViewHolderRe) convertView.getTag();
							}
						}
					}
				}

				if (convertView == null) {
					if (boardRow.equals("0")) {
						convertView = mInflater.inflate(R.layout.list_item_itemsview_notice, null);

						holderNotice = new ViewHolderNotice();
						holderNotice.name = (TextView) convertView.findViewById(R.id.name);
						holderNotice.subject = (TextView) convertView.findViewById(R.id.subject);
						holderNotice.comment = (TextView) convertView.findViewById(R.id.comment);
						holderNotice.iconnew = (ImageView) convertView.findViewById(R.id.iconnew);

						convertView.setTag(holderNotice);
					} else {
						if (boardDep.equals("1")) {
							convertView = mInflater.inflate(R.layout.list_item_itemsview, null);

							holder = new ViewHolder();
							holder.name = (TextView) convertView.findViewById(R.id.name);
							holder.subject = (TextView) convertView.findViewById(R.id.subject);
							holder.comment = (TextView) convertView.findViewById(R.id.comment);
							holder.iconnew = (ImageView) convertView.findViewById(R.id.iconnew);

							convertView.setTag(holder);
						} else {
							convertView = mInflater.inflate(R.layout.list_item_reitemsview, null);

							holderRe = new ViewHolderRe();
							holderRe.name = (TextView) convertView.findViewById(R.id.name);
							holderRe.subject = (TextView) convertView.findViewById(R.id.subject);
							holderRe.comment = (TextView) convertView.findViewById(R.id.comment);
							holderRe.iconnew = (ImageView) convertView.findViewById(R.id.iconnew);

							convertView.setTag(holderRe);
						}
					}
				}

				if (boardRow.equals("0")) {
					// Bind the data efficiently with the holder.
					holderNotice.name.setText(Html.fromHtml(name));
					holderNotice.subject.setText(subject);
					holderNotice.comment.setText(comment);
					if (isNew == 1) {
						holderNotice.iconnew.setImageResource(R.drawable.ic_brightness_1_red_6dp);
					} else {
						holderNotice.iconnew.setImageResource(0);
					}
					if (comment.equals("0")) {
						holderNotice.comment.setBackgroundResource(0);
					} else {
						holderNotice.comment.setBackgroundResource(R.drawable.layout_circle);
					}
					if (isRead == 1) {
						holderNotice.subject.setTextColor(Color.parseColor("#AAAAAA"));
					} else {
						holderNotice.subject.setTextColor(Color.parseColor("#000000"));
					}
				} else {
					if (boardDep.equals("1")) {
						// Bind the data efficiently with the holder.
						holder.name.setText(Html.fromHtml(name));
						holder.subject.setText(subject);
						holder.comment.setText(comment);
						if (isNew == 1) {
							holder.iconnew.setImageResource(R.drawable.ic_brightness_1_red_6dp);
						} else {
							holder.iconnew.setImageResource(0);
						}
						if (comment.equals("0")) {
							holder.comment.setBackgroundResource(0);
						} else {
							holder.comment.setBackgroundResource(R.drawable.layout_circle);
						}
						if (isRead == 1) {
							holder.subject.setTextColor(Color.parseColor("#AAAAAA"));
						} else {
							holder.subject.setTextColor(Color.parseColor("#000000"));
						}
					} else {
						// Bind the data efficiently with the holder.
						holderRe.name.setText(Html.fromHtml(name));
						holderRe.subject.setText(subject);
						holderRe.comment.setText(comment);
						if (isNew == 1) {
							holderRe.iconnew.setImageResource(R.drawable.ic_brightness_1_red_6dp);
						} else {
							holderRe.iconnew.setImageResource(0);
						}
						if (comment.equals("0")) {
							holderRe.comment.setBackgroundResource(0);
						} else {
							holderRe.comment.setBackgroundResource(R.drawable.layout_circle);
						}
						if (isRead == 1) {
							holderRe.subject.setTextColor(Color.parseColor("#AAAAAA"));
						} else {
							holderRe.subject.setTextColor(Color.parseColor("#000000"));
						}
					}
				}

				return convertView;
			}
        }

		static class ViewHolder {
			TextView name;
			TextView subject;
			TextView comment;
			ImageView iconnew;
		}

		static class ViewHolderNotice {
			TextView name;
			TextView subject;
			TextView comment;
			ImageView iconnew;
		}

		static class ViewHolderRe {
			TextView name;
			TextView subject;
			TextView comment;
			ImageView iconnew;
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
					last_position = position;
					HashMap<String, Object> item;
					item = m_arrayItems.get(position);
					Intent intent = new Intent(ItemsActivity.this, ArticleViewActivity.class);

					intent.putExtra("MODE", (Integer) m_nMode);
					intent.putExtra("SUBJECT", (String) item.get("subject"));
					intent.putExtra("DATE", (String) item.get("date"));
					intent.putExtra("USERNAME", (String) item.get("name"));
					intent.putExtra("USERID", (String) item.get("id"));
					intent.putExtra("boardNo", (String) item.get("boardNo"));
					intent.putExtra("HIT", (String) item.get("hit"));
					intent.putExtra("BOARDID", m_itemsLink);
					intent.putExtra("boardName", m_itemsTitle);
					startActivityForResult(intent, GlobalConst.REQUEST_VIEW);
				}
			}
		});

		AdView m_adView = (AdView) this.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		m_adView.loadAd(adRequest);

		m_app = (MoojigaeApplication)getApplication();

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

	private static class MyHandler extends Handler {
		private final WeakReference<ItemsActivity> mActivity;
		public MyHandler(ItemsActivity activity) {
			mActivity = new WeakReference<ItemsActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			ItemsActivity activity = mActivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}

	private final MyHandler mHandler = new MyHandler(this);

	public void run() {
		if (!getData()) {
			// Login
			SetInfo setInfo = new SetInfo();
			if (!setInfo.GetUserInfo(ItemsActivity.this)) {
				m_app.m_strUserID = "";
				m_app.m_strUserPW = "";
				m_app.m_nPushYN = true;
			} else {
				m_app.m_strUserID = setInfo.m_userID;
				m_app.m_strUserPW = setInfo.m_userPW;
				m_app.m_nPushYN = setInfo.m_pushYN;
			}

			Login login = new Login();
			m_LoginStatus = login.LoginTo(ItemsActivity.this, m_app.m_httpRequest, m_app.m_strUserID, m_app.m_strUserPW);
			m_strErrorMsg = login.m_strErrorMsg;

			if (m_LoginStatus > 0) {
				if (getData()) {
					m_LoginStatus = 1;
				}
			}
		} else {
			m_LoginStatus = 1;
		}
		mHandler.sendEmptyMessage(0);
	}

	private void handleMessage(Message msg) {
		if (m_pd != null) {
			if (m_pd.isShowing()) {
				m_pd.dismiss();
			}
		}
		displayData();
	}

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
		String url = GlobalConst.m_strServer + "/board-api-list.do?boardId=" + m_itemsLink + "&page=" + Page;

		String result = m_app.m_httpRequest.requestPost(url, "", url);

		if (result.contains("onclick=\"userLogin()")) {
			return false;
		} else {
			m_strErrorMsg = Utils.getMatcherFirstString("(?<=<b>시스템 메세지입니다</b></font><br>)(.|\\n)*?(?=<br>)", result);
			if (!m_strErrorMsg.equalsIgnoreCase("")) return false;
		}

		// 각 항목 찾기
		HashMap<String, Object> item;

		// DB에 해당 글 번호를 확인한다.
		final DBHelper db = new DBHelper(this);

		try {
			JSONObject boardObject = new JSONObject(result);
			JSONArray arrayItem = boardObject.getJSONArray("item");
			for(int i = 0; i < arrayItem.length(); i++) {
				JSONObject jsonItem = arrayItem.getJSONObject(i);
				item = new HashMap<>();

				// boaardRow
				String boardRow = jsonItem.getString("boardRow");
				item.put("boardRow", boardRow);

				// boaardNo
				String boardNo = jsonItem.getString("boardNo");
				item.put("boardNo", boardNo);

				// 새글 여부
				String isNew = jsonItem.getString("recentArticle");
				// isNew
				if (isNew.equals("Y")) {
					item.put("isNew", 1);
				} else {
					item.put("isNew", 0);
				}

				// 업데이트 여부
				String isUpdated = jsonItem.getString("updatedArticle");
				if (isUpdated.equals("Y")) {
					item.put("isUpdated", 1);
				} else {
					item.put("isUpdated", 0);
				}

				// 답변글 여부
				item.put("boardDep", jsonItem.getString("boardDep"));
				// boaardId
				item.put("boardId", jsonItem.getString("boardId"));
				// subject
				String boardTitle = jsonItem.getString("boardTitle");
				boardTitle = Utils.repalceHtmlSymbol(boardTitle);
				item.put("subject", boardTitle);
				// writer
				item.put("name", jsonItem.getString("userNick"));
				// MemoCount
				item.put("comment", jsonItem.getString("boardMemo_cnt"));
				// date
				item.put("date", jsonItem.getString("boardRegister_dt"));
				// 조회수
				item.put("hit", jsonItem.getString("boardRead_cnt"));

				if (db.exist(boardNo)) {
					item.put("read", 1);
				} else {
					item.put("read", 0);
				}

				m_arrayItems.add(item);

			}
		} catch (Exception e) {
				e.printStackTrace();
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
        startActivityForResult(intent, GlobalConst.REQUEST_WRITE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
		switch (requestCode) {
			case GlobalConst.REQUEST_WRITE:
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
			case GlobalConst.REQUEST_VIEW:
				if (resultCode == RESULT_OK) {
					if (m_arrayItems.size() > last_position) {
						HashMap<String, Object> item;
						item = m_arrayItems.get(last_position);
						item.put("read", 1);
						m_arrayItems.set(last_position, item);
						m_adapter.notifyDataSetChanged();
					}
				} else if (resultCode == GlobalConst.RESULT_DELETE) {
					if (m_arrayItems.size() > last_position) {
						m_arrayItems.remove(last_position);
						m_adapter.notifyDataSetChanged();
					}
				}
				break;
			default:
				break;

		}
    }
}