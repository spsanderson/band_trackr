package com.mybandtrackr.android.foursquare;

import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class OAuthWebViewClient extends WebViewClient {
	
	private final OAuthActivity mOAuthActivity;

	private static final String TAG = "foursquare.OAuthWebViewClient";
	
	OAuthWebViewClient(OAuthActivity oAuthActivity) {
		mOAuthActivity = oAuthActivity;
	}

	@Override
	public void onReceivedError(WebView view, int errorCode,
			String description, String failingUrl) {
		mOAuthActivity.authenticationError();
	}
	
	private static final String FRAGMENT = "#access_token=";
	private static final String FRAGMENT_ERROR = "error";

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		
		int error = url.indexOf(FRAGMENT_ERROR);
		
		if (error > -1) {
			Log.e(TAG, "Received error URL response from 4sq: " + url);
			mOAuthActivity.authenticationError();
		}
		
		int start = url.indexOf(FRAGMENT);
		
		if (start > -1) {
			String accessToken = url.substring(start + FRAGMENT.length(), url.length());
			
			Log.d(TAG, "Received access token from 4sq: " + accessToken);
			mOAuthActivity.authenticationSuccess(accessToken);
		}
	}
	
}