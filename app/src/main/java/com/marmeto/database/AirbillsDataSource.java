package com.marmeto.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class AirbillsDataSource {
	// Database fields
	private static SQLiteDatabase database;
	private DatabaseHelper dbHelper;
	private String[] allColumns = { DatabaseHelper.SHIPPINGID,
			DatabaseHelper.TNTLABEL, DatabaseHelper.MPACODES,
			DatabaseHelper.TASK };

	public AirbillsDataSource(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public void insertAirbill(String dhlLabel, String tntLabels,
			String destination, String comments, String action, Long timestamp) {

		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.DHLLABEL, dhlLabel);
		values.put(DatabaseHelper.TNTLABELS, tntLabels);
		values.put(DatabaseHelper.DESTINATION, destination);
		values.put(DatabaseHelper.COMMENTS, comments);
		values.put(DatabaseHelper.TASK, action);
		values.put(DatabaseHelper.TIMESTAMP, timestamp);
		database.insert(DatabaseHelper.AIRBILL, null, values);
		database.close();
	}
 

	public void deleteAirbill(Airbills airbills) {
		long id = airbills.getId();
		System.err.println("AIRBILL deleted with id: " + id);
		database.delete(DatabaseHelper.AIRBILL, DatabaseHelper.AIRBILLID
				+ " = " + id, null);
	}
 
	public static List<String> getSAirbillIDs() {
		List<String> ids = new ArrayList<String>();
		String id = "";
		String Query = "Select " + DatabaseHelper.AIRBILLID + " from "
				+ DatabaseHelper.AIRBILL + ";";
		Cursor cursor = database.rawQuery(Query, null);
		if (cursor.moveToFirst()) {
			do {
				id = cursor.getString(0); // Here you can get data from table
											// and stored in string if it has
											// only one string.
				System.err.println("IN HERE WITH AIRBILL TABLE ID " + id);
				ids.add(id);
			} while (cursor.moveToNext());
		}
		return ids;
	} 

	public boolean checkIfDHLLabelDoesNotExist(String dhlLabel) {
		System.err.println("IN HERE WITH LABEL "+ getAirbillAction("1"));
		String Query = "Select * from " + DatabaseHelper.AIRBILL
				+ " where " + DatabaseHelper.DHLLABEL + " = '" + dhlLabel+"' AND ("+DatabaseHelper.TASK +"='ASSOCIATE' OR "+DatabaseHelper.TASK +"='RE_ASSOCIATE');";
		Cursor cursor = database.rawQuery(Query, null);
		if (cursor.getCount() <= 0) {
			//Does not exist so true
			return true;
		}else{
			//Exists return false
			return false;
		}
		
	}
	
	public boolean checkIfTNTLabelDoesNotExist(String tntLabel) {

		String Query = "Select * from " + DatabaseHelper.AIRBILL
				+ " where " + DatabaseHelper.TNTLABELS + " like '%" + tntLabel+"%' AND ("+DatabaseHelper.TASK +"='ASSOCIATE' OR "+DatabaseHelper.TASK +"='RE_ASSOCIATE');";
		Cursor cursor = database.rawQuery(Query, null);
		System.err.println("THERE IS A COUNT OF: " + cursor.getCount());
		if (cursor.getCount() <= 0) {
			return true;
		}else{
			return false;
		}
	}

//	/**
//	 * Determine if the provided tnt label is already in an airbill
//	 * 
//	 * @param mpaCode
//	 * @return true if not in the case
//	 */
//	public boolean checkIfTNTLabelIsStored(String tntLabel) {
//	  
//		String results = "";
//		boolean success = true;
//		System.err.println("TESTING TNT Label " + tntLabel);
//		String Query = "SELECT " + DatabaseHelper.TNTLABELS + " FROM "
//				+ DatabaseHelper.AIRBILL + " " + "WHERE "
//				+ DatabaseHelper.TASK + "='ASSOCIATE';";
//		Cursor cursor = database.rawQuery(Query, null);
//
//		if (cursor.moveToFirst()) {
//			do {
//				results = cursor.getString(0); // Here you can get data from
//												// table and stored in string if
//												// it has only one string.
//				System.err.println("IN HERE WITH TNT LABELS " + results);
//				if (results.contains(tntLabel)) {
//					success = false;
//
//				}
//			} while (cursor.moveToNext());
//		}
//		return success;
//
//	}
 

	/**
	 * Determine if the dhl label is already used
	 * 
	 * @param //mpaCode
	 * @return true if not in the case
	 */
	public boolean checkIfDHLLabelStored(String dhlLabel) {
		String results = "";
		boolean success = true;
		String Query = "SELECT " + DatabaseHelper.DHLLABEL + " FROM "
				+ DatabaseHelper.AIRBILL + ";";
		Cursor cursor = database.rawQuery(Query, null);

		if (cursor.moveToFirst()) {
			do {
				results = cursor.getString(0); // Here you can get data from
												// table and stored in string if
												// it has only one string.
				System.err.println("IN HERE WITH DHL LABEL " + results);
				System.err
						.println("ATTEMPTING TO COMPARE TO LABEL " + dhlLabel);
				if (results.equals(dhlLabel)) {
					success = false;
				}
			} while (cursor.moveToNext());
		}
		return success;
	}
 

	public String getTNTLabels(String id) {
		String results = "";
		String Query = "SELECT " + DatabaseHelper.TNTLABELS + " FROM "
				+ DatabaseHelper.AIRBILL + " WHERE "
				+ DatabaseHelper.AIRBILLID + " = '" + id + "';";
		Cursor cursor = database.rawQuery(Query, null);

		if (cursor.moveToFirst()) {
			do {
				results = cursor.getString(0); // Here you can get data from
												// table and stored in string if
												// it has only one string.
				System.err.println("IN HERE WITH TNT LABELS " + results);
			} while (cursor.moveToNext());
		}

		return results;
	}

	public String getDHLLabel(String id) {
		String results = "";
		String Query = "SELECT " + DatabaseHelper.DHLLABEL + " FROM "
				+ DatabaseHelper.AIRBILL + " WHERE "
				+ DatabaseHelper.AIRBILLID + " = '" + id + "';";
		Cursor cursor = database.rawQuery(Query, null);

		if (cursor.moveToFirst()) {
			do {
				results = cursor.getString(0); // Here you can get data from
												// table and stored in string if
												// it has only one string.

			} while (cursor.moveToNext());
		}

		return results;
	}

	public String getAirbillTimestamp(String id) {
		String results = "";
		String Query = "SELECT " + DatabaseHelper.TIMESTAMP + " FROM "
				+ DatabaseHelper.AIRBILL + " WHERE "
				+ DatabaseHelper.AIRBILLID + " = '" + id + "';";
		Cursor cursor = database.rawQuery(Query, null);

		if (cursor.moveToFirst()) {
			do {
				results = cursor.getString(0); // Here you can get data from
												// table and stored in string if
												// it has only one string.

			} while (cursor.moveToNext());
		}

		return results;
	}

	public String getAirbillAction(String id) {
		String results = "";
		String Query = "SELECT " + DatabaseHelper.TASK + " FROM "
				+ DatabaseHelper.AIRBILL + " WHERE "
				+ DatabaseHelper.AIRBILLID + " = '" + id + "';";
		Cursor cursor = database.rawQuery(Query, null);

		if (cursor.moveToFirst()) {
			do {
				results = cursor.getString(0); // Here you can get data from
												// table and stored in string if
												// it has only one string.

			} while (cursor.moveToNext());
		}

		return results;
	}
	
	public String getDestination(String id) {
		String results = "";
		String Query = "SELECT " + DatabaseHelper.DESTINATION + " FROM "
				+ DatabaseHelper.AIRBILL + " WHERE "
				+ DatabaseHelper.AIRBILLID + " = '" + id + "';";
		Cursor cursor = database.rawQuery(Query, null);

		if (cursor.moveToFirst()) {
			do {
				results = cursor.getString(0); // Here you can get data from
												// table and stored in string if
												// it has only one string.

			} while (cursor.moveToNext());
		}

		return results;
	}
	
	public String getComments(String id) {
		String results = "";
		String Query = "SELECT " + DatabaseHelper.COMMENTS + " FROM "
				+ DatabaseHelper.AIRBILL + " WHERE "
				+ DatabaseHelper.AIRBILLID + " = '" + id + "';";
		Cursor cursor = database.rawQuery(Query, null);

		if (cursor.moveToFirst()) {
			do {
				results = cursor.getString(0); // Here you can get data from
												// table and stored in string if
												// it has only one string.

			} while (cursor.moveToNext());
		}

		return results;
	}
	
	/**
	 * Check if the database exist, if it doesn't create it.
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	public boolean checkAirbillDataBase() {
		System.err.println("CHECKING IF AIRBILL DB EXISTS");
	    SQLiteDatabase checkDB = null;
	    try {
	        checkDB = SQLiteDatabase.openDatabase(DatabaseHelper.AIRBILL, null,
	                SQLiteDatabase.OPEN_READONLY);
	        checkDB.close();
	    } catch (SQLiteException e) {
	        // database doesn't exist yet so created it
	    	System.err.println("AIRBILL DOESN'T EXIST, CREATE EMPTY SHELL");
	    	SQLiteDatabase db = dbHelper.getWritableDatabase();
	    	db.execSQL(DatabaseHelper.AIRBILL_CREATE);
	    }
	    return checkDB != null;
	}
	

	/**
	 * Delete the queue and re-create an empty one
	 */
	public void deleteAirbillQueue() {

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + DatabaseHelper.AIRBILL);
		db.execSQL(DatabaseHelper.AIRBILL_CREATE);
	}

}
