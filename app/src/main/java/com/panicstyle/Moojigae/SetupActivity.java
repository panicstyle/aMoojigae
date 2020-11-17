package com.panicstyle.Moojigae;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;

public class SetupActivity extends AppCompatActivity implements Runnable {
	static final int SETUP_CODE = 1234;
	private SetInfo m_setInfo;
	private ProgressDialog m_pd;
	private int m_loginStatus;
	MoojigaeApplication m_app;
	private String m_strErrorMsg;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup);
		setTitle("로그인 설정하기");

		m_app = (MoojigaeApplication)getApplication();

		m_setInfo = new SetInfo();
		if (m_setInfo.GetUserInfo(this)) {
			String userID = m_setInfo.m_userID;
			String userPW = m_setInfo.m_userPW;
			boolean pushYN = m_setInfo.m_pushYN;
			EditText tID = (EditText) findViewById(R.id.id);
			tID.setText(userID);
			EditText tPW = (EditText) findViewById(R.id.password);
			tPW.setText(userPW);
			Switch switchYN = (Switch) findViewById(R.id.pusy_yn);
			switchYN.setChecked(pushYN);
		}

		findViewById(R.id.sign_in_button).setOnClickListener(mClickListener);
	}

	public void SaveData() {
		m_pd = ProgressDialog.show(this, "", "로그인중", true, false);
		Thread thread = new Thread(this);
		thread.start();
	}

	private static class MyHandler extends Handler {
		private final WeakReference<SetupActivity> mActivity;
		public MyHandler(SetupActivity activity) {
			mActivity = new WeakReference<SetupActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			SetupActivity activity = mActivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}

	private final MyHandler mHandler = new MyHandler(this);

	public void run() {
		LoadData(this);
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
		if (m_loginStatus == 1) {
			if (getParent() == null) {
				setResult(Activity.RESULT_OK);
			} else {
				getParent().setResult(Activity.RESULT_OK);
			}
			finish();
		} else {
			AlertDialog.Builder ab = null;
			ab = new AlertDialog.Builder( this );
			ab.setMessage( "로그인을 실패했습니다.\n오류내용 : " + m_strErrorMsg + "\n아이디와 비밀번호를 다시 한번 확인하세요.");
			ab.setPositiveButton(android.R.string.ok, null);
			ab.setTitle("로그인 오류");
			ab.show();
		}
	}

	private void LoadData(Context context) {
		EditText textID = (EditText) findViewById(R.id.id);
		EditText textPW = (EditText) findViewById(R.id.password);
		Switch switchYN = (Switch) findViewById(R.id.pusy_yn);

		m_setInfo.m_userID = textID.getText().toString();
		m_setInfo.m_userPW = textPW.getText().toString();
		m_setInfo.m_pushYN = switchYN.isChecked();

		Login login = new Login();

		login.Logout(this, m_app.m_httpRequest);

		m_loginStatus = login.LoginTo(this, m_app.m_httpRequest, m_setInfo.m_userID, m_setInfo.m_userPW);
		m_strErrorMsg = login.m_strErrorMsg;

		if (m_loginStatus <= 0) {
			return ;
		}
		m_setInfo.SaveUserInfo(this);

		m_app.m_strUserID = m_setInfo.m_userID;
		m_app.m_strUserPW = m_setInfo.m_userPW;
		m_app.m_nPushYN = m_setInfo.m_pushYN;

//		Toast.makeText(this, "저장합니다", Toast.LENGTH_SHORT).show();

		login.PushRegisterUpdate(this, m_app.m_httpRequest, m_app.m_strUserID, m_app.m_strRegId, m_app.m_nPushYN);
	}

	Button.OnClickListener mClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.sign_in_button:
					SaveData();
					break;
			}
		}

		;
	};
}
