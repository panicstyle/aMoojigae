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
import com.panicstyle.Moojigae.BoardActivity;
import com.panicstyle.Moojigae.DBHelper;
import com.panicstyle.Moojigae.GlobalConst;
import com.panicstyle.Moojigae.MainActivity;
import com.panicstyle.Moojigae.MoojigaeApplication;
import com.panicstyle.Moojigae.R;
import com.panicstyle.Moojigae.SetInfo;
import com.panicstyle.Moojigae.Utils;
import com.panicstyle.Moojigae.WebActivity;

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

public class BoardFragment extends Fragment {

    private static final String TAG = "BoardFragment";
    private List<HashMap<String, String>> m_arrayItems;
    private ListView m_listView;
    private EfficientAdapter m_adapter;
    private String m_itemsLink = "";
    private RequestQueue queue;

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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_board, container, false);
        m_listView = (ListView) root.findViewById(R.id.listView);

        m_arrayItems = new ArrayList<HashMap<String, String>>();
        m_adapter = new EfficientAdapter(getActivity(), m_arrayItems);
        m_listView.setAdapter(m_adapter);
        m_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> item;
                item = m_arrayItems.get(position);

                String strTitle = (String) item.get("title");
                String strType = (String) item.get("type");
                String strValue = (String) item.get("value");

                if (strType.contains("link")) {
                    Intent intent = new Intent(getContext(), WebActivity.class);
                    intent.putExtra("ITEMS_TITLE", strTitle);
                    intent.putExtra("ITEMS_LINK", strValue);
                    startActivity(intent);
                } else if (strType.contains("menu")) {
                    Intent intent = new Intent(getContext(), BoardActivity.class);
                    intent.putExtra("BOARD_TITLE", strTitle);
                    intent.putExtra("BOARD_CODE", strValue);
                    startActivity(intent);
                }
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
                            HashMap<String, String> item;
                            JSONArray arrayItem = boardObject.getJSONArray("menu");
                            for(int i = 0; i < arrayItem.length(); i++) {
                                JSONObject jsonItem = arrayItem.getJSONObject(i);
                                item = new HashMap<>();

                                String strValue;

                                // title
                                strValue  = jsonItem.getString("title");
                                item.put("title", strValue);

                                // type
                                strValue  = jsonItem.getString("type");
                                item.put("type", strValue);

                                // value
                                strValue  = jsonItem.getString("value");
                                item.put("value", strValue);

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
}