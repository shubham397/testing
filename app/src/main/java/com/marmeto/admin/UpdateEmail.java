package com.marmeto.admin;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.marmeto.admin.AdminMain;
import com.marmeto.connections.LoadingScreen;
import com.marmeto.database.EmailAddress;
import com.marmeto.database.EmailAddressDataSource;
import com.marmeto.database.UserDataSource;
import com.marmeto.global.EmailGroups;
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
import android.widget.Spinner;
import android.widget.TextView;

public class UpdateEmail extends Activity {

	final Context context = this;

	EditText newEmailEdit1;
	EditText newEmailEdit2;
	EditText newEmailEdit3;
	TextView title;
	String error = "";
	String phoneData = "";
	String server = "";
	private Spinner userList;
	String emailAddress;
	TextView emailAddressView;
	private UserDataSource userdatasource;
	private EmailAddressDataSource emailDS;
	private static String result;
	private static String currentEmail1;
	private static String currentEmail3;
	private static String currentEmail2;
	private boolean hasErrors = true;

	// private LocationManager locationManager = null;

	Handler handler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.update_email);
		userList = (Spinner) findViewById(R.id.userList);

		newEmailEdit1 = (EditText) findViewById(R.id.newEmailEdit1);
		newEmailEdit2 = (EditText) findViewById(R.id.newEmailEdit2);
		newEmailEdit3 = (EditText) findViewById(R.id.newEmailEdit3);

		Intent intent = getIntent();
		if (intent.hasExtra("result")) {
			try {
				result = parseResult(intent.getExtras().getString("result"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		newEmailEdit1.addTextChangedListener(new TextWatcher() {

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

		findViewById(R.id.updateEmail).setOnClickListener(new handleSubmit());
		findViewById(R.id.logout).setOnClickListener(new handleLogout());

	}

	public void enableSubmitIfReady() {

		boolean newEmailIsReady = newEmailEdit1.getText().toString().length() > 0;

		if (newEmailIsReady) {
			findViewById(R.id.updateEmail).setEnabled(true);
		} else {
			findViewById(R.id.updateEmail).setEnabled(false);
		}
	}

	private class handleSubmit implements OnClickListener {
		public void onClick(View v) {

			String newEmail1 = "";
			String newEmail2 = "";
			String newEmail3 = "";

			newEmail1 = newEmailEdit1.getText().toString();
			newEmail2 = newEmailEdit2.getText().toString();
			newEmail3 = newEmailEdit3.getText().toString();
			if (!isValidEmail(newEmail1)) {
				invalidEmailFormat();
			} else {
				if (newEmail2.equals("")) {
					if (newEmail3.equals("")) {
						updateEmailConfirm(newEmail1, newEmail2, newEmail3);
					} else {
						invalidEmailFormat();
					}
				} else {
					if (!isValidEmail(newEmail2)) {
						invalidEmailFormat();
					} else {
						if (newEmail3.equals("")) {
							updateEmailConfirm(newEmail1, newEmail2, newEmail3);
						} else {
							if (isValidEmail(newEmail3)) {
								updateEmailConfirm(newEmail1, newEmail2,
										newEmail3);
							} else {
								invalidEmailFormat();
							}
						}
					}
				}
			}

		}
	}

	/*
	 * Validate that the input matches an e-mail address
	 */
	private final static boolean isValidEmail(CharSequence target) {
		if (target == null) {
			return false;
		} else {
			return android.util.Patterns.EMAIL_ADDRESS.matcher(target)
					.matches();
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

	/*
	 * Provide second confirmation of password change
	 */
	private void updateEmailConfirm(final String newEmail1,
			final String newEmail2, final String newEmail3) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		TextView myMsg = new TextView(this);
		myMsg.setText(getText(R.string.attention));
		myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
		myMsg.setTextSize(25);
		myMsg.setTextColor(Color.RED);

		// set title
		alertDialogBuilder.setCustomTitle(myMsg);

		String message = getText(R.string.emailchangeconfirm) + " " + newEmail1;

		if (!newEmail2.equals("")) {
			message = message + ", " + newEmail2;
		}

		if (!newEmail3.equals("")) {
			message = message + ", " + newEmail3;
		}

		// set dialog message
		alertDialogBuilder
				.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								// UPDATE EMAIL
								updateEmail(newEmail1, newEmail2, newEmail3);
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
	 * Send request to update the e-mail address
	 */
	private void updateEmail(String newEmail1, String newEmail2,
			String newEmail3) {
		// check if you are connected or not
		if (isConnected()) {

			String json = "";

			// 3. build jsonObject
			JSONObject parent = new JSONObject();
			JSONObject address1 = new JSONObject();
			JSONObject address2 = new JSONObject();
			JSONObject address3 = new JSONObject();
			JSONObject jsonObject = new JSONObject();
			JSONArray addresses = new JSONArray();

			try {
				address1.put("address", newEmail1);
				address2.put("address", newEmail2);
				address3.put("address", newEmail3);
				addresses.put(address1);
				addresses.put(address2);
				addresses.put(address3);

				parent.put("emailList", addresses);
				parent.put("group", EmailGroups.PCI_TO);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// 4. convert JSONObject to JSON to String
			json = parent.toString();
			System.err.println("SENDING JSON STRING: " + json);
			// SEMD E-MAIL REQUEST
			Intent intent = new Intent(UpdateEmail.this, LoadingScreen.class);

			intent.setAction(Intent.ACTION_SEND);

			intent.putExtra("task", "updateEmail");
			intent.putExtra("data", json);
			intent.setType("text/plain");

			startActivity(intent);
			finish();
		} else {
			// SEND NO CONNECTION ALERT
			noConnectionAlert();
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
	 * Provide error for update fail
	 */
	private void cannotUpdateError() {
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
				.setMessage(R.string.updateFail)
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

	/*
	 * Provide notification for an invalid email format.
	 */
	private void invalidEmailFormat() {

		ErrorHandling err = new ErrorHandling();
		err.handleError(context);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		TextView myMsg = new TextView(this);
		myMsg.setText(getText(R.string.invalidemailformat));
		myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
		myMsg.setTextSize(25);
		myMsg.setTextColor(Color.RED);

		// set title
		alertDialogBuilder.setCustomTitle(myMsg);

		// set dialog message
		alertDialogBuilder
				.setMessage(R.string.invalidemail)
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

		JSONArray jArray = new JSONArray(result);
		String message = "";
		JSONObject jObject = jArray.getJSONObject(0);

		hasErrors = false;
		JSONArray emailListArray = jObject.getJSONArray("emailAddressList");

		if (emailListArray.length() == 3) {
			newEmailEdit1.setText(emailListArray.get(0).toString());
			newEmailEdit2.setText(emailListArray.get(1).toString());
			newEmailEdit3.setText(emailListArray.get(2).toString());
		}
		if (emailListArray.length() == 2) {
			newEmailEdit1.setText(emailListArray.get(0).toString());
			newEmailEdit2.setText(emailListArray.get(1).toString());
		}
		if (emailListArray.length() == 1) {
			newEmailEdit1.setText(emailListArray.get(0).toString());
		}

		if (emailListArray.length() != 0) {
			findViewById(R.id.updateEmail).setEnabled(true);
		} else {
			findViewById(R.id.updateEmail).setEnabled(false);
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
