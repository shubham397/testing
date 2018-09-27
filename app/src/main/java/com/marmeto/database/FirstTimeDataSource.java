package com.marmeto.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class FirstTimeDataSource {
	// Database fields
	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;
	private String[] allColumns = { DatabaseHelper.FLAGID, DatabaseHelper.FLAG };

	public FirstTimeDataSource(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public FirstTime createFlag(String flag) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.FLAG, flag);
		long insertId = database.insert(DatabaseHelper.FIRST, null, values);
		Cursor cursor = database.query(DatabaseHelper.FIRST, allColumns,
				DatabaseHelper.FLAGID + " = " + insertId, null, null, null,
				null);
		cursor.moveToFirst();
		FirstTime newFlag = cursorToFlags(cursor);
		cursor.close();
		return newFlag;
	}

	public void deleteFlag(FirstTime flags) {
		long id = flags.getId();
		System.err.println("Flag deleted with id: " + id);
		database.delete(DatabaseHelper.FIRST, DatabaseHelper.FLAGID + " = " + id,
				null);
	}

	public List<FirstTime> getAllFlags() {
		List<FirstTime> flagList = new ArrayList<FirstTime>();

		Cursor cursor = database.query(DatabaseHelper.FIRST, allColumns, null,
				null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			FirstTime flags = cursorToFlags(cursor);
			flagList.add(flags);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return flagList;
	}

	private FirstTime cursorToFlags(Cursor cursor) {
		FirstTime flag = new FirstTime();
		flag.setId(cursor.getLong(0));
		flag.setFlag(cursor.getString(1));
		return flag;
	}
}
