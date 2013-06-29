package com.mybandtrackr.android.test.lastfm;

import com.mybandtrackr.android.lastfm.LastFmAdapter;

import android.test.AndroidTestCase;

import android.test.RenamingDelegatingContext;

import static com.mybandtrackr.android.test.Constants.*;

public final class LastFmAdapterTest extends AndroidTestCase {

	private static final long TIME = System.currentTimeMillis();
	private static final long ONE_DAY_MILIS = 1000 * 60 * 60 * 24;
	
	private LastFmAdapter mLastFmAdapter;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		RenamingDelegatingContext context 
			= new RenamingDelegatingContext(getContext(), TEST_FILE_PREFIX);
		
		mLastFmAdapter = new LastFmAdapter(context);
		mLastFmAdapter.open();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
		mLastFmAdapter.close();
		mLastFmAdapter = null;
	}
	
	public void testPreConditions() {
		assertNotNull(mLastFmAdapter);
	}
	
	public void testCreateCacheEntry() {
		mLastFmAdapter.createCacheEntry(FOO, FOOBAR, TIME);
		
		assertTrue(mLastFmAdapter.hasCacheEntry(FOO));
	}
	
	public void testCacheContainsKey() {
		assertFalse(mLastFmAdapter.hasCacheEntry(FOO));
		
		mLastFmAdapter.createCacheEntry(FOO, FOOBAR, TIME);
		
		assertTrue(mLastFmAdapter.hasCacheEntry(FOO));
	}
	
	public void testGetCacheEntryResponse() {
		assertNull("Should return an empty string for non-existing entries.", 
				   mLastFmAdapter.getCacheEntryResponse(FOO));
		
		mLastFmAdapter.createCacheEntry(FOO, FOOBAR, TIME);
		
		assertTrue(mLastFmAdapter.getCacheEntryResponse(FOO).equals(FOOBAR));
	}
	
	public void testDeleteCacheEntry(String key) {
		mLastFmAdapter.createCacheEntry(FOO, FOOBAR, TIME);
		mLastFmAdapter.deleteCacheEntry(FOO);
		
		assertFalse(mLastFmAdapter.hasCacheEntry(FOO));
	}
	
	public void testDeleteCacheEntries(String key) {
		mLastFmAdapter.createCacheEntry(FOO, FOOBAR, TIME);
		mLastFmAdapter.createCacheEntry(BAR, FOOBAR, TIME);
		mLastFmAdapter.deleteCacheEntries();
		
		assertFalse(mLastFmAdapter.hasCacheEntry(FOO));
		assertFalse(mLastFmAdapter.hasCacheEntry(BAR));
	}
	
	public void testIsCacheEntryExpired() {
		assertFalse("Should return false for invalid keys",
					mLastFmAdapter.isCacheEntryExpired(FOOBAR));
		
		mLastFmAdapter.createCacheEntry(FOO, FOOBAR, TIME - ONE_DAY_MILIS);
		mLastFmAdapter.createCacheEntry(BAR, FOOBAR, TIME + ONE_DAY_MILIS);
		
		assertTrue(mLastFmAdapter.isCacheEntryExpired(FOO));
		assertTrue(!mLastFmAdapter.isCacheEntryExpired(BAR));
	}
}
