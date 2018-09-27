package com.marmeto.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class ShippingCaseDataSource {
	 // Database fields
	  private static SQLiteDatabase database;
	  private DatabaseHelper dbHelper;
	  private String[] allColumns = { DatabaseHelper.SHIPPINGID,
			  DatabaseHelper.TNTLABEL, DatabaseHelper.MPACODES, DatabaseHelper.TASK };

	  public ShippingCaseDataSource(Context context) {
	    dbHelper = new DatabaseHelper(context);
	  }

	  public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	  }

	  public void close() {
	    dbHelper.close();
	  }
	  
	  public void insertShippingCase(String tntLabel, String mpaCodes, String action, Long timestamp) {
		 
		  ContentValues values = new ContentValues();
		  values.put(DatabaseHelper.TNTLABEL, tntLabel);
		    values.put(DatabaseHelper.MPACODES, mpaCodes);
		    values.put(DatabaseHelper.TASK, action); 
		    values.put(DatabaseHelper.TIMESTAMP, timestamp); 
		  database.insert(DatabaseHelper.SHIPPINGCASE, null, values);
		  database.close();
		}

		


//	  public ShippingCase addShippingCase(String tntLabel, String mpaCodes, String action) {
//	    ContentValues values = new ContentValues();
//	    values.put(DatabaseHelper.TNTLABEL, tntLabel);
//	    values.put(DatabaseHelper.MPACODES, mpaCodes);
//	    values.put(DatabaseHelper.TASK, action);
//	    long insertId = database.insert(DatabaseHelper.SHIPPINGCASE, null,
//	        values);
//	    Cursor cursor = database.query(DatabaseHelper.SHIPPINGCASE,
//	        allColumns, DatabaseHelper.SHIPPINGID + " = " + insertId, null,
//	        null, null, null);
//	    cursor.moveToFirst();
//	    ShippingCase newShippingCase = cursorToShippingCases(cursor);
//	    cursor.close();
//	    return newShippingCase;
//	  }

	  public void deleteShippingCase(ShippingCase cases) {
	    long id = cases.getId();
	    System.err.println("Shipping Case deleted with id: " + id);
	    database.delete(DatabaseHelper.SHIPPINGCASE, DatabaseHelper.SHIPPINGID
	        + " = " + id, null);
	  }
	  
		/**
		 * Check if the database exist, if it doesn't create it.
		 * 
		 * @return true if it exists, false if it doesn't
		 */
		public boolean checkShippingCaseDataBase() {
			System.err.println("CHECKING IF SHIPPING CASE DB EXISTS");
		    SQLiteDatabase checkDB = null;
		    try {
		        checkDB = SQLiteDatabase.openDatabase(DatabaseHelper.SHIPPINGCASE, null,
		                SQLiteDatabase.OPEN_READONLY);
		        checkDB.close();
		    } catch (SQLiteException e) {
		        // database doesn't exist yet so created it
		    	System.err.println("SHIPPING CASE DOESN'T EXIST, CREATE EMPTY SHELL");
		    	SQLiteDatabase db = dbHelper.getWritableDatabase();
		    	db.execSQL(DatabaseHelper.SHIPPINGCASE_CREATE);
		    }
		    return checkDB != null;
		}
	  
