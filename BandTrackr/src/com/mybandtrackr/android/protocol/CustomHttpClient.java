package com.mybandtrackr.android.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.params.HttpParams;
import org.xml.sax.InputSource;

import com.mybandtrackr.android.App;


import android.util.Log;

public class CustomHttpClient {

	private static final String TAG = CustomHttpClient.class.getSimpleName();

	public static Object executeGet(HttpClient client, String uri, ReturnType returnType) throws URISyntaxException, ClientProtocolException, IOException {
		HttpGet request = new HttpGet();
		request.setURI(new URI(uri));
		return executeBaseRequest(client, request, returnType);
	}

	public static Object executePost(HttpClient client, String uri, HttpParams params, ReturnType returnType) throws ClientProtocolException, IOException {
		HttpPost request = new HttpPost(uri);
		request.setParams(params);
		return executeBaseRequest(client, request, returnType);
	}

	public static Object executeBaseRequest(HttpClient client, HttpRequestBase request, ReturnType returnType) throws ClientProtocolException, IOException {

		Object obj = null;

		HttpResponse httpResponse = client.execute(request);

		if (null != httpResponse) {

			int statusCode = httpResponse.getStatusLine().getStatusCode();

			switch (returnType) {
			case HttpResponse:
				obj = httpResponse;
				break;
			case InputStreamReader:
				if (statusCode == HttpURLConnection.HTTP_OK) {
					obj = new InputStreamReader(httpResponse.getEntity().getContent());
				}
				break;
			case InputSource:
				if (statusCode == HttpURLConnection.HTTP_OK) {
					obj = new InputSource(new InputStreamReader(httpResponse.getEntity().getContent()));
				}
				break;
			case String:
				if (statusCode == HttpURLConnection.HTTP_OK) {
					obj = getString(new InputStreamReader(httpResponse.getEntity().getContent()));
				}
				break;
			default:
				Log.e(TAG, "HttpStatusCode is not OK! (status: " + String.valueOf(statusCode) + "; returnType:" + returnType.name() + ")");
			}
		}
		
		return obj;
	}

	public static String getString(InputStreamReader is) throws IllegalStateException, IOException {

		BufferedReader in = null;
		StringBuffer sb = new StringBuffer("");
		try {
			in = new BufferedReader(is);
			String line = "";
			while ((line = in.readLine()) != null) {
				sb.append(line + App.LINE_SEPARATOR);
			}
			in.close();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					Log.e(TAG, "Error closing BufferReader! " + e.getMessage());
				}
			}
		}
		return sb.toString();
	}

	public enum ReturnType {
		InputStreamReader, InputSource, HttpResponse, String
	}

}
