package com.marmeto.rxc;

 
import java.util.List;
 




import org.json.JSONException;
import org.json.JSONObject;

 




import com.marmeto.database.UserDataSource;
import com.marmeto.database.Users;
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
import android.os.Bundle;
import android.os.Handler; 
import android.text.Editable;
import android.text.TextWatcher; 
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener; 
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class UpdateRxCRole extends Activity {

	final Context context = this;

	EditText newPassEdit;
	EditText newPassConfirmEdit;
	TextView title;
	String error = "";
	String phoneData = "";
	String server = "";
	private String previousPage;
	private Spinner userList;

	private UserDataSource userdatasource; 

	// private LocationManager locationManager = null;

	Handler handler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.updaterole);
		userList = (Spinner) findViewById(R.id.userList);

		// open the data sources
		userdatasource = new UserDataSource(this);
		userdatasource.open();
 

		// get a list of the usernames and store in a string array for spinner
		List<Users> values = userdatasource.getAllUsers();
		
		String username = "";
		String[] usernames = new String[1];
	
		if (values != null && !values.isEmpty()) {
	 
			username = values.get(0).getUsername();

			int index = 0;
			for (Users value : values) {
				if(!value.toString().contains("PCI") && !value.toString().contains("Admin")){
					usernames[index] = value.toString();
					index++;
				} 
			}
		}
	
		
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(context,
				R.layout.customspinner, usernames);
		spinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		userList.setAdapter(spinnerAdapter);
		title = (TextView) findViewById(R.id.title);

	 

		// setContentView(R.layout.main);
		Intent intent = getIntent();
		
		if (intent.hasExtra("previousPage")) {
			previousPage = intent.getExtras().getString("previousPage");
		}

		newPassEdit = (EditText) findViewById(R.id.newPasswordEdit);
		newPassConfirmEdit = (EditText) findViewById(R.id.newPasswordConfirmEdit);
		newPassConfirmEdit.addTextChangedListener(new TextWatcher() {
       
			 @Override
		      public void afterTextChanged(Editable arg0) {
		         enableSubmitIfReady();
		      }

		      @Override
		      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		      }

		      @Override
		      public void onTextChanged(CharSequence s, int start, int before, int count) {
		      }
        }); 
		findViewById(R.id.updateRole).setEnabled(false);
		findViewById(R.id.updateRole).setOnClickListener(new handleSubmit());
		findViewById(R.id.logout).setOnClickListener(new handleLogout());

	}
	
	 public void enableSubmitIfReady() {

		 boolean newPassIsReady =newPassEdit.getText().toString().length()>0;
		    boolean newPassConfirmIsReady =newPassConfirmEdit.getText().toString().length()>0;

		    if (newPassIsReady && newPassConfirmIsReady) {
		    	findViewById(R.id.updateRole).setEnabled(true);
		   } else {
			   findViewById(R.id.updateRole).setEnabled(false);
		    }
		  }

	private class handleSubmit implements OnClickListener {
		public void onClick(View v) {

			String username = "";
			String newPassword = "";
			String newPasswordConfirm = "";

			Spinner spinner = (Spinner) findViewById(R.id.userList);
			username = String.valueOf(spinner.getSelectedItem());
 

			newPassword = newPassEdit.getText().toString();
			newPasswordConfirm = newPassConfirmEdit.getText().toString();
			if (!newPassword.equals(newPasswordConfirm)) {
				passwordsNoMatchError();
			}else{
				//update password for this user
				updatePasswordConfirm(username, newPassword);
				 
			} 
		
		}
	}
	
 
	
	/*
	 * Provide second confirmation of password change
	 */
	private void updatePasswordConfirm(final String username, final String newPassword) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
	 
		TextView myMsg = new TextView(this);
		myMsg.setText(getText(R.string.attention));
		myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
		myMsg.setTextSize(25);
		myMsg.setTextColor(Color.RED);

		// set title
		alertDialogBuilder.setCustomTitle(myMsg);
		
		String message = username + " " + getText(R.string.passchangeconfirm);
		// set dialog message
		alertDialogBuilder
				.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) { 
								dialog.cancel();
								if(userdatasource.updatePassword(username,newPassword)){
									Intent intent = new Intent(UpdateRxCRole.this, Success.class);
									
									String json = "";

									// 3. build jsonObject
									JSONObject jsonObject = new JSONObject();
									try {
										jsonObject.put("success", getText(R.string.rolesuccess));
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

									// 4. convert JSONObject to JSON to String
									json = jsonObject.toString();
									System.err.println("SENDING JSON STRING: " + json);
									

									intent.putExtra("task", "updatePassword");
									intent.putExtra("previousPage", previousPage);
									intent.putExtra("result", json.toString());
									intent.setType("text/plain");

									startActivity(intent);
									finish();
								}else{
									 cannotUpdateError();
								}
							}
						}).setNegativeButton(R.string.no,
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
	 * Provide error for update fail
	 */
	private void cannotUpdateError() {
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
	 * Provide notification for an invalid password.
	 */
	private void passwordsNoMatchError() {
		ErrorHandling err = new ErrorHandling();
		err.handleError(context);
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		TextView myMsg = new TextView(this);
		myMsg.setText(getText(R.string.newpasses_nonmatch));
		myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
		myMsg.setTextSize(25);
		myMsg.setTextColor(Color.RED);

		// set title
		alertDialogBuilder.setCustomTitle(myMsg);
		 

		// set dialog message
		alertDialogBuilder
				.setMessage(R.string.tryagain)
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
	
	private class handleLogout implements OnClickListener {
		public void onClick(View v) {
			Logout logout = new Logout();
			logout.logoutRequest(context);
			
		}
	}
	
	@Override
	public void onBackPressed() {

	 

	}

}
