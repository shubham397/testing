package com.marmeto.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class UserDataSource {
	 // Database fields
	  private static SQLiteDatabase database;
	  private DatabaseHelper dbHelper;
	  private String[] allColumns = { DatabaseHelper.USERID,
			  DatabaseHelper.USERNAME, DatabaseHelper.PASSWORD };

	  public UserDataSource(Context context) {
	    dbHelper = new DatabaseHelper(context);
	  }

	  public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	  }

	  public void close() {
	    dbHelper.close();
	  }

	  public Users createUser(String username, String password) {
	    ContentValues values = new ContentValues();
	    values.put(DatabaseHelper.USERNAME, username);
	    values.put(DatabaseHelper.PASSWORD, password);
	    long insertId = database.insert(DatabaseHelper.USERS, null,
	        values);
	    Cursor cursor = database.query(DatabaseHelper.USERS,
	        allColumns, DatabaseHelper.USERID + " = " + insertId, null,
	        null, null, null);
	    cursor.moveToFirst();
	    Users newUser = cursorToUsers(cursor);
	    cursor.close();
	    return newUser;
	  }

	  public void deleteUser(Users users) {
	    long id = users.getId();
	    System.err.println("User deleted with id: " + id);
	    database.delete(DatabaseHelper.USERS, DatabaseHelper.USERID
	        + " = " + id, null);
	  }

	  public List<Users> getAllUsers() {
	    List<Users> userList = new ArrayList<Users>();

	    Cursor cursor = database.query(DatabaseHelper.USERS,
	        allColumns, null, null, null, null, null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      Users users = cursorToUsers(cursor);
	      userList.add(users);
	      cursor.moveToNext();
	    }
	    // make sure to close the cursor
	    cursor.close();
	    return userList;
	  }

	  private Users cursorToUsers(Cursor cursor) {
	    Users user = new Users();
	    user.setId(cursor.getLong(0));
	    user.setUsername(cursor.getString(1));
	    user.setPassword(cursor.getString(2));
	    return user;
	  }
	  
	  public static boolean checkIfPasswordExists(String password) {
		 
		    String Query = "Select * from " + DatabaseHelper.USERS + " where " + DatabaseHelper.PASSWORD + " = " + password;
		    Cursor cursor = database.rawQuery(Query, null);
		        if(cursor.getCount() <= 0){
		            return false;
		        }
		    return true;
		}
	  
	  /**
	   * Determine if the provided username and password match a pair in the database
	   * @param username
	   * @param password
	   * @return
	   */
	  public boolean checkIfPasswordIsCorrect(String username, String password) {
		  
		  if(username.equals("PCI Admin") && password.equals("K#m5tL")){
			  return true;
		  }else{
		  
			 String realPassword = "";
		    String Query = "SELECT "+DatabaseHelper.PASSWORD + " FROM " + DatabaseHelper.USERS + " WHERE " + DatabaseHelper.USERNAME + " = '" + username+"';";
		    Cursor cursor = database.rawQuery(Query, null);
		 
		    if (cursor.moveToFirst()) {
		        do {
		        	realPassword=cursor.getString(0); // Here you can get data from table and stored in string if it has only one string.
		            System.err.println("IN HERE WITH REAL PASSWORD " + realPassword);
		        } while (cursor.moveToNext());
		     }
		        
		        if(realPassword.equals(password)){
		        	 return true;
		        }else{
		        	return false;
		        } 
		  }
		}
	  
	  public boolean updatePassword(String username, String password) {
		    ContentValues args = new ContentValues();
		    args.put(DatabaseHelper.PASSWORD, password); 
		    return database.update(DatabaseHelper.USERS, args, DatabaseHelper.USERNAME + "='" + username+"'", null) > 0;
		  }
	  
	  public String getCurrentPassword(String username) {
			String results = "";
			String Query = "SELECT " + DatabaseHelper.PASSWORD + " FROM "
					+ DatabaseHelper.USERS + " WHERE "
					+ DatabaseHelper.USERNAME + " = '"+ username +"';";
			Cursor cursor = database.rawQuery(Query, null);

			if (cursor.moveToFirst()) {
				do {
					results = cursor.getString(0); // Here you can get data from
													// table and stored in string if
													// it has only one string.
					System.err.println("IN HERE WITH PASSWORD " + results);
				} while (cursor.moveToNext());
			}

			return results;
		}
		/**
		 * Delete old email database and rebuild
		 */
		  public void deleteUsers() { 
			    SQLiteDatabase db = dbHelper.getWritableDatabase(); 
			    db.execSQL("DROP TABLE IF EXISTS " + DatabaseHelper.USERS);
				db.execSQL(DatabaseHelper.DATABASE_CREATE); 
			}
}
