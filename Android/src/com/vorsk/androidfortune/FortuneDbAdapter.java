package com.vorsk.androidfortune;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//Yanked from http://developer.android.com/training/notepad/notepad-ex1.html
// and http://www.androiddesignpatterns.com/2012/05/correctly-managing-your-sqlite-database.html

public class FortuneDbAdapter {
	private static FortuneDbAdapter mInstance = null;
	
	public static final String KEY_ID = "_id";
	
	public static final String KEY_TEXT = "fortunetext";
	public static final String KEY_UPVOTES = "upvotes";
	public static final String KEY_DOWNVOTES = "downvotes";
	public static final String KEY_UPVOTED = "upvoted";
	public static final String KEY_DOWNVOTED = "downvoted";
	
	public static final String KEY_SUBMITDATE = "submitdate";
	public static final String KEY_VIEWDATE = "viewdate";
	public static final String KEY_FLAG = "flag";
	public static final String KEY_OWNER = "owner";
	
	private static final String TAG = "FortuneDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	/**
	 * Database creation sql statement
	 */
	private static final String DATABASE_CREATE =
			"create table fortunes" +
	        "(" + KEY_ID + " integer primary key autoincrement, " +
			      KEY_TEXT + " text not null, " +
			      KEY_UPVOTES + " integer, " +
			      KEY_DOWNVOTES + " integer, " +
			      KEY_UPVOTED +  " integer, " +
			      KEY_DOWNVOTED + " integer, " +
			      KEY_SUBMITDATE + " integer not null, " +
			      KEY_VIEWDATE + " integer, " +
			      KEY_FLAG + " integer, " +
			      KEY_OWNER + " integer);";

	private static final String DATABASE_NAME = "data";
	private static final String DATABASE_TABLE = "fortunes";
	private static final int DATABASE_VERSION = 2;

	private final Context mContext;

	public static FortuneDbAdapter getInstance(Context ctx) {
		
		// Use the application context, which will ensure that you 
	    // don't accidentally leak an Activity's context.
	    // See this article for more information: http://bit.ly/6LRzfx
	    if (mInstance == null) {
	      mInstance = new FortuneDbAdapter(ctx.getApplicationContext());
	    }
	    return mInstance;
	}
	
	//overloaded method. need to initialize using the other first though
	public static FortuneDbAdapter getInstance(){
	    if (mInstance == null) {
	    	Log.v(TAG,"FortuneDbAdapter instance has not been initialized.");
	    }
	    return mInstance;
	}
	
