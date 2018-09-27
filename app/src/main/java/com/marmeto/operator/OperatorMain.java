package com.marmeto.operator;

 

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView; 

import com.marmeto.global.Logout;
import com.marmeto.sproxil.Login;
import com.marmeto.sproxiltnt.R;

public class OperatorMain extends Activity {
 
	final Context context = this;
 

	private String username = "";
	private String password = ""; 
 
	Handler handler = new Handler();
 

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.operator_main);
		Intent i = new Intent("com.motorolasolutions.intent.action.HOMEKEY_MODE");  
		i.putExtra("state", 0);   
		sendBroadcast(i);  
		 
		// setContentView(R.layout.main);
		Intent intent = getIntent();
	 
		if (intent.hasExtra("username")) {
			username = intent.getExtras().getString("username");
		}

		if (intent.hasExtra("password")) {
			password = intent.getExtras().getString("password");
		} 
	 
		findViewById(R.id.aggregateLayout).setOnClickListener(new handleAgregate()); 
		
		findViewById(R.id.logout).setOnClickListener(new handleLogout());

	}
 
 
	 
	private class handleAgregate implements OnClickListener {
		public void onClick(View v) {
			Intent intent = new Intent(OperatorMain.this, Aggregate.class);

			intent.setAction(Intent.ACTION_SEND);
 
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
 

	@Override
	public void onBackPressed() { 

	}
 

}