//
//	  public List<ShippingCase> getAllShippingCases() {
//	    List<ShippingCase> casesList = new ArrayList<ShippingCase>();
//
//	    Cursor cursor = database.query(DatabaseHelper.SHIPPINGCASE,
//	        allColumns, null, null, null, null, null);
//
//	    cursor.moveToFirst();
//	    while (!cursor.isAfterLast()) {
//	    	ShippingCase cases = cursorToShippingCases(cursor);
//	    	casesList.add(cases);
//	      cursor.moveToNext();
//	    }
//	    // make sure to close the cursor
//	    cursor.close();
//	    return casesList;
//	  }
	  
	  public static  List<String> getShippingCaseIDs() {
		    List<String> ids = new ArrayList<String>();
			 String id = "";
		    String Query = "Select " + DatabaseHelper.SHIPPINGID + " from " + DatabaseHelper.SHIPPINGCASE + ";";
		    Cursor cursor = database.rawQuery(Query, null);
		    if (cursor.moveToFirst()) {
		        do {
		        	id=cursor.getString(0); // Here you can get data from table and stored in string if it has only one string.
		            System.err.println("IN HERE WITH SHIPPING CASE TABLE ID " + id);
		           ids.add(id);
		        } while (cursor.moveToNext());
		     }
		    return ids;
		}
	  

	  private ShippingCase cursorToShippingCases(Cursor cursor) {
		  ShippingCase cases = new ShippingCase();
		  cases.setId(cursor.getLong(0));
		  cases.setTNTLabel(cursor.getString(1));
		  cases.setMPACodes(cursor.getString(2));
	    return cases;
	  }
	  
	  public static boolean checkIfLabelExists(String tntLabel) {
		 
		    String Query = "Select * from " + DatabaseHelper.SHIPPINGCASE + " where " + DatabaseHelper.TNTLABEL + " = " + tntLabel;
		    Cursor cursor = database.rawQuery(Query, null);
		        if(cursor.getCount() <= 0){
		            return false;
		        }
		    return true;
		}
	  
	  /**
	   * Determine if the provided mpaCode is already in a shipping case
	   * @param mpaCode 
	   * @return true if not in the case
	   */
	  public boolean checkIfMPACodeIsStored(String mpaCode) {
			 String results = "";
			 boolean success = true;
			 System.err.println("TESTING MPA CODE " + mpaCode);
		    String Query = "SELECT "+DatabaseHelper.MPACODES + " FROM " + DatabaseHelper.SHIPPINGCASE + " "
		    		+ "WHERE " + DatabaseHelper.TASK +"='AGGREGATE';";
		    Cursor cursor = database.rawQuery(Query, null);
		 
		    if (cursor.moveToFirst()) {
		        do {
		        	results=cursor.getString(0); // Here you can get data from table and stored in string if it has only one string.
		            System.err.println("IN HERE WITH MPA CODES " + results);
		            if(results.contains(mpaCode)){
		            	 success = false;
			        	
			        } 
		        } while (cursor.moveToNext());
		     }
		      return success;
		       
		}
	  
	  /**
	   * Determine if the tnt label was once aggregated then deaggregated so can be re-used
	   * @param mpaCode 
	   * @return true if not in the case
	   */
	  public boolean checkIfMPACodeIsStoredReaggregated(String mpaCode) {
			 String results = "";
			 int aggregatedCount = 0;
			 int deaggregatedCount = 0;
			 String tntLabel = "";
			 boolean success = true;
			 
			 //get the aggregated count for where this mpa code exists
		    String aggregatedCountQuery = "SELECT Count("+DatabaseHelper.MPACODES + ") FROM " + DatabaseHelper.SHIPPINGCASE  + " "
		    		+ "WHERE " + DatabaseHelper.TASK +"='AGGREGATE' AND "+ DatabaseHelper.MPACODES + " LIKE'%" + mpaCode + "%';";
		    Cursor cursor = database.rawQuery(aggregatedCountQuery, null);
		 
		    if (cursor.moveToFirst()) {
		        do {
		        	aggregatedCount=cursor.getInt(0); // Here you can get data from table and stored in string if it has only one string.
		            System.err.println("IN HERE WITH AGGREGATED COUNT: " + aggregatedCount);
		         
		        } while (cursor.moveToNext());
		     }
		    
		    //GET TNT LABEL FOR WHERE THAT CODE EXISTS TO SEARCH FOR DE-AGGREGATIONS SINCE DE-AGGREGATIONS MAY NOT HAVE MPA CODES IF DONE BY TNT LABEL
		    
		    if(aggregatedCount > 0){
		    	 //get the aggregated count for where this mpa code exists
			    String tntLabelQuery = "SELECT "+DatabaseHelper.TNTLABEL + " FROM " + DatabaseHelper.SHIPPINGCASE  + " "
			    		+ "WHERE " + DatabaseHelper.TASK +"='AGGREGATE' AND "+ DatabaseHelper.MPACODES + " LIKE'%" + mpaCode + "%';";
			    Cursor cursor3 = database.rawQuery(tntLabelQuery, null);
			 
			    if (cursor3.moveToFirst()) {
			        do {
			        	tntLabel=cursor3.getString(0); // Here you can get data from table and stored in string if it has only one string.
			            System.err.println("IN HERE WITH TNT LABEL: " + tntLabel);
			         
			        } while (cursor3.moveToNext());
			     }
		    	
		    }
		    
		    
		    //get the deaggregated count for where this mpa code exists
		    String deaggregatedCountQuery = "SELECT Count("+DatabaseHelper.TNTLABEL + ") FROM " + DatabaseHelper.SHIPPINGCASE  + " "
		    		+ "WHERE " + DatabaseHelper.TASK +"!='AGGREGATE' AND "+ DatabaseHelper.TNTLABEL + " = '" + tntLabel + "';";
		    Cursor cursor2 = database.rawQuery(deaggregatedCountQuery, null);
		 
		    if (cursor2.moveToFirst()) {
		        do {
		        	deaggregatedCount=cursor2.getInt(0); // Here you can get data from table and stored in string if it has only one string.
		            System.err.println("IN HERE WITH DEAGGREGATED COUNT: " + deaggregatedCount);
		         
		        } while (cursor2.moveToNext());
		     }
		    
		    if(aggregatedCount == 0 && deaggregatedCount != 0){
		    	success = true;
		    }else{
		    	if(deaggregatedCount >= aggregatedCount ){
		    		success = true;
		    	}else{
		    		success = false;
		    	}
		    }
		    
		    return success;
		}
	  
	  
	  /**
	   * Determine if the tnt label is already used
	   * @param mpaCode 
	   * @return true if not in the case
	   */
	  public boolean checkIfTNTLabelStored(String tntLabel) {
			 String results = "";
			 boolean success = true;
		    String Query = "SELECT "+DatabaseHelper.TNTLABEL + " FROM " + DatabaseHelper.SHIPPINGCASE + ";";
		    Cursor cursor = database.rawQuery(Query, null);
		 
		    if (cursor.moveToFirst()) {
		        do {
		        	results=cursor.getString(0); // Here you can get data from table and stored in string if it has only one string.
		            System.err.println("IN HERE WITH TNT LABEL " + results);
		            System.err.println("ATTEMPTING TO COMPARE TO LABEL " + tntLabel);
		            if(results.equals(tntLabel)){
			        	 success = false;
			        }
		        } while (cursor.moveToNext());
		     }
		    return success;
		}
	  
	  /**
	   * Determine if the tnt label was once aggregated then deaggregated so can be re-used
	   * @param mpaCode 
	   * @return true if not in the case
	   */
	  public boolean checkIfTNTLabelStoredReaggregated(String tntLabel) {
			 String results = "";
			 int aggregatedCount = 0;
			 int deaggregatedCount = 0;
			 boolean success = true;
			 
			 //get the aggregated count for this tntlabel
		    String aggregatedCountQuery = "SELECT Count("+DatabaseHelper.TNTLABEL + ") FROM " + DatabaseHelper.SHIPPINGCASE  + " "
		    		+ "WHERE " + DatabaseHelper.TASK +"='AGGREGATE' AND "+ DatabaseHelper.TNTLABEL + "='" + tntLabel + "';";
		    Cursor cursor = database.rawQuery(aggregatedCountQuery, null);
		 
		    if (cursor.moveToFirst()) {
		        do {
		        	aggregatedCount=cursor.getInt(0); // Here you can get data from table and stored in string if it has only one string.
		            System.err.println("IN HERE WITH AGGREGATED COUNT: " + aggregatedCount);
		         
		        } while (cursor.moveToNext());
		     }
		    
		    //get the deaggregated count for this tnt label
		    String deaggregatedCountQuery = "SELECT Count("+DatabaseHelper.TNTLABEL + ") FROM " + DatabaseHelper.SHIPPINGCASE  + " "
		    		+ "WHERE " + DatabaseHelper.TASK +"!='AGGREGATE' AND "+ DatabaseHelper.TNTLABEL + "='" + tntLabel + "';";
		    Cursor cursor2 = database.rawQuery(deaggregatedCountQuery, null);
		 
		    if (cursor2.moveToFirst()) {
		        do {
		        	deaggregatedCount=cursor2.getInt(0); // Here you can get data from table and stored in string if it has only one string.
		            System.err.println("IN HERE WITH DEAGGREGATED COUNT: " + deaggregatedCount);
		         
		        } while (cursor2.moveToNext());
		     }
		    
		    if(aggregatedCount == 0 && deaggregatedCount != 0){
		    	success = true;
		    }else{
		    	if(deaggregatedCount >= aggregatedCount ){
		    		success = true;
		    	}else{
		    		success = false;
		    	}
		    }
		    
		    return success;
		}
	  
	  public String getMPACodes(String id) {
			 String results = "";
		    String Query = "SELECT "+DatabaseHelper.MPACODES + " FROM " + DatabaseHelper.SHIPPINGCASE + " WHERE " + DatabaseHelper.SHIPPINGID + " = '" + id+"';";
		    Cursor cursor = database.rawQuery(Query, null);
		 
		    if (cursor.moveToFirst()) {
		        do {
		        	results=cursor.getString(0); // Here you can get data from table and stored in string if it has only one string.
		            System.err.println("IN HERE WITH MPA CODES " + results);
		        } while (cursor.moveToNext());
		     }
		        
		    return results;
		}
	  
	  public String getTNTLabel(String id) {
			 String results = "";
		    String Query = "SELECT "+DatabaseHelper.TNTLABEL + " FROM " + DatabaseHelper.SHIPPINGCASE + " WHERE " + DatabaseHelper.SHIPPINGID + " = '" + id+"';";
		    Cursor cursor = database.rawQuery(Query, null);
		 
		    if (cursor.moveToFirst()) {
		        do {
		        	results=cursor.getString(0); // Here you can get data from table and stored in string if it has only one string.
		           
		        } while (cursor.moveToNext());
		     }
		        
		    return results;
		}
	  
	  public String getTimestamp(String id) {
			 String results = "";
		    String Query = "SELECT "+DatabaseHelper.TIMESTAMP + " FROM " + DatabaseHelper.SHIPPINGCASE + " WHERE " + DatabaseHelper.SHIPPINGID + " = '" + id+"';";
		    Cursor cursor = database.rawQuery(Query, null);
		 
		    if (cursor.moveToFirst()) {
		        do {
		        	results=cursor.getString(0); // Here you can get data from table and stored in string if it has only one string.
		           
		        } while (cursor.moveToNext());
		     }
		        
		    return results;
		}
	  
	  public String getAction(String id) {
			 String results = "";
		    String Query = "SELECT "+DatabaseHelper.TASK + " FROM " + DatabaseHelper.SHIPPINGCASE + " WHERE " + DatabaseHelper.SHIPPINGID + " = '" + id+"';";
		    Cursor cursor = database.rawQuery(Query, null);
		 
		    if (cursor.moveToFirst()) {
		        do {
		        	results=cursor.getString(0); // Here you can get data from table and stored in string if it has only one string.
		       
		        } while (cursor.moveToNext());
		     }
		        
		    return results;
		}
	  
		/**
		 * Delete the queue and re-create an empty one
		 */
		  public void deleteQueue() {
			 
			  
			    SQLiteDatabase db = dbHelper.getWritableDatabase(); 
			    db.execSQL("DROP TABLE IF EXISTS " + DatabaseHelper.SHIPPINGCASE);
				db.execSQL(DatabaseHelper.SHIPPINGCASE_CREATE); 
			}
 
}