	/**
	 * Constructor - takes the context to allow the database to be opened/created
	 * 
	 * @param ctx the Context within which to work
	 */
	public FortuneDbAdapter(Context ctx) {
		this.mContext = ctx;
	}
	
	
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.v(TAG, "Creating database of version " + DATABASE_VERSION);
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS fortunes");
			onCreate(db);
		}
	}

	/**
	 * Open the database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException if the database could be neither opened or created
	 */
	public FortuneDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mContext);
		mDb = mDbHelper.getWritableDatabase();
		
		Log.v(TAG, "Db version: "+ mDb.getVersion());
		return this;
	}

	public void close() {
		mDbHelper.close();
	}


	/**
	 * Create a new fortune from user
	 * 
	 * @param body the body of the fortune
	 * @return rowId or -1 if failed
	 */
	public long createFortune(String text) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_TEXT, text);
		initialValues.put(KEY_SUBMITDATE, System.currentTimeMillis() );
		
		initialValues.put(KEY_UPVOTES, 0);
		initialValues.put(KEY_DOWNVOTES, 0);
		initialValues.put(KEY_UPVOTED, 0);
		initialValues.put(KEY_DOWNVOTED, 0);
		initialValues.put(KEY_VIEWDATE, -1);
		initialValues.put(KEY_FLAG, 0);
		initialValues.put(KEY_OWNER, 1);
		
		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}
	
	/**
	 * Take a JSON string and insert it into the database
	 * 
	 * @param body the body of the fortune
	 * @return rowId or -1 if failed
	 * @throws JSONException 
	 */
	public long createFortuneFromJson(String json) throws JSONException {
		JSONObject data = new JSONObject(json);
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_ID, data.getInt("fortuneID"));
		initialValues.put(KEY_TEXT, data.getString("text"));
		initialValues.put(KEY_UPVOTES, data.getInt("upvote"));
		initialValues.put(KEY_DOWNVOTES, data.getInt("downvote"));
		initialValues.put(KEY_UPVOTED, 0);
		initialValues.put(KEY_DOWNVOTED, 0);
		initialValues.put(KEY_SUBMITDATE, data.getInt("uploadDate"));
		initialValues.put(KEY_VIEWDATE, -1);
		initialValues.put(KEY_FLAG, 0);
		initialValues.put(KEY_OWNER, data.getInt("uploaders"));
		
		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}
	
	/**
	 * Returns the JSON string representation of row
	 * 
	 * @param id row to create Json of
	 * @return String JSON string
	 */
	public String createFortuneJson(int id) {
		Cursor c = 
				mDb.query(true, DATABASE_TABLE, null, KEY_ID + "=" + id, null,
						null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		
		JSONObject json = new JSONObject();
		try {
			json.put("fortuneID", c.getInt(c.getColumnIndexOrThrow(KEY_ID)));
			json.put("text", c.getString(c.getColumnIndexOrThrow(KEY_TEXT)));
			json.put("uploadDate", c.getInt(c.getColumnIndexOrThrow(KEY_SUBMITDATE)));
		}
		catch (Exception e) {
			Log.v(TAG,e.getMessage());
		}
		
		String s = json.toString();
		return s;
	}

	/**
	 * Delete the fortune with the given rowId
	 * 
	 * @param rowId id of fortune to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteFortune(long rowId) {

		return mDb.delete(DATABASE_TABLE, KEY_ID + "=" + rowId, null) > 0;
	}

	/**
	 * Return a list of all fortunes
	 * 
	 * @return ArrayList of all fortunes
	 */
	public ArrayList<Fortune> fetchAllFortunes() {
		ArrayList<Fortune> fortunes = new ArrayList<Fortune>();
		Cursor c = mDb.query(DATABASE_TABLE, null, null, null, null, null, null);
		
		if (c != null) {
			c.moveToFirst();
		}
		
		while ( !c.isAfterLast() ) {
			fortunes.add(new Fortune(c));
			c.moveToNext();
		}
		return fortunes;
	}
	
	/**
	 * Return a list of all the fortunes by the current user
	 * 
	 * @return ArrayList of all fortunes
	 */
	public ArrayList<Fortune> fetchAllByUser() {
		ArrayList<Fortune> fortunes = new ArrayList<Fortune>();
		Cursor c = mDb.query(DATABASE_TABLE, null, KEY_OWNER + "=" + 1, null, null, null, null);
		
		if (c != null) {
			c.moveToFirst();
		}
		
		while ( !c.isAfterLast() ) {
			fortunes.add(new Fortune(c));
			c.moveToNext();
		}
		return fortunes;
	}

	/**
	 * Return a Cursor positioned at the fortune that matches the given rowId
	 * 
	 * @param fortuneId id of fortune to retrieve
	 * @return Fortune object 
	 */
	public Fortune fetchFortune(long fortuneId) {

		Cursor mCursor = 
				mDb.query(true, DATABASE_TABLE, null, KEY_ID + "=" + fortuneId, null,
						null, null, null, null);
		if ( mCursor == null )
			return null;
		
		mCursor.moveToFirst();
		return new Fortune(mCursor);
	}
	
	
	/**
	 * UPDATE ALL THE THINGS!!! (updates all fields of fortune whether you need to or not)
	 * 
	 * @param fortune row/object to update.
	 * @return true if the fortune was successfully updated, false otherwise
	 */
	public boolean updateFortune(Fortune f) {
		ContentValues args = new ContentValues();
		args.put(KEY_TEXT, f.getFortuneText());
		args.put(KEY_UPVOTES, f.getUpvotes());
		args.put(KEY_DOWNVOTES, f.getDownvotes());
		args.put(KEY_UPVOTED, f.getUpvoted() ? 1 : 0);
		args.put(KEY_DOWNVOTED, f.getDownvoted() ? 1 : 0);
		//don't update submit date
		if ( f.getSeen()!= null )
			args.put(KEY_VIEWDATE, f.getSeen().getTime()/1000);
		args.put(KEY_FLAG, f.getFlagged() ? 1 : 0);
		//args.put(KEY_OWNER, f.getOwner()? 1 : 0 ); //probably shouldn't update
		return mDb.update(DATABASE_TABLE, args, KEY_ID + "=" + f.getFortuneID(), null) > 0;
	}
	
	/**
	 * Update the fortune using the details provided.
	 * 
	 * @param id id of fortune to update
	 * @param columnName column to update
	 * @param value new value to update column to
	 * @return true if the fortune was successfully updated, false otherwise
	 */
	public boolean updateFortuneCol(int id, String columnName, String value) {
		ContentValues args = new ContentValues();
		args.put(columnName, value);
		return mDb.update(DATABASE_TABLE, args, KEY_ID + "=" + id, null) > 0;
	}
	
	/**
	 * Update the fortune using the details provided.
	 * 
	 * @param id id of fortune to update
	 * @param columnName column to update
	 * @param value new value to update column to
	 * @return true if the fortune was successfully updated, false otherwise
	 */
	public boolean updateFortuneCol(int id, String columnName, long value) {
		ContentValues args = new ContentValues();
		args.put(columnName, value);
		return mDb.update(DATABASE_TABLE, args, KEY_ID + "=" + id, null) > 0;
	}
	
	/**
	 * Update the fortune using the details provided.
	 * 
	 * @param id id of fortune to update
	 * @param columnName column to update
	 * @param value new value to update column to
	 * @return true if the fortune was successfully updated, false otherwise
	 */
	public boolean updateFortuneCol(int id, String columnName, boolean value) {
		return updateFortuneCol(id, columnName, value ? 1 : 0);
	}
	
	/**
	 * Update the fortune using the details provided.
	 * 
	 * @param id id of fortune to update
	 * @param columnName column to update
	 * @param value new value to update column to
	 * @return true if the fortune was successfully updated, false otherwise
	 */
	public boolean updateFortuneCol(int id, String columnName, Date value) {
		return updateFortuneCol(id, columnName, value.getTime()/1000);
	}
	
	/**
	 * Remove all users and groups from database. Probably should remove. Or hide.
	 */
	public void removeAll()
	{
	    mDb.delete(DATABASE_TABLE, null, null);
	}
}
