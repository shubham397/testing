package com.marmeto.supervisor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.marmeto.connections.LoadingScreen;
import com.marmeto.database.EmailAddress;
import com.marmeto.database.EmailAddressDataSource;
import com.marmeto.database.ShippingCase;
import com.marmeto.database.ShippingCaseDataSource;
import com.marmeto.database.UserDataSource;
import com.marmeto.global.ErrorHandling;
import com.marmeto.global.Logout;
import com.marmeto.global.ShippingCaseAction;
import com.marmeto.sproxil.Login;
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
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class SendReport extends Activity {

	final Context context = this;

	EditText newEmailEdit;
	TextView title;
	String error = "";
	String phoneData = "";
	String server = "";
	private Spinner userList;
	private String emailAddress;

	private UserDataSource userdatasource;
	private EmailAddressDataSource emailDS;

	private ShippingCaseDataSource shippingCaseDS;

	// private LocationManager locationManager = null;
	private String tntLabel = "";
	private String mpaCodes = "";
	private String timestamp = "";
	private String action = "";
	int caseCount = 0;
	int aggregatedCases = 0;
	int deaggregatedCases = 0;
	int reaggregatedCases = 0;
	Handler handler = new Handler();
 

	TextView aggregatedNumberOfCasesView;
	TextView deaggregatedNumberOfCasesView;
	TextView emailAddressView;
	
	private static String result;
	private static String currentEmail1 = "";
	private static String currentEmail3 = "";
	private static String currentEmail2 = "";
	private boolean hasErrors = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.sendreport);

		// open the data sources
		shippingCaseDS = new ShippingCaseDataSource(context);
		shippingCaseDS.open();
		emailAddressView = (TextView) findViewById(R.id.emailaddressview);
		Intent intent = getIntent();
		if (intent.hasExtra("result")) {
			try {
				result = parseResult(intent.getExtras().getString("result"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
 

		// get list of all shipping case labels and corresponding mpa codes
		List<String> values = shippingCaseDS.getShippingCaseIDs();
		caseCount = values.size();
		String[] tntLabels = new String[values.size()];
		if (values != null && !values.isEmpty()) {
			int index = 0;
			for (String value : values) {
				
		 
				
				tntLabels[index] = value.toString();

				String currentAction = shippingCaseDS
						.getAction(tntLabels[index]);
				if (currentAction.equals(ShippingCaseAction.AGGREGATE
						.toString())) {
					aggregatedCases++;
				}
				if (currentAction.equals(ShippingCaseAction.DEAGGREGATE
						.toString()) || currentAction.equals(ShippingCaseAction.DEAGGREGATE_BY_TNT
								.toString())) {
					deaggregatedCases++;
				}
				index++;
			}
		}

		aggregatedNumberOfCasesView = (TextView) findViewById(R.id.aggregatedcasenumberview);
		aggregatedNumberOfCasesView.setText(Integer.toString(aggregatedCases));
		if(aggregatedCases == 0 && deaggregatedCases == 0){
			findViewById(R.id.sendReportButton).setEnabled(false);
		}

		deaggregatedNumberOfCasesView = (TextView) findViewById(R.id.deaggregatedcasenumberview);
	
		deaggregatedNumberOfCasesView.setText(Integer
				.toString(deaggregatedCases));

		  

		findViewById(R.id.sendReportButton).setOnClickListener(
				new handleSubmit());

		findViewById(R.id.logout).setOnClickListener(new handleLogout()); 

	}

	private class handleLogout implements OnClickListener {
		public void onClick(View v) {
			Logout logout = new Logout();
			logout.logoutRequest(context);

		}
	}

  

	private class handleSubmit implements OnClickListener {
		public void onClick(View v) {

			sendReport();

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
	 * Send request to update the e-mail address
	 */
	private void sendReport() {
		// check if you are connected or not
		if (isConnected()) {

			// build JSON String to send
			String json = "";

			// 3. build jsonObject

			JSONArray array = new JSONArray();
			shippingCaseDS = new ShippingCaseDataSource(context);
			shippingCaseDS.open();

			List<String> values = shippingCaseDS.getShippingCaseIDs();
			caseCount = values.size();
			String[] tntLabels = new String[values.size()];
			if (values != null && !values.isEmpty()) {
				int index = 0;
				for (String value : values) {
					tntLabels[index] = value.toString();
					tntLabel = shippingCaseDS.getTNTLabel(value);
					mpaCodes = shippingCaseDS.getMPACodes(value);
					timestamp = shippingCaseDS.getTimestamp(value);
					action = shippingCaseDS.getAction(value);

					index++;
					JSONObject jsonObject = new JSONObject();
					try {
						String test = mpaCodes.replace("[", "")
								.replace("]", "").replaceAll(" ", "");
						String[] codesSplit = test.split(",");
						jsonObject.put("tntCode", tntLabel);
						JSONArray mpaArray = new JSONArray(
								Arrays.asList(codesSplit));
						jsonObject.put("mpaCodes", mpaArray);
						jsonObject.put("action", action);
						jsonObject.put("aggregateTime", timestamp);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					array.put(jsonObject);

				}
			}
			JSONObject parentObject = new JSONObject();
			try {
				parentObject.put("shipcases", array);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Convert JSONObject to string to send
			json = parentObject.toString();
			System.err.println(json);

			// SEMD REPORT
			Intent intent = new Intent(SendReport.this, LoadingScreen.class);

			intent.setAction(Intent.ACTION_SEND);

			intent.putExtra("task", "sendReport");
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

	@Override
	public void onBackPressed() {
 

	}
	
	/**
	 * Parse through the JSON objects for each task and return either the successful message or the error messages to the user
	 * 
	 * @param result - the JSON object to parse
	 * @return message - the message or error response
	 * @throws JSONException
	 */
	private String parseResult(String result) throws JSONException {
	 
		
		JSONArray jArray = new JSONArray(result);
		String message = "";
		JSONObject jObject =  jArray.getJSONObject(0);
		 
		hasErrors = false;
		JSONArray emailListArray = jObject.getJSONArray("emailAddressList");
		if(emailListArray.length() == 3){
			currentEmail1 = emailListArray.get(0).toString(); 
			currentEmail2 = emailListArray.get(1).toString(); 
			currentEmail3 = emailListArray.get(2).toString();  
		}
		if(emailListArray.length() == 2){
			currentEmail1 = emailListArray.get(0).toString(); 
			currentEmail2 = emailListArray.get(1).toString();  
		}
		if(emailListArray.length() == 1){
			currentEmail1 = emailListArray.get(0).toString();  
		}
		if(emailListArray.length() == 0){
			findViewById(R.id.sendReportButton).setEnabled(false);
		}else{
			String emailList = currentEmail1;
			
			if(!currentEmail2.equals("")){
				emailList = emailList +  ", " + currentEmail2;
			}
			
			if(!currentEmail3.equals("")){
				emailList = emailList +  ", " + currentEmail3;
			}
			System.err.println("EMAIL LIST: "+ emailList);
			emailAddressView.setText(emailList);
		}

		return message;
	}

}
