package com.marmeto.rxc;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.marmeto.connections.LoadingScreen;
import com.marmeto.database.AirbillsDataSource;
import com.marmeto.database.SettingsDataSource;
import com.marmeto.global.ErrorHandling;
import com.marmeto.global.Logout;
import com.marmeto.global.ShippingCaseAction;
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
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class AssignDestination extends Activity {

	final Context context = this;
	private AirbillsDataSource airbillDS;
	private String buttonClicked = "";

	EditText addNoteEdit;
	TextView title;
	TextView dhlTrackingNumber; 
	private ArrayList<String> tntLabels = new ArrayList<String>();
	private static String trackingNumber = "";

	private Spinner locationList;
	private String location = "";

	private SettingsDataSource settingsDS;

	// private LocationManager locationManager = null;

	Handler handler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.assign_destination);
		locationList = (Spinner) findViewById(R.id.destinationlist);

		// open the data sources
		settingsDS = new SettingsDataSource(this);
		settingsDS.open();

		// get a list of the destinations and store in a string array for
		// spinner
		String values = settingsDS.getLocationList();
		settingsDS.close();
		String[] locations = values.split(",");

		Intent intent = getIntent();
		if (intent.hasExtra("tntLabels")) {
			tntLabels = intent.getStringArrayListExtra("tntLabels");
		}
		if (intent.hasExtra("buttonClicked")) {
			buttonClicked = intent.getExtras().getString("buttonClicked");
		}

		if (intent.hasExtra("trackingNumber")) {
			trackingNumber = intent.getExtras().getString("trackingNumber");
		}

		ArrayAdapter<String> locationArray = new ArrayAdapter<String>(
				AssignDestination.this, R.layout.customspinner, locations);
		locationArray
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		locationList.setAdapter(locationArray);
		title = (TextView) findViewById(R.id.title); 
		dhlTrackingNumber = (TextView) findViewById(R.id.dhlTrackingNumber);
		dhlTrackingNumber.setText(trackingNumber);

		((TextView) findViewById(R.id.addNoteEdit))
				.setMovementMethod(new ScrollingMovementMethod());

		addNoteEdit = (EditText) findViewById(R.id.addNoteEdit);
		
		if (addNoteEdit != null) {
			addNoteEdit.setHorizontallyScrolling(false);
			addNoteEdit.setLines(4);
		}
		
		findViewById(R.id.updateDestinationButton).setOnClickListener(
				new handleSubmit());
		findViewById(R.id.logout).setOnClickListener(new handleLogout());
	}

	private class handleSubmit implements OnClickListener {
		public void onClick(View v) {

			String setLocation = "";
			String setNote = "";

			Spinner spinner = (Spinner) findViewById(R.id.destinationlist);
			setLocation = spinner.getSelectedItem().toString();

			setNote = addNoteEdit.getText().toString();
			// update password for this user
			updateDestinationConfirm(tntLabels.toString(), setLocation,
					trackingNumber, setNote);

		}
	}
	
	private class updateDestinationConfirm implements OnClickListener {
		public void onClick(View v) {
			

			

		}
	}

	/*
	 * Provide second confirmation of password change
	 */
	private void updateDestinationConfirm(final String tntNumbers,
			final String destination, final String trackingNumber,
			final String note) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		TextView myMsg = new TextView(this);
		myMsg.setText(getText(R.string.attention));
		myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
		myMsg.setTextSize(25);
		myMsg.setTextColor(Color.RED);

		// set title
		alertDialogBuilder.setCustomTitle(myMsg);

		String message = getText(R.string.updatedestinationconfirm) + " "
				+ destination + " "
				+ getText(R.string.updatedestinationconfirm2) + " "
				+ trackingNumber + " "
				+ getText(R.string.updatedestinationconfirm3);
		// set dialog message
		alertDialogBuilder
				.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								Intent intent = new Intent(AssignDestination.this, AssociateAirbill.class);

								ArrayList<String> myList = new ArrayList<String>(tntLabels);
								intent.putExtra("trackingNumber", trackingNumber); 
								intent.putExtra("destination", destination); 
								intent.putExtra("note", note); 
								intent.putExtra("buttonClicked", buttonClicked); 
								intent.putStringArrayListExtra("tntLabels", myList);
								intent.setType("text/plain");

								startActivity(intent);
								finish();
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
	 * Send request to update the e-mail address
	 */
	private void updateDestination(String tntcodes, String destination,
			String trackingNumber, String note) {
		// check if you are connected or not
		if (isConnected()) {

			String json = "";

			// 3. build jsonObject
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("tntCodes", tntcodes);
				jsonObject.put("destinationName", destination);
				jsonObject.put("carrierTrackingNo", trackingNumber);
				jsonObject.put("carrierID", "1");
				jsonObject.put("from", "RxC");
				jsonObject.put("note", note);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// 4. convert JSONObject to JSON to String
			json = jsonObject.toString();
			// SEMD E-MAIL REQUEST
			Intent intent = new Intent(AssignDestination.this,
					LoadingScreen.class);

			intent.setAction(Intent.ACTION_SEND);

			intent.putExtra("task", "assignDestination");

			intent.putExtra("data", json);
			intent.setType("text/plain");

			startActivity(intent);
			// finish();
		} else {
			// SEND NO CONNECTION ALERT
			noConnectionAlert();
		}
	}

	/*
	 * Provide confirmation of password change
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
				.setMessage(R.string.noconnection)
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

								Intent intent = new Intent(
										AssignDestination.this, RxCMain.class);

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
