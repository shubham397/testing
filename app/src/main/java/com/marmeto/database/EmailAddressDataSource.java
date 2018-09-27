package com.marmeto.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class EmailAddressDataSource {
	// Database fields
	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;
	private String[] allColumns = { DatabaseHelper.EMAILID, DatabaseHelper.EMAILADDRESS };

	public EmailAddressDataSource(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public EmailAddress createEmail(String address) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.EMAILADDRESS, address);
		long insertId = database.insert(DatabaseHelper.EMAIL, null, values);
		Cursor cursor = database.query(DatabaseHelper.EMAIL, allColumns,
				DatabaseHelper.EMAILID + " = " + insertId, null, null, null,
				null);
		cursor.moveToFirst();
		EmailAddress newEmail = cursorToFlags(cursor);
		cursor.close();
		return newEmail;
	}

	public void deleteEmail(EmailAddress emails) {
		long id = emails.getId();
		System.err.println("Email deleted with id: " + id);
		database.delete(DatabaseHelper.EMAIL, DatabaseHelper.EMAILID + " = " + id,
				null);
	}

	public List<EmailAddress> getAllEmails() {
		List<EmailAddress> emailList = new ArrayList<EmailAddress>();

		Cursor cursor = database.query(DatabaseHelper.EMAIL, allColumns, null,
				null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			EmailAddress emails = cursorToFlags(cursor);
			emailList.add(emails);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return emailList;
	}

	private EmailAddress cursorToFlags(Cursor cursor) {
		EmailAddress email = new EmailAddress();
		email.setId(cursor.getLong(0));
		email.setFlag(cursor.getString(1));
		return email;
	}
	 
	/**
	 * Delete old email database and rebuild
	 */
	  public void deleteEMails() {
		 
		  
		    SQLiteDatabase db = dbHelper.getWritableDatabase(); 
		    db.execSQL("DROP TABLE IF EXISTS " + DatabaseHelper.EMAIL);
			db.execSQL(DatabaseHelper.EMAILDB_CREATE); 
		}
}
