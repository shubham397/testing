package com.marmeto.connections;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.marmeto.admin.UpdateEmail;
import com.marmeto.database.EmailAddressDataSource;
import com.marmeto.database.Settings;
import com.marmeto.global.Success;
import com.marmeto.rxc.RxCMain;
import com.marmeto.rxc.SendRxCReport;
import com.marmeto.rxc.UpdateRxCEmail;
import com.marmeto.rxc.UpdateShipment;
import com.marmeto.sproxiltnt.R;
import com.marmeto.supervisor.SendReport;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class LoadingScreen extends Activity {

	final Context context = this;
	// Introduce an delay
	private final int WAIT_TIME = 5500;
	private String response = "";

 
	private String previousPage = "";
	 

	private static String task = "";
	private String data = "";

	private static LinkedList<String> tntCodes = new LinkedList<String>();
	private static LinkedList<String> errorMessages = new LinkedList<String>();
	/*
	 * RxC results
	 */
	private static String confirmationCode = "";
	private static String tntLabel = "";
	private static String trackingNumber = "";
	private static String destination = "";
	private static String note = "";

	private static int error = 0;
	private static boolean hasErrors;
	private String PROD_KEY = "P@oA^~$~z83-jb@LD5fL";
	private String UAT_KEY = "c3uF%#+t=&JNKKgc#FFg";
	//private String appKey = PROD_KEY;
	private String appKey = UAT_KEY;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		super.onCreate(savedInstanceState);

		setContentView(R.layout.loadingscreen);

		findViewById(R.id.mainSpinner1).setVisibility(View.VISIBLE);

		Intent intent = getIntent();

		if (intent.hasExtra("previousPage")) {
			previousPage = intent.getExtras().getString("previousPage");
		}
 

		if (intent.hasExtra("task")) {
			task = intent.getExtras().getString("task");
		}

		if (intent.hasExtra("data")) {
			data = intent.getExtras().getString("data");
		}
		 
		if (intent.hasExtra("tntLabel")) {
			tntLabel = intent.getExtras().getString("tntLabel");
		}

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {

				handleSubmit(task, data);

			}
		}, WAIT_TIME);
	}

	/*
	 * Asynchronously submit data
	 * 
	 * @author Dan
	 */
	private void handleSubmit(String task, String data) {

		URLs url = new URLs(task);
		String version = ((Settings) getApplication()).getVersion();
		String address = url.getURL(version);

		if (address.equals("")) {
			cannotSubmit();
		} else {
			new HttpAsyncTask(task, data).execute(address);
		}

	}

	/*
	 * Provide notification for submission failure, most likely due to not
	 * finding URL
	 */
	private void cannotSubmit() {
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
				.setMessage(R.string.fail)
				.setCancelable(false)
				.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
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

	/*
	 * Asynchronously send data to server and wait for response
	 * 
	 * @author Dan
	 */
	private class HttpAsyncTask extends AsyncTask<String, Void, String> {

		private String task;
		private String data;

		public HttpAsyncTask(String task, String data) {
			this.task = task;
			this.data = data;
		}

		protected String doInBackground(String... urls) {
			if(!task.equals("getShipment")&& !task.equals("getEmails")){
			return POST(urls[0], data);
			}else{
				if(task.equals("getShipment")){
					return GETShipment(urls[0], data);
				}else{
					 
						return GETEmails(urls[0], data);
					 
				}
			}
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {

			// based on task, return a specific intent

			Intent intent = null;

			if (!result.equals("")) {

				if (task.equals("updateEmail")) {

					intent = new Intent(LoadingScreen.this, Success.class);
					intent.putExtra("task", task);
					intent.putExtra("hasErrors", hasErrors);
					intent.putExtra("result", result);
					intent.putExtra("data", data); 
					intent.putExtra("previousPage", previousPage);
					intent.setType("text/plain");

					startActivity(intent);
					finish();
				}
				
				if (task.equals("getEmails")) {
					if(previousPage.equals("admin")){
						intent = new Intent(LoadingScreen.this, UpdateEmail.class);
						intent.putExtra("task", task);
						intent.putExtra("hasErrors", hasErrors);
						intent.putExtra("result", result);
						intent.putExtra("data", data); 
						intent.putExtra("previousPage", previousPage);
						intent.setType("text/plain");
	
						startActivity(intent);
						finish();
					}
					if(previousPage.equals("supervisor")){
						intent = new Intent(LoadingScreen.this, SendReport.class);
						intent.putExtra("task", task);
						intent.putExtra("hasErrors", hasErrors);
						intent.putExtra("result", result);
						intent.putExtra("data", data); 
						intent.putExtra("previousPage", previousPage);
						intent.setType("text/plain");
	
						startActivity(intent);
						finish();
					}
					if(previousPage.equals("rxcadmin")){
						intent = new Intent(LoadingScreen.this, UpdateRxCEmail.class);
						intent.putExtra("task", task);
						intent.putExtra("hasErrors", hasErrors);
						intent.putExtra("result", result);
						intent.putExtra("data", data); 
						intent.putExtra("previousPage", previousPage);
						intent.setType("text/plain");
	
						startActivity(intent);
						finish();
					}
					if(previousPage.equals("RxCMain")){
						intent = new Intent(LoadingScreen.this, SendRxCReport.class);
						intent.putExtra("task", task);
						intent.putExtra("hasErrors", hasErrors);
						intent.putExtra("result", result);
						intent.putExtra("data", data); 
						intent.putExtra("previousPage", previousPage);
						intent.setType("text/plain");
	
						startActivity(intent);
						finish();
					}
				}

				if (task.equals("sendReport")) {
					intent = new Intent(LoadingScreen.this, Success.class);
					intent.putExtra("task", task);
					intent.putExtra("hasErrors", hasErrors);
					intent.putExtra("result", result);
					intent.putExtra("confirmationCode", confirmationCode);
					
					intent.putExtra("data", data);
					intent.setType("text/plain");

					startActivity(intent);
					finish();
				}
				
				if (task.equals("sendRxCReport")) {
					intent = new Intent(LoadingScreen.this, Success.class);
					intent.putExtra("task", task);
					intent.putExtra("hasErrors", hasErrors);
					intent.putExtra("result", result);
					intent.putExtra("confirmationCode", confirmationCode);
					
					intent.putExtra("data", data);
					intent.setType("text/plain");

					startActivity(intent);
					finish();
				}


				if (task.equals("assignDestination")) {
					intent = new Intent(LoadingScreen.this, Success.class);
					intent.putExtra("task", task);
					intent.putExtra("hasErrors", hasErrors);
					intent.putExtra("result", result);
					intent.putExtra("confirmationCode", confirmationCode);
					intent.putExtra("data", data);
					intent.setType("text/plain");

					startActivity(intent);
					finish();
				}

				if (task.equals("getShipment")) {
					intent = new Intent(LoadingScreen.this,
							UpdateShipment.class);
					intent.putExtra("task", task);
					intent.putExtra("hasErrors", hasErrors);
					intent.putExtra("result", result);
					intent.putExtra("tntLabel", tntLabel);
					intent.putExtra("trackingNumber", trackingNumber);
					intent.putExtra("destination", destination);
					intent.putExtra("note", note);
					intent.putExtra("data", data);
					intent.setType("text/plain");

					startActivity(intent);
					finish();
				}

				if (task.equals("updateShipment")) {
					intent = new Intent(LoadingScreen.this, Success.class);
					intent.putExtra("task", task);
					intent.putExtra("hasErrors", hasErrors);
					intent.putExtra("result", result);
					intent.putExtra("confirmationCode", confirmationCode);
					intent.putExtra("data", data);
					intent.setType("text/plain");

					startActivity(intent);
					finish();
				}
				
				if (task.equals("deaggregateShipper")) {
					intent = new Intent(LoadingScreen.this, Success.class);
					intent.putExtra("task", task);
					intent.putExtra("hasErrors", hasErrors);
					intent.putExtra("result", result);
					intent.putExtra("confirmationCode", confirmationCode);
					intent.putExtra("data", data);
					intent.setType("text/plain");

					startActivity(intent);
					finish();
				}

				// intent.setAction(Intent.ACTION_SEND);

			} else {
				noResponseError();
			}
		}
	}

	/*
	 * Handle post request provided with url and the task plus data to send a
	 * JSON object and return a reply
	 * 
	 * @param url - url to perform task
	 * 
	 * @param task - the task being performed
	 * 
	 * @param data - the data being sent
	 * 
	 * @return
	 */
	private String POST(String url, String data) {
		InputStream inputStream = null;
		String result = "";
		try {

			// 1. create HttpClient
			HttpClient httpclient = new DefaultHttpClient();

			// 2. make POST request to the given URL
			// HttpPost httpPost = new HttpPost(url);

			HttpPut httpput = new HttpPut(url);
			System.err.println(data);
			// 5. set json to StringEntity
			StringEntity se = new StringEntity(data);
		
			// 6. set httpPost Entity
			httpput.setEntity(se);

			// 7. Set some headers to inform server about the type of the
			// content
			httpput.setHeader("appKey", appKey);
			//httpput.setHeader("appKey", appKey);
			
			httpput.setHeader("Content-Type","application/json");

			// 8. Execute POST request to the given URL
			HttpResponse httpResponse = httpclient.execute(httpput);

			// 9. receive response as inputStream
			inputStream = httpResponse.getEntity().getContent();

			// 10. convert inputstream to string
			if (inputStream != null) {
				result = convertInputStreamToString(inputStream);

			} else {
				System.err.println("ERROR IN RESPONSE");
				result = (String) getText(R.string.noresponse);
			}
		} catch (Exception e) {
			Log.d("InputStream", e.getLocalizedMessage());
		}

		// 11. return result

		return result;
	}
	
	private String GETShipment(String url, String data) {
		InputStream inputStream = null;
		String result = "";
		try {

			// 1. create HttpClient
			HttpClient httpclient = new DefaultHttpClient();

			// 2. make POST request to the given URL
			// HttpPost httpPost = new HttpPost(url); 
			String getURL = url+"?tntCode="+data;
			System.err.println("URL: " + getURL);
			HttpGet httpget = new HttpGet(getURL);
			 
			httpget.setHeader("appKey", "c3uF%#+t=&JNKKgc#FFg");
			//httpget.setHeader("appKey", "P@oA^~$~z83-jb@LD5fL");
		 
			// 8. Execute POST request to the given URL
			HttpResponse httpResponse = httpclient.execute(httpget);
		
		 
			
			// 9. receive response as inputStream
			inputStream = httpResponse.getEntity().getContent();

			// 10. convert inputstream to string
			if (inputStream != null) {
				result = convertInputStreamToString(inputStream);
		 ;
			} else {
				
				result = (String) getText(R.string.noresponse);
			}
		} catch (Exception e) {
			Log.d("InputStream", e.getLocalizedMessage());
		}

		// 11. return result

		return result;
	}
	
	private String GETEmails(String url, String data) {
		InputStream inputStream = null;
		String result = "";
		try {

			// 1. create HttpClient
			HttpClient httpclient = new DefaultHttpClient();

			// 2. make POST request to the given URL
			// HttpPost httpPost = new HttpPost(url); 
			 
			System.err.println("URL: " + url);
			HttpGet httpget = new HttpGet(url);
			 
			httpget.setHeader("appKey", "c3uF%#+t=&JNKKgc#FFg");
			//httpget.setHeader("appKey", "P@oA^~$~z83-jb@LD5fL");
		 
			// 8. Execute POST request to the given URL
			HttpResponse httpResponse = httpclient.execute(httpget);
		
		 
			
			// 9. receive response as inputStream
			inputStream = httpResponse.getEntity().getContent();

			// 10. convert inputstream to string
			if (inputStream != null) {
				result = convertInputStreamToString(inputStream);
	 
			} else {
				
				result = (String) getText(R.string.noresponse);
			}
		} catch (Exception e) {
			Log.d("InputStream", e.getLocalizedMessage());
		}

		// 11. return result

		return result;
	}


	/*
	 * Converts the input stream to a string
	 */
	private String convertInputStreamToString(InputStream inputStream)
			throws IOException, JSONException {
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));
		String line = "";
		String result = ""; 
		while ((line = bufferedReader.readLine()) != null) 
			result += line;
	 

		inputStream.close();
		System.err.println("IN CONVERT INPUT STREAM " + result);

		return result;

	}

	/*
	 * Provide error for update fail
	 */
	private void noResponseError() {
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
				.setMessage(R.string.noresponse)
				.setCancelable(false)
				.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
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
	
	@Override
	public void onBackPressed() {
 

	}

}