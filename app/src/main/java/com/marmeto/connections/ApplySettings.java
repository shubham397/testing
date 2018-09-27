package com.marmeto.connections;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.marmeto.database.Settings;
import com.marmeto.global.Success;
import com.marmeto.sproxiltnt.R;

public class ApplySettings extends Activity {

	final Context context = this;
	// Introduce an delay
	private final int WAIT_TIME = 5500;
	private String response = "";

	String TAG = "ApplySettings";

	private String task = "";
	private String data = "";
	private String previousPage = "";
	private String oldVersion = "";

	private static LinkedList<String> results = new LinkedList<String>();

	private static int error = 0;
	private static boolean hasErrors = true;
	
	private String PROD_KEY = "P@oA^~$~z83-jb@LD5fL";
	private String UAT_KEY = "c3uF%#+t=&JNKKgc#FFg";
	//private String appKey = PROD_KEY;
	private String appKey = UAT_KEY;
 
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.loadingscreen);

		findViewById(R.id.mainSpinner1).setVisibility(View.VISIBLE);

		Intent intent = getIntent();

		if (intent.hasExtra("data")) {
			data = intent.getExtras().getString("data");
		}
		if (intent.hasExtra("task")) {
			task = intent.getExtras().getString("task");
		}
		if (intent.hasExtra("previousPage")) {
			previousPage = intent.getExtras().getString("previousPage");
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
		Log.d(TAG, "THE TASK: " + task);
		URLs url = new URLs(task);
		oldVersion = ((Settings) getApplication()).getVersion();
		String address = url.getURL(oldVersion);
		Log.d(TAG, "CONNECTING TO: " + address);
		Log.d(TAG, "CONNECTING WITH: " + appKey);

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

			return POST(urls[0], data);
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
		 
			// based on task, return a specific intent

			Intent intent = new Intent(ApplySettings.this, Success.class);

			
			if (task.equals("updateSettings")) {
 
			 
				intent.putExtra("task", task);
				intent.putExtra("hasErrors", hasErrors);
				intent.putExtra("previousPage", previousPage);
				intent.putExtra("result", result);
				intent.putExtra("oldVersion", oldVersion);
				intent.putExtra("data", data);
				if (hasErrors) {
					intent.putExtra("settingsApplied", false);
				} else {
					intent.putExtra("settingsApplied", true);
				}
				intent.setType("text/plain");

				startActivity(intent);
				finish();
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
			HttpPut HttpPut = new HttpPut(url);

			// 5. set json to StringEntity
			StringEntity se = new StringEntity(data);

			// 6. set httpPost Entity
			HttpPut.setEntity(se);

			// 7. Set some headers to inform server about the type of the
			// content
			
			HttpPut.setHeader("appKey", appKey);
			//HttpPut.setHeader("appKey", "c3uF%#+t=&JNKKgc#FFg"); 
			
			// 8. Execute PUT request to the given URL
			HttpResponse httpResponse = httpclient.execute(HttpPut);
	 
			// 9. receive response as inputStream
			inputStream = httpResponse.getEntity().getContent();
		 

			// 10. convert inputstream to string
			if (inputStream != null) {
				result = convertInputStreamToString(inputStream);

			} else {
				result = (String) getText(R.string.noresponse);
			}
		} catch (Exception e) {
			Log.d(TAG, e.getLocalizedMessage());
		}

		// 11. return result
		Log.d(TAG, "Result Is: "+result);
 
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
	 
	 
			return result;
	 

	}

	@Override
	public void onBackPressed() {
 

	}

}