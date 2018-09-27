package com.marmeto.admin;

 

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView; 

import com.marmeto.connections.ApplySettings;
import com.marmeto.connections.LoadingScreen;
import com.marmeto.global.ErrorHandling;
import com.marmeto.global.Logout;
import com.marmeto.sproxil.Login;
import com.marmeto.sproxiltnt.R;

public class AdminMain extends Activity {
 
	final Context context = this;
 

	private String username = "";
	private String password = ""; 
 
	Handler handler = new Handler();
 

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.admin_main);
		 
		// setContentView(R.layout.main);
		Intent intent = getIntent();
	 
		if (intent.hasExtra("username")) {
			username = intent.getExtras().getString("username");
		}

		if (intent.hasExtra("password")) {
			password = intent.getExtras().getString("password");
		} 
	 
		findViewById(R.id.updatePasswordLayout).setOnClickListener(new handelUpdatePassword());
		findViewById(R.id.updateSettingsLayout).setOnClickListener(new handleUpdateSettings());
		findViewById(R.id.changeEmailLayout).setOnClickListener(new handleChangeEmail());
		
		findViewById(R.id.logout).setOnClickListener(new handleLogout());

	}
 
	private class handleUpdateSettings implements OnClickListener {
		public void onClick(View v) {
			
			if (isConnected()) {

				// build JSON String to send
				String json = "";

				// 3. build jsonObject

				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("settingsRequest", "settingsRequest");
				} catch (JSONException e) {
				 
					e.printStackTrace();
				}
			 

				// Convert JSONObject to string to send
				json = jsonObject.toString();
				System.err.println(json);

				// SEMD REPORT
				Intent intent = new Intent(AdminMain.this, ApplySettings.class);

				intent.setAction(Intent.ACTION_SEND);

				String task = "updateSettings";
				intent.putExtra("task", task);
				intent.putExtra("previousPage", "admin");
				intent.putExtra("data", json);
				intent.setType("text/plain");

				startActivity(intent);
				finish();
			} else {
				// SEND NO CONNECTION ALERT
				noConnectionAlert();
			}
	 
		}
	}
	
	/**
	 * Check if there is a network connection
	 * 
	 * @return
	 */
	public boolean isConnected() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected())
			return true;
		else
			return false;
	}
	
	/*
	 * Alert that there is no connection enabled
	 */
	private void noConnectionAlert() {
		
		ErrorHandling err = new ErrorHandling();
		err.handleError(context);
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		TextView myMsg = new TextView(this);
		myMsg.setText(getText(R.string.attention));
		myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
		myMsg.setTextSize(25);
		myMsg.setTextColor(Color.RED);

		// set title
		alertDialogBuilder.setCustomTitle(myMsg);

		// set dialog message
		alertDialogBuilder
				.setMessage(R.string.noconnectionemail)
				.setCancelable(false)
				.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
		TextView msgTxt = (TextView) alertDialog
				.findViewById(android.R.id.message);
		msgTxt.setTextSize(25);
	}
	 
	private class handelUpdatePassword implements OnClickListener {
		public void onClick(View v) {
		//open update Password Page
			Intent intent = new Intent(AdminMain.this, UpdateRole.class);

			intent.setAction(Intent.ACTION_SEND);
 
			intent.setType("text/plain");

			startActivity(intent);
			finish();
		}
	}
	
	private class handleChangeEmail implements OnClickListener {
		public void onClick(View v) {
			
			if (isConnected()) {

				// build JSON String to send
				String json = "";

				// 3. build jsonObject

				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("getEmails", "getEmails");
				} catch (JSONException e) {
				 
					e.printStackTrace();
				}
			 

				// Convert JSONObject to string to send
				json = jsonObject.toString();
				System.err.println(json);

				// SEMD REPORT
				Intent intent = new Intent(AdminMain.this, LoadingScreen.class);

				intent.setAction(Intent.ACTION_SEND);

				String task = "getEmails";
				intent.putExtra("task", task);
				intent.putExtra("previousPage", "admin");
				intent.putExtra("data", json);
				intent.setType("text/plain");

				startActivity(intent);
				finish();
			} else {
				// SEND NO CONNECTION ALERT
				noConnectionAlert();
			}
			
			
			
			
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
