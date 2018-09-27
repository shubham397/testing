package com.marmeto.global;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;

import com.marmeto.sproxil.Login;
import com.marmeto.operator.Aggregate;
import com.marmeto.operator.OperatorMain;
import com.marmeto.sproxiltnt.R;

public class Logout {
	
	 

	/*
	 * Provide  confirmation of exiting page
	 */
	public void logoutRequest(final Context context) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		TextView myMsg = new TextView(context);
		myMsg.setText(context.getText(R.string.attention));
		myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
		myMsg.setTextSize(25);
		myMsg.setTextColor(Color.RED);

		// set title
		alertDialogBuilder.setCustomTitle(myMsg);
		
		// set dialog message
		alertDialogBuilder
				.setMessage(context.getText(R.string.logoutrequestconfirm))
				.setCancelable(false)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();

								Intent intent = new Intent(context,
										Login.class);

								context.startActivity(intent);
								((Activity) context).finish();
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
	 * Set the proper intent return based on the task
	 */
	private Intent setIntent(Context callingContext, String page) {
		
		Intent intent = new Intent(callingContext, Login.class);
		 

		return intent;
	}

}
