package com.marmeto.global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.marmeto.admin.AdminMain;
import com.marmeto.connections.ApplySettings;
import com.marmeto.database.AirbillsDataSource;
import com.marmeto.database.EmailAddressDataSource;
import com.marmeto.database.FirstTimeDataSource;
import com.marmeto.database.Settings;
import com.marmeto.database.SettingsDataSource;
import com.marmeto.database.ShippingCaseDataSource;
import com.marmeto.database.UserDataSource;
import com.marmeto.sproxil.Login;
import com.marmeto.rxc.RxCAdmin;
import com.marmeto.rxc.RxCMain;
import com.marmeto.sproxiltnt.R;
import com.marmeto.supervisor.SupervisorMain;

public class Success extends Activity {

	String TAG = "Success";
	final Context context = this;
	private static String task;
	private static String result;
	private boolean hasErrors;
	private String data;
	private String previousPage;
	private String email;

	protected String caseLimit = "";
	protected String locations = "";
	protected String version = "";
	protected String rxcPass = "";
	protected String pciPass = "";

	private String resultString = "";
	private String error = "An error occured, please try again.";

	private String confirmationCode = "";

	private String oldVersion = "";
	
	private FirstTimeDataSource ftdatasource;
	private UserDataSource userdatasource;
	private ShippingCaseDataSource shippingCaseDS;
	private AirbillsDataSource airbillDS;
	private EmailAddressDataSource emaildatasource;
	private SettingsDataSource settingsDS;
	ListView list;

	ArrayList<String> web = new ArrayList<String>();
	ArrayList<Integer> imageId = new ArrayList<Integer>();

	// private LocationManager locationManager = null;

