package com.marmeto.rxc;

import com.marmeto.admin.AdminMain;
import com.marmeto.database.UserDataSource;
import com.marmeto.global.Logout;
import com.marmeto.sproxil.Login;
import com.marmeto.operator.OperatorMain;
import com.marmeto.sproxiltnt.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Spinner;
import android.widget.TextView;

public class AirbillQueueSuccess extends Activity {

	final Context context = this; 
	private String task;
	private String result;
	

	// private LocationManager locationManager = null;

	Handler handler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.airbill_queue_success);
		
		Intent intent = getIntent();

		if (intent.hasExtra("result")) {
			result = intent.getExtras().getString("result");
		}
		
		if (intent.hasExtra("task")) {
			task = intent.getExtras().getString("task");
		} 
		//set result string
		TextView body =  (TextView) findViewById(R.id.result);
		body.setText(result);
	 

		findViewById(R.id.associateMoreButton).setOnClickListener(new handleAggregateMore());
		findViewById(R.id.logout).setOnClickListener(new handleLogout()); 
	}
	
	 

	@Override
	public void onBackPressed() {
 

	}
	
	private class handleAggregateMore implements OnClickListener {
		public void onClick(View v) {
			Intent intent = new Intent(AirbillQueueSuccess.this, RxCMain.class);

			intent.setType("text/plain");

			startActivity(intent);
			finish();

		}
	}

	private class handleLogout implements OnClickListener {
		public void onClick(View v) {
			Logout logout = new Logout();
			logout.logoutRequest(context);

		}
	}
 

}
