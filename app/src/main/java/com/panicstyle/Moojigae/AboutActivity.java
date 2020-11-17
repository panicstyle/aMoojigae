package com.panicstyle.Moojigae;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle("앱정보 보기");

        Context context = AboutActivity.this;
        String version;
        try {
        	PackageInfo i = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        	version = i.versionName;
        } catch (NameNotFoundException e) { 
        	version = "Unknown";
        }
        
        String versionInfo = "버전 : " + version;

        TextView t = (TextView)findViewById(R.id.textVersion);
        t.setText(versionInfo);        
                
    }
}
