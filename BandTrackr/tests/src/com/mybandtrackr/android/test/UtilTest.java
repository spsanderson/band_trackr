package com.mybandtrackr.android.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.mybandtrackr.android.Util;


import junit.framework.TestCase;

public class UtilTest extends TestCase {
	
	private static String TEST = "Very Young Oragutans Could Grow Bananas Rather Well";

	public void testStringToStream() throws IOException {
		InputStream in = Util.stringToStream(TEST);
		String read = new BufferedReader(new InputStreamReader(in)).readLine();
		
		assertEquals(TEST, read);
	}

	public void testStreamToString() throws IOException {
		String read = Util.streamToString(Util.stringToStream(TEST));
		
		assertEquals(TEST, read);
	}

}
