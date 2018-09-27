package com.marmeto.rxc;

import java.util.Arrays;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.marmeto.admin.AdminMain;
import com.marmeto.connections.LoadingScreen;
import com.marmeto.database.SettingsDataSource;
import com.marmeto.global.ErrorHandling;
import com.marmeto.global.Logout;
import com.marmeto.global.Success;
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
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class UpdateShipment extends Activity {

	final Context context = this;

	EditText addTrackingEdit;
	EditText addNoteEdit;
	TextView title;
	TextView tntPin;

	private static String tntLabel = "";
	private static String task;
	private static String result;
	private boolean hasErrors = true;
	private Spinner locationList;
	private String previousLocation = "";
	private String previousTrackingNumber = "";
	private String previousNote = "";

	private SettingsDataSource settingsDS;

	// private LocationManager locationManager = null;

	Handler handler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.update_shipment);
		locationList = (Spinner) findViewById(R.id.destinationlist);
		Intent intent = getIntent();
		if (intent.hasExtra("task")) {
			task = intent.getExtras().getString("task");
		}

		if (intent.hasExtra("result")) {
			try {
				result = parseResult(intent.getExtras().getString("result"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (hasErrors) {
			cannotFindShipment();
		}

		// open the data sources
		settingsDS = new SettingsDataSource(this);
		settingsDS.open();

		// get a list of the destinations and store in a string array for
		// spinner
		String values = settingsDS.getLocationList();
		settingsDS.close();
		String[] locations = values.split(",");

		if (intent.hasExtra("tntLabel")) {
			tntLabel = intent.getExtras().getString("tntLabel");
		}

		ArrayAdapter<String> locationArray = new ArrayAdapter<String>(
				UpdateShipment.this, R.layout.customspinner, locations);
		locationArray
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		locationList.setAdapter(locationArray);

		// SET SPINNER TO PREVIOUS LOCATION
		if (!previousLocation.equals(null)) {
			int spinnerPostion = locationArray.getPosition(previousLocation);
			locationList.setSelection(spinnerPostion);
			spinnerPostion = 0;
		}
		title = (TextView) findViewById(R.id.title);

		tntPin = (TextView) findViewById(R.id.tntpin);
		tntPin.setText(tntLabel);

		((TextView) findViewById(R.id.addNoteEdit))
				.setMovementMethod(new ScrollingMovementMethod());

		addTrackingEdit = (EditText) findViewById(R.id.addTrackingEdit);
		addNoteEdit = (EditText) findViewById(R.id.addNoteEdit);

		addTrackingEdit.setText(previousTrackingNumber);
		addNoteEdit.setText(previousNote);

		findViewById(R.id.updateDestinationButton).setOnClickListener(
				new handleSubmit());
		findViewById(R.id.logout).setOnClickListener(new handleLogout());

	}

	/*
	 * Provide error for update fail
	 */
	private void cannotFindShipment() {
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
				.setMessage(R.string.shipmentnotfound)
				.setCancelable(false)
				.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								Intent intent = new Intent(UpdateShipment.this,
										ScanLabel.class);
								intent.putExtra("previousPage", "getShipment");
								intent.setType("text/plain");

								startActivity(intent);
								finish();
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

	private class handleSubmit implements OnClickListener {
		public void onClick(View v) {

			String setLocation = "";
			String setTracking = "";
			String setNote = "";

			Spinner spinner = (Spinner) findViewById(R.id.destinationlist);
			setLocation = spinner.getSelectedItem().toString();

			setTracking = addTrackingEdit.getText().toString();
			setNote = addNoteEdit.getText().toString();
			// update password for this user
			updateDestinationConfirm(tntLabel, setLocation, setTracking,
					setNote);

		}
	}

	/*
	 * Provide second confirmation of password change
	 */
	private void updateDestinationConfirm(final String tntNumber,
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
								updateDestination(tntNumber, destination,
										trackingNumber, note);
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
	private void updateDestination(String tntcode, String destination,
			String trackingNumber, String note) {
		// check if you are connected or not
		if (isConnected()) {

			String json = "";

			// 3. build jsonObject
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("tntCode", tntcode);
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
			Intent intent = new Intent(UpdateShipment.this, LoadingScreen.class);

			intent.setAction(Intent.ACTION_SEND);

			intent.putExtra("task", "updateShipment");

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

								Intent intent = new Intent(UpdateShipment.this,
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

	/**
	 * Parse through the JSON objects for each task and return either the
	 * successful message or the error messages to the user
	 * 
	 * @param result
	 *            - the JSON object to parse
	 * @return message - the message or error response
	 * @throws JSONException
	 */
	private String parseResult(String result) throws JSONException {
		System.err.println("IN PARSE RESULT: " + task);

		JSONObject jObject = new JSONObject(result);
		String message = "";

		if (task.equals("getShipment")) {

			if (jObject.has("carrierTrackingNo")) {
				hasErrors = false;

				previousTrackingNumber = jObject.getString("carrierTrackingNo");

			}

			if (jObject.has("note")) {
				hasErrors = false;

				previousNote = jObject.getString("note");

			}

			if (jObject.has("destination")) {
				hasErrors = false;
				JSONObject destinationArray = jObject
						.getJSONObject("destination");
				previousLocation = destinationArray.getString("destination");

			}

			if (jObject.has("errors")) {
				hasErrors = true;
			}

		}

		return message;
	}
	
	private class handleLogout implements OnClickListener {
		public void onClick(View v) {
			Logout logout = new Logout();
			logout.logoutRequest(context);
			
		}
	}

}
