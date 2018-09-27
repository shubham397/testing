package com.marmeto.database;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	static final String FILE_DIR = "biogen";
	static final String DBNAME = "track_and_trace";

	// First time use flag table
	static final String FIRST = "first_time";
	static final String FLAGID = "flagID";
	static final String FLAG = "flag";

	// Email address table
	static final String EMAIL = "email";
	static final String EMAILID = "emailID";
	static final String EMAILADDRESS = "address";

	// Users table variables
	static final String USERS = "users";
	static final String USERID = "userID";
	static final String USERNAME = "username";
	static final String PASSWORD = "password";

	// Shipping Case table variables
	static final String SHIPPINGCASE = "shipping_cases";
	static final String SHIPPINGID = "shippingID";
	static final String TNTLABEL = "tnt_label";
	static final String MPACODES = "mpa_codes";
	static final String TIMESTAMP = "timestamp";
	static final String TASK = "task";

	// Shipping Case table variables
	static final String AIRBILL = "airbill";
	static final String AIRBILLID = "airbillID";
	static final String DHLLABEL = "dhl_label";
	static final String TNTLABELS = "tnt_labels";
	static final String DESTINATION = "destination";
	static final String COMMENTS = "comment";

	// Settings table
	static final String SETTINGS = "settings";
	static final String SETTINGSID = "settingsID";
	static final String CASELIMIT = "case_limit";
	static final String LOCATIONS = "locations";
	static final String VERSION = "version";
	static final String PCIADMINPASS = "pciadmin";
	static final String RXCADMINPASS = "rxcadmin";

	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	public static final String DATABASE_CREATE = "CREATE TABLE " + USERS + " ("
			+ USERID + " INTEGER PRIMARY KEY , " + USERNAME
			+ " VARCHAR UNIQUE, " + PASSWORD + " VARCHAR)";

	public static final String SHIPPINGCASE_CREATE = "CREATE TABLE "
			+ SHIPPINGCASE + " (" + SHIPPINGID + " INTEGER PRIMARY KEY , "
			+ TNTLABEL + " VARCHAR, " + MPACODES + " VARCHAR, " + TIMESTAMP
			+ " TIMESTAMP NOT NULL, " + TASK + " VARCHAR )";

	private static final String FLAGDB_CREATE = "CREATE TABLE " + FIRST + " ("
			+ FLAGID + " INTEGER PRIMARY KEY , " + FLAG + " INTEGER UNIQUE)";

	public static final String EMAILDB_CREATE = "CREATE TABLE " + EMAIL + " ("
			+ EMAILID + " INTEGER PRIMARY KEY , " + EMAILADDRESS + " VARCHAR)";

	public static final String SETTINGSDB_CREATE = "CREATE TABLE " + SETTINGS
			+ " (" + SETTINGSID + " INTEGER PRIMARY KEY , " + CASELIMIT
			+ " VARCHAR, " + LOCATIONS + " VARCHAR, " + VERSION + " VARCHAR, "
			+ PCIADMINPASS + " VARCHAR, " + RXCADMINPASS + " VARCHAR)";

	public static final String AIRBILL_CREATE = "CREATE TABLE " + AIRBILL
			+ " (" + AIRBILLID + " INTEGER PRIMARY KEY , " + DHLLABEL
			+ " VARCHAR, " + TNTLABELS + " VARCHAR, " + DESTINATION
			+ " VARCHAR, " + COMMENTS + " VARCHAR, " + TIMESTAMP
			+ " TIMESTAMP NOT NULL, " + TASK + " VARCHAR )";

	public DatabaseHelper(Context context) {
		super(context, DBNAME, null, DATABASE_VERSION);
		// super(context, Environment.getExternalStorageDirectory()
		// + File.separator + FILE_DIR
		// + File.separator + DBNAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// create user table
		db.execSQL(DATABASE_CREATE);
		// create first time flag table
		db.execSQL(FLAGDB_CREATE);
		db.execSQL(SHIPPINGCASE_CREATE);
		db.execSQL(SETTINGSDB_CREATE);
		db.execSQL(EMAILDB_CREATE);
		db.execSQL(AIRBILL_CREATE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DatabaseHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + USERS);
		db.execSQL("DROP TABLE IF EXISTS " + FIRST);
		db.execSQL("DROP TABLE IF EXISTS " + SHIPPINGCASE);
		db.execSQL("DROP TABLE IF EXISTS " + EMAIL);
		db.execSQL("DROP TABLE IF EXISTS " + SETTINGS);
		db.execSQL("DROP TABLE IF EXISTS " + AIRBILL);
		onCreate(db);

	}

}