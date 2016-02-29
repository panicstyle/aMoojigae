package com.panicstyle.Moojigae;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
	static final int SETUP_CODE = 1234;
	private SetInfo m_setInfo;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		setTitle("로그인설정...");

		m_setInfo = new SetInfo();
		boolean isSuccess = false;
		if (m_setInfo.GetUserInfo(this)) {
			isSuccess = true;
		} else if (m_setInfo.GetUserInfoXML(this)) {
			isSuccess = true;
		}
		String userID = m_setInfo.m_userID;
		String userPW = m_setInfo.m_userPW;
		EditText tID = (EditText) findViewById(R.id.id);
		tID.setText(userID);
		EditText tPW = (EditText) findViewById(R.id.password);
		tPW.setText(userPW);

		findViewById(R.id.sign_in_button).setOnClickListener(mClickListener);
	}

	public void SaveData() {
		EditText textID = (EditText) findViewById(R.id.id);
		EditText textPW = (EditText) findViewById(R.id.password);

		m_setInfo.m_userID = textID.getText().toString();
		m_setInfo.m_userPW = textPW.getText().toString();
		m_setInfo.SaveUserInfo(this);

		Toast.makeText(this, "저장합니다", Toast.LENGTH_SHORT).show();

		if (getParent() == null) {
			setResult(Activity.RESULT_OK);
		} else {
			getParent().setResult(Activity.RESULT_OK);
		}
		finish();
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
