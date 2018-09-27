package com.marmeto.operator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKManager.FEATURE_TYPE;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.BarcodeManager.DeviceIdentifier;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.ScanDataCollection.LabelType;
import com.symbol.emdk.barcode.ScanDataCollection.ScanData;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.Scanner.DataListener;
import com.symbol.emdk.barcode.Scanner.StatusListener;
import com.symbol.emdk.barcode.Scanner.TriggerType;
import com.symbol.emdk.barcode.ScannerConfig;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.StatusData;
import com.symbol.emdk.barcode.StatusData.ScannerStates;
import com.marmeto.admin.AdminMain;
import com.marmeto.database.FirstTimeDataSource;
import com.marmeto.database.Settings;
import com.marmeto.database.SettingsDataSource;
import com.marmeto.database.ShippingCaseDataSource;
import com.marmeto.database.UserDataSource;
import com.marmeto.database.Users;
import com.marmeto.global.AddLabel;
import com.marmeto.global.ErrorHandling;
import com.marmeto.global.Logout;
import com.marmeto.global.ShippingCaseAction;
import com.marmeto.global.Success;
import com.marmeto.sproxil.Login;
import com.marmeto.rxc.RxCAdmin;
import com.marmeto.rxc.RxCMain;
import com.marmeto.sproxiltnt.R;
import com.marmeto.supervisor.SupervisorMain;

