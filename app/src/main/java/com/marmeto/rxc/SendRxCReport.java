package com.marmeto.rxc;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.marmeto.connections.LoadingScreen;
import com.marmeto.database.AirbillsDataSource;
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

public class SendRxCReport extends Activity {

	final Context context = this;

	EditText newEmailEdit;
	TextView title;
	String error = "";
	String phoneData = "";
	String server = "";
	private Spinner userList;
	private String emailAddress;
	private String task = "";

	private UserDataSource userdatasource;
	private EmailAddressDataSource emailDS;

	private AirbillsDataSource airbillDS;

	// private LocationManager locationManager = null;
	private String dhlLabel = "";
	private String tntLabelsString = "";
	private String timestamp = "";
	private String action = "";
	private String destination = "";
	int caseCount = 0;
	int aggregatedCases = 0;  
	int aggregatedShippers = 0;  
	int disassociatedByDHL = 0;  
	int disassociatedByTNT = 0; 
	int shippersReassigned = 0; 
	int reassociated = 0; 
	int reassociatedShippers = 0; 
	int destroyed = 0; 
	int universalIncrement = 0;
	
	Handler handler = new Handler();
 

	TextView aggregatedNumberOfCasesView; 
	TextView disassociatedByDHLCasesView; 
	TextView disassociatedByTNTCasesView; 
	TextView reassociatedCasesView; 
	TextView destroyedCasesView; 
	
	TextView emailAddressView;
	
	private static String result;
	private static String currentEmail1 = "";
	private static String currentEmail3 = "";
	private static String currentEmail2 = "";
	private boolean hasErrors = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.send_rxc_report);

		// open the data sources
		airbillDS = new AirbillsDataSource(context);
		airbillDS.open();
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
		
		if (intent.hasExtra("task")) {
			task = intent.getExtras().getString("task");
		} 
 

		// get list of all shipping case labels and corresponding mpa codes
		List<String> values = airbillDS.getSAirbillIDs();
		caseCount = values.size();
		String[] tntLabels = new String[values.size()];
		System.err.println("Case Count: " + caseCount);
		System.err.println("tntLabels: " + tntLabels.toString());
		if (values != null && !values.isEmpty()) {
			
			int index = 0;
			for (String value : values) {
				
		 
				
				tntLabels[index] = value.toString();

				String currentAction = airbillDS
						.getAirbillAction(tntLabels[index]);
				//get associated Shippers
				String shippers = airbillDS.getTNTLabels(tntLabels[index]);
				shippers = shippers.replaceAll("\\[", "").replaceAll("\\]","");
				String[] shipperCounts = shippers.split(",");
				
				if (currentAction.equals(ShippingCaseAction.ASSOCIATE
						.toString())) {
					aggregatedCases++;
					aggregatedShippers = aggregatedShippers + shipperCounts.length;
					universalIncrement++;
				}
				
				if (currentAction.equals(ShippingCaseAction.DISASSOCIATE_BY_DHL
						.toString())) {
					disassociatedByDHL++;
					universalIncrement++;
				}
				if (currentAction.equals(ShippingCaseAction.DISASSOCIATE_BY_TNT
						.toString())) {
					disassociatedByTNT++;
					shippersReassigned = shippersReassigned + shipperCounts.length;
					universalIncrement++;
				}
				if (currentAction.equals(ShippingCaseAction.RE_ASSOCIATE
						.toString())) {
					reassociated++;
					reassociatedShippers = reassociatedShippers + shipperCounts.length;
					universalIncrement++;
				}
				if (currentAction.equals(ShippingCaseAction.DESTROY
						.toString())) {
					destroyed++;
					universalIncrement++;
				} 
				index++;
			}
		}
		 
		
		aggregatedNumberOfCasesView = (TextView) findViewById(R.id.aggregatedcasenumberview);
		aggregatedNumberOfCasesView.setText(Integer.toString(aggregatedCases) + " Shipments : " + Integer.toString(aggregatedShippers) + " Shipper Cases");
		
		disassociatedByDHLCasesView = (TextView) findViewById(R.id.disassociatebydhlnumber);
		disassociatedByDHLCasesView.setText(Integer.toString(disassociatedByDHL));
		
		disassociatedByTNTCasesView = (TextView) findViewById(R.id.disassociatedbytntnumber);
		disassociatedByTNTCasesView.setText(Integer.toString(disassociatedByTNT) + " Shipments : " + Integer.toString(shippersReassigned) + " Shipper Cases");
		
		reassociatedCasesView = (TextView) findViewById(R.id.reassociatedsshipmentsnumber);
		reassociatedCasesView.setText(Integer.toString(reassociated) + " Shipments : " + Integer.toString(reassociatedShippers) + " Shipper Cases");
		
		destroyedCasesView = (TextView) findViewById(R.id.destroyedshipmentsnumber);
		destroyedCasesView.setText(Integer.toString(destroyed));
		
		if(universalIncrement == 0){
			findViewById(R.id.sendReportButton).setEnabled(false);
		} 

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
			airbillDS = new AirbillsDataSource(context);
			airbillDS.open();

			List<String> values = airbillDS.getSAirbillIDs();
			caseCount = values.size();
			//String[] tntLabels = new String[values.size()];
			if (values != null && !values.isEmpty()) {
				int index = 0;
				for (String value : values) {
					//tntLabels[index] = value.toString();
					dhlLabel = airbillDS.getDHLLabel(value);
					tntLabelsString = airbillDS.getTNTLabels(value);
					timestamp = airbillDS.getAirbillTimestamp(value);
					action = airbillDS.getAirbillAction(value);
					destination = airbillDS.getDestination(value);
					
					index++;
					JSONObject jsonObject = new JSONObject();
					try {
						String test = tntLabelsString.replace("[", "")
								.replace("]", "").replaceAll(" ", "");
						String[] codesSplit = test.split(",");
						
						JSONArray mpaArray;
						if(Arrays.toString(codesSplit).contains("\"\"")){
							mpaArray= new JSONArray();
						}else{
							mpaArray= new JSONArray(
									Arrays.asList(codesSplit));
						}
						jsonObject.put("carrierTrackingNo", dhlLabel);
						
					
						 
						jsonObject.put("shipcaseLabels", mpaArray);
						jsonObject.put("destinationName", destination);
						jsonObject.put("from", "RxC");
						jsonObject.put("action", action);
						jsonObject.put("scanTime", timestamp);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					array.put(jsonObject);

				}
			}
			JSONObject parentObject = new JSONObject();
			try {
				parentObject.put("shipments", array);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Convert JSONObject to string to send
			json = parentObject.toString();
			System.err.println(json);

			// SEMD REPORT
			Intent intent = new Intent(SendRxCReport.this, LoadingScreen.class);

			intent.setAction(Intent.ACTION_SEND);

			intent.putExtra("task", "sendRxCReport");
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
		JSONObject jObject =  jArray.getJSONObject(2);
		 
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
