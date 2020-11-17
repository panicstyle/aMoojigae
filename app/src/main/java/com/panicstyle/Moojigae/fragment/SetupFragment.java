package com.panicstyle.Moojigae.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.panicstyle.Moojigae.AboutActivity;
import com.panicstyle.Moojigae.MainActivity;
import com.panicstyle.Moojigae.R;
import com.panicstyle.Moojigae.SetupActivity;

public class SetupFragment extends Fragment {

    private static final String TAG = "SetupFragment";
    private List<HashMap<String, String>> m_arrayItems;
    private ListView m_listView;
    private EfficientAdapter m_adapter;

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
        
        View root = inflater.inflate(R.layout.fragment_setup, container, false);

        m_listView = (ListView) root.findViewById(R.id.listView);
        m_arrayItems = new ArrayList<HashMap<String, String>>();

        m_adapter = new EfficientAdapter(getActivity(), m_arrayItems);
        m_listView.setAdapter(m_adapter);

        m_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if ((position - 1) > m_arrayItems.size()) {
                    return;
                }
                Log.i(TAG, "onItemClick : " + position);
                HashMap<String, String> item = new HashMap<String, String>();
                item = (HashMap<String, String>) m_arrayItems.get(position);
                String strTitle = (String) item.get("title");
                String strType = (String) item.get("type");
                String strValue = (String) item.get("value");

                if (strValue.equalsIgnoreCase("setup")) {
                    Intent intent = new Intent(getContext(), SetupActivity.class);
                    startActivity(intent);
                } else if (strValue.equalsIgnoreCase("about")) {
                    Intent intent = new Intent(getContext(), AboutActivity.class);
                    startActivity(intent);
                }
            }
        });

        loadMenuData();

        return root;
    }

    private void loadMenuData() {
        m_arrayItems.clear();
        HashMap<String, String> item1, item2;
        item1 = new HashMap<>();
        item2 = new HashMap<>();

        item1.put("title", "로그인 설정하기");
        item1.put("type", "activity");
        item1.put("value", "setup");

        item2.put("title", "앱정보 보기");
        item2.put("type", "activity");
        item2.put("value", "about");

        m_arrayItems.add(item1);
        m_arrayItems.add(item2);

        m_adapter.notifyDataSetChanged();

        Log.i(TAG, "loadSetup()");
    }
}