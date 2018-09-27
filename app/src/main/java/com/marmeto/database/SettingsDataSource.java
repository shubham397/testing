package com.marmeto.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SettingsDataSource {
	String TAG = "SettingsDataSource";
	// Database fields
	private static SQLiteDatabase database;
	private DatabaseHelper dbHelper;
	private String[] allColumns = { DatabaseHelper.SETTINGSID,
			DatabaseHelper.CASELIMIT, DatabaseHelper.LOCATIONS,
			DatabaseHelper.VERSION, DatabaseHelper.PCIADMINPASS,
			DatabaseHelper.RXCADMINPASS };

	public SettingsDataSource(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public Settings createSettings(String caseLimit, String locations,
			String version, String pciPass, String rxcPass) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.CASELIMIT, caseLimit);
		values.put(DatabaseHelper.LOCATIONS, locations);
		values.put(DatabaseHelper.VERSION, version);
		values.put(DatabaseHelper.PCIADMINPASS, pciPass);
		values.put(DatabaseHelper.RXCADMINPASS, rxcPass);
		long insertId = database.insert(DatabaseHelper.SETTINGS, null, values);
		Cursor cursor = database.query(DatabaseHelper.SETTINGS, allColumns,
				DatabaseHelper.SETTINGSID + " = " + insertId, null, null, null,
				null);
		cursor.moveToFirst();
		Settings newSettings = cursorToUsers(cursor);
		cursor.close();
		return newSettings;
	}

	public void deleteSettngs(Settings settings) {
		long id = settings.getId();
		Log.d(TAG, "Settings deleted with id: " + id);
		database.delete(DatabaseHelper.SETTINGS, DatabaseHelper.SETTINGSID
				+ " = " + id, null);
	}

	public List<Settings> getAllSettings() {
		List<Settings> settingsList = new ArrayList<Settings>();

		Cursor cursor = database.query(DatabaseHelper.SETTINGS, allColumns,
				null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Settings settings = cursorToUsers(cursor);
			settingsList.add(settings);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return settingsList;
	}

	private Settings cursorToUsers(Cursor cursor) {
		Settings settings = new Settings();
		settings.setId(cursor.getLong(0));
		settings.setCaseLimit(cursor.getString(1));
		settings.setLocations(cursor.getString(2));
		settings.setPCIAdminPass(cursor.getString(3));
		settings.setRxCAdminPass(cursor.getString(4));
		return settings;
	}

	public boolean updateLocations(String locations) {
		ContentValues args = new ContentValues();
		args.put(DatabaseHelper.LOCATIONS, locations);
		return database.update(DatabaseHelper.SETTINGS, args,
				DatabaseHelper.SETTINGSID + "='1'", null) > 0;
	}

	public boolean updateVersion(String version) {
		ContentValues args = new ContentValues();
		args.put(DatabaseHelper.VERSION, version);
		return database.update(DatabaseHelper.SETTINGS, args,
				DatabaseHelper.SETTINGSID + "='1'", null) > 0;
	}

	public boolean updateCaseLimit(String caseLimit) {
		ContentValues args = new ContentValues();
		args.put(DatabaseHelper.CASELIMIT, caseLimit);
		return database.update(DatabaseHelper.SETTINGS, args,
				DatabaseHelper.SETTINGSID + "='1'", null) > 0;
	}

	public boolean updatePCIPass(String pciPass) {
		ContentValues args = new ContentValues();
		args.put(DatabaseHelper.PCIADMINPASS, pciPass);
		return database.update(DatabaseHelper.SETTINGS, args,
				DatabaseHelper.SETTINGSID + "='1'", null) > 0;
	}

	public boolean updateRxCPass(String rxcPass) {
		ContentValues args = new ContentValues();
		args.put(DatabaseHelper.RXCADMINPASS, rxcPass);
		return database.update(DatabaseHelper.SETTINGS, args,
				DatabaseHelper.SETTINGSID + "='1'", null) > 0;
	}

	/**
	 * Delete old settings database and rebuild
	 */
	public void deleteSettings() {

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + DatabaseHelper.SETTINGS);
		db.execSQL(DatabaseHelper.SETTINGSDB_CREATE);
	}

	public String getLocationList() {
		String results = "";
		String Query = "SELECT " + DatabaseHelper.LOCATIONS + " FROM "
				+ DatabaseHelper.SETTINGS + " WHERE "
				+ DatabaseHelper.SETTINGSID + " = '1';";
		Cursor cursor = database.rawQuery(Query, null);

		if (cursor.moveToFirst()) {
			do {
				results = cursor.getString(0); // Here you can get data from
												// table and stored in string if
												// it has only one string.
				Log.d(TAG, "IN HERE WITH LOCATIONS " + results);
			} while (cursor.moveToNext());
		}

		return results;
	}

	public String getVersion() {
		String results = "";
		String Query = "SELECT " + DatabaseHelper.VERSION + " FROM "
				+ DatabaseHelper.SETTINGS + " WHERE "
				+ DatabaseHelper.SETTINGSID + " = '1';";
		Cursor cursor = database.rawQuery(Query, null);

		if (cursor.moveToFirst()) {
			do {
				results = cursor.getString(0); // Here you can get data from
												// table and stored in string if
												// it has only one string.
				Log.d(TAG, "IN HERE WITH LOCATIONS " + results);
			} while (cursor.moveToNext());
		}

		return results;
	}
	
	
	
}
