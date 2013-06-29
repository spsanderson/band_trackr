package com.mybandtrackr.android.persistence;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class OAuthDataHelper {

	private static final String DATABASE_NAME = "oauth.db";
	private static final int DATABASE_VERSION = 4;

	private static final String TABLE_NAME = "oauth_data";

	private static final String COL_ID = "id";
	private static final String COL_TOKEN = "oauth_token";
	private static final String COL_TOKEN_SECRET = "oauth_token_secret";
	private static final String COL_PROVIDER = "provider";
	private static final String COL_IS_AUTHORIZED = "is_authorized";
	private static final String COL_VALID_UNTIL = "valid_until";
	private static final String COL_CUSTOM_DATA = "custom_data";

	private Context context;
	private SQLiteDatabase db;

	private SQLiteStatement insertStmt;
	private static final String INSERT = "insert into " + TABLE_NAME + "(" + COL_PROVIDER + ", " + COL_TOKEN + ", " + COL_TOKEN_SECRET + ", "
			+ COL_IS_AUTHORIZED + ", " + COL_CUSTOM_DATA + ", " + COL_VALID_UNTIL + ") values (?, ?, ?, ?, ?, ?)";

	private SQLiteStatement updateStmt;
	private static final String UPDATE = "update " + TABLE_NAME + " set " + COL_TOKEN + " = ?, " + COL_TOKEN_SECRET + " = ?, " + COL_IS_AUTHORIZED + " = ?, "
			+ COL_VALID_UNTIL + " = ?, " + COL_CUSTOM_DATA + " = ? where provider = ?";

	public OAuthDataHelper(Context context) {
		this.context = context;
		OpenHelper openHelper = new OpenHelper(this.context);
		this.db = openHelper.getWritableDatabase();
		this.insertStmt = this.db.compileStatement(INSERT);
		this.updateStmt = this.db.compileStatement(UPDATE);
	}

	public void close() {
		if (db.isOpen()) {
			db.close();
		}
	}

	public long insert(String provider, String oauthToken, String oauthTokenSecret, String isAuthorized, String validUntil, String customData) {
		this.insertStmt.bindString(1, provider);
		this.insertStmt.bindString(2, null != oauthToken ? oauthToken : "");
		this.insertStmt.bindString(3, null != oauthTokenSecret ? oauthTokenSecret : "");
		this.insertStmt.bindString(4, null != isAuthorized ? isAuthorized : "");
		this.insertStmt.bindString(5, null != customData ? customData : "");
		this.insertStmt.bindString(6, null != validUntil ? validUntil : "");
		
		return this.insertStmt.executeInsert();
	}

	public void updateByProvider(String provider, String oauthToken, String oauthTokenSecret, String isAuthorized, String customData, String validUntil) {
		this.updateStmt.bindString(1, null != oauthToken ? oauthToken : "");
		this.updateStmt.bindString(2, null != oauthTokenSecret ? oauthTokenSecret : "");
		this.updateStmt.bindString(3, null != isAuthorized ? isAuthorized : "");
		this.updateStmt.bindString(4, null != customData ? customData : "");
		this.updateStmt.bindString(5, null != validUntil ? validUntil : "");
		this.updateStmt.bindString(6, provider);

		this.updateStmt.execute();
	}

	public void deleteAll() {
		this.db.delete(TABLE_NAME, null, null);
	}

	public void deleteByProvider(String oauthDbKey) {
		this.db.delete(TABLE_NAME, COL_PROVIDER + " = ?", new String[] { oauthDbKey });
	}

	public Map<String, Map<String, String>> selectAll() {
		Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>(0);
		Cursor cursor = this.db.query(TABLE_NAME,
				new String[] { COL_TOKEN, COL_TOKEN_SECRET, COL_PROVIDER, COL_IS_AUTHORIZED, COL_CUSTOM_DATA, COL_VALID_UNTIL }, null, null, null, null,
				COL_PROVIDER + " desc");
		if (cursor.moveToFirst()) {
			do {
				Map<String, String> data = new HashMap<String, String>(0);
				// parse provider data from result set
				data.put(COL_TOKEN, cursor.getString(0));
				data.put(COL_TOKEN_SECRET, cursor.getString(1));
				data.put(COL_IS_AUTHORIZED, cursor.getString(3));
				data.put(COL_CUSTOM_DATA, cursor.getString(4));
				data.put(COL_VALID_UNTIL, cursor.getString(5));

				// put provider to result map
				map.put(cursor.getString(2), data);

			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return map;
	}

	private static class OpenHelper extends SQLiteOpenHelper {

		OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_NAME + "(" + COL_ID + " INTEGER PRIMARY KEY, " + COL_TOKEN + " TEXT, " + COL_TOKEN_SECRET + " TEXT, "
					+ COL_PROVIDER + " TEXT, " + COL_IS_AUTHORIZED + " TEXT, " + COL_CUSTOM_DATA + " TEXT, " + COL_VALID_UNTIL + " TEXT)");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("Example", "Upgrading database, this will drop tables and recreate.");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}
}