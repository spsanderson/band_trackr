package com.mybandtrackr.android.test.lastfm;

import static com.mybandtrackr.android.test.Constants.BAR;
import static com.mybandtrackr.android.test.Constants.FOO;
import static com.mybandtrackr.android.test.Constants.FOOBAR;
import static com.mybandtrackr.android.test.Constants.TEST_FILE_PREFIX;

import java.io.IOException;
import java.io.InputStream;

import com.mybandtrackr.android.Util;
import com.mybandtrackr.android.lastfm.DatabaseCache;

import de.umass.lastfm.Caller;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

public final class DatabaseCacheTest extends AndroidTestCase {
	
	private static final long TIME = System.currentTimeMillis();
	private static final long ONE_DAY_MILIS = 1000 * 60 * 60 * 24;
	
	private DatabaseCache mDatabaseCache;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		RenamingDelegatingContext context 
			= new RenamingDelegatingContext(getContext(), TEST_FILE_PREFIX);
		
		mDatabaseCache = new DatabaseCache(context);
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
		mDatabaseCache = null;
	}
	
	/**
	 * The pre-conditions test should ensure that {@link de.umass.lastfm.Caller} is 
	 * configured with the {@link com.mybandtrackr.android.lastfm.DatabaseCache} in
	 * {@link com.mybandtrackr.android.App#onCreate()} 
	 */
	public void testPreConditions() {
		Caller.getInstance().getCache().getClass().equals(DatabaseCache.class);
		assertNotNull(mDatabaseCache);
	}
	
	public void testStore() {
		mDatabaseCache.store(FOO, getTestStream(), TIME);
		assertTrue(mDatabaseCache.contains(FOO));
	}
	
	public void testLoad() throws IOException {
		assertNull("Should have returned null for non-existing entries", 
				   mDatabaseCache.load(FOO));
		
		mDatabaseCache.store(FOO, getTestStream(), TIME);
		
		InputStream in = mDatabaseCache.load(FOO);
		String read = Util.streamToString(in);
		assertEquals(FOOBAR, read);
	}
	
	public void testRemove() {
		mDatabaseCache.store(FOO, getTestStream(), TIME);
		mDatabaseCache.remove(FOO);
		assertFalse(mDatabaseCache.contains(FOO));
	}
	
	public void testIsExpired() {
		mDatabaseCache.store(FOO, getTestStream(), TIME - ONE_DAY_MILIS);
		mDatabaseCache.store(BAR, getTestStream(), TIME + ONE_DAY_MILIS);
		
		assertTrue(mDatabaseCache.isExpired(FOO));
		assertTrue(!mDatabaseCache.isExpired(BAR));
	}

	private InputStream getTestStream() {
		return Util.stringToStream(FOOBAR);
	}
	
}
