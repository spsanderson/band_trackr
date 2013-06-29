package com.mybandtrackr.android;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.widget.Toast;

public final class Util {

	/**
	 * Toast makeText utility method, default Toast.LENGTH_LONG
	 * @param message
	 * @param appContext
	 */
	public static void showToast(CharSequence message, Context appContext) {
		showToast(message, appContext, Toast.LENGTH_SHORT);
	}

	/**
	 * Toast makeText utility method
	 * @param message
	 * @param appContext
	 */
	public static void showToast(CharSequence message, Context appContext, int duration) {
		Toast toast = Toast.makeText(appContext, message, duration);
		toast.show();
	}

	/**
	 * Takes a string and converts it to a stream of bytes.
	 * 
	 * <em>Note: this will use the Android's default platform encoding,
	 * which is <strong>UTF-8</strong>.</em>
	 * 
	 * @param string to stream
	 * @return a stream of bytes
	 */
	public static InputStream stringToStream(String string) {
		return new ByteArrayInputStream(string.getBytes());
	}

	/**
	 * Takes a stream and reads it's bytes into a string.
	 *
	 * @param stream to read from
	 * @return a string of the streams content
	 * @throws IOException if an I/O error occurs
	 */
	public static String streamToString(InputStream inputStream) throws IOException {
		StringBuilder sb = getStringBuilderForStream(inputStream);

		return sb.toString();
	}

	private static StringBuilder getStringBuilderForStream(InputStream inputStream) throws IOException {
		InputStreamReader reader = new InputStreamReader(inputStream);
		StringBuilder sb = new StringBuilder(inputStream.available());
		char[] buf = new char[2048];
		int read;
		while ((read = reader.read(buf, 0, buf.length)) != -1) {
			sb.append(buf, 0, read);
		}

		return sb;
	}

	public static boolean isSimilar(String first, String second, int maxLesd) {
		if (null != first && null != second) {
			int levenshteinDistance = StringUtils.getLevenshteinDistance(first, second);
			return levenshteinDistance < maxLesd;
		}
		if (null == first && null == second) {
			return true;
		}
		return false;

	}

}