	Handler handler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.success);

		Intent intent = getIntent();

		if (intent.hasExtra("task")) {
			task = intent.getExtras().getString("task");
		}
		if (intent.hasExtra("oldVersion")) {
			oldVersion = intent.getExtras().getString("oldVersion");
		}

		if (intent.hasExtra("result")) {
			try {
				result = parseResult(intent.getExtras().getString("result"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		Log.d(TAG, "IN HERE WITH WEB ARRAY: " + web.toString());
		CustomList adapter = new CustomList(Success.this, web, imageId);
		list = (ListView) findViewById(R.id.resultList);
		list.setAdapter(adapter);

		if (intent.hasExtra("data")) {
			data = intent.getExtras().getString("data");
		}

		if (intent.hasExtra("email")) {
			email = intent.getExtras().getString("email");
		}

		if (intent.hasExtra("previousPage")) {
			previousPage = intent.getExtras().getString("previousPage");
		}

		Log.d(TAG, "THE RESULT IS: " + null);

		if (result == null || result.equals("")) {
			noResponseError();
		} else {
			// if update email and has no errors, update locally
			if (task.equals("updateEmail") && !hasErrors) {
				// open the data sources
				// emaildatasource = new EmailAddressDataSource(this);
				// emaildatasource.open();
				// // delete all e-mails
				// emaildatasource.deleteEMails();
				// // add new e-mail address
				// emaildatasource.createEmail(email);
				// emaildatasource.close();
			} else {
				if (task.equals("updateEmail") && hasErrors) {
				}
			}

			// UPDATE SETTINGS TASK
			if (task.equals("updateSettings") && !hasErrors) {
				// if no errors, apply updates and return
				if (!hasErrors) {
					resultString = "Update was a success!";
					// open the data sources
					settingsDS = new SettingsDataSource(context);
					settingsDS.open();
					// Delete Old Settings
					settingsDS.deleteSettings();
					settingsDS.createSettings(caseLimit, locations, version,
							pciPass, rxcPass);
					settingsDS.close();
					//set the new version
					((Settings) this.getApplication()).setVersion(version);
					//if old version and new version are different, update again to make sure settings match up
					if(!oldVersion.equals(version)){
						
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
						Log.d(TAG, "JSON: "+json);
						
						Intent intent2 = new Intent(Success.this, ApplySettings.class);

						intent2.setAction(Intent.ACTION_SEND);

						String task = "updateSettings";
						intent2.putExtra("task", task);
						intent2.putExtra("previousPage", "admin");
						intent2.putExtra("data", json);
						intent2.setType("text/plain");

						startActivity(intent2);
						finish();
					}

					// if from login, this is first time update so add users and
					// flag
					if (previousPage.equals("login")) {
						ftdatasource = new FirstTimeDataSource(context);
						ftdatasource.open();
						ftdatasource.createFlag("1");
						// open the data sources
						userdatasource = new UserDataSource(context);
						userdatasource.open();
						// TO DO CREATE ENCRYPTED PASSWORDS
						userdatasource.createUser("PCI Admin", pciPass);
						userdatasource.createUser("PCI Operator", "test");
						userdatasource.createUser("PCI Supervisor", "test");
						userdatasource.createUser("RxC Admin", rxcPass);
						userdatasource.createUser("RxC User", "test");
						ftdatasource.close();
						userdatasource.close();
						intent = new Intent(Success.this, Login.class);

						intent.setType("text/plain");

						startActivity(intent);
						finish();

					} else {
						// open the data sources
						userdatasource = new UserDataSource(context);
						userdatasource.open();
						// GET CURRENT USERNAMES AND THEN DELETE AND REBUILD
						String pciOpPass = userdatasource
								.getCurrentPassword("PCI Operator");
						String pciSupPass = userdatasource
								.getCurrentPassword("PCI Supervisor");
						String rxcUserPass = userdatasource
								.getCurrentPassword("RxC User");

						userdatasource.deleteUsers();
						userdatasource.createUser("PCI Admin", pciPass);
						userdatasource.createUser("PCI Operator", pciOpPass);
						userdatasource.createUser("PCI Supervisor", pciSupPass);
						userdatasource.createUser("RxC Admin", rxcPass);
						userdatasource.createUser("RxC User", rxcUserPass);

						userdatasource.close();

					}

				}
			}

			// UPDATE SENDING REPORT TASK
			if (task.equals("sendReport") && !hasErrors) {
				// open the data sources
				shippingCaseDS = new ShippingCaseDataSource(this);
				shippingCaseDS.open();
				// delete all e-mails
				shippingCaseDS.deleteQueue();
				shippingCaseDS.close();
			}

			else {
				if (task.equals("sendReport") && hasErrors) {
					// Still delete but warn user of errors so change icon
					// open the data sources
					shippingCaseDS = new ShippingCaseDataSource(this);
					shippingCaseDS.open();
					// delete all e-mails
					shippingCaseDS.deleteQueue();
					shippingCaseDS.close();

				}
			}

			// UPDATE SENDING REPORT TASK
			if (task.equals("sendRxCReport") && !hasErrors) {
				// open the data sources
				airbillDS = new AirbillsDataSource(this);
				airbillDS.open();
				// delete the queue
				airbillDS.deleteAirbillQueue();
				airbillDS.close();
			}

			else {
				if (task.equals("sendRxCReport") && hasErrors) {
					// Still delete but warn user of errors so change icon
					// open the data sources
					airbillDS = new AirbillsDataSource(this);
					airbillDS.open();
					// delete all e-mails
					airbillDS.deleteAirbillQueue();
					airbillDS.close();

				}
			}

		}

		if (task.equals("assignDestination") && hasErrors) {
		}

		// set result string

		findViewById(R.id.logout).setOnClickListener(new handleLogout());
	}

	/*
	 * Provide error for update fail
	 */
	private void noResponseError() {

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
				.setMessage(R.string.noresponse)
				.setCancelable(false)
				.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								Intent intent = setIntent();

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

	@Override
	public void onBackPressed() {

	}

	private class handleLogout implements OnClickListener {
		public void onClick(View v) {
			Logout logout = new Logout();
			logout.logoutRequest(context);

		}
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
		Log.d(TAG, "IN PARSE RESULT: " + task);

		JSONObject jObject = new JSONObject(result);
		String message = "";

		if (task.equals("updatePassword")) {
			if (jObject.has("success")) {
				message = jObject.getString("success");

				web.add(message);
				imageId.add(R.drawable.success);
			}
		}

		// Message is either a success or a failure
		if (task.equals("updateEmail")) {

			if (jObject.has("messages")) {
				hasErrors = false;
				JSONObject messagesArray = jObject.getJSONObject("messages");
				message = messagesArray.getString("success");
				JSONArray emailArray = messagesArray.getJSONArray("success");

				web.add(emailArray.get(0).toString());
				imageId.add(R.drawable.success);
				if (emailArray.length() > 1) {
					if (!emailArray.get(1).toString().contains("[]")) {
						web.add(emailArray.get(1).toString());
						imageId.add(R.drawable.success);
					}
				}
				if (emailArray.length() > 2) {
					if (!emailArray.get(2).toString().contains("[]")) {
						web.add(emailArray.get(2).toString());
						imageId.add(R.drawable.success);
					}
				}

			}

			if (jObject.has("errors")) {
				hasErrors = true;
				JSONObject errorArray = jObject.getJSONObject("errors");
				message = errorArray.getString("errors");

				message = message.substring(message.indexOf("\"") + 1);
				message = message.substring(0, message.indexOf("\""));
				web.add(message);
				imageId.add(R.drawable.pinfake);

			}

		}

		// SEND REPORT TASK
		// Message returned can be a combination of successes and errors based
		// on different tnt labesl
		if (task.equals("sendReport")) {

			if (jObject.has("messages")) {
				hasErrors = false;

				JSONObject messagesArray = jObject.getJSONObject("messages");
				Iterator<String> iter = messagesArray.keys();

				while (iter.hasNext()) {
					String messageArray = "";
					message = "";
					String tntCode = iter.next();

					JSONArray resultArray = messagesArray.getJSONArray(tntCode);
					if (resultArray.length() == 2) {
						confirmationCode = resultArray.getString(0);
						messageArray = resultArray.getString(1);
					}
					message = messageArray + "\nConfirmation #: "
							+ confirmationCode + "\n\n";
					web.add(message);
					imageId.add(R.drawable.success);

				}

			}

			if (jObject.has("errors")) {
				hasErrors = true;
				JSONObject errorArray = jObject.getJSONObject("errors");
				Iterator<String> iter = errorArray.keys();

				while (iter.hasNext()) {
					message = "";
					String tntCode = iter.next();
					try {
						Object value = errorArray.get(tntCode);
						value = value.toString().replaceAll("\\[\"", "")
								.replaceAll("\"\\]", "");
						message = message + tntCode + " - " + value + "\n\n";

					} catch (JSONException e) {
						// TO DO WRITE ERROR
					}
					web.add(message);
					imageId.add(R.drawable.pinfake);
				}

			}

		}

		// SEND REPORT TASK
		// Message returned can be a combination of successes and errors based
		// on different tnt labesl
		if (task.equals("sendRxCReport")) {

			if (jObject.has("messages")) {
				hasErrors = false;

				JSONObject messagesArray = jObject.getJSONObject("messages");
				Iterator<String> iter = messagesArray.keys();

				while (iter.hasNext()) {
					String messageArray = "";
					message = "";
					String tntCode = iter.next();

					JSONArray resultArray = messagesArray.getJSONArray(tntCode);
					Log.d(TAG, "RESULT ARRAY MESSAGE: "
							+ resultArray.toString());
					for(int i = 0; i < resultArray.length(); i++){
						message = resultArray.getString(i) + "\n\n";
						web.add(message);
						imageId.add(R.drawable.success);
					}
				
				

				}

			}

			if (jObject.has("errors")) {
				hasErrors = true;
				JSONObject errorArray = jObject.getJSONObject("errors");
				Iterator<String> iter = errorArray.keys();

				while (iter.hasNext()) {
					message = "";
					String tntCode = iter.next();
					try {
						Object value = errorArray.get(tntCode);
						value = value.toString().replaceAll("\\[\"", "")
								.replaceAll("\"\\]", "");
						message = message + tntCode + " - " + value + "\n\n";

					} catch (JSONException e) {
						// TO DO WRITE ERROR
					}
					web.add(message);
					imageId.add(R.drawable.pinfake);
				}

			} 

		}

		if (task.equals("assignDestination") || task.equals("updateShipment")) {

			if (jObject.has("messages")) {
				hasErrors = false;
				JSONObject messagesArray = jObject.getJSONObject("messages");
				message = messagesArray.getString("success");

				message = message.substring(message.indexOf("\"") + 1);
				message = message.substring(0, message.indexOf("\""));
				web.add(message);
				imageId.add(R.drawable.success);

			}

			if (jObject.has("errors")) {
				hasErrors = true;
				JSONObject failure = jObject.getJSONObject("errors");
				message = jObject.getString("errors");

				if (failure.has("failure")) {
					message = failure.getString("failure");
					message = message.substring(message.indexOf("\"") + 1);
					message = message.substring(0, message.indexOf("\""));
					web.add(message);
					imageId.add(R.drawable.pinfake);
				} else {
					Iterator<String> iter = failure.keys();

					while (iter.hasNext()) {
						message = "";
						String tntCode = iter.next();
						try {
							Object value = failure.get(tntCode);
							value = value.toString().replaceAll("\\[\"", "")
									.replaceAll("\"\\]", "");
							message = message + tntCode + " - " + value
									+ "\n\n";

						} catch (JSONException e) {
							// TO DO WRITE ERROR
						}
						web.add(message);
						imageId.add(R.drawable.pinfake);
					}
				}

			}

		}

		if (task.equals("getShipment")) {

			if (jObject.has("messages")) {
				hasErrors = false;
				JSONObject messagesArray = jObject.getJSONObject("messages");
				message = messagesArray.getString("success");

				message = message.substring(message.indexOf("\"") + 1);
				message = message.substring(0, message.indexOf("\""));
				web.add(message);
				imageId.add(R.drawable.success);

			}

			if (jObject.has("errors")) {
				hasErrors = true;
				JSONObject failure = jObject.getJSONObject("errors");
				message = jObject.getString("errors");

				if (failure.has("failure")) {
					message = failure.getString("failure");
					message = message.substring(message.indexOf("\"") + 1);
					message = message.substring(0, message.indexOf("\""));
					web.add(message);
					imageId.add(R.drawable.pinfake);
				} else {
					Iterator<String> iter = failure.keys();

					while (iter.hasNext()) {
						message = "";
						String tntCode = iter.next();
						try {
							Object value = failure.get(tntCode);
							value = value.toString().replaceAll("\\[\"", "")
									.replaceAll("\"\\]", "");
							message = message + tntCode + " - " + value
									+ "\n\n";

						} catch (JSONException e) {
							// TO DO WRITE ERROR
						}
						web.add(message);
						imageId.add(R.drawable.pinfake);
					}
				}

			}

		}

		// UPDATE SETTINGS TASK
		if (task.equals("updateSettings")) {

			if (jObject.has("settings")) {
				hasErrors = false;

				if (jObject.has("settings")) {

					JSONObject settingsObject = jObject
							.getJSONObject("settings");
					// Store strings in alphabetized string separated by commas
					if (settingsObject.has("destinations")) {
						JSONArray destinationArray = settingsObject
								.getJSONArray("destinations");

						locations = destinationArray.toString()
								.replace("[", "").replace("]", "")
								.replaceAll("\"", "").trim();
						String[] sortedDestinations = locations.split(",");
						Arrays.sort(sortedDestinations);
						String destination = "";
						int i = 1;
						for (String location : sortedDestinations) {
							if (i != sortedDestinations.length) {
								destination += location + ",";
							} else {
								destination += location;
							}
							i++;
						}
						locations = destination;

					} else {
						message = "An error occured while updating the settings. Please try again later.";
						web.add(message);
						imageId.add(R.drawable.pinfake);
						hasErrors = true;
					}
					if (settingsObject.has("VERSION")) {
						version = settingsObject.getString("VERSION");
					} else {
						message = "An error occured while updating the settings. Please try again later.";
						web.add(message);
						imageId.add(R.drawable.pinfake);
						hasErrors = true;
					}
					if (settingsObject.has("CARTONS_PER_SHIPCASE")) {
						caseLimit = settingsObject
								.getString("CARTONS_PER_SHIPCASE");
					} else {
						message = "An error occured while updating the settings. Please try again later.";
						web.add(message);
						imageId.add(R.drawable.pinfake);
						hasErrors = true;
					}
					if (settingsObject.has("PCI_ADMIN")) {
						pciPass = settingsObject.getString("PCI_ADMIN");
					} else {
						message = "An error occured while updating the settings. Please try again later.";
						web.add(message);
						imageId.add(R.drawable.pinfake);
						hasErrors = true;
					}
					if (settingsObject.has("RXC_ADMIN")) {
						rxcPass = settingsObject.getString("RXC_ADMIN");
					} else {
						message = "An error occured while updating the settings. Please try again later.";
						web.add(message);
						imageId.add(R.drawable.pinfake);
						hasErrors = true;
					}
				}
				// set variables from JSONObjectcaseLimit, locations, version
				message = "Successfully Updated the Settings";
				web.add(message);
				imageId.add(R.drawable.success);
			}

			if (jObject.has("errors")) {
				hasErrors = true;
				message = "An error occured while updating the settings. Please try again later.";
				web.add(message);
				imageId.add(R.drawable.pinfake);
			}
		}

		return message;
	}

	/*
	 * Set the proper intent return based on the task
	 */
	private Intent setIntent() {
		Intent intent = null;

		if (task == null || task.equals("")) {
			intent = new Intent(Success.this, Login.class);
		}

		if (task.equals("updatePassword")) {

			if (previousPage.equals("admin")) {
				intent = new Intent(Success.this, AdminMain.class);
			}
			if (previousPage.equals("rxcadmin")) {
				intent = new Intent(Success.this, RxCAdmin.class);
			}
		}

		if (task.equals("updateEmail")) {
			if (previousPage.equals("admin")) {
				intent = new Intent(Success.this, AdminMain.class);
			}
			if (previousPage.equals("rxcadmin")) {
				intent = new Intent(Success.this, RxCAdmin.class);
			}

		}

		if (task.equals("sendReport")) {
			intent = new Intent(Success.this, SupervisorMain.class);
		}
		if (task.equals("sendRxCReport")) {
			intent = new Intent(Success.this, RxCMain.class);
		}

		if (task.equals("updateSettings")) {
			if (previousPage.equals("login")) {
				intent = new Intent(Success.this, Login.class);
			}
			if (previousPage.equals("admin")) {
				intent = new Intent(Success.this, AdminMain.class);
			}
			if (previousPage.equals("rxcadmin")) {
				intent = new Intent(Success.this, RxCAdmin.class);
			}
		}
		if (task.equals("assignDestination")) {
			intent = new Intent(Success.this, RxCMain.class);
		}
		if (task.equals("updateShipment")) {
			intent = new Intent(Success.this, RxCMain.class);
		}

		return intent;
	}

}
