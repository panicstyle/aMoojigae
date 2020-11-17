package com.panicstyle.Moojigae.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.gsm.GsmCellLocation;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.panicstyle.Moojigae.ArticleViewActivity;
import com.panicstyle.Moojigae.DBHelper;
import com.panicstyle.Moojigae.GlobalConst;
import com.panicstyle.Moojigae.MainActivity;
import com.panicstyle.Moojigae.MoojigaeApplication;
import com.panicstyle.Moojigae.R;
import com.panicstyle.Moojigae.SetInfo;
import com.panicstyle.Moojigae.Utils;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecentMemoFragment extends Fragment {

    private static final String TAG = "RecentMemoFragment";
    private List<HashMap<String, Object>> m_arrayItems;
    private ListView m_listView;
    private EfficientAdapter m_adapter;
    private String m_itemsLink = "";
    private RequestQueue queue;

    private static RecentFragment _instance;
    private RequestQueue _requestQueue;
    private SharedPreferences _preferences;

    private static class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<HashMap<String, Object>> arrayItems;

        public EfficientAdapter(Context context, List<HashMap<String, Object>> data) {
            mInflater = LayoutInflater.from(context);
            arrayItems = data;
        }

        public int getCount() {
            return arrayItems.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView != null) {
                Object a = convertView.getTag();
                if (!(a instanceof ViewHolder)) {
                    convertView = null;
                }
            }
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_recent_itemsview, null);

                holder = new ViewHolder();
                holder.boardName = (TextView) convertView.findViewById(R.id.boardName);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.subject = (TextView) convertView.findViewById(R.id.subject);
                holder.comment = (TextView) convertView.findViewById(R.id.comment);
                holder.iconnew = (ImageView) convertView.findViewById(R.id.iconnew);

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
            String hit = (String) item.get("hit");
            int isNew = (Integer) item.get("isNew");
            int isReply = (Integer) item.get("isReply");
            int isRead = (Integer) item.get("read");
            String boardName = (String) item.get("boardName");
            // Bind the data efficiently with the holder.
            name = "<b>" + name + "</b>&nbsp;" + date + "&nbsp;(" + hit + "&nbsp;읽음)" ;
            holder.name.setText(Html.fromHtml(name));
            holder.subject.setText(subject);
            holder.comment.setText(comment);
            holder.boardName.setText(boardName);
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

            return convertView;
        }

        static class ViewHolder {
            TextView boardName;
            TextView name;
            TextView subject;
            TextView comment;
            ImageView iconnew;
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_recent_memo, container, false);
        m_listView = (ListView) root.findViewById(R.id.listView);

        m_arrayItems = new ArrayList<HashMap<String, Object>>();
        m_adapter = new EfficientAdapter(getActivity(), m_arrayItems);
        m_listView.setAdapter(m_adapter);
        m_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, Object> item;
                item = m_arrayItems.get(position);
                Intent intent = new Intent(getContext(), ArticleViewActivity.class);

                intent.putExtra("SUBJECT", (String) item.get("subject"));
                intent.putExtra("DATE", (String) item.get("date"));
                intent.putExtra("USERNAME", (String) item.get("name"));
                intent.putExtra("USERID", (String) item.get("id"));
                intent.putExtra("boardNo", (String) item.get("boardNo"));
                intent.putExtra("HIT", (String) item.get("hit"));
                intent.putExtra("BOARDID", (String) item.get("boardId"));
                intent.putExtra("boardName", (String) item.get("boardName"));
                startActivityForResult(intent, GlobalConst.REQUEST_VIEW);
            }
        });

        m_adapter.notifyDataSetChanged();

        CookieManager manager = new CookieManager();
        CookieHandler.setDefault( manager  );

        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(getContext());

        LoadMenuData();

        return root;
    }

    public void LoadMenuData() {
        String url = GlobalConst.m_strServer + "/board-api-menu.do?comm=moo2_menu";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "response = " + response);
                        try {
                            JSONObject boardObject = new JSONObject(response);

                            // recent
                            m_itemsLink = boardObject.getString("recent");
                            LoadData();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Response Error");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = super.getHeaders();
                Log.d(TAG, "LoadMenuData header=" + headers);
                return headers;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                // since we don't know which of the two underlying network vehicles
                // will Volley use, we have to handle and store session cookies manually
                Log.d(TAG, "LoadMenuData header=" + response.headers);
                return super.parseNetworkResponse(response);
            }
        };
        queue.add(stringRequest);
    }

    public void LoadData() {
        String url = GlobalConst.m_strServer + "/board-api-recent-memo.do?part=index&rid=50&pid=" + m_itemsLink;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "response = " + response);
                        try {
                            // Display the first 500 characters of the response string.
                            HashMap<String, Object> item;

                            // DB에 해당 글 번호를 확인한다.
                            final DBHelper db = new DBHelper(getContext());

                            JSONObject boardObject = new JSONObject(response);
                            JSONArray arrayItem = boardObject.getJSONArray("item");
                            for (int i = 0; i < arrayItem.length(); i++) {
                                JSONObject jsonItem = arrayItem.getJSONObject(i);
                                item = new HashMap<>();

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

                                // boardId.
                                String strBoardId = jsonItem.getString("boardId");
                                item.put("boardId", strBoardId);
                                // boardName
                                String strBoardName = jsonItem.getString("boardName");
                                item.put("boardName", strBoardName);
                                // boardNo
                                String strBoardNo = jsonItem.getString("boardNo");
                                item.put("boardNo", strBoardNo);
                                // subject
                                String strSubject = jsonItem.getString("boardTitle");
                                strSubject = Utils.repalceHtmlSymbol(strSubject);
                                item.put("subject", strSubject);
                                // writer
                                String strName = jsonItem.getString("userNick");
                                item.put("name", strName);
                                // comment
                                String strComment = jsonItem.getString("boardMemo_cnt");
                                item.put("comment", strComment);
                                // date
                                String strDate = jsonItem.getString("boardRegister_dt");
                                item.put("date", strDate);
                                // 조회수
                                item.put("hit", jsonItem.getString("boardRead_cnt"));
                                item.put("isReply", 0);

                                if (db.exist(strBoardNo)) {
                                    item.put("read", 1);
                                } else {
                                    item.put("read", 0);
                                }

                                m_arrayItems.add(item);
                            }
                            m_adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Response Error");
            }
        });
        queue.add(stringRequest);
    }
}