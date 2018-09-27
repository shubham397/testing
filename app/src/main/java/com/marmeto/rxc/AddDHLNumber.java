package com.marmeto.rxc;

import com.marmeto.database.AirbillsDataSource;
import com.marmeto.global.ErrorHandling;
import com.marmeto.global.Logout;
import com.marmeto.sproxiltnt.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class AddDHLNumber extends Activity {

	final Context context = this;
	private AirbillsDataSource airbillDS;
	private String buttonClicked = "";
	EditText addTrackingEdit;
	TextView title;
	String task = "";

	// private LocationManager locationManager = null;

	Handler handler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.add_dhl_number);

		title = (TextView) findViewById(R.id.title);

		addTrackingEdit = (EditText) findViewById(R.id.addTrackingEdit);

		Intent intent = getIntent();

		if (intent.hasExtra("buttonClicked")) {
			buttonClicked = intent.getExtras().getString("buttonClicked");
		}
		
		if (intent.hasExtra("task")) {
			task = intent.getExtras().getString("task");
		}

		addTrackingEdit.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				enableSubmitIfReady();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		findViewById(R.id.scanShipperCasesButton).setOnClickListener(
				new handleSubmit());
		findViewById(R.id.logout).setOnClickListener(new handleLogout());
		findViewById(R.id.undo).setOnClickListener(new handleUndo());

		findViewById(R.id.scanShipperCasesButton).setEnabled(false);
		findViewById(R.id.undo).setEnabled(false);
	}

	public void enableSubmitIfReady() {

		boolean dhlIsReady = addTrackingEdit.getText().toString().length() > 0;

		if (dhlIsReady) {
			findViewById(R.id.scanShipperCasesButton).setEnabled(true);
			findViewById(R.id.undo).setEnabled(true);
		} else {
			findViewById(R.id.scanShipperCasesButton).setEnabled(false);
			findViewById(R.id.undo).setEnabled(false);
		}
	}

	private class handleUndo implements OnClickListener {
		public void onClick(View v) {

			addTrackingEdit = (EditText) findViewById(R.id.addTrackingEdit);
			addTrackingEdit.setText("");
		}
	}

	private class handleSubmit implements OnClickListener {
		public void onClick(View v) {

			String setTracking = "";

			setTracking = addTrackingEdit.getText().toString();
			// update password for this user
			addDHLTracking(setTracking);

		}
	}

	/*
	 * Provide second confirmation of password change
	 */
	private void addDHLTracking(final String trackingNumber) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		TextView myMsg = new TextView(this);
		myMsg.setText(getText(R.string.attention));
		myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
		myMsg.setTextSize(25);
		myMsg.setTextColor(Color.RED);

		// set title
		alertDialogBuilder.setCustomTitle(myMsg);

		String message = getText(R.string.assigntrackingconfirm) + " "
				+ trackingNumber;
		// set dialog message
		alertDialogBuilder
				.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								airbillDS = new AirbillsDataSource(context);
								airbillDS.open();

								if (buttonClicked.equals("ASSOCIATE")) {

									if (airbillDS
											.checkIfDHLLabelDoesNotExist(trackingNumber)) {

										Intent intent = new Intent(
												AddDHLNumber.this,
												AssignDestination.class);
										intent.putExtra("trackingNumber",
												trackingNumber);
										intent.putExtra("buttonClicked",
												buttonClicked);
										intent.putExtra("task",
												task);
										intent.setType("text/plain");

										startActivity(intent);
										finish();
									} else {
										dhlTrackingExists();
									}
								} else {
									// RE-ASSOCIATE SO DO NOT CHECK IF LABEL
									// EXISTS
									Intent intent = new Intent(
											AddDHLNumber.this,
											AssignDestination.class);
									intent.putExtra("trackingNumber",
											trackingNumber);
									intent.putExtra("buttonClicked",
											buttonClicked);
									intent.putExtra("task",
											task);
									intent.setType("text/plain");

									startActivity(intent);
									finish();
								}
							}
						})
				.setNegativeButton(R.string.no,
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

	/*
	 * Carton exists in other shipping case
	 */
	private void dhlTrackingExists() {

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
				.setMessage(R.string.shipmentexists)
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

	@Override
	public void onBackPressed() {

	}

	/*
	 * Provide confirmation of password change
	 */
	private void backRequest() {
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
				.setMessage(R.string.backrequest)
				.setCancelable(false)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								backRequestConfirm();
							}
						})
				.setNegativeButton(R.string.no,
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

	/*
	 * Provide second confirmation of exiting page
	 */
	private void backRequestConfirm() {
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
				.setMessage(getText(R.string.backrequestconfirm))
				.setCancelable(false)
				.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();

								Intent intent = new Intent(AddDHLNumber.this,
										RxCMain.class);

								startActivity(intent);
								finish();
							}
						})
				.setNegativeButton(R.string.cancel,
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

	private class handleLogout implements OnClickListener {
		public void onClick(View v) {
			Logout logout = new Logout();
			logout.logoutRequest(context);

		}
	}

}
