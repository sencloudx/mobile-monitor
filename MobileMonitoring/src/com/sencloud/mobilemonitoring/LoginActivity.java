package com.sencloud.mobilemonitoring;

import com.sencloud.cameratest.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class LoginActivity extends Activity {
	private Button login;
//	private Button set_net;
//	private TextView rem_pwd;
//	private ImageView pwd_image;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);

		MyApplication.getInstance().addActivity(this);
		login = (Button) findViewById(R.id.btn_login);
		login.setOnClickListener(loginClick);
		
//		set_net = (Button)findViewById(R.id.set_net_button);
//		set_net.setOnClickListener(setNetClick);
		
//		rem_pwd = (TextView)findViewById(R.id.rem_pwd);
//		rem_pwd.setOnClickListener(remPwdClick);
		
//		pwd_image = (ImageView)findViewById(R.id.pwd_image);
//		pwd_image.setImageResource(R.drawable.no_select_pwd);
	}

	private OnClickListener loginClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			final String ipname = "192.168.2.69";
			intent.putExtra("ipname", ipname);
			intent.setClass(LoginActivity.this, MobileMonitoringActivity.class);
			startActivityForResult(intent,0);
		}

	};
	/*
	private OnClickListener setNetClick = new OnClickListener() {
		public void onClick(View v){
			Intent intent = new Intent();
			final String ipname = "192.168.1.124";
			intent.putExtra("ipname", ipname);
			intent.setClass(LoginActivity.this, CameraTestActivity.class);
			startActivityForResult(intent,0);
		}
		
	};
	
	private OnClickListener remPwdClick = new OnClickListener() {
		public void onClick(View v){
			pwd_image.setImageResource(R.drawable.select_pwd);
		}
	};*/
}