public class Aggregate extends Activity implements EMDKListener,
		StatusListener, DataListener {
	// Declare a variable to store EMDKManager object
	final Context context = this;
	private EMDKManager emdkManager = null;
	private EMDKResults results = null;

	private ShippingCaseDataSource shippingCaseDS;

	// Declare a variable to store Barcode Manager object
	private BarcodeManager barcodeManager = null;

	// Declare a variable to hold scanner device to scan
	private Scanner scanner = null;
	int successfulScans = 0;
	private String task = "";

	private SettingsDataSource settingsDS;
	private int caseLimit = 0;

	// Boolean to explain whether the scanning is in progress or not at any
	// specific point of time
	boolean isScanning = false;

	private TextView helpTextView = null;
	private TextView incrementView = null;

	private LinkedList<String> mpaCodes = new LinkedList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aggregate);

		helpTextView = (TextView) findViewById(R.id.help);
		incrementView = (TextView) findViewById(R.id.result);
		findViewById(R.id.trackandtraceButton).setEnabled(false);

		// The EMDKManager object will be created and returned in the callback.
		results = EMDKManager.getEMDKManager(context, this);
		// Check the return status of getEMDKManager and update the status Text
		// View accordingly
		if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
			// statusTextView.setText("EMDKManager Request Failed");
			// SEND ERROR

		}

		// open the data sources
		settingsDS = new SettingsDataSource(this);
		settingsDS.open();

		List<Settings> values = settingsDS.getAllSettings();
		if (values != null && !values.isEmpty()) {

			caseLimit = Integer.parseInt(values.get(0).getCaseLimit());
		}

		findViewById(R.id.undo).setEnabled(false);
		findViewById(R.id.undoall).setEnabled(false);
		findViewById(R.id.logout).setOnClickListener(new handleLogout());
	 
		findViewById(R.id.undo).setOnClickListener(new handleUndo());
		findViewById(R.id.undoall).setOnClickListener(new handleUndoAll());

		findViewById(R.id.trackandtraceButton).setOnClickListener(
				new handleAddTandTLabel());
	}

	private class handleUndo implements OnClickListener {
		public void onClick(View v) {

			mpaCodes.removeLast();
			successfulScans--;
			incrementView.setText(Integer.toString(successfulScans));
			if (successfulScans < caseLimit) {
				findViewById(R.id.trackandtraceButton).setEnabled(false);
			}
			if (successfulScans == 0) {
				findViewById(R.id.undo).setEnabled(false);
				findViewById(R.id.undoall).setEnabled(false);
			}
			System.err.println(mpaCodes.toString());
		}
	}
	
	private class handleUndoAll implements OnClickListener {
		public void onClick(View v) {

			mpaCodes.removeAll(mpaCodes);
			successfulScans = 0;
			incrementView.setText(Integer.toString(successfulScans));
			if (successfulScans < caseLimit) {
				findViewById(R.id.trackandtraceButton).setEnabled(false);
			}
			if (successfulScans == 0) {
				findViewById(R.id.undo).setEnabled(false);
				findViewById(R.id.undoall).setEnabled(false);
			} 
		}
	}

	private class handleAddTandTLabel implements OnClickListener {
		public void onClick(View v) {
			emdkManager.release();

			Intent intent = new Intent(Aggregate.this, AddLabel.class);

			ArrayList<String> myList = new ArrayList<String>(mpaCodes);
			intent.putExtra("action", ShippingCaseAction.AGGREGATE.toString());
			intent.putExtra("previousPage", "aggregate");
			intent.putStringArrayListExtra("mpaCodes", myList);
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
	protected void onDestroy() {
		super.onDestroy();
		if (emdkManager != null) {

			// Clean up the objects created by EMDK manager
			emdkManager.release();
			emdkManager = null;
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

		if (scanner != null) {
			// releases the scanner hardware resources for other application
			// to use. You must call this as soon as you're done with the
			// scanning.
			// emdkManager.release();
			// scanner.disable();
			scanner = null;
		}
	}

	@Override
	public void onClosed() {
		// TODO Auto-generated method stub
		// The EMDK closed abruptly. // Clean up the objects created by EMDK
		// manager
		if (this.emdkManager != null) {

			this.emdkManager.release();
			this.emdkManager = null;
		}
	}

	@Override
	public void onOpened(EMDKManager emdkManager) {
		// TODO Auto-generated method stub
		this.emdkManager = emdkManager;
		// Method call to set some decoder parameters to scanner
		setScannerParameters();

		// Toast to indicate that the user can now start scanning
		Toast.makeText(Aggregate.this,
				"Press Hard Scan Button to start scanning...",
				Toast.LENGTH_SHORT).show();

	}
	
	@Override
    protected void onResume() { 
	        super.onResume();
	        System.out.println("onResume() called because screen turned on.");
	     	 
    }
	
	@Override
    protected void onPause() {
	
        super.onPause();
        System.out.println("onPause() called because screen turned off.");
        
    }

	@Override
	public void onData(ScanDataCollection scanDataCollection) {
		// TODO Auto-generated method stub
		// Use the scanned data, process it on background thread using AsyncTask
		// and update the UI thread with the scanned results
		new AsyncDataUpdate().execute(scanDataCollection);

	}

	// AsyncTask that configures the scanned data on background
	// thread and updated the result on UI thread with scanned data and type of
	// label
	private class AsyncDataUpdate extends
			AsyncTask<ScanDataCollection, Void, String> {

		@Override
		protected String doInBackground(ScanDataCollection... params) {
			ScanDataCollection scanDataCollection = params[0];

			// Status string that contains both barcode data and type of barcode
			// that is being scanned
			String barcodeData = "";
			// The ScanDataCollection object gives scanning result and the
			// collection of ScanData. So check the data and its status
			if (scanDataCollection != null
					&& scanDataCollection.getResult() == ScannerResults.SUCCESS) {

				ArrayList<ScanData> scanData = scanDataCollection.getScanData();

				// Iterate through scanned data and prepare the statusStr
				for (ScanData data : scanData) {
					// Get the scanned data
					barcodeData = data.getData();
					// Get the type of label being scanned
					LabelType labelType = data.getLabelType();
					// Concatenate barcode data and label type
				}
			}

			// Return result to populate on UI thread
			return barcodeData;
		}

		@Override
		protected void onPostExecute(String result) {
			// result is MPA CODE
			// validate code

			shippingCaseDS = new ShippingCaseDataSource(context);
			shippingCaseDS.open();

			helpTextView.setText("");
			// if carton doesn't exist in other case

			if (validateMPACode(result)) {
				System.err.println("ABOUT TO CHECK MPA CODE: " + result);
				if (shippingCaseDS.checkIfMPACodeIsStored(result)) {
					if (mpaCodes.contains(result)) {
						System.err.println("IN HERE WITH MPA CODES: " + mpaCodes.toString());
						// THROW ERROR THAT CODE SCANNED TWICE
						scannedTwice();
					} else {
						// if valid add and increment number
						if (mpaCodes.size() < caseLimit) {
							System.err.println("ADDING MPA CODE: " + result);
							mpaCodes.add(result);
							successfulScans++;
							incrementView.setText(Integer
									.toString(successfulScans));
							findViewById(R.id.undo).setEnabled(true);
							findViewById(R.id.undoall).setEnabled(true);
							if (successfulScans == caseLimit) {
								findViewById(R.id.trackandtraceButton)
										.setEnabled(true);
							}
						} else {
							maxmimumReached();
						}
					}
				} else {
					cartonExists();
				}

			} else {
				// not a valid QR code, throw error
				invalidCarton();
			}

		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}

	private boolean validateMPACode(String code) {
		boolean result = true;
		// Make sure length is 30 characters
 
		if (containsOnlyNumbers(code) && code.length() == 13) {

			result = true;

		}else{
			result = false;
		}
		return result;
	}
	
	public static boolean containsOnlyNumbers(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isDigit(str.charAt(i)))
				return false;
		}
		return true;
	}

	 

	/*
	 * Notify in case of scanner errors
	 */
	private void scannedTwice() {
		
		ErrorHandling err = new ErrorHandling();
		err.handleError(context);
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
		System.err.println("IN ERROR MESSAGE WITH MPA CODES: " + mpaCodes.toString());
		TextView myMsg = new TextView(this);
		myMsg.setText(getText(R.string.attention));
		myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
		myMsg.setTextSize(25);
		myMsg.setTextColor(Color.RED);

		// set title
		alertDialogBuilder.setCustomTitle(myMsg);
		
		// set dialog message
		alertDialogBuilder
				.setMessage(R.string.scannedtwice)
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
	 * QR Code not recognized error
	 */
	private void invalidCarton() {
		
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
				.setMessage(R.string.invalidcartonqr)
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
	 * Carton exists in other shipping case
	 */
	private void cartonExists() {
		
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
				.setMessage(R.string.cartonexists)
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
	public void onStatus(StatusData statusData) {
		// TODO Auto-generated method stub
		// process the scan status event on the background thread using
		// AsyncTask and update the UI thread with current scanner state
		new AsyncStatusUpdate().execute(statusData);

	}

	// AsyncTask that configures the current state of scanner on background
	// thread and updates the result on UI thread
	private class AsyncStatusUpdate extends AsyncTask<StatusData, Void, String> {

		@Override
		protected String doInBackground(StatusData... params) {
			// Get the current state of scanner in background
			StatusData statusData = params[0];
			String statusStr = "";
			ScannerStates state = statusData.getState();
			// Different states of Scanner
			switch (state) {
			// Scanner is IDLE
			case IDLE:
				statusStr = "The scanner enabled and its idle";
				isScanning = false;
				break;
			// Scanner is SCANNING
			case SCANNING:
				statusStr = "Scanning..";
				isScanning = true;
				break;
			// Scanner is waiting for trigger press
			case WAITING:
				statusStr = "Waiting for trigger press..";
				break;
			default:
				break;
			}
			// Return result to populate on UI thread
			return statusStr;
		}

		@Override
		protected void onPostExecute(String result) {

		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}

	// This is a callback method when user presses any hardware button on the
	// device
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		// check for scanner hard key press.
		if ((keyCode == KeyEvent.KEYCODE_BUTTON_L1)
				|| (keyCode == KeyEvent.KEYCODE_BUTTON_R1)) {

			// Skip the key press if the repeat count is not zero.
			if (event.getRepeatCount() != 0) {
				return true;
			}

			try {
				if (scanner == null) {
					initializeScanner();
				}

				if ((scanner != null) && (isScanning == false)) {
					// Starts an asynchronous Scan. The method will not turn on
					// the scanner. It will, however, put the scanner in a state
					// in which the scanner can be turned ON either by pressing
					// a hardware trigger or can be turned ON automatically.
					scanner.read();
				}

			} catch (Exception e) {
				// Display if there is any exception while performing operation
				scannerError();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/*
	 * Notify in case of scanner errors
	 */
	private void scannerError() {
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
				.setMessage(R.string.scannererror)
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

	// This is a callback method when user releases any hardware button on the
	// device
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		// check for scanner trigger key press.
		if ((keyCode == KeyEvent.KEYCODE_BUTTON_L1)
				|| (keyCode == KeyEvent.KEYCODE_BUTTON_R1)) {

			// Skip the key press if the repeat count is not zero.
			if (event.getRepeatCount() != 0) {
				return true;
			}

			try {
				if ((scanner != null) && (isScanning == true)) {
					// This Cancels any pending asynchronous read() calls
					scanner.cancelRead();
				}
			} catch (Exception e) {
				// DISPLAY ERROR
				scannerError();
			}
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	// Method to set some decoder parameters in the ScannerConfig object
	public void setScannerParameters() {
		try {

			if (scanner == null) {
				// Method call to initialize the scanner parameters
				initializeScanner();
			}

			ScannerConfig config = scanner.getConfig();
			// Set the code128
			config.decoderParams.qrCode.enabled = true;
			scanner.setConfig(config);

		} catch (Exception e) {
			// DISPLAY ERROR
			// statusTextView.setText(e.getMessage());
		}
	}

	// Method to initialize and enable Scanner and its listeners
	private void initializeScanner() throws ScannerException {

		if (scanner == null) {

			// Get the Barcode Manager object
			barcodeManager = (BarcodeManager) this.emdkManager
					.getInstance(FEATURE_TYPE.BARCODE);

			// Get default scanner defined on the device
			scanner = barcodeManager.getDevice(DeviceIdentifier.DEFAULT);
			// scanner = barcodeManager.getDevice(list.get(0));

			// Add data and status listeners
			scanner.addDataListener(this);
			scanner.addStatusListener(this);

			// The trigger type is set to HARD by default and HARD is not
			// implemented in this release.
			// So set to SOFT_ALWAYS
			scanner.triggerType = TriggerType.SOFT_ALWAYS;

			// Enable the scanner
			scanner.enable();
		}

	}

	@Override
	public void onBackPressed() {
		 
	}

	/*
	 * Notify in case of scanner errors
	 */
	private void maxmimumReached() {
		
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
				.setMessage(R.string.maxcases)
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

								Intent intent = new Intent(Aggregate.this,
										OperatorMain.class);

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
	
	

}
