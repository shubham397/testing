package com.marmeto.supervisor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
import android.os.Environment;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.marmeto.admin.AdminMain;
import com.marmeto.connections.ApplySettings;
import com.marmeto.connections.LoadingScreen;
import com.marmeto.database.ShippingCase;
import com.marmeto.database.ShippingCaseDataSource;
import com.marmeto.database.UserDataSource;
import com.marmeto.database.Users;
import com.marmeto.global.AddLabel;
import com.marmeto.global.ErrorHandling;
import com.marmeto.global.Logout;
import com.marmeto.global.ShippingCaseAction;
import com.marmeto.sproxil.Login;
import com.marmeto.rxc.RxCAdmin;
import com.marmeto.rxc.RxCMain;
import com.marmeto.rxc.ScanLabel;
import com.marmeto.sproxiltnt.R;

public class SupervisorMain extends Activity {

	final Context context = this;

	private String username = "";
	private String password = "";
	private ShippingCaseDataSource shippingCaseDS;

	Handler handler = new Handler();

	ArrayList<String> tntCodes = new ArrayList<String>();

	ArrayList<String> codes = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.supervisor_main);

		// setContentView(R.layout.main);
		Intent intent = getIntent();

		if (intent.hasExtra("username")) {
			username = intent.getExtras().getString("username");
		}

		if (intent.hasExtra("password")) {
			password = intent.getExtras().getString("password");
		}

		// Add shipping case and mpa codes

		// get list of TNT CODES, MAP TO
//		GetTNTCodes();
//		GetMPACodes();
//		PopulateDatabase();

		findViewById(R.id.deaggregateLayout).setOnClickListener(
				new handleDeaggregate());
		findViewById(R.id.reaggregateLayout).setOnClickListener(
				new handleReaggregate());
		findViewById(R.id.uploadReportLayout).setOnClickListener(
				new handleUploadReport());

		findViewById(R.id.logout).setOnClickListener(new handleLogout());

	}

	private void PopulateDatabase() {

		//
		System.err.println("CODE SIZE HERE " + codes.size());
		ArrayList<String> mpaCodes = new ArrayList<String>();
		int j = 0;
		int k = 0;
		for (int i = 0; i < codes.size(); i++) {
			// pre populate the queue
			k++;

			mpaCodes.add(codes.get(i));

			if (k % 50 == 0) {

				System.err.println("j " + j);
				// System.err.println("j " + j);
				System.err.println("MPACODE SIZE HERE " + mpaCodes.size());

				shippingCaseDS = new ShippingCaseDataSource(context);
				shippingCaseDS.open();
				shippingCaseDS.insertShippingCase(tntCodes.get(j),
						mpaCodes.toString(),
						ShippingCaseAction.AGGREGATE.toString(),
						System.currentTimeMillis());
				//
				System.err.println(tntCodes.get(0));
				System.err.println(mpaCodes.toString());
				j++;
				mpaCodes.clear();
			}

		}

	}

	public void GetMPACodes() {

		try {
			File f = new File("/sdcard/mpacodes.txt");
			FileInputStream fileIS = new FileInputStream(f);
			BufferedReader buf = new BufferedReader(new InputStreamReader(
					fileIS));
			String readString = new String();

			int i = 0;

			while ((readString = buf.readLine()) != null) {

				codes.add(readString);

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void GetTNTCodes() {

		try {
			File f = new File("/sdcard/tntcodes.txt");
			FileInputStream fileIS = new FileInputStream(f);
			BufferedReader buf = new BufferedReader(new InputStreamReader(
					fileIS));
			String readString = new String();

			int i = 0;
			while ((readString = buf.readLine()) != null) {
				tntCodes.add(readString);

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class handleReaggregate implements OnClickListener {
		public void onClick(View v) {
			Intent intent = new Intent(SupervisorMain.this,
					DeaggregateByShipper.class);

			intent.setAction(Intent.ACTION_SEND);
			intent.putExtra("previousPage", "supervisor");
			intent.setType("text/plain");

			startActivity(intent);
			finish();
		}
	}

	private class handleUploadReport implements OnClickListener {
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
				Intent intent = new Intent(SupervisorMain.this,
						LoadingScreen.class);

				intent.setAction(Intent.ACTION_SEND);

				String task = "getEmails";
				intent.putExtra("task", task);
				intent.putExtra("previousPage", "supervisor");
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
				.setMessage(R.string.noconnectionreports)
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

	private class handleDeaggregate implements OnClickListener {
		public void onClick(View v) {
			Intent intent = new Intent(SupervisorMain.this, Deaggregate.class);

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
