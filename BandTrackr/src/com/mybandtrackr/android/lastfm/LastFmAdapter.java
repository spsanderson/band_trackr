package com.mybandtrackr.android.lastfm;

import org.apache.http.util.EncodingUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Provides access to the last.fm database which is exclusively used as a 
 * cache for last.fm responses.
 * 
 * @see com.mybandtrackr.android.lastfm.DatabaseCache
 */
public class LastFmAdapter {

	public static final String KEY_KEY = "key";
	public static final String KEY_RESPONSE = "response";
	public static final String KEY_EXPIRATION_DATE = "expiration_date";

	private static final String TAG = LastFmAdapter.class.getSimpleName();
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String DATABASE_NAME = "lastfm.db";
	private static final String DATABASE_TABLE = "cache";
	private static final int DATABASE_VERSION = 2;

	/**
	 * Database queries
	 */
	private static final String DATABASE_CREATE_STATEMENT = "CREATE TABLE " + DATABASE_TABLE + " (" + KEY_KEY + " VARCHAR(32) PRIMARY KEY, " + KEY_RESPONSE
			+ " BLOB NOT NULL," + KEY_EXPIRATION_DATE + " TIMESTAMP NOT NULL" + ");";
	private static final String DATABASE_QUERY_BY_KEY = KEY_KEY + "= ?";

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE_STATEMENT);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE + ";");
			onCreate(db);
		}

	}

	/**
	 * Constructor - takes the provided context to allow for the database to be
	 * opened/created.
	 * 
	 * @param context the Context within which to work.
	 */
	public LastFmAdapter(Context context) {
		mCtx = context;
	}

	/**
	 * Open the last.fm database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException if the database could be neither opened or created
	 */
	public LastFmAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	/**
	 * Creates a new cache entry identified by the supplied key mapped to the 
	 * supplied response which will be valid up to the expiration date. The 
	 * key will later be used to reference the entry to retrieve the stored response.
	 *  
	 * @param key of the cache entry.
	 * @param response to cache.
	 * @param expirationDate after which the cache will be treated as invalid.
	 */
	public void createCacheEntry(String key, String response, long expirationDate) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_KEY, key);
		initialValues.put(KEY_RESPONSE, EncodingUtils.getBytes(response, "UTF-8"));
		initialValues.put(KEY_EXPIRATION_DATE, expirationDate);

		mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	/**
	 * Returns the response of the cache entry.
	 * 
	 * @param key of the cache entry.
	 * @return the cache response or null if the entry doesn't exist.
	 */
	public String getCacheEntryResponse(String key) {
		String[] columns = new String[] { KEY_RESPONSE };
		String[] selectionArgs = new String[] { key };
		Cursor c = mDb.query(DATABASE_TABLE, columns, DATABASE_QUERY_BY_KEY, selectionArgs, null, null, null);

		if (c != null && c.moveToFirst()) {
			byte[] response = c.getBlob(c.getColumnIndex(KEY_RESPONSE));
			c.close();
			return EncodingUtils.getString(response, "UTF-8");
		}

		c.close();
		return null; // Cache entry doesn't exist

	}

	/**
	 * Deletes a cache entry that matches the supplied key.
	 * 
	 * The entry may or may not be present in the database.
	 * @param key of the cache entry.
	 */
	public void deleteCacheEntry(String key) {
		String[] whereArgs = new String[] { key };
		mDb.delete(DATABASE_TABLE, DATABASE_QUERY_BY_KEY, whereArgs);
	}

	/**
	 * This will drop the cache table, clearing the cache completely. Afterwards, 
	 * the cache table will be recreated.
	 */
	public void deleteCacheEntries() {
		mDb.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE + ";");
		mDb.execSQL(DATABASE_CREATE_STATEMENT);
	}

	/**
	 * Used to check whether the cache entry has expired.
	 * 
	 * @param key of the cache entry.
	 * @return bool whether the key has expired or not, 
	 * 		or false if the key doesn't exist.
	 */
	public boolean isCacheEntryExpired(String key) {
		String[] columns = new String[] { KEY_EXPIRATION_DATE };
		String[] selectionArgs = new String[] { key };
		Cursor c = mDb.query(DATABASE_TABLE, columns, DATABASE_QUERY_BY_KEY, selectionArgs, null, null, null);

		if (c != null && c.moveToFirst()) {
			long expirationDate = c.getLong(c.getColumnIndex(KEY_EXPIRATION_DATE));
			c.close();
			return expirationDate < System.currentTimeMillis();
		}

		c.close();
		return false; // Cache entry doesn't exist
	}

	/**
	 * Returns whether there is a cache entry with the supplied key.
	 * 
	 * @param key of the cache entry.
	 * @return bool whether such an entry exists.
	 */
	public boolean hasCacheEntry(String key) {
		String[] selectionArgs = new String[] { key };
		Cursor c = mDb.query(DATABASE_TABLE, null, DATABASE_QUERY_BY_KEY, selectionArgs, null, null, null);

		if (c != null && c.moveToFirst()) {
			boolean result = c.getCount() == 1;
			c.close();
			return result;
		}

		c.close();
		return false; // Cache entry doesn't exist
	}

}
