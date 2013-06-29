package com.mybandtrackr.android.lastfm;

import java.io.IOException;
import java.io.InputStream;

import com.mybandtrackr.android.Util;

import android.content.Context;
import android.util.Log;

import de.umass.lastfm.cache.Cache;

/**
 * The <code>AndroidCache</code> handles caching of last.fm responses in the
 * database using the {@link LastFmAdapter}.
 * 
 * <em>Note: the cache is configured once the app is initialized during the 
 * execution of {@link com.mybandtrackr.android.App#onCreate()} once for the 
 * {@link de.umass.lastfm.Caller} singleton instance and shouldn't be 
 * configured anywhere else.</em>
 * 
 * @see de.umass.lastfm.Caller#setCache(Cache)
 * @see de.umass.lastfm.cache.ExpirationPolicy
 */
public class DatabaseCache extends Cache {

	private static final String TAG = DatabaseCache.class.getSimpleName();
	private LastFmAdapter mLastFmAdapter;

	/**
	 * Take's the current context with which to initialize an instance of the 
	 * {@link LastFmAdapter} to create/open the cache database.
	 * 
	 * @param context the Context within which to initialize {@link LastFmAdapter}
	 */
	public DatabaseCache(Context context) {
		mLastFmAdapter = new LastFmAdapter(context).open();
	}

	@Override
	public void store(String cacheEntryName, InputStream inputStream, long expirationDate) {
		String response;

		try {
			response = Util.streamToString(inputStream);
		} catch (IOException e) {
			return; // We simply don't store the result if an I/O error occurs
		}

		if (mLastFmAdapter.hasCacheEntry(cacheEntryName)) {

			if (!mLastFmAdapter.isCacheEntryExpired(cacheEntryName)) {
				Log.w(TAG, "Cache entry not expired but store is called. ");
			}

			mLastFmAdapter.deleteCacheEntry(cacheEntryName);

		}

		mLastFmAdapter.createCacheEntry(cacheEntryName, response, expirationDate);
	}

	@Override
	public boolean contains(String cacheEntryName) {
		return mLastFmAdapter.hasCacheEntry(cacheEntryName);
	}

	@Override
	public InputStream load(String cacheEntryName) {
		String response = mLastFmAdapter.getCacheEntryResponse(cacheEntryName);

		if (response != null) {
			return Util.stringToStream(response);
		}
		return null;
	}

	@Override
	public void remove(String cacheEntryName) {
		mLastFmAdapter.deleteCacheEntry(cacheEntryName);
	}

	@Override
	public boolean isExpired(String cacheEntryName) {
		return mLastFmAdapter.isCacheEntryExpired(cacheEntryName);
	}

	@Override
	public void clear() {
		mLastFmAdapter.deleteCacheEntries();
	}

}
